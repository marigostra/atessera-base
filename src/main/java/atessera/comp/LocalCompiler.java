// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;

import org.apache.logging.log4j.*;

import atessera.comp.*;
import atessera.util.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.apache.commons.io.IOUtils.*;
import static java.nio.file.Files.*;
import static atessera.util.TextUtils.*;

public final class LocalCompiler implements Compiler
{
    static private final Logger log = LogManager.getLogger();
    static private String
	CHARSET = "UTF-8";

    @Override public CompilationResult compile(CompilationTask task)
    {
	final var res = new CompilationResult();
	res.setOutput(new ArrayList<>());
	res.setErrorOutput(new ArrayList<>());
	res.setTextOutputFiles(new HashMap<>());
	res.setBinaryOutputFiles(new HashMap<>());
	res.setExitCode(0);
	try {
	    try(final var temp = new TempDir()) {
		final Path tempDir = temp.getPath();
		for(final var e: task.getTextSources().entrySet())
		    writeTextFile(tempDir.resolve(e.getKey()), e.getValue());
		for(final var e: task.getBinarySources().entrySet())
		{
		    try (var os = newOutputStream(tempDir.resolve(e.getKey()))) {
			copy(new ByteArrayInputStream(e.getValue()), os);
		    }
		}
		for(final var cmd: task.getCommands())
		{
		    log.trace("Running {} in {}", cmd, tempDir.toString());
		    final var c = temp.execShell(cmd);
		    final int exitCode = c.waitFor();
		    res.getOutput().add(new ArrayList<>(c.output));
		    res.getErrorOutput().add(new ArrayList<>(c.error));
		    if (exitCode != 0)
		    {
			log.error("Compilation failed in {} with exit code {}", tempDir.toString(), exitCode);
			res.setExitCode(exitCode);
			for(final var f: task.getSaveTextFilesOnFailure())
			{
			    final var ff = tempDir.resolve(f);
			    if (exists(ff))
			    {
			    log.trace("Saving {} from {}", f, tempDir.toString());
			    res.getTextOutputFiles().put(f, readTextFile(ff));
			    }
			    else
				log.warn("No file {} requested to be saved on compilation failure", ff.toString());
			}
			return res;
		    }
		}
		    for(final var f: task.getSaveTextFilesOnSuccess())
		    {
			final var ff = tempDir.resolve(f);
			if (exists(ff))
			{
			log.trace("Saving {} from {}", f, tempDir.toString());
			res.getTextOutputFiles().put(f, readTextFile(ff));
			} else
			    				log.warn("No file {} requested to be saved on compilation failure", ff.toString());
		    }
		    for(final var f: task.getSaveBinaryFilesOnSuccess())
		    {
			final var ff = tempDir.resolve(f);
			if (exists(ff))
			{
			log.trace("Saving {} from {}", f, tempDir.toString());
			final var buf = new ByteArrayOutputStream();
			try (var is = newInputStream(ff)) {
			    copy(is, buf);
			}
			res.getBinaryOutputFiles().put(f, buf.toByteArray());
			} else
			    			    				log.warn("No file {} requested to be saved on compilation failure", ff.toString());
		    }
		return res;
	    }
	}
	catch(Throwable ex)
	{
	    log.error("Exception during local compilation", ex);
	    final var wr = new StringWriter();
	    ex.printStackTrace(new PrintWriter(wr));
	    res.setExitCode(-1);
	    res.setStackTrace(wr.toString());
	    return null;
	}
    }
}
