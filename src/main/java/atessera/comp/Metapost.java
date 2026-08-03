// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

import static java.util.Objects.*;

public final class Metapost
{
    private final Compiler compiler;
    private List<String> source;
    private byte[] output;
    private List<String> log;
    private List<List<String>> rawOutput, rawErrorOutput;
    private String stackTrace;
    
    public Metapost(Compiler compiler, List<String> source)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
    }

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
