// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import org.apache.logging.log4j.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

/**
 * Facade for compiling LaTeX documents to PDF using {@code pdflatex}.
 *
 * <p>This class encapsulates a three-pass {@code pdflatex} pipeline:</p>
 *
 * <ol>
 *   <li>First pass &mdash; resolves cross-references.</li>
 *   <li>Second pass &mdash; resolves table of contents and bibliography.</li>
 *   <li>Third pass &mdash; finalises all references and produces the
 *       definitive PDF.</li>
 * </ol>
 *
 * <p>All three passes run with {@code --interaction=batchmode} to suppress
 * interactive prompts. The LaTeX source is written as {@code main.tex} in
 * the working directory. Additional text files (e.g. code listings) and
 * binary files (e.g. images) can be supplied alongside.</p>
 *
 * <p>After a successful compilation the resulting PDF is available via
 * {@link #getOutput()}. On failure the contents of {@code main.log} are
 * available via {@link #getLog()}.</p>
 *
 * @see Compiler
 * @see LocalCompiler
 */
public final class PdfLatex
{
    static private final Logger logger = LogManager.getLogger();
    
    private final Compiler compiler;
    private List<String> source;
    private Map<String, byte[]> images;
    private Map<String, List<String>> listings;
    private byte[] output;
    private List<String> log;
    private List<List<String>> rawOutput, rawErrorOutput;
    private String stackTrace;
    
    /**
     * Constructs a new {@code PdfLatex} compilation facade.
     *
     * @param compiler  the underlying {@link Compiler} to use; must not be
     *                  {@code null}
     * @param source    the LaTeX source as a list of lines; must not be
     *                  {@code null}
     * @param images    binary image files to include; keys are file names,
     *                  values are raw bytes; must not be {@code null}
     * @param listings  additional text files (e.g. code listings); keys are
     *                  file names, values are lists of lines; must not be
     *                  {@code null}
     */
    public PdfLatex(Compiler compiler, List<String> source,
		    Map<String, byte[]> images, Map<String, List<String>> listings)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
	this.images = requireNonNull(images, "images can't be null");
	this.listings = requireNonNull(listings, "listings can't be null");
    }

    /**
     * Runs the three-pass {@code pdflatex} pipeline.
     *
     * @return {@code true} if all three passes completed successfully and
     *         the PDF was produced; {@code false} otherwise
     * @throws IllegalStateException if the compiler result is inconsistent
     *         (e.g. zero exit code but no PDF, or non-zero exit code but
     *         no log file)
     */
    public boolean compile()
    {
	//	logger.info("Compiling {}", source.stream().collect(joining("\n")));
	final var task = new CompilationTask();
	task.setCommands(List.of(
				 "pdflatex --interaction=batchmode main.tex",
				 				 "pdflatex --interaction=batchmode main.tex",
				 				 "pdflatex --interaction=batchmode main.tex"
				 ));
	task.setTextSources(new HashMap<>());
	task.getTextSources().put("main.tex", source);
	task.getTextSources().putAll(listings);
	task.setBinarySources(images);
	task.setSaveTextFilesOnFailure(List.of("main.log"));
	task.setSaveBinaryFilesOnSuccess(List.of("main.pdf"));
	task.setSaveTextFilesOnSuccess(Collections.emptyList());
	final var res = compiler.compile(task);
	rawOutput = res.getOutput();
	rawErrorOutput = res.getErrorOutput();
	if (res.getExitCode() == 0)
	{
	    final var b = res.getBinaryOutputFiles().get("main.pdf");
	    if (b == null)
		throw new IllegalStateException("No main.pdf in pdflatex output with exit code equal to zero");
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
     * Returns the compiled PDF document.
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
     *         was not captured (e.g. because compilation succeeded or has
     *         not been run)
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
