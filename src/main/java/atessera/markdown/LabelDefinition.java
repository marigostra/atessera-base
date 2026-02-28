// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import org.commonmark.node.CustomBlock;

public final class LabelDefinition extends CustomBlock
{
    private String label;

    public LabelDefinition(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    @Override public String toString()
    {
	return new String(new StringBuilder()
			  .append("(label \"")
			  .append(label)
			  .append("\")"));
    }
}
