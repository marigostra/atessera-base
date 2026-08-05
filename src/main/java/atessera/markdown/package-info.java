// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides the Markdown processing subsystem for Alpha Tessera Base.
 *
 * <p>This package extends the <a href="https://github.com/commonmark/commonmark-java">commonmark-java</a>
 * library with custom AST nodes, block parsers, inline link processors,
 * and renderers that target both LaTeX and HTML output. It is the core
 * of the text-to-document pipeline, sitting between the raw Markdown
 * input and the format-specific translators in {@link atessera.publ}.</p>
 *
 * <h2>Architecture</h2>
 *
 * <p>The package is organized into several subpackages, each handling a
 * distinct concern:</p>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.blocks blocks}</dt>
 *   <dd>Custom AST node types that extend commonmark-java's
 *       {@link org.commonmark.node.CustomBlock CustomBlock} and
 *       {@link org.commonmark.node.CustomNode CustomNode}. These
 *       represent domain-specific constructs such as bibliography items,
 *       labels, math expressions, cross-references, citations, and
 *       advanced images.</dd>
 *
 *   <dt>{@link atessera.markdown.parsers parsers}</dt>
 *   <dd>Custom {@link org.commonmark.parser.block.BlockParserFactory
 *       BlockParserFactory} implementations that recognize the extended
 *       syntax (e.g. {@code *** [#key]}, {@code @@ label @@},
 *       {@code > [||] caption}) and produce the corresponding
 *       {@link atessera.markdown.blocks} nodes.</dd>
 *
 *   <dt>{@link atessera.markdown.renderers renderers}</dt>
 *   <dd>Custom {@link org.commonmark.renderer.NodeRenderer NodeRenderer}
 *       implementations that convert the extended AST nodes into LaTeX
 *       or HTML output. These are the rendering backends used by
 *       {@link atessera.markdown.LatexTarget LatexTarget} and
 *       {@link atessera.markdown.HtmlTarget HtmlTarget}.</dd>
 *
 *   <dt>{@link atessera.markdown.tex tex}</dt>
 *   <dd>A standalone LaTeX rendering pipeline (independent of
 *       commonmark-java's built-in renderers). Includes
 *       {@link atessera.markdown.tex.TexRenderer TexRenderer} (the
 *       top-level renderer), {@link atessera.markdown.tex.CoreRenderer
 *       CoreRenderer} (handles standard Markdown nodes), and
 *       {@link atessera.markdown.tex.TexWriter TexWriter} (buffered
 *       output with LaTeX-specific conveniences).</dd>
 *
 *   <dt>{@link atessera.markdown.cust cust}</dt>
 *   <dd>Customized inline parsing infrastructure. Includes a modified
 *       {@link atessera.markdown.cust.InlineParserImpl InlineParserImpl}
 *       that extends commonmark-java's inline parser with support for
 *       additional delimiter processors and link markers, and a
 *       {@link atessera.markdown.cust.StaggeredDelimiterProcessor
 *       StaggeredDelimiterProcessor} that dispatches delimiter runs to
 *       multiple processors based on run length.</dd>
 * </dl>
 *
 * <h2>Key top-level classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.LatexTarget LatexTarget}</dt>
 *   <dd>Configurable Markdown-to-LaTeX pipeline. Wraps a
 *       {@link org.commonmark.parser.Parser Parser} and a
 *       {@link atessera.markdown.tex.TexRenderer TexRenderer}, with
 *       feature flags to enable citations, labels, math, references,
 *       and advanced images. Provides convenience methods
 *       {@code parse(String)} and {@code parse(List<String>)}.</dd>
 *
 *   <dt>{@link atessera.markdown.HtmlTarget HtmlTarget}</dt>
 *   <dd>Configurable Markdown-to-HTML pipeline. Wraps a
 *       {@link org.commonmark.parser.Parser Parser} and an HTML
 *       renderer with feature flags for extended character processing,
 *       citations, labels, math, references, and advanced images.
 *       Supports splitting output at chapter boundaries and translating
 *       references via {@link atessera.markdown.RefTranslation
 *       RefTranslation}.</dd>
 *
 *   <dt>{@link atessera.markdown.Renderers Renderers}</dt>
 *   <dd>Interface defining the contract for rendering all custom AST
 *       node types. Implemented by {@link atessera.markdown.RenderersBase
 *       RenderersBase} (provides default {@code toString()}-based
 *       rendering) and overridden by domain-specific targets.</dd>
 *
 *   <dt>{@link atessera.markdown.EnumNodes EnumNodes}</dt>
 *   <dd>A utility for depth-first traversal of the AST, applying a
 *       {@link java.util.function.Consumer Consumer} to every node.
 *       Used extensively for collecting bibliography items and citation
 *       definitions after parsing.</dd>
 * </dl>
 *
 * <h2>Link processors</h2>
 *
 * <p>Several {@link org.commonmark.parser.beta.LinkProcessor
 * LinkProcessor} implementations intercept inline link syntax
 * ({@code [text]}) and reinterpret it based on the text prefix:</p>
 *
 * <ul>
 *   <li>{@link atessera.markdown.MathLinkProcessor MathLinkProcessor}
 *       &mdash; text starting with {@code $} becomes inline math
 *       ({@link atessera.markdown.MathDefinition MathDefinition}).</li>
 *   <li>{@link atessera.markdown.RefLinkProcessor RefLinkProcessor}
 *       &mdash; text starting with {@code @} becomes a cross-reference
 *       ({@link atessera.markdown.Reference Reference}); double
 *       {@code @@} denotes a page reference.</li>
 *   <li>{@link atessera.markdown.CiteLinkProcessor CiteLinkProcessor}
 *       &mdash; text starting with {@code #} becomes a citation
 *       ({@link atessera.markdown.CiteReference CiteReference}).</li>
 * </ul>
 *
 * <h2>Data flow</h2>
 *
 * <pre>
 * Markdown text
 *     │
 *     ▼
 * LatexTarget / HtmlTarget
 *     │
 *     ├──► Parser (commonmark-java + custom block parsers + link processors)
 *     │
 *     ▼
 * AST (commonmark nodes + custom blocks/nodes from atessera.markdown.blocks)
 *     │
 *     ▼
 * Renderer (CoreRenderer + TexNodeRenderer / HtmlNodeRenderer)
 *     │
 *     ▼
 * LaTeX / HTML output
 * </pre>
 *
 * @see atessera.markdown.blocks
 * @see atessera.markdown.parsers
 * @see atessera.markdown.renderers
 * @see atessera.markdown.tex
 * @see atessera.markdown.cust
 * @see atessera.publ
 */
package atessera.markdown;