package org.zh.odn;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class OdnGraph extends TinkerGraph {

	private static final long serialVersionUID = 3403585500487708004L;
	
	public static final String CLASS_NAME_KEY = "className";
	public static final String RELATION_NAME_KEY = "relationName";
	public static final String RELATION_VUL_KEY = "propagate_vul";
	public static final String SELF_VUL_KEY = "self_vul";
	public static final String OVERALL_VUL_KEY = "overall_vul";
	public static final String VISITED_KEY = "visited";
	public static final String RELATION_CONNECTOR = "-->";
	private static final Logger log = Logger.getLogger(OdnGraph.class);
	static { log.setLevel(Level.DEBUG); }
	
	/**
	 * Instantiate of ODN according to specified GraphML file.
	 * @param graphFilePath Input GraphML file
	 */
	public OdnGraph(String graphFilePath, double selfVul, double propagateVul) {
		log.debug("Loading ODN from file [" + graphFilePath + "]...");
		try {
			// read the initial ODN from file
			GraphMLReader.inputGraph(this, new FileInputStream(graphFilePath));
			log.debug("File data loaded. Now preparing vertices...");
			// set vertex name
			int vcount = 0;
			for(Vertex vertex : this.getVertices()) {
				vertex.setProperty(CLASS_NAME_KEY, vertex.getId());
				// set the flag indicating it is not visited yet
				vertex.setProperty(VISITED_KEY, false);
				vertex.setProperty(SELF_VUL_KEY, selfVul);
				vcount++;
			}
			log.debug("All [" + vcount + "] vertices are ready. Now preparing edges...");
			// set edge name
			int ecount = 0;
			for(Edge edge : this.getEdges()) {
				//String relationName = edge.getVertex(Direction.OUT).getId().toString() + RELATION_CONNECTOR
				//		+ edge.getVertex(Direction.IN).getId().toString();
				edge.setProperty(RELATION_NAME_KEY, edge.getId());
				// TODO: calculate vulnerability of edges and vertices
				edge.setProperty(RELATION_VUL_KEY, propagateVul);
				ecount++;
			}
			log.debug("All [" + ecount + "] edges are ready. Finished loading ODN.");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void rewire(Vertex src, String prefix, Vertex lastVertex) {
		for(Vertex v : src.getVertices(Direction.OUT)) {
			if(!v.getId().toString().startsWith(prefix)) {
				String edgeId = lastVertex.getId() + RELATION_CONNECTOR + v.getId(); 
				if(this.getEdge(edgeId) == null) {
					this.addEdge(edgeId, lastVertex, v, "");
				}
			} else {
				rewire(v, prefix, lastVertex);
			}
		}
	}
	
	public OdnGraph(String graphFilePath, String prefix, double selfVul, double propagateVul) {
		this(graphFilePath, selfVul, propagateVul);
		log.debug("Loading Sub ODN from file [" + graphFilePath + "] with prefix [" + prefix + "]...");
		LinkedList<Vertex> removeList = new LinkedList<Vertex>();
		LinkedList<Vertex> reserveList = new LinkedList<Vertex>();
		log.debug("Checking each vertex in original ODN if they start with prefix [" + prefix + "]...");
		for(Vertex v : this.getVertices()) {
			if(!v.getId().toString().startsWith(prefix)) {
				removeList.add(v);
			} else {
				reserveList.add(v);
				log.debug(v.getId());
			}
		}
		log.debug("Finished checking prefix [" + prefix + "]. Found [" + removeList.size() + "] vertices. Removing them...");
		for(Vertex vertexToReserve : reserveList) {
			rewire(vertexToReserve, prefix, vertexToReserve);
		}
		for(Vertex vertexToRemove : removeList) {
			this.removeVertex(vertexToRemove);
		}
		log.debug("Complete. [" + removeList.size() + "] vertices removed. Now removing duplicate edges...");
		// find duplicated edges and add them to a list
		HashSet<String> existingEdges = new HashSet<String>();
		LinkedList<Edge> edgesToRemove = new LinkedList<Edge>();
		for(Edge currEdge : this.getEdges()) {
			String relationStrIn = currEdge.getVertex(Direction.IN).getId() + "  -->  " + currEdge.getVertex(Direction.OUT).getId();
			String relationStrOut = currEdge.getVertex(Direction.OUT).getId() + "  -->  " + currEdge.getVertex(Direction.IN).getId();
			if(existingEdges.contains(relationStrIn) || existingEdges.contains(relationStrOut)) {
				edgesToRemove.add(currEdge);
			} else {
				existingEdges.add(relationStrIn);
			}
		}
		// remove duplicated edges
		for(Edge edgeToRemove : edgesToRemove) {
			this.removeEdge(edgeToRemove);
		}
		log.debug("Complete. [" + edgesToRemove.size() + "] duplicate edges removed.");
	}
	
	public void saveToGraphml(String savePath) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(savePath);
			GraphMLWriter.outputGraph(this, fos);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public double getVulnerability(String objectId) {
		double vul = -1.0;
		Vertex dst = this.getVertex(objectId);
		if(dst != null) {
			vul = 0.0;
			log.debug("Calculating the vulnerability of Object [" + objectId +"]...");
			for(Vertex src : this.getVertices()) {
				if(!src.equals(dst)) {
					VertexPair vp = new VertexPair(this, src, dst);
					vul += vp.getVulnerability();
				}
			}
			log.debug("Done. Vulnerability of Object [" + objectId +"] is " + String.format("%.3f", vul) +".");
		}
		return vul;
	}
	
	public void printAllVulnerabilities() {
		log.debug("Starting to print all vulnerabilities of the ODN...");
		for(Vertex v : this.getVertices()) {
			double vul = getVulnerability(v.getId().toString());
			v.setProperty(OVERALL_VUL_KEY, vul);
		}
		log.debug("Done calculating all vulnerabilities of the ODN.");
		
	}
	
	public void calculateAllVulnerabilities() {
		log.debug("Starting to calculate all vulnerabilities of the ODN...");
		for(Vertex v : this.getVertices()) {
			log.debug("Overall vulnerability of [" + v.getId() + "] is ["+ String.format("%.3f", v.getProperty(OVERALL_VUL_KEY)) +"].");
		}
		log.debug("All vulnerabilities have been printed.");
	}
	
	public static void main(String[] args) {
		OdnGraph graph = new OdnGraph("odn.graphml", "com.even.trendcraw", 0.1, 0.2);
		graph.calculateAllVulnerabilities();
		graph.printAllVulnerabilities();
		graph.saveToGraphml("odn_inner.graphml");
		//Vertex start = graph.getVertex("com.even.trendcraw.GoogleTrendsDataPull@954049115");
		//Vertex end = graph.getVertex("com.even.trendcraw.MySqlConnection@771153740");
		//VertexPair vp = new VertexPair(graph, start, end);
		//log.debug(vp.getVulnerability());
	}
}
