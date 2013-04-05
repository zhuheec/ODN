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

	private static final long serialVersionUID = 7981569470984390270L;
	
	private WeightedGraph<OdgObject, OdgRelation> graph;
	
	public OdgRelation(WeightedGraph<OdgObject, OdgRelation> graph, double propagatedVul) {
		this.graph = graph;
		graph.setEdgeWeight(this, propagatedVul);
	}

	/**
	 * Returns the propagated vulnerability of this edge.
	 * @return propagated vulnerability
	 */
	public double getPropagatedVul() {
		return graph.getEdgeWeight(this);
	}

	/**
	 * Assigns a propagated vulnerability to an edge.
	 * @param propagatedVul propagated vulnerability
	 */
	public void setPropagatedVul(double propagatedVul) {
		graph.setEdgeWeight(this, propagatedVul);
	}
	
}
