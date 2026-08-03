// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

public final class GNUPlotTemplate extends Base
{
    public GNUPlotTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "gnuplot.vm");
    }

    public GNUPlotTemplate setText(List<String> text)
    {
	context.put("TEXT", requireNonNullElse(text, new ArrayList<String>()).stream().collect(joining("\n")));
	return this;
    }
}
