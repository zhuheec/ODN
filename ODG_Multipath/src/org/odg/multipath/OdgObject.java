package org.odg.multipath;

/**
 * The vertex implementation of ODG.
 * Each edge has its self-originated vulnerability.
 * @author He Zhu
 *
 */
public class OdgObject {
	
	private String name;
	private double cloudOriginatedVul = 0.1;
	
	public OdgObject(String name, double cloudOriginatedVul) {
		this.name = name;
		this.cloudOriginatedVul = cloudOriginatedVul;
	}

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

	/**
	 * Returns the name of the object
	 * @return object name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Assigns the name of the object
	 * @param name object name
	 */
	public void setName(String name) {
		this.name = name;
	}
}
