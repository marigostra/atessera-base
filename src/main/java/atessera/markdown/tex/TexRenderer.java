// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown.tex;

import org.commonmark.Extension;
import org.commonmark.internal.renderer.NodeRendererMap;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

public final class TexRenderer implements Renderer
{
    private final boolean stripNewlines;

    private final List<TexNodeRendererFactory> nodeRendererFactories;

    private TexRenderer(Builder builder)
    {
        this.stripNewlines = builder.stripNewlines;
        this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
        this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
        this.nodeRendererFactories.add(context -> new CoreRenderer(context));
    }

    static public Builder builder()
    {
        return new Builder();
    }

    @Override public void render(Node node, Appendable output)
    {
        RendererContext context = new RendererContext(new TexWriter(output));
        context.render(node);
    }

    @Override public String render(Node node)
    {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    static public final class Builder
    {
        private boolean stripNewlines = false;
        private List<TexNodeRendererFactory> nodeRendererFactories = new ArrayList<>();
        public TexRenderer build() {
            return new TexRenderer(this);
        }
        public Builder stripNewlines(boolean stripNewlines)
	{
            this.stripNewlines = stripNewlines;
            return this;
        }
        public Builder nodeRendererFactory(TexNodeRendererFactory nodeRendererFactory)
	{
            this.nodeRendererFactories.add(nodeRendererFactory);
            return this;
        }
	/*
        public Builder extensions(Iterable<? extends Extension> extensions)
	{
            for (Extension extension : extensions) {
                if (extension instanceof TexRenderer.TextContentRendererExtension) {
                    TexRenderer.TextContentRendererExtension textContentRendererExtension = (TexRenderer.TextContentRendererExtension) extension;
                    textContentRendererExtension.extend(this);
                }
            }
            return this;
        }
	*/
    }
    /*
    public interface TextContentRendererExtension extends Extension
    {
        void extend(TexRenderer.Builder rendererBuilder);
    }
    */

    private class RendererContext implements TexNodeRendererContext
    {
        private final TexWriter texContentWriter;
        private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

        private RendererContext(TexWriter texContentWriter)
	{
            this.texContentWriter = texContentWriter;
	    //            for (int i = nodeRendererFactories.size() - 1; i >= 0; i--)
	    for (int i = 0;i < nodeRendererFactories.size();i++)
	    {
                TexNodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
                NodeRenderer nodeRenderer = nodeRendererFactory.create(this);
                nodeRendererMap.add(nodeRenderer);
            }
        }

        @Override public boolean stripNewlines() {
            return stripNewlines;
        }

        @Override public TexWriter getWriter()
	{
            return texContentWriter;
        }

        @Override public void render(Node node)
	{
            nodeRendererMap.render(node);
        }
    }
}
