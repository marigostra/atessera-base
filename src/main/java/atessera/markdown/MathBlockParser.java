// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.regex.*;

import org.commonmark.node.Block;
import org.commonmark.node.DefinitionMap;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

import java.util.List;

public class MathBlockParser extends AbstractBlockParser
{
        static private final Pattern
	    PAT_SIMPLE = Pattern.compile("^\\s*\\$\\$(.+)\\$\\$\\s*$"),
	    PAT_EQ = Pattern.compile("^\\s*\\$\\$\\$(.+)\\$\\$\\$\\s*$"),
	    	    PAT_EQ_WITH_LABEL = Pattern.compile("^\\s*\\$\\$(.+)\\$\\$\\(([a-zA-Z0-9=_:.-]+)\\)\\s*$");

    private final MathBlockDefinition block;

    public MathBlockParser(MathBlockDefinition.Type type, String text, String label)
    {
        block = new MathBlockDefinition(type, text, label);
    }

    @Override public Block getBlock()
    {
        return block;
    }

    @Override public boolean isContainer()
    {
        return false;
    }

    @Override public boolean canContain(Block childBlock)
    {
        return false;
    }

    @Override public BlockContinue tryContinue(ParserState parserState)
    {
	                return BlockContinue.none();
    }

    static public class Factory implements BlockParserFactory
    {
        @Override public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser)
	{
	            if (state.getIndent() >= 4)
                return BlockStart.none();
            final var index = state.getNextNonSpaceIndex();
            final var content = state.getLine().getContent();

	    var m = PAT_EQ.matcher(content);
	    if (m.find())
		return BlockStart.of(new MathBlockParser(MathBlockDefinition.Type.EQUATION, m.group(1).trim(), null)).atIndex(m.end());
	    m = PAT_EQ_WITH_LABEL.matcher(content);
	    if (m.find())
		return BlockStart.of(new MathBlockParser(MathBlockDefinition.Type.EQUATION, m.group(1).trim(), m.group(2).trim())).atIndex(m.end());
	    m = PAT_SIMPLE.matcher(content);
	    if (m.find())
		return BlockStart.of(new MathBlockParser(MathBlockDefinition.Type.REGULAR, m.group(1).trim(), null)).atIndex(m.end());





	    		                return BlockStart.none();
                }
        }
    }

