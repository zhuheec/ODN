package org.zh.odn;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class PackageGroup {
	
	private Hashtable<String, Integer> pkgTable;
	private Graph graph;
	private int clsCount;
	
	public PackageGroup(String path) {
		graph = ODN.getODN(path);
		pkgTable = new Hashtable<String, Integer>();
	}
	
	public void groupByName(int prefixCount) {
		pkgTable.clear();
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
			clsCount++;
			String[] clsName = v.getId().toString().split("\\.");
			String pkgName = "";
			if(clsName.length >= prefixCount + 1) {
				for(int i = 0; i < prefixCount; i++) {
					pkgName += clsName[i] + ".";
				}
			} 
			if(pkgName.length() > 1) {
				pkgName = pkgName.substring(0, pkgName.length() - 1);
				Integer count = pkgTable.get(pkgName);
				if(count == null) {
					pkgTable.put(pkgName, 1);
				} else {
					pkgTable.put(pkgName, ++count);
				}
			}
		}
	}
	
	public void removeNodesWithoutPrefix(String prefix) {
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
			v.setProperty("name", v.getId());
			if(!v.getId().toString().startsWith(prefix)) {
				graph.removeVertex(v);
			}
		}
		removeDuplicateRelations();
	}
	
	public void printClasses() {
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
			System.out.println(v.getId());
		}
	}
	
	public void printRelations() {
		Iterator<Edge> it = graph.getEdges().iterator();
		while(it.hasNext()) {
			Edge ed = it.next();
			System.out.println(ed.getVertex(Direction.IN).getId() + "  -->  " + ed.getVertex(Direction.OUT).getId());
		}
	}
	
	private void removeDuplicateRelations() {
		HashSet<String> edges = new HashSet<String>();
		Iterator<Edge> it = graph.getEdges().iterator();
		while(it.hasNext()) {
			Edge ed = it.next();
			
			String relationStrIn = ed.getVertex(Direction.IN).getId() + "  -->  " + ed.getVertex(Direction.OUT).getId();
			String relationStrOut = ed.getVertex(Direction.OUT).getId() + "  -->  " + ed.getVertex(Direction.IN).getId();
			if(edges.contains(relationStrIn) || edges.contains(relationStrOut)) {
				graph.removeEdge(ed);
			} else {
				edges.add(relationStrIn);
			}
		}
	}
	
	public void printPackages() {
		TreeSet<String> keys = new TreeSet<String>(pkgTable.keySet());
		for(String pkgName : keys) {
			System.out.println(pkgName + " " + pkgTable.get(pkgName));
		}
		System.out.println("Total Class Count: " + clsCount);
	}
	
	public static void main(String[] args) {
		PackageGroup group = new PackageGroup("instagram_class.graphml");
		group.removeNodesWithoutPrefix("com.instagram.android.activity");
		group.printRelations();
		ODN.saveToGraphml(group.graph, "instagram_activity_odn.graphml");
	}
}
