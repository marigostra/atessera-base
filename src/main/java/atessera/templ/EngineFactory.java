// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import org.apache.logging.log4j.*;

import org.apache.velocity.app.*;
import org.apache.velocity.*;
import org.apache.velocity.runtime.resource.loader.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.resource.util.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

/**
 * Factory for creating and caching Apache Velocity engines configured with
 * in-memory template storage.
 *
 * <p>This class accepts a map of template names to template source lines at
 * construction time. When {@link #newEngine()} is called, it configures a
 * {@link VelocityEngine} with a {@code StringResourceLoader} pointing to a
 * static, named repository ({@code "GlobalRepo"}), populates it with all the
 * templates, and caches the engine for subsequent calls.</p>
 *
 * <p>Well-known template names are exposed as public constants:</p>
 *
 * <ul>
 *   <li>{@link #PUBLICATION} &mdash; {@code "publication.vm"}</li>
 *   <li>{@link #PRESENTATION} &mdash; {@code "presentation.vm"}</li>
 *   <li>{@link #METAPOST} &mdash; {@code "metapost.vm"}</li>
 *   <li>{@link #GNUPLOT} &mdash; {@code "gnuplot.vm"}</li>
 * </ul>
 *
 * @see Base
 * @see org.apache.velocity.app.VelocityEngine
 */
public final class EngineFactory 
{
    /** Template name for publication LaTeX source. */
    static public final String
	PUBLICATION = "publication.vm",
    /** Template name for presentation (Beamer) LaTeX source. */
	PRESENTATION = "presentation.vm",
    /** Template name for MetaPost figure source. */
	METAPOST = "metapost.vm",
    /** Template name for GNUPlot chart source. */
	GNUPLOT = "gnuplot.vm";
    
    static private final Logger log = LogManager.getLogger();

    /** Template sources keyed by template name. */
    final Map<String, List<String>> templates;

    /** Cached Velocity engine; created on first call to {@link #newEngine()}. */
    private VelocityEngine cached = null;

    /**
     * Constructs a new engine factory with the given template sources.
     *
     * @param templates a map where keys are template resource names and
     *                  values are the template source lines; must not be
     *                  {@code null}
     */
    public EngineFactory(Map<String, List<String>> templates)
    {
	this.templates = requireNonNull(templates, "templates can't be null");
    }

    /**
     * Creates (or returns the cached) Velocity engine configured with all
     * templates registered in this factory.
     *
     * <p>The engine uses a static {@code StringResourceRepository} named
     * {@code "GlobalRepo"} so that templates survive engine re-creation.
     * The returned engine is cached and the same instance is returned on
     * subsequent calls.</p>
     *
     * @return a configured {@link VelocityEngine} ready for template
     *         retrieval
     * @throws ResourceNotFoundException if a template cannot be loaded
     */
    public synchronized VelocityEngine newEngine() throws ResourceNotFoundException
    {
	if (cached != null)
	    return cached;
        final var engine = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty("str.resource.loader.class", StringResourceLoader.class.getName());
	props.setProperty("str.resource.loader.cache", "true");
        props.setProperty(RuntimeConstants.RESOURCE_LOADER, "str");
        props.setProperty("str.resource.loader.repository.name", "GlobalRepo");
        props.setProperty("str.resource.loader.repository.static", "true");
        engine.init(props);
	final StringResourceRepository repo = StringResourceLoader.getRepository("GlobalRepo");
        for(var e: templates.entrySet()) 
            repo.putStringResource(e.getKey(), e.getValue().stream().collect(joining("\n")));
        cached = engine;
        return engine;
    }
}
