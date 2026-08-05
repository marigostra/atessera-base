// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.io.*;
import org.apache.logging.log4j.*;

import org.apache.velocity.app.*;
import org.apache.velocity.*;
import org.apache.velocity.runtime.resource.loader.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.resource.util.*;

import atessera.json.*;
import atessera.markdown.*;
import atessera.templ.*;

import static java.util.Objects.*;
import static java.lang.Character.*;
import static java.util.stream.Collectors.*;
import static atessera.util.TextUtils.*;
import static atessera.util.LatexUtils.*;

/**
 * Translates a {@link PublicationContent} into a compilable LaTeX document.
 *
 * <p>This is the primary translator for producing PDF output. It performs
 * the following steps:</p>
 *
 * <ol>
 *   <li><strong>Section translation</strong> &mdash; iterates over all
 *       sections of the publication and converts each one into a
 *       {@link PublicationTemplate.Section} according to its
 *       {@link PublicationContent.SectionType}:
 *       <ul>
 *         <li>{@code MARKDOWN} &mdash; parsed through the package-private
 *             {@code Markdown} renderer (which extends
 *             {@link atessera.markdown.LatexTarget LatexTarget}) and
 *             converted to LaTeX.</li>
 *         <li>{@code LATEX} &mdash; passed through unchanged.</li>
 *         <li>{@code LISTING} &mdash; wrapped as a source code listing
 *             with a file reference, label, caption, and language hint.</li>
 *         <li>{@code EQUATION} &mdash; wrapped as a numbered equation
 *             with an optional label.</li>
 *         <li>{@code PLANTUML}, {@code METAPOST}, {@code GNUPLOT},
 *             {@code GRAPHVIZ_DOT} &mdash; treated as images; the
 *             translator expects a corresponding {@code .pdf} file to be
 *             produced by the compilation layer.</li>
 *       </ul>
 *   </li>
 *   <li><strong>Bibliography extraction</strong> &mdash; uses
 *       {@link BiblioExtractor} to scan all Markdown sections for
 *       {@link atessera.markdown.blocks.BibItem BibItem} nodes, collects
 *       them into a map, and sorts them alphabetically by the
 *       letters-and-digits content of their formatted text.</li>
 *   <li><strong>Abstract processing</strong> &mdash; if the publication
 *       has an abstract section of type {@code MARKDOWN}, it is parsed
 *       and included before the main content.</li>
 *   <li><strong>Template assembly</strong> &mdash; all collected data is
 *       fed into a {@link PublicationTemplate}, which renders the final
 *       LaTeX source via Apache Velocity.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 *
 * <p>Instances of this class are <strong>not</strong> thread-safe. Each
 * translation should use its own instance.</p>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * EngineFactory factory = new EngineFactory(templates);
 * PublicationContent content = PublicationContent.fromJson(jsonString);
 *
 * TexTranslator translator = new TexTranslator(factory, content);
 * List<String> latexSource = translator.translate();
 * }</pre>
 *
 * @see PublicationContent
 * @see PublicationTemplate
 * @see BiblioExtractor
 * @see atessera.markdown.LatexTarget
 */
public final class TexTranslator
{
    static private final Logger log = LogManager.getLogger();

    /** Expected file extension for pre-compiled images. */
    static private final String IMAGES_EXT = ".pdf";

    /** Factory for obtaining Velocity engine instances. */
    final EngineFactory engineFactory;

    /** The publication content to translate. */
    final PublicationContent publ;

    /** Package-private Markdown-to-LaTeX renderer. */
    private final Markdown markdown = new Markdown();

    /**
     * Constructs a new translator.
     *
     * @param engineFactory the Velocity engine factory; must not be
     *                      {@code null}
     * @param publ          the publication content to translate; must not
     *                      be {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public TexTranslator(EngineFactory engineFactory, PublicationContent publ)
    {
	this.engineFactory = requireNonNull(engineFactory, "engineFactory can't be null");
	this.publ = requireNonNull(publ, "publ can't be null");
    }

    /**
     * Performs the full translation and returns the resulting LaTeX
     * source as a list of lines.
     *
     * <p>This method orchestrates section translation, bibliography
     * extraction, abstract processing, and final template rendering.</p>
     *
     * @return the generated LaTeX source, one string per line; never
     *         {@code null}
     * @throws IllegalStateException if the abstract section has an
     *         unsupported type
     */
    public List<String> translate()
    {
	final var templ = new PublicationTemplate(engineFactory);
	templ.setHeader(publ);
	templ.setSections(translateSections());
	final var biblio = new ArrayList<>(new BiblioExtractor().extract(publ.getSections())
					   .entrySet()
					   .stream()
					   .toList());
						   log.info("{} bibliography entries collected", biblio.size());
	Collections.sort(biblio, (e1, e2)->{
		return onlyLettersAndDigits(e1.getValue()).toLowerCase().compareTo(onlyLettersAndDigits(e2.getValue()).toLowerCase());
	    });
	templ.setBiblio(biblio);	    
	if (publ.getAbs() != null && publ.getAbs().getType() != null)
	{
	    if (publ.getAbs().getType() == PublicationContent.SectionType.MARKDOWN)
	    {
		if (publ.getAbs().getSource() != null && !publ.getAbs().getSource().isEmpty())
		{
		    final var s = new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, markdown.parse(publ.getAbs().getSource().stream().collect(joining("\n"))), "");
		    templ.setAbstract(s.getText());
		} else
		    templ.setAbstract( "");
	    } else
		throw new IllegalStateException("Unsupported abstract type: " + publ.getAbs().getType());
	} else
	    templ.setAbstract("");
	final StringWriter w = new StringWriter();
	templ.render(w );
	return Arrays.asList(w.toString().split("\n", -1));
    }

    /**
     * Converts all publication sections into template-ready section
     * objects.
     *
     * <p>Each section is mapped according to its
     * {@link PublicationContent.SectionType}. Sections with no source or
     * empty source produce a blank text section. Unrecognized section
     * types produce a placeholder with a {@code FIXME} marker.</p>
     *
     * @return the list of translated sections; never {@code null}, but
     *         may be empty
     */
    List<PublicationTemplate.Section> translateSections()
    {
	if (publ.getSections() == null)
	    return Collections.emptyList();
	return publ.getSections().stream()
	.map(sect -> {
		switch(requireNonNullElse(sect.getType(), PublicationContent.SectionType.MARKDOWN))
		{
		case MARKDOWN:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, markdown.parse(sect.getSource().stream().collect(joining("\n"))), "");
		case LATEX:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT,
							   sect.getSource().stream().collect(joining("\n")), "");
		case LISTING:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", ""); else
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.LISTING, sect.getId() + ".lst", "", sect.getLabel(), translateCaption(sect.getCaption()), sect.getListingLang() );
		case EQUATION:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.EQUATION,
							   sect.getSource().stream()
							   .map(s -> s.trim())
							   .filter(s -> !s.isEmpty())
							   .collect(joining("\n")), "",
							   sect.getLabel(), "", null);
		case PLANTUML:
		case METAPOST:
		case GNUPLOT:
		case GRAPHVIZ_DOT:
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.IMAGE, "",
							   sect.getId() + IMAGES_EXT,
							   sect.getLabel(), translateCaption(sect.getCaption()), null);
		default:
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\nFIXME! Section " + sect.getType().toString() + "\n\n", "");
		}
	    }).toList();
    }

    /**
     * Translates a caption (list of Markdown lines) into a single LaTeX
     * string.
     *
     * @param caption the caption lines in Markdown; may be {@code null}
     *                or empty
     * @return the translated caption, or an empty string if the input is
     *         {@code null} or empty
     */
    private String translateCaption(List<String> caption)
    {
	if (caption == null || caption.isEmpty())
	    return "";
	return markdown.parse(caption).stream().collect(joining("\n"));
    }

    /**
     * Extracts only letters and digits from the given text, discarding
     * all other characters. Used for sorting bibliography entries in a
     * locale-independent manner.
     *
     * @param text the input text; may be {@code null}
     * @return a string containing only the letters and digits from the
     *         input; never {@code null}
     */
    private String onlyLettersAndDigits(String text)
    {
	final var b = new StringBuilder();
	for(int i = 0;i < text.length();i++)
	{
	    final var ch = text.charAt(i);
	    if (isLetter(ch) || isDigit(ch))
		b.append(ch);
	}
	return new String(b);
    }
}