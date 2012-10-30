package org.zh.odn.trace;

import java.util.ArrayList;

public class OdnTracer {
	private static ArrayList<Throwable> traceList = new ArrayList<Throwable>();
	
	public static void trace() {
		traceList.add(new Throwable());
	}
	
	public static void printTrace(Throwable t) {
		StackTraceElement[] es = t.getStackTrace();
		for (int i = 0; i < es.length; i++) {
			StackTraceElement e = es[i];
			System.out.println(" in class:" + e.getClassName()
					+ " in source file:" + e.getFileName() + " in method:"
					+ e.getMethodName() + " at line:" + e.getLineNumber() + " "
					+ (e.isNativeMethod() ? "native" : ""));
		}
	}
	
	public static void printTrace() {
		for(Throwable t : traceList) {
			printTrace(t);
		}
	}
}
