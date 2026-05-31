// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.util;

import java.security.*;

public final class IdStr
{
    static IdStr instance = null;

    final SecureRandom rand;

    protected IdStr()
    {
	rand = new SecureRandom();
	rand.setSeed(System.currentTimeMillis());
    }

    public synchronized String getId(int length)
{
    if (length < 1)
	throw new IllegalArgumentException("length must be greater than zero");
    final StringBuilder b = new StringBuilder();
    while (b.length() < length)
    {
	final byte[] bytes = new byte[32];
    rand.nextBytes(bytes);
    final var str = new String(bytes);
    for(int i = 0;i < str.length() && b.length() < length;i++)
	if (isIdChar(str.charAt(i)))
	b.append(str.charAt(i));
    }
    return new String(b);
}

static boolean isIdChar(char ch)
    {
	return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || (ch == '_');
    }

    static public String getRandomId(int length)
    {
	if (instance != null)
	    return instance.getId(length);
	instance = new IdStr();
	return instance.getId(length);
    }
}
