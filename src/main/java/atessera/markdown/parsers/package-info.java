// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides custom block parser implementations for the extended Markdown
 * syntax.
 *
 * <p>This package contains {@link org.commonmark.parser.block.BlockParserFactory
 * BlockParserFactory} and {@link org.commonmark.parser.block.AbstractBlockParser
 * AbstractBlockParser} implementations that recognize Alpha Tessera's
 * domain-specific block constructs and produce the corresponding
 * {@link atessera.markdown.blocks} AST nodes.</p>
 *
 * <h2>Parser classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.parsers.BibItemParserFactory BibItemParserFactory}
 *       / {@link atessera.markdown.parsers.BibItemParser BibItemParser}</dt>
 *   <dd>Recognizes bibliography item blocks starting with
 *       {@code *** [#key]}. Produces
 *       {@link atessera.markdown.blocks.BibItem BibItem} nodes. The
 *       block continues as long as subsequent lines are indented by at
 *       least 4 spaces.</dd>
 *
 *   <dt>{@link atessera.markdown.parsers.LabelParserFactory LabelParserFactory}
 *       / {@link atessera.markdown.parsers.LabelParser LabelParser}</dt>
 *   <dd>Recognizes label lines of the form {@code @@ label @@}.
 *       Produces {@link atessera.markdown.blocks.Label Label} nodes.
 *       Labels are used as targets for cross-references.</dd>
 *
 *   <dt>{@link atessera.markdown.parsers.MultiBlockParserFactory MultiBlockParserFactory}
 *       / {@link atessera.markdown.parsers.MultiBlockParser MultiBlockParser}</dt>
 *   <dd>Recognizes stylized blocks starting with {@code >} followed by
 *       an optional type indicator (e.g. {@code [||]} for columns,
 *       {@code [!]} for alerts) and a caption. Produces
 *       {@link atessera.markdown.blocks.MultiBlock MultiBlock} nodes.
 *       The block continues on subsequent lines that start with
 *       {@code >}.</dd>
 * </dl>
 *
 * <p>Additional block parsers for math blocks
 * ({@link atessera.markdown.MathBlockParser MathBlockParser}),
 * citation definitions ({@link atessera.markdown.CiteBlockParser
 * CiteBlockParser}), and advanced images
 * ({@link atessera.markdown.AdvImageBlockParser AdvImageBlockParser})
 * are defined directly in the parent {@link atessera.markdown} package.</p>
 *
 * @see atessera.markdown.blocks
 * @see atessera.markdown.LatexTarget
 * @see atessera.markdown.HtmlTarget
 */
package atessera.markdown.parsers;