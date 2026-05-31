// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import java.io.*;

public interface CompilerTask extends AutoCloseable
{
    static public final String
	LATEX_OUTPUT_FILE = "main.pdf",
	IMAGE_PNG = "main.png",
	IMAGE_EPS = "main.eps",
		IMAGE_PDF = "main.pdf",
		IMAGE_SVG = "main.svg";

    Map<String, byte[]> run() throws IOException;
    int getExitCode();
    String getStdout();
    String getStderr();
    void close();
}
