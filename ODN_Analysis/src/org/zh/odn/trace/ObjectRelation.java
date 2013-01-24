/*
 * This file defines the function to add ODN relationship.
 * When to add ODN relationship:
 * 1. At the end of the constructors, link the object to all its members (if they have been initialized).
 * 2. 
 * 
 */

package org.zh.odn.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class ObjectRelation {

	public static final boolean DEBUG = true;
	public static final String RELATION_SYNTAX = "-->";

	private static ObjectRelation instance = null;
	private static Logger log = Logger.getLogger(ObjectRelation.class);
	static{
		log.setLevel(Level.DEBUG);
	}

	public static ObjectRelation getInstance() {
		return instance;
	}
	
	private Graph objectGraph;
	
	public ObjectRelation() {
		objectGraph = new TinkerGraph();
	}
	
	private Vertex getOrCreateObject(Object obj) {
		String objId = obj.getClass().getName() + "@" + obj.hashCode();
		Vertex ret = objectGraph.getVertex(objId);
		if (ret == null) {
			ret = objectGraph.addVertex(objId);
		}
		return ret;
	}
	
	public void add(Object src, Object dest) {
		Vertex srcVertex = getOrCreateObject(src);
		Vertex destVertex = getOrCreateObject(dest);
		String edgeId = srcVertex.getId() + RELATION_SYNTAX + destVertex.getId();
		if(objectGraph.getEdge(edgeId) == null) {
			objectGraph.addEdge(edgeId, srcVertex, destVertex, "");
		}
	}
	
	public static void addRelation(Object src, Object... dst) {
		if(DEBUG && (src != null)) {
			ObjectRelation or = ObjectRelation.getInstance();
			for(Object dest : dst) {
				if(dest != null) {
					or.add(src, dest);
				}
			}
		}
	}
	
	public static void save() {
		if(DEBUG) {
			ObjectRelation or = ObjectRelation.getInstance();
			FileOutputStream fos;
			try {
				SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
				File file = new File("odn_" + fm.format(new Date()) + ".graphml");
				fos = new FileOutputStream(file);
				log.debug("Starting to output ODN graph to file [" + file.getAbsolutePath() + "]...");
				GraphMLWriter.outputGraph(or.objectGraph, fos);
				log.debug("ODN graph output complete.");
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	public static void graphFileToGraphml(String path) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			TinkerGraph graph = (TinkerGraph) ois.readObject();
			ois.close();
			SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
			File file = new File("odn_" + fm.format(new Date()) + ".graphml");
			FileOutputStream fos = new FileOutputStream(file);
			log.debug("Starting to output ODN graph to file [" + file.getAbsolutePath() + "]...");
			GraphMLWriter.outputGraph(graph, fos);
			log.debug("ODN graph output complete.");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	public static void androidOutputToGraphml(String path) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			TinkerGraph graph = new TinkerGraph();
			String line = null;
			while((line = br.readLine()) != null) {
				String[] vtx = line.split("-->");
				if(vtx.length == 2 && !vtx[0].equals(vtx[1])) {
					Vertex v1 = graph.getVertex(vtx[0]);
					if (v1 == null) {
						v1 = graph.addVertex(vtx[0]);
					}
					Vertex v2 = graph.getVertex(vtx[1]);
					if (v2 == null) {
						v2 = graph.addVertex(vtx[1]);
					}
					graph.addEdge(line, v1, v2, "");
				}
			}
			br.close();
			SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddHHmmss");
			File file = new File("odn_" + fm.format(new Date()) + ".graphml");
			FileOutputStream fos = new FileOutputStream(file);
			log.debug("Starting to output ODN graph to file [" + file.getAbsolutePath() + "]...");
			GraphMLWriter.outputGraph(graph, fos);
			log.debug("ODN graph output complete.");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	
	public void printAll() {
		for(Edge e : objectGraph.getEdges()) {
			log.debug(e.getVertex(Direction.OUT) + RELATION_SYNTAX + e.getVertex(Direction.IN));
		}
	}
	
	public static void main(String[] args) {
		androidOutputToGraphml("odn_20121118155948.txt");
	}
}
