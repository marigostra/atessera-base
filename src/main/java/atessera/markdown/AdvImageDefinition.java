// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import org.commonmark.node.CustomBlock;

public final class AdvImageDefinition extends CustomBlock
{
    private String src, alt, comment;

    public AdvImageDefinition(String src, String alt, String comment)
    {
        this.src = src;
	this.alt = alt;
	this.comment = comment;
    }

    public String getSrc()
    {
	return src;
    }

    public String getAlt()
    {
        return alt;
    }

    public String getComment()
    {
	return comment;
    }
}
