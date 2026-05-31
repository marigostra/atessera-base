// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp.local;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.*;
import org.apache.logging.log4j.*;

import atessera.util.*;
import atessera.comp.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.apache.commons.io.IOUtils.*;
import static java.nio.file.Files.*;
import static atessera.util.TextUtils.*;

final class LocalTask implements CompilerTask
{
    static private final Logger log = LogManager.getLogger();
    static private String
	CHARSET = "UTF-8",
	CMD = "chmod 777 main.sh && ./main.sh";

    final String scriptName;
    final TempDir temp = new TempDir();
    final Map<String, List<String>> textInputFiles;
    final Map<String, byte[]> binaryInputFiles, outputFiles;
    private int exitCode = -1;
    private final List<String> stdOut = new ArrayList<>(), stdErr = new ArrayList<>();

    LocalTask(String scriptName,
			 Map<String, List<String>> textInputFiles,
			 Map<String, byte[]> binaryInputFiles,
			 Map<String, byte[]> outputFiles)
    {
	this.scriptName = requireNonNull(scriptName, "scriptName can't be null");
	this.textInputFiles = requireNonNull(textInputFiles, "textInputFiles can't be null");
	this.binaryInputFiles = requireNonNull(binaryInputFiles, "binaryInputFiles can't be null");
	this.outputFiles = requireNonNull(outputFiles, "outputFiles can't be null");
    }

    @Override public Map<String, byte[]> run() throws IOException
    {
	final Path tempDir = temp.getPath();
	writeTextFile(tempDir.resolve("main.sh"), readJavaResource(getClass(), scriptName + ".sh"));
	for(final var e: textInputFiles.entrySet())
	    writeTextFile(tempDir.resolve(e.getKey()), e.getValue());
		for(final var e: binaryInputFiles.entrySet())
	{
	    try (var os = newOutputStream(tempDir.resolve(e.getKey()))) {
		copy(new ByteArrayInputStream(e.getValue()), os);
	    }
	}
		log.info("Running {} in {}", CMD, tempDir.toString());
		final var c = temp.execShell(CMD);
				exitCode = c.waitFor();
				if (exitCode > 0)
				{
				    log.error("Failed local compilation in {} with exit code {}", tempDir.toString(), exitCode);
				    for(int i = 0;i < Math.min(c.error.size(), 10);i++)
					log.trace(scriptName + " stderr: " + c.error.get(i));
		    throw new CompilationException(exitCode, stdOut, stdErr);				    
				}
				    				    log.trace("Running " + scriptName + " with '" + CMD + "' in " + tempDir.toString() + " succeeded with exit code 0");
		stdOut.addAll(c.output);
				stdErr.addAll(c.error);
	final var fetched = new HashMap<String, byte[]>();
	for(final var e: outputFiles.entrySet())
	{
	    final var buf = new ByteArrayOutputStream();
	    try (var is = newInputStream(tempDir.resolve(e.getKey()))) {
		copy(is, buf);
	}
	    fetched.put(e.getKey(), buf.toByteArray());
	    log.trace("Fetched " + e.getKey() + " of " + fetched.get(e.getKey()).length + " bytes (id=" + tempDir.toString() + ")");
	}
	outputFiles.putAll(fetched);
	log.trace("{} compilation finished", scriptName);
	return outputFiles;
    }

    @Override public void close()
    {
	temp.close();
    }

    @Override public int getExitCode()
    {
	if (exitCode < 0)
	    throw new IllegalStateException("Requesting exit code before finishing the execution of the command");
	return exitCode;
    }

    @Override public String getStdout()
    {
	return stdOut.stream().collect(joining("\n"));
	    }

        @Override public String getStderr()
    {
		return stdErr.stream().collect(joining("\n"));

    }
    }
