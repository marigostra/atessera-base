// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides custom node renderers for the extended Markdown AST nodes.
 *
 * <p>This package contains {@link org.commonmark.renderer.NodeRenderer
 * NodeRenderer} implementations that convert Alpha Tessera's custom AST
 * nodes into LaTeX or HTML output. These renderers are plugged into the
 * rendering pipelines of {@link atessera.markdown.LatexTarget
 * LatexTarget} and {@link atessera.markdown.HtmlTarget HtmlTarget}.</p>
 *
 * <h2>Renderer classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.renderers.TexNodeRenderer TexNodeRenderer}</dt>
 *   <dd>Renders custom AST nodes into LaTeX. Delegates to the
 *       {@link atessera.markdown.Renderers Renderers} interface for
 *       each node type. Supports headings (via
 *       {@code renderHeadingOpening}/{@code renderHeadingClosing}),
 *       math definitions and blocks, labels, references, bibliography
 *       items, and multi-blocks. When {@code silentBibItems} is
 *       {@code true}, bibliography items are silently collected rather
 *       than rendered inline.</dd>
 *
 *   <dt>{@link atessera.markdown.renderers.HtmlNodeRenderer HtmlNodeRenderer}</dt>
 *   <dd>Renders custom AST nodes into HTML. Delegates to the
 *       {@link atessera.markdown.Renderers Renderers} interface for
 *       each node type. Handles headings (with customizable prefix via
 *       {@code onHeading}), text (with customizable escaping via
 *       {@code escape}), math, labels, references, citations, advanced
 *       images, and multi-blocks.</dd>
 * </dl>
 *
 * <p>Both renderers walk the AST by recursively calling
 * {@code context.render()} on child nodes, allowing standard
 * commonmark-java nodes to be handled by the core renderer while custom
 * nodes are intercepted and processed by these renderers.</p>
 *
 * @see atessera.markdown.Renderers
 * @see atessera.markdown.RenderersBase
 * @see atessera.markdown.blocks
 */
package atessera.markdown.renderers;