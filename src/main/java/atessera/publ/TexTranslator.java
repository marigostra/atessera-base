// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.io.*;

import org.apache.velocity.app.*;
import org.apache.velocity.*;
import org.apache.velocity.runtime.resource.loader.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.resource.util.*;

import atessera.comp.*;
import atessera.json.*;
import atessera.markdown.*;

import static java.util.Objects.*;
import static java.lang.Character.*;
import static java.util.stream.Collectors.*;
import static atessera.util.TextUtils.*;
import static atessera.util.LatexUtils.*;

public final class TexTranslator
{
    static private final String
	TEXT = "TEXT",
	IMAGES_EXT = ".pdf";

    final GraphicsManager graphics;
    final VelocityEngine engine;
    final PublicationContent publ;
    final Map<Integer, String> listings;
    final List<Map.Entry<String, String>> biblio = new ArrayList<>();

    public TexTranslator(String templName,     GraphicsManager graphics,
			 PublicationContent publ, Map<Integer, String> listings) throws IOException
    {
	this.graphics = graphics;
	this.publ = requireNonNull(publ, "publ can't be null");
	this.listings = listings;
	engine = new VelocityEngine();
	Properties props = new Properties();
	props.setProperty("str.resource.loader.class", StringResourceLoader.class.getName());
	props.setProperty("str.resource.loader.cache", "true");
	props.setProperty(RuntimeConstants.RESOURCE_LOADER, "str");
	engine.init(props);
	final var  repo = StringResourceLoader.getRepository();
	repo.putStringResource("main.vm", readJavaResource(getClass(), templName)
			       .stream()
			       .collect(joining("\n")));
	//Collecting biblio
	final var biblio = publ.getSections().stream()
	.filter(e -> e.getType() == PublicationContent.SectionType.MARKDOWN)
	.map(e -> {
		final var p = new LatexTarget(EnumSet.of(LatexTarget.Features.CITE));
		p.parse(e.getSource());
		return p.biblio;
	    })
	.reduce((a, b) -> {var r = new HashMap<String, String>(a);r.putAll(b); return r;});
	if (biblio.isPresent())
	{
	    this.biblio.addAll(biblio.get().entrySet().stream().toList());
	    Collections.sort(this.biblio, (e1, e2)->{
		    return onlyLettersAndDigits(e1.getValue()).toLowerCase().compareTo(onlyLettersAndDigits(e2.getValue()).toLowerCase());
		});
	}
    }

    List<String> translate()
    {
	final var context = new VelocityContext();
	context.put("SECTIONS", translateSections(publ));
	context.put("TITLE", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim());
	context.put("TITLE_CAP", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim().toUpperCase());
	context.put("SUBTITLE", escapeRelaxed(requireNonNullElse(publ.getSubtitle(), "")).trim());
	context.put("AUTHORS", escapeRelaxed(requireNonNullElse(publ.getAuthors(), "")).trim());
	context.put("BOOK_TYPE", escapeRelaxed(requireNonNullElse(publ.getBookType(), "")).trim());
	context.put("TITLE_PAGE_TOP_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageTopNote(), "")).trim());
	context.put("TITLE_PAGE_BOTTOM_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageBottomNote(), "")).trim());
	context.put("DATE", escape(requireNonNullElse(publ.getDate(), "")).trim());
	context.put("LOCATION", escapeRelaxed(requireNonNullElse(publ.getLocation(), "")).trim());
	context.put("BIBLIO", "");
	context.put("BIBLIO", translateBiblio());
	if (publ.getAbs() != null && publ.getAbs().getType() != null)
	{
	    if (publ.getAbs().getType() == PublicationContent.SectionType.MARKDOWN)
	    {
		if (publ.getAbs().getSource() != null && !publ.getAbs().getSource().isEmpty())
		{
		    final var s = new Section(TEXT, markdown.parse(publ.getAbs().getSource().stream().collect(joining("\n"))), "");
		    context.put("ABSTRACT", s.getText());
		} else
		    context.put("ABSTRACT", "");
	    } else
		throw new IllegalStateException("Unsupported abstract type: " + publ.getAbs().getType());
	} else
	    context.put("ABSTRACT", "");
	context.put("TOC_BEGIN", "false");
	context.put("TOC_END", "true");
	final Template templ = engine.getTemplate("main.vm");
	final StringWriter w = new StringWriter();
	templ.merge( context, w );
	return Arrays.asList(w.toString().split("\n", -1));
    }

    List<Section> translateSections(PublicationContent cont)
    {
	if (cont.getSections() == null)
	    return Arrays.asList();
	final var res = new ArrayList<Section>();
	for(int i = 0;i < cont.getSections().size();i++)
	{
	    final var sect = cont.getSections().get(i);
	    final Section s;
	    switch(requireNonNull(sect.getType(), "A section can't be processed without information about its type"))
	    {
	    case MARKDOWN:
		if (sect.getSource() == null || sect.getSource().isEmpty())
		    s = new Section("TEXT", "\n\n", ""); else
		    s = new Section("TEXT", markdown.parse(sect.getSource().stream().collect(joining("\n"))), "");
		break;
	    case LATEX:
		if (sect.getSource() == null || sect.getSource().isEmpty())
		    s = new Section("TEXT", "\n\n", ""); else
		    s = new Section("TEXT", sect.getSource().stream().collect(joining("\n")), "");
		break;
	    case LISTING:
		if (sect.getSource() == null || sect.getSource().isEmpty())
		    s = new Section("TEXT", "\n\n", ""); else
		    s = new Section("LISTING", listings.get(Integer.valueOf(i)), "", sect.getLabel(), translateCaption(sect.getCaption()), sect.getListingLang() );
		break;
	    case EQUATION:
		s = translateEquationSection(sect, i);
		break;
	    case PLANTUML:
	    case METAPOST:
	    case GNUPLOT:
	    case GRAPHVIZ_DOT:
		s = new Section("IMAGE", "",
				graphics.getImageId(i) + IMAGES_EXT,
				sect.getLabel(),
				translateCaption(sect.getCaption()),
				null);
		break;
	    default:
		throw new IllegalArgumentException("Unknown section type in a publication: " + sect.getType().toString());
	    }
	    res.add(s);
	}
	return res;
    }

    private Section translateEquationSection(PublicationContent.Section sect, int index)
    {
	if (sect.getSource() == null || sect.getSource().isEmpty())
	    return new Section("TEXT", "\n\n", "");
	return new Section("EQUATION",
			   sect.getSource().stream()
			   .map(s -> s.trim())
			   .filter(s -> !s.isEmpty())
			   .collect(joining("\n")), "",
			   sect.getLabel(), "", null);
    }

    private String translateCaption(List<String> caption)
    {
	if (caption == null || caption.isEmpty())
	    return "";
	return markdown.parse(caption).stream().collect(joining("\n"));
    }

    private String translateBiblio()
    {
	return biblio.stream()
	.map(e -> {
		return "\\bibitem{" + escapeRelaxed(e.getKey()) + "}\n" +
		markdown.parse(e.getValue()) + "\n";
	    }).collect(joining("\n"));
    }

    private String onlyLettersAndDigits(String text)
    {
	final var b = new StringBuilder();
	for(int i = 0;i < text.length();i++)
	{
	    final var ch = text.charAt(i);
	    if (isLetter(ch) || isDigit(ch))
		b.append(ch);
	}
	return new String(b);
    }

    static public final class Section
    {
	final String type, text, imageName, label, caption, lang;
	Section(String type, String text ,String imageName,
		String label, String caption, String lang)
	{
	    this.type = requireNonNull(type, "type can't be null");
	    this.text = requireNonNullElse(text, "");
	    this.imageName = requireNonNullElse(imageName, "");
	    this.label = escapeRelaxed(requireNonNullElse(label, ""));
	    this.caption = requireNonNullElse(caption, "");
	    this.lang = escapeRelaxed(requireNonNullElse(lang, ""));
	}
	Section(String type, String text ,String imageName)
	{
	    this(type, text, imageName, null, null, null);
	}
	public String getType() { return type; }
	public String getText() { return text; }
	public String getImageName() { return imageName; }
	public String getLabel() { return label; }
	public String getCaption() { return caption; }
	public String getLang() { return lang; }
    }

    final LatexTarget markdown =  new LatexTarget(EnumSet.of(
							     LatexTarget.Features.CITE,
							     LatexTarget.Features.LABEL,
							     LatexTarget.Features.REFERENCES,
							     LatexTarget.Features.MATH)){
	    @Override public String render(CiteReference citeRef) { return "\\cite{" + escapeRelaxed(citeRef.getRef() )+ "}"; }
	    @Override public String render(LabelDefinition label) { return "\\label{" + escapeRelaxed(label.getLabel()) + "}\n\n"; }

	    @Override public String render(MathDefinition math)
	    {
		return "$" + math.getText() + "$";
	    }

	    @Override public String render(MathBlockDefinition math)
	    {
		switch(math.getType())
		{
		case REGULAR:
		    return "$$" + math.getText() + "$$\n\n";
		case EQUATION:
		    if (math.getLabel() != null)
			return "\\begin{equation}\n\\label{" + escapeRelaxed(math.getLabel()) + "}\n" + math.getText() + "\n\\end{equation}\n\n";else
			return "\\begin{equation}\n" + math.getText() + "\n\\end{equation}\n\n";
		default:
		    throw new IllegalArgumentException("Unknown math block type: " + math.getType().toString());
		}
	    }

	    @Override public String render(Reference ref)
	    {
		switch(ref.getType())
		{
		case REGULAR:
		    return "\\ref{" + escapeRelaxed(ref.getRef()) + "}";
		case PAGE:
		    return "\\pageref{" + escapeRelaxed(ref.getRef()) + "}";
		default:
		    throw new IllegalArgumentException("Unknown reference type: " + ref.getType().toString());
		}
	    }
	    @Override public String renderHeadingOpening(int level)
	    {
		/*
		  if (publ.getContent().getType() != PublicationContent.Type.TUTORIAL)
		  return super.renderHeadingOpening(level);
		**/
		switch(level)
		{
		case 1:
		    return "\\needspace{6cm}\n\\chapter{";
		case 2:
		    return "\\needspace{3cm}\n\\section{";
		case 3:
		    return "\\needspace{3cm}\n\\subsection{";
		case 4:
		    return "\\subsubsection{";
		}
		return "{";
	    }
	};
}
