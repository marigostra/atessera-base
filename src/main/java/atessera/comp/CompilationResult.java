// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import lombok.*;

/**
 * Holds the outcome of a compilation executed by a {@link Compiler}.
 *
 * <p>This DTO carries all information about the compilation run:</p>
 *
 * <ul>
 *   <li>The captured stdout and stderr of each executed command (one entry
 *       per command in {@code output} and {@code errorOutput}).</li>
 *   <li>Any text or binary output files collected according to the
 *       {@link CompilationTask#getSaveTextFilesOnSuccess() save rules}.</li>
 *   <li>The exit code of the last executed command, or {@code -1} if an
 *       internal exception occurred.</li>
 *   <li>A stack trace string in case of an internal error.</li>
 * </ul>
 *
 * @see CompilationTask
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CompilationResult
{
    /**
     * Standard output captured from each command. Each element in the
     * outer list corresponds to one command; the inner list contains the
     * lines printed by that command.
     */
    private List<List<String>> output;

    /**
     * Standard error captured from each command. Structured identically to
     * {@link #output}.
     */
    private List<List<String>> errorOutput;

    /**
     * Text files collected from the working directory. Keys are file names
     * (as specified in the task's save rules), values are file contents as
     * lists of lines.
     */
    private Map<String, List<String>> textOutputFiles;

    /**
     * Binary files collected from the working directory. Keys are file
     * names (as specified in the task's save rules), values are raw byte
     * contents.
     */
    private Map<String, byte[]> binaryOutputFiles;

    /**
     * Exit code of the last executed command. A value of {@code 0}
     * indicates success. A value of {@code -1} indicates that an internal
     * exception occurred before or during compilation.
     */
    private int exitCode;

    /**
     * Stack trace captured when an internal exception occurs during
     * compilation. {@code null} if no exception was thrown.
     */
    private String stackTrace;
}
