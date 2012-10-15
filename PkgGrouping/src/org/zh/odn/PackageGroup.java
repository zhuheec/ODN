package org.zh.odn;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
	
	public void printPackages() {
		TreeSet<String> keys = new TreeSet<String>(pkgTable.keySet());
		for(String pkgName : keys) {
			System.out.println(pkgName + " " + pkgTable.get(pkgName));
		}
		System.out.println("Total Class Count: " + clsCount);
	}
	
	public static void main(String[] args) {
		PackageGroup group = new PackageGroup("instagram_class.graphml");
		group.groupByName(4);
		group.printPackages();
	}
}
