package org.odg.multipath;

/**
 * The edge implementation of ODG.
 * Each edge has its propagated vulnerability.
 * @author He Zhu
 *
 */
public class OdgEdge {
	
	private double propagatedVul = 0.0;

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
