// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import lombok.*;
import org.commonmark.node.CustomBlock;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class MathBlockDefinition extends CustomBlock
{
    public enum Type {REGULAR, EQUATION};
    private Type type;
    private String text, label;
}
