// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.util;

public final class LatexUtils
{
        static public String escape(String str)
    {
	final var b = new StringBuilder();
	for(int i = 0;i < str.length();i++)
	{
	    final char ch = str.charAt(i);
	    switch(ch)
	    {
	    case '\\':
				b.append("{\\textbackslash}");
		break;
	    case '{':
	    case '}':
	    case '_':
	    case '^':
	    case '#':
	    case '&':
	    case '$':
	    case '%':
			    case '~':
		b.append("\\").append(ch);
		break;
			    case '\"':
		//FIXME: Find better sequence to print quotes
		b.append("{\'\'}");
		break;
			    default:
		b.append(ch);
	    }
	}
	return new String(b);
    }

    static public String escapeStrict(String str)
    {
	final var b = new StringBuilder();
	for(int i = 0;i < str.length();i++)
	{
	    final char ch = str.charAt(i);
	    switch(ch)
	    {
	    case '\\':
				b.append("{\\textbackslash}");
		break;
	    case '{':
	    case '}':
	    case '_':
		//	    case '^':
	    case '#':
	    case '&':
	    case '$':
	    case '%':
		b.append("\\").append(ch);
		break;
							    case '^':
		b.append("\\textasciicircum{}");
		break;
					    case '~':
		b.append("\\textasciitilde{}");
		break;
	    case '-':
	    case '<':
	    case '>':
	    case '\'':
	    case '`':
		b.append("{").append(ch).append("}");
		break;
			    case '\"':
		//FIXME: Find better sequence to print quotes
		b.append("{\'\'}");
		break;
	    default:
		b.append(ch);
	    }
	}
	return new String(b);
    }

            static public String escapeRelaxed(String str)
    {
	final var b = new StringBuilder();
	for(int i = 0;i < str.length();i++)
	{
	    final char ch = str.charAt(i);
	    switch(ch)
	    {
	    case '\\':
				b.append("{\\textbackslash}");
		break;
	    case '{':
	    case '}':
	    case '_':
		//	    case '^':
	    case '#':
	    case '&':
	    case '$':
	    case '%':
		//	    case '~':
		b.append("\\").append(ch);
				break;
											    case '^':
		b.append("\\textasciicircum{}");
		break;
	    case '\"':
		//FIXME: Find better sequence to print quotes
		b.append("{\'\'}");
		break;
	    default:
		b.append(ch);
	    }
	}
	return new String(b);
    }
}
