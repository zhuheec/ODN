package org.odg.multipath;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

public class Odg extends ListenableUndirectedWeightedGraph<OdgObject,OdgRelation> {

	private static final long serialVersionUID = 202762474932879914L;

	public Odg() {
		super(OdgRelation.class);
	}
	
	public static void main(String[] args) {
		Odg odg = new Odg();
		OdgObject obj1 = new OdgObject("a.b.Cls1", 0.2);
		OdgObject obj2 = new OdgObject("a.b.Cls2", 0.3);
		OdgObject obj3 = new OdgObject("a.b.Cls3", 0.4);
		odg.addVertex(obj1);
		odg.addVertex(obj2);
		odg.addVertex(obj3);
		OdgRelation rel1 = new OdgRelation(odg, 0.3);
		OdgRelation rel2 = new OdgRelation(odg, 0.3);
		OdgRelation rel3 = new OdgRelation(odg, 0.3);
		odg.addEdge(obj1, obj2, rel1);
		odg.addEdge(obj2, obj3, rel2);
		odg.addEdge(obj1, obj3, rel3);
		int deg = odg.degreeOf(obj1);
		System.out.println("Degree of obj1 is: " + deg);
		VulSolver solver = new VulSolver(odg);
		solver.getMinCut();
		
		// get min path sets
		List<GraphPath<OdgObject, OdgRelation>> paths = VulSolver.getMinPathSets(odg, obj1, obj3);
		for(GraphPath<OdgObject, OdgRelation> path : paths) {
			System.out.print("Current path: " + path.getStartVertex().getName());
			for(OdgRelation rel : path.getEdgeList()) {
				System.out.print(" --> " + odg.getEdgeTarget(rel).getName());
			}
			System.out.println();
		}
	}
}
