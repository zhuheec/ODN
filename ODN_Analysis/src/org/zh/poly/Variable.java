package org.zh.poly;

import java.util.HashMap;

public class Variable {
	
	private static HashMap<String, Variable> varMap;
	
	static {
		varMap = new HashMap<String, Variable>();
	}
	
	public static Variable getVariable(String name) {
		Variable var = varMap.get(name);
		if(var == null) {
			var = new Variable(name);
			varMap.put(name, var);
		}
		return var;
	}
	
	private String name;
	
	private Variable(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
