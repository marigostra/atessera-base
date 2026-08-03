// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

/**
 * Velocity template for generating GNUPlot chart source.
 *
 * <p>This class populates the {@code gnuplot.vm} template with the plot
 * commands, producing an executable GNUPlot script. The resulting script
 * can be compiled with {@link atessera.comp.GNUPlot GNUPlot}.</p>
 *
 * <p>Typical usage:</p>
 *
 * <pre>{@code
 * GNUPlotTemplate t = new GNUPlotTemplate(factory);
 * t.setText(gnuplotCommands);
 * List<String> gpSource = t.renderToStringList();
 * }</pre>
 *
 * @see Base
 * @see atessera.comp.GNUPlot
 */
public final class GNUPlotTemplate extends Base
{
    /**
     * Constructs a new GNUPlot template.
     *
     * @param engineFactory the factory for obtaining a Velocity engine;
     *                      must not be {@code null}
     * @throws org.apache.velocity.exception.ResourceNotFoundException if
     *         {@code gnuplot.vm} cannot be loaded
     */
    public GNUPlotTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "gnuplot.vm");
    }

    /**
     * Sets the GNUPlot commands.
     *
     * @param text the GNUPlot script lines; may be {@code null} or empty
     * @return this template (for fluent chaining)
     */
    public GNUPlotTemplate setText(List<String> text)
    {
	context.put("TEXT", requireNonNullElse(text, new ArrayList<String>()).stream().collect(joining("\n")));
	return this;
    }
}
