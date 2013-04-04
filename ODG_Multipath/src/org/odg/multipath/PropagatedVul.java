package org.odg.multipath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class PropagatedVul {
	
	public static final int MAX_PATH_NUM = 10;
	List<GraphPath<OdgObject, OdgRelation>> kpaths;
	
	private ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph;
	
	public PropagatedVul(ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph,
			OdgObject startObj, OdgObject endObj) {
		this.graph = graph;
		// find all paths between the two objects
		KShortestPaths<OdgObject, OdgRelation> kpathsFinder = new KShortestPaths<OdgObject, OdgRelation>(graph, startObj, MAX_PATH_NUM);
		kpaths = kpathsFinder.getPaths(endObj);
	}
	/**
	 * Calculates the upper bound of the propagated vulnerability between two objects.
	 * It first finds all paths between the two objects. If there are two many paths,
	 * then it only takes the MAX_PATH_NUM shortest paths in consideration.
	 * @param startObj The starting object
	 * @param endObj The ending object
	 * @return the propagated vulnerability between the two objects
	 */
	public double getPropagatedVulUpperBound() {
		// start to calculate propagated vulnerability with the initial value 1
		double propagatedVul = 1.0;
		// for each path
		for(GraphPath<OdgObject, OdgRelation> path : kpaths) {
			double pathPropagationProb = 1.0;
			// calculate the probability of propagation for this path
			for(OdgRelation edge : path.getEdgeList()) {
				pathPropagationProb *= edge.getPropagatedVul();
			}
			// get probability of the failure of the propagation for the path
			pathPropagationProb = 1 - pathPropagationProb;
			// multiply the all probability of failure of propagation together
			propagatedVul *= pathPropagationProb;
		}
		// the upper bound is the probability of this case (none of the paths propagates) not happening
		propagatedVul = 1 - propagatedVul;
		return propagatedVul;
	}
	
	/**
	 * Calculates the lower bound of the propagated vulnerability between two objects.
	 * It first finds all paths between the two objects. Then it finds all cases of 
	 * @param startObj
	 * @param endObj
	 * @return
	 */
	public double getPropagatedVulLowerBound() {
		boolean[] isPathFail = new boolean[kpaths.size()];
		
		boolean[][] tried = new boolean[10][10];
		
		for(int i = 0; i < kpaths.size(); i++) {
			GraphPath<OdgObject, OdgRelation> path = kpaths.get(i);
			HashSet<OdgRelation> cut = new HashSet<OdgRelation>();
			List<OdgRelation> edges = path.getEdgeList();
			for(int j = 0; j < edges.size(); j++) {
				if(!tried[i][j]) {
					cut.add(edges.get(j));
					tried[i][j] = true;
					break;
				}
			}
		}
		return 0;
	}
}
