package org.zh.odn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class VertexPair {

	private static final Logger log = Logger.getLogger(VertexPair.class);
	
	static { log.setLevel(Level.DEBUG); }
	
	private Graph graph = null;
	private String startTag = "";
	private String endTag = "";
	
	
	private LinkedList<LinkedList<Vertex>> allPaths;
	private Graph pathTree;
	private HashMap<LinkedList<Vertex>, HashSet<String[]>> pathConditionsMap;
	private int pathCount = 0;
	private double startSelfVul = 1.0;
	
	
	/**
	 * Create an instance for vulnerability calculation between two nodes.
	 * @param graph The graph where the two nodes are
	 * @param startVertex The node to start
	 * @param endVertex The node to end
	 */
	public VertexPair(Graph graph, Vertex startVertex, Vertex endVertex) {
		// set graph to calculate
		this.graph = graph;
		log.debug("[Graph] has been assigned to calculate vulnerability.");
		
		// set start vertex
		this.startTag = startVertex.getId().toString();
		// set self vulnerability of start vertex
		startSelfVul = Double.parseDouble(startVertex.getProperty(OdnGraph.SELF_VUL_KEY).toString());
		log.debug("[StartTag] has been set to ["+ startTag +"].");
		
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
		
		// initialize and calculate the map of paths with conditions 
		pathConditionsMap = new HashMap<LinkedList<Vertex>, HashSet<String[]>>();
		log.debug("[PathConditionsMap] has been initialized.");
		calculatePathsWithConditions();
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
	 * List all paths with conditions for each to be chosen.
	 * We do this to make propagation vulnerability for each
	 * path independent, and to make it easier for overall
	 * propagation vulnerability calculation.
	 */
	private void calculatePathsWithConditions() {
		log.debug("Starting to find conditions to make each path in [PathTree] independent...");
		// initialize failed edges linked list
		LinkedList<String[]> failedEdges = new LinkedList<String[]>();
		log.debug("[FailedEdgesList] has been initialized.");
		
		// get maximum path length to consider
		int maxValidPathLen = getMaxValidPathLength(2);
		
		LinkedList<Vertex> prevPath = null;
		for(LinkedList<Vertex> path : allPaths) {
			if(path.size() <= maxValidPathLen) {
				// add and merge the new failed edge
				String[] currFailedEdge = getFailedEdge(prevPath, path);
				if(currFailedEdge != null) {
					addAndMerge(failedEdges, currFailedEdge);
				}
				pathConditionsMap.put(new LinkedList<Vertex>(path), new HashSet<String[]>(failedEdges));
				prevPath = path;
			}
		}
	}
	
	private int getMaxValidPathLength(int offset) {
		// find the minimum path length among all paths
		int minPathLen = Integer.MAX_VALUE;
		for(LinkedList<Vertex> path : allPaths) {
			if(path.size() < minPathLen) {
				minPathLen = path.size();
			}
		}
		// return min length plus offset
		return minPathLen + offset;
	}
	
	/**
	 * Check if the edge to add can cover previous edges in the list.
	 * If so, remove previous duplicate edges.
	 * At last, add the edge.
	 * @param failedEdges The linked list for existing failed edges
	 * @param edge The edge to add
	 */
	private void addAndMerge(LinkedList<String[]> failedEdges, String[] edge) {
		/* due to DFS, the newly added edge cannot be covered by current 
		 * failed edges. Instead, it may cover current failed edges. */
		
		// the root of a sub tree to be checked
		Vertex subTreeStart = pathTree.getVertex(edge[1]);
		LinkedList<String[]> edgesToRemove = new LinkedList<String[]>();
		// check if the sub tree contains any existing failed edge. If so,
		// remove them from failed edges list.
		for(String[] currentEdge : failedEdges) {
			if(isSubTreeContainsEdge(subTreeStart, currentEdge)) {
				edgesToRemove.add(currentEdge);
			}
		}
		failedEdges.removeAll(edgesToRemove);
		// in the end, add the latest edge
		failedEdges.add(edge);
	}
	
	/**
	 * Check if the sub tree with the root start (fist parameter) contains
	 * the specified edge (second parameter)
	 * @param start The start node of the sub tree
	 * @param edge The edge to check
	 * @return Whether or not the sub tree contains the tartget edge.
	 */
	private boolean isSubTreeContainsEdge(Vertex start, String[] edge) {
		for(Vertex v : start.getVertices(Direction.OUT)) {
			if(v.getId().equals(edge[0])) {
				for(Vertex v2 : v.getVertices(Direction.OUT)) {
					if(v2.getId().equals(edge[1])) {
						return true;
					}
				}
			}
			// DFS for target edge
			return isSubTreeContainsEdge(v, edge);
		}
		return false;
	}
	
	/**
	 * Get the edge which must be failed to make attacks along current path 
	 * instead of previous path
	 * @param prevPath Previous path
	 * @param currPath Current path
	 * @return The edge which must be failed
	 */
	private String[] getFailedEdge(LinkedList<Vertex> prevPath, LinkedList<Vertex> currPath) {
		String[] ret = null;
		if(prevPath != null) {
			// get size of possible overlapped vertices
			int idx = prevPath.size() > currPath.size() ? currPath.size() : prevPath.size();
			for(int i = 0; i < idx; i++) {
				// find the branching point of two paths
				if(!prevPath.get(i).getId().equals(currPath.get(i).getId())) {
					// return the first different edge in previous path
					ret = new String[] { prevPath.get(i - 1).getId().toString(),
							prevPath.get(i).getId().toString() };
					break;
				}
			}	
		}
		return ret;
	}
	
	/**
	 * Get the original name of a vertex which does not have extra
	 * index added during vulnerability analysis
	 * @param vertexId The value of getId() of the vertex
	 * @return The original name of a vertex, which is a class name in ODN
	 */
	private String getVertexDisplayName(String vertexId) {
		return (vertexId.indexOf(":") == -1 ? 
				vertexId : vertexId.substring(0, vertexId.indexOf(":")));
	}
	
	/**
	 * Get the edge display name which is the two vertices' original name
	 * separated by "-->"
	 * @param edge The String pair of vertex Id representing the edge
	 * @return Edge display name
	 */
	private String getEdgeDisplayName(String[] edge) {
		String srcVtxName = getVertexDisplayName(edge[0]);
		String destVtxName = getVertexDisplayName(edge[1]);
		String edgeId = srcVtxName + OdnGraph.RELATION_CONNECTOR + destVtxName;
		return edgeId;
	}
	
	/**
	 * Get the propagation vulnerability of an edge from the graph
	 * @param edge The String pair of vertex Id representing the edge
	 * @return Propagation vulnerability of the edge
	 */
	private double getEdgeVulnerability(String[] edge) {
		double vul = -1;
		// Try either way: A-->B
		Iterator<Edge> it = graph.getEdges(OdnGraph.RELATION_NAME_KEY, 
				getEdgeDisplayName(edge)).iterator();
		Edge e = null;
		if(it.hasNext()) {
			e = it.next();
		} else {
			// Try either way: B-->A
			it = graph.getEdges(OdnGraph.RELATION_NAME_KEY, 
					getEdgeDisplayName(new String[] {edge[1], edge[0]})).iterator();
			if(it.hasNext()) {
				e = it.next();
			}
		}
		if(e != null) {
			vul = Double.parseDouble(e.getProperty(OdnGraph.RELATION_VUL_KEY).toString());
		}
		// return a negative value if edge does not exist
		return vul;
	}
	
	/**
	 * Get the propagation vulnerability of the vertex pair.
	 * @return Propagation vulnerability
	 */
	public double getVulnerability() {
		double vul = 0.0;
		// for each path, calculate vulnerability
		for(LinkedList<Vertex> path : pathConditionsMap.keySet()) {
			double pathVul = 1.0;
			// (1) calculate propagation vulnerability of this path
			for(int i = 0; i < path.size() - 1; i++) {
				String[] edge = { path.get(i).getId().toString(), 
						path.get(i + 1).getId().toString() };
				pathVul *= getEdgeVulnerability(edge);
			}
			// (2) include conditions of other paths to fail
			for(String[] failedEdge: pathConditionsMap.get(path)) {
				pathVul *= (1 - getEdgeVulnerability(failedEdge));
			}
			// (3) add together values of different paths because the event is independent
			vul += pathVul;
		}
		return (vul * startSelfVul);
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
	 * Print the path tree.
	 */
	public void printPathTree() {
		log.debug("Starting to print the [PathTree] from [" + startTag + "] to [" + endTag + "]...");
		// print tree from the root vertex
		printPathTree(pathTree.getVertex(startTag));
		log.debug("Finshed printing the [PathTree].");
	}
	
	/**
	 * Print all paths from start vertex to end vertex with conditions
	 */
	public void printPathsWithConditions() {
		ArrayList<LinkedList<Vertex>> paths = new ArrayList<LinkedList<Vertex>>(pathConditionsMap.keySet());
		log.debug("Starting to print all [" + paths.size() + "] paths with conditions...");
		for(int i = 0; i < paths.size(); i++) {
			LinkedList<Vertex> path = paths.get(i);
			// (1) print the path
			StringBuffer output = new StringBuffer("Path[" + i + "]: ");
			for (Vertex node : path) {
				output.append("[" + getVertexDisplayName(node.getId().toString()) + "]");
				if(node != path.getLast()) {
					output.append(OdnGraph.RELATION_CONNECTOR);
				}
			}
			// (2) print failed edges
			if(pathConditionsMap.get(path).size() > 0) {
				output.append(". Failed edges: ");
				for(String[] edge: pathConditionsMap.get(path)) {
					output.append("[" + getEdgeDisplayName(edge) + "], ");
				}
				log.debug(output.substring(0, output.length() - 2));
			} else {
				output.append(". No failed edges required.");
				log.debug(output);
			}
		}
		log.debug("Finished printing all [" + paths.size() + "] paths with conditions.");
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
		
		VertexPair vul = new VertexPair(graph, vb, ve);
		vul.printPathsWithConditions();
		System.out.println(vul.getVulnerability());
	}
}
