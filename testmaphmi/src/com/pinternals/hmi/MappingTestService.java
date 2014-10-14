package com.pinternals.hmi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class HmiRespHandler extends DefaultHandler {
	private StringBuilder content;
	protected StringBuilder outputXML;
	protected List<String> messages = new ArrayList<String>(100);
	private String messageLevel;

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		content = new StringBuilder();
		if ("message".equals(qName))
			messageLevel = atts.getValue("level");
		else if ("exportParameters".equals(qName)) {
			// TODO add code here
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("outputXML".equals(qName))
			outputXML = content;
		else if ("message".equals(qName))
			messages.add(messageLevel + ":" + content.toString());
		content = null;
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content.append(ch, start, length);
	}

	public void endDocument() throws SAXException {
	}
}

public class MappingTestService {
	public static String respTypeId = "com.sap.aii.utilxi.hmi.core.msg.HmiResponse";
	public static String outpTypeId = "com.sap.aii.utilxi.hmi.api.HmiMethodOutput";
	StringBuilder methodOutput = new StringBuilder();
	// StringBuilder methodFault = new StringBuilder();
	Map<String, String> atts = new HashMap<String, String>();
	InputSource inpSource = null;
	private final String _all = "<testExecutionRequest>%ref%<testData><inputXml>%input%</inputXml>%parameters%<traceLevel>%traceLevel%</traceLevel></testData></testExecutionRequest>"; 
	private final String _ref = "<ref><vc swcGuid=\"%guid%\" vcType=\"S\"/><key typeID=\"XI_TRAFO\"><elem>%MM_NAME%</elem><elem>%MM_NAMESPACE%</elem></key></ref>";
	private final String _param = "<parameters><testParameterInfo></testParameterInfo></parameters>"; 
	private final String _hp = "<HeaderParameters><properties>%body%</properties></HeaderParameters>"; 
	private final String _ip = "<ImportingParameters><properties>%body%</properties></ImportingParameters>"; 
	private final String _prop = "<property name=\"%n%\">%v%</property>"; 

	private final String _hprefix = "" +
	"<instance typeid=\"com.sap.aii.util.hmi.core.msg.HmiRequest\">" + 
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ClientId\">" +
		"<value index=\"0\" isnull=\"false\">%ClientId%</value></attribute>" + 
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ClientLanguage\">" + 
		"<value index=\"0\" isnull=\"false\">%ClientLanguage%</value></attribute>" +
	"<attribute isleave=\"false\" name=\"ClientLevel\">" +
		"<value index=\"0\" isnull=\"false\">" +
			"<instance typeid=\"com.sap.aii.util.applcomp.ApplCompLevel\">" +
				"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"Release\">" +
					"<value index=\"0\" isnull=\"false\">%Release%</value></attribute>" +
				"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"SupportPackage\">" +
					"<value index=\"0\" isnull=\"false\">%SupportPackage%</value></attribute></instance></value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ClientPassword\">" +
		"<value index=\"0\" isnull=\"false\">dummy</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ClientUser\">" +
		"<value index=\"0\" isnull=\"false\">%ClientUser%</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ControlFlag\">" +
		"<value index=\"0\" isnull=\"false\">0</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"HmiSpecVersion\">" +
		"<value index=\"0\" isnull=\"false\">1.0</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"MethodId\">" +
		"<value index=\"0\" isnull=\"false\">executemappingmethod</value></attribute>" +
	"<attribute isleave=\"false\" name=\"MethodInput\">" +
		"<value index=\"0\" isnull=\"false\">" +
			"<instance typeid=\"com.sap.aii.util.hmi.api.HmiMethodInput\">" +
				"<attribute isleave=\"false\" name=\"Parameters\">" +
					"<value index=\"0\" isnull=\"false\">" +
						"<instance typeid=\"com.sap.aii.util.hmi.core.gdi2.EntryStringString\">" +
							"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"Key\">" +
								"<value index=\"0\" isnull=\"false\">body</value></attribute>" +
							"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"Value\">" +
								"<value index=\"0\" isnull=\"false\">";
	private final String _hpsuffix = "" +
								"</value></attribute></instance></value></attribute></instance></value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"RequestId\">" +
		"<value index=\"0\" isnull=\"false\">%RequestId%</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"RequiresSession\">" +
		"<value index=\"0\" isnull=\"false\">%RequiresSession%</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ServerApplicationId\">" +
		"<value index=\"0\" isnull=\"false\">systemType</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ServerLogicalSystemName\">" +
		"<value index=\"0\" isnull=\"false\">ZZ_BNN</value></attribute>" +
	"<attribute isleave=\"true\" leave_typeid=\"string\" name=\"ServiceId\">" +
		"<value index=\"0\" isnull=\"false\">mappingtestservice</value></attribute></instance>";

	public void prepareRequest(HmiConstants hmc, Map<String,String> mmparams, InputStream payload, OutputStream rez) throws IOException {
		
	}
	
	public void feedAttribute(String name, StringBuilder value, int ideep,
			String iname, String pname) {
		// name имя аттрибута
		// value текст
		// ideep уровень вложенности по instance
		// iname typeId родительского instance
		// pname имя родительского атрибута
		if (ideep == 1 && respTypeId.equals(iname))
			atts.put(name, value.toString());
		else if (ideep == 2 && outpTypeId.equals(iname)
				&& "MethodOutput".equals(pname)) {
			if ("Return".equals(name)) {
				methodOutput = value;
			} else if ("ContentType".equals(name)) {
				// do nothing
			} else
				throw new IllegalStateException("not implemented output attribute: "
						+ name);
		} else if (ideep == 2 && outpTypeId.equals(iname)
				&& "MethodFault".equals(pname)) {
			throw new IllegalStateException("MethodFault detected. Write the code here!");
		} else
			throw new IllegalStateException("Bad input data");
		// System.out.println("*" + iname + "_" + ideep + "_" + pname + "*" +
		// name + "=" + value);
	}

	public void endHmi() {
		inpSource = new InputSource(new StringReader(methodOutput.toString()));
//		System.out.println(methodOutput.toString());
	}
	
	public void parseSpecific(SAXParser sp) throws SAXException, IOException {
		HmiRespHandler h = new HmiRespHandler();
		sp.parse(inpSource, h);
		inpSource = null;
//		System.out.println(h.outputXML);
	}
}
