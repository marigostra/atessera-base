// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.util;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import static java.nio.file.Files.*;

public final class TempDir implements AutoCloseable
{
    final Path path;

    public TempDir()
    {
	try {
	    path = createTempDirectory(Paths.get("/x"), ".atessera-");
	}
	catch(IOException ex)
	{
	    //FIXME: log
	    throw new RuntimeException(ex);
	}
    }

    @Override public void close()
    {
	//FIXME:
	try {
	    try (final var s = walk(path)){
		final var l = new ArrayList<>(s.toList());
		Collections.reverse(l);
		l.forEach(p -> {
			try {
			    delete(p);
			}
			catch(IOException ex)
			{
			    //FIXME: logging
			    throw new RuntimeException(ex);
			}
		    });
	    }
	}
	catch(IOException ex)
	{
	    //FIXME: log
	    throw new RuntimeException(ex);
	}
    }

    public File getFile()
    {
	return path.toFile();
    }

    public Path getPath()
    {
	return path;
    }

        public ShellCmd execShell(String cmd)
    {
	return new ShellCmd(cmd, getFile().getAbsolutePath());
    }


    public int execAndWait(String cmd)
    {
	//FIXME: output to log
	return execShell(cmd).waitFor();
    }

}
