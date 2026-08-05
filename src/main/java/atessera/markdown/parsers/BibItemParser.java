// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.parsers;

import java.util.*;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;
import atessera.markdown.blocks.*;

public class  BibItemParser extends AbstractBlockParser
{
    private final BibItem bibItem;

    public BibItemParser(BibItem bibItem)
    {
        this.bibItem = bibItem;
    }

    @Override public BlockContinue tryContinue(ParserState parserState)
    {
        if (parserState.isBlank() || parserState.getIndent() < 4)
	                return BlockContinue.none();
	    return BlockContinue.atIndex(parserState.getNextNonSpaceIndex());
    }

    @Override public List<DefinitionMap<?>> getDefinitions()
    {
        var map = new DefinitionMap<>(BibItem.class);
        map.putIfAbsent(bibItem.getLabel(), bibItem);
        return List.of(map);
    }

    @Override public BibItem getBlock()
    {
        return bibItem;
    }

    @Override public boolean isContainer()
    {
        return true;
    }
    
    @Override public boolean canContain(Block childBlock)
    {
	//FIXME: Only paragraphs
        return true;
    }
}
