// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides template-based source code generation using Apache Velocity.
 *
 * <p>This package transforms structured document descriptions (from
 * {@link atessera.json}) into source files for external tools such as
 * LaTeX, MetaPost, and GNUPlot. Each template class corresponds to a
 * specific Velocity template (a {@code .vm} file) and exposes a fluent API
 * for populating the template context.</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.templ.EngineFactory EngineFactory}</dt>
 *   <dd>Creates and caches {@link org.apache.velocity.app.VelocityEngine}
 *       instances configured with {@code StringResourceLoader} so that
 *       templates can be supplied programmatically at runtime.</dd>
 *
 *   <dt>{@link atessera.templ.Base Base}</dt>
 *   <dd>Common base class for all templates. Wraps a
 *       {@link org.apache.velocity.VelocityContext VelocityContext} and
 *       provides {@code render} and {@code renderToStringList} methods.</dd>
 *
 *   <dt>{@link atessera.templ.PublicationTemplate PublicationTemplate}</dt>
 *   <dd>Generates LaTeX source for a full publication (book, paper, thesis,
 *       etc.) using the {@code publication.vm} template.</dd>
 *
 *   <dt>{@link atessera.templ.PresentationTemplate PresentationTemplate}</dt>
 *   <dd>Generates LaTeX Beamer source for a presentation using the
 *       {@code presentation.vm} template.</dd>
 *
 *   <dt>{@link atessera.templ.MetapostTemplate MetapostTemplate}</dt>
 *   <dd>Generates MetaPost source for a figure using the
 *       {@code metapost.vm} template.</dd>
 *
 *   <dt>{@link atessera.templ.GNUPlotTemplate GNUPlotTemplate}</dt>
 *   <dd>Generates GNUPlot source for a chart using the
 *       {@code gnuplot.vm} template.</dd>
 * </dl>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * // Prepare templates (normally loaded once)
 * Map<String, List<String>> templates = Map.of(
 *     "publication.vm", List.of("... template content ...")
 * );
 * EngineFactory factory = new EngineFactory(templates);
 *
 * // Generate a publication
 * PublicationTemplate publ = new PublicationTemplate(factory);
 * publ.setHeader(content);
 * publ.setSections(sections);
 * List<String> latexSource = publ.renderToStringList();
 * }</pre>
 *
 * @see atessera.comp
 * @see atessera.json
 * @see org.apache.velocity.app.VelocityEngine
 */
package atessera.templ;
