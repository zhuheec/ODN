package org.zh.odn.trace;

public class A {
	public A() {
		B b = new B("test");
		ObjectRelation.getInstance().add(this, b);
	}
	
	public static void main(String[] args) {
		new A();
		new A();
		ObjectRelation.getInstance().printAll();
	}
}

class B {
	public B(String s) {
		ObjectRelation.getInstance().add(this, s);
	}
}
