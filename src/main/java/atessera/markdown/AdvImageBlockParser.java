// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.*;
import java.util.regex.*;

import org.commonmark.node.Block;
import org.commonmark.node.DefinitionMap;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

public class AdvImageBlockParser extends AbstractBlockParser
{
    static private final Pattern
	PAT_ADV_IMAGE = Pattern.compile("^\\*\\*\\*\\s*\\[([a-zA-Z0-9-_=+/:.]+)\\](.*)\\*\\*\\*(.*)$");

    private final AdvImageDefinition block;

    public AdvImageBlockParser(String src, String alt, String comment)
    {
        block = new AdvImageDefinition(src, alt, comment);
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
        if (parserState.getIndent() >= 4)
            return BlockContinue.atColumn(4);
            return BlockContinue.none();
        }

    @Override public List<DefinitionMap<?>> getDefinitions()
    {
        var map = new DefinitionMap<>(AdvImageDefinition.class);
        map.putIfAbsent(block.getSrc(), block);
        return List.of(map);
    }

    static public class Factory implements BlockParserFactory
    {
        @Override public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser)
	{
            if (state.getIndent() >= 4)
                return BlockStart.none();
            final var index = state.getNextNonSpaceIndex();
            final var content = state.getLine().getContent();
	    final var m = PAT_ADV_IMAGE.matcher(content);
	    if (!m.find())
		                return BlockStart.none();
	    return BlockStart.of(new AdvImageBlockParser(m.group(1).trim(), m.group(3).trim(), m.group(2).trim())).atIndex(m.end());
        }
    }
}
