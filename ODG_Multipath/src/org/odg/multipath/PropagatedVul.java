package org.odg.multipath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class PropagatedVul {
	
	public static final int MAX_PATH_NUM = 10;
	
	private List<GraphPath<OdgObject, OdgRelation>> kpaths;
	private ArrayList<HashSet<OdgRelation>> cutList;
	
	public PropagatedVul(ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph,
			OdgObject startObj, OdgObject endObj) {
		//initialize cut list
		cutList = new ArrayList<HashSet<OdgRelation>>();
		// find all paths between the two objects
		KShortestPaths<OdgObject, OdgRelation> kpathsFinder = new KShortestPaths<OdgObject, OdgRelation>(graph, startObj, MAX_PATH_NUM);
		kpaths = kpathsFinder.getPaths(endObj);
		System.out.println("There are " + kpaths.size() + " paths from [" + startObj.getName() + "] to [" + endObj.getName() + "].");
		// calculate all cuts and save them to the cut list
		findPath(0, null);
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
		double propagatedVul = 1.0;
		for(HashSet<OdgRelation> cut : cutList) {
			double cutVul = 1.0;
			for(OdgRelation edge : cut) {
				cutVul *= 1 - edge.getPropagatedVul();
			}
			cutVul = 1 - cutVul;
			propagatedVul *= cutVul;
		}
		return propagatedVul;
	}
	
	/**
	 * A recursive function that finds all paths between the source and destination object
	 * @param currentPathIndex The index of current path from 0 to total number of path - 1
	 * @param currentCut set of edges in current cut. Once a cut is found, it will be added to this set 
	 */
	private void findPath(int currentPathIndex, HashSet<OdgRelation> currentCut) {
		// if current path index is not the last, get the current and the next path
		GraphPath<OdgObject, OdgRelation> currentPath = kpaths.get(currentPathIndex);
		// create an array to record if each edge has been visited
		boolean[] isEdgeVisited = new boolean[currentPath.getEdgeList().size()];
		// get all edges for the current path
		List<OdgRelation> edges = currentPath.getEdgeList();
		// traverse the edges in the path
		for(int j = 0; j < edges.size(); j++) {
			// only check edges not visited
			if(!isEdgeVisited[j]) {
				// create a cut instance for a new cut 
				if(currentCut == null) {
					currentCut = new HashSet<OdgRelation>();
				}
				// add current edge to current cut
				currentCut.add(edges.get(j));
				// set the state of the edge to visited
				isEdgeVisited[j] = true;
				// if reaches the last path, add this cut to the cut list
				if(currentPathIndex == kpaths.size() - 1) {
					// reach the end of the paths list, add the path to set
					cutList.add(new HashSet<OdgRelation>(currentCut));
				} else {
					findPath(currentPathIndex + 1, currentCut);
				}
				currentCut.remove(edges.get(j));
			}
		}
	}
}
