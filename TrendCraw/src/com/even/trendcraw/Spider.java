// or done

package com.even.trendcraw;

import java.util.List;

import org.zh.odn.trace.ObjectRelation;

public class Spider extends Thread {
	protected List<Trend> trends;
	protected MySqlConnection conn;
	
	public Spider() {
		conn = new MySqlConnection();
		ObjectRelation.addRelation(this, conn);
	}
}
