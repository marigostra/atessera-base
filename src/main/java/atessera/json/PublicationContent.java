// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.json;

import java.util.*;
import lombok.*;
import com.google.gson.*;

import static java.util.Objects.*;

/**
 * Structured description of a publication (book, paper, thesis, etc.).
 *
 * <p>This class captures both the bibliographic metadata and the content
 * structure of a publication. It is designed to be serialized to and from
 * JSON via {@link #toJson(PublicationContent)} and
 * {@link #fromJson(String)}, and serves as the primary input for
 * {@link atessera.templ.PublicationTemplate}.</p>
 *
 * <h2>Supported publication types</h2>
 *
 * <p>Defined by the {@link Type} enum:</p>
 * <ul>
 *   <li>{@link Type#GRADUATION_WORK GRADUATION_WORK} &mdash; graduation
 *       qualification work</li>
 *   <li>{@link Type#COURSE_WORK COURSE_WORK} &mdash; course project/paper</li>
 *   <li>{@link Type#PAPER PAPER} &mdash; conference or journal paper</li>
 *   <li>{@link Type#THESIS THESIS} &mdash; master's or PhD thesis</li>
 *   <li>{@link Type#BOOK BOOK} &mdash; a full book</li>
 * </ul>
 *
 * <h2>Supported section types</h2>
 *
 * <p>Each {@link Section} within the publication has a
 * {@link SectionType} that determines how its source text is processed:</p>
 * <ul>
 *   <li>{@link SectionType#MARKDOWN MARKDOWN} &mdash; text in Markdown
 *       format</li>
 *   <li>{@link SectionType#LATEX LATEX} &mdash; raw LaTeX source</li>
 *   <li>{@link SectionType#EQUATION EQUATION} &mdash; a mathematical
 *       equation</li>
 *   <li>{@link SectionType#TABLE TABLE} &mdash; a table</li>
 *   <li>{@link SectionType#LISTING LISTING} &mdash; source code listing</li>
 *   <li>{@link SectionType#METAPOST METAPOST} &mdash; MetaPost figure</li>
 *   <li>{@link SectionType#GNUPLOT GNUPLOT} &mdash; GNUPlot chart</li>
 *   <li>{@link SectionType#PLANTUML PLANTUML} &mdash; PlantUML diagram</li>
 *   <li>{@link SectionType#GRAPHVIZ_DOT GRAPHVIZ_DOT} &mdash; Graphviz
 *       (dot layout)</li>
 *   <li>{@link SectionType#GRAPHVIZ_NEATO GRAPHVIZ_NEATO} &mdash; Graphviz
 *       (neato layout)</li>
 *   <li>{@link SectionType#GRAPHVIZ_TWOPI GRAPHVIZ_TWOPI} &mdash; Graphviz
 *       (twopi layout)</li>
 *   <li>{@link SectionType#GRAPHVIZ_CIRCO GRAPHVIZ_CIRCO} &mdash; Graphviz
 *       (circo layout)</li>
 * </ul>
 *
 * @see atessera.templ.PublicationTemplate
 */
@Data
@NoArgsConstructor
public final class PublicationContent
{
    /** Shared Gson instance for JSON serialization. */
    static private final Gson gson = new Gson();

    /**
     * Immutable set of all publication types. Convenient for validation
     * and iteration.
     */
    static public final Set<Type> ALL_TYPES = EnumSet.allOf(Type.class);

    /**
     * Immutable set of all section types. Convenient for validation and
     * iteration.
     */
    static public final Set<SectionType> ALL_SECTION_TYPES = EnumSet.allOf(SectionType.class);

    /**
     * Enumerates the types of publications supported by Alpha Tessera.
     */
    public enum Type
    {
        /** Graduation qualification work. */
        GRADUATION_WORK,
        /** Course project or course paper. */
        COURSE_WORK,
        /** Conference or journal paper. */
        PAPER,
        /** Master's or PhD thesis. */
        THESIS,
        /** A full book. */
        BOOK
    };

    /**
     * Enumerates the types of content that a section may contain.
     * Each type implies a different processing pipeline (e.g. Markdown
     * goes through the Markdown parser, MetaPost goes through the
     * {@code Metapost} compiler, etc.).
     */
    public enum SectionType
    {
        /** Text in Markdown format. */
        MARKDOWN,
        /** Raw LaTeX source (passed through as-is). */
        LATEX,
        /** A mathematical equation. */
        EQUATION,
        /** A table. */
        TABLE,
        /** A source code listing. */
        LISTING,
        /** A MetaPost figure. */
        METAPOST,
        /** A GNUPlot chart. */
        GNUPLOT,
        /** A PlantUML diagram. */
        PLANTUML,
        /** A Graphviz diagram using the dot layout engine. */
        GRAPHVIZ_DOT,
        /** A Graphviz diagram using the neato layout engine. */
        GRAPHVIZ_NEATO,
        /** A Graphviz diagram using the twopi layout engine. */
        GRAPHVIZ_TWOPI,
        /** A Graphviz diagram using the circo layout engine. */
        GRAPHVIZ_CIRCO
    };

    /** The overall type of this publication. */
    private Type type;

    /** Output file name (e.g. {@code "paper.pdf"}). */
    private String outFile;

    /** Publication title. */
    private String title;

    /** Publication subtitle (optional). */
    private String subtitle;

    /** Authors string (e.g. {@code "Ivanov I. I., Petrov P. P."}). */
    private String authors;

    /** Publication date (free-form string). */
    private String date;

    /** Publication location (city, country). */
    private String location;

    /**
     * Where or how the work was published (mostly relevant for papers and
     * proceedings).
     */
    private String published;

    /** Organization associated with the publication. */
    private String org;

    /** Keywords for indexing. */
    private String keywords;

    /** ISBN identifier. */
    private String isbn;

    /** Copyright notice. */
    private String copyright;

    /** UDK (Universal Decimal Classification) index. */
    private String udk;

    /** BBK (Library-Bibliographic Classification) index. */
    private String bbk;

    /** Whether to include a table of contents at the beginning. */
    private boolean tocBegin;

    /** Whether to include a table of contents at the end. */
    private boolean tocEnd;

    /** Student group identifier (for academic works). */
    private String stGroup;

    /** Specialty number (for academic works). */
    private String spNum;

    /** Specialty name (for academic works). */
    private String spName;

    /** Supervisor name (for academic works). */
    private String svName;

    /** Supervisor academic degree. */
    private String svDegree;

    /** Supervisor academic rank. */
    private String svRank;

    /** Optional note displayed at the top of the title page. */
    private String titlePageTopNote;

    /** Optional note displayed at the bottom of the title page. */
    private String titlePageBottomNote;

    /** Type of book binding or format (e.g. {@code "hardcover"}). */
    private String bookType;

    /** Number of text columns per page. */
    private int numCols;

    /** Page width (in the units expected by the LaTeX document class). */
    private int pageWidth;

    /** Page height (in the units expected by the LaTeX document class). */
    private int pageHeight;

    /** Top page margin (in the units expected by the LaTeX document class). */
    private int pageTopMargin;

    /** Left page margin (in the units expected by the LaTeX document class). */
    private int pageLeftMargin;

    /** Bottom page margin (in the units expected by the LaTeX document class). */
    private int pageBottomMargin;

    /** Right page margin (in the units expected by the LaTeX document class). */
    private int pageRightMargin;

    /** Abstract section (displayed before the main content). */
    private Section abs;

    /** Issue/edition information section. */
    private Section issueInfo;

    /** The list of content sections that make up the publication body. */
    private List<Section> sections;

    /**
     * Extension attributes map. Allows storing arbitrary key-value pairs
     * without modifying the class structure. Keys and values are
     * free-form strings.
     */
    private Map<String, String> xAttr;

    /**
     * Represents a single content section within a publication.
     *
     * <p>Each section has a {@link SectionType} that determines how its
     * source text is interpreted, and may carry additional metadata such
     * as an identifier, a LaTeX label, a caption, and a language hint
     * (for listings). The {@code alternativeSource} field provides a
     * fallback representation for environments that cannot process the
     * primary source type.</p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Section
    {
        /**
         * The type of content in this section. Determines which
         * processing pipeline is used.
         */
        private SectionType type;

        /**
         * Unique identifier for this section (used for cross-referencing).
         */
        private String id;

        /**
         * LaTeX label for referencing this section (e.g. for
         * {@code \\label} and {@code \\ref} commands).
         */
        private String label;

        /**
         * Programming language hint for source code listings (e.g.
         * {@code "java"}, {@code "python"}). Used for syntax highlighting.
         */
        private String listingLang;

        /**
         * Primary source text of the section. Interpreted according to
         * the section's {@link #type}.
         */
        private List<String> source;

        /**
         * Alternative (fallback) representation of the section content.
         * Used when the target output format does not support the primary
         * source type. For example, a Markdown section may carry a plain
         * text fallback.
         */
        private List<String> alternativeSource;

        /**
         * Caption text displayed alongside the section (e.g. figure
         * caption, table caption).
         */
        private List<String> caption;
    }

    /**
     * Serializes a {@link PublicationContent} to its JSON representation.
     *
     * @param c the publication content to serialize; if {@code null}, an
     *          empty default instance is serialized
     * @return a JSON string; never {@code null}
     */
    static public String toJson(PublicationContent c)
    {
        return gson.toJson(requireNonNullElse(c, new PublicationContent()));
    }

    /**
     * Deserializes a JSON string into a {@link PublicationContent}.
     *
     * @param s the JSON string; if {@code null} or empty, a default
     *          empty instance is returned
     * @return the deserialized publication content; never {@code null}
     */
    static public PublicationContent fromJson(String s)
    {
        if (s == null || s.isEmpty())
            return new PublicationContent();
        final var res = gson.fromJson(s, PublicationContent.class);
        return res != null?res:new PublicationContent();
    }
}
