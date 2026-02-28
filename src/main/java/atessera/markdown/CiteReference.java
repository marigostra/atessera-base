// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import org.commonmark.node.CustomNode;

public class CiteReference extends CustomNode
{
    private String ref;

    public CiteReference(String ref)
    {
        this.ref = ref;
    }

    public String getRef()
    {
        return ref;
    }
}
