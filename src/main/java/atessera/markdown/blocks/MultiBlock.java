// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.blocks;

import lombok.*;
import org.commonmark.node.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiBlock extends CustomBlock
{
    private String label, type, caption;

    public boolean isColumns()
    {
	return type != null && type.equals("[||]");
    }

        public boolean isAlert()
    {
	return type != null && type.equals("[!]");
    }

}
