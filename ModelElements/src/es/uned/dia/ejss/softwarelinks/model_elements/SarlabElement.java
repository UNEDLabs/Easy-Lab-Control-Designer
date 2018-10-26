package es.uned.dia.ejss.softwarelinks.model_elements;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SarlabElement extends AbstractModelElement {
	private static final String ICON_PATH = "es/uned/dia/ejss/softwarelinks/resources/SarlabElement.png";
  private static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon(ICON_PATH);
  
  protected static final Object CONNECTION_OK = "Server OK";
  protected static final Object SERVER_KO = "Server is not working";
  private SarlabConfigurationModel config = new SarlabConfigurationModel();

  private DefaultTableModel htmlProxyTableModel = new DefaultTableModel(new Object[] {"Description", "URL", "Path", "Type", "Element"}, 1);
  private DefaultTableModel websocketProxyTableModel = new DefaultTableModel(new Object[] {"Description", "URL", "Path", "Type", "Element"}, 1);
  private JCheckBox securityCheckbox = new JCheckBox("Secure connection");
  private JTextField serverText = new JTextField("localhost", 20);
  private JTextField portText = new JTextField("80", 6);
  private JTextField expIdText = new JTextField();
  private JTable htmlProxyTable;
  private JTable websocketProxyTable;

  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "SARLAB"; }
  
  public String getConstructorName() { return "SARLAB"; }
  
  public String getInitializationCode(String _name) { // Code for the LINT in JS
    return ""; 
  } 

  public String getSourceCode(String name) { // Code that goes into the body of the model
    String onConnect = "";
    Iterator iter = htmlProxyTableModel.getDataVector().iterator();
    while(iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0);
      String path = (String)row.get(2);
      String type = (String)row.get(3);
      String element = (String)row.get(4);
      if(!element.isEmpty()) {
        switch(type) {
          case "RIP Server":
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getHTTPUrlById('%s', '%s'));", element, id, path);
            //onConnect += String.format("%s.proxy = _model._sarlab.getHTTPProxy('%s', '%s');", element, id, path);
            break;
          case "Camera":
            onConnect += String.format("_model._userUnserialize({'%s':_model._sarlab.getCamUrlById('%s', '%s')});", element, id, path);
            break;
          }
      }
    }
    Iterator j = websocketProxyTableModel.getDataVector().iterator();
    while(j.hasNext()) {
      Vector row = (Vector)j.next();
      String id = (String)row.get(0);
      String path = (String)row.get(2);
      String type = (String)row.get(3);
      String element = (String)row.get(4);
      if(!element.isEmpty()) {
        switch(type) {
          case "RIP Server":
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getWebsocketsUrlById('%s'));", element, id);
            break;
          case "Camera":
            onConnect += String.format("_model._userUnserialize({'%s':_model._sarlab.getCamUrlById('%s', '%s')});", element, id, path);
            break;
          default:
            onConnect += String.format("%s.transport.setHost(_model._sarlab.getWebsocketsUrlById('%s'));", element, id);
            break;
          }
      }
    }

    String code = String.format("var %s = new SarlabProxy('%s', '%s', '%s', '%s', '%s'); _model._sarlab = %s; "
        + "_model._sarlab.connect = function(callback) { "
        + "this.connectExperience(this.experience, function() { %s if(callback != undefined) callback(); }.bind(_model)); }",
        name, config.getSecurity(), config.getHost(), "SARLABV8.0", config.getPort(), config.getExperience(), name, onConnect);
    return code;
  } 
  
  public String getImportStatements() { // Required for Lint
    return "SoftwareLinks/SarlabProxy.js"; 
  }

	public String getTooltip() {
		return "";
	}

	@Override
	protected String getHtmlPage() { 
		return "es/uned/dia/ejss/softwarelinks/resources/SarlabElement.html"; 
	}

	protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.setPreferredSize(new Dimension(600, 400));
		JPanel serverPanel = createServerPanel(mainPanel);
		JScrollPane websocketPanel = createWebsocketPanel();
		JScrollPane proxyPanel = createProxyPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add("Server", serverPanel);
		mainPanel.add("WebSocket Connections", websocketPanel);
		mainPanel.add("HTML Proxy", proxyPanel);

//    htmlProxyTable.addMouseListener (new MouseAdapter() {        
//      public void mousePressed (MouseEvent _evt) {
//        if (htmlProxyTable.isEnabled ()) {
//          int row = htmlProxyTable.rowAtPoint(_evt.getPoint()); 
//          int col = htmlProxyTable.columnAtPoint(_evt.getPoint()); 
//          if(row != -1 && col == 4) {
//            htmlProxyTable.setRowSelectionInterval(row, row);
//            String value = "";
//            String variable = collection.chooseVariable(htmlProxyTable, "", value);
////          String variable = collection.chooseViewElement(htmlProxyTable, Group.class, value);
//            htmlProxyTable.setValueAt(variable, row, col);
//          }
//        }
//      }
//    });    
		
//
//    JButton passwordLinkButton = new JButton(LINK_ICON);
//    passwordLinkButton.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        String value = mPasswordField.getText().trim();
//        if (!ModelElementsUtilities.isLinkedToVariable(value))
//          value = "";
//        else
//          value = ModelElementsUtilities.getPureValue(value);
//        String variable = collection.chooseVariable(mPasswordField, "String",
//            value);
//        if (variable != null)
//          mPasswordField.setText("%" + variable + "%");
//      }
//    });
//


		return mainPanel;
	}

	private JPanel createServerPanel(final Component parent) {
		JPanel serverPanel = new JPanel();
		serverPanel.setBorder(new TitledBorder(null, "SARLAB configuration", TitledBorder.LEADING, TitledBorder.TOP));
		serverPanel.setMinimumSize(new Dimension(620, 120));
		serverPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
		serverPanel.setPreferredSize(new Dimension(620, 120));
		SpringLayout sl_topPanel = new SpringLayout();
		serverPanel.setLayout(sl_topPanel);

		JLabel serverLabel = new JLabel("Server IP SARLAB:");
		sl_topPanel.putConstraint(SpringLayout.NORTH, serverLabel, 7, SpringLayout.NORTH, serverPanel);
		sl_topPanel.putConstraint(SpringLayout.WEST, serverLabel, 5, SpringLayout.WEST, serverPanel);
		serverPanel.add(serverLabel);

		JLabel portLabel = new JLabel("Server port SARLAB:");
		sl_topPanel.putConstraint(SpringLayout.NORTH, portLabel, 0, SpringLayout.NORTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.EAST, serverText);
		serverPanel.add(portLabel);

		JLabel expIdLabel = new JLabel("Experience Identifier:");
		sl_topPanel.putConstraint(SpringLayout.NORTH, expIdLabel, 6, SpringLayout.SOUTH, serverLabel);
		sl_topPanel.putConstraint(SpringLayout.EAST, expIdLabel, 0, SpringLayout.EAST, serverLabel);
        sl_topPanel.putConstraint(SpringLayout.WEST, expIdLabel, 5, SpringLayout.WEST, serverPanel);
		serverPanel.add(expIdLabel);

		sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, serverText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
        sl_topPanel.putConstraint(SpringLayout.WEST, serverText, 6, SpringLayout.EAST, serverLabel);
        serverPanel.add(serverText);

        sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, portText, 0, SpringLayout.VERTICAL_CENTER, serverLabel);
        sl_topPanel.putConstraint(SpringLayout.WEST, portText, 6, SpringLayout.EAST, portLabel);
        sl_topPanel.putConstraint(SpringLayout.EAST, portText, -6, SpringLayout.EAST, serverPanel);
        serverPanel.add(portText);
        portText.setColumns(6);

        sl_topPanel.putConstraint(SpringLayout.VERTICAL_CENTER, expIdText, 6, SpringLayout.VERTICAL_CENTER, expIdLabel);
        sl_topPanel.putConstraint(SpringLayout.WEST, expIdText, 6, SpringLayout.EAST, expIdLabel);
        sl_topPanel.putConstraint(SpringLayout.EAST, expIdText, -6, SpringLayout.EAST, serverPanel);
        serverPanel.add(expIdText);

        sl_topPanel.putConstraint(SpringLayout.NORTH, securityCheckbox, 11, SpringLayout.SOUTH, expIdLabel);
        sl_topPanel.putConstraint(SpringLayout.WEST, securityCheckbox, 5, SpringLayout.WEST, serverPanel);
        serverPanel.add(securityCheckbox);

		JButton testButton = new JButton("Get Server Info");
        sl_topPanel.putConstraint(SpringLayout.NORTH, testButton, 8, SpringLayout.SOUTH, expIdText);
		sl_topPanel.putConstraint(SpringLayout.SOUTH, testButton, 9, SpringLayout.SOUTH, serverPanel);
		sl_topPanel.putConstraint(SpringLayout.HORIZONTAL_CENTER, testButton, 0, SpringLayout.HORIZONTAL_CENTER, serverPanel);
    
		AbstractAction testServer = new AbstractAction("Get Server Info"){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				boolean serverResponds = getServerInfo();
				if(serverResponds) {
					JOptionPane.showMessageDialog(parent, CONNECTION_OK);
				} else {
					JOptionPane.showMessageDialog(parent, SERVER_KO);
				}
			}
		};
		testButton.setAction(testServer);

		serverPanel.add(testButton);
		return serverPanel;
	}

  private boolean getServerInfo() {
    String response;
    try {
      config.setServer(securityCheckbox.isSelected(), serverText.getText(), portText.getText());
      config.setExperience(expIdText.getText());
      String urlformat = "http://%s:%s/SARLABV8.0/webresources/service?idExp=%s";
      String url = String.format(urlformat, config.getHost(), config.getPort(), URLEncoder.encode(config.getExperience(), "UTF-8"));
      response = (String)SarlabElement.httpget(url);
      config.load(response);
      List<Map<String,String>> htmlproxies = config.getHtmlProxies();
      Iterator<Map<String, String>> iter = htmlproxies.iterator();
      htmlProxyTableModel.getDataVector().removeAllElements();
      while(iter.hasNext()) {
        Map<String, String> proxy = iter.next();
        htmlProxyTableModel.addRow(new Object[] {proxy.get("description"), proxy.get("url"), "", "Camera", ""});
      }      
      List<Map<String, String>> wsproxies = config.getWebsocketProxies();
      Iterator<Map<String, String>> j = wsproxies.iterator();
      websocketProxyTableModel.getDataVector().removeAllElements();
      while(j.hasNext()) {
        Map<String, String> proxy = j.next();
        websocketProxyTableModel.addRow(new Object[] {proxy.get("description"), proxy.get("url"), "", "Camera", ""});
      }
      
      
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
	private JScrollPane createWebsocketPanel() {
    websocketProxyTable = new JTable(websocketProxyTableModel);
    websocketProxyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane websocketScrollPane = new JScrollPane(websocketProxyTable);
    websocketScrollPane.setBorder(BorderFactory.createTitledBorder("Websocket Proxies"));
    return websocketScrollPane;
	}

  private JScrollPane createProxyPanel() {
    htmlProxyTable = new JTable(htmlProxyTableModel);
    htmlProxyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    TableColumn column = htmlProxyTable.getColumnModel().getColumn(3);
    JComboBox comboBox = new JComboBox();
    comboBox.addItem("RIP Server");
    comboBox.addItem("Camera");
    column.setCellEditor(new DefaultCellEditor(comboBox));
    JScrollPane htmlProxyScrollPane = new JScrollPane(htmlProxyTable);
    htmlProxyScrollPane.setBorder(BorderFactory.createTitledBorder("HTML Proxies"));
    return htmlProxyScrollPane;
  }

  public String savetoXML() {
    Boolean security = securityCheckbox.isSelected();
    String server = serverText.getText().trim();
    String port = portText.getText().trim();
    config.setServer(security, server, port);
    config.setExperience(expIdText.getText());
    config.addLinks(htmlProxyTableModel.getDataVector());
    config.addWebsocketsLinks(websocketProxyTableModel.getDataVector());
    return config.dump();
  }
  
  public void readfromXML(String inputXML) {
    config.restore(inputXML);
    serverText.setText(config.getHost());
    portText.setText(config.getPort());
    expIdText.setText(config.getExperience());
    htmlProxyTableModel.getDataVector().removeAllElements();
    for(Map<String, String> link : config.getLinks()) {
      String id = link.get("id"),
          ip =link.get("ip"),
          path = link.get("path"),
          type = link.get("type"),
          element = link.get("element");
      htmlProxyTableModel.addRow(new Object[] {id, ip, path, type, element});
    }
    websocketProxyTableModel.getDataVector().removeAllElements();
    for(Map<String, String> link : config.getWebsocketsLinks()) {
      String id = link.get("id"),
          ip =link.get("ip"),
          path = link.get("path"),
          type = link.get("type"),
          element = link.get("element");
      websocketProxyTableModel.addRow(new Object[] {id, ip, path, type, element});
    }
  }

  public static String httpget(String request) throws Exception {
    Object response = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(request);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            response = responseBody;
        } catch(NoHttpResponseException e) {
        } finally {
          httpclient.close();
        }
        return (String)response;
  }
}


class SarlabConfigurationModel {
  private static final String XNL_SARLAB = "sarlab";
  private static final String XNL_SERVER = "server";
  private static final String XNL_PORT = "port";
  private static final String XNL_EXPERIENCE = "experience";
  private static final String XNL_WEBSOCKETS_LINKS = "wslinks";
  private static final String XNL_LINKS = "links";
  private static final String XNL_LABEL_LINK = "link";
  private static final String XNL_ID = "id";
  private static final String XNL_IP = "ip";
  private static final String XNL_PATH = "path";
  private static final String XNL_TYPE = "type";
  private static final String XNL_ELEMENT = "element";
  private Boolean security = false;
  private String host = "localhost";
  private String port = "80";
  private String experience = "exp";
  
  private ArrayList<Map<String, String>> websocketProxies = new ArrayList<>();
  private ArrayList<Map<String, String>> websocketLinks = new ArrayList<>(); 
  private ArrayList<Map<String, String>> htmlProxies = new ArrayList<>();
  private ArrayList<Map<String, String>> htmlLinks = new ArrayList<>(); 
  
  public void setServer(Boolean secure, String host, String port) {
    this.security = secure;
    this.host = host;
    if (host.length() <= 0) {
      this.host = "localhost";
    }
    this.port = port;
    if(port.length() <= 0) {
      this.port = "80";
    }
  }

  public void setExperience(String experience) {
       this.experience = experience;
  }

  public Boolean getSecurity() {
    return security;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }

  public String getExperience() {
    return experience;
  }
  
  public List<Map<String, String>> getWebsocketProxies() {
    return websocketProxies;
  }

  public List<Map<String, String>> getHtmlProxies() {
    return htmlProxies;
  }

  public void addWebsocketsLink(String id, String ip, String path, String type, String element) {
    Map<String, String> row = new HashMap<>();
    row.put("id", id);
    row.put("ip", ip);
    row.put("path", path);
    row.put("type", type);
    row.put("element", element);
    websocketLinks.add(row);
  }

  public void addLink(String id, String ip, String path, String type, String element) {
    Map<String, String> row = new HashMap<>();
    row.put("id", id);
    row.put("ip", ip);
    row.put("path", path);
    row.put("type", type);
    row.put("element", element);
    htmlLinks.add(row);
  }
  
  public void addLinks(Vector data) {
    htmlLinks.clear();
    Iterator iter = data.iterator();
    while(iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0),
          ip = (String)row.get(1),
          path = (String)row.get(2),
          type = (String)row.get(3),
          element = (String)row.get(4);
      addLink(id, ip, path, type, element);
    }
  }

  public void addWebsocketsLinks(Vector data) {
    websocketLinks.clear();
    Iterator iter = data.iterator();
    while(iter.hasNext()) {
      Vector row = (Vector)iter.next();
      String id = (String)row.get(0),
          ip = (String)row.get(1),
          path = (String)row.get(2),
          type = (String)row.get(3),
          element = (String)row.get(4);
      addWebsocketsLink(id, ip, path, type, element);
    }
  }
  
  public List<Map<String, String>> getLinks() {
    return htmlLinks;
  }

  public List<Map<String, String>> getWebsocketsLinks() {
    return websocketLinks;
  }
  
  public String load(String config) {
    JsonObject response = null;
    try {
      InputStream stream = new ByteArrayInputStream(config.getBytes("UTF-8"));
      JsonReader reader = Json.createReader(stream);
      response = reader.readObject();
      JsonObject websocketProxyList = (JsonObject)response.get("ListConnectionProxyWebsocket");
      this.websocketProxies = getProxies(websocketProxyList, "ConnectionProxyWebsocket");
      JsonObject htmlProxyList = (JsonObject)response.get("ListConnectionProxyHTML");
      this.htmlProxies = getProxies(htmlProxyList, "ConnectionProxyHTML");
    } catch (UnsupportedEncodingException | JsonException e) {
      System.err.println(e.getCause());
      return null;
    }
    return "";
  }
  
  private ArrayList<Map<String, String>> getProxies(JsonObject proxyList, String key) {
    ArrayList<Map<String, String>> proxies = new ArrayList<>();
    JsonArray proxiesArray = (JsonArray)proxyList.get(key);
    Iterator<JsonValue> iter = proxiesArray.iterator();
    while(iter.hasNext()) {
      JsonObject proxy = (JsonObject)iter.next();
      if (proxy != null) {
        HashMap<String, String> htmlProxy = new HashMap<>();
        String ip = proxy.getString("IPInternal");
        int port = proxy.getInt("PortInternal");
        String description = proxy.getString("Description");
        htmlProxy.put("url", ip+":"+port);
        htmlProxy.put("description", description);
        proxies.add(htmlProxy);
      }
    }
    return proxies;
  }
  
  public void restore(String state) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      InputSource is = new InputSource(new StringReader(state));        
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(is);
      host = doc.getElementsByTagName(XNL_SERVER).item(0).getTextContent();
      port = doc.getElementsByTagName(XNL_PORT).item(0).getTextContent();
      experience = doc.getElementsByTagName(XNL_EXPERIENCE).item(0).getTextContent();

      htmlLinks.clear();
      NodeList links = doc.getElementsByTagName(XNL_LINKS).item(0).getChildNodes();
      for (int i=0; i<links.getLength(); i++) {
        NamedNodeMap info = links.item(i).getAttributes();
        String id = info.getNamedItem(XNL_ID).getTextContent(),
            ip = info.getNamedItem(XNL_IP).getTextContent(),
            path = info.getNamedItem(XNL_PATH).getTextContent(),
            type = info.getNamedItem(XNL_TYPE).getTextContent(),
            element = info.getNamedItem(XNL_ELEMENT).getTextContent();
        addLink(id, ip, path, type, element);
      }

      websocketLinks.clear();
      links = doc.getElementsByTagName(XNL_WEBSOCKETS_LINKS).item(0).getChildNodes();
      for (int j=0; j<links.getLength(); j++) {
        NamedNodeMap info = links.item(j).getAttributes();
        String id = info.getNamedItem(XNL_ID).getTextContent(),
            ip = info.getNamedItem(XNL_IP).getTextContent(),
            path = info.getNamedItem(XNL_PATH).getTextContent(),
            type = info.getNamedItem(XNL_TYPE).getTextContent(),
            element = info.getNamedItem(XNL_ELEMENT).getTextContent();
        addWebsocketsLink(id, ip, path, type, element);
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      System.err.println("Error al restaurar el estado del elemento.");
    } catch(Exception e) {
      System.err.println("Error desconocido al restaurar el estado del elemento.");
    }
  }

  public String dump() {
    String result = "<" + XNL_SARLAB + ">"
         + "<" + XNL_SERVER + ">" + host + "</" + XNL_SERVER + ">"
         + "<" + XNL_PORT + ">" + port + "</" + XNL_PORT + ">"
         + "<" + XNL_EXPERIENCE + ">" + experience + "</" + XNL_EXPERIENCE + ">";
    result += "<" + XNL_LINKS + ">";
    for(Map<String, String> link : htmlLinks) {
        result += "<" + XNL_LABEL_LINK + " " + 
            XNL_ID + "=\"" + link.get("id") + "\" " +
            XNL_IP + "=\"" + link.get("ip") + "\" " +
            XNL_PATH + "=\"" + link.get("path") + "\" " +
            XNL_TYPE + "=\"" + link.get("type") + "\" " +
            XNL_ELEMENT + "=\"" + link.get("element") + "\" " +
        "></" + XNL_LABEL_LINK + ">";
    }
    result += "</" + XNL_LINKS + ">"; 
    result += "<" + XNL_WEBSOCKETS_LINKS + ">";
    for(Map<String, String> link : websocketLinks) {
      result += "<" + XNL_LABEL_LINK + " " +
          XNL_ID + "=\"" + link.get("id") + "\" " +
          XNL_IP + "=\"" + link.get("ip") + "\" " +
          XNL_PATH + "=\"" + link.get("path") + "\" " +
          XNL_TYPE + "=\"" + link.get("type") + "\" " +
          XNL_ELEMENT + "=\"" + link.get("element") + "\" " +
      "></" + XNL_LABEL_LINK + ">";
    }
    result += "</" + XNL_WEBSOCKETS_LINKS + ">"; 
    result += "</" + XNL_SARLAB + ">";
    return result;
  }
}