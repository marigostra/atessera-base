// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.renderers;

import java.util.*;

import org.commonmark.node.*;
import org.commonmark.renderer.*;

import org.commonmark.renderer.html.*;

import atessera.markdown.*;
import atessera.markdown.tex.*;
import atessera.markdown.blocks.*;

public abstract class HtmlNodeRenderer implements NodeRenderer
    {
	final Renderers renderers;
	final HtmlNodeRendererContext context;
	final HtmlWriter writer;
	
	public HtmlNodeRenderer(Renderers renderers, HtmlNodeRendererContext context)
	{
	    this.renderers = renderers;
	    this.context = context;
	    this.writer = context.getWriter();
	}


	protected abstract String onHeading(Heading heading, Map<String, String> attr);
	protected abstract String escape(String escape);


	@Override public void render(Node node)
	{
	    if (node instanceof Text text)
	    {
		writer.raw(escape(text.getLiteral()));
		return;
	    }
	    if (node instanceof Heading heading)
	    {
		final var attr = new HashMap<String, String>();
				final var prefix = onHeading(heading, attr);
		String htag = "h" + heading.getLevel();
		writer.line();
		writer.tag(htag, attr);
		if (prefix != null)
		    writer.raw(escape(prefix));
		renderChildren(heading);
		writer.tag('/' + htag);
		writer.line();
	    }
	    	    if (node instanceof Reference ref)
		writer.raw(renderers.render(ref));
		    	    	    if (node instanceof Label label)
		writer.raw(renderers.render(label));
				    
				    		if (node instanceof MultiBlock b)
	{
	    writer.raw(renderers.renderBegin(b));
	    renderChildren(b);
	    writer.raw(renderers.renderEnd(b));
	}

				    		    	    	    if (node instanceof MathDefinition math)
		writer.raw(renderers.render(math));
								    				    		    	    	    if (node instanceof MathBlockDefinition math)
		writer.raw(renderers.render(math));
	    if (node instanceof AdvImageDefinition advImage)
		writer.raw(renderers.render(advImage));
	    if (node instanceof CiteReference citeRef)
		writer.raw(renderers.render(citeRef));
	}
	
	private void renderChildren(Node node)
	{
	    for(Node n = node.getFirstChild();n != null; n = n.getNext())
		context.render(n);
	}

		@Override public Set<Class<? extends Node>> getNodeTypes()
	{
	    return new HashSet<>(Arrays.asList(
					       					       AdvImageDefinition.class,
										       MultiBlock.class,
					       Heading.class,
										       Label.class,
										       MathDefinition.class,
										       MathBlockDefinition.class,
										       Reference.class,
										       CiteReference.class,
										       Text.class));
	}

    }
