// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

import static java.util.Objects.*;

/**
 * Facade for compiling MetaPost figures to cropped PDF.
 *
 * <p>This class encapsulates a three-step MetaPost pipeline:</p>
 *
 * <ol>
 *   <li>{@code mpost} &mdash; compiles the MetaPost source ({@code main.mp})
 *       into a PostScript figure ({@code figure.mps}). The environment
 *       variable {@code TEX=latex} is set so that MetaPost uses LaTeX for
 *       typesetting labels.</li>
 *   <li>{@code mptopdf} &mdash; converts {@code figure.mps} to
 *       {@code figure-mps.pdf}.</li>
 *   <li>{@code pdfcrop} &mdash; crops the PDF to its bounding box, producing
 *       {@code figure-mps-crop.pdf}.</li>
 * </ol>
 *
 * <p>After a successful compilation the cropped PDF is available via
 * {@link #getOutput()}. On failure the contents of {@code main.log} are
 * available via {@link #getLog()}.</p>
 *
 * @see Compiler
 * @see LocalCompiler
 */
public final class Metapost
{
    private final Compiler compiler;
    private List<String> source;
    private byte[] output;
    private List<String> log;
    private List<List<String>> rawOutput, rawErrorOutput;
    private String stackTrace;
    
    /**
     * Constructs a new {@code Metapost} compilation facade.
     *
     * @param compiler the underlying {@link Compiler} to use; must not be
     *                 {@code null}
     * @param source   the MetaPost source as a list of lines; must not be
     *                 {@code null}
     */
    public Metapost(Compiler compiler, List<String> source)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
    }

    /**
     * Runs the MetaPost-to-PDF pipeline.
     *
     * @return {@code true} if all steps completed successfully and the
     *         cropped PDF was produced; {@code false} otherwise
     * @throws IllegalStateException if the compiler result is inconsistent
     *         (e.g. zero exit code but no PDF, or non-zero exit code but
     *         no log file)
     */
    public boolean compile()
    {
	final var task = new CompilationTask();
	task.setCommands(List.of(
				 "TEX=latex mpost -interaction=batchmode main.mp end",
				 				 "mptopdf figure.mps",
				 				 "pdfcrop figure-mps.pdf"
				 ));
	task.setTextSources(new HashMap<>());
	task.getTextSources().put("main.mp", source);
			 task.setBinarySources(new HashMap<>());
	task.setSaveTextFilesOnFailure(List.of("main.log"));
	task.setSaveBinaryFilesOnSuccess(List.of("figure-mps-crop.pdf"));
	task.setSaveTextFilesOnSuccess(Collections.emptyList());
	final var res = compiler.compile(task);
	rawOutput = res.getOutput();
	rawErrorOutput = res.getErrorOutput();
	if (res.getExitCode() == 0)
	{
	    final var b = res.getBinaryOutputFiles().get("figure-mps-crop.pdf");
	    if (b == null)
		throw new IllegalStateException("No figure-mps-crop.pdf in metapost output with exit code equal to zero");
	    output = b;
	    return true;
	}
	if (res.getExitCode() > 0)
	{
	    final var l = res.getTextOutputFiles().get("main.log");
	    if (l == null)
		throw new IllegalStateException("No main.log in pdflatex with exit code equal to " + res.getExitCode());
	    log = l;
	    return false;
	}
	stackTrace = res.getStackTrace();
	return false;
    }

    /**
     * Returns the compiled cropped PDF figure.
     *
     * @return the PDF as a byte array, or {@code null} if compilation has
     *         not been run or was unsuccessful
     */
    public byte[] getOutput()
    {
	return output;
    }

    /**
     * Returns the contents of {@code main.log} after a failed compilation.
     *
     * @return the log file as a list of lines, or {@code null} if the log
     *         was not captured
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
