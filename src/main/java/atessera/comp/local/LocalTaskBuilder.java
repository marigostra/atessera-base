// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp.local;

import java.util.*;

import static java.util.Objects.*;
import atessera.comp.*;

public final class LocalTaskBuilder implements CompilerTaskBuilder
{
    private Format format = null ;
    private final Map<String, List<String>> inputSources = new HashMap<>();
    private final Map<String, byte[]>
	inputFiles = new HashMap<>(),
	outputFiles = new HashMap<>();

    public CompilerTask build()
    {
	requireNonNull(format, "format is not set");
	return new LocalTask(format.toString().toLowerCase(), inputSources, inputFiles, outputFiles);
    }

    @Override public CompilerTaskBuilder format(Format format)
    {
	requireNonNull(format, "format can't be null");
	this.format = format;
	switch(format)
	{
	case LATEX:
	    outputFiles.put(CompilerTask.LATEX_OUTPUT_FILE, new byte[0]);
	    return this;
	case PDFLATEX:
	    outputFiles.put(CompilerTask.LATEX_OUTPUT_FILE, new byte[0]);
	    return this;
	case METAPOST:
	case GNUPLOT:
	    outputFiles.put(CompilerTask.IMAGE_EPS, new byte[0]);
	    outputFiles.put(CompilerTask.IMAGE_PNG, new byte[0]);
	    outputFiles.put(CompilerTask.IMAGE_PDF, new byte[0]);
	    outputFiles.put(CompilerTask.IMAGE_SVG, new byte[0]);
	    return this;
	case PLANTUML:
	case DOT:
	case NEATO:
	case TWOPI:
	case CIRCO:
	    outputFiles.put(CompilerTask.IMAGE_EPS, new byte[0]);
	    outputFiles.put(CompilerTask.IMAGE_PNG, new byte[0]);
	    return this;
	default:
	    throw new IllegalArgumentException("Unsupported format: " + format.toString());
	}
    }

    @Override public CompilerTaskBuilder src(List<String> source)
    {
	requireNonNull(source, "source can't be null");
	requireNonNull(format, "format is not set");
	switch(format)
	{
	case LATEX:
	    inputSources.put("main.tex", source);
	    return this;
	case PDFLATEX:
	    inputSources.put("main.tex", source);
	    return this;
	case PLANTUML:
	    inputSources.put("main.uml", source);
	    return this;
	case METAPOST:
	    inputSources.put("main.mp", source);
	    return this;
	case GNUPLOT:
	    inputSources.put("main.plot", source);
	    return this;
	case DOT:
	    inputSources.put("main.dot", source);
	    return this;
	case NEATO:
	    inputSources.put("main.neato", source);
	    return this;
	case TWOPI:
	    inputSources.put("main.twopi", source);
	    return this;
	case CIRCO:
	    inputSources.put("main.circo", source);
	    return this;
	}
	throw new IllegalArgumentException("Unsupported format: " + format.toString());
    }

    @Override public CompilerTaskBuilder input(String name, byte[] data)
    {
	requireNonNull(name, "name can't be empty");
	requireNonNull(data, "data can't be null");
	if (name.isEmpty())
	    throw new IllegalArgumentException("name can't be null");
	inputFiles.put(name, data);
	return this;
    }
}
