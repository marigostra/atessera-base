// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import lombok.*;

/**
 * Describes a compilation job to be executed by a {@link Compiler}.
 *
 * <p>A task bundles all the information needed to run one or more external
 * tools in sequence:</p>
 *
 * <ul>
 *   <li><strong>Text sources</strong> &mdash; named text files (e.g.
 *       {@code main.tex}, {@code src.plot}) to be written into the working
 *       directory.</li>
 *   <li><strong>Binary sources</strong> &mdash; named binary files (e.g.
 *       images) to be placed alongside the text sources.</li>
 *   <li><strong>Commands</strong> &mdash; shell commands to execute in
 *       order. If any command returns a non-zero exit code, execution stops
 *       immediately.</li>
 *   <li><strong>Save rules</strong> &mdash; lists of file names to collect
 *       from the working directory after compilation (separate lists for
 *       success and failure scenarios).</li>
 * </ul>
 *
 * @see CompilationResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CompilationTask
{
    /**
     * Text files to write into the working directory before compilation.
     * Keys are file names (relative to the working directory root), values
     * are the file contents as lists of lines.
     */
    private Map<String, List<String>> textSources;

    /**
     * Binary files to write into the working directory before compilation.
     * Keys are file names, values are raw byte contents.
     */
    private Map<String, byte[]> binarySources;

    /**
     * Shell commands to execute sequentially in the working directory.
     * Execution stops on the first non-zero exit code.
     */
    private List<String> commands;

    /**
     * Names of text files to collect from the working directory when all
     * commands complete successfully.
     */
    private List<String> saveTextFilesOnSuccess;

    /**
     * Names of binary files to collect from the working directory when all
     * commands complete successfully.
     */
    private List<String> saveBinaryFilesOnSuccess;

    /**
     * Names of text files to collect from the working directory when a
     * command fails (non-zero exit code).
     */
    private List<String> saveTextFilesOnFailure;
}
