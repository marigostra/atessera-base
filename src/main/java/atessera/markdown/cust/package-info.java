// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides customized inline parsing infrastructure for the Markdown
 * processing pipeline.
 *
 * <p>This package contains modifications to commonmark-java's built-in
 * inline parsing components, enabling more flexible delimiter processing
 * and finer control over which inline constructs are recognized.</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.cust.InlineParserImpl InlineParserImpl}</dt>
 *   <dd>A customized version of commonmark-java's
 *       {@code InlineParserImpl} that accepts additional
 *       {@link org.commonmark.parser.delimiter.DelimiterProcessor
 *       DelimiterProcessor} instances, custom
 *       {@link org.commonmark.parser.beta.InlineContentParserFactory
 *       InlineContentParserFactory} list, and a flag to control whether
 *       HTML inline constructs ({@code EntityInlineParser},
 *       {@code HtmlInlineParser}) are recognized. Used by both
 *       {@link atessera.markdown.LatexTarget LatexTarget} and
 *       {@link atessera.markdown.HtmlTarget HtmlTarget} to ensure
 *       consistent inline parsing behaviour.</dd>
 *
 *   <dt>{@link atessera.markdown.cust.StaggeredDelimiterProcessor
 *        StaggeredDelimiterProcessor}</dt>
 *   <dd>A composite {@link org.commonmark.parser.delimiter.DelimiterProcessor
 *       DelimiterProcessor} that dispatches delimiter runs to one of
 *       several child processors based on the length of the run. This
 *       allows multiple processors to share the same delimiter character
 *       (e.g. {@code *}) with different minimum lengths. Child
 *       processors must have distinct minimum lengths.</dd>
 * </dl>
 *
 * <h2>Usage</h2>
 *
 * <p>These classes are internal to the Markdown pipeline and are
 * typically not instantiated directly by users. They are wired into
 * {@link atessera.markdown.LatexTarget LatexTarget} and
 * {@link atessera.markdown.HtmlTarget HtmlTarget} via the
 * {@link org.commonmark.parser.Parser.Builder Parser.Builder}:
 *
 * <pre>{@code
 * p.inlineParserFactory(c -> new InlineParserImpl(c, allowHtmlInlines));
 * }</pre>
 *
 * @see atessera.markdown.LatexTarget
 * @see atessera.markdown.HtmlTarget
 */
package atessera.markdown.cust;