//or done
package com.even.trendcraw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zh.odn.trace.ObjectRelation;

public class TrendsDataPull {
	
	protected ArrayList<Trend> trends;
	protected Document doc;
	
	public TrendsDataPull(String url) {
		trends = new ArrayList<Trend>();
		doc = getDomElement(getXmlFromUrl(url)); // getting DOM element
		
		// add relation
		ObjectRelation.addRelation(this, url, doc, trends);
	}
	
	public ArrayList<Trend> getTrends() {
		return trends;
	}

	protected String getXmlFromUrl(String url) {
		StringBuilder xml = new StringBuilder();
		ObjectRelation.addRelation(this, xml); // add relation
		URL site;
		try {
			site = new URL(url);
			ObjectRelation.addRelation(this, site); // add relation
			URLConnection conn = site.openConnection();
			ObjectRelation.addRelation(conn, site); // add relation
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			ObjectRelation.addRelation(in, conn); // add relation
			String inputLine;
	        while ((inputLine = in.readLine()) != null) {
	        	xml.append(inputLine);
	        }
	        in.close();
	        ObjectRelation.addRelation(xml, inputLine); // add relation
	        ObjectRelation.addRelation(inputLine, in); // add relation
			Log.d("Get response success from URL: " + url);
			// return XML
			return xml.toString();
		} catch (MalformedURLException e) {
			Log.e("Invalid URL form.");
		} catch (IOException e) {
			Log.e("I/O exception: " + e.getMessage() +".");
		}
		return null;
	}

	protected Document getDomElement(String xml) {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);
			
			ObjectRelation.addRelation(is, xml); // add relation
			ObjectRelation.addRelation(doc, db, is); // add relation

		} catch (ParserConfigurationException e) {
			Log.e(e.getMessage());
			return null;
		} catch (SAXException e) {
			Log.e(e.getMessage());
			return null;
		} catch (IOException e) {
			Log.e(e.getMessage());
			return null;
		}
		// return DOM
		return doc;
	}

	protected String getValue(Element item, String nodeName) {
		NodeList nl = item.getElementsByTagName(nodeName);
		ObjectRelation.addRelation(item, nodeName); // add relation
		return this.getElementValue(nl.item(0));
	}
	
	protected String getAttributeValue(Element item, String nodeName, String attributeName) {
		String ret = null;
		NodeList nl = item.getElementsByTagName(nodeName);
		ObjectRelation.addRelation(nl, item, nodeName); // add relation
		Node firstNode = nl.item(0);
		ObjectRelation.addRelation(firstNode, nl); // add relation
		if (firstNode != null && firstNode.hasAttributes()) {
			Node n = firstNode.getAttributes().getNamedItem(attributeName);
			if(n != null) {
				ret = n.getNodeValue();
			}
		}
		ObjectRelation.addRelation(ret, firstNode); // add relation
		return ret;
	}

	protected String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						ObjectRelation.addRelation(child, elem); // add relation
						return child.getNodeValue();
					}
				}
			}
		}
		return null;
	}
}
