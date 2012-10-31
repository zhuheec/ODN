package org.zh.odn.trace;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class OdnTracer {
	
	private static Logger log = Logger.getLogger(OdnTracer.class);
	
	private static HashMap<Object, Throwable> traceList = new HashMap<Object, Throwable>();
	
	static { log.setLevel(Level.DEBUG); }
	
	public static void trace(Object obj) {
		traceList.put(obj, new Throwable());
	}
	
	public static void printTrace(Object obj, Throwable t) {
		StackTraceElement[] es = t.getStackTrace();
		for (int i = 0; i < es.length; i++) {
			StackTraceElement e = es[i];
			// do not output trace info of self
			if(! es[i].getClassName().equals(OdnTracer.class.getName())) {
				log.debug("Object [" + obj + "] in class [" + e.getClassName() + "], method [" 
						+ e.getMethodName() + "] "+ (e.isNativeMethod() ? "Native" : ""));
			}
		}
	}
	
	public static void printTrace() {
		for(Object key : traceList.keySet()) {
			printTrace(key, traceList.get(key));
			log.debug("------");
		}
	}
}
