// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

//TODO: В~нашем университете прошла конференция [AINL-2026](https://ainlconf.ru/2026/program "Открыть программу выступлений на сайте конференции")~---

import java.util.*;
import java.util.stream.*;

import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.*;
import org.commonmark.renderer.html.*;
import org.commonmark.internal.util.Escaping;
import org.commonmark.parser.block.*;

import atessera.markdown.blocks.*;
import atessera.markdown.parsers.*;
import atessera.markdown.renderers.*;

import static java.util.Objects.*;
import static java.lang.Character.*;

public class HtmlTarget extends RenderersBase
{
    static public final char
	NBSP = 160,
	LQUOT = 171,
	RQUOT = 187,
	MDASH = 0x2014,
	NDASH = 0x2013;

    public enum Features {ADV_IMAGE, CITE, LABEL, EXT_CHARS, REFERENCES, MATH};

    public final Parser parser;
    private final Set<Features> features;
    public final Map<String, String> biblio = new HashMap<>();

    public HtmlTarget(Set<Features> features, List<BlockParserFactory> blockParserFactories, boolean allowHtmlInlines)
    {
	this.features = features;	
	final var p = new Parser.Builder();
	if (blockParserFactories != null)
	for(var f: blockParserFactories)
	    p.customBlockParserFactory(f);
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
	    p.customBlockParserFactory(new CiteBlockParser.Factory());
	}
	p.inlineParserFactory(c -> new atessera.markdown.cust.InlineParserImpl(c, allowHtmlInlines));
	this.parser = p.build();
    }

    public HtmlTarget(Set<Features> features, List<BlockParserFactory> blockParserFactories)
    {
	this(features, blockParserFactories, false);
    }

        public HtmlTarget(Set<Features> features)
    {
	this(features, Collections.emptyList());
    }

    public HtmlTarget()
    {
	this(EnumSet.noneOf(Features.class));
    }

    public RefTranslation translateRef(String ref)
    {
	return new RefTranslation(ref);
    }

    public String getRefTitle(RefTranslation refTranslation)
    {
	return null;
    }

    @Override public String render(AdvImageDefinition advImage)
    {
	return new String(new StringBuilder()
			  .append("<img src=\"")
			  .append(extChars(Escaping.escapeHtml(advImage.getSrc())))
			  .append("\" alt=\"")
			  .append(escape(advImage.getAlt()))
			  .append("\">\n"));
    }

    protected String onHeading(int level, StringBuilder builder)
    {
	return "";
    }

    protected void updateAttributes(String tagName, Map<String, String> attr, RefTranslation refTranslation)
    {
    }


	public Renderer newRenderer(StringBuilder b)
	{
return new HtmlRenderer.Builder()
	.attributeProviderFactory(c -> new AnchorAttributes())
	.nodeRendererFactory(c -> new HtmlNodeRenderer(this, c){
		@Override protected String onHeading(Heading heading, Map<String, String> attr) { return HtmlTarget.this.onHeading(heading.getLevel(), b); }
		@Override protected String escape(String text) { return HtmlTarget.this.escape(text); }
	    })
	.escapeHtml(false)
	.build();
    }

	    public String parse(String text)
	    {
					final var b = new StringBuilder();
		final var renderer = newRenderer(b);
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

    public String parse(String text, List<Integer> splits)
    {
	final var b = new StringBuilder();
	final var renderer = new HtmlRenderer.Builder()
	.attributeProviderFactory(c -> new AnchorAttributes())
	.nodeRendererFactory(c -> new HtmlNodeRenderer(this, c){
		@Override protected String onHeading(Heading heading, Map<String, String> attr)
		{
		    if (heading.getLevel() == 1)
			splits.add(Integer.valueOf(b.length()));
		    return HtmlTarget.this.onHeading(heading.getLevel(), b);
		}
		@Override protected String escape(String text)
		{
		    return HtmlTarget.this.escape(text);
		}
	    })
	.escapeHtml(false)
	.build();
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

    public String escape(String text)
    {
	var t = text;
	if (features.contains(Features.EXT_CHARS))
	    t = extChars(t);
	t = Escaping.escapeHtml(t);
	return t;
    }

    static public String extChars(String text)
    {
	final var b = new StringBuilder();
	boolean wasEscaping = false;
	for(int i = 0;i < text.length();i++)
	{
	    final char
	    c = text.charAt(i),
	    cc = (i + 1 < text.length())?text.charAt(i + 1):'\0',
	    ccc = (i + 2 < text.length())?text.charAt(i + 2):'\0';
	    if (wasEscaping)
	    {
		b.append(c);
		wasEscaping = false;
		continue;
	    }
	    switch(c)
	    {
	    case '\\':
		wasEscaping = true;
		continue;
	    case '~':
		if (i > 0 && !isWhitespace(text.charAt(i)))
		    b.append(NBSP); else
		    b.append("~");
		continue;
	    case '<':
		if (cc == '<')
		{
		    b.append(LQUOT);
		    i++;
		    continue;
		}
		b.append("<");
		continue;
	    case '>':
		if (cc == '>')
		{
		    b.append(RQUOT);
		    i++;
		    continue;
		}
		b.append(">");
		continue;
	    case '-':
		if (ccc == '-' && cc == '-')
		{
		    b.append(MDASH);
		    i += 2;
		    continue;
		}
		if (cc == '-')
		{
		    b.append(NDASH);
		    i++;
		    continue;
		}
		b.append("-");
		continue;
	    default:
		b.append(c);
	    }
	}
	return new String(b);
    }

    final class AnchorAttributes implements AttributeProvider
    {
	@Override public void setAttributes(Node node, String tagName, Map<String,String> attr)
	{
	    if (!tagName.equalsIgnoreCase("a"))
	    {
		updateAttributes(tagName.toLowerCase(), attr, null);
		return;
	    }
	    final var origRef = requireNonNull(attr.get("href"), "The attr map must contain the 'href' entry");
	    final var tr = translateRef(origRef);
	    if (tr != null)
		attr.put("href", tr.getTranslatedRef());
	    if (!attr.containsKey("title"))
	    {
		var title = getRefTitle(requireNonNullElse(tr, new RefTranslation(origRef)));
		if (title != null && !title.trim().isEmpty())
		{
		    if (features.contains(Features.EXT_CHARS))
			title = extChars(title);
		    attr.put("title", title.trim());
		}
	    } else
	    {
		if (features.contains(Features.EXT_CHARS))
		    attr.put("title", extChars(attr.get("title")));
	    }
	    updateAttributes("a", attr, requireNonNullElse(tr, new RefTranslation(origRef)));
	}
    }

}
