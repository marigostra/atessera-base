// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides custom AST node types for the extended Markdown syntax.
 *
 * <p>This package defines domain-specific node types that extend
 * commonmark-java's {@link org.commonmark.node.CustomBlock CustomBlock}
 * and {@link org.commonmark.node.CustomNode CustomNode} classes. These
 * nodes represent constructs that go beyond standard Markdown and are
 * specific to the Alpha Tessera publishing pipeline.</p>
 *
 * <h2>Node types</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markdown.blocks.BibItem BibItem}</dt>
 *   <dd>Represents a bibliography entry. Created by
 *       {@link atessera.markdown.parsers.BibItemParser BibItemParser}
 *       when encountering a {@code *** [#key]} block. Carries a label
 *       (citation key) and is rendered as a {@code \bibitem} entry in
 *       LaTeX output.</dd>
 *
 *   <dt>{@link atessera.markdown.blocks.Label Label}</dt>
 *   <dd>Represents a cross-reference label. Created by
 *       {@link atessera.markdown.parsers.LabelParser LabelParser} when
 *       encountering a {@code @@ label @@} line. Used as a target for
 *       {@link atessera.markdown.Reference Reference} nodes.</dd>
 *
 *   <dt>{@link atessera.markdown.blocks.MultiBlock MultiBlock}</dt>
 *   <dd>Represents a stylized block with a type indicator (e.g.
 *       {@code [||]} for columns, {@code [!]} for alerts) and an
 *       optional caption. Created by
 *       {@link atessera.markdown.parsers.MultiBlockParser
 *       MultiBlockParser} from {@code > [type] caption} syntax.</dd>
 * </dl>
 *
 * <p>Other custom node types (such as {@link atessera.markdown.MathDefinition},
 * {@link atessera.markdown.MathBlockDefinition}, {@link atessera.markdown.Reference},
 * {@link atessera.markdown.CiteReference}, {@link atessera.markdown.CiteDefinition},
 * and {@link atessera.markdown.AdvImageDefinition}) are defined directly in the
 * parent {@link atessera.markdown} package.</p>
 *
 * @see atessera.markdown.parsers
 * @see atessera.markdown.renderers
 */
package atessera.markdown.blocks;