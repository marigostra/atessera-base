// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.*;
import java.util.stream.*;

import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.*;
import org.commonmark.renderer.html.*;
import org.commonmark.internal.util.Escaping;

//import org.apache.logging.log4j.*;

import atessera.markdown.tex.*;

import static java.util.Objects.*;

public class LatexTarget
{
    public enum Features {ADV_IMAGE, CITE, LABEL, REFERENCES, MATH};

    public final Parser parser;
    final org.commonmark.renderer.Renderer renderer;
    private final Set<Features> features;
    public final Map<String, String> biblio = new HashMap<>();

    public LatexTarget(Set<Features> features)
    {
	final var p = new Parser.Builder();
	if (features.contains(Features.ADV_IMAGE))
	    p.customBlockParserFactory(new AdvImageBlockParser.Factory());
	if (features.contains(Features.LABEL))
	    p.customBlockParserFactory(new LabelBlockParser.Factory());
	if (features.contains(Features.MATH))
	{
	    p.linkProcessor(new MathLinkProcessor());
	    p.customBlockParserFactory(new MathBlockParser.Factory());
	}
	if (features.contains(Features.REFERENCES))
	    p.linkProcessor(new RefLinkProcessor());
	if (features.contains(Features.CITE))
	{
	    p.linkProcessor(new CiteLinkProcessor());
	    p.customBlockParserFactory(new CiteBlockParser.Factory());
	}
	p.inlineParserFactory(c -> new atessera.markdown.cust.InlineParserImpl(c, false));
	this.parser = p.build();
	this.renderer = new TexRenderer.Builder()
	.nodeRendererFactory(c -> new TexNodeRenderer(c))
	.build();
	this.features = features;
    }

    public LatexTarget()
    {
	this(EnumSet.noneOf(Features.class));
    }

    protected String render(AdvImageDefinition advImage)
    {
	return "";
    }

    protected String render(CiteReference citeRef)
    {
	return citeRef != null?citeRef.toString():"null";
    }

    protected String render(MathDefinition math)
    {
	return math != null?math.toString():"null";
    }

    protected String render(MathBlockDefinition math)
    {
	return math != null?math.toString():"null";
    }

    protected String renderHeadingOpening(int level)
    {
	switch(level)
	{
	case 1:
	    return "\\section{";
	case 2:
	    	    return "\\subsection{";
	case 3:
	    	    return "\\subsubsection{";
	}
	return "{";
    }

        protected String renderHeadingClosing()
    {
	return "}\n\n";
    }

    protected String render(LabelDefinition label)
    {
		return label != null?label.toString():"null";
    }

    protected String render(Reference ref)
    {
	return ref != null?ref.toString():"null";
    }

    public String parse(String text)
    {
	final var b = new StringBuilder();
	final var doc = parser.parse(text);
	if (features.contains(Features.CITE))
	{
	    new EnumNodes(n -> {
		    if (n instanceof CiteDefinition cite)
			biblio.put(cite.getRef().trim(), cite.getText().trim());
	    }).enumerate(doc);
	}
	renderer.render(doc, b);
	return new String(b);
    }

    public List<String> parse(List<String> text)
    {
	final var t = text.stream().collect(Collectors.joining("\n")) + "\n";
	return Arrays.asList(parse(t).split("\n"));
    }

    final class TexNodeRenderer implements NodeRenderer
    {
	final TexNodeRendererContext context;
	final TexWriter writer;
	TexNodeRenderer(TexNodeRendererContext context)
	{
	    this.context = context;
	    this.writer = context.getWriter();
	}
	@Override public Set<Class<? extends Node>> getNodeTypes()
	{
	    return new HashSet<>(Arrays.asList(
					       					       Heading.class,
										       					       Reference.class,
					       CiteReference.class,
					       MathDefinition.class,
					       					       MathBlockDefinition.class,
					       LabelDefinition.class));
	}
	@Override public void render(Node node)
	{
	    if (node instanceof CiteReference citeRef)
		writer.write(LatexTarget.this.render(citeRef));
	    	    if (node instanceof MathDefinition math)
		writer.write(LatexTarget.this.render(math));
		    	    	    if (node instanceof MathBlockDefinition math)
												writer.write(LatexTarget.this.render(math));
	    	    	    if (node instanceof LabelDefinition label)
				writer.write(LatexTarget.this.render(label));
			    if (node instanceof Reference ref)
			       				writer.write(LatexTarget.this.render(ref));
	    	    if (node instanceof Heading h)
		    {
			writer.write(renderHeadingOpening(h.getLevel()));
			renderChildren(h);
			writer.write(renderHeadingClosing());
		    }
	}
	private void renderChildren(Node node)
	{
	    for(Node n = node.getFirstChild();n != null; n = n.getNext())
		context.render(n);
	    	}
    }
}
