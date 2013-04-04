package org.odg.multipath;

import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class VulSolver {
	
	private ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph;
	
	
	public VulSolver(ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph, ) {
		this.graph = graph;
		// find all paths between the two objects
		KShortestPaths<OdgObject, OdgRelation> kpathsFinder = new KShortestPaths<OdgObject, OdgRelation>(graph, startObj, MAX_PATH_NUM);
		kpaths = kpathsFinder.getPaths(endObj);
	}
	
	public void getMinCut() {
		StoerWagnerMinimumCut<OdgObject, OdgRelation> cut = new StoerWagnerMinimumCut<OdgObject, OdgRelation>(graph);
		Set<OdgObject> cutVtx = cut.minCut();
		for(OdgObject v : cutVtx) {
			System.out.println("Min cut vertices: " + v.getName());
		}
	}
	
	public static List<GraphPath<OdgObject, OdgRelation>> getMinPathSets(
			ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph,
			OdgObject startObj,
			OdgObject endObj)
	{
		
		return sp.getPaths(endObj);
	}
	
	
	
	
	
}
