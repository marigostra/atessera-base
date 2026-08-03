// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

//https://velocity.apache.org/engine/1.7/developer-guide.html

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

public final class EngineFactory 
{
static public final String
    PUBLICATION = "publication.vm",
    PRESENTATION = "presentation.vm",
    METAPOST = "metapost.vm",
    GNUPLOT = "gnuplot.vm";
    
    static private final Logger log = LogManager.getLogger();

    final Map<String, List<String>> templates;
    private VelocityEngine cached = null;

    public EngineFactory(Map<String, List<String>> templates)
    {
	this.templates = requireNonNull(templates, "templates can't be null");
    }

    /*
    public VelocityEngine newEngine() throws ResourceNotFoundException
    {
	final var engine = new VelocityEngine();
	Properties props = new Properties();
props.setProperty("str.resource.loader.class", StringResourceLoader.class.getName());
props.setProperty("str.resource.loader.cache", "true");
props.setProperty(RuntimeConstants.RESOURCE_LOADER, "str");
	engine.init(props);
	final var  repo = StringResourceLoader.getRepository();
	for(var e: templates.entrySet())
	    repo.putStringResource(e.getKey(), e.getValue().stream().collect(joining("\n")));
	return engine;
    }
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
