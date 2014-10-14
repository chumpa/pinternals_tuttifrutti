package com.pinternals.dsr;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import biz.source_code.base64Coder.Base64Coder;

public class SAPJEEDSRExt {
	HttpClient httpcl;
	public static String mappingservice = "Appl: /rep/mappingtestservice/int";
	private final String soapE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
	private final String soapHB = "<soapenv:Header/><soapenv:Body>";
	private final String dsr1 = "<urn:readDsrRecords xmlns:urn=\"urn:SAPJEEDSRExt\">";
	private final String dsr2s = "<startTime>!</startTime>";
	private final String dsr3e = "<endTime>!</endTime>";
	private final String dsr4fc = "<fc>";
	private final String dsr5app = "<action>!</action>";
	private final String dsr6usr = "<user>!</user>";
	private final String dsr9cf = "</fc>";
	private final String dsrAmx = "<maxRecords>!</maxRecords>";
	private final String dsrL = "</urn:readDsrRecords>";
	private final String paosBE = "</soapenv:Body></soapenv:Envelope>";
	private static SAXParserFactory spf = null;
	private static SAXParser sp = null;

	protected DSR parse(InputSource is, int maxRecords)
			throws ParserConfigurationException, SAXException, IOException {
		if (spf == null) {
			spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			sp = spf.newSAXParser();
		}
		DSR d = new DSR(maxRecords);
		ParseHandler p = new ParseHandler(d);
		sp.parse(is, p);
		return d;
	}

	public SAPJEEDSRExt(String host, String uname, String passwd)
			throws MalformedURLException {
		URL u = new URL(host + "/tc~je~dsr~ws~web/SAPJEEDSRExt_Service");
		httpcl = new HttpClient(u, uname, passwd);
	}

	public DSR request(String operation, long startTime, long endTime,
			String user, int maxRecords) throws IOException,
			ParserConfigurationException, SAXException {
		StringBuilder req = new StringBuilder();
		req.append(soapE).append(soapHB).append(dsr1).append("\n");
		req.append(dsr2s.replace("!", Long.toString(startTime)));
		req.append(dsr3e.replace("!", Long.toString(endTime)));
		req.append(dsr4fc).append(dsr5app.replace("!", operation));
		req.append(dsr6usr.replace("!", user));
		req.append(dsr9cf);
		req.append(dsrAmx.replace("!", Integer.toString(maxRecords)));
		req.append(dsrL).append(paosBE);
		int rc = httpcl.request(req);
		System.out.println("DSR http: " + rc);
		InputSource is = new InputSource(httpcl.conn.getInputStream());
		DSR d = parse(is, maxRecords);
		return d;
	}
}

class HttpClient {
	private static String charset = "UTF-8";
	private static String ct = "text/xml;charset=" + charset;
	URL url;
	String auth;
	HttpURLConnection conn;

	HttpClient(URL url, String uname, String passwd) {
		this.url = url;
		StringBuilder sb = new StringBuilder(uname).append(":").append(passwd);
		auth = "Basic " + Base64Coder.encodeString(sb.toString());
	}

	protected int request(StringBuilder sb) throws IOException {
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(10000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "close");
		// conn.setRequestProperty("User-Agent", ua);
		conn.setRequestProperty("Accept-Charset", charset);
		conn.setRequestProperty("Content-Type", ct);
		conn.setRequestProperty("SOAPAction", "\"\"");
		conn.setRequestProperty("Authorization", auth);
		conn.setUseCaches(false);
		conn.connect();
		OutputStream os = conn.getOutputStream();
		// TODO this is VERY memory-greedy
		String s = sb.toString();
//		System.out.println(s);
		byte[] b = s.getBytes(charset);
		os.write(b, 0, b.length);
		os.flush();
		os.close();
		return conn.getResponseCode();
	}
}

class ParseHandler extends DefaultHandler {
	private StringBuilder content;
	private boolean mainRecordExt = false;
	private Map<String, StringBuilder> row = null;
	DSR dsr;

	ParseHandler(DSR d) {
		this.dsr = d;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		boolean b = "mainRecordExt".equals(qName);
		if (b) {
			row = new HashMap<String, StringBuilder>();
//			System.out.println(qName + "~" + dsr);
		}
		mainRecordExt = mainRecordExt || b;
		content = new StringBuilder();
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
//		System.out.println("/" + qName);
		boolean b = "mainRecordExt".equals(qName);
		if (mainRecordExt && !b) {
			row.put(qName, content);
//			System.out.println(qName + "_" + dsr.toString());
		} else if (b) {
			dsr.feed(row);
			mainRecordExt = false;
		}
		content = null;
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content.append(ch, start, length);
	}

	public void endDocument() throws SAXException {
	}
}