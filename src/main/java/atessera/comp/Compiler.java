// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

/**
 * Strategy interface for executing a compilation task.
 *
 * <p>Implementations of this interface encapsulate the runtime environment
 * in which compilation commands are executed. The default implementation
 * is {@link LocalCompiler}, which runs commands in an isolated temporary
 * directory on the local machine. Alternative implementations could, for
 * example, delegate to a Docker container or a remote build server.</p>
 *
 * @see CompilationTask
 * @see CompilationResult
 * @see LocalCompiler
 */
public interface Compiler
{
    /**
     * Executes the given compilation task.
     *
     * @param task the compilation task describing sources, commands, and
     *        which output files to collect; must not be {@code null}
     * @return the result of the compilation, including exit codes, captured
     *         output, and requested output files; never {@code null} in
     *         normal operation
     */
    CompilationResult compile(CompilationTask task);
}
