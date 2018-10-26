package es.uned.dia.softwarelinks.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RIPConfigurationModel {
	// XML Node labels for saving the state of the elements 
	private static final String XML_NODE_LABEL_MATLAB = "rpcmatlab";
	private static final String XML_NODE_LABEL_SERVER = "server";
	private static final String XML_NODE_LABEL_PORT = "port";
//	private static final String XML_NODE_LABEL_PATH = "path";
	private static final String XML_NODE_LABEL_LINKS = "links";
	private static final String XML_NODE_LABEL_ROW = "row";
	private static final String XML_NODE_LABEL_TRANSPORT = "transport";
	private static final String XML_NODE_LABEL_LABVIEW = "matlab";
	private static final String XML_NODE_LABEL_MODEL = "model";
	private static final String XML_NODE_LABEL_GET = "get";
	private static final String XML_NODE_LABEL_SET = "set";
	private static final String XML_NODE_LABEL_MODE = "mode";

	private String mode;
	private String server;
	private String port;
	private String protocol;
	private Vector<Vector<Object>> data;

	public void setData(Vector<Vector<Object>> data) {
		this.data = data;
	}

	public void setMode(String mode) {
		switch(mode.toLowerCase()) {
		case "local":
		case "remote":
			this.mode = mode.toLowerCase();
			break;
		default:
		}
	}

	public void setServer(String server, String port, String protocol) {
		this.server = server;
		if (server.length() <= 0) {
			this.server = "localhost";
		}
		this.port = port;
		if(port.length() <= 0) {
			this.port = "2055";
		}
		if("tcp".equalsIgnoreCase(protocol) || "http".equalsIgnoreCase(protocol)) {
			this.protocol = protocol.toLowerCase();
		}
	}

	public String getURL() {
		return protocol + "://" + server + ":" + port;
	}

	public Vector<Vector<Object>> getDataVector() {
		return data;
	}

	public Object[][] getData() {
		Object[][] dataAsMatrix = new Object[data.size()][];
		int i = 0;
		for(Vector<Object> row : data) {
			dataAsMatrix[i++] = row.toArray(); 
		}
		return dataAsMatrix;
	}

	public String getServer() {
		return server;
	}
	
	public String getPort() {
		return port;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getMode() {
		return mode;
	}

	public void restore(String state) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			InputSource is = new InputSource(new StringReader(state));        
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			// Server configuration
			String server = doc.getElementsByTagName(XML_NODE_LABEL_SERVER).item(0).getTextContent();
			String port = doc.getElementsByTagName(XML_NODE_LABEL_PORT).item(0).getTextContent();
			String transport = doc.getElementsByTagName(XML_NODE_LABEL_TRANSPORT).item(0).getTextContent();
			String mode = doc.getElementsByTagName(XML_NODE_LABEL_MODE).item(0).getTextContent();
			setServer(server, port, transport);
			setMode(mode);
			// The links between matlab variables and ejs variables 
			Node links = doc.getElementsByTagName(XML_NODE_LABEL_LINKS).item(0);
			if (links != null) {
				NodeList linksList = links.getChildNodes();
				int i = 0; 
				Node node = linksList.item(0);
				setData(new Vector<Vector<Object>>());
				while(node != null) {
					if(node.getNodeName() == XML_NODE_LABEL_ROW) { 
						Object[] row = new Object[4];
						Node next = node.getFirstChild();
						while(next != null) {
							String value = next.getTextContent();
							switch(next.getNodeName()) {
							case XML_NODE_LABEL_LABVIEW:
								row[0] = value;
								break;
							case XML_NODE_LABEL_MODEL:
								row[1] = value;
								break;
							case XML_NODE_LABEL_GET:
								row[2] = Boolean.valueOf(value);
								break;
							case XML_NODE_LABEL_SET:
								row[3] = Boolean.valueOf(value);
								break;
							}
							next = next.getNextSibling();
						}
						data.add(arrayToVector(row));
					}
					node = linksList.item(++i);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Error al restaurar el estado del elemento.");
		}
	}

	private Vector<Object> arrayToVector(Object[] array) {
		Vector<Object> toReturn = new Vector<>();
		for(Object item : array) {
			toReturn.add(item);
		}
		return toReturn;
	}
	
	public String dump() {
		String result = "<" + XML_NODE_LABEL_MATLAB + ">" +
				"<" + XML_NODE_LABEL_SERVER + ">" + server + "</" + XML_NODE_LABEL_SERVER + ">" +
	   		  	"<" + XML_NODE_LABEL_PORT + ">" + port + "</" + XML_NODE_LABEL_PORT + ">" +
	   		  	"<" + XML_NODE_LABEL_TRANSPORT + ">" + protocol + "</" + XML_NODE_LABEL_TRANSPORT + ">" +
	   		  	"<" + XML_NODE_LABEL_MODE + ">" + mode + "</" + XML_NODE_LABEL_MODE + ">";
		if(data != null) {
			result += "<" + XML_NODE_LABEL_LINKS + ">";
			Iterable<Vector<Object>> links = (Iterable<Vector<Object>>)this.data;
			for(Vector<Object> v : links) {
				result += "<" + XML_NODE_LABEL_ROW + ">" + 
					"<" + XML_NODE_LABEL_LABVIEW + ">" + v.elementAt(0) + "</" + XML_NODE_LABEL_LABVIEW + ">" +  
					"<" + XML_NODE_LABEL_MODEL + ">" + v.elementAt(1) + "</" + XML_NODE_LABEL_MODEL + ">" + 
					"<" + XML_NODE_LABEL_GET + ">" + v.elementAt(2) + "</" + XML_NODE_LABEL_GET + ">" +  
					"<" + XML_NODE_LABEL_SET + ">" + v.elementAt(3) + "</" + XML_NODE_LABEL_SET + ">" +  
					"</" + XML_NODE_LABEL_ROW + ">" + "\n";
			}
			result += "</" + XML_NODE_LABEL_LINKS + ">";
		}
		result += "</" + XML_NODE_LABEL_MATLAB + ">";
		return result;
	}
}