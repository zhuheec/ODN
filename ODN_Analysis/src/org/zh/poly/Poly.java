package org.zh.poly;

import java.util.ArrayList;

public class Poly {
	private ArrayList<Poly> polys;
	private ArrayList<Term> terms;
	
	public Poly(Term... terms) {
		polys = new ArrayList<Poly>();
		this.terms = new ArrayList<Term>();
		for(int i = 0; i < terms.length; i++) {
			this.terms.add(terms[i]);
		}
	}
	
	public void addPoly(Poly poly) {
		
		if(polys.size() == 0) {
			terms.addAll(poly.terms);
		} else {
			ArrayList<Term> mergedTerms = new ArrayList<Term>();
			for(Term t1 : terms) {
				for(Term t2 : poly.terms) {
					mergedTerms.add(new Term(t1, t2));
				}
			}
			terms = mergedTerms;
		}
		polys.add(poly);
	}
	
	public ArrayList<Term> getTerms() {
		return terms;
	}
}
