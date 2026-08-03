// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import atessera.markdown.blocks.*;

public class RenderersBase implements Renderers
{
@Override public String render(AdvImageDefinition advImage)
    {
	return advImage != null?advImage.toString():"";
    }

    @Override public String render(CiteReference citeRef)
    {
	return citeRef != null?citeRef.toString():"";
    }

    @Override public String render(MathDefinition math)
    {
	return math != null?math.toString():"";
    }

    @Override public String render(MathBlockDefinition math)
    {
	return math != null?math.toString():"";
    }

    @Override public String renderHeadingOpening(int level)
    {
	return "";
    }

        @Override public String renderHeadingClosing()
    {
	return "";
    }

    @Override public String render(Label label)
    {
		return label != null?label.toString():"";
    }

    @Override public String render(Reference ref)
    {
	return ref != null?ref.toString():"";
    }

        @Override public String renderBegin(MultiBlock block)
    {
	return "";
    }
    
    @Override public String renderEnd(MultiBlock block)
    {
	return "";
    }
}
    
