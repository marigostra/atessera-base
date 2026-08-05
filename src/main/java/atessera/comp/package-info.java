// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides the compilation subsystem.
 *
 * <p>This package encapsulates the process of invoking external compilers and
 * tools (such as {@code pdflatex}, {@code mpost}, and {@code gnuplot}) in
 * isolated temporary directories. It follows a three-layer architecture:</p>
 *
 * <dl>
 *   <dt>Contracts</dt>
 *   <dd>{@link atessera.comp.Compiler Compiler} (strategy interface),
 *       {@link atessera.comp.CompilationTask CompilationTask} (input DTO),
 *       {@link atessera.comp.CompilationResult CompilationResult} (output DTO)
 *       &mdash; define the protocol for compilation.</dd>
 *
 *   <dt>Runtime</dt>
 *   <dd>{@link atessera.comp.LocalCompiler LocalCompiler} &mdash; the default
 *       implementation that executes shell commands inside a
 *       {@link atessera.util.TempDir TempDir}.</dd>
 *
 *   <dt>Facades</dt>
 *   <dd>{@link atessera.comp.PdfLatex PdfLatex},
 *       {@link atessera.comp.Metapost Metapost},
 *       {@link atessera.comp.GNUPlot GNUPlot} &mdash; high-level classes that
 *       encapsulate multi-step pipelines for specific tools and delegate the
 *       actual execution to a {@code Compiler}.</dd>
 * </dl>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * Compiler compiler = new LocalCompiler();
 *
 * // Compile a LaTeX document
 * PdfLatex pdfLatex = new PdfLatex(compiler, latexSource, images, listings);
 * if (pdfLatex.compile()) {
 *     byte[] pdf = pdfLatex.getOutput();
 * }
 *
 * // Compile a MetaPost figure
 * Metapost mp = new Metapost(compiler, mpSource);
 * if (mp.compile()) {
 *     byte[] pdf = mp.getOutput();
 * }
 *
 * // Compile a GNUPlot chart
 * GNUPlot gp = new GNUPlot(compiler, gpSource);
 * if (gp.compile()) {
 *     byte[] pdf = gp.getOutput();
 * }
 * }</pre>
 *
 * @see atessera.util.TempDir
 * @see atessera.util.ShellCmd
 */
package atessera.comp;
