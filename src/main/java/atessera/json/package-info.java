// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides the document model classes for Alpha Tessera Base.
 *
 * <p>This package defines the structured descriptions of publications and
 * presentations that serve as the primary input format for the entire
 * Alpha Tessera pipeline. Each model class is a JSON-serializable DTO
 * (via {@link com.google.gson.Gson Gson}) that captures both metadata
 * (title, authors, date, etc.) and content (sections, frames, figures).</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.json.PublicationContent PublicationContent}</dt>
 *   <dd>Complete description of a publication (book, paper, thesis, etc.):
 *       bibliographic metadata, page geometry, abstract, table of contents
 *       settings, and a list of typed content sections.</dd>
 *
 *   <dt>{@link atessera.json.PublicationContent.Section Section}</dt>
 *   <dd>A single content section within a publication. Each section has a
 *       type (Markdown, LaTeX, equation, listing, MetaPost, GNUPlot,
 *       Graphviz, etc.) and the corresponding source text.</dd>
 *
 *   <dt>{@link atessera.json.PresentationContent PresentationContent}</dt>
 *   <dd>Complete description of a presentation: title, authors, theme, and
 *       a list of frames (slides).</dd>
 *
 *   <dt>{@link atessera.json.PresentationContent.Frame Frame}</dt>
 *   <dd>A single slide within a presentation, with a type, title, subtitle,
 *       and source content.</dd>
 *
 *   <dt>{@link atessera.json.PresentationContent.Figure Figure}</dt>
 *   <dd>A standalone figure (MetaPost, GNUPlot, PlantUML, or Graphviz)
 *       associated with a presentation.</dd>
 * </dl>
 *
 * <h2>Data flow</h2>
 *
 * <p>These model objects are the entry point of the pipeline:</p>
 *
 * <pre>
 * JSON string
 *     │
 *     ▼
 * PublicationContent / PresentationContent   (this package)
 *     │
 *     ▼
 * atessera.templ                              (Velocity templates)
 *     │
 *     ▼
 * atessera.comp                               (external compilers)
 *     │
 *     ▼
 * PDF output
 * </pre>
 *
 * <h2>Serialization</h2>
 *
 * <p>Both {@code PublicationContent} and {@code PresentationContent}
 * provide static {@code toJson()} and {@code fromJson()} convenience
 * methods. Null-safe: {@code fromJson(null)} and {@code fromJson("")}
 * return an empty default instance.</p>
 *
 * @see atessera.templ.PublicationTemplate
 * @see atessera.templ.PresentationTemplate
 * @see atessera.comp
 */
package atessera.json;
