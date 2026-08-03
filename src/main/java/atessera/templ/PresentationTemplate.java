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

/**
 * Velocity template for generating LaTeX Beamer source of a presentation.
 *
 * <p>This class populates the {@code presentation.vm} template with
 * metadata (title, authors, date) and slide frames, producing a compilable
 * LaTeX Beamer document.</p>
 *
 * <p>Typical usage:</p>
 *
 * <pre>{@code
 * PresentationTemplate t = new PresentationTemplate(factory);
 * t.setHeader(presentationContent);
 * t.setFrames(frames);
 * List<String> latexSource = t.renderToStringList();
 * }</pre>
 *
 * @see Base
 * @see PresentationContent
 */
public final class PresentationTemplate extends Base
{
    /**
     * Constructs a new presentation template.
     *
     * @param engineFactory the factory for obtaining a Velocity engine;
     *                      must not be {@code null}
     * @throws ResourceNotFoundException if {@code presentation.vm} cannot
     *         be loaded
     */
    public PresentationTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "presentation.vm");
    }

    /**
     * Sets the presentation metadata from a {@link PresentationContent}
     * object.
     *
     * <p>Populates template variables: {@code TITLE}, {@code AUTHORS},
     * {@code DATE}, and {@code FOOTLINE}.</p>
     *
     * @param c the presentation metadata; must not be {@code null}
     */
    public void setHeader(PresentationContent c)
    {
	context.put("TITLE", escapeRelaxed(requireNonNullElse(c.getTitle(), "")));
		context.put("AUTHORS", escapeRelaxed(requireNonNullElse(c.getAuthors(), "")));
				context.put("DATE", escapeRelaxed(requireNonNullElse(c.getDate(), "")));
				context.put("FOOTLINE", "");//FIXMEL:
    }

    /**
     * Sets the frames (slides) of the presentation.
     *
     * @param frames the list of frames; must not be {@code null}
     */
    public void setFrames(List<Frame> frames)
    {
	context.put("FRAMES", requireNonNull(frames, "frames can't be null"));
    }

    /**
     * Represents a single frame (slide) within a presentation.
     *
     * <p>Each frame has a type ({@code REGULAR} or {@code FIGURE}), a
     * title, a subtitle, and body text.</p>
     */
    @Data
    static public final class Frame
    {
	/** Enumerates the possible frame types. */
	public enum Type
	{
	    /** A regular content slide. */
	    REGULAR,
	    /** A slide containing a figure. */
	    FIGURE
	};
	
	final String type, title, subtitle, text;

	/**
	 * Constructs a frame from a list of text lines.
	 *
	 * @param type     the frame type; must not be {@code null}
	 * @param title    the frame title; will be escaped (may be
	 *                 {@code null})
	 * @param subtitle the frame subtitle; will be escaped (may be
	 *                 {@code null})
	 * @param text     the frame body as a list of lines; may be
	 *                 {@code null} or empty
	 */
	public Frame(Type type, String title, String subtitle, List<String> text)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.title = escapeRelaxed(requireNonNullElse(title, ""));
	    	    this.subtitle = escapeRelaxed(requireNonNullElse(subtitle, ""));
		    final List<String> t = requireNonNullElse(text, Collections.emptyList());
		    this.text = t.stream().collect(joining("\n"));
	}

	/**
	 * Constructs a frame from a single text string.
	 *
	 * @param type     the frame type; must not be {@code null}
	 * @param title    the frame title; will be escaped (may be
	 *                 {@code null})
	 * @param subtitle the frame subtitle; will be escaped (may be
	 *                 {@code null})
	 * @param text     the frame body as a single string; may be
	 *                 {@code null}
	 */
	public Frame(Type type, String title, String subtitle, String text)
	{
	    this.type = requireNonNull(type, "type can't be null").toString();
	    this.title = escapeRelaxed(requireNonNullElse(title, ""));
	    	    this.subtitle = escapeRelaxed(requireNonNullElse(subtitle, ""));
		    this.text = requireNonNullElse(text, "");
	}

    }
}
