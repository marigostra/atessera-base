// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;

import atessera.markdown.*;
import atessera.markdown.parsers.*;
import atessera.markdown.blocks.*;
import atessera.json.PublicationContent.*;

import static java.util.stream.Collectors.*;

/**
 * Extracts bibliography entries from the Markdown sections of a
 * publication.
 *
 * <p>This utility scans all sections of type {@code MARKDOWN} in a
 * publication, parses each one through a dedicated
 * {@link LatexTarget} instance configured with a
 * {@link atessera.markdown.parsers.BibItemParserFactory
 * BibItemParserFactory}, and collects every
 * {@link atessera.markdown.blocks.BibItem BibItem} node found in the
 * resulting document tree.</p>
 *
 * <p>The result is a {@link Map} where:</p>
 * <ul>
 *   <li><strong>Key</strong> &mdash; the citation label (e.g.
 *       {@code "knuth1984"}), as defined in the Markdown source.</li>
 *   <li><strong>Value</strong> &mdash; the fully rendered LaTeX
 *       representation of the bibliography entry, suitable for
 *       inclusion in a {@code thebibliography} environment.</li>
 * </ul>
 *
 * <h2>Processing details</h2>
 *
 * <ol>
 *   <li>Filters the section list to retain only
 *       {@link SectionType#MARKDOWN MARKDOWN} sections.</li>
 *   <li>For each section, parses the Markdown source through a
 *       {@link LatexTarget} that recognizes
 *       {@link atessera.markdown.blocks.BibItem BibItem} blocks.</li>
 *   <li>Walks the resulting AST using
 *       {@link atessera.markdown.EnumNodes EnumNodes} to find all
 *       {@code BibItem} nodes.</li>
 *   <li>Renders each {@code BibItem} to its LaTeX representation and
 *       collects the results into a map.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 *
 * <p>Instances of this class are stateless and thread-safe. A single
 * instance may be reused across multiple extractions.</p>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * BiblioExtractor extractor = new BiblioExtractor();
 * Map<String, String> bibliography = extractor.extract(publication.getSections());
 * for (var entry : bibliography.entrySet()) {
 *     System.out.println("Key: " + entry.getKey());
 *     System.out.println("Text: " + entry.getValue());
 * }
 * }</pre>
 *
 * @see atessera.markdown.blocks.BibItem
 * @see atessera.markdown.parsers.BibItemParserFactory
 * @see atessera.markdown.LatexTarget
 * @see TexTranslator
 */
public final class BiblioExtractor
{
    /**
     * The Markdown-to-LaTeX renderer used for parsing bibliography
     * items. Configured with a {@link BibItemParserFactory} and a
     * {@link atessera.markdown.MathLinkProcessor MathLinkProcessor},
     * and with bibliography output disabled ({@code false}) so that
     * bibliography items are collected silently.
     */
    final LatexTarget markup = new LatexTarget(List.of(new BibItemParserFactory()), List.of(new MathLinkProcessor()), false);

    /**
     * Extracts bibliography entries from the given list of sections.
     *
     * <p>Only sections of type {@link SectionType#MARKDOWN MARKDOWN}
     * are processed. Other section types are silently ignored.</p>
     *
     * @param sections the list of publication sections; may be
     *                 {@code null} or empty
     * @return a map from citation label to rendered bibliography text;
     *         never {@code null}, but may be empty if no bibliography
     *         items are found
     */
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