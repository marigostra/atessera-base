// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import lombok.*;
import org.commonmark.node.CustomNode;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class Reference extends CustomNode
{
    public enum Type {REGULAR , PAGE};

    private Type type;
    private String ref;
}
