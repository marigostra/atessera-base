// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import org.commonmark.node.CustomBlock;

public final class CiteDefinition extends CustomBlock
{
    private final String ref, text;

    public CiteDefinition(String ref, String text)
    {
        this.ref = ref;
	this.text = text;
    }

    public String getRef()
    {
	return ref;
    }

    public String getText()
    {
        return text;
    }
}
