package com.pinternals.hmi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import biz.source_code.base64Coder.Base64Coder;
import com.pinternals.dsr.SAPJEEDSRExt;

class HmiHandler extends DefaultHandler {
	MappingTestService x;
	private int deep = 0;
	private int ideep = 0;
	private static int maxdeep = 255;
	private char[] stack = new char[maxdeep];
	private String[] attribute_at_name = new String[maxdeep];
	private String[] instance_at_typeid = new String[maxdeep];
	private StringBuilder[] value_text = new StringBuilder[maxdeep];

	HmiHandler(MappingTestService x) {
		this.x = x;
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		// System.out.println(localName + " : " + qName);
		this.stack[deep] = qName.charAt(0);
		if ("attribute".equals(qName)) {
			attribute_at_name[deep] = atts.getValue("name");
		} else if ("value".equals(qName)) {
			// TODO: может быть обработка многоиндексных value
			value_text[deep + 1] = new StringBuilder();
		} else if ("instance".equals(qName)) {
			ideep++;
			instance_at_typeid[deep] = atts.getValue("typeid");
		} else
			throw new IllegalStateException("Unknown element: " + qName
					+ " is not implemented yet");
		deep++;
		if (deep > maxdeep)
			throw new IllegalStateException("source xml very deep; limit of "
					+ maxdeep + " is exceed");
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		deep--;
		switch (stack[deep]) {
		case 'a':
			if (deep == 0 || stack[deep - 1] != 'i')
				throw new IllegalStateException("attribute is not inside instance");
			break;
		case 'v':
			if (deep == 0 || stack[deep - 1] != 'a')
				throw new IllegalStateException("value is not inside attribute");
			x.feedAttribute(attribute_at_name[deep - 1], value_text[deep + 1], ideep, instance_at_typeid[deep - 2], deep > 4 ? attribute_at_name[deep - 4]
					: null);
			value_text[deep + 1] = null;
			break;
		case 'i':
			ideep--;
			if (!(deep == 0 || stack[deep - 1] == 'v'))
				throw new IllegalStateException("instance is not root or not inside value");
			break;
		default:
			throw new IllegalStateException("unknown stack state: "
					+ stack[deep]);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (stack[deep - 1] == 'v') {
			value_text[deep].append(ch, start, length);
		}
	}

	public void endDocument() throws SAXException {
		if (deep != 0)
			throw new IllegalStateException("end document reached but stack is not empty");
		// free all StringBuilders here
		// All the builders used at this.x have to be handled at x.endHmi();
		x.endHmi();
		for (int i = 0; i < maxdeep; i++) {
			if (value_text[i] != null) value_text[i].setLength(0);
			value_text[i] = null;
		}
	}
}

class HmiReqResp {
	// before connect
	public InputStream requestStream = null;
	public long millisBeforeRequest;
	// after connect
	public String stringdtSapResponse;
	public long millisAfterRequest, millisSapResponse;
	private OutputStream requestStreamOut = null;
	public InputStream respStreamIn = null;
	public InputStream errStreamIn = null;
	public int responseCode = 0;
	private HmiHandler handler = null;
	private MappingTestService mts = new MappingTestService();
	private String url;
	private static SAXParserFactory spf = null;
	private static SAXParser sp = null;

	HmiReqResp(String url, InputStream requestStream) {
		this.requestStream = requestStream;
	}

	protected static void prepareParser() throws ParserConfigurationException,
			SAXException {
		spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		sp = spf.newSAXParser();
	}
	static Date parseHttpResponseDate(String dt) throws ParseException {
		// format is like 'Mon, 13 Oct 2014 13:41:05 GMT'
//		dt = "04 Jan 2001";
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH); // HH:mm:ss
		sdf.setLenient(true);
		Date d = sdf.parse(dt);
		return d;
	}

	public void connect(HttpURLConnection conn) throws IOException {
		if (this.requestStream == null)
			throw new IllegalArgumentException("set requestStream at first");
		millisBeforeRequest = new Date().getTime();
		conn.connect();
		requestStreamOut = conn.getOutputStream();
		LightHMIClient.ioCopy(requestStream, requestStreamOut);
		requestStreamOut.flush();
		requestStreamOut.close();

		millisAfterRequest = new Date().getTime();
		stringdtSapResponse = conn.getHeaderField("date"); // like 'Mon, 13 Oct 2014 13:41:05 GMT'
		respStreamIn = conn.getInputStream();
		errStreamIn = conn.getErrorStream();
		responseCode = conn.getResponseCode();
	}

	private void mappingtestservice(InputSource input) throws SAXException,
			IOException, ParserConfigurationException {
		handler = new HmiHandler(mts);
		sp.parse(input, handler);
		mts.parseSpecific(sp);
	}

	public void pullOutput(OutputStream out) throws IOException,
			ParserConfigurationException, SAXException {
		if (responseCode == HttpURLConnection.HTTP_OK) {
			// TODO: проверка на форму ввода пароля или прочий html-шлак
			InputSource input = new InputSource(respStreamIn);
			input.setSystemId(url);
			if (spf == null) prepareParser();
			mappingtestservice(input);
			// LightHMIClient.ioCopy(respStreamIn, out);
		} else
			LightHMIClient.ioCopy(errStreamIn, out);
	}
}

public class LightHMIClient {
	private static String charset = "UTF-8";
	private static String ua = "pinternals.com";
	private static String ct = "text/xml;charset=" + charset;
	private URL url = null;
	private HttpURLConnection conn = null;
	private static byte[] iobuf = new byte[16384];
	private String uname = null, httpauth = null;

	public LightHMIClient(String host, String uname, String passwd)
			throws MalformedURLException {
		this.url = new URL(host + "/rep/mappingtestservice/int?container=ejb");
		StringBuilder sb = new StringBuilder(uname).append(":").append(passwd);
		this.uname = uname;
		this.httpauth = "Basic " + Base64Coder.encodeString(sb.toString());
	}

	public boolean ifExists() throws IOException {
		boolean b = false;
		conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		b = true;
		return b;
	}

	public HmiReqResp doPost(HmiReqResp rqs) throws IOException {
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(10000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", httpauth);
		conn.setRequestProperty("Content-Type", ct);
		conn.setRequestProperty("User-Agent", ua);
		conn.setRequestProperty("Accept-Charset", charset);
		// if (contentlength!=-1) conn.setRequestProperty("Content-Length",
		// Integer.toString(contentlength));
		conn.setUseCaches(false);
		rqs.connect(conn);
		try {
			rqs.millisSapResponse = HmiReqResp.parseHttpResponseDate(rqs.stringdtSapResponse).getTime();
		} catch (ParseException pe) {
			String s = pe.getMessage() + " at offset " + pe.getErrorOffset();
			throw new IOException("Http-date parse error: " + s);
		}
		return rqs;
	}

	public static void main(String[] args) {
		try {
			String host = args[0];
			String uname = args[1];
			String pwd = args[2];
			LightHMIClient l = new LightHMIClient(host, uname, pwd);
			SAPJEEDSRExt e = new SAPJEEDSRExt(host, uname, pwd);
			int q = 1000;
			if (l.ifExists()) {
				for (int i=0; i<10; i++) {
					System.out.println(i);
					FileInputStream f = new FileInputStream("rawhmi3.bin");
	//				System.out.println("resource exists: " + args[0]);
	//				OutputStream r = new FileOutputStream("rawhmi3.html");
					HmiReqResp hr = l.doPost(new HmiReqResp(host, f));
					hr.pullOutput(null);
	
	//				long j = hr.millisSapResponse - (hr.millisAfterRequest - hr.millisBeforeRequest) - 1500; 
					long j = hr.millisSapResponse - q; 
					long k = hr.millisSapResponse + q;
	//				System.out.println("hr.millisBeforeRequest:\t" + hr.millisBeforeRequest);
	//				System.out.println("hr.millisAfterRequest:\t" + hr.millisAfterRequest);
	//				System.out.println("hr.millisSapResponse:\t" + hr.millisSapResponse);
	//				System.out.println("j:\t\t\t" + j);
	//				System.out.println("k:\t\t\t" + k);
					
	//				System.out.println("finished http request:" + hr.responseCode);
					e.request(SAPJEEDSRExt.mappingservice, j, k, uname, 100);
					Thread.sleep(q*3/2);
				}
			} else
				System.err.println("No SAP server found at " + host);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static void ioCopy(InputStream in, OutputStream out)
			throws IOException {
		int len = in.read(iobuf);
		while (len != -1) {
			out.write(iobuf, 0, len);
			len = in.read(iobuf);
		}
	}
}