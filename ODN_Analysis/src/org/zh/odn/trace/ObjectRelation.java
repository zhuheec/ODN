package org.zh.odn.trace;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class ObjectRelation {
	
	public static final String RELATION_SYNTAX = "-->";
	private static Logger log = Logger.getLogger(ObjectRelation.class);
	private static ObjectRelation instance = new ObjectRelation();
	
	static { log.setLevel(Level.DEBUG); }
	
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
			objectGraph.addEdge(edgeId, srcVertex, destVertex, null);
		}
	}
	
	public void printAll() {
		for(Edge e : objectGraph.getEdges()) {
			log.debug(e.getVertex(Direction.OUT) + RELATION_SYNTAX + e.getVertex(Direction.IN));
		}
	}
}
