//or done

package com.even.trendcraw;

import java.util.List;

import org.zh.odn.trace.ObjectRelation;

public class GoogleTrendSpider extends Spider {

	private static final String[] countries = new String[] { 
		"Australia","Canada", "China", "Hong Kong", "India", "Japan", "Russia",
		"Singapore", "Taiwan", "United Kingdom", "United States" };
	
	private static final String[] hls = new String[] { 
		"en", "en", "zh-CN", "zh-TW", "en", "ja", "ru", "en", "zh-TW", "en", "en" };

	private static final String[] gls = new String[] { 
		"au", "ca", "cn", "hk", "in", "jp", "ru", "sg", "tw", "gb", "us" };

	private static final String[] ranges = { "day", "week", "month" };

	public void run() {
		while(true) {
			// get trends for all countries
			for (int countryIndex = 0; countryIndex < countries.length; countryIndex++) {
				for (int rangeIndex = ranges.length; rangeIndex >= 0; rangeIndex--) {
					// choose corresponding query string for each country and each range
					String qstr = getQueryString(countryIndex, rangeIndex);
					trends = crawTrends(qstr);
					ObjectRelation.addRelation(trends, qstr);
					ObjectRelation.addRelation(this, trends);
					// save trends to database
					for (Trend t : trends) {
						GoogleTrend gt = (GoogleTrend) t;
						conn.insertGoogleTrend(gt, rangeIndex + 2, countryIndex + 1);
						ObjectRelation.addRelation(conn, t);
						break;
					}
					break;
				}
				break;
			}
			ObjectRelation.save();
			try {
				Log.d("Sleep for 60 minutes...");
				Thread.sleep(1000 * 60 * 60); // sleep 60 minutes
			} catch (InterruptedException e) {
				Log.e("Sleep interrupted: " + e.getMessage());
			} 
		}
	}
	
	private String getQueryString(int countryIndex, int rangeIndex) {
		StringBuilder query = new StringBuilder("http://www.google.com/m/services/trends/");
		if (rangeIndex == ranges.length) { // real time data
			query.append("get?category=web");
		} else { // historical data
			query.append("gethistorics?category=web");
			query.append("&span=");
			query.append(ranges[rangeIndex]);
		}
		query.append("&hl=");
		query.append(hls[countryIndex]);
		query.append("&gl=");
		query.append(gls[countryIndex]);
		return query.toString();
	}

	private List<Trend> crawTrends(String url) {
		TrendsDataPull pull = new GoogleTrendsDataPull(url);
		ObjectRelation.addRelation(this, pull);
		return pull.getTrends();
	}
}
