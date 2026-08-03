// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import java.io.*;
import lombok.*;

import org.apache.velocity.*;
import org.apache.velocity.exception.*;
import atessera.json.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.util.LatexUtils.*;

public final class PublicationTemplate extends Base
{
    public PublicationTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "publication.vm");
    }

    public void setHeader(PublicationContent publ)
    {
	context.put("TITLE", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim());
	context.put("TITLE_CAP", escapeRelaxed(requireNonNullElse(publ.getTitle(), "")).trim().toUpperCase());
	context.put("SUBTITLE", escapeRelaxed(requireNonNullElse(publ.getSubtitle(), "")).trim());
	context.put("AUTHORS", escapeRelaxed(requireNonNullElse(publ.getAuthors(), "")).trim());
	context.put("BOOK_TYPE", escapeRelaxed(requireNonNullElse(publ.getBookType(), "")).trim());
	context.put("TITLE_PAGE_TOP_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageTopNote(), "")).trim());
	context.put("TITLE_PAGE_BOTTOM_NOTE", escapeRelaxed(requireNonNullElse(publ.getTitlePageBottomNote(), "")).trim());
	context.put("DATE", escape(requireNonNullElse(publ.getDate(), "")).trim());
	context.put("LOCATION", escapeRelaxed(requireNonNullElse(publ.getLocation(), "")).trim());
	//FIXME:
		context.put("TOC_BEGIN", "false");
	context.put("TOC_END", "true");
    }

    public PublicationTemplate setSections(List<Section> sections)
    {
	context.put("SECTIONS", requireNonNull(sections, "sections can't be null"));
	return this;
    }

        public PublicationTemplate setAbstract(String abs)
    {
	context.put("ABSTRACT", abs);
	return this;
    }


    	public PublicationTemplate setBiblio(List<Map.Entry<String, String>> biblio)
    {
	context.put("BIBLIO", biblio.stream()
	.map(e -> {
		return "\\bibitem{" + escapeRelaxed(e.getKey()) + "}\n" +
		escapeRelaxed(e.getValue()) + "\n";
	    }).collect(joining("\n")));
	return this;
    }
    


    @Data
    static public final class Section
    {
	public enum Type {TEXT, IMAGE, EQUATION, LISTING};

	final String type, text, imageName, label, caption, lang;
	
	public Section(Type type, String text ,String imageName,
		String label, String caption, String lang)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.text = requireNonNullElse(text, "");
	    this.imageName = requireNonNullElse(imageName, "");
	    this.label = escapeRelaxed(requireNonNullElse(label, ""));
	    this.caption = requireNonNullElse(caption, "");
	    this.lang = escapeRelaxed(requireNonNullElse(lang, ""));
	}

	public Section(Type type, String text ,String imageName)
	{
	    this(type, text, imageName, null, null, null);
	}
	    }
}
