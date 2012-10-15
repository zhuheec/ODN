package org.zh.odn;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;


public class ComplexNetwork {
	
	private Graph graph;
	
	public ComplexNetwork(String graphFilePath) {
		graph = new TinkerGraph();
		try {
			GraphMLReader.inputGraph(graph, new FileInputStream(graphFilePath));
		} catch (IOException e) {
			Log.e(e);
		}
		// initiate security score
		initSecurityScore();
		// get node list
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			// get current node
			Vertex v = it.next();
			// compute node degree
			v.setProperty(VertexProperty.IN_DEGREE, getDegree(v, Direction.IN));
			v.setProperty(VertexProperty.OUT_DEGREE, getDegree(v, Direction.OUT));
			//Log.d(v.getId().toString() + "\t" + v.getProperty(VertexProperty.IN_DEGREE) + "\t" + 
			//		v.getProperty(VertexProperty.OUT_DEGREE) + "\t" + v.getProperty(VertexProperty.SECURITY_SCORE));
		}
	}
	
	/**
	 * Save the network into a graphml file
	 */
	public void saveAsFile(String graphFilePath) {
		try {
			GraphMLWriter.outputGraph(graph, new FileOutputStream(graphFilePath));
		} catch (IOException e) {
			Log.e(e);
		}
	}
	
	/**
	 * Get the inbound or outbound degree of this vertex
	 * @param v target vertex
	 * @param direction the direction of the edge
	 * @return the degree of target vertex
	 */
	public int getDegree(Vertex v, Direction direction) {
		int degree = 0;
		Iterator<Edge> it = v.getEdges(direction).iterator();
		
		while(it.hasNext()) {
			++degree;
			it.next();
		}
		return degree;
	}
	
	/**
	 * Compute the security score of one vertex.
	 * (1) Initially all the nodes has zero security score.
	 * (2) Then we find nodes which are considered as
	 * security-intensive, and call them "source nodes". They are given scores according to statistical
	 * reference.
	 * (3) The inbound neighbors are given the same score as the source nodes. The score can be accumulated
	 * if there are more than one score given. Starting from every source node, it forms a tree with the
	 * source node as the root.
	 * @param v
	 */
	public void computeDescendentsSecurityScore(Vertex v) {
		v.setProperty(VertexProperty.IS_VISITED, true);
		int parentSecurityScore = (int) v.getProperty(VertexProperty.SECURITY_SCORE);
		// get inbound neighbors list
		Iterator<Vertex> it = v.getVertices(Direction.IN).iterator();
		while(it.hasNext()) {
			Vertex neighbor = it.next();
			boolean isSource = (boolean) neighbor.getProperty(VertexProperty.IS_SOURCE);
			boolean isVisited = (boolean) neighbor.getProperty(VertexProperty.IS_VISITED);
			int securityScore = (int) neighbor.getProperty(VertexProperty.SECURITY_SCORE);
			if(!isSource) {
				neighbor.setProperty(VertexProperty.SECURITY_SCORE, securityScore + parentSecurityScore);
				if(!isVisited) {
					computeDescendentsSecurityScore(neighbor);
				}
			}
		}
	}
	
	public void writeDegreeStat(String statFilePath) {
		// hash table to store the degree count
		TreeMap<Integer, Integer> inDegreeMap = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> outDegreeMap = new TreeMap<Integer, Integer>();
		// get node list
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			// get current node
			Vertex v = it.next();
			// get inbound degree
			int in_degree = (int) v.getProperty(VertexProperty.IN_DEGREE);
			if(inDegreeMap.containsKey(in_degree)) {
				int in_count = inDegreeMap.get(in_degree) + 1;
				inDegreeMap.put(in_degree, in_count);
			} else {
				inDegreeMap.put(in_degree, 1);
			}
			// get outbound degree
			int out_degree = (int) v.getProperty(VertexProperty.OUT_DEGREE);
			if(outDegreeMap.containsKey(out_degree)) {
				int out_count = outDegreeMap.get(out_degree) + 1;
				outDegreeMap.put(out_degree, out_count);
			} else {
				outDegreeMap.put(out_degree, 1);
			}
		}
		// write map to file
		try {
			FileWriter fileWriter = new FileWriter(statFilePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			// write inbound degree stat
			bufferedWriter.write("---------- INBOUND  DEGREE STAT ----------");
			bufferedWriter.newLine();
			Iterator<Integer> initr = inDegreeMap.keySet().iterator();
			while(initr.hasNext()) {
				int indeg = initr.next();
				bufferedWriter.write(indeg + "\t" + inDegreeMap.get(indeg));
				bufferedWriter.newLine();
			}
			// write outbound degree stat
			bufferedWriter.write("---------- OUTBOUND DEGREE STAT ----------");
			bufferedWriter.newLine();
			Iterator<Integer> outitr = outDegreeMap.keySet().iterator();
			while(outitr.hasNext()) {
				int outdeg = outitr.next();
				bufferedWriter.write(outdeg + "\t" + outDegreeMap.get(outdeg));
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			Log.e(e);
		}
	}
	
	private void initSecurityScore() {
		// get node list
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			// get current node
			Vertex v1 = it.next();
			// set scores for source nodes
			boolean isSource;
			int securityScore;
			if(v1.getId().toString().toLowerCase().contains("security")) {
				isSource = true;
				securityScore = 1;
			} else {
				isSource = false;
				securityScore = 0;
			}
			v1.setProperty(VertexProperty.IS_SOURCE, isSource);
			v1.setProperty(VertexProperty.SECURITY_SCORE, securityScore);
			v1.setProperty(VertexProperty.IS_VISITED, false);
		}
			
		// compute non-source security scores recursively
		it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v2 = it.next();
			computeDescendentsSecurityScore(v2);
		}
	}
	
	public double getClusteringCoeff(Vertex v) {
		double neighborEdgeCount = 0;
		double neighborCount = 0;
		Iterator<Vertex> it = v.getVertices(Direction.BOTH).iterator();
		HashSet<Vertex> neighbors = new HashSet<Vertex>();
		// build the neighbors set
		while(it.hasNext()) {
			neighborCount++;
			neighbors.add(it.next());
		}
		for(Vertex nbr : neighbors) {
			Iterator<Vertex> itnbr = nbr.getVertices(Direction.BOTH).iterator();
			while(itnbr.hasNext()) {
				if(neighbors.contains(itnbr.next())) {
					neighborEdgeCount++;
				}
			}
		}
		return neighborEdgeCount / (neighborCount * (neighborCount + 1));
	}
	
	public double getClusteringCoeff() {
		int vertexCount = 0;
		double coeff = 0;
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			vertexCount ++;
			coeff += getClusteringCoeff(it.next());
		}
		return coeff / vertexCount;
	}
	
	public static void main(String[] Args) {
		ComplexNetwork cn = new ComplexNetwork("instagram_class.graphml");
	}
}
