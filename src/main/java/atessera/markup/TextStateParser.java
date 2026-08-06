// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markup;


import java.util.*;

import static java.util.Objects.*;

/**
 * A generic two-phase line-by-line text parser based on a state machine pattern.
 * <p>
 * This parser processes a list of strings (typically lines of a text file)
 * in two sequential phases:
 * <ol>
 *   <li><b>Headers phase</b>: The parser iterates over lines at the beginning
 *       of the input and passes each non-empty trimmed line to a set of
 *       {@link HeaderLine} matchers. As long as at least one matcher accepts
 *       the line, the parser stays in this phase. The first line that is not
 *       accepted by any header matcher ends the headers phase, and this same
 *       line is immediately handed over to the next phase without being
 *       discarded.</li>
 *   <li><b>States phase</b>: Each line (starting from the one that terminated
 *       the headers phase) is checked against a set of {@link NewStateLine}
 *       matchers to detect state boundaries. If a matcher returns a new
 *       {@link State}, the previous state is {@link State#commit committed}
 *       and the new state becomes active. Otherwise, the line is dispatched
 *       to {@link State#onLine onLine} of the current state.</li>
 * </ol>
 * After all lines are processed, the last active state is committed.
 * <p>
 * The generic type parameter {@code <M>} represents the model object that
 * accumulates parsing results. It is passed to all callbacks and is expected
 * to be mutated during parsing. The same model instance is shared across all
 * phases, states, and matchers.
 *
 * @param <M> the type of the model object populated during parsing
 *
 * @see HeaderLine
 * @see NewStateLine
 * @see State
 */
public class TextStateParser<M>
{
    /**
     * Matches a line as a header entry during the headers phase.
     * <p>
     * Implementations should inspect the given line and, if it represents a
     * header record, update the model accordingly and return {@code true}.
     * Returning {@code false} signals that this line is not a header, which
     * causes the parser to exit the headers phase immediately. The line that
     * caused the exit is then passed to the states phase.
     *
     * @param <M> the type of the model object
     */
    public interface HeaderLine<M>
    {
	/**
	 * Attempts to parse the given line as a header entry.
	 *
	 * @param line  the trimmed, non-empty line to inspect
	 * @param model the model to update if the line is accepted
	 * @return {@code true} if the line was recognised as a header and processed,
	 *         {@code false} otherwise
	 */
	boolean parse(String line, M model);
    }

    /**
     * Detects a state boundary and creates the corresponding {@link State}.
     * <p>
     * During the states phase, each trimmed line is passed to all registered
     * {@code NewStateLine} matchers. If a matcher returns a non-{@code null}
     * {@link State}, the parser commits the current state and switches to the
     * returned one. If all matchers return {@code null}, the line is dispatched
     * to the current state via {@link State#onLine onLine}.
     *
     * @param <M> the type of the model object
     */
    public interface NewStateLine<M>
    {
	/**
	 * Inspects the given line and optionally creates a new state for it.
	 *
	 * @param line         the trimmed line to inspect
	 * @param currentState the currently active state, may be {@code null}
	 * @param model        the model to associate with any newly created state
	 * @return a new {@link State} instance if this line starts a new section,
	 *         or {@code null} if no transition should occur
	 */
	State<M> parse(String line, State<M> currentState, M model);
    }

    /**
     * Represents a parsing state that processes lines belonging to a specific
     * section of the input.
     * <p>
     * While this state is active, each line that does not trigger a state
     * transition is passed to {@link #onLine onLine}. When the parser is about
     * to switch to a different state or reaches the end of input,
     * {@link #commit commit} is called to finalize any accumulated data.
     *
     * @param <M> the type of the model object
     */
    public interface State<M>
    {
	/**
	 * Processes a single line within the current state. The line is passed
	 * in its original form (without trimming).
	 *
	 * @param line  the line of text to process
	 * @param model the model to update
	 */
	void onLine(String line, M model);

	/**
	 * Finalizes this state. Called when the parser is about to transition
	 * to a different state or when the end of input is reached. After this
	 * method is called, the state is discarded and no further calls to
	 * {@link #onLine onLine} will be made for it.
	 *
	 * @param model the model to finalize
	 */
	void commit(M model);
    }

    /** The model instance populated during parsing. */
    final M model;

    /** The state used before any state transition occurs. */
    final State<M> initialState;

    /** Header matchers used during the headers phase. */
    final List<HeaderLine<M>> headerLines;

    /** State-boundary matchers used during the states phase. */
    final List<NewStateLine<M>> newStateLines;

    /**
     * Creates a new parser.
     *
     * @param model         the model to populate during parsing, must not be {@code null}
     * @param initialState  the initial state to use before any transition occurs,
     *                      must not be {@code null}
     * @param headerLines   the list of header matchers; if {@code null}, an empty
     *                      list is used
     * @param newStateLines the list of state-boundary matchers; if {@code null},
     *                      an empty list is used
     * @throws NullPointerException if {@code model} or {@code initialState} is {@code null}
     */
    public TextStateParser(M model, State<M> initialState,
			   List<HeaderLine<M>> headerLines, List<NewStateLine<M>> newStateLines)
    {
	this.model = requireNonNull(model, "model can't be null");
	this.initialState = requireNonNull(initialState, "initialState can't be null");
	this.headerLines = requireNonNullElse(headerLines, List.of());
	this.newStateLines = requireNonNullElse(newStateLines, List.of());
    }

    /**
     * Executes the two-phase parsing over the given list of lines.
     * <p>
     * The method first enters the headers phase: non-empty trimmed lines are
     * offered to {@link HeaderLine} matchers. The first line not recognised as
     * a header terminates the headers phase and is immediately processed in the
     * states phase.
     * <p>
     * During the states phase, each trimmed line is checked against
     * {@link NewStateLine} matchers. If a matcher returns a new {@link State},
     * the current state is committed and the new state becomes active.
     * Otherwise, the original (untrimmed) line is dispatched to the current
     * state via {@link State#onLine onLine}. After all lines are exhausted,
     * the last active state is committed.
     *
     * @param lines the lines to parse
     */
    public void parse(List<String> lines)
    {
	State<M> state = initialState;
	boolean parsingHeaders = true;
	for(final var line: lines)
	{
	    if (parsingHeaders)
	    {
		final var l = line.trim();
		if (l.isEmpty())
		    continue;
		var parsed = false;
		for(final var h: headerLines)
		    if (h.parse(l, model))
		    {
			parsed = true;
			break;
		    }
		if (parsed)
		    continue;
		parsingHeaders = false;
	    } //parsingHeaders

	    State<M> newState = null;
	    for(final var s: newStateLines)
	    {
		newState = s.parse(line.trim(), state, model);
		if (newState != null)
		    break;
	    }
	    if (newState == null)
	    {
		if (state != null)
		    state.onLine(line, model);
		continue;
	    }
	    if (state != null)
		state.commit(model);
	    state = newState;
	} //for(lines)
	if (state != null)
	    state.commit(model);
    }
}
