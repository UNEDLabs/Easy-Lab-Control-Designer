package es.uned.dia.ejss.softwarelinks.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import es.uned.dia.softwarelinks.nodejs.RIPInfo;
import es.uned.dia.softwarelinks.nodejs.RIPExperienceInfo;

public class RIPConfigurationModel {
  public static final String RIP_SSE = RIPExperienceInfo.RIP_SSE;
  public static final String RIP_WEBSOCKETS = RIPExperienceInfo.RIP_WEBSOCKETS;
  
  private static final String XNL_STEP = "step";
  private static final String XNL_INIT = "init";
	// XML Node labels for saving the state of the elements 
	private static final String XNL_RIP = "rip";
	private static final String XNL_SERVER = "server";
	private static final String XNL_PORT = "port";
  private static final String XNL_EXPID = "expid";
  private static final String XNL_DESCRIPTION = "description";
  private static final String XNL_API = "api";
	private static final String XNL_LINKS = "links";
	private static final String XNL_ROW = "row";
	private static final String XNL_TRANSPORT = "transport";
	private static final String XNL_SERVERVAR = "servervar";
	private static final String XNL_MODEL = "model";
	private static final String XNL_GET = "get";
	private static final String XNL_SET = "set";

	private String server = "localhost";
	private String port = "8080";
	private String protocol = "";
	private Vector<Vector> data = new Vector<>();
  private String initCode = "";
  private String stepCode = "";
  private RIPExperienceInfo metadata = new RIPExperienceInfo();

  public void setMetadata(RIPExperienceInfo metadata) {
    this.metadata = metadata;
  }
  
	public void setData(Vector<Vector> data) {
		this.data = data;
	}

  public void setServer(String server, String port) {
    setServer(server, port, "http");
  }

  public void setServer(String server) {
    setServer(server, "", "http");
  }

  public void setServer(String server, String port, String protocol) {
		try {
      this.server = URLEncoder.encode(server, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      this.server = "http://localhost:8080/RIP";
    }
		this.port = port;
		try {
	    int portNumber = Integer.valueOf(port);
	    if(portNumber < 0 || portNumber > 65535) {
	      throw new NumberFormatException();
	    }
		} catch(NumberFormatException e) {
      this.port = "";
		}
		if("tcp".equalsIgnoreCase(protocol) || "http".equalsIgnoreCase(protocol)) {
			this.protocol = protocol.toLowerCase();
		}
	}

  public void setApi(String api) {
    this.metadata.setApi(api);
  }
  
  public void setInitCode(String code) {
    this.initCode = code;
  }
  
  public void setStepCode(String code) {
    this.stepCode = code;
  }
  
  public String getURL() {
		return protocol + "://" + server + ":" + port;
	}

	public Vector<Vector> getDataVector() {
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
	
	public RIPExperienceInfo getMetadata() {
    return this.metadata;
  }
	
	public String getServer() {
		try {
      return URLDecoder.decode(server, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return server;
    }
	}
	
	public String getPort() {
		return port;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getInitCode() {
	  return initCode;
	}
	
	public String getStepCode() {
    return stepCode;
  }	

	public String getApi() {
	  return this.metadata.getApi();
	}

	public void restore(String state) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			InputSource is = new InputSource(new StringReader(state));        
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			// Server configuration
			String server = doc.getElementsByTagName(XNL_SERVER).item(0).getTextContent();
			String port = doc.getElementsByTagName(XNL_PORT).item(0).getTextContent();
			String transport = doc.getElementsByTagName(XNL_TRANSPORT).item(0).getTextContent();
      this.server = server;
      String expid = doc.getElementsByTagName(XNL_EXPID).item(0).getTextContent();
      String description = doc.getElementsByTagName(XNL_DESCRIPTION).item(0).getTextContent();
      metadata = new RIPExperienceInfo();
      RIPInfo info = new RIPInfo(expid, description);
      metadata.setInfo(info);
      String api = doc.getElementsByTagName(XNL_API).item(0).getTextContent();
      setApi(api);
			NodeList init = doc.getElementsByTagName(XNL_INIT),
			    step = doc.getElementsByTagName(XNL_STEP);
      initCode = (init.item(0) != null) ? init.item(0).getTextContent() : "";
      stepCode = (step.item(0) != null) ? step.item(0).getTextContent() : "";
			// The links between server variables and ejs variables 
			Node links = doc.getElementsByTagName(XNL_LINKS).item(0);
			if (links != null) {
				NodeList linksList = links.getChildNodes();
				int i = 0; 
				Node node = linksList.item(0);
				setData(new Vector<Vector>());
				while(node != null) {
					if(node.getNodeName() == XNL_ROW) { 
						Object[] row = new Object[4];
						Node next = node.getFirstChild();
						while(next != null) {
							String value = next.getTextContent();
							switch(next.getNodeName()) {
							case XNL_SERVERVAR:
								row[0] = value;
								break;
							case XNL_MODEL:
								row[1] = value;
								break;
							case XNL_GET:
								row[2] = Boolean.valueOf(value);
								break;
							case XNL_SET:
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
			System.err.println("[ERROR]: Found invalid XML serialization.");
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
	  String name = "", description = "";
	  if(metadata != null && metadata.getInfo() != null) {
	    name = metadata.getInfo().getName();
      description = metadata.getInfo().getDescription();
	  }
		String result = "<" + XNL_RIP + ">" +
				"<" + XNL_SERVER + ">" + server + "</" + XNL_SERVER + ">" +
 		  	"<" + XNL_PORT + ">" + port + "</" + XNL_PORT + ">" +
 		  	"<" + XNL_EXPID + ">" + name + "</" + XNL_EXPID + ">" +
        "<" + XNL_DESCRIPTION + ">" + description + "</" + XNL_DESCRIPTION + ">" +
        "<" + XNL_API + ">" + getApi() + "</" + XNL_API + ">" +
 		  	"<" + XNL_TRANSPORT + ">" + protocol + "</" + XNL_TRANSPORT + ">";
		if(data != null) {
			result += "<" + XNL_LINKS + ">";
			Iterable<Vector> links = (Iterable<Vector>)this.data;
			for(Vector<Object> v : links) {
				result += "<" + XNL_ROW + ">" + 
					"<" + XNL_SERVERVAR + ">" + v.elementAt(0) + "</" + XNL_SERVERVAR + ">" +  
					"<" + XNL_MODEL + ">" + v.elementAt(1) + "</" + XNL_MODEL + ">" + 
					"<" + XNL_GET + ">" + v.elementAt(2) + "</" + XNL_GET + ">" +  
					"<" + XNL_SET + ">" + v.elementAt(3) + "</" + XNL_SET + ">" +  
					"</" + XNL_ROW + ">" + "\n";
			}
			result += "</" + XNL_LINKS + ">";
		}
		result += "<" + XNL_INIT + ">" + initCode + "</" + XNL_INIT + ">";
    result += "<" + XNL_STEP + ">" + stepCode + "</" + XNL_STEP + ">";
		result += "</" + XNL_RIP + ">";
		return result;
	}
}