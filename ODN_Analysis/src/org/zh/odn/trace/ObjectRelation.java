package org.zh.odn.trace;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class ObjectRelation {
	public static final String RELATION_SYNTAX = "-->";
	private static ObjectRelation instance = null;
	static{ instance = new ObjectRelation(); }
	public static ObjectRelation getInstance() {
		return instance;
	}
	
	private Graph objectGraph;
	
	public ObjectRelation() {
		objectGraph = new TinkerGraph();
	}
	
	private Vertex getOrCreateObject(Object obj) {
		Vertex ret = objectGraph.getVertex(obj.getClass().getName() 
				+ "@" + obj.hashCode());
		if (ret == null) {
			ret = objectGraph.addVertex(obj.toString());
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
}
