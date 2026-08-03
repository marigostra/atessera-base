// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import java.io.*;
//import lombok.*;

import org.apache.velocity.*;
import org.apache.velocity.exception.*;


import static java.util.Objects.*;

class Base
{
    final VelocityContext context = new VelocityContext();
    final Template templ;

    Base(EngineFactory engineFactory, String templName) throws ResourceNotFoundException
    {
	final var engine = engineFactory.newEngine();
	templ = engine.getTemplate(requireNonNull(templName, "templName can't be null"));
    }

    public void render(Writer wr)
    {
	templ.merge( context, wr );
    }

    public List<String> renderToStringList()
    {
	final StringWriter w = new StringWriter();
	render(w);
	return Arrays.asList(w.toString().split("\n", -1));
    }
}
