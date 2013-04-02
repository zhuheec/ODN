package org.odg.multipath;

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * The edge implementation of ODG.
 * Each edge has its propagated vulnerability.
 * @author He Zhu
 *
 */
public class OdgRelation extends DefaultWeightedEdge {
	
	private WeightedGraph<OdgObject, OdgRelation> graph;
	private double propagatedVul = 0.0;
	
	public OdgRelation(WeightedGraph<OdgObject, OdgRelation> graph, double propagatedVul) {
		System.out.println("OdgRelation initialized!");
		this.graph = graph;
		graph.setEdgeWeight(this, propagatedVul);
	}

	/**
	 * Returns the propagated vulnerability of this edge.
	 * @return propagated vulnerability
	 */
	public double getPropagatedVul() {
		return propagatedVul;
	}

	/**
	 * Assigns a propagated vulnerability to an edge.
	 * @param propagatedVul propagated vulnerability
	 */
	public void setPropagatedVul(double propagatedVul) {
		this.propagatedVul = propagatedVul;
	}
	
}
