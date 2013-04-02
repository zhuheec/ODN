package org.odg.multipath;

import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class VulSolver {
	
	private ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph;
	
	public VulSolver(ListenableUndirectedWeightedGraph<OdgObject, OdgRelation> graph) {
		this.graph = graph;
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
		KShortestPaths<OdgObject, OdgRelation> sp = new KShortestPaths<OdgObject, OdgRelation>(graph, startObj, 10);
		return sp.getPaths(endObj);
	}
	
	public double getPropagatedVulUpperBound(OdgObject startObj, OdgObject endObj) {
		return 0;
	}
	
}
