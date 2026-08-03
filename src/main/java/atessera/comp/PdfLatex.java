// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import org.apache.logging.log4j.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

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
    

    public PdfLatex(Compiler compiler, List<String> source,
		    Map<String, byte[]> images, Map<String, List<String>> listings)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
	this.images = requireNonNull(images, "images can't be null");
	this.listings = requireNonNull(listings, "listings can't be null");
    }

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

    public byte[] getOutput()
    {
	return output;
    }

    public List<String> getLog()
    {
	return log;
    }

    public List<List<String>> getRawOutput()
    {
	return rawOutput;
    }

        public List<List<String>> getRawErrorOutput()
    {
	return rawErrorOutput;
    }

    public String getStackTrace()
    {
	return stackTrace;
    }
}
