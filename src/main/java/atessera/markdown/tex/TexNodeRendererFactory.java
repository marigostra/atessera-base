// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.tex;

import org.commonmark.renderer.NodeRenderer;

public interface TexNodeRendererFactory
{
    NodeRenderer create(TexNodeRendererContext context);
}
