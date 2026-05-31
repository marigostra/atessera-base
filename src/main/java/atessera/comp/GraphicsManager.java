// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.comp;

import java.util.*;
import java.io.*;
import org.apache.logging.log4j.*;

import atessera.comp.*;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.util.IdStr.*;
import static atessera.comp.CompilerFactory.*;
import static atessera.comp.CompilerTask.*;

public final class GraphicsManager
{
    static private final Logger log = LogManager.getLogger();

    final String type;
    final Map<Integer, Image> images = new HashMap<>();
    final Map<String, byte[]> bitmapsPdf = new HashMap<>();

    public GraphicsManager(String type)
    {
	this.type = requireNonNull(type, "type can't be null");
    }

        private void compile(Image image, int index) throws IOException
    {
	final var b = newCompilerTaskBuilder(type)
	.format(image.format)
	.src(image.source);
	final var preview = ((image.source.size() < 5)?image.source:image.source.subList(0, 5))
	.stream()
	.map(s -> "'" + s.trim() + "'")
	.collect(joining(", "));
		log.trace("Compiling image " + image.toString());
	log.trace("Compiling the image #" + index + " of " + image.format + " format (" + preview + ")");
	try (final var c = b.build()){
	    try {
		image.output = c.run();
			//TODO: Better to check that main.png and main.eps present in the output map
	    }
	    finally {
		image.stdout = c.getStdout();
		image.stderr = c.getStderr();
	    }
	}
	log.trace("The " + image.format.toString() + " image compiled successfully");
    }

    public void add(int index, Format format, List<String> source)
    {
	if (index < 0)
	    throw new IllegalArgumentException("index can't be negative (" + index + ")");
	requireNonNull(format, "format can't be null");
	requireNonNull(source, "source can't be null");
	images.put(Integer.valueOf(index), new Image(format, source));
    }

    public void addBitmapPdf(String id, byte[] bytes)
    {
	bitmapsPdf.put(requireNonNull(id, "id can't be null"),
		       requireNonNull(bytes, "bytes can't be null"));
    }

    public boolean hasId(String id)
    {
	for(var e: images.entrySet())
	    if (e.getValue().id.equals(id))
		return true;
	return false;
    }

    public void compile() throws IOException
    {
	for(var e: images.entrySet())
	    compile(e.getValue(), e.getKey().intValue());
	    }

    public String getImageId(int index)
    {
	return images.get(Integer.valueOf(index)).id;
    }

    /*
    byte[] getCompiledImage(int index, String format)
    {
	requireNonNull(format, "format can't be null");
	return images.get(Integer.valueOf(index)).output.get(format);
    }

    byte[] getImagePng(int index)
    {
	return images.get(Integer.valueOf(index)).output.get(CompilerBuilder.IMAGE_PNG);
    }

    byte[] getImageEps(int index)
    {
	return images.get(Integer.valueOf(index)).output.get(CompilerBuilder.IMAGE_EPS);
    }
    */

    public Map<String, byte[]> getAllImages(String format)
    {
	requireNonNull(format, "format can't be null");
	final var res = new HashMap<String, byte[]>();
	for(var e: images.entrySet())
	    res.put(e.getValue().id, e.getValue().output.get(format));
	if (format.equals(IMAGE_PDF))
	    for(var e: bitmapsPdf.entrySet())
		res.put(e.getKey() + ".pdf", e.getValue());
	return res;
    }

    static final class Image
    {
	final String id = getRandomId(10);
	final Format format;
	final List<String> source;
	String stdout = null, stderr = null;
	Map<String, byte[]> output = null;

	Image(Format format, List<String> source)
	{
	    this.format = format;
	    this.source = source;
	}

	@Override public String toString()
	{
	    final var b = new StringBuilder();
	    b.append("format=").append(format.toString());
	    b.append(", text=").append(source.stream()
				       .map(i -> ("\"" + i + "\""))
				       .collect(joining(", "))
				       );
	    return new String(b);
	}
    }
}
