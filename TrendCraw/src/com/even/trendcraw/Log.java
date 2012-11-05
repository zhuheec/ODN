package com.even.trendcraw;


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
			fileWriter = new FileWriter("trend_craw.log");
		} catch (IOException e) {
			System.err.println("Error while accessing log file: " + e.getMessage());
		}
		bufferedWriter = new BufferedWriter(fileWriter);
	}
	
	public static void e(String msg) {
		String output = getTimeString() + "ERROR: " + msg;
		System.err.println(output);
		try {
			bufferedWriter.write(output);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			System.err.println("Error while writing log file: " + e.getMessage());
		}
		
	}
	
	public static void d(String msg) {
		System.out.println(getTimeString() + "DEBUG: " + msg);
	}
	
	private static String getTimeString() {
		date.setTime(System.currentTimeMillis());
		return dateFormatter.format(date);
	}
}
