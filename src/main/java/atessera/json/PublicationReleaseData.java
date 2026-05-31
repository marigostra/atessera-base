// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.json;

import java.util.*;
import lombok.*;
import com.google.gson.*;

import static java.util.Objects.*;

@Data
@NoArgsConstructor
public final class PublicationReleaseData
{
    static private final Gson gson = new Gson();

        @Data
    static public final class Graphics
    {
	String label;
	String pngFilePath;
    }

    private String id, sourceContentId;
    private long creationTimestamp;
    private String pdfFilePath;
    private Map<String, Graphics> graphics;

    static public String toJson(PublicationReleaseData d)
    {
	return gson.toJson(requireNonNullElse(d, new PublicationReleaseData()));
    }

    static public PublicationReleaseData fromJson(String s)
    {
	if (s == null || s.isEmpty())
	    return new PublicationReleaseData();
final PublicationReleaseData res = gson.fromJson(s, PublicationReleaseData.class);
return res != null?res:new PublicationReleaseData();
    }

}
