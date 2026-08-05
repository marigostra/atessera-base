// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;

import atessera.markdown.*;
import atessera.markdown.parsers.*;
import atessera.markdown.blocks.*;
import atessera.json.PublicationContent.*;

import static java.util.stream.Collectors.*;

public final class BiblioExtractor
{
    final LatexTarget markup = new LatexTarget(List.of(new BibItemParserFactory()), List.of(new MathLinkProcessor()), false);

    public Map<String, String> extract(List<Section> sections)
    {
	return sections.stream()
	.filter(e -> e.getType() == SectionType.MARKDOWN)
	.flatMap(e -> {
		final var doc = markup.parser.parse(e.getSource().stream().collect(joining("\n")));
		final var items = new ArrayList<BibItem>();
		new EnumNodes(n -> {
			if (n instanceof BibItem bibItem)
			    items.add(bibItem);
		}).enumerate(doc);
		return items.stream();
	    })
	.collect(toMap(
		       e -> e.getLabel(),
		       e -> {
			   final var b = new StringBuilder();
			   markup.renderer.render(e, b);
			   return new String(b);
			   }));
    }
}
