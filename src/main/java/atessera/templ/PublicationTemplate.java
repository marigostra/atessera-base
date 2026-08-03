// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import java.io.*;
import lombok.*;

import org.apache.velocity.*;
import org.apache.velocity.exception.*;
import atessera.json.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.util.LatexUtils.*;

/**
 * Velocity template for generating LaTeX source of a complete publication.
 *
 * <p>This class populates the {@code publication.vm} template with metadata
 * (title, authors, date, etc.) and content sections, producing a
 * compilable LaTeX document. The template is designed to be compiled with
 * {@link atessera.comp.PdfLatex PdfLatex}.</p>
 *
 * <p>Typical usage:</p>
 *
 * <pre>{@code
 * PublicationTemplate t = new PublicationTemplate(factory);
 * t.setHeader(publicationContent);
 * t.setAbstract(abstractText);
 * t.setSections(sections);
 * t.setBiblio(bibliography);
 * List<String> latexSource = t.renderToStringList();
 * }</pre>
 *
 * @see Base
 * @see atessera.comp.PdfLatex
 * @see PublicationContent
 */
public final class PublicationTemplate extends Base
{
    /**
     * Constructs a new publication template.
     *
     * @param engineFactory the factory for obtaining a Velocity engine;
     *                      must not be {@code null}
     * @throws ResourceNotFoundException if {@code publication.vm} cannot
     *         be loaded
     */
    public PublicationTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "publication.vm");
    }

    /**
     * Sets the publication metadata from a {@link PublicationContent} object.
     *
     * <p>Populates template variables: {@code TITLE}, {@code TITLE_CAP},
     * {@code SUBTITLE}, {@code AUTHORS}, {@code BOOK_TYPE},
     * {@code TITLE_PAGE_TOP_NOTE}, {@code TITLE_PAGE_BOTTOM_NOTE},
     * {@code DATE}, {@code LOCATION}, {@code TOC_BEGIN}, and
     * {@code TOC_END}.</p>
     *
     * @param publ the publication metadata; must not be {@code null}
     */
    public void setHeader(PublicationContent publ)
    {
	context.put("TITLE", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim());
	context.put("TITLE_CAP", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim().toUpperCase());
	context.put("SUBTITLE", escapeRelaxed(requireNonNullElse(publ.getSubtitle(), "")).trim());
	context.put("AUTHORS", escapeRelaxed(requireNonNullElse(publ.getAuthors(), "")).trim());
	context.put("BOOK_TYPE", escapeRelaxed(requireNonNullElse(publ.getBookType(), "")).trim());
	context.put("TITLE_PAGE_TOP_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageTopNote(), "")).trim());
	context.put("TITLE_PAGE_BOTTOM_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageBottomNote(), "")).trim());
	context.put("DATE", escape(requireNonNullElse(publ.getDate(), "")).trim());
	context.put("LOCATION", escapeRelaxed(requireNonNullElse(publ.getLocation(), "")).trim());
	//FIXME:
		context.put("TOC_BEGIN", "false");
	context.put("TOC_END", "true");
    }

    /**
     * Sets the content sections of the publication.
     *
     * @param sections the list of sections; must not be {@code null}
     * @return this template (for fluent chaining)
     */
    public PublicationTemplate setSections(List<Section> sections)
    {
	context.put("SECTIONS", requireNonNull(sections, "sections can't be null"));
	return this;
    }

    /**
     * Sets the abstract text of the publication.
     *
     * @param abs the abstract text (may be {@code null})
     * @return this template (for fluent chaining)
     */
    public PublicationTemplate setAbstract(String abs)
    {
	context.put("ABSTRACT", abs);
	return this;
    }

    /**
     * Sets the bibliography entries.
     *
     * @param biblio a list of key-value pairs where keys are citation keys
     *               and values are the formatted bibliography text; must
     *               not be {@code null}
     * @return this template (for fluent chaining)
     */
    public PublicationTemplate setBiblio(List<Map.Entry<String, String>> biblio)
    {
	context.put("BIBLIO", biblio.stream()
	.map(e -> {
		return "\\bibitem{" + escapeRelaxed(e.getKey()) + "}\n" +
		escapeRelaxed(e.getValue()) + "\n";
	    }).collect(joining("\n")));
	return this;
    }
    
    /**
     * Represents a section within a publication.
     *
     * <p>Each section has a type ({@code TEXT}, {@code IMAGE},
     * {@code EQUATION}, or {@code LISTING}) and associated content such
     * as text, an image name, a label, a caption, and a language hint
     * (for listings).</p>
     */
    @Data
    static public final class Section
    {
	/** Enumerates the possible section types. */
	public enum Type
	{
	    /** Plain text or Markdown content. */
	    TEXT,
	    /** An embedded image. */
	    IMAGE,
	    /** A mathematical equation. */
	    EQUATION,
	    /** A source code listing. */
	    LISTING
	};

	final String type, text, imageName, label, caption, lang;
	
	/**
	 * Constructs a fully specified section.
	 *
	 * @param type      the section type; must not be {@code null}
	 * @param text      the section text (may be {@code null})
	 * @param imageName the image file name (may be {@code null})
	 * @param label     the LaTeX label; will be escaped (may be
	 *                  {@code null})
	 * @param caption   the section caption (may be {@code null})
	 * @param lang      the language for listings; will be escaped (may
	 *                  be {@code null})
	 */
	public Section(Type type, String text ,String imageName,
		String label, String caption, String lang)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.text = requireNonNullElse(text, "");
	    this.imageName = requireNonNullElse(imageName, "");
	    this.label = escapeRelaxed(requireNonNullElse(label, ""));
	    this.caption = requireNonNullElse(caption, "");
	    this.lang = escapeRelaxed(requireNonNullElse(lang, ""));
	}

	/**
	 * Constructs a section with only type, text, and image name.
	 *
	 * @param type      the section type; must not be {@code null}
	 * @param text      the section text (may be {@code null})
	 * @param imageName the image file name (may be {@code null})
	 */
	public Section(Type type, String text ,String imageName)
	{
	    this(type, text, imageName, null, null, null);
	}
    }
}
