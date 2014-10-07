package com.sap.ide.esr.tools.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DOMUtils {

   private static DocumentBuilderFactory factory;
   private static DOMUtils instance;
   private DocumentBuilder builder;
   private static SoftReference transformer = null;


   private DOMUtils() throws ParserConfigurationException {
      if(null == factory) {
         factory = DocumentBuilderFactory.newInstance();
      }

      this.builder = factory.newDocumentBuilder();
   }

   public Document readDocument(InputStream stream) throws SAXException, IOException {
      return this.readDocument(new InputSource(stream));
   }

   public Node clone(Element element, Element cloneDom, boolean deep) {
      return element.getOwnerDocument().importNode(cloneDom, deep);
   }

   public Document readDocument(InputSource inputSource) throws SAXException, IOException {
      return this.builder.parse(inputSource);
   }

   public static DOMUtils getInstance() {
      if(null == instance) {
         try {
            instance = new DOMUtils();
         } catch (ParserConfigurationException var1) {
            throw new RuntimeException("Parser configuration error", var1);
         }
      }

      return instance;
   }

   public Element getFirstElement(Element parent, String string) {
      Element[] elements = this.getChildElementsByName(parent, string);
      return null != elements && elements.length > 0?elements[0]:null;
   }

   public Element getFirstElement(Element parent) {
      Element[] elements = this.getChildElements(parent);
      return null != elements && elements.length > 0?elements[0]:null;
   }

   public String getAttributeValue(Element element, String attrName) {
      Attr node = element.getAttributeNode(attrName);
      return null != node?node.getValue():null;
   }

   public Element[] getChildElements(Element parent) {
      if(null == parent) {
         return null;
      } else {
         NodeList nodes = parent.getChildNodes();
         ArrayList result = new ArrayList();

         for(int index = 0; index < nodes.getLength(); ++index) {
            Node node = nodes.item(index);
            if(node instanceof Element) {
               result.add((Element)node);
            }
         }

         return (Element[])result.toArray(new Element[result.size()]);
      }
   }

   public Element[] getChildElementsByName(Element parent, String elementName) {
      if(null != parent && null != elementName) {
         Element[] children = this.getChildElements(parent);
         ArrayList result = new ArrayList();
         Element[] arr$ = children;
         int len$ = children.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            Element element = arr$[i$];
            String tagName = element.getTagName();
            if(tagName.contains(":")) {
               String localName = tagName.substring(tagName.indexOf(":") + 1);
               if(elementName.contains(":")) {
                  String eleName = elementName.substring(elementName.indexOf(":") + 1);
                  if(!StringUtil.isEmpty(localName) && !StringUtil.isEmpty(eleName) && localName.equals(eleName)) {
                     result.add(element);
                  }
               } else if(null != localName && localName.equals(elementName)) {
                  result.add(element);
               }
            } else if(elementName.equals(tagName)) {
               result.add(element);
            }
         }

         return (Element[])result.toArray(new Element[result.size()]);
      } else {
         return null;
      }
   }

   public String getLocalName(Element element) {
      String tagName = element.getTagName();
      return tagName.contains(":")?tagName.substring(tagName.indexOf(":") + 1):tagName;
   }

   public String getChildText(Element parent, String name) {
      Element element = this.getFirstElement(parent, name);
      return null != element?element.getTextContent():null;
   }

   public static String transformDOMtoText(Node domElement) throws TransformerException {
      return transformDOMtoText(domElement, true);
   }

   public static String transformDOMtoText(Node domElement, boolean format) throws TransformerException {
      return transformDOMtoText(domElement, format, true);
   }

   public static String transformDOMtoText(Node domElement, boolean format, boolean omitXML) throws TransformerException {
      Transformer transformer = getTransformer();
      DOMSource domSource = new DOMSource(domElement);
      StringWriter stringWriter = new StringWriter();
      StreamResult result = new StreamResult(stringWriter);
      if(omitXML) {
         transformer.setOutputProperty("omit-xml-declaration", "yes");
      } else {
         transformer.setOutputProperty("omit-xml-declaration", "no");
      }

      if(format) {
         transformer.setOutputProperty("indent", "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
      } else {
         transformer.setOutputProperty("indent", "no");
      }

      transformer.transform(domSource, result);
      String text = stringWriter.toString();
	try {
		PrintWriter pw = new PrintWriter("DOM_" + new Date().getTime() + ".log");
		pw.println(text);
		pw.close();
	} catch (Exception e) {}
      
      return text.trim();
   }

   private static Transformer getTransformer() throws TransformerException {
      if(transformer == null || transformer.get() == null) {
         TransformerFactory tFactory = TransformerFactory.newInstance();
         transformer = new SoftReference(tFactory.newTransformer());
      }

      return (Transformer)transformer.get();
   }

   public Document createDocument() {
      return this.builder.newDocument();
   }

   public String getElementText(Element element) {
      NodeList nodes = element.getChildNodes();
      String text = "";
      if(null != nodes) {
         for(int i = 0; i < nodes.getLength(); ++i) {
            if(nodes.item(i) instanceof Text) {
               text = text + ((Text)nodes.item(i)).getTextContent();
            }
         }
      }

      return text;
   }

   public Node[] getChildNodes(NodeList list) {
      if(null == list) {
         return new Node[0];
      } else {
         ArrayList nodes = new ArrayList();

         for(int i = 0; i < list.getLength(); ++i) {
            nodes.add(list.item(i));
         }

         return (Node[])nodes.toArray(new Node[nodes.size()]);
      }
   }

}
