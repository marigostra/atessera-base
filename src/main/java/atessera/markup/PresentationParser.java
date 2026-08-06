// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markup;

import java.util.*;
import java.util.regex.*;

import atessera.json.*;

import static atessera.json.PresentationContent.*;

/**
 * Parses Alpha Tessera presentation files into {@link PresentationContent}.
 * <p>
 * A presentation file consists of optional global headers followed by a
 * sequence of frames. Each frame is delimited by {@code FRAME BEGIN} and
 * {@code FRAME END} markers and may contain optional metadata lines followed
 * by source content.
 *
 * <h2>File format example</h2>
 * <pre>{@code
 * TITLE EN My Presentation
 * SUBTITLE An Optional Subtitle
 * AUTHORS John Doe, Jane Roe
 * DATE 2025-01-15
 * THEME default
 *
 * FRAME BEGIN MARKDOWN
 * TITLE First Slide
 * SUBTITLE Introduction
 * LABEL intro
 * ## Hello World
 * This is the slide body.
 * FRAME END
 *
 * FRAME BEGIN LATEX
 * TITLE LaTeX Slide
 * \section{Introduction}
 * \begin{itemize}
 *   \item Point one
 * \end{itemize}
 * FRAME END
 * }</pre>
 *
 * <h3>Global headers</h3>
 * Global headers must appear before the first {@code FRAME BEGIN} line.
 * Each header occupies a single line and starts with a keyword:
 * <ul>
 *   <li>{@code TITLE <lang> <text>} — presentation title; {@code <lang>}
 *       is a two-letter uppercase language code (e.g. {@code EN}, {@code RU})</li>
 *   <li>{@code SUBTITLE <text>} — presentation subtitle</li>
 *   <li>{@code AUTHORS <text>} — comma-separated list of authors</li>
 *   <li>{@code DATE <text>} — date string</li>
 *   <li>{@code THEME <text>} — theme name</li>
 * </ul>
 * All global headers are optional. Empty lines between headers are ignored.
 *
 * <h3>Frame syntax</h3>
 * Each frame starts with {@code FRAME BEGIN [type]} and ends with
 * {@code FRAME END}. The optional {@code type} is a case-insensitive name
 * matching one of the {@link FrameType} enum constants (e.g. {@code MARKDOWN},
 * {@code LATEX}, {@code METAPOST}, {@code GNUPLOT}, {@code PLANTUML},
 * {@code LISTING}, {@code EQUATION}, {@code GRAPHVIZ_DOT}, etc.).
 * If the type is omitted or unrecognised, {@link FrameType#MARKDOWN MARKDOWN}
 * is used by default.
 * <p>
 * Within a frame, optional metadata lines may appear before any source
 * content. Metadata lines are recognised by their keyword prefix:
 * <ul>
 *   <li>{@code TITLE <text>} — frame title</li>
 *   <li>{@code SUBTITLE <text>} — frame subtitle</li>
 *   <li>{@code LABEL <text>} — cross-reference label</li>
 *   <li>{@code LISTING_LANG <text>} — programming language for listing frames</li>
 * </ul>
 * The first line inside a frame that does not match any metadata keyword
 * ends the metadata section. That line and all subsequent lines until
 * {@code FRAME END} form the frame's source content. Empty lines before
 * the first metadata or source line are skipped.
 * <p>
 * Frame-level metadata keys are the same strings as some global header keys
 * ({@code TITLE}, {@code SUBTITLE}), but they are interpreted in their
 * respective context and do not conflict.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * var content = PresentationParser.parse(lines);
 * }</pre>
 *
 * @see PresentationContent
 * @see PresentationContent.Frame
 * @see FrameType
 */
public final class PresentationParser extends TextStateParser<PresentationContent>
{
    // ---- Regex patterns ----

    /** Matches a global TITLE header: TITLE, two-letter language code, and title text. */
    static final Pattern PAT_HEADER_TITLE = Pattern.compile(
	"^\\s*TITLE\\s+([A-Z]{2})\\s+(.*)\\s*$");

    /** Matches a global SUBTITLE header. */
    static final Pattern PAT_HEADER_SUBTITLE = Pattern.compile(
	"^\\s*SUBTITLE\\s+(.*)\\s*$");

    /** Matches a global AUTHORS header. */
    static final Pattern PAT_HEADER_AUTHORS = Pattern.compile(
	"^\\s*AUTHORS\\s+(.*)\\s*$");

    /** Matches a global DATE header. */
    static final Pattern PAT_HEADER_DATE = Pattern.compile(
	"^\\s*DATE\\s+(.*)\\s*$");

    /** Matches a global THEME header. */
    static final Pattern PAT_HEADER_THEME = Pattern.compile(
	"^\\s*THEME\\s+(.*)\\s*$");

    /** Matches {@code FRAME BEGIN [type]}. Group 2 captures the optional frame type. */
    static final Pattern PAT_FRAME_BEGIN = Pattern.compile(
	"^\\s*FRAME\\s+BEGIN(\\s+([A-Za-z_][A-Za-z0-9_]*))?\\s*$");

    /** Matches {@code FRAME END}. */
    static final Pattern PAT_FRAME_END = Pattern.compile(
	"^\\s*FRAME\\s+END\\s*$");

    /** Matches a frame-level TITLE metadata line. */
    static final Pattern PAT_META_TITLE = Pattern.compile(
	"^\\s*TITLE\\s+(.*)\\s*$");

    /** Matches a frame-level SUBTITLE metadata line. */
    static final Pattern PAT_META_SUBTITLE = Pattern.compile(
	"^\\s*SUBTITLE\\s+(.*)\\s*$");

    /** Matches a LABEL metadata line. */
    static final Pattern PAT_META_LABEL = Pattern.compile(
	"^\\s*LABEL\\s+(.*)\\s*$");

    /** Matches a LISTING_LANG metadata line. */
    static final Pattern PAT_META_LISTING_LANG = Pattern.compile(
	"^\\s*LISTING_LANG\\s+(.*)\\s*$");

    // ---- Header matchers ----

    /**
     * Matches a line as a global header entry during the headers phase.
     * <p>
     * Each implementation checks whether the given trimmed line starts with
     * a specific keyword and, if so, updates the corresponding field in
     * {@link PresentationContent}.
     */
    static abstract class AbstractHeaderLine implements HeaderLine<PresentationContent>
    {
	final Pattern pattern;

	AbstractHeaderLine(Pattern pattern)
	{
	    this.pattern = pattern;
	}

	@Override public final boolean parse(String line, PresentationContent pres)
	{
	    final var m = pattern.matcher(line);
	    if (m.matches())
	    {
		apply(m, pres);
		return true;
	    }
	    return false;
	}

	abstract void apply(Matcher m, PresentationContent pres);
    }

    static final class TitleHeader extends AbstractHeaderLine
    {
	TitleHeader() { super(PAT_HEADER_TITLE); }
	@Override void apply(Matcher m, PresentationContent pres)
	{
	    pres.setTitle(m.group(2));
	}
    }

    static final class SubtitleHeader extends AbstractHeaderLine
    {
	SubtitleHeader() { super(PAT_HEADER_SUBTITLE); }
	@Override void apply(Matcher m, PresentationContent pres)
	{
	    pres.setSubtitle(m.group(1));
	}
    }

    static final class AuthorsHeader extends AbstractHeaderLine
    {
	AuthorsHeader() { super(PAT_HEADER_AUTHORS); }
	@Override void apply(Matcher m, PresentationContent pres)
	{
	    pres.setAuthors(m.group(1));
	}
    }

    static final class DateHeader extends AbstractHeaderLine
    {
	DateHeader() { super(PAT_HEADER_DATE); }
	@Override void apply(Matcher m, PresentationContent pres)
	{
	    pres.setDate(m.group(1));
	}
    }

    static final class ThemeHeader extends AbstractHeaderLine
    {
	ThemeHeader() { super(PAT_HEADER_THEME); }
	@Override void apply(Matcher m, PresentationContent pres)
	{
	    pres.setTheme(m.group(1));
	}
    }

    // ---- States ----

    /**
     * The state used when the parser is not currently inside any frame.
     * <p>
     * This state ignores all lines (they are whitespace or text between
     * frames). Its {@link #commit} is a no-op.
     */
    static final class InitialState implements State<PresentationContent>
    {
	@Override public void onLine(String line, PresentationContent pres)
	{
	    // Ignore text outside frames
	}

	@Override public void commit(PresentationContent pres)
	{
	    // Nothing to commit
	}
    }

    /**
     * The state that accumulates a single frame.
     * <p>
     * A frame begins when {@code FRAME BEGIN} is encountered and ends at
     * {@code FRAME END}. Within the frame, optional metadata lines
     * ({@code TITLE}, {@code SUBTITLE}, {@code LABEL}, {@code LISTING_LANG})
     * are parsed first. The first line that does not match any metadata
     * keyword starts the source section. All subsequent lines (including
     * that first non-metadata line) are collected as source content.
     * <p>
     * When {@link #commit} is called, the accumulated data is wrapped into
     * a {@link Frame} and appended to the presentation's frame list.
     */
    static final class FrameState implements State<PresentationContent>
    {
	final FrameType frameType;
	String frameTitle;
	String frameSubtitle;
	String frameLabel;
	String frameListingLang;
	final List<String> sourceLines = new ArrayList<>();
	boolean inMetadata = true;
	boolean seenAnyNonEmpty = false;

	FrameState(FrameType frameType)
	{
	    this.frameType = frameType;
	}

	@Override public void onLine(String line, PresentationContent pres)
	{
	    if (!inMetadata)
	    {
		sourceLines.add(line);
		return;
	    }
	    final var trimmed = line.trim();
	    if (trimmed.isEmpty())
	    {
		// In metadata phase, skip empty lines before any content
		if (!seenAnyNonEmpty)
		    return;
		// After we've seen metadata, an empty line starts source
		inMetadata = false;
		sourceLines.add(line);
		return;
	    }

	    seenAnyNonEmpty = true;

	    var m = PAT_META_TITLE.matcher(trimmed);
	    if (m.matches())
	    {
		frameTitle = m.group(1);
		return;
	    }

	    m = PAT_META_SUBTITLE.matcher(trimmed);
	    if (m.matches())
	    {
		frameSubtitle = m.group(1);
		return;
	    }

	    m = PAT_META_LABEL.matcher(trimmed);
	    if (m.matches())
	    {
		frameLabel = m.group(1);
		return;
	    }

	    m = PAT_META_LISTING_LANG.matcher(trimmed);
	    if (m.matches())
	    {
		frameListingLang = m.group(1);
		return;
	    }

	    // First non-metadata, non-empty line: switch to source mode
	    inMetadata = false;
	    sourceLines.add(line);
	}

	@Override public void commit(PresentationContent pres)
	{
	    final var frame = new Frame(frameType, null /* ID */, frameTitle, frameSubtitle,
		frameLabel, frameListingLang,
		List.copyOf(sourceLines));
	    var frames = pres.getFrames();
	    if (frames == null)
	    {
		frames = new ArrayList<>();
		pres.setFrames(frames);
	    }
	    frames.add(frame);
	}
    }

    // ---- State-boundary matchers ----

    /**
     * Detects {@code FRAME BEGIN [type]} and creates a new {@link FrameState}.
     * <p>
     * The frame type string is resolved case-insensitively against
     * {@link FrameType} enum names. If the type is missing or does not match
     * any known type, {@link FrameType#MARKDOWN} is used.
     */
    static final class FrameBeginMatcher implements NewStateLine<PresentationContent>
    {
	@Override public State<PresentationContent> parse(String line,
		State<PresentationContent> currentState, PresentationContent pres)
	{
	    final var m = PAT_FRAME_BEGIN.matcher(line);
	    if (!m.matches())
		return null;

	    final var typeStr = m.group(2);
	    final var frameType = resolveFrameType(typeStr);
	    return new FrameState(frameType);
	}

	static FrameType resolveFrameType(String typeStr)
	{
	    if (typeStr == null || typeStr.isEmpty())
		return FrameType.MARKDOWN;
	    try
	    {
		return FrameType.valueOf(typeStr.toUpperCase());
	    }
	    catch (IllegalArgumentException e)
	    {
		return FrameType.MARKDOWN;
	    }
	}
    }

    /**
     * Detects {@code FRAME END} and returns the initial state, effectively
     * closing the current frame.
     * <p>
     * The parser will call {@link State#commit} on the current
     * {@link FrameState} before switching, which adds the completed frame
     * to the model.
     */
    static final class FrameEndMatcher implements NewStateLine<PresentationContent>
    {
	@Override public State<PresentationContent> parse(String line,
		State<PresentationContent> currentState, PresentationContent pres)
	{
	    if (PAT_FRAME_END.matcher(line).matches())
		return new InitialState();
	    return null;
	}
    }

    // ---- Constructor ----

    /**
     * Creates a new presentation parser with all header and state-boundary
     * matchers configured.
     */
    public PresentationParser()
    {
	super(new PresentationContent(),
	      new InitialState(),
	      List.of(new TitleHeader(),
		      new SubtitleHeader(),
		      new AuthorsHeader(),
		      new DateHeader(),
		      new ThemeHeader()),
	      List.of(new FrameBeginMatcher(),
		      new FrameEndMatcher()));
    }

    // ---- Public API ----

    /**
     * Returns the parsed {@link PresentationContent} model.
     * <p>
     * This method should be called after {@link #parse} to retrieve the
     * result. The returned model is the same instance that was passed to
     * the constructor and mutated during parsing.
     *
     * @return the populated presentation content, never {@code null}
     */
    public PresentationContent getResult()
    {
	return model;
    }

    /**
     * Convenience method that parses a list of lines and returns the
     * populated {@link PresentationContent}.
     *
     * @param lines the lines of a presentation file to parse,
     *              must not be {@code null}
     * @return the parsed presentation content, never {@code null}
     * @throws NullPointerException if {@code lines} is {@code null}
     */
    static public PresentationContent parsePresentation(List<String> lines)
    {
	final var parser = new PresentationParser();
	parser.parse(lines);
	return parser.getResult();
    }
}
