package org.zh.odn.trace;

public class A {
	public A() {
		new B();
		OdnTracer.trace(this);
	}
	
	public static void main(String[] args) {
		new A();
		OdnTracer.printTrace();
	}
}

class B {
	public B() {
		OdnTracer.trace(this);
	}
}
