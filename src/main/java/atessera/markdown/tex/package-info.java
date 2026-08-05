// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides a standalone LaTeX rendering pipeline for Markdown ASTs.
 *
 * <p>This package implements a complete LaTeX renderer that is
 * independent of commonmark-java's built-in HTML renderer. It processes
 * the full Markdown AST (both standard and custom nodes) and produces
 * LaTeX source suitable for inclusion in a full document.</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.tex.TexRenderer TexRenderer}</dt>
 *   <dd>The top-level renderer implementing
 *       {@link org.commonmark.renderer.Renderer Renderer}. Built via a
 *       {@link atessera.markdown.tex.TexRenderer.Builder Builder} that
 *       accepts custom
 *       {@link atessera.markdown.tex.TexNodeRendererFactory
 *       TexNodeRendererFactory} instances.  Internally creates a {@code RendererContext}
 *       that manages a
 *       {@link org.commonmark.internal.renderer.NodeRendererMap
 *       NodeRendererMap} for dispatching nodes to the appropriate
 *       renderer.</dd>
 *
 *   <dt>{@link atessera.markdown.tex.CoreRenderer CoreRenderer}</dt>
 *   <dd>Handles all standard Markdown nodes (Document, Heading,
 *       Paragraph, lists, emphasis, code, links, images, etc.) and
 *       converts them to LaTeX. This is the workhorse of the rendering
 *       pipeline. Registered as the last (fallback) node renderer
 *       factory in {@code TexRenderer}.</dd>
 *
 *   <dt>{@link atessera.markdown.tex.TexWriter TexWriter}</dt>
 *   <dd>A lightweight wrapper around an {@link java.lang.Appendable
 *       Appendable} that provides LaTeX-specific output conveniences:
 *       {@code line()} for newlines, {@code whitespace()} for
 *       non-doubled spaces, and {@code writeStripped(String)} for
 *       collapsed whitespace.</dd>
 *
 *   <dt>{@link atessera.markdown.tex.TexNodeRendererFactory
 *        TexNodeRendererFactory}</dt>
 *   <dd>A functional interface (single method {@code create}) for
 *       producing {@link org.commonmark.renderer.NodeRenderer
 *       NodeRenderer} instances given a
 *       {@link atessera.markdown.tex.TexNodeRendererContext
 *       TexNodeRendererContext}.</dd>
 *
 *   <dt>{@link atessera.markdown.tex.TexNodeRendererContext
 *        TexNodeRendererContext}</dt>
 *   <dd>Interface providing renderers with access to the
 *       {@link atessera.markdown.tex.TexWriter TexWriter}, the
 *       {@code stripNewlines} flag, and the ability to recursively
 *       {@code render(Node)} child nodes.</dd>
 * </dl>
 *
 * <h2>Rendering pipeline</h2>
 *
 * <pre>
 * TexRenderer.render(Node, Appendable)
 *     │
 *     ▼
 * RendererContext.render(Node)
 *     │
 *     ▼
 * NodeRendererMap.render(Node)
 *     │
 *     ├──► TexNodeRenderer       (custom nodes: headings, math, labels, refs, bib items)
 *     │
 *     └──► CoreRenderer          (standard nodes: paragraphs, lists, code, emphasis, links, images)
 *              │
 *              ▼
 *         TexWriter              (buffered LaTeX output)
 * </pre>
 *
 * @see atessera.markdown.LatexTarget
 * @see atessera.markdown.renderers.TexNodeRenderer
 * @see atessera.markdown.renderers
 */
package atessera.markdown.tex;
