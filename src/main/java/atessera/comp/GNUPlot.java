// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

import static java.util.Objects.*;

/**
 * Facade for compiling GNUPlot scripts to cropped PDF charts.
 *
 * <p>This class encapsulates a two-step GNUPlot pipeline:</p>
 *
 * <ol>
 *   <li>{@code gnuplot} &mdash; executes the script ({@code src.plot}) and
 *       produces {@code plot.pdf}.</li>
 *   <li>{@code pdfcrop} &mdash; crops the PDF to its bounding box,
 *       producing {@code plot-crop.pdf}.</li>
 * </ol>
 *
 * <p>After a successful compilation the cropped PDF is available via
 * {@link #getOutput()}. On failure the last block of stderr is available
 * via {@link #getLog()}.</p>
 *
 * @see Compiler
 * @see LocalCompiler
 */
public final class GNUPlot
{
    private final Compiler compiler;
    private List<String> source;
    private byte[] output;
    private List<String> log;
    private List<List<String>> rawOutput, rawErrorOutput;
    private String stackTrace;
    
    /**
     * Constructs a new {@code GNUPlot} compilation facade.
     *
     * @param compiler the underlying {@link Compiler} to use; must not be
     *                 {@code null}
     * @param source   the GNUPlot script as a list of lines; must not be
     *                 {@code null}
     */
    public GNUPlot(Compiler compiler, List<String> source)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
    }

    /**
     * Runs the GNUPlot-to-PDF pipeline.
     *
     * @return {@code true} if all steps completed successfully and the
     *         cropped PDF was produced; {@code false} otherwise
     * @throws IllegalStateException if the compiler result is inconsistent
     *         (e.g. zero exit code but no PDF, or non-zero exit code with
     *         empty error output)
     */
    public boolean compile()
    {
	final var task = new CompilationTask();
	task.setCommands(List.of(
				 "gnuplot src.plot",
				 "pdfcrop plot.pdf"
				 ));
	task.setTextSources(new HashMap<>());
	task.getTextSources().put("src.plot", source);
	task.setBinarySources(new HashMap<>());
	task.setSaveTextFilesOnFailure(Collections.emptyList());
	task.setSaveBinaryFilesOnSuccess(List.of("plot-crop.pdf"));
	task.setSaveTextFilesOnSuccess(Collections.emptyList());
	final var res = compiler.compile(task);
	rawOutput = res.getOutput();
	rawErrorOutput = res.getErrorOutput();
	if (res.getExitCode() == 0)
	{
	    final var b = res.getBinaryOutputFiles().get("plot-crop.pdf");
	    if (b == null)
		throw new IllegalStateException("No plot-crop.pdf in metapost output with exit code equal to zero");
	    output = b;
	    return true;
	}
	if (res.getExitCode() > 0)
	{
	    if (rawErrorOutput.isEmpty())
		throw new IllegalStateException("Empty raw error output");
	    log = rawErrorOutput.get(rawErrorOutput.size() - 1);
	    return false;
	}
	stackTrace = res.getStackTrace();
	return false;
    }
    
    /**
     * Returns the compiled cropped PDF chart.
     *
     * @return the PDF as a byte array, or {@code null} if compilation has
     *         not been run or was unsuccessful
     */
    public byte[] getOutput()
    {
	return output;
    }

    /**
     * Returns the last block of stderr after a failed compilation.
     *
     * @return the error output as a list of lines, or {@code null} if no
     *         error was captured
     */
    public List<String> getLog()
    {
	return log;
    }

    /**
     * Returns the raw stdout captured from each command in the pipeline.
     *
     * @return an unmodifiable view of the captured stdout, or {@code null}
     *         before compilation
     */
    public List<List<String>> getRawOutput()
    {
	return rawOutput;
    }

    /**
     * Returns the raw stderr captured from each command in the pipeline.
     *
     * @return an unmodifiable view of the captured stderr, or {@code null}
     *         before compilation
     */
    public List<List<String>> getRawErrorOutput()
    {
	return rawErrorOutput;
    }

    /**
     * Returns the stack trace if an internal exception occurred during
     * compilation.
     *
     * @return the stack trace as a string, or {@code null} if no exception
     *         was thrown
     */
    public String getStackTrace()
    {
	return stackTrace;
    }
}
