// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.parsers;

import java.util.*;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;
import atessera.markdown.blocks.*;

public class MultiBlockParser extends AbstractBlockParser
{
    private final MultiBlock block;

    public MultiBlockParser(MultiBlock block)
    {
        this.block = block;
    }

    @Override public BlockContinue tryContinue(ParserState parserState)
    {
        if (parserState.isBlank() || parserState.getIndent() >= 1)
	                return BlockContinue.none();
	if (parserState.getLine().getContent().charAt(parserState.getIndex()) == '>')
	    return BlockContinue.atIndex(parserState.getIndex() + 1);
		                return BlockContinue.none();
    }

    @Override public List<DefinitionMap<?>> getDefinitions()
    {
        var map = new DefinitionMap<>(MultiBlock.class);
        map.putIfAbsent(block.getLabel(), block);
        return List.of(map);
    }

    @Override public MultiBlock getBlock()
    {
        return block;
    }

    @Override public boolean isContainer() {
        return true;
    }
    
    @Override public boolean canContain(Block childBlock)
    {
        return true;
    }
}
