package org.zh.poly;

import java.util.ArrayList;
import java.util.HashMap;

public class Term {
	private int coefficient = 1;
	private HashMap<String, Variable> vars;
	
	public Term(Term... terms) {
		vars = new HashMap<String, Variable>();
		for(Term t : terms) {
			coefficient *= t.coefficient;
			vars.putAll(t.vars);
		}
	}
	
	public void setCoefficient(int coefficient) {
		this.coefficient = coefficient;
	}
	
	public void addVar(Variable var) {
		if(!vars.containsKey(var.getName())) {
			vars.put(var.getName(), var);
		}
	}
	
	public ArrayList<Variable> getVars() {
		return new ArrayList<Variable>(vars.values());
	}
}
