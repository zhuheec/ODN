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
	public static final String RELATION_VUL_KEY = "vulnerability";
	public static final String RELATION_CONNECTOR = "-->";
	private static final Logger log = Logger.getLogger(OdnGraph.class);
	static { log.setLevel(Level.DEBUG); }
	
	/**
	 * Instantiate of ODN according to specified GraphML file.
	 * @param graphFilePath Input GraphML file
	 */
	public OdnGraph(String graphFilePath) {
		log.debug("Loading ODN from file [" + graphFilePath + "]...");
		try {
			// read the initial ODN from file
			GraphMLReader.inputGraph(this, new FileInputStream(graphFilePath));
			log.debug("File data loaded. Now preparing vertices...");
			// set vertex name
			int vcount = 0;
			for(Vertex vertex : this.getVertices()) {
				vertex.setProperty(CLASS_NAME_KEY, vertex.getId());
				vcount++;
			}
			log.debug("All [" + vcount + "] vertices are ready. Now preparing edges...");
			// set edge name
			int ecount = 0;
			for(Edge edge : this.getEdges()) {
				String relationName = edge.getVertex(Direction.OUT).getId().toString() + RELATION_CONNECTOR
						+ edge.getVertex(Direction.IN).getId().toString();
				edge.setProperty(RELATION_NAME_KEY, relationName);
				// TODO: calculate vulnerability of edges and vertices
				edge.setProperty(RELATION_VUL_KEY, 0.5);
				ecount++;
			}
			log.debug("All [" + ecount + "] edges are ready. Finished loading ODN.");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public OdnGraph(String graphFilePath, String prefix) {
		this(graphFilePath);
		log.debug("Loading Sub ODN from file [" + graphFilePath + "] with prefix [" + prefix + "]...");
		LinkedList<Vertex> removeList = new LinkedList<Vertex>();
		log.debug("Checking each vertex in original ODN if they start with prefix [" + prefix + "]...");
		for(Vertex v : this.getVertices()) {
			if(!v.getId().toString().startsWith(prefix)) {
				removeList.add(v);
			} else {
				log.debug(v.getId());
			}
		}
		log.debug("Finished checking prefix [" + prefix + "]. Found [" + removeList.size() + "] vertices. Removing them...");
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
	
	public static void main(String[] args) {
		Graph graph = new OdnGraph("instagram_class.graphml", "com.instagram.android.activity");
		Vertex start = graph.getVertex("com.instagram.android.activity.TumblrAuthActivity");
		Vertex end = graph.getVertex("com.instagram.android.activity.XAuthActivity");
		VertexPair vp = new VertexPair(graph, start, end);
		log.debug(vp.getVulnerability());
	}
}