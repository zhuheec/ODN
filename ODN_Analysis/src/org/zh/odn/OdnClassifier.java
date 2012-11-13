package org.zh.odn;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class OdnClassifier {
	
	private static final Logger log = Logger.getLogger(OdnClassifier.class);
	static { log.setLevel(Level.DEBUG); }
	
	private Hashtable<String, Integer> pkgTable;
	private Graph graph;
	
	public OdnClassifier(Graph graph) {
		log.debug("Initializing ODN Classifer...");
		this.graph = graph;
		pkgTable = new Hashtable<String, Integer>();
		log.debug("Finished initializing ODN Classifer.");
	}
	
	public OdnClassifier(String path) {
		this(new OdnGraph(path, 0.1, 0.1));
	}
	
	public void groupByName(int prefixCount) {
		pkgTable.clear();
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
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
	
	public void printClasses() {
		Iterator<Vertex> it = graph.getVertices().iterator();
		while(it.hasNext()) {
			Vertex v = it.next();
			log.debug("Class [ " + v.getId() +" ]");
		}
	}
	
	public void printClasses(String prefix) {
		log.debug("Printing all classes with prefix [" + prefix + "]...");
		int count = 0;
		for(Vertex v : graph.getVertices()) {
			if(v.getId().toString().startsWith(prefix)) {
				log.debug("Class [ " + v.getId() +" ]");
				count++;
			}
		}
		log.debug("Done printing [" + count + "] classes with prefix [" + prefix + "].");
	}
	
	public void printRelations() {
		Iterator<Edge> it = graph.getEdges().iterator();
		while(it.hasNext()) {
			Edge ed = it.next();
			log.debug("Relation [ " + ed.getVertex(Direction.IN).getId() + "  -->  " + ed.getVertex(Direction.OUT).getId() +" ]");
		}
	}
	
	public void printGroups() {
		TreeSet<String> keys = new TreeSet<String>(pkgTable.keySet());
		for(String pkgName : keys) {
			log.debug("Package group [ " + pkgName + " ], class count [ " + pkgTable.get(pkgName) + " ]");
		}
	}
	
	public static void main(String[] args) {
		OdnClassifier group = new OdnClassifier("instagram_class.graphml");
		group.groupByName(4);
		group.printClasses("com.instagram.api.request");
	}
}
