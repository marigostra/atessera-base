// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.templ;

import java.util.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

/**
 * Velocity template for generating MetaPost figure source.
 *
 * <p>This class populates the {@code metapost.vm} template with the figure
 * drawing commands, producing a compilable MetaPost source file. The
 * resulting source can be compiled with
 * {@link atessera.comp.Metapost Metapost}.</p>
 *
 * <p>Typical usage:</p>
 *
 * <pre>{@code
 * MetapostTemplate t = new MetapostTemplate(factory);
 * t.setText(metapostCommands);
 * List<String> mpSource = t.renderToStringList();
 * }</pre>
 *
 * @see Base
 * @see atessera.comp.Metapost
 */
public final class MetapostTemplate extends Base
{
    /**
     * Constructs a new MetaPost template.
     *
     * @param engineFactory the factory for obtaining a Velocity engine;
     *                      must not be {@code null}
     * @throws org.apache.velocity.exception.ResourceNotFoundException if
     *         {@code metapost.vm} cannot be loaded
     */
    public MetapostTemplate(EngineFactory engineFactory)
    {
	super(engineFactory, "metapost.vm");
    }

    /**
     * Sets the MetaPost drawing commands.
     *
     * @param text the MetaPost source lines; may be {@code null} or empty
     * @return this template (for fluent chaining)
     */
    public MetapostTemplate setText(List<String> text)
    {
	context.put("TEXT", requireNonNullElse(text, new ArrayList<String>()).stream().collect(joining("\n")));
	return this;
    }
}
