// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class CompilationTask
{
    private Map<String, List<String>> textSources;
    private Map<String, byte[]> binarySources;
    private List<String> commands;
    private List<String> saveTextFilesOnSuccess, saveBinaryFilesOnSuccess, saveTextFilesOnFailure;
}
