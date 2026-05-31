// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;

public interface CompilerTaskBuilder
{
    CompilerTask build();
    CompilerTaskBuilder format(Format format);
    CompilerTaskBuilder src(List<String> source);
    CompilerTaskBuilder input(String name, byte[] data);
}
