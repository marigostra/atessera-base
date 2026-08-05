// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides translators that convert structured publication descriptions
 * into target output formats.
 *
 * <p>This package sits between the document model layer
 * ({@link atessera.json}) and the template/compilation layers
 * ({@link atessera.templ}, {@link atessera.comp}). It takes a
 * {@link atessera.json.PublicationContent PublicationContent} object and
 * produces either LaTeX source (for subsequent PDF compilation) or HTML
 * output (for web viewing).</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.publ.TexTranslator TexTranslator}</dt>
 *   <dd>Main translator that converts a full publication into a compilable
 *       LaTeX document. Delegates Markdown-to-LaTeX conversion to the
 *       package-private {@code Markdown} class, bibliography extraction to
 *       {@link atessera.publ.BiblioExtractor BiblioExtractor}, and final
 *       assembly to {@link atessera.templ.PublicationTemplate
 *       PublicationTemplate}.</dd>
 *
 *   <dt>{@link atessera.publ.HtmlTranslator HtmlTranslator}</dt>
 *   <dd>Translates a publication into a set of HTML chapter files with
 *       working cross-references, auto-numbered headings, and citation
 *       support. Uses a two-pass algorithm: the first pass collects all
 *       labels and reference targets, the second pass renders the content
 *       with resolved links.</dd>
 *
 *   <dt>{@link atessera.publ.BiblioExtractor BiblioExtractor}</dt>
 *   <dd>Utility that scans all Markdown sections of a publication and
 *       extracts bibliography entries (represented as
 *       {@link atessera.markdown.blocks.BibItem BibItem} nodes) into a
 *       {@code Map} keyed by citation label.</dd>
 * </dl>
 *
 * <h2>Data flow</h2>
 *
 * <pre>
 * PublicationContent (atessera.json)
 *     │
 *     ├──► TexTranslator ──► PublicationTemplate ──► LaTeX ──► PDF
 *     │
 *     └──► HtmlTranslator ──► HTML chapter files
 * </pre>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * // Prepare the engine factory with Velocity templates
 * EngineFactory factory = new EngineFactory(templates);
 *
 * // Load publication content from JSON
 * PublicationContent content = PublicationContent.fromJson(jsonString);
 *
 * // Generate LaTeX
 * TexTranslator tex = new TexTranslator(factory, content);
 * List&lt;String&gt; latexSource = tex.translate();
 *
 * // Generate HTML
 * try (HtmlTranslator html = new HtmlTranslator(content)) {
 *     html.translate();
 * }
 * }</pre>
 *
 * @see atessera.json.PublicationContent
 * @see atessera.templ.PublicationTemplate
 * @see atessera.markdown
 */
package atessera.publ;