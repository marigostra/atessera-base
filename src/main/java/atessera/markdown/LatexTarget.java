// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.*;
import java.util.stream.*;
import org.commonmark.parser.*;
import org.commonmark.parser.block.*;
import org.commonmark.parser.beta.*;

import atessera.markdown.tex.*;
import atessera.markdown.parsers.*;
import atessera.markdown.renderers.*;
import atessera.markdown.blocks.*;

import static atessera.util.LatexUtils.*;

public class LatexTarget extends RenderersBase
{
    public enum Features {ADV_IMAGE, CITE, LABEL, REFERENCES, MATH, SILENT_BIB_ITEMS};

    public final Parser parser;
    public final org.commonmark.renderer.Renderer renderer;
    private final Set<Features> features;
    public final Map<String, String> biblio = new HashMap<>();

    public LatexTarget(Set<Features> features)
    {
	final var p = new Parser.Builder();
	if (features.contains(Features.ADV_IMAGE))
	    p.customBlockParserFactory(new AdvImageBlockParser.Factory());
	if (features.contains(Features.LABEL))
	    p.customBlockParserFactory(new LabelParserFactory());
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
	    p.customBlockParserFactory(new BibItemParserFactory());
	}
	p.inlineParserFactory(c -> new atessera.markdown.cust.InlineParserImpl(c, false));
	this.parser = p.build();
	this.renderer = new TexRenderer.Builder()
	.nodeRendererFactory(c -> new TexNodeRenderer(this, c, features.contains(Features.SILENT_BIB_ITEMS)))
	.build();
	this.features = features;
    }

    public LatexTarget(List<BlockParserFactory> blockFactories, List<LinkProcessor> linkProcs, boolean silentBibItems)
    {
	final var p = new Parser.Builder();
	for(var f: blockFactories)
	    p.customBlockParserFactory(f);
	for(var pr: linkProcs)
	    p.linkProcessor(pr);
		p.inlineParserFactory(c -> new atessera.markdown.cust.InlineParserImpl(c, false));
	this.parser = p.build();
	this.renderer = new TexRenderer.Builder()
	.nodeRendererFactory(c -> new TexNodeRenderer(this, c, silentBibItems))
	.build();
	this.features = EnumSet.noneOf(Features.class);
    }


    public LatexTarget()
    {
	this(EnumSet.noneOf(Features.class));
    }

            @Override public String renderBegin(BibItem bibItem)
    {
	return "\\bibitem{" + escapeRelaxed(bibItem.getLabel()) + "}\n";
    }
    
    @Override public String renderEnd(BibItem bibItem)
    {
		return "\n";
    }


    @Override public String renderHeadingOpening(int level)
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

        @Override public String renderHeadingClosing()
    {
	return "}\n\n";
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
    }
