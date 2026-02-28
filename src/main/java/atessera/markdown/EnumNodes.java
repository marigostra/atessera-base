// SPDX-License-Identifier: BUSL-1.1
// Copyright 2024-2026 Michael Pozhidaev <msp@luwrain.org>

package atessera.markdown;

import java.util.*;
import java.util.function.*;

import org.commonmark.node.*;

import static java.util.Objects.*;

public final class EnumNodes
{
    final Consumer<Node> cons;
    public EnumNodes(Consumer<Node> cons) { this.cons = requireNonNull(cons, "cons can't be null"); }

    public void enumerate(Node node)
    {
	requireNonNull(node, "node can't be null");
	cons.accept(node);
		    for(Node n = node.getFirstChild();n != null; n = n.getNext())
			enumerate(n);
    }

    
}
