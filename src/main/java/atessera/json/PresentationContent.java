// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.json;

import java.util.*;
import lombok.*;
import com.google.gson.*;

import static java.util.Objects.*;

@Data
@NoArgsConstructor
public final class PresentationContent
{
    static private final Gson gson = new Gson();
        static public final Set<FrameType> ALL_FRAME_TYPES = EnumSet.allOf(FrameType.class);
            static public final Set<FigureType> ALL_FIGURE_TYPES = EnumSet.allOf(FigureType.class);

    public enum FrameType {MARKDOWN, LATEX, METAPOST, GNUPLOT, PLANTUML, LISTING, EQUATION, GRAPHVIZ_DOT, GRAPHVIZ_NEATO, GRAPHVIZ_TWOPI, GRAPHVIZ_CIRCO};
        public enum FigureType {METAPOST, GNUPLOT, PLANTUML, GRAPHVIZ_DOT, GRAPHVIZ_NEATO, GRAPHVIZ_TWOPI, GRAPHVIZ_CIRCO};

    private String title, subtitle, authors, date, theme, outFile;
    private List<Frame> frames;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Frame
    {
	private FrameType type;
	private String id, title, subtitle, label, listingLang;
	private List<String> source;
    }

        @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public final class Figure
    {
	private FigureType type;
	private String id, label;
	private List<String> source;
    }


    static public String toJson(PresentationContent c)
    {
	return gson.toJson(requireNonNullElse(c, new PresentationContent()));
    }

    static public PresentationContent fromJson(String s)
    {
	if (s == null || s.isEmpty())
	    return new PresentationContent();
	final var res = gson.fromJson(s, PresentationContent.class);
	return res != null?res:new PresentationContent();
    }
}
