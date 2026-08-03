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

public final class MultiBlockParserFactory implements BlockParserFactory
{
static private final Pattern
    PAT_HEADING = Pattern.compile("^\\s*(\\[[0-9!|%*]{0,5}\\])?(\\s+(.*))?\\s*$");
    
        @Override public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser)
    {
	if (state.getIndent() >= 1)
                return BlockStart.none();
            var index = state.getNextNonSpaceIndex();
            var content = state.getLine().getContent();
            if (content.charAt(index) != '>')
                return BlockStart.none();
            index++;
	                                var label = "";
					var caption = String.valueOf(content).substring(index).trim();
					final var m = PAT_HEADING.matcher(caption);
					if (m.find())
					    return BlockStart.of(new MultiBlockParser(new MultiBlock(label,
												     requireNonNullElse(m.group(1), ""),
												     requireNonNullElse(m.group(3), ""))))
					    .atIndex(content.length());
					return BlockStart.of(new MultiBlockParser(new MultiBlock(label, "", caption)))
																															.atIndex(content.length());
            }
    }
