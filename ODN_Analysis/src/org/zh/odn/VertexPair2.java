package org.zh.odn;

import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.zh.poly.Poly;
import org.zh.poly.Term;
import org.zh.poly.Variable;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class VertexPair2 {

	private static final Logger log = Logger.getLogger(VertexPair.class);
	
	static { log.setLevel(Level.DEBUG); }
	
	private Graph graph = null;
	private String startTag = "";
	private String endTag = "";
	
	
	private LinkedList<LinkedList<Vertex>> allPaths;
	private Graph pathTree;
	private int pathCount = 0;
	//private double startSelfVul = 1.0;
	
	
	/**
	 * Create an instance for vulnerability calculation between two nodes.
	 * @param graph The graph where the two nodes are
	 * @param startVertex The node to start
	 * @param endVertex The node to end
	 */
	public VertexPair2(Graph graph, Vertex startVertex, Vertex endVertex) {
		// set graph to calculate
		this.graph = graph;
		log.debug("[Graph] has been assigned to calculate vulnerability.");
		
		// set start vertex
		this.startTag = startVertex.getId().toString();
		// set self vulnerability of start vertex
		//startSelfVul = Double.parseDouble(startVertex.getProperty(OdnGraph.SELF_VUL_KEY).toString());
		//log.debug("[StartTag] has been set to ["+ startTag +"].");
		
		// set end vertex
		this.endTag = endVertex.getId().toString();
		log.debug("[EndTag] has been set to ["+ endTag +"].");
		
		// initialize path tree
		pathTree = new TinkerGraph();
		pathTree.addVertex(startVertex.getId());
		log.debug("[PathTree] has been initialized with ["+ startVertex.getId() +"] as root.");
		
		// initialize the list of all path
		allPaths = new LinkedList<LinkedList<Vertex>>();
		log.debug("[AllPathsList] has been initialized.");
		
		// initialize visited paths list
		LinkedList<Vertex> visited = new LinkedList<Vertex>();
		visited.add(startVertex);
		log.debug("[VisitedPathsList] has been initialized with start vertex [" + startVertex.getId() + "]");
		
		// recursively perform DFS
		log.debug("Starting to perform DFS recursively...");
		depthFirst(visited);
		log.debug("DFS done. Paths are ready for view and [PathTree] is also ready.");
	}
	
	/**
	 * Convert an iterable object to a linked list
	 * @param iterable An iterable object
	 * @return A linked list
	 */
	private <T> LinkedList<T> toLinkedList(Iterable<T> iterable) {
		LinkedList<T> list = new LinkedList<T>();
		for (T t : iterable) {
	        list.add(t);
	    }
	    return list;
	}

	/**
	 * Perform depth-first search for paths from source to destination
	 * @param visitedVertices The list of currently visited nodes
	 */
	private void depthFirst(LinkedList<Vertex> visited) {
		// get all neighbors of the last node in current path
		LinkedList<Vertex> vertices = this.toLinkedList(visited.getLast().getVertices(Direction.BOTH));
		// remove visited neighbors
		// examine adjacent nodes
		for (Vertex v : vertices) {
			// do not re-visit a node
			if (!visited.contains(v)) {
				// append current vertex to visited list
				visited.addLast(v);
				if(v.getId().equals(endTag)) {
					// add path to path list if reached the end node
					addPath(new LinkedList<Vertex>(visited));
				} else {
					// recursively call depth-first algorithm
					depthFirst(visited);
				}
				// backtracking
				visited.removeLast();
			}
		}
	}
	
	/**
	 * Add path to path list, and add it to the path tree
	 * @param visited An applicable path
	 */
	private void addPath(LinkedList<Vertex> visited) {
		allPaths.add(visited); // add path to path list
		pathCount++; // unique ID for current path, used to differentiate vertex 
		Vertex treeNode = pathTree.getVertex(startTag); // root node for path tree
		int index = 1;
		while(index < visited.size()) {
			Vertex current = visited.get(index); 
			boolean isChild = false;
			// do not add duplicate vertex
			for(Vertex neighbor : treeNode.getVertices(Direction.OUT)) {
				// check if current node exists in the tree path
				if(neighbor.getProperty(OdnGraph.CLASS_NAME_KEY).equals(current.getProperty(OdnGraph.CLASS_NAME_KEY))) {
					isChild = true;
					// change vertex ID in allPaths list to be consistent with tree
					visited.set(index, neighbor);
					// continue to check next vertex in this path
					treeNode = neighbor;
					break;
				}
			}
			// add path in the tree only when they are not duplicate
			if(!isChild) {
				Vertex newNode = pathTree.addVertex(current.getProperty(OdnGraph.CLASS_NAME_KEY).toString()
						+ ":" + pathCount);
				newNode.setProperty(OdnGraph.CLASS_NAME_KEY, current.getProperty(OdnGraph.CLASS_NAME_KEY));
				pathTree.addEdge(treeNode.getId() + OdnGraph.RELATION_CONNECTOR + newNode.getId(), 
						treeNode, newNode, "");
				treeNode = newNode;
				visited.set(index, newNode);
			}
			index++;
		}
	}
	
	/**
	 * Print the path tree starting from a vertex
	 * to the end node of this instance
	 * @param vertex The vertex to start
	 */
	private void printPathTree(Vertex vertex) {
		// check if vertex has children
		if(vertex.getVertices(Direction.OUT).iterator().hasNext()) {
			StringBuffer output = new StringBuffer("[" + vertex.getId() + "] has children: ");
			for(Vertex v : vertex.getVertices(Direction.OUT)) {
				output.append("[" + v.getId() + "], ");
			}
			// print the children of vertex
			log.debug(output.substring(0, output.length() - 2));
			// recursively print children of vertex's children
			for(Vertex v : vertex.getVertices(Direction.OUT)) {
				printPathTree(v);
			}
		}
	}

	/**
	 * Print all paths from start vertex to end vertex
	 */
	public void printPaths() {
		log.debug("Starting  to print all [" + allPaths.size() + "] paths of the path tree...");
		for(int i = 0; i < allPaths.size(); i++) {
			LinkedList<Vertex> path = allPaths.get(i);
			StringBuffer output = new StringBuffer("Path[" + i + "]: ");
			for (Vertex node : path) {
				output.append("[" + node.getId() + "]");
				if(node != path.getLast()) {
					output.append(OdnGraph.RELATION_CONNECTOR);
				}
			}
			log.debug(output);
		}
		log.debug("Finished printing all [" + allPaths.size() + "] paths.");
	}
	
	/**
	 * Updated on Jan 24, 2013 by He Zhu
	 * I really hope this is the last time to review this.
	 */
	public double getVulnerability() {
		log.debug("Starting to calculate vul...");
		double vul = 0.0;
		for(int i = 0; i < allPaths.size(); i++) {
			Poly poly = new Poly();
			for(int j = 0; j <= i; j++) {
				LinkedList<Vertex> path = allPaths.get(j);
				Term term1 = new Term();
				Term term2 = new Term();
				for(int k = 0; k < path.size() - 1; k++) {
					term2.addVar(Variable.getVariable(
							"[" + path.get(k).getId().toString().split(":")[0] + "]"
							+ OdnGraph.RELATION_CONNECTOR
							+ path.get(k + 1).getId().toString().split(":")[0] + "]"));
				}
				
				if(j == i) {
					poly.addPoly(new Poly(term2));
				} else {
					term2.setCoefficient(-1);
					poly.addPoly(new Poly(term1, term2));
				}	
			}
			// accumulate the vul of each poly here
			for(Term t : poly.getTerms()) {
				double termVul = t.getCoefficient();
				for(Variable var: t.getVars()) {
					termVul *= getVarVul(var);
				}
				vul += termVul;
			}
		}
		return vul;
	}
	
	private double getVarVul(Variable var) {
		double vul = 0.0;
		Edge edge = graph.getEdge(var.getName());
		if(edge != null) {
			vul = Double.parseDouble(edge.getProperty(OdnGraph.RELATION_VUL_KEY).toString());
		}
		return vul;
	}
	
	/**
	 * Print the path tree.
	 */
	public void printPathTree() {
		log.debug("Starting to print the [PathTree] from [" + startTag + "] to [" + endTag + "]...");
		// print tree from the root vertex
		printPathTree(pathTree.getVertex(startTag));
		log.debug("Finshed printing the [PathTree].");
	}
	
	public static void main(String[] args) {
		// this graph is directional
		Graph graph = new TinkerGraph();
		Vertex va = graph.addVertex("A");
		va.setProperty(OdnGraph.CLASS_NAME_KEY, "A");
		Vertex vb = graph.addVertex("B");
		vb.setProperty(OdnGraph.CLASS_NAME_KEY, "B");
		Vertex vc = graph.addVertex("C");
		vc.setProperty(OdnGraph.CLASS_NAME_KEY, "C");
		Vertex vd = graph.addVertex("D");
		vd.setProperty(OdnGraph.CLASS_NAME_KEY, "D");
		Vertex ve = graph.addVertex("E");
		ve.setProperty(OdnGraph.CLASS_NAME_KEY, "E");
		Vertex vf = graph.addVertex("F");
		vf.setProperty(OdnGraph.CLASS_NAME_KEY, "F");
		
		Edge ab = graph.addEdge(1, va, vb, "A-->B");
		ab.setProperty(OdnGraph.RELATION_NAME_KEY, "A-->B");
		ab.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge ac = graph.addEdge(2, va, vc, "A-->C");
		ac.setProperty(OdnGraph.RELATION_NAME_KEY, "A-->C");
		ac.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge bd = graph.addEdge(3, vb, vd, "B-->D");
		bd.setProperty(OdnGraph.RELATION_NAME_KEY, "B-->D");
		bd.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge be = graph.addEdge(4, vb, ve, "B-->E");
		be.setProperty(OdnGraph.RELATION_NAME_KEY, "B-->E");
		be.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge bf = graph.addEdge(5, vb, vf, "B-->F");
		bf.setProperty(OdnGraph.RELATION_NAME_KEY, "B-->F");
		bf.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge ce = graph.addEdge(6, vc, ve, "C-->E");
		ce.setProperty(OdnGraph.RELATION_NAME_KEY, "C-->E");
		ce.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge cf = graph.addEdge(7, vc, vf, "C-->F");
		cf.setProperty(OdnGraph.RELATION_NAME_KEY, "C-->F");
		cf.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		Edge ef = graph.addEdge(8, ve, vf, "E-->F");
		ef.setProperty(OdnGraph.RELATION_NAME_KEY, "E-->F");
		ef.setProperty(OdnGraph.RELATION_VUL_KEY, 0.5);
		
		VertexPair2 vul = new VertexPair2(graph, vb, ve);
		vul.printPaths();
	}
}
