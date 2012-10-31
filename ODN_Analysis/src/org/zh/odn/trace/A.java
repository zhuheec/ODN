package org.zh.odn.trace;

public class A {
	public A() {
		new B();
		OdnTracer.trace(this);
	}
	
	public static void main(String[] args) {
		//new A();
		//OdnTracer.printTrace();
		String s = "111::222";
		String[] arr = s.split(":::");
		for (String ss : arr) {
			System.out.println(ss);
		}
	}
}

class B {
	public B() {
		OdnTracer.trace(this);
	}
}
