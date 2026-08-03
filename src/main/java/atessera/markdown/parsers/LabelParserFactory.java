// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.parsers;

import java.util.regex.*;

import org.commonmark.node.Block;
import org.commonmark.node.DefinitionMap;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

import java.util.List;

public final class LabelParserFactory implements BlockParserFactory
{
        static private final Pattern
    PAT_LABEL = Pattern.compile("^\\s*@@\\s*(.{1,30})\\s*@@\\s*$");

    
    @Override public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser)
	{
	            if (state.getIndent() >= 4)
                return BlockStart.none();
            final var index = state.getNextNonSpaceIndex();
            final var content = state.getLine().getContent();
	    final var m = PAT_LABEL.matcher(content);
	    if (!m.find())
		                return BlockStart.none();
	    return BlockStart.of(new LabelParser(m.group(1).trim())).atIndex(m.end());
                }
        }
