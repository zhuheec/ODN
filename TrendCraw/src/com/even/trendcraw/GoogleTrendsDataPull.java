// or done

package com.even.trendcraw;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zh.odn.trace.ObjectRelation;

public class GoogleTrendsDataPull extends TrendsDataPull {
	
	// XML node keys
	static final String KEY_ITEM = "item"; // parent node
	static final String KEY_QUERY = "query";
	static final String KEY_RANK_NOW = "rank";
	static final String KEY_RANK_PREV = "prev_rank";
	static final String KEY_SCORE = "score";
	static final String KEY_PAGE = "landing_page";
	static final String KEY_SNIPPET = "snippet";
	static final String KEY_TIME = "timestamp";


	public GoogleTrendsDataPull(String url) {
		super(url);
		NodeList nl = doc.getElementsByTagName(KEY_ITEM);
		// looping through all item nodes <item>
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
		    String query = getValue(e, KEY_QUERY);
		    String rank_now = getValue(e, KEY_RANK_NOW);
		    String rank_prev = getValue(e, KEY_RANK_PREV);
		    String score = getValue(e, KEY_SCORE);
		    String page = getAttributeValue(e, KEY_PAGE, "url");
		    String snippet_src = getAttributeValue(e, KEY_SNIPPET, "src");
		    String snippet_content = getValue(e, KEY_SNIPPET);
		    String time = getValue(e, KEY_TIME);
		    
		    GoogleTrend gt = new GoogleTrend(query, rank_now, rank_prev, score, 
		    		page, snippet_src, snippet_content, time);
		    trends.add(gt);
		    
		    // add relations
		    ObjectRelation.addRelation(nl, e);
		    ObjectRelation.addRelation(e, query, rank_now, rank_prev,
		    		score, page, snippet_src, snippet_content, time);
		    ObjectRelation.addRelation(this, gt);
		}
		ObjectRelation.addRelation(this, url, nl);
	}
}
