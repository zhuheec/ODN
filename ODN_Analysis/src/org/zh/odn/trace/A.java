package org.zh.odn.trace;

public class A {
	public A() {
		new B();
		OdnTracer.trace(this);
	}
	
	public static void main(String[] args) {
		A a = new A();
		System.out.println(a);
	}
}

class B {
	public B() {
		OdnTracer.trace(this);
	}
}
