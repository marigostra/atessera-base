// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import atessera.markdown.*;
import atessera.markdown.blocks.*;
import static atessera.util.LatexUtils.*;

final class Markdown extends LatexTarget
{
    Markdown()
    {
	super(EnumSet.of(
			 			 LatexTarget.Features.SILENT_BIB_ITEMS,
			 LatexTarget.Features.CITE,
			 LatexTarget.Features.LABEL,
			 LatexTarget.Features.REFERENCES,
			 LatexTarget.Features.MATH));
    }
    
    @Override public String render(CiteReference citeRef)
    {
	return "\\cite{" + escapeRelaxed(citeRef.getRef() )+ "}";
    }

    @Override public String render(Label label)
    {
	return "\\label{" + escapeRelaxed(label.getLabel()) + "}\n\n";
    }

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
}
