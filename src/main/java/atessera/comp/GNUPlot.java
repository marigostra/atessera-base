// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

import static java.util.Objects.*;

public final class GNUPlot
{
    private final Compiler compiler;
    private List<String> source;
    private byte[] output;
    private List<String> log;
    private List<List<String>> rawOutput, rawErrorOutput;
    private String stackTrace;
    
    public GNUPlot(Compiler compiler, List<String> source)
    {
	this.compiler = requireNonNull(compiler, "compiler can't be null");
	this.source = requireNonNull(source, "source can't be null");
    }

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
