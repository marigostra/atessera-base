// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

/**
 * Provides general-purpose utility classes used throughout Alpha Tessera Base.
 *
 * <p>This package contains low-level helpers for shell command execution,
 * temporary directory management, text file I/O, LaTeX string escaping,
 * random identifier generation, and SVG math rendering. These utilities
 * are used by the compilation ({@link atessera.comp}), templating
 * ({@link atessera.templ}), and Markdown processing
 * ({@link atessera.markdown}) subsystems.</p>
 *
 * <h2>Key classes</h2>
 *
 * <dl>
 *   <dt>{@link atessera.util.ShellCmd ShellCmd}</dt>
 *   <dd>Executes a shell command via {@code /bin/bash -c} in a specified
 *       working directory. Captures standard output and error streams in
 *       separate threads. Provides both instance-level
 *       ({@code waitFor()}) and static ({@code execAndWait()}) methods
 *       for synchronous execution.</dd>
 *
 *   <dt>{@link atessera.util.TempDir TempDir}</dt>
 *   <dd>Creates and manages a temporary directory. Implements
 *       {@link java.lang.AutoCloseable AutoCloseable} so the directory
 *       and all its contents are recursively deleted when the
 *       try-with-resources block exits. Provides convenience methods
 *       for executing shell commands within the temporary directory.</dd>
 *
 *   <dt>{@link atessera.util.TextUtils TextUtils}</dt>
 *   <dd>Static helpers for reading and writing text files, reading Java
 *       resources, and processing line-based configuration files
 *       (skipping comments and empty lines, converting to uppercase).</dd>
 *
 *   <dt>{@link atessera.util.LatexUtils LatexUtils}</dt>
 *   <dd>Static methods for escaping text for LaTeX output. Three
 *       levels of escaping are provided:
 *       <ul>
 *         <li>{@code escape(String)} &mdash; full escaping of all
 *             special characters including {@code ~} and {@code "}.</li>
 *         <li>{@code escapeStrict(String)} &mdash; strict escaping
 *             with additional handling for {@code -}, {@code <},
 *             {@code >}, {@code '}, {@code `}, {@code ^}, and
 *             {@code ~}.</li>
 *         <li>{@code escapeRelaxed(String)} &mdash; relaxed escaping
 *             that leaves {@code ~} unescaped (suitable for LaTeX
 *             arguments where tilde has its normal meaning).</li>
 *       </ul>
 *   </dd>
 *
 *   <dt>{@link atessera.util.IdStr IdStr}</dt>
 *   <dd>Generates random identifier strings of a specified length using
 *       {@link java.security.SecureRandom SecureRandom}. Only
 *       alphanumeric characters and underscores are used. Provides a
 *       static convenience method {@code getRandomId(int)} backed by a
 *       singleton instance.</dd>
 *
 *   <dt>{@link atessera.util.SvgGenerator SvgGenerator}</dt>
 *   <dd>Converts LaTeX math expressions to SVG images using the
 *       {@code latex} and {@code dvisvgm} command-line tools. Supports
 *       configurable temporary directories, SVG scaling, and unique ID
 *       generation for multiple formulas on the same page. Provides a
 *       {@code checkDependencies()} method to verify that the required
 *       tools are available.</dd>
 * </dl>
 *
 * <h2>Usage notes</h2>
 *
 * <p>All classes in this package are stateless or have minimal mutable
 * state, making them suitable for use across multiple threads with
 * appropriate external synchronization where needed (e.g.
 * {@link atessera.util.SvgGenerator SvgGenerator} uses
 * {@code synchronized} on the {@code generateSvg} method to serialize
 * formula counter access).</p>
 *
 * @see atessera.comp
 * @see atessera.templ
 * @see atessera.markdown
 */
package atessera.util;