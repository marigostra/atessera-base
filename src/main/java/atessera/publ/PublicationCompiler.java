// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.publ;

import java.util.*;
import java.io.*;
import org.apache.logging.log4j.*;

import atessera.markdown.*;
import atessera.comp.*;
import atessera.json.*;
import atessera.json.PublicationReleaseData.Graphics;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static atessera.util.IdStr.*;
import static atessera.comp.CompilerFactory.*;
import static atessera.comp.CompilerTask.*;

abstract class PublicationCompiler
{
    static private final Logger log = LogManager.getLogger();

    static public final String
	OUTPUT_FILE_PDF = "doc.pdf",
	OUTPUT_FILE_HTML = "output.html";

    //final CompilerBuilder.Type type;
    final PublicationContent publ;
    final String id = getRandomId(8);
    		final GraphicsManager gr;
    final PublicationReleaseData data = new PublicationReleaseData();

    protected PublicationCompiler(PublicationContent publ)
    {
	this.publ = requireNonNull(publ, "publ can't be null");
	this.gr = new GraphicsManager("");
		data.setId(id);
			data.setGraphics(new HashMap<>());
    }

    abstract protected void saveGraphicsFile(String name, byte[] data) throws IOException;
        abstract protected String saveOutputFile(String name, byte[] data) throws IOException;
    abstract protected void saveCompilationDetails(String name, String text) throws IOException;
    abstract protected byte[] readBinaryImage(String fileName) throws IOException;

    public boolean compile() throws IOException
    {
	requireNonNull(publ.getSections(), "No sections in the publication to compile");
log.trace("Compiling graphics");
compileGraphics();
		log.trace("All graphics have been successfully compiled");
		if (!compileTex())
		    return false;
		if (!compileHtml())
		    return false;
		return true;
    }

        boolean compileHtml() throws IOException
    {
	final var tr = new HtmlTranslator(publ);
	tr.translate();
	for(final var e: tr.output.entrySet())
	{
	final var os = new ByteArrayOutputStream();
	try (final var w = new BufferedWriter(new OutputStreamWriter(os))) {
	    for(var l: e.getValue())
	    {
		w.write(l);
		w.newLine();
	    }
	    w.flush();
	    os.flush();
	    saveOutputFile(e.getKey(), os.toByteArray());
	}
	}
	return true;
    }

    boolean compileTex() throws IOException
    {
		//Collecting listings
		final var listings = new HashMap<Integer, String>();
			for(int i = 0;i < publ.getSections().size();i++)
			    listings.put(Integer.valueOf(i), getRandomId(8) + ".lst");
			final var tr = new TexTranslator("book.vm", gr, publ, listings);
	final var mainSource = tr.translate();
	saveCompilationDetails("latex-main.tex", mainSource.stream().collect(joining("\n")));
	final var imageExt = IMAGE_PDF.substring(IMAGE_PDF.indexOf("."));
	final var b = newCompilerTaskBuilder("local")
	.format(Format.PDFLATEX)
	.src(mainSource);
	for(var e: gr.getAllImages(IMAGE_PDF).entrySet())
	    b.input(e.getKey() + imageExt, e.getValue());
				for(int i = 0;i < publ.getSections().size();i++)
				    b.input(listings.get(Integer.valueOf(i)), publ.getSections().get(i).getSource()
					    .stream().collect(joining("\n"))
					    .getBytes());
				final Map<String, byte[]> output;
	try (final var c = b.build()) {
	    log.trace("Running latex for compilation of the main file of the publication");
	    final var startTime = System.currentTimeMillis();
	    output = c.run();
	    	    log.trace("Compiler finished in " + (System.currentTimeMillis() - startTime) + "ms");
	    if (!output.containsKey(CompilerTask.LATEX_OUTPUT_FILE))
	    {
		//		log.error("Compiler has finished without errors, but there is no any compiled data (" + CompilerBuilder.LATEX_OUTPUT_FILE + ")");
		return false;
	    }
	    data.setCreationTimestamp(new Date().getTime());
	    //	    data.setPdfFilePath(saveOutputFile(OUTPUT_FILE_PDF, output.get(CompilerBuilder.LATEX_OUTPUT_FILE)));
	}
	return true;
    }

        void compileGraphics() throws IOException
    {
	for(int i = 0;i < publ.getSections().size();i++)
	{
	    final var s = publ.getSections().get(i);
	    switch(s.getType())
	    {
	    case PLANTUML:
		gr.add(i, Format.PLANTUML, requireNonNullElse(s.getSource(), Arrays.asList()));
		break;
			    case METAPOST:
		gr.add(i, Format.METAPOST, requireNonNullElse(s.getSource(), Arrays.asList()));
		break;
					    case GNUPLOT:
		gr.add(i, Format.GNUPLOT, requireNonNullElse(s.getSource(), Arrays.asList()));
		break;
							    case GRAPHVIZ_DOT:
		gr.add(i, Format.DOT, requireNonNullElse(s.getSource(), Arrays.asList()));
		break;
	    }
	}
gr.compile();
final var bitmaps = collectBitmaps();
log.info("" + bitmaps.size() + " bitmaps");
for(var b: bitmaps)
    if (!gr.hasId(b))
    {
	log.info("Reading " + b);
	var data = readBinaryImage(b + ".pdf");
	if (data == null)
	    throw new IOException("No binary image: " + b);
	log.info("Adding " + b + ".pdf");
	gr.addBitmapPdf(b, data);
    }
    }

    List<String> collectBitmaps()
    {
	final var res = new ArrayList<String>();
	if (publ.getSections() == null)
	    return res;
	for(final var s: publ.getSections())
	    if (s != null && s.getSource() != null)
	    {
		final var l = new LatexTarget(EnumSet.allOf(LatexTarget.Features.class));
		final var doc = l.parser.parse(s.getSource().stream().collect(joining("\n")));
		new EnumNodes(n -> {
			if (n instanceof org.commonmark.node.Image image)
			    res.add(image.getDestination());
		}).enumerate(doc);
	    }
	return res;
    }

    public PublicationReleaseData getReleaseData()
    {
	return data;
    }

    static String finalLines(String text)
    {
var lines = Arrays.asList(text.split("\n", -1));
if (lines.size() > 10)
    lines = lines.subList(lines.size() - 10, lines.size());
return lines.stream().collect(joining("\n"));
	}
}
