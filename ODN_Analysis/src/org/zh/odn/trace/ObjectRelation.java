/*
 * This file defines the function to add ODN relationship.
 * When to add ODN relationship:
 * 1. At the end of the constructors, link the object to all its members (if they have been initialized).
 * 2. 
 * 
 */

package org.zh.odn.trace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
		instance = new ObjectRelation(); 
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
		String objStr = obj.getClass().getName() + "@" + obj.hashCode();
		Vertex ret = objectGraph.getVertex(objStr);
		if (ret == null) {
			ret = objectGraph.addVertex(objStr);
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
}
