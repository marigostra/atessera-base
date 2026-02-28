// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

//FIXME: Strip backslashes used for escaping

import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;

public class CiteLinkProcessor implements LinkProcessor {
    @Override public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context)
    {
	/*
        if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("^"))
            return LinkResult.wrapTextIn(new InlineFootnote(), linkInfo.afterTextBracket()).includeMarker();
	**/
        if (linkInfo.destination() != null || linkInfo.label() != null)
            return LinkResult.none();
        final var text = linkInfo.text();
        if (!text.startsWith("#"))
            return LinkResult.none();
        var ref = text.substring(1);
        final var position = linkInfo.afterTextBracket();
        return LinkResult.replaceWith(new CiteReference(ref), position);
    }
}
