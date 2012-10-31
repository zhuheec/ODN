package org.zh.odn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
	private static Date date = new Date();
	private static FileWriter fileWriter;
	private static BufferedWriter bufferedWriter;
	
	static {
		try {
			fileWriter = new FileWriter("graph.txt");
		} catch (IOException e) {
			System.err.println("Error while accessing log file: " + e.getMessage());
		}
		bufferedWriter = new BufferedWriter(fileWriter);
	}
	
	public static void e(String msg) {
		String output = getTimeString() + "ERROR: " + msg;
		System.err.println(output);
	}
	
	public static void e(Exception ex) {
		e(ex.getClass().getName() + " " + ex.getMessage());
	}
	
	public static void d(String msg) {
		System.out.println(getTimeString() + "DEBUG: " + msg);
		try {
			bufferedWriter.write(msg);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			System.err.println("Error while writing log file: " + e.getMessage());
		}
	}
	
	private static String getTimeString() {
		date.setTime(System.currentTimeMillis());
		return dateFormatter.format(date);
	}
}
