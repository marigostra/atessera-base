// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.*;
import org.commonmark.renderer.html.*;
import org.commonmark.internal.util.Escaping;

import atessera.util.SvgGenerator;

import static java.util.Objects.*;
import static java.lang.Character.*;

public class HtmlTarget
{
    static public final char
        NBSP = 160,
        LQUOT = 171,
        RQUOT = 187,
        MDASH = 0x2014,
        NDASH = 0x2013;

    public enum Features {ADV_IMAGE, CITE, LABEL, EXT_CHARS, REFERENCES, MATH};

    public final Parser parser;
    private final Set<Features> features;
    public final Map<String, String> biblio = new HashMap<>();
    
    private final Map<String, Integer> equationNumbers = new LinkedHashMap<>();
    
    private int formulasGenerated = 0;
    private int formulasFailed = 0;
    private int referencedFormulas = 0;

    public HtmlTarget(Set<Features> features, boolean allowHtmlInlines)
    {
        this.features = features;    
        final var p = new Parser.Builder();
        if (features.contains(Features.ADV_IMAGE))
            p.customBlockParserFactory(new AdvImageBlockParser.Factory());
        if (features.contains(Features.LABEL))
            p.customBlockParserFactory(new LabelBlockParser.Factory());
        if (features.contains(Features.MATH))
        {
            p.linkProcessor(new MathLinkProcessor());
            p.customBlockParserFactory(new MathBlockParser.Factory());
        }
        if (features.contains(Features.REFERENCES))
            p.linkProcessor(new RefLinkProcessor());
        if (features.contains(Features.CITE))
        {
            p.linkProcessor(new CiteLinkProcessor());
            p.customBlockParserFactory(new CiteBlockParser.Factory());
        }
        p.inlineParserFactory(c -> new atessera.markdown.cust.InlineParserImpl(c, allowHtmlInlines));
        this.parser = p.build();
    }

    public HtmlTarget(Set<Features> features)
    {
        this(features, false);
    }

    public HtmlTarget()
    {
        this(EnumSet.noneOf(Features.class));
    }
    
    private static String loadCss() {
        try (InputStream is = HtmlTarget.class.getResourceAsStream("/styles.css")) {
            if (is == null) {
                System.err.println("Warning: styles.css not found in resources");
                return "";
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            System.err.println("Warning: Could not load styles.css: " + e.getMessage());
            return "";
        }
    }
    
    public String parseWithStyle(String text) {
        String content = parse(text);
        String css = loadCss();
        
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "<meta charset=\"UTF-8\">\n" +
               "<title>Converted Document</title>\n" +
               "<style>\n" + css + "</style>\n" +
               "</head>\n" +
               "<body>\n" + content + "\n</body>\n" +
               "</html>";
    }

    protected String translateRef(String ref)
    {
        return ref;
    }

    protected String getRefTitle(String ref, String translatedRef)
    {
        return null;
    }

    protected String render(AdvImageDefinition advImage)
    {
        return "<img src=\"" + extChars(Escaping.escapeHtml(advImage.getSrc())) + 
               "\" alt=\"" + escape(advImage.getAlt()) + "\">\n";
    }

    protected String render(CiteReference citeRef)
    {
        return "<cite class=\"ref\" data-ref=\"" + escape(citeRef.getRef()) + "\">[" + 
               biblio.getOrDefault(citeRef.getRef(), citeRef.getRef()) + "]</cite>";
    }

    protected String render(MathDefinition math)
    {
        return renderMathFormula(math.getText(), false, null);
    }

    protected String render(MathBlockDefinition math)
    {
        return renderMathFormula(math.getText(), true, math.getLabel());
    }

    private String renderMathFormula(String formula, boolean isBlock, String label) {
        String svg;
        
        String latexExpression;
        if (isBlock) {
            latexExpression = "$$" + formula + "$$";
        } else {
            latexExpression = "$" + formula + "$";
        }
        
        try {
            svg = SvgGenerator.generateSvg(latexExpression);
            formulasGenerated++;
        } catch (Exception e) {
            formulasFailed++;
            System.err.println("Failed to generate SVG for formula: " + formula);
            svg = "<span class=\"math-error\">[Math Error]</span>";
        }
        
        StringBuilder result = new StringBuilder();
        
        if (isBlock) {
            boolean hasLabel = (label != null && !label.isEmpty());
            
            if (hasLabel) {
                int number = equationNumbers.size() + 1;
                equationNumbers.put(label, number);
                referencedFormulas++;
                
                result.append("<div class=\"math-block\" id=\"").append(label).append("\">\n");
                result.append(svg).append("\n");
                result.append("<span class=\"eq-number\">(").append(number).append(")</span>\n");
                result.append("</div>\n");
            } else {
                result.append("<div class=\"math-block\">\n");
                result.append(svg).append("\n");
                result.append("</div>\n");
            }
        } else {
            result.append("<span class=\"math-inline\">").append(svg).append("</span>");
        }
        
        return result.toString();
    }

    protected String render(LabelDefinition label)
    {
        return "<span class=\"label\" id=\"" + escape(label.getLabel()) + "\"></span>\n";
    }

    protected String render(Reference ref)
    {
        String refId = ref.getRef();
        
        if (equationNumbers.containsKey(refId)) {
            int number = equationNumbers.get(refId);
            return "<a href=\"#" + refId + "\" class=\"ref\">(" + number + ")</a>";
        }
        
        if (ref.getType() == Reference.Type.PAGE) {
            return "<span class=\"ref\" data-ref=\"" + escape(refId) + "\">[p. " + escape(refId) + "]</span>";
        } else {
            return "<a href=\"#" + escape(refId) + "\" class=\"ref\">[" + escape(refId) + "]</a>";
        }
    }

    protected String onHeading(int level, StringBuilder builder)
    {
        return "";
    }
    
    public void printStatistics(long totalTime) {
        System.out.println("\n=== Conversion Statistics ===");
        System.out.println("SVG generated successfully: " + formulasGenerated);
        System.out.println("SVG failed: " + formulasFailed);
        System.out.println("Referenced formulas: " + referencedFormulas);
        System.out.println("Total conversion time: " + totalTime + " ms");
        System.out.println("=============================\n");
    }

    public String parse(String text)
    {
        final var b = new StringBuilder();
        final var renderer = new HtmlRenderer.Builder()
            .attributeProviderFactory(c -> new AnchorAttributes())
            .nodeRendererFactory(c -> new HtmlNodeRenderer(c){
                @Override String onHeading(Heading heading, Map<String, String> attr)
                {
                    return HtmlTarget.this.onHeading(heading.getLevel(), b);
                }
            })
            .escapeHtml(false)
            .build();
        final var doc = parser.parse(text);
        if (features.contains(Features.CITE))
        {
            new EnumNodes(n -> {
                    if (n instanceof CiteDefinition cite)
                        biblio.put(cite.getRef().trim(), cite.getText().trim());
            }).enumerate(doc);
        }
        renderer.render(doc, b);
        return new String(b);
    }

    public String parse(String text, List<Integer> splits)
    {
        final var b = new StringBuilder();
        final var renderer = new HtmlRenderer.Builder()
            .attributeProviderFactory(c -> new AnchorAttributes())
            .nodeRendererFactory(c -> new HtmlNodeRenderer(c){
                @Override String onHeading(Heading heading, Map<String, String> attr)
                {
                    if (heading.getLevel() == 1)
                        splits.add(Integer.valueOf(b.length()));
                    return HtmlTarget.this.onHeading(heading.getLevel(), b);
                }
            })
            .escapeHtml(false)
            .build();
        final var doc = parser.parse(text);
        if (features.contains(Features.CITE))
        {
            new EnumNodes(n -> {
                    if (n instanceof CiteDefinition cite)
                        biblio.put(cite.getRef().trim(), cite.getText().trim());
            }).enumerate(doc);
        }
        renderer.render(doc, b);
        return new String(b);
    }

    public List<String> parse(List<String> text)
    {
        final var t = text.stream().collect(Collectors.joining("\n")) + "\n";
        return Arrays.asList(parse(t).split("\n"));
    }

    public String escape(String text)
    {
        var t = text;
        if (features.contains(Features.EXT_CHARS))
            t = extChars(t);
        t = Escaping.escapeHtml(t);
        return t;
    }

    static public String extChars(String text)
    {
        final var b = new StringBuilder();
        boolean wasEscaping = false;
        for(int i = 0;i < text.length();i++)
        {
            final char
            c = text.charAt(i),
            cc = (i + 1 < text.length())?text.charAt(i + 1):'\0',
            ccc = (i + 2 < text.length())?text.charAt(i + 2):'\0';
            if (wasEscaping)
            {
                b.append(c);
                wasEscaping = false;
                continue;
            }
            switch(c)
            {
            case '\\':
                wasEscaping = true;
                continue;
            case '~':
                if (i > 0 && !isWhitespace(text.charAt(i)))
                    b.append(NBSP); else
                    b.append("~");
                continue;
            case '<':
                if (cc == '<')
                {
                    b.append(LQUOT);
                    i++;
                    continue;
                }
                b.append("<");
                continue;
            case '>':
                if (cc == '>')
                {
                    b.append(RQUOT);
                    i++;
                    continue;
                }
                b.append(">");
                continue;
            case '-':
                if (ccc == '-' && cc == '-')
                {
                    b.append(MDASH);
                    i += 2;
                    continue;
                }
                if (cc == '-')
                {
                    b.append(NDASH);
                    i++;
                    continue;
                }
                b.append("-");
                continue;
            default:
                b.append(c);
            }
        }
        return new String(b);
    }

    final class AnchorAttributes implements AttributeProvider
    {
        @Override public void setAttributes(Node node, String tagName, Map<String,String> attr)
        {
            if (!tagName.toLowerCase().equals("a"))
                return;
            final var origRef = requireNonNull(attr.get("href"), "The attr map must contain the 'href' entry");
            final var tr = translateRef(origRef);
            if (tr != null)
                attr.put("href", tr);
            if (!attr.containsKey("title"))
            {
                var title = getRefTitle(origRef, requireNonNullElse(tr, origRef));
                if (title != null && !title.trim().isEmpty())
                {
                    if (features.contains(Features.EXT_CHARS))
                        title = extChars(title);
                    attr.put("title", title.trim());
                }
            } else
            {
                if (features.contains(Features.EXT_CHARS))
                    attr.put("title", extChars(attr.get("title")));
            }
        }
    }

    class HtmlNodeRenderer implements NodeRenderer
    {
        final HtmlNodeRendererContext context;
        final HtmlWriter writer;
        HtmlNodeRenderer(HtmlNodeRendererContext context)
        {
            this.context = context;
            this.writer = context.getWriter();
        }
        String onHeading(Heading heading, Map<String, String> attr)
        {
            return null;
        }
        @Override public Set<Class<? extends Node>> getNodeTypes()
        {
            return new HashSet<>(Arrays.asList(
                                                   AdvImageDefinition.class,
                                                   Heading.class,
                                                   LabelDefinition.class,
                                                   MathDefinition.class,
                                                   MathBlockDefinition.class,
                                                   Reference.class,
                                                   CiteReference.class,
                                                   Text.class));
        }
        @Override public void render(Node node)
        {
            if (node instanceof Text text)
            {
                writer.raw(escape(text.getLiteral()));
                return;
            }
            if (node instanceof Heading heading)
            {
                final var attr = new HashMap<String, String>();
                final var prefix = onHeading(heading, attr);
                String htag = "h" + heading.getLevel();
                writer.line();
                writer.tag(htag, attr);
                if (prefix != null)
                    writer.raw(escape(prefix));
                renderChildren(heading);
                writer.tag('/' + htag);
                writer.line();
            }
            if (node instanceof Reference ref)
                writer.raw(HtmlTarget.this.render(ref));
            if (node instanceof LabelDefinition label)
                writer.raw(HtmlTarget.this.render(label));
            if (node instanceof MathDefinition math)
                writer.raw(HtmlTarget.this.render(math));
            if (node instanceof MathBlockDefinition math)
                writer.raw(HtmlTarget.this.render(math));
            if (node instanceof AdvImageDefinition advImage)
                writer.raw(HtmlTarget.this.render(advImage));
            if (node instanceof CiteReference citeRef)
                writer.raw(HtmlTarget.this.render(citeRef));
        }
        private void renderChildren(Node node)
        {
            for(Node n = node.getFirstChild();n != null; n = n.getNext())
                context.render(n);
        }
    }
}