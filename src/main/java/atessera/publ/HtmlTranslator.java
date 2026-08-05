// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

import atessera.json.*;
import atessera.markdown.*;
import atessera.markdown.blocks.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.json.PublicationContent.*;
import static atessera.util.IdStr.*;

/**
 * Translates a {@link PublicationContent} into a set of HTML chapter
 * files with resolved cross-references.
 *
 * <p>This translator produces HTML output suitable for web viewing. It
 * employs a <strong>two-pass algorithm</strong> to resolve
 * cross-references and auto-numbering before the final rendering:</p>
 *
 * <ol>
 *   <li><strong>First pass ({@link #fillRefs()})</strong> &mdash; scans
 *       all sections to collect labels and assign hierarchical numbers
 *       (chapter, section, subsection, equation, listing, figure).
 *       Builds internal maps:
 *       <ul>
 *         <li>{@code refs} &mdash; maps a label to its human-readable
 *             number (e.g. {@code "1.2"}).</li>
 *         <li>{@code refIds} &mdash; maps a label to a random HTML
 *             anchor id.</li>
 *         <li>{@code refChapNums} &mdash; maps a label to the chapter
 *             number where it appears.</li>
 *         <li>{@code biblio} &mdash; maps a citation key to its
 *             formatted bibliography text.</li>
 *       </ul>
 *   </li>
 *   <li><strong>Second pass ({@link #translate()})</strong> &mdash;
 *       renders each Markdown section through an {@link HtmlTarget}
 *       that resolves references using the maps built in the first
 *       pass. The rendered content is split at chapter boundaries
 *       (determined by level-1 headings) and stored in the
 *       {@link #output} map, keyed by file name (e.g.
 *       {@code "Introduction.html"}, {@code "Chapter1.html"}).</li>
 * </ol>
 *
 * <h2>Auto-numbering</h2>
 *
 * <p>Headings receive hierarchical numbers in Russian academic style:</p>
 * <ul>
 *   <li>Level 1 &rarr; <em>Глава~1.</em>, <em>Глава~2.</em>, ...</li>
 *   <li>Level 2 &rarr; <em>1.1</em>, <em>1.2</em>, ...</li>
 *   <li>Level 3 &rarr; <em>1.1.1</em>, <em>1.1.2</em>, ...</li>
 * </ul>
 *
 * <p>Equations, listings, and figures within a chapter are numbered
 * relative to that chapter (e.g. equation 1.1, 1.2, ...).</p>
 *
 * <h2>Cross-references</h2>
 *
 * <p>Regular references ({@code [label]}) are rendered as HTML links
 * ({@code <a href="ChapterN.html#anchor">number</a>}). Page references
 * are not yet supported and produce a placeholder.</p>
 *
 * <h2>Thread safety</h2>
 *
 * <p>Instances of this class are <strong>not</strong> thread-safe. Each
 * translation should use its own instance. The class implements
 * {@link AutoCloseable} for consistency, though no resources currently
 * require cleanup.</p>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * PublicationContent content = PublicationContent.fromJson(jsonString);
 * try (HtmlTranslator translator = new HtmlTranslator(content)) {
 *     translator.translate();
 *     // Access results via translator.output
 * }
 * }</pre>
 *
 * @see PublicationContent
 * @see HtmlTarget
 * @see TexTranslator
 */
public final class HtmlTranslator implements AutoCloseable
{
    /** Length of generated HTML anchor identifiers. */
    static final int ID_LEN = 10;

    /**
     * The HTML renderer used for the second pass. Overrides several
     * methods to inject resolved references, citation links, math
     * rendering, and auto-numbered headings.
     */
    private final HtmlTarget markdown = new HtmlTarget(EnumSet.allOf(HtmlTarget .Features.class)){
			    @Override public String escape(String text) { return super.escape(text).replaceAll("\u00A0", "&#160;");}
	    @Override public String render(AdvImageDefinition image) { return "IMAGE"; }
		@Override public String render(Reference ref) { return HtmlTranslator.this.getReference(ref); }
				@Override public String render(CiteReference citeRef) { return HtmlTranslator.this.getCiteRef(citeRef); }
				@Override public String render(MathDefinition math) { return HtmlTranslator.this.getMath(math); }
		@Override public String onHeading(int level, StringBuilder b) { return HtmlTranslator.this.getHeading(level); }
	};

    /** The publication content being translated. */
    final PublicationContent publ;

    /**
     * Accumulates the rendered HTML output. Keys are chapter file names
     * (e.g. {@code "Introduction.html"}, {@code "Chapter1.html"}),
     * values are lists of HTML lines.
     */
    final Map<String, List<String>> output = new HashMap<>();

    /**
     * Maps a label to its human-readable number (e.g. {@code "1.2"}).
     * Populated during the first pass.
     */
    final Map<String, String> refs = new HashMap<>();

    /**
     * Maps a label to its HTML anchor id (a random string).
     * Populated during the first pass.
     */
    final Map<String, String> refIds = new HashMap<>();

    /**
     * Maps a citation key to its formatted bibliography text.
     * Populated during the first pass.
     */
    final Map<String, String> biblio = new HashMap<String, String>();

    /**
     * Maps a label to the chapter number where it appears.
     * Populated during the first pass.
     */
    final Map<String, Integer> refChapNums = new HashMap<>();

    /** Running counters for heading auto-numbering (second pass). */
    private int h1Num = 0, h2Num = 0, h3Num = 0;

    /**
     * Constructs a new HTML translator.
     *
     * @param publ the publication content to translate; must not be
     *             {@code null}
     * @throws NullPointerException if {@code publ} is {@code null}
     */
    public HtmlTranslator(PublicationContent publ) throws IOException
    {
	requireNonNull(publ, "publ can't be null");
	this.publ = publ;
    }

    /**
     * Performs the full two-pass translation, populating the
     * {@link #output} map.
     *
     * <p>After this method returns, {@link #output} contains the
     * rendered HTML for each chapter, keyed by file name.</p>
     *
     * @throws IOException          if an I/O error occurs
     * @throws IllegalArgumentException if an unknown reference or citation
     *         key is encountered
     */
    void translate() throws IOException
    {
	refs.clear();
	fillRefs();
	var chap = new ArrayList<String>();
	int chapNum = 0;
	for(final Section s: requireNonNullElse(publ.getSections(), new ArrayList<Section>()))
	{
	    switch(requireNonNullElse(s.getType(), SectionType.MARKDOWN))
	    {
	    case MARKDOWN: {
		final var splits = new ArrayList<Integer>();
		final var text = requireNonNullElse(s.getSource(), new ArrayList<String>())
		.stream()
		.collect(joining("\n"));
		final var rendered = markdown.parse(text, splits);
		int prevPos = 0;
		for(final int p: splits)
		{
		    		    final var chunk = rendered.substring(prevPos, p);
		    chap.addAll(Arrays.asList(chunk.split("\n")));
		    output.put(getChapterFileName(chapNum), chap );
		    chapNum++;
		    chap = new ArrayList<>();
		    prevPos = p;
		}
				    		    final var chunk = rendered.substring(prevPos);
		    chap.addAll(Arrays.asList(chunk.split("\n")));


		break;
	    }
	}

    }
	output.put("chap" + chapNum, chap);
    }

    /**
     * Resolves a cross-reference to an HTML link.
     *
     * <p>Regular references produce an anchor tag linking to the
     * referenced chapter and anchor. Page references are not yet
     * supported.</p>
     *
     * @param ref the reference node; must not be {@code null} and must
     *            have a non-null type and ref
     * @return the HTML representation of the reference
     * @throws IllegalArgumentException if the reference type is
     *         {@code null}, the ref key is {@code null}, or the
     *         referenced label is unknown
     */
    String getReference(Reference ref)
    {
	if (ref.getType() == null || ref.getRef() == null)
	    throw new IllegalArgumentException("Illegal ref object");
	switch(ref.getType())
	{
	case REGULAR: {
	    	    final var id = refIds.get(ref.getRef().trim());
		    	    final var chap = refChapNums.get(ref.getRef().trim());
	    final var res = refs.get(ref.getRef().trim());
	    if (res == null)
		throw new IllegalArgumentException("Unknown reference: " + ref.getRef());
	    if (id != null && chap != null)
		return "<a href=\"" + getChapterFileName(chap.intValue()) + "#" + id + "\">" + res + "</a>";
	    return res;
	}
	case PAGE:
	    return "!СТРАНИЦА ОТСУТСТВУЕТ!";
	}
	return "";
    }

    /**
     * Validates a citation reference against the bibliography map.
     *
     * <p>Each citation key (there may be multiple, separated by commas)
     * is checked for presence in the {@link #biblio} map. If a key is
     * missing, an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param citeRef the citation reference node; must not be
     *                {@code null}
     * @return an empty string (the actual rendering of the citation is
     *         handled elsewhere)
     * @throws IllegalArgumentException if a citation key is unknown
     */
    String getCiteRef(CiteReference citeRef)
    {
	if (citeRef.getRef() == null)
	    throw new IllegalArgumentException("Illegal cite ref object");
	for(final var ref : citeRef.getRef().split(",", -1))
	{
    	    final var res = biblio.get(ref.trim());
	    if (res == null)
		throw new IllegalArgumentException("Unknown cite reference: " + citeRef.getRef());
	}
	return "";
    }

    /**
     * Renders inline math as HTML-escaped text.
     *
     * @param math the math definition; must not be {@code null}
     * @return the HTML-escaped math text
     */
    String getMath(MathDefinition math)
    {
	if (math.getText() == null)
	    return "!ВНУТРЕННЯЯ ОШИБКА!";
	return markdown.escape(math.getText());
    }

    /**
     * Produces an auto-numbered heading prefix based on the heading
     * level and the current counter state.
     *
     * <p>Counters are reset appropriately when a higher-level heading
     * is encountered:</p>
     * <ul>
     *   <li>Level 1 increments the chapter counter and resets section
     *       and subsection counters.</li>
     *   <li>Level 2 increments the section counter and resets the
     *       subsection counter.</li>
     *   <li>Level 3 increments the subsection counter.</li>
     * </ul>
     *
     * @param level the heading level (1, 2, or 3)
     * @return the heading number prefix (e.g. {@code "Глава~1. "},
     *         {@code "1.2 "}, {@code "1.2.3 "})
     * @throws RuntimeException if the heading level is not 1, 2, or 3
     */
    String getHeading(int level)
    {
	switch(level)
	{
	case 1:
	    h1Num++;
	    h2Num = 0;
	    h3Num = 0;
	    return "Глава~" + h1Num + ". ";
	case 2:
	    h2Num++;
	    h3Num = 0;
	    return String.valueOf(h1Num) + "." + String.valueOf(h2Num) + " ";
	case 3:
	    h3Num++;
	    	    return String.valueOf(h1Num) + "." + String.valueOf(h2Num) + "." + h3Num + " ";
	default:
	    throw new RuntimeException("Unsupported heading level: " + level);
	}
    }

    /**
     * First pass: scans all sections to collect labels, assign numbers,
     * and build the reference maps.
     *
     * <p>This method walks through every section and uses a dedicated
     * {@link HtmlTarget} instance to extract labels from headings,
     * math blocks, and bibliography items. Non-Markdown sections
     * (listings, equations, MetaPost, GNUPlot) are also processed to
     * assign numbers and register their labels.</p>
     */
    void fillRefs()
    {
	final AtomicInteger
	h1Num = new AtomicInteger(0),
	h2Num = new AtomicInteger(0),
	h3Num = new AtomicInteger(0),
	eqNum = new AtomicInteger(1),
	listNum = new AtomicInteger(1),
	pictNum = new AtomicInteger(1);
	final var m = new HtmlTarget(EnumSet.allOf(HtmlTarget.Features.class)){
		@Override public String onHeading(int level, StringBuilder b)
		{
		    if (level == 1)
		    {
		    h1Num.set(h1Num.get() + 1);
		    h2Num.set(0);
		    h3Num.set(0);
		    eqNum.set(1);
		    listNum.set(1);
		    pictNum.set(1);
		    return null;
		    }
		    if (level == 2)
		    {
			h2Num.set(h2Num.get() + 1);
			return null;
		    }
		    		    if (level == 3)
		    {
			h3Num.set(h3Num.get() + 1);
			return null;
		    }
				    return null;
		}
		@Override public String render(MathBlockDefinition math)
		{
		    if (requireNonNullElse(math.getLabel(), "").isEmpty())
			return "";
		    refs.put(math.getLabel().trim(), h1Num.toString() + "." + eqNum.toString());
									refIds.put(math.getLabel().trim(), getRandomId(ID_LEN));
						refChapNums.put(math.getLabel().trim(), Integer.valueOf(h1Num.get()));
						eqNum.set(eqNum.get() + 1);
		    return "";
		}
		@Override public String render(Label label)
		{
		    if (requireNonNullElse(label.getLabel(), "").trim().isEmpty())
			return "";
		    final String v;
		    if (h2Num.get() == 0 && h3Num.get() == 0)
v = h1Num.toString(); else
		    		    if (h3Num.get() == 0)
v = h1Num.toString() + "." + h2Num.toString(); else
v = h1Num.toString() + "." + h2Num.toString() + "." + h3Num.toString();
				    			refs.put(label.getLabel().trim(), v);
						refIds.put(label.getLabel().trim(), v);
						refChapNums.put(label.getLabel().trim(), Integer.valueOf(h1Num.get()));
			return "";
		}
	    };

		for(final Section s: requireNonNullElse(publ.getSections(), new ArrayList<Section>()))
	{
	    switch(requireNonNullElse(s.getType(), SectionType.MARKDOWN))
	    {
	    case MARKDOWN:
		m.parse(s.getSource());
		break;
	    case LISTING:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + listNum.toString());
		listNum.set(listNum.get() + 1);
		break;
			    case METAPOST:
	    case GNUPLOT:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + pictNum.toString());
		pictNum.set(pictNum.get() + 1);
		break;
			    case EQUATION:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + eqNum.toString());
		eqNum.set(eqNum.get() + 1);
		break;
	    }
	}
		biblio.putAll(m.biblio);
    }

    /**
     * Returns the file name for a chapter given its zero-based index.
     *
     * <p>Chapter 0 is named {@code "Introduction.html"}; all subsequent
     * chapters are named {@code "ChapterN.html"} where N is the chapter
     * number.</p>
     *
     * @param chapNum the zero-based chapter index
     * @return the file name (e.g. {@code "Introduction.html"} or
     *         {@code "Chapter3.html"})
     */
    String getChapterFileName(int chapNum)
    {
	if (chapNum == 0)
	    return "Introduction.html";
	return "Chapter" + chapNum + ".html";
    }

    /**
     * No-op cleanup method required by {@link AutoCloseable}.
     */
    @Override public void close()
    {
    }
}