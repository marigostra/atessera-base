// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import java.io.*;

import org.apache.velocity.*;
import org.apache.velocity.exception.*;

import static java.util.Objects.*;

/**
 * Common base class for all Velocity-based templates.
 *
 * <p>Wraps an Apache Velocity {@link Template} and its associated
 * {@link VelocityContext}. Subclasses populate the context via their
 * setter methods and then call {@link #render(Writer)} or
 * {@link #renderToStringList()} to produce the final output.</p>
 *
 * <p>Template loading is delegated to {@link EngineFactory}, which
 * configures Velocity with an in-memory {@code StringResourceLoader}
 * so that template sources can be supplied programmatically.</p>
 *
 * @see EngineFactory
 * @see PublicationTemplate
 * @see PresentationTemplate
 * @see MetapostTemplate
 * @see GNUPlotTemplate
 */
class Base
{
    /** The Velocity context shared with subclasses for populating template variables. */
    final VelocityContext context = new VelocityContext();

    /** The compiled Velocity template. */
    final Template templ;

    /**
     * Loads the named template via the given engine factory.
     *
     * @param engineFactory the factory that provides a configured
     *                      {@link VelocityEngine}; must not be {@code null}
     * @param templName     the template resource name (e.g.
     *                      {@code "publication.vm"}); must not be
     *                      {@code null}
     * @throws ResourceNotFoundException if the template cannot be found
     */
    Base(EngineFactory engineFactory, String templName) throws ResourceNotFoundException
    {
	final var engine = engineFactory.newEngine();
	templ = engine.getTemplate(requireNonNull(templName, "templName can't be null"));
    }

    /**
     * Merges the context into the template and writes the result to the
     * given writer.
     *
     * @param wr the writer to receive the rendered output; must not be
     *           {@code null}
     */
    public void render(Writer wr)
    {
	templ.merge( context, wr );
    }

    /**
     * Renders the template and returns the result split into lines.
     *
     * @return the rendered output as a list of lines (trailing empty lines
     *         are preserved)
     */
    public List<String> renderToStringList()
    {
	final StringWriter w = new StringWriter();
	render(w);
	return Arrays.asList(w.toString().split("\n", -1));
    }
}
