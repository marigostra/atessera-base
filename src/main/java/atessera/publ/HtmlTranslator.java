// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.util.concurrent.atomic.*;
import java.io.*;

import atessera.json.*;
import atessera.markdown.*;
import atessera.markdown.blocks.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.util.IdStr.*;
import static atessera.json.PublicationContent.*;

public final class HtmlTranslator implements AutoCloseable
{
static final int
    ID_LEN = 10;

        private final HtmlTarget markdown = new HtmlTarget(EnumSet.allOf(HtmlTarget .Features.class)){
			    @Override public String escape(String text) { return super.escape(text).replaceAll("\u00A0", "&#160;");}
	    @Override public String render(AdvImageDefinition image) { return "IMAGE"; }
		@Override public String render(Reference ref) { return HtmlTranslator.this.getReference(ref); }
				@Override public String render(CiteReference citeRef) { return HtmlTranslator.this.getCiteRef(citeRef); }
				@Override public String render(MathDefinition math) { return HtmlTranslator.this.getMath(math); }
		@Override public String onHeading(int level, StringBuilder b) { return HtmlTranslator.this.getHeading(level); }
	};

    final PublicationContent publ;
    final Map<String, List<String>> output = new HashMap<>();
    final Map<String, String> refs = new HashMap<>(), refIds = new HashMap<>();
    final Map<String, String> biblio = new HashMap<String, String>();
    final Map<String, Integer> refChapNums = new HashMap<>();
    private int h1Num = 0, h2Num = 0, h3Num = 0;


    public HtmlTranslator(PublicationContent publ) throws IOException
    {
	requireNonNull(publ, "publ can't be null");
	this.publ = publ;
    }

    void translate() throws IOException
    {
	refs.clear();
	fillRefs();
	var chap = new ArrayList<String>();
	int chapNum = 0;
	for(final Section s: requireNonNullElse(publ.getSections(), new ArrayList<Section>()))
	{
	    switch(requireNonNullElse(s.getType(), SectionType.MARKDOWN))
	    {
	    case MARKDOWN: {
		final var splits = new ArrayList<Integer>();
		final var text = requireNonNullElse(s.getSource(), new ArrayList<String>())
		.stream()
		.collect(joining("\n"));
		final var rendered = markdown.parse(text, splits);
		int prevPos = 0;
		for(final int p: splits)
		{
		    		    final var chunk = rendered.substring(prevPos, p);
		    chap.addAll(Arrays.asList(chunk.split("\n")));
		    output.put(getChapterFileName(chapNum), chap );
		    chapNum++;
		    chap = new ArrayList<>();
		    prevPos = p;
		}
				    		    final var chunk = rendered.substring(prevPos);
		    chap.addAll(Arrays.asList(chunk.split("\n")));


		break;
	    }
	}

    }
	output.put("chap" + chapNum, chap);
    }

    String getReference(Reference ref)
    {
	if (ref.getType() == null || ref.getRef() == null)
	    throw new IllegalArgumentException("Illegal ref object");
	switch(ref.getType())
	{
	case REGULAR: {
	    	    final var id = refIds.get(ref.getRef().trim());
		    	    final var chap = refChapNums.get(ref.getRef().trim());
	    final var res = refs.get(ref.getRef().trim());
	    if (res == null)
		throw new IllegalArgumentException("Unknown reference: " + ref.getRef());
	    if (id != null && chap != null)
		return "<a href=\"" + getChapterFileName(chap.intValue()) + "#" + id + "\">" + res + "</a>";
	    return res;
	}
	case PAGE:
	    return "!СТРАНИЦА ОТСУТСТВУЕТ!";
	}
	return "";
    }

        String getCiteRef(CiteReference citeRef)
    {
	if (citeRef.getRef() == null)
	    throw new IllegalArgumentException("Illegal cite ref object");
	for(final var ref : citeRef.getRef().split(",", -1))
	{
    	    final var res = biblio.get(ref.trim());
	    if (res == null)
		throw new IllegalArgumentException("Unknown cite reference: " + citeRef.getRef());
	}
	return "";
    }


        String getMath(MathDefinition math)
    {
	if (math.getText() == null)
	    return "!ВНУТРЕННЯЯ ОШИБКА!";
	return markdown.escape(math.getText());
    }

    String getHeading(int level)
    {
	switch(level)
	{
	case 1:
	    h1Num++;
	    h2Num = 0;
	    h3Num = 0;
	    return "Глава~" + h1Num + ". ";
	case 2:
	    h2Num++;
	    h3Num = 0;
	    return String.valueOf(h1Num) + "." + String.valueOf(h2Num) + " ";
	case 3:
	    h3Num++;
	    	    return String.valueOf(h1Num) + "." + String.valueOf(h2Num) + "." + h3Num + " ";
	default:
	    throw new RuntimeException("Unsupported heading level: " + level);
	}
    }


    void fillRefs()
    {
	final AtomicInteger
	h1Num = new AtomicInteger(0),
	h2Num = new AtomicInteger(0),
	h3Num = new AtomicInteger(0),
	eqNum = new AtomicInteger(1),
	listNum = new AtomicInteger(1),
	pictNum = new AtomicInteger(1);
	final var m = new HtmlTarget(EnumSet.allOf(HtmlTarget.Features.class)){
		@Override public String onHeading(int level, StringBuilder b)
		{
		    if (level == 1)
		    {
		    h1Num.set(h1Num.get() + 1);
		    h2Num.set(0);
		    h3Num.set(0);
		    eqNum.set(1);
		    listNum.set(1);
		    pictNum.set(1);
		    return null;
		    }
		    if (level == 2)
		    {
			h2Num.set(h2Num.get() + 1);
			return null;
		    }
		    		    if (level == 3)
		    {
			h3Num.set(h3Num.get() + 1);
			return null;
		    }
				    return null;
		}
		@Override public String render(MathBlockDefinition math)
		{
		    if (requireNonNullElse(math.getLabel(), "").isEmpty())
			return "";
		    refs.put(math.getLabel().trim(), h1Num.toString() + "." + eqNum.toString());
									refIds.put(math.getLabel().trim(), getRandomId(ID_LEN));
						refChapNums.put(math.getLabel().trim(), Integer.valueOf(h1Num.get()));
						eqNum.set(eqNum.get() + 1);
		    return "";
		}
		@Override public String render(Label label)
		{
		    if (requireNonNullElse(label.getLabel(), "").trim().isEmpty())
			return "";
		    final String v;
		    if (h2Num.get() == 0 && h3Num.get() == 0)
v = h1Num.toString(); else
		    		    if (h3Num.get() == 0)
v = h1Num.toString() + "." + h2Num.toString(); else
v = h1Num.toString() + "." + h2Num.toString() + "." + h3Num.toString();
				    			refs.put(label.getLabel().trim(), v);
						refIds.put(label.getLabel().trim(), v);
						refChapNums.put(label.getLabel().trim(), Integer.valueOf(h1Num.get()));
			return "";
		}
	    };

		for(final Section s: requireNonNullElse(publ.getSections(), new ArrayList<Section>()))
	{
	    switch(requireNonNullElse(s.getType(), SectionType.MARKDOWN))
	    {
	    case MARKDOWN:
		m.parse(s.getSource());
		break;
	    case LISTING:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + listNum.toString());
		listNum.set(listNum.get() + 1);
		break;
			    case METAPOST:
	    case GNUPLOT:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + pictNum.toString());
		pictNum.set(pictNum.get() + 1);
		break;
			    case EQUATION:
		if (!requireNonNullElse(s.getLabel(), "").trim().isEmpty())
		    refs.put(s.getLabel().trim(), h1Num.toString() + "." + eqNum.toString());
		eqNum.set(eqNum.get() + 1);
		break;
	    }
	}
		biblio.putAll(m.biblio);
    }

    String getChapterFileName(int chapNum)
    {
	if (chapNum == 0)
	    return "Introduction.html";
	return "Chapter" + chapNum + ".html";
    }

    @Override public void close()
    {
    }
}
