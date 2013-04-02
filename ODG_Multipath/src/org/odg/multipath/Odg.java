package org.odg.multipath;

import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class Odg extends ListenableUndirectedWeightedGraph<OdgVertex,OdgEdge> {

	private static final long serialVersionUID = 202762474932879914L;

	public Odg() {
		super(OdgEdge.class);
	}
}
