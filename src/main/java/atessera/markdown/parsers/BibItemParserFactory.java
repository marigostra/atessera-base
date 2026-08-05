// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.parsers;

import java.util.*;
import java.util.regex.*;

import org.commonmark.node.*;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;
import atessera.markdown.blocks.*;

import static java.util.Objects.*;

public final class BibItemParserFactory implements BlockParserFactory
{
static private final Pattern
    PAT_BIB_ITEM = Pattern.compile("^\\*\\*\\*\\s+\\[#([-a-zA-Z0-9_]+)\\]");
    
        @Override public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser)
    {
	if (state.getIndent() > 0)
                return BlockStart.none();
            var content = state.getLine().getContent();
	    final var m = PAT_BIB_ITEM.matcher(content);
	    if (!m.find())
                return BlockStart.none();
	    return BlockStart.of(new BibItemParser(new BibItem(m.group(1))))
	    .atIndex(m.end(1) + 1); // +1 -- the closing bracket
            }
    }
