// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.json;

import java.util.*;
import lombok.*;
import com.google.gson.*;

import static java.util.Objects.*;

@Data
@NoArgsConstructor
public final class PublicationContent
{
    static private final Gson gson = new Gson();
    static public final Set<Type> ALL_TYPES = EnumSet.allOf(Type.class);
    static public final Set<SectionType> ALL_SECTION_TYPES = EnumSet.allOf(SectionType.class);

    public enum Type { GRADUATION_WORK, COURSE_WORK, PAPER, THESIS , BOOK};
    public enum SectionType {MARKDOWN, LATEX, EQUATION, TABLE, LISTING, METAPOST, GNUPLOT, PLANTUML, GRAPHVIZ_DOT, GRAPHVIZ_NEATO, GRAPHVIZ_TWOPI, GRAPHVIZ_CIRCO};

    private Type type;
    private String /*subject,*/ title, subtitle, authors, date, location;
    private String published, org, keywords; //mostly for papers and proceedings
        private String isbn, copyright, udk, bbk;
    
    private String stGroup, spNum, spName;
    private String svName, svDegree, svRank;


    private String titlePageTopNote, titlePageBottomNote, bookType;
    private int numCols, pageWidth, pageHeight, pageTopMargin, pageLeftMargin, pageBottomMargin, pageRightMargin;
    private Section abs, issueInfo;
    private List<Section> sections;
    private Map<String, String> xAttr;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Section
    {
	private SectionType type;
	private String id, label, listingLang;
	private List<String> source, alternativeSource, caption;
    }

    static public String toJson(PublicationContent c)
    {
	return gson.toJson(requireNonNullElse(c, new PublicationContent()));
    }

    static public PublicationContent fromJson(String s)
    {
	if (s == null || s.isEmpty())
	    return new PublicationContent();
	final var res = gson.fromJson(s, PublicationContent.class);
	return res != null?res:new PublicationContent();
    }
}
