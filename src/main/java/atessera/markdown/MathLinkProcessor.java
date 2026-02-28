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

public class MathLinkProcessor implements LinkProcessor {
    @Override public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context)
    {
        if (linkInfo.destination() != null || linkInfo.label() != null)
            return LinkResult.none();
        final var text = linkInfo.text();
        if (!text.startsWith("$"))
            return LinkResult.none();
	        final var position = linkInfo.afterTextBracket();
        var exp = text.substring(1);
        return LinkResult.replaceWith(new MathDefinition(exp), position);
    }
}
