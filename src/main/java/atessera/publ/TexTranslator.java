// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.io.*;
import org.apache.logging.log4j.*;

import org.apache.velocity.app.*;
import org.apache.velocity.*;
import org.apache.velocity.runtime.resource.loader.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.resource.util.*;

import atessera.json.*;
import atessera.markdown.*;
import atessera.templ.*;

import static java.util.Objects.*;
import static java.lang.Character.*;
import static java.util.stream.Collectors.*;
import static atessera.util.TextUtils.*;
import static atessera.util.LatexUtils.*;

public final class TexTranslator
{
    static private final Logger log = LogManager.getLogger();
    static private final String IMAGES_EXT = ".pdf";

    final EngineFactory engineFactory;
    final PublicationContent publ;
    private final Markdown markdown = new Markdown();

    public TexTranslator(EngineFactory engineFactory, PublicationContent publ)
    {
	this.engineFactory = requireNonNull(engineFactory, "engineFactory can't be null");
	this.publ = requireNonNull(publ, "publ can't be null");
    }

    public List<String> translate()
    {
	final var templ = new PublicationTemplate(engineFactory);
	templ.setHeader(publ);
	templ.setSections(translateSections());
	final var biblio = new ArrayList<>(new BiblioExtractor().extract(publ.getSections())
					   .entrySet()
					   .stream()
					   .toList());
						   log.info("{} bibliography entries collected", biblio.size());
	Collections.sort(biblio, (e1, e2)->{
		return onlyLettersAndDigits(e1.getValue()).toLowerCase().compareTo(onlyLettersAndDigits(e2.getValue()).toLowerCase());
	    });
	templ.setBiblio(biblio);	    
	if (publ.getAbs() != null && publ.getAbs().getType() != null)
	{
	    if (publ.getAbs().getType() == PublicationContent.SectionType.MARKDOWN)
	    {
		if (publ.getAbs().getSource() != null && !publ.getAbs().getSource().isEmpty())
		{
		    final var s = new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, markdown.parse(publ.getAbs().getSource().stream().collect(joining("\n"))), "");
		    templ.setAbstract(s.getText());
		} else
		    templ.setAbstract( "");
	    } else
		throw new IllegalStateException("Unsupported abstract type: " + publ.getAbs().getType());
	} else
	    templ.setAbstract("");
	final StringWriter w = new StringWriter();
	templ.render(w );
	return Arrays.asList(w.toString().split("\n", -1));
    }
    
    List<PublicationTemplate.Section> translateSections()
    {
	if (publ.getSections() == null)
	    return Collections.emptyList();
	return publ.getSections().stream()
	.map(sect -> {
		switch(requireNonNullElse(sect.getType(), PublicationContent.SectionType.MARKDOWN))
		{
		case MARKDOWN:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, markdown.parse(sect.getSource().stream().collect(joining("\n"))), "");
		case LATEX:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT,
							   sect.getSource().stream().collect(joining("\n")), "");
		case LISTING:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", ""); else
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.LISTING, sect.getId() + ".lst", "", sect.getLabel(), translateCaption(sect.getCaption()), sect.getListingLang() );
		case EQUATION:
		    if (sect.getSource() == null || sect.getSource().isEmpty())
			return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\n", "");
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.EQUATION,
							   sect.getSource().stream()
							   .map(s -> s.trim())
							   .filter(s -> !s.isEmpty())
							   .collect(joining("\n")), "",
							   sect.getLabel(), "", null);
		case PLANTUML:
		case METAPOST:
		case GNUPLOT:
		case GRAPHVIZ_DOT:
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.IMAGE, "",
							   sect.getId() + IMAGES_EXT,
							   sect.getLabel(), translateCaption(sect.getCaption()), null);
		default:
		    return new PublicationTemplate.Section(PublicationTemplate.Section.Type.TEXT, "\n\nFIXME! Section " + sect.getType().toString() + "\n\n", "");
		}
	    }).toList();
    }

    private String translateCaption(List<String> caption)
    {
	if (caption == null || caption.isEmpty())
	    return "";
	return markdown.parse(caption).stream().collect(joining("\n"));
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
}
