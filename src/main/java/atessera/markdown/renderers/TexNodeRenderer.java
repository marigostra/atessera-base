// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.renderers;

import java.util.*;

import org.commonmark.node.*;
import org.commonmark.renderer.*;

import atessera.markdown.*;
import atessera.markdown.tex.*;
import atessera.markdown.blocks.*;

public class TexNodeRenderer implements NodeRenderer
{
    final Renderers renderers;
    final TexNodeRendererContext context;
    final TexWriter writer;
	
    public TexNodeRenderer(Renderers renderers, TexNodeRendererContext context)
    {
	this.renderers = renderers;
	this.context = context;
	this.writer = context.getWriter();
    }
	
    
    @Override public void render(Node node)
    {
	if (node instanceof Heading h)
	{
	    writer.write(renderers.renderHeadingOpening(h.getLevel()));
	    renderChildren(h);
	    writer.write(renderers.renderHeadingClosing());
	}
		if (node instanceof MultiBlock b)
	{
	    writer.write(renderers.renderBegin(b));
	    renderChildren(b);
	    writer.write(renderers.renderEnd(b));
	}
	if (node instanceof CiteReference citeRef)
	    writer.write(renderers.render(citeRef));
	if (node instanceof MathDefinition math)
	    writer.write(renderers.render(math));
	if (node instanceof MathBlockDefinition math)
	    writer.write(renderers.render(math));
	if (node instanceof Label label)
	    writer.write(renderers.render(label));
	if (node instanceof Reference ref)
	    writer.write(renderers.render(ref));
    }

        @Override public Set<Class<? extends Node>> getNodeTypes()
    {
	return new HashSet<>(Arrays.asList(
					   Heading.class,
					   Reference.class,
					   MultiBlock.class,
					   CiteReference.class,
					   MathDefinition.class,
					   MathBlockDefinition.class,
					   Label.class));
    }

    
    private void renderChildren(Node node)
    {
	for(Node n = node.getFirstChild();n != null; n = n.getNext())
	    context.render(n);
    }
}
