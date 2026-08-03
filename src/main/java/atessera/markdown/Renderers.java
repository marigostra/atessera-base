// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import atessera.markdown.blocks.*;

public interface Renderers
{
    String render(AdvImageDefinition advImage);
    String render(CiteReference citeRef);
    String render(MathDefinition math);
    String render(MathBlockDefinition math);
    String renderHeadingOpening(int level);
    String renderHeadingClosing();
    String render(Label label);
    String render(Reference ref);
    String renderBegin(MultiBlock block);
    String renderEnd(MultiBlock block);
}
