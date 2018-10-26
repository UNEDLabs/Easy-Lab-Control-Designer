package org.colos.ejss.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class BasicElement {

  // ------------------------------------
  // Static part
  // -------------------------------------
  
  static private final DocumentBuilder sDocumentBuilder;

  static private final String sENCODING = "UTF-8"; // "UTF-16";
//  static private Charset sCHARSET;

  static {
//    try { sCHARSET = Charset.forName(sENCODING); }
//    catch (Exception exc) { sCHARSET = null; }

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    try {
      sDocumentBuilder = docFactory.newDocumentBuilder();
    } 
    catch (ParserConfigurationException exception) {
      throw new RuntimeException(exception);
    }
  }

  // ------------------------------------
  // Non-static part
  // ------------------------------------
  
  private Document mDocument;
  private Element mElement;

  public BasicElement(String tagName) {
    mDocument = sDocumentBuilder.newDocument();
    mElement = mDocument.createElement(tagName);
    mDocument.appendChild(mElement);    
  }
  

//  public Document getDocument() {
//    return mDocument;
//  }
  
  // --- root element methods

//  public Element getRootElement() {
//    return mElement;
//  }

  public void saveToFile(File file) {
    write(mDocument, file);
  }

  public boolean readRootElement(String filename, String rootName) {
    try {
     mElement = read(filename,rootName);
     return true;
    } catch (Exception e) {
      System.err.println("XML BasicElement: Could not read file: "+filename);
      e.printStackTrace();
      return false;
    }
  }
  
//  protected void setAttribute(String name, String value) {
//    setAttribute(mElement, name, value);
//  }
 
  protected Element addElement(String tagName) {
    return addElement(mElement, tagName);
  }

  protected void addTextElement(String tagName, String value) {
    addTextElement(mElement, tagName, value);
  }

  protected Element getElement(String tagName) {
    return getElement(mElement, tagName);
  }
  
  // --- Child elements methods
  
  protected Element getElement(Node parent, String tagName) {
    Node child = getNodeByName(parent, tagName);
    if (child instanceof Element) return (Element) child;
    return null;
  }
  
  protected Element addElement(Node parent, String tagName) {
    Element element = mDocument.createElement(tagName);
    parent.appendChild(element);
    return element;
  }

  protected void removeNode(Node node, String tagName) {
    Node child = getNodeByName(node, tagName);
    if (child!=null) node.removeChild(child);
  }
  
  protected void addTextElement(Element element, String tagName, String value) {
    Element node = mDocument.createElement(tagName);
    node.appendChild(mDocument.createTextNode(value));
    element.appendChild(node);
  }

  protected void addCDATAElement(Element element, String tagName, String value) {
    Element node = mDocument.createElement(tagName);
    node.appendChild(mDocument.createCDATASection(value));
    element.appendChild(node);
  }

  // ------------------------------------
  // Static utilities
  //------------------------------------

  static protected List<Element> getElementList(Element element, String tagName) {
    ArrayList<Element> list = new ArrayList<Element>();
    NodeList nodeList = element.getElementsByTagName(tagName);
    for (int i=0,n=nodeList.getLength(); i<n;i++) {
      Node node = nodeList.item(i);
      if (node instanceof Element) list.add((Element) node);
    }
    return list;
  }
  
//  static protected void setAttribute(Element element, String name, String value) {
//    Attr attr = element.getAttributeNode(name);
//    if (attr!=null) attr.setValue(value);
//    else element.setAttribute(name, value);
//  }

//  static protected String getAttribute(Element element, String name) {
//    Attr attr = element.getAttributeNode(name);
//    if (attr!=null) return attr.getValue();
//    return null;
//  }

	static protected String evaluateNode(Node node, String name) {
	   Node child = getNodeByName(node, name);
     if (child==null) return null;
     if (child.getFirstChild()==null) return null;
     return child.getFirstChild().getNodeValue();
	 }

   static protected Node getNodeByName(Node node, String name) {
     NodeList list = node.getChildNodes();
     for (int i=0,n=list.getLength(); i<n;i++) {
       Node child = list.item(i);
       if (child.getNodeName().equals(name)) return child;
     }
     return null;
   }

   /**
    * Write the content into an XML file
    * @param filename
    */
   static protected void write(Document document, File file) {
     TransformerFactory transformerFactory = TransformerFactory.newInstance();
     try {
       file.getParentFile().mkdirs();
       Writer writer = new OutputStreamWriter(new FileOutputStream(file)); //,sCHARSET);
       Transformer transformer = transformerFactory.newTransformer();
       transformer.setOutputProperty(OutputKeys.ENCODING,sENCODING);
       DOMSource source = new DOMSource(document);
       StreamResult result =  new StreamResult(writer);
       transformer.transform(source, result);
     } catch (Exception exception) {
       throw new RuntimeException(exception);
     }
   }
   
   /**
    *  Get root XML element
    * @param inputStream
    * @param rootName
    * @return root XML element
    */
   static protected Element read(String filename, String rootName) throws Exception {
     DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
     DocumentBuilder builder = builderFactory.newDocumentBuilder();
     Document document = builder.parse(new FileInputStream(filename));       
     Element rootElement = document.getDocumentElement();
     rootElement.normalize();
     if(!rootElement.getNodeName().equals(rootName)) return null;
     return rootElement;
   }

   /**
    * Saves a String to file using our extended charset
    * @param filename String The name of the file to save
    * @param content String The content to be saved
    * @return File the file created, null if there was any error
    */
   static protected File saveToFile (String filename, String content) {
     try {
       File file = new File(filename);
       file.getParentFile().mkdirs();
       Writer writer;
       try { 
         Charset charset = Charset.forName(sENCODING);
         writer = new OutputStreamWriter(new FileOutputStream(file),charset);
       }
       catch (Exception exc) { 
         writer = new FileWriter(file);
       }
       writer.write(content,0,content.length());
       writer.flush();
       writer.close();
       return file;
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     }
   }

   



}
