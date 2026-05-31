// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import java.io.*;

import atessera.comp.local .LocalTaskBuilder;

public final class CompilerFactory
{
    static public CompilerTaskBuilder newCompilerTaskBuilder(String type)
    {
	return new LocalTaskBuilder();
    }
}
