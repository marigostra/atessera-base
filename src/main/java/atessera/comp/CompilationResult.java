// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CompilationResult
{
        private List<List<String>> output, errorOutput;
    private Map<String, List<String>> textOutputFiles;
    private Map<String, byte[]> binaryOutputFiles;
    private int exitCode;
    private String stackTrace;
}
