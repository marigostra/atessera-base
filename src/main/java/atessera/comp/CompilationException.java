// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public final class CompilationException extends RuntimeException
{
    private final List<String> stdOut, stdErr;

    public CompilationException(int exitCode, List<String> stdOut, List<String> stdErr)
    {
	super(stdErr.stream().collect(joining("\n")));
    this.stdOut = stdOut;
    this.stdErr = stdErr;
}

    public List<String> getStdOut()
    {
	return unmodifiableList(stdOut);
    }

    public List<String> getStdErr()
    {
	return unmodifiableList(stdErr);
    }
}
