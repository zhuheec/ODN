package org.zh.odn;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class ODN {

	public static final String CLASS_NAME_KEY = "className";
	public static final String RELATION_NAME_KEY = "relationName";
	public static final String RELATION_VUL_KEY = "vulnerability";
	public static final String RELATION_CONNECTOR = "-->";
	
	private static final Logger log = Logger.getLogger(ODN.class);
	
	static { log.setLevel(Level.DEBUG); }
	
	/**
	 * Get the graph instance of ODN according to
	 * specified GraphML file.
	 * @param graphFilePath Input GraphML file
	 * @return Graph instance of ODNs
	 */
	public static Graph getODN(String graphFilePath) {
		log.debug("Loading ODN from file [" + graphFilePath + "]...");
		Graph graph = new TinkerGraph();
		try {
			// read the initial ODN from file
			GraphMLReader.inputGraph(graph, new FileInputStream(graphFilePath));
			log.debug("File data loaded. Now preparing vertices...");
			// set vertex name
			int vcount = 0;
			for(Vertex vertex : graph.getVertices()) {
				vertex.setProperty(CLASS_NAME_KEY, vertex.getId());
				vcount++;
			}
			log.debug("All [" + vcount + "] vertices are ready. Now preparing edges...");
			// set edge name
			int ecount = 0;
			for(Edge edge : graph.getEdges()) {
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
			graph = null;
		}
		return graph;
	}
	
	public static void saveToGraphml(Graph graph, String savePath) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(savePath);
			GraphMLWriter.outputGraph(graph, fos);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Graph graph = getODN("instagram_class.graphml");
	}
}
