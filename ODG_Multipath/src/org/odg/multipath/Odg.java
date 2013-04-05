package org.odg.multipath;

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
		OdgRelation rel1 = new OdgRelation(odg, 0.2);
		OdgRelation rel2 = new OdgRelation(odg, 0.3);
		OdgRelation rel3 = new OdgRelation(odg, 0.3);
		odg.addEdge(obj1, obj2, rel1);
		odg.addEdge(obj2, obj3, rel2);
		odg.addEdge(obj1, obj3, rel3);
		
		PropagatedVul vul = new PropagatedVul(odg, obj1, obj3);
		System.out.println("Upper bound: " + vul.getPropagatedVulUpperBound());
		System.out.println("Lower bound: " + vul.getPropagatedVulLowerBound());
	}
}
