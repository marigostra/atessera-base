// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import static java.util.Objects.*;

/** Saves the information about a translated reference for future title generation or JavaScript integration.*/
public class RefTranslation
{
    protected final String origRef;
    protected String translatedRef;

    public RefTranslation(String origRef, String translatedRef)
    {
	this.origRef = requireNonNull(origRef, "origRef can't be null");
	this.translatedRef = requireNonNull(translatedRef, "translatedRef can't be null");
    }

    public RefTranslation(String ref)
    {
	this(ref, ref);
    }

    public final String getOrigRef()
    {
	return origRef;
    }

    public String getTranslatedRef()
    {
	return translatedRef;
    }
}
