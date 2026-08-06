// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides a generic state-machine-based framework for parsing line-oriented
 * markup formats.
 *
 * <p>This package contains reusable parsing infrastructure that is used by
 * higher-level subsystems to process structured text files. It does not
 * define a specific markup language; instead it provides the abstract
 * machinery that concrete parsers (such as
 * {@link atessera.markup.PresentationParser PresentationParser}) build
 * upon.</p>
 *
 * <h2>Core abstractions</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markup.TextStateParser TextStateParser}&lt;M&gt;</dt>
 *   <dd>A generic two-phase line-by-line parser parameterized by a model
 *       type {@code M}. It processes a list of lines in two sequential
 *       phases:
 *       <ol>
 *         <li><b>Headers phase</b> &mdash; non-empty trimmed lines at the
 *             beginning are offered to a set of
 *             {@link atessera.markup.TextStateParser.HeaderLine HeaderLine}
 *             matchers. The first unrecognised line ends this phase and is
 *             forwarded to the states phase without being discarded.</li>
 *         <li><b>States phase</b> &mdash; each trimmed line is checked
 *             against {@link atessera.markup.TextStateParser.NewStateLine
 *             NewStateLine} matchers to detect section boundaries. When a
 *             boundary is found, the previous
 *             {@link atessera.markup.TextStateParser.State State} is
 *             {@link atessera.markup.TextStateParser.State#commit
 *             committed} and a new state becomes active. Lines that do not
 *             trigger a transition are dispatched to the current state's
 *             {@link atessera.markup.TextStateParser.State#onLine onLine}
 *             method.</li>
 *       </ol>
 *       The model instance is shared across all phases, states, and
 *       matchers, accumulating results as parsing progresses.</dd>
 *
 *   <dt>Nested interfaces</dt>
 *   <dd>
 *     <ul>
 *       <li>{@link atessera.markup.TextStateParser.HeaderLine HeaderLine}
 *           &mdash; inspects a header line and updates the model if
 *           recognised.</li>
 *       <li>{@link atessera.markup.TextStateParser.NewStateLine NewStateLine}
 *           &mdash; detects state boundaries and creates new
 *           {@link atessera.markup.TextStateParser.State State} instances.</li>
 *       <li>{@link atessera.markup.TextStateParser.State State}
 *           &mdash; accumulates lines belonging to a single logical section
 *           and finalizes with {@code commit}.</li>
 *     </ul>
 *   </dd>
 * </dl>
 *
 * <h2>Concrete implementations</h2>
 *
 * <dl>
 *   <dt>{@link atessera.markup.PresentationParser PresentationParser}</dt>
 *   <dd>Parses Alpha Tessera presentation files (with
 *       {@code FRAME BEGIN}/&#8203;{@code FRAME END} markers,
 *       global headers like {@code TITLE}, {@code SUBTITLE},
 *       {@code AUTHORS}, and frame-level metadata) into a
 *       {@link atessera.json.PresentationContent PresentationContent}
 *       model. Extends {@code TextStateParser&lt;PresentationContent&gt;}
 *       with:
 *       <ul>
 *         <li>Header matchers for {@code TITLE}, {@code SUBTITLE},
 *             {@code AUTHORS}, {@code DATE}, and {@code THEME}.</li>
 *         <li>Frame state that accumulates source lines and metadata
 *             ({@code TITLE}, {@code SUBTITLE}, {@code LABEL},
 *             {@code LISTING_LANG}) inside a frame.</li>
 *         <li>State-boundary matchers that detect
 *             {@code FRAME BEGIN [type]} and {@code FRAME END}.</li>
 *       </ul>
 *       Provides a static convenience method
 *       {@link atessera.markup.PresentationParser#parsePresentation
 *       parsePresentation(List&lt;String&gt;)} for one-step parsing.</dd>
 * </dl>
 *
 * <h2>Extending the framework</h2>
 *
 * <p>To parse a new markup format, extend
 * {@link atessera.markup.TextStateParser TextStateParser} with a suitable
 * model type, supply implementations of {@code HeaderLine},
 * {@code NewStateLine}, and {@code State}, and pass them to the
 * constructor. The two-phase design handles the common pattern of
 * &laquo;global metadata followed by delimited sections&raquo; that
 * appears in many line-oriented formats.</p>
 *
 * <h2>Relationship to other packages</h2>
 *
 * <p>This package sits at the bottom of the parsing stack and is consumed
 * by the presentation subsystem. It is independent of the Markdown
 * processing pipeline in {@link atessera.markdown}, which uses
 * commonmark-java and has its own parser architecture.</p>
 *
 * @see atessera.markup.TextStateParser
 * @see atessera.markup.PresentationParser
 * @see atessera.json.PresentationContent
 */
package atessera.markup;
