package com.even.trendcraw;

import java.util.Date;

import org.zh.odn.trace.ObjectRelation;



public class GoogleTrend extends Trend {
	
	private boolean historical;
	
	private String query;
	private int rankNow;
	private int rankPrev;
	private float score;
	private String loadingPage;
	private String snippetSrc;
	private String snippetContent;
	private Date timeStamp;
	
	public GoogleTrend(String query, String rankNow, String rankPrev,
			String score, String loadingPage, String snippetSrc, 
			String snippetContent, String timeStamp) {
		// common fields
		this.query = query;
		this.rankNow = Integer.parseInt(rankNow);
		this.score = Float.parseFloat(score);
		this.loadingPage = loadingPage;
		/* validation: 
		 * - historical trend has time stamp.
		 * - latest trend has previous ranking and snippet */
		if(timeStamp == null) { // latest trend
			this.historical = false;
			this.timeStamp = new Date(); // set to current date
			if(rankPrev == null) { //indicates this is a new trend
				this.rankPrev = -1;
			} else {
				this.rankPrev = Integer.parseInt(rankPrev);
			}
			this.snippetSrc = snippetSrc;
			this.snippetContent = snippetContent;
		} else { // historical trend
			this.historical = true;
			this.timeStamp = new Date(Long.parseLong(timeStamp) * 1000);
			this.rankPrev = -1;
			this.snippetSrc = null;
			this.snippetContent = null;
		}
		ObjectRelation.addRelation(this, query, rankNow, rankPrev, score, loadingPage,
				snippetSrc, snippetContent, timeStamp);
	}
	
	public String toString() {
		return "[GoogleTrend] Query: " + this.query + ", Score: " + this.score + ".";
	}
	
	public boolean isHistorical() {
		return historical;
	}
	
	public void setHistorial(boolean historical) {
		this.historical = historical;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public int getRankNow() {
		return rankNow;
	}
	
	public void setRankNow(int rankNow) {
		this.rankNow = rankNow;
	}
	
	public int getRankPrev() {
		return rankPrev;
	}
	
	public void setRankPrev(int rankPrev) {
		this.rankPrev = rankPrev;
	}
	
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public String getLoadingPage() {
		return loadingPage;
	}
	public void setLoadingPage(String loadingPage) {
		this.loadingPage = loadingPage;
	}
	
	public String getSnippetSrc() {
		return snippetSrc;
	}

	public void setSnippetSrc(String snippetSrc) {
		this.snippetSrc = snippetSrc;
	}

	public String getSnippetContent() {
		return snippetContent;
	}

	public void setSnippetContent(String snippetContent) {
		this.snippetContent = snippetContent;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public java.sql.Timestamp getSqlTimestamp() {
		return new java.sql.Timestamp(timeStamp.getTime());
	}
}
