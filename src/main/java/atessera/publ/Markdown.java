// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import atessera.markdown.*;
import atessera.markdown.blocks.*;
import static atessera.util.LatexUtils.*;

/**
 * Specialized Markdown-to-LaTeX renderer for publications.
 *
 * <p>This package-private class extends {@link LatexTarget} and overrides
 * rendering methods to produce LaTeX output suitable for inclusion in a
 * full publication document (book, paper, thesis, etc.). It is used
 * exclusively by {@link TexTranslator}.</p>
 *
 * <h2>Enabled features</h2>
 *
 * <p>The renderer activates the following {@link LatexTarget.Features}:</p>
 * <ul>
 *   <li>{@link LatexTarget.Features#SILENT_BIB_ITEMS SILENT_BIB_ITEMS}
 *       &mdash; bibliography items are collected silently without
 *       producing visible output in the main text flow.</li>
 *   <li>{@link LatexTarget.Features#CITE CITE} &mdash; citation
 *       references ({@code [@key]}) are rendered as
 *       {@code \cite{key}}.</li>
 *   <li>{@link LatexTarget.Features#LABEL LABEL} &mdash; label markers
 *       are rendered as {@code \label{key}}.</li>
 *   <li>{@link LatexTarget.Features#REFERENCES REFERENCES} &mdash;
 *       cross-references are rendered as {@code \ref{key}} or
 *       {@code \pageref{key}}.</li>
 *   <li>{@link LatexTarget.Features#MATH MATH} &mdash; inline math is
 *       rendered as {@code $...$}, display math as {@code $$...$$}, and
 *       numbered equations as
 *       {@code \begin{equation}...\end{equation}}.</li>
 * </ul>
 *
 * <h2>Heading rendering</h2>
 *
 * <p>Headings are mapped to LaTeX sectioning commands with
 * {@code \needspace} hints to avoid page breaks immediately after a
 * heading:</p>
 *
 * <ul>
 *   <li>Level 1 &rarr; {@code \needspace{6cm}\chapter{...}}</li>
 *   <li>Level 2 &rarr; {@code \needspace{3cm}\section{...}}</li>
 *   <li>Level 3 &rarr; {@code \needspace{3cm}\subsection{...}}</li>
 *   <li>Level 4 &rarr; {@code \subsubsection{...}}</li>
 * </ul>
 *
 * @see LatexTarget
 * @see TexTranslator
 * @see atessera.markdown.blocks.BibItem
 * @see atessera.markdown.blocks.Label
 */
final class Markdown extends LatexTarget
{
    /**
     * Constructs a new Markdown-to-LaTeX renderer with the feature set
     * appropriate for publication content.
     */
    Markdown()
    {
	super(EnumSet.of(
			 			 LatexTarget.Features.SILENT_BIB_ITEMS,
			 LatexTarget.Features.CITE,
			 LatexTarget.Features.LABEL,
			 LatexTarget.Features.REFERENCES,
			 LatexTarget.Features.MATH));
    }

    /**
     * Renders a citation reference as a LaTeX {@code \cite} command.
     *
     * @param citeRef the citation reference node; must not be
     *                {@code null}
     * @return the LaTeX {@code \cite{...}} command with the reference
     *         key escaped
     */
    @Override public String render(CiteReference citeRef)
    {
	return "\\cite{" + escapeRelaxed(citeRef.getRef() )+ "}";
    }

    /**
     * Renders a label marker as a LaTeX {@code \label} command followed
     * by two newlines.
     *
     * @param label the label node; must not be {@code null}
     * @return the LaTeX {@code \label{...}} command
     */
    @Override public String render(Label label)
    {
	return "\\label{" + escapeRelaxed(label.getLabel()) + "}\n\n";
    }

    /**
     * Renders inline math as a LaTeX inline math expression
     * ({@code $...$}).
     *
     * @param math the inline math definition; must not be {@code null}
     * @return the math text wrapped in dollar signs
     */
    @Override public String render(MathDefinition math)
    {
	return "$" + math.getText() + "$";
    }

    /**
     * Renders a math block as a LaTeX display math or equation
     * environment.
     *
     * <p>Regular display math is rendered as {@code $$...$$}. Numbered
     * equations are rendered using the {@code equation} environment,
     * with an optional {@code \label} if a label is present.</p>
     *
     * @param math the math block definition; must not be {@code null}
     * @return the LaTeX representation of the math block
     * @throws IllegalArgumentException if the math block type is
     *         unrecognized
     */
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

    /**
     * Renders a cross-reference as a LaTeX {@code \ref} or
     * {@code \pageref} command.
     *
     * @param ref the reference node; must not be {@code null}
     * @return the LaTeX reference command
     * @throws IllegalArgumentException if the reference type is
     *         unrecognized
     */
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

    /**
     * Renders the opening of a heading at the given level.
     *
     * <p>Adds {@code \needspace} commands before chapters and sections
     * to prevent orphaned headings at the bottom of a page.</p>
     *
     * @param level the heading level (1-based, where 1 is the topmost)
     * @return the LaTeX sectioning command opening, including the
     *         opening brace
     */
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
}