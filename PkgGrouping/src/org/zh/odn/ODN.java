package org.zh.odn;

import java.io.FileInputStream;
import java.io.IOException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

public class ODN {

	/**
	 * Get the graph instance of ODN according to
	 * specified GraphML file.
	 * @param graphFilePath Input GraphML file
	 * @return Graph instance of ODNs
	 */
	public static Graph getODN(String graphFilePath) {
		Graph graph = new TinkerGraph();
		try {
			GraphMLReader.inputGraph(graph, new FileInputStream(graphFilePath));
		} catch (IOException e) {
			Log.e(e);
			graph = null;
		}
		return graph;
	}
}
