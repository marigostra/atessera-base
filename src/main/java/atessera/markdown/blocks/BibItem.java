// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.blocks;

import org.commonmark.node.CustomBlock;
import lombok.*;

@Data
@AllArgsConstructor
public final class BibItem extends CustomBlock
{
    private String label;
}
