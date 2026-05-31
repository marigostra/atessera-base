// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2025 Michael Pozhidaev <msp@luwrain.org>

package atessera.util;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class TextUtils
{
    static public List<String> readTextFile(Path path)
    {
	return readTextFile(path.toFile());
    }
    
    static public List<String> readTextFile(File file)
    {
	try {
	    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
		final List<String> lines = new ArrayList<>();
		String line = reader.readLine();
		while (line != null)
		{
		    lines.add(line);
		    line = reader.readLine();
		}
		return lines;
	    }
	}
	catch(IOException e)
	{
	    throw new RuntimeException(e);
	}
    }

        static public String[] readLines(File file, boolean toUpperCase)
    {
	final List<String> res = new ArrayList<>();
	for(String line: readTextFile(file))
	    if (!line.trim().isEmpty() && line.trim().charAt(0) != '#')
		res.add(toUpperCase?line.trim().toUpperCase():line.trim());
	return res.toArray(new String[res.size()]);
    }

    static public Set<String> readSetUpperCase(File file)
    {
	final Set<String> res = new HashSet();
	final List<String> lines = readTextFile(file);
	for(String l: lines)
	{
	    final String s = l.trim();
	    if (!s.isEmpty())
		res.add(s.toUpperCase());
	}
	return res;
    }

        static public void writeTextFile(Path path, List<String> lines)
    {
	writeTextFile(path.toFile(), lines);
    }

    static public void writeTextFile(File file, List<String> lines)
    {
	try {
	    try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
		for(String s: lines)
		{
		    w.write(s);
		    w.newLine();
		}
		w.flush();
	    }
	}
	catch(IOException e)
	{
	    throw new RuntimeException(e);
	}
    }

    static public List<String> readJavaResource(Class c, String resName)
    {
	try {
	    final List<String> res = new ArrayList<>();
	    try (final BufferedReader r = new BufferedReader(new InputStreamReader(c.getResourceAsStream(resName), "UTF-8"))) {
		String line = r.readLine();
		while(line != null)
		{
		    res.add(line);
		    line = r.readLine();
		}
	    }
	    return res;
	}
	catch(IOException e)
	{
	    throw new RuntimeException(e);
	}
    }
}
