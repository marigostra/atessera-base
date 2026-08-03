// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.json;

import java.util.*;
import lombok.*;
import com.google.gson.*;

import static java.util.Objects.*;

/**
 * Structured description of a presentation (LaTeX Beamer slides).
 *
 * <p>This class captures the metadata (title, authors, date, theme) and
 * the slide content of a presentation. It is designed to be serialized to
 * and from JSON via {@link #toJson(PresentationContent)} and
 * {@link #fromJson(String)}, and serves as the primary input for
 * {@link atessera.templ.PresentationTemplate}.</p>
 *
 * <h3>Supported frame types</h3>
 *
 * <p>Each {@link Frame} within the presentation has a {@link FrameType}
 * that determines how its source text is processed:</p>
 * <ul>
 *   <li>{@link FrameType#MARKDOWN MARKDOWN} &mdash; text in Markdown
 *       format</li>
 *   <li>{@link FrameType#LATEX LATEX} &mdash; raw LaTeX source</li>
 *   <li>{@link FrameType#METAPOST METAPOST} &mdash; MetaPost figure</li>
 *   <li>{@link FrameType#GNUPLOT GNUPLOT} &mdash; GNUPlot chart</li>
 *   <li>{@link FrameType#PLANTUML PLANTUML} &mdash; PlantUML diagram</li>
 *   <li>{@link FrameType#LISTING LISTING} &mdash; source code listing</li>
 *   <li>{@link FrameType#EQUATION EQUATION} &mdash; mathematical
 *       equation</li>
 *   <li>{@link FrameType#GRAPHVIZ_DOT GRAPHVIZ_DOT} &mdash; Graphviz (dot
 *       layout)</li>
 *   <li>{@link FrameType#GRAPHVIZ_NEATO GRAPHVIZ_NEATO} &mdash; Graphviz
 *       (neato layout)</li>
 *   <li>{@link FrameType#GRAPHVIZ_TWOPI GRAPHVIZ_TWOPI} &mdash; Graphviz
 *       (twopi layout)</li>
 *   <li>{@link FrameType#GRAPHVIZ_CIRCO GRAPHVIZ_CIRCO} &mdash; Graphviz
 *       (circo layout)</li>
 * </ul>
 *
 * <h3>Supported figure types</h3>
 *
 * <p>The {@link Figure} class describes standalone figures (independent of
 * a particular frame) with the following {@link FigureType}s:</p>
 * <ul>
 *   <li>{@link FigureType#METAPOST METAPOST}</li>
 *   <li>{@link FigureType#GNUPLOT GNUPLOT}</li>
 *   <li>{@link FigureType#PLANTUML PLANTUML}</li>
 *   <li>{@link FigureType#GRAPHVIZ_DOT GRAPHVIZ_DOT}</li>
 *   <li>{@link FigureType#GRAPHVIZ_NEATO GRAPHVIZ_NEATO}</li>
 *   <li>{@link FigureType#GRAPHVIZ_TWOPI GRAPHVIZ_TWOPI}</li>
 *   <li>{@link FigureType#GRAPHVIZ_CIRCO GRAPHVIZ_CIRCO}</li>
 * </ul>
 *
 * @see atessera.templ.PresentationTemplate
 */
@Data
@NoArgsConstructor
public final class PresentationContent
{
    /** Shared Gson instance for JSON serialization. */
    static private final Gson gson = new Gson();

    /**
     * Immutable set of all frame types. Convenient for validation and
     * iteration.
     */
    static public final Set<FrameType> ALL_FRAME_TYPES = EnumSet.allOf(FrameType.class);

    /**
     * Immutable set of all figure types. Convenient for validation and
     * iteration.
     */
    static public final Set<FigureType> ALL_FIGURE_TYPES = EnumSet.allOf(FigureType.class);

    /**
     * Enumerates the types of content that a presentation frame may
     * contain. Each type implies a different processing pipeline.
     */
    public enum FrameType
    {
        /** Text in Markdown format. */
        MARKDOWN,
        /** Raw LaTeX source (passed through as-is). */
        LATEX,
        /** A MetaPost figure. */
        METAPOST,
        /** A GNUPlot chart. */
        GNUPLOT,
        /** A PlantUML diagram. */
        PLANTUML,
        /** A source code listing. */
        LISTING,
        /** A mathematical equation. */
        EQUATION,
        /** A Graphviz diagram using the dot layout engine. */
        GRAPHVIZ_DOT,
        /** A Graphviz diagram using the neato layout engine. */
        GRAPHVIZ_NEATO,
        /** A Graphviz diagram using the twopi layout engine. */
        GRAPHVIZ_TWOPI,
        /** A Graphviz diagram using the circo layout engine. */
        GRAPHVIZ_CIRCO
    };

    /**
     * Enumerates the types of standalone figures that can be associated
     * with a presentation.
     */
    public enum FigureType
    {
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

    /** Presentation title. */
    private String title;

    /** Presentation subtitle (optional). */
    private String subtitle;

    /** Authors string (e.g. {@code "Ivanov I. I."}). */
    private String authors;

    /** Presentation date (free-form string). */
    private String date;

    /** Beamer theme name (e.g. {@code "Madrid"}, {@code "Copenhagen"}). */
    private String theme;

    /** Output file name (e.g. {@code "slides.pdf"}). */
    private String outFile;

    /** The list of frames (slides) that make up the presentation. */
    private List<Frame> frames;

    /**
     * Represents a single frame (slide) within a presentation.
     *
     * <p>Each frame has a {@link FrameType} that determines how its
     * source text is interpreted, and may carry optional metadata: an
     * identifier, a title, a subtitle, a LaTeX label, and a programming
     * language hint for listings.</p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Frame
    {
        /**
         * The type of content in this frame. Determines which processing
         * pipeline is used.
         */
        private FrameType type;

        /**
         * Unique identifier for this frame (used for cross-referencing).
         */
        private String id;

        /** Frame title (displayed at the top of the slide). */
        private String title;

        /** Frame subtitle (displayed below the title). */
        private String subtitle;

        /**
         * LaTeX label for referencing this frame (e.g. for
         * {@code \\label} and {@code \\ref} commands).
         */
        private String label;

        /**
         * Programming language hint for source code listings (e.g.
         * {@code "java"}, {@code "python"}). Used for syntax highlighting.
         */
        private String listingLang;

        /**
         * Source text of the frame. Interpreted according to the frame's
         * {@link #type}.
         */
        private List<String> source;
    }

    /**
     * Represents a standalone figure associated with a presentation,
     * independent of a particular frame.
     *
     * <p>Figures are compiled separately and may be referenced from
     * multiple frames. Each figure has a {@link FigureType}, an optional
     * identifier, a LaTeX label, and source text.</p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Figure
    {
        /**
         * The type of figure. Determines which compiler is used (e.g.
         * {@link FigureType#METAPOST} uses
         * {@link atessera.comp.Metapost}).
         */
        private FigureType type;

        /**
         * Unique identifier for this figure (used for cross-referencing).
         */
        private String id;

        /**
         * LaTeX label for referencing this figure.
         */
        private String label;

        /**
         * Source text of the figure. Interpreted according to the
         * figure's {@link #type}.
         */
        private List<String> source;
    }

    /**
     * Serializes a {@link PresentationContent} to its JSON representation.
     *
     * @param c the presentation content to serialize; if {@code null}, an
     *          empty default instance is serialized
     * @return a JSON string; never {@code null}
     */
    static public String toJson(PresentationContent c)
    {
        return gson.toJson(requireNonNullElse(c, new PresentationContent()));
    }

    /**
     * Deserializes a JSON string into a {@link PresentationContent}.
     *
     * @param s the JSON string; if {@code null} or empty, a default
     *          empty instance is returned
     * @return the deserialized presentation content; never {@code null}
     */
    static public PresentationContent fromJson(String s)
    {
        if (s == null || s.isEmpty())
            return new PresentationContent();
        final var res = gson.fromJson(s, PresentationContent.class);
        return res != null?res:new PresentationContent();
    }
}
