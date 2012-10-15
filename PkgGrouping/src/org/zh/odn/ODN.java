package org.zh.odn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

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
	
	public static void saveToGraphml(Graph graph, String savePath) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(savePath);
			GraphMLWriter.outputGraph(graph, fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
