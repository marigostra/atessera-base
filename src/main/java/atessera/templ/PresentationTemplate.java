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

public final class PresentationTemplate extends Base
{
    public PresentationTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "presentation.vm");
    }

    public void setHeader(PresentationContent c)
    {
	context.put("TITLE", escapeRelaxed(requireNonNullElse(c.getTitle(), "")));
		context.put("AUTHORS", escapeRelaxed(requireNonNullElse(c.getAuthors(), "")));
				context.put("DATE", escapeRelaxed(requireNonNullElse(c.getDate(), "")));
				context.put("FOOTLINE", "");//FIXMEL:
    }

    public void setFrames(List<Frame> frames)
    {
	context.put("FRAMES", requireNonNull(frames, "frames can't be null"));
    }

    @Data
    static public final class Frame
    {
	public enum Type {REGULAR, FIGURE};
	
	final String type, title, subtitle, text;

	public Frame(Type type, String title, String subtitle, List<String> text)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.title = escapeRelaxed(requireNonNullElse(title, ""));
	    	    this.subtitle = escapeRelaxed(requireNonNullElse(subtitle, ""));
		    final List<String> t = requireNonNullElse(text, Collections.emptyList());
		    this.text = t.stream().collect(joining("\n"));
	}

		public Frame(Type type, String title, String subtitle, String text)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.title = escapeRelaxed(requireNonNullElse(title, ""));
	    	    this.subtitle = escapeRelaxed(requireNonNullElse(subtitle, ""));
		    this.text = requireNonNullElse(text, "");
	}

    }
}
