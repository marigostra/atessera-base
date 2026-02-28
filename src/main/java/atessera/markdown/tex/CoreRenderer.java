// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.tex;

import java.util.*;

import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.internal.renderer.text.BulletListHolder;
import org.commonmark.internal.renderer.text.ListHolder;
import org.commonmark.internal.renderer.text.OrderedListHolder;
import atessera.util.*;

final class CoreRenderer extends AbstractVisitor implements NodeRenderer
{
    protected final TexNodeRendererContext context;
    private final TexWriter wr;
    private ListHolder listHolder;

    CoreRenderer(TexNodeRendererContext context)
    {
        this.context = context;
        this.wr = context.getWriter();
    }

    @Override public Set<Class<? extends Node>> getNodeTypes()
    {
        return new HashSet<>(Arrays.asList(
                Document.class,
		                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class
        ));
    }

    @Override public void render(Node node)
    {
	if (node instanceof Image image)
	{
	    visit(image);
	    return;
	}
        node.accept(this);
    }

    @Override public void visit(Document document)
    {
        visitChildren(document);
    }

    @Override public void visit(BlockQuote blockQuote)
    {
        wr.write('«');
        visitChildren(blockQuote);
        wr.write('»');
        writeEndOfLineIfNeeded(blockQuote, null);
    }

    @Override public void visit(Code code)
    {
        wr.write("{\\ttfamily ");
        wr.write(LatexUtils.escapeStrict(code.getLiteral()));
        wr.write("}");
    }

    @Override public void visit(FencedCodeBlock fencedCodeBlock)
    {
	/*
        if (context.stripNewlines())
	{
            wr.writeStripped(fencedCodeBlock.getLiteral());
            writeEndOfLineIfNeeded(fencedCodeBlock, null);
        } else
	*/
	wr.write("{ " + fencedCodeBlock.getLiteral() + "}");
    }

    @Override public void visit(HardLineBreak hardLineBreak)
    {
	wr.write("\\\\");
	writeEndOfLine();
    }

    @Override public void visit(Heading heading)
    {
	wr.write("\\section{");
        visitChildren(heading);
	wr.write("}");
	//        writeEndOfLineIfNeeded(heading, ':');
	writeEndOfLine();
		writeEndOfLine();
    }

    @Override public void visit(Emphasis em)
    {
	wr.write("{\\it ");
        visitChildren(em);
	wr.write("}");
    }

        @Override public void visit(StrongEmphasis strongEmphasis)
    {
	wr.write("{\\bf ");
        visitChildren(strongEmphasis);
	wr.write("}");
    }

    @Override public void visit(ThematicBreak thematicBreak)
    {
        if (!context.stripNewlines()) {
            wr.write("***");
        }
        writeEndOfLineIfNeeded(thematicBreak, null);
    }

    @Override public void visit(HtmlInline htmlInline)
    {
        writeText(htmlInline.getLiteral());
    }

    @Override public void visit(HtmlBlock htmlBlock)
    {
        writeText(htmlBlock.getLiteral());
    }

    @Override public void visit(Image image)
    {
	wr.write("\\begin{wrapfigure}{r}{0.25\\textwidth}\n");
	wr.write("\\centering\n");
	wr.write("\\includegraphics[width=0.23\\textwidth]{" + LatexUtils.escapeStrict(image.getDestination()) + ".pdf}\n");
	//     "\caption{Пример рисунка с обтеканием текста}"
	//     "\label{fig:example}"
	wr.write("\\end{wrapfigure}");
    }

    @Override public void visit(IndentedCodeBlock indentedCodeBlock)
    {
		wr.write("{\\footnotesize");
		writeEndOfLine();
	wr.write("\\begin{verbatim}");
	            writeEndOfLine();
            wr.write(indentedCodeBlock.getLiteral());
		wr.write("\\end{verbatim}");
		            writeEndOfLine();
			    wr.write("}");
			    writeEndOfLine();
			    if (indentedCodeBlock.getNext() != null)
				writeEndOfLine();
    }

    @Override public void visit(Link link)
    {
        writeLink(link, link.getTitle(), link.getDestination());
    }

    @Override public void visit(ListItem listItem)
    {
	wr.write("\\item{");
	//	    writeEndOfLine();
	    writeEndOfLine();
            visitChildrenOfListItem(listItem);
	    writeEndOfLine();
    }

        @Override public void visit(BulletList bulletList)
    {
        if (listHolder != null) 
            writeEndOfLine();
        listHolder = new BulletListHolder(listHolder, bulletList);
	wr.write("\\begin{itemize}");
		writeEndOfLine();
        visitChildren(bulletList);
	wr.write("\\end{itemize}");
        writeEndOfLine();
	if (bulletList.getNext() != null)
	writeEndOfLine();
        if (listHolder.getParent() != null) 
            listHolder = listHolder.getParent(); else
            listHolder = null;
    }

    @Override public void visit(OrderedList orderedList)
    {
	wr.write("\\begin{enumerate}");
	writeEndOfLine();
        if (listHolder != null)
            writeEndOfLine();
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
	//        writeEndOfLineIfNeeded(orderedList, null);
	        if (listHolder.getParent() != null)
            listHolder = listHolder.getParent(); else
            listHolder = null;
		//		writeEndOfLine();
	wr.write("\\end{enumerate}");
	writeEndOfLine();
	if (orderedList.getNext() != null)
	    writeEndOfLine();
    }

    @Override public void visit(Paragraph paragraph)
    {
        visitChildren(paragraph);
	writeEndOfLine();
	//Do not write separating new line after last paragraphs, especially in list items
	if (paragraph.getNext() != null)
	    writeEndOfLine();
    }

    @Override public void visit(SoftLineBreak softLineBreak)
    {
        writeEndOfLineIfNeeded(softLineBreak, null);
    }

    @Override public void visit(Text text)
    {
        writeText(LatexUtils.escapeRelaxed(text.getLiteral()).replaceAll("/", "{\\\\slash}"));//FIXME: Make it better customizable
    }

    @Override protected void visitChildren(Node parent)
    {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    void visitChildrenOfListItem(Node parent)
    {
        Node node = parent.getFirstChild();
	if (node == null)
	    return;
	Node next = node.getNext();
	context.render(node);
	wr.write("}");
	node = next;
        while (node != null)
	{
	    next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private void writeText(String text)
    {
        if (context.stripNewlines()) 
            wr.writeStripped(text); else
            wr.write(text);
    }

    private void writeLink(Node node, String title, String destination)
    {
        boolean hasChild = node.getFirstChild() != null;
	//        boolean hasTitle = title != null && !title.equals(destination);
        boolean hasDestination = destination != null && !destination.equals("");
        if (hasChild)
	{
            wr.write("\\href{");
	    	        if (hasDestination)
	{
            wr.write(destination);
        }
			wr.write("}{");
            visitChildren(node);
            wr.write("}");
        }
	}

    private void writeEndOfLineIfNeeded(Node node, Character c)
    {
        if (context.stripNewlines())
	{
            if (c != null) 
                wr.write(c);
            if (node.getNext() != null)
                wr.whitespace();
        } else
	{
	    if (node.getParent() != null && !(node.getParent() instanceof ListItem))
	                if (node.getNext() != null) 
                wr.line();
        }
    }

    private void writeEndOfLine()
    {
            wr.line();
    }
}
