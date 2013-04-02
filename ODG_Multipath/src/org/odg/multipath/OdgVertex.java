package org.odg.multipath;

/**
 * The vertex implementation of ODG.
 * Each edge has its self-originated vulnerability.
 * @author He Zhu
 *
 */
public class OdgVertex {
	private double cloudOriginatedVul;

	/**
	 * Returns the cloud-originated vulnerability of this vertex
	 * @return cloud-originated vulnerability
	 */
	public double getCloudOriginatedVul() {
		return cloudOriginatedVul;
	}

	/**
	 * Assigns the cloud-originated vulnerability to this vertex
	 * @param selfOriginatedVul cloud-originated vulnerability
	 */
	public void setCloudOriginatedVul(double selfOriginatedVul) {
		this.cloudOriginatedVul = selfOriginatedVul;
	}
}
