// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.regex.*;

import org.commonmark.node.Block;
import org.commonmark.node.DefinitionMap;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

import java.util.List;

public class LabelBlockParser extends AbstractBlockParser
{
        static private final Pattern
    PAT_LABEL = Pattern.compile("^\\s*@@\\s*(.{1,30})\\s*@@\\s*$");

    private final LabelDefinition block;

    public LabelBlockParser(String label)
    {
        block = new LabelDefinition(label);
    }

    @Override public Block getBlock()
    {
        return block;
    }

    @Override public boolean isContainer()
    {
        return true;
    }

    @Override public boolean canContain(Block childBlock)
    {
        return true;
    }

    @Override public BlockContinue tryContinue(ParserState parserState)
    {
        if (parserState.getIndent() >= 4) {
            // It looks like content needs to be indented by 4 so that it's part of a footnote (instead of starting a new block).
            return BlockContinue.atColumn(4);
        } else {
            // We're not continuing to give other block parsers a chance to interrupt this definition.
            // But if no other block parser applied (including another FootnotesBlockParser), we will
            // accept the line via lazy continuation (same as a block quote).
            return BlockContinue.none();
        }
    }

    @Override public List<DefinitionMap<?>> getDefinitions()
    {
        var map = new DefinitionMap<>(LabelDefinition.class);
        map.putIfAbsent(block.getLabel(), block);
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
	    final var m = PAT_LABEL.matcher(content);
	    if (!m.find())
		                return BlockStart.none();
	    return BlockStart.of(new LabelBlockParser(m.group(1).trim())).atIndex(m.end());
                }
        }
    }

