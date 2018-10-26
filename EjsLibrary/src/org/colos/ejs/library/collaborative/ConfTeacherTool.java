/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Teacher Interface: this is the class teacher connection with the collaborative simulation
 */


public class ConfTeacherTool {
	
	//Actions
	private static final String http = "http://";
	private static final String strAccept = "accept";
	
	//URL String generated
	private String htmlGenerated=null;
	
	//Params Dialog
	private String paramServer;
	private String paramPort;
	
	//IP public direction
	private InetAddress IPDirection;

	//Dimension frame
	private Dimension dim;
	
	//static variables
	protected static ConfTeacherTool CONF_TEACH_TOOL=null;
	protected static Dimension defaultSize = new Dimension (500, 370);
	protected static String defaultServer="http://localhost:8080";
	protected static String defaultPort="50000";
	
	//Interfaces embedded
	protected ListStudentsTree teacherGUI;

	//Simulation of teacher to manage it
	protected SimulationCollaborative sim;
	
	//Array Fields
	protected ArrayList<String> ListFields = new ArrayList<String>();
	
	//Swing Components
	JFrame mainFrame;
	JButton acceptButton;
	JButton cancelButton;
	JButton connectButton;
	JTextField ServerField;
    JTextField IPField;
    JTextField PortField;
    JTextField UserField;
    JPasswordField PasswordField;
    JTextField HtmlField;
    JLabel ServerLabel;
    JLabel PortLabel;
    JLabel UserLabel;
    JLabel PasswordLabel;
    JLabel HtmlLabel;
    JCheckBox IPCBox;
    JPanel panel;
    JPanel panelButton;
    JPanel panelButtonUp;
    JPanel panelButtonDown;
    JTextArea statusArea;
    JScrollPane panelArea;
   
    //**Changes 13/04/2011
    private boolean isCollaborative = false; 
    protected static boolean spanish = false; 
    private String directorname = null;
    
   /**
    * Constructor without parameters.
  	*/
    public ConfTeacherTool() {
	    this(defaultSize,null,defaultServer,defaultPort);
	}

    
   /**
    * Constructor with parameters
    * @param dim Dimension 
    * @param sim SimualationCollaborative Simulation for collaboration
    * @param paramServer String Public web server
    * @param paramPort String Connection Port
    */
	public ConfTeacherTool(Dimension dim, SimulationCollaborative sim,String paramServer,String paramPort) {
	    this.dim = dim;
	    this.sim = sim;
	    this.paramServer=paramServer;
	    this.paramPort=paramPort;
	    try{
	    	if(sim.getModel()._getParameter("is_collaborative")!=null && sim.getModel()._getParameter("is_collaborative").equals("true")) this.isCollaborative = true;
	    	if(this.isCollaborative){
	    		if(sim.getModel()._getParameter("Port_Teacher")!=null) this.paramPort = sim.getModel()._getParameter("Port_Teacher");
	    		else this.paramPort = "50000";
	    		if(sim.getModel()._getParameter("language")!=null){
	    			if(sim.getModel()._getParameter("language").equals("es")) spanish = true;
	    		}
	    		if(sim.getModel()._getParameter("directorname")!=null) this.directorname = sim.getModel()._getParameter("directorname");
	    		else this.directorname="teacher";
	    	}
	    }catch(Exception e){System.err.println(spanish?"Parámetros Moodle nulos":"Null Teacher Applet Parameters");}
    }
	
	
	/**
	* Method to get the interface
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param paramServer String Public web server
	* @param paramPort String Connection Port
	* @return ConfTeacherTool Object Interface
	*/
	protected static ConfTeacherTool getTool(SimulationCollaborative simulation,String paramServer,String paramPort) 
    {
	   if (CONF_TEACH_TOOL==null){CONF_TEACH_TOOL= new ConfTeacherTool(defaultSize,simulation,paramServer,paramPort);}
	   return CONF_TEACH_TOOL;
	}

	
	/**
	* Method to get the interface
	* @param dim Dimension
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param paramServer String Public web server
	* @param paramPort String Connection Port
	* @return ConfTeacherTool Object Interface
	*/
	protected static ConfTeacherTool getTool(Dimension dim,SimulationCollaborative simulation,String paramServer,String paramPort) 
	{
	   if (CONF_TEACH_TOOL==null) CONF_TEACH_TOOL = new ConfTeacherTool(dim,simulation,paramServer,paramPort);
		   return CONF_TEACH_TOOL;
	} 

	
	
	// ---------------------------
	// Private Methods
	// ---------------------------
	/**
	* Interface creation
	*/
	protected void createGUI() 
	{
		//Frame
		JFrame.setDefaultLookAndFeelDecorated(false);
		mainFrame = new JFrame ("HTML generation");
        mainFrame.setSize(this.dim);
        
        //Buttons
        acceptButton = new JButton("Accept");
        acceptButton.setHorizontalAlignment(JButton.CENTER);
        acceptButton.setActionCommand(strAccept);
        acceptButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				acceptAction(evt);
			}
		});
        
        cancelButton = new JButton("Cancel");
        cancelButton.setHorizontalAlignment(JButton.CENTER);
        cancelButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent evt){
        		cancelAction(evt);
        	}
        });
		
        connectButton = new JButton("Connect the student's server");
        connectButton.setHorizontalAlignment(JButton.CENTER);
        connectButton.setEnabled(false);
        connectButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent evt){
        		connectAction(evt);
        	}
        });
        
        //Fields **Changes 13/04/2011
        ServerField = new JTextField();
        ServerField.setText(this.paramServer);
        ServerField.setCaretPosition(ServerField.getText().length());
        UserField = new JTextField();
        PasswordField = new JPasswordField(10);
        
        IPField = new JTextField();
        IPDirection = getIPLocal();
        IPField.setText(IPDirection.getHostAddress());
        PortField = new JTextField();
        PortField.setText(this.paramPort);
        HtmlField = new JTextField();
        HtmlField.setEnabled(false);
    
        //Labels
        ServerLabel = new JLabel("Server URL");
        ServerLabel.setHorizontalAlignment(JLabel.CENTER);
        UserLabel = new JLabel ("Identificator");
        UserLabel.setHorizontalAlignment(JLabel.CENTER);
        PasswordLabel = new JLabel("Password");
        PasswordLabel.setHorizontalAlignment(JLabel.CENTER);
   
        PortLabel = new JLabel ("Port");
        PortLabel.setHorizontalAlignment(JLabel.CENTER);
        HtmlLabel = new JLabel();
        HtmlLabel.setForeground(new Color(255,0,0));
        HtmlLabel.setText("Web generated");
        HtmlLabel.setHorizontalAlignment(JLabel.CENTER);
        
        //ChecxBox
        IPCBox = new JCheckBox();
        IPCBox.setText("Use local IP");
        IPCBox.setHorizontalAlignment(JCheckBox.CENTER);
        
        //Text Area
        statusArea = new JTextArea();
        statusArea.setFont(new Font("Courier", Font.BOLD, 12));
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
     
        //ScrollPane
        panelArea = new JScrollPane(statusArea);
        panelArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelArea.setPreferredSize(new Dimension(100,100));
        panelArea.setBorder(
  	      BorderFactory.createCompoundBorder(
    		       BorderFactory.createCompoundBorder(
      		         BorderFactory.createTitledBorder("Communication channel"),
        		      BorderFactory.createEmptyBorder(5,5,5,5)),
          		 panelArea.getBorder()));
        
        
        //Creation of a Panel and set the Layout
        panel = new JPanel();
        
        panel.setLayout(new GridLayout(6,2));
        
        //Add the controls JText and the labels JLabels
        panel.add(ServerLabel);
        panel.add(ServerField);     
        panel.add(IPCBox);
        panel.add(IPField);
        panel.add(PortLabel);
        panel.add(PortField);
        panel.add(UserLabel);
        panel.add(UserField);
        panel.add(PasswordLabel);
        panel.add(PasswordField);
        panel.add(HtmlLabel);
        panel.add(HtmlField);
        
        //Add the buttons
        panelButtonUp = new JPanel();
        panelButtonUp.setLayout(new GridLayout(1,2));
        panelButtonUp.add(acceptButton);
        panelButtonUp.add(cancelButton);
        panelButtonUp.setBorder(
      	      BorderFactory.createCompoundBorder(      
            		      		BorderFactory.createEmptyBorder(7,10,7,10),
            		      			panelButtonUp.getBorder()));
        
        panelButtonDown = new JPanel();
        panelButtonDown.setLayout(new BorderLayout());
        panelButtonDown.add(connectButton,  BorderLayout.NORTH);
        panelButtonDown.setBorder(
        	      BorderFactory.createCompoundBorder(      
              		      		BorderFactory.createEmptyBorder(7,7,7,7),
              		      			panelButtonDown.getBorder()));
        
        panelButton = new JPanel();
        panelButton.setLayout(new GridLayout(2,1));
        panelButton.add(panelButtonUp);
        panelButton.add(panelButtonDown);
        
        mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(panelButton, BorderLayout.CENTER);
        mainFrame.getContentPane().add(panelArea, BorderLayout.SOUTH);
        
        Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((d.width-defaultSize.width)/2,(d.height-defaultSize.height)/2);
	}
	
	
	/**
	* Cancel Action of interface
	* @param evt ActionEvent
	*/
	private void cancelAction(ActionEvent evt)
	{
		setVisible(false);
		ServerField.setText(paramServer);
		UserField.setText("");
		PasswordField.setText("");
		
		IPField.setText(IPDirection.getHostAddress());
		PortField.setText(paramPort);
		HtmlField.setText("");
		statusArea.setText("");
	}
	
	
	/**
	* Get the IP Local of the PC
	* @return InetAddress The IP Local of the master
	*/
	private InetAddress getIPLocal()
	{
		InetAddress ipLocal = null;
		try 
		{
			ipLocal = InetAddress.getLocalHost();
			try 
			{
				for(Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();ni.hasMoreElements();)
				{
				      NetworkInterface theNI = ni.nextElement();
				      for(Enumeration<InetAddress> ia = theNI.getInetAddresses();ia.hasMoreElements();)
				      {
				        InetAddress anAddress = ia.nextElement();
				        if(anAddress.isSiteLocalAddress())
				        	ipLocal = anAddress;
				        
				      }
				 }
			}catch (SocketException e) {e.printStackTrace();}
		}catch (UnknownHostException e) {e.printStackTrace();}
		
		return ipLocal;
	}
	
	
	/**
	* Accept Action of interface
	* @param evt ActionEvent
	*/
	@SuppressWarnings({ "deprecation" })
	private void acceptAction(ActionEvent evt)
	{
		if((htmlGenerated!=null) && (ListStudentsTree.getTool()!=null))
		{
			setText("A HTML page generated and a Server is Running");
			return;
		}
			
		//Add to the ArrayList
		ListFields.clear();
		
		ListFields.add(ServerField.getText());
        ListFields.add(IPField.getText());
        
        if(IPCBox.isSelected()) ListFields.add("local");
        else ListFields.add("public");
        
        ListFields.add(PortField.getText());
        ListFields.add(UserField.getText());
        ListFields.add(PasswordField.getText());
        
		if(!CheckEmptyFields(ListFields)){
			JOptionPane.showMessageDialog(mainFrame, "Some empty field", "Field Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(!PasswordField.getText().equals("ejs")){
			JOptionPane.showMessageDialog(mainFrame, "Error in the password", "Password Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
				
		/**GET PROPERTIES OF THE SIMULATION**/
		//Add the package name
		String pack = this.sim.getClass().getName();
		int index = pack.lastIndexOf('.');
		if (index>=0) pack = pack.substring(0,index);
		pack = firstToUpper(pack);
		ListFields.add(pack);
			    
		//Add the main frame
		String _mainFrame = this.sim.getView().getElements().get(1).toString();
		ListFields.add(_mainFrame);
		String dimensionFrame = null;
		
		
		//Add the dimension of the frame
		dimensionFrame = Integer.toString(this.sim.getView().getComponent(_mainFrame).getSize().width-7);
		dimensionFrame = dimensionFrame + "-";
		dimensionFrame = dimensionFrame + Integer.toString(this.sim.getView().getComponent(_mainFrame).getSize().height-15);
		ListFields.add(dimensionFrame);
			    
		/**GENERATION OF THE HTML FOR THE STUDENT**/
		//Sendind Post Petition
		statusArea.setText("Sending Petition...");
		String response = petitionPost(new DataSend(ListFields, false));
		
		if(response.equals("Error")) HtmlField.setText("No generated HTML");
		else
		{
			response=response.substring(4, response.length());//Extract the "null" string
			if(response.contains("Error"))
			{
				setText(response);
				setText("No generated HTML");
			}
			else
			{
				HtmlField.setText(response);
				HtmlField.setEnabled(true);
				htmlGenerated = response;
				if(ListStudentsTree.getTool()== null) connectButton.setEnabled(true);
				else setText("A server is just running");		
				setText("Generated HTML");
			}
		}
	}
	
	
	/**
	* Connect Action of interface
	* @param evt ActionEvent
	*/
	private void connectAction(ActionEvent evt)
	{		
		sim.setTeacherSim(true);
		sim.setStudentSim(false);
		teacherGUI = ListStudentsTree.getTool(sim, Integer.parseInt(PortField.getText()), directorname);
		teacherGUI.setVisible(true);
		connectButton.setEnabled(false);
	}
	
	protected void createStudentList(){
		sim.setTeacherSim(true);
		sim.setStudentSim(false);
		teacherGUI = ListStudentsTree.getTool(sim,Integer.parseInt(this.paramPort),directorname);
		teacherGUI.setVisible(true);
	}
	
	protected boolean getIsCollaborative(){
		return this.isCollaborative;
	}
	/**
	* Check the fields of interface
	* @param evt ActionEvent
	*/
	private boolean CheckEmptyFields(ArrayList<String> list)
	{
		int i=0;
		for (Iterator<String> it = list.iterator(); it.hasNext();)
		{
			if(i==0){
				if(it.next().equals(http)) 
					return false;
			}
			else{
				if(it.next().equals("")) 
					return false;
			}
			i++;
		}	
		return true;
	}
	
	
	/**
	* Pass the arguments a URL String
	* @param _args String[] Arguments to send at the public web server
	* @return String A String which represents the arguments in a URL
	*/
	private String dataSendtoDataPost(String[] _args)
	{
		String data=null;
		
		try 
		{
			data = URLEncoder.encode("IP","UTF-8") + "=" + URLEncoder.encode(_args[1],"UTF-8");
			data+= "&" + URLEncoder.encode("IPLP","UTF-8") + "=" + URLEncoder.encode(_args[2],"UTF-8");
			data+= "&" + URLEncoder.encode("PORT","UTF-8") + "=" + URLEncoder.encode(_args[3],"UTF-8");
			data+= "&" + URLEncoder.encode("USER","UTF-8") + "=" + URLEncoder.encode(_args[4],"UTF-8");
			data+= "&" + URLEncoder.encode("PASSWORD","UTF-8") + "=" + URLEncoder.encode(_args[5],"UTF-8");
			data+= "&" + URLEncoder.encode("PACKAGE","UTF-8") + "=" + URLEncoder.encode(_args[6],"UTF-8");
			data+= "&" + URLEncoder.encode("MAINFRAME","UTF-8") + "=" + URLEncoder.encode(_args[7],"UTF-8");
			data+= "&" + URLEncoder.encode("DIMENSION","UTF-8") + "=" + URLEncoder.encode(_args[8],"UTF-8");
		}catch (UnsupportedEncodingException e) {System.err.println(e.getMessage());}
		//System.out.println(data);
		return data;
	}
	
	//Added 13/04/2011
	/*private void ReadConfFile(){
		File myfile = new File ("../CollaborativeConf.txt");
		FileReader fr;
		BufferedReader br;
		String line = null;
		try {
			fr = new FileReader (myfile);
			br = new BufferedReader(fr);
			line = br.readLine();
			this.serverMoodle = line.substring(line.indexOf("=")+1);
			line = br.readLine();
			if(line.contains("true")) this.useMoodle = true;
			else this.useMoodle = false;
		} catch (IOException e) {e.printStackTrace();}
		//this.serverMoodle = "http://62.204.199.148/remote_laboratories_applets";
		
	}*/
	// ---------------------------
	// End Private Methods
	// ---------------------------
	
	
	
	// ---------------------------
	// Static methods
	// ---------------------------
	/**
	* Pass the first character to Upper
	* @param _name String String which will be convert
	* @return String String converted
	*/
	static private String firstToUpper (String _name) {
	    return _name.substring(0,1).toUpperCase()+_name.substring(1);
	}
	//---------------------------
	// End Static methods
	// ---------------------------
	
	
	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Set the text in the JTextArea of the interface
	* @param text String
	*/
	protected void setText(String text){
		statusArea.append("\n");
		statusArea.append(text);
	}
	
	
	/**
	* Send the Http order to the public web server
	* @param send DataSend
	* @return String Html generated response
	*/
	protected String petitionPost(DataSend send)
	{
		URL hostUrl = null;
		String html_response = null;
		
		try
		{
			//URL of PHP page
			String host = send.getHost(); 
			String dirHost = host+"/createHtmlStudent.php";//URL of PHP http://www.aurova2.ua.es/ejs/create.php
			//System.out.println(dirHost);
			hostUrl = new URL(dirHost); //Object URL of PHP page
			
			//Pass the data to URL String
			String[] _args = send.DataSendtoString();			
			String sendDataPost = dataSendtoDataPost(_args); 
			//System.out.println(sendDataPost);
			
			//Send the Http Request
			URLConnection conexion =  hostUrl.openConnection();
			
			//Open the connection
			conexion.setDoOutput(true);
			OutputStreamWriter buffersalida = new OutputStreamWriter(conexion.getOutputStream());
			
			//Write the object in the buffer
			buffersalida.write(sendDataPost);
			buffersalida.flush();
			
			//Get the response of a public web server
			BufferedReader bufferentrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
			String linea = null;
			
			while((linea=bufferentrada.readLine())!=null)
				html_response+=linea;
			
			buffersalida.close();
			bufferentrada.close();
			
		}catch(Exception e){
			setText(e.getMessage());
			System.err.println(e.getMessage());
			return "Error";
		 }
		
		return html_response;
		
	}
	

	/**
	* Set visible the interface according to visible parameter
	* @param visible boolean
	*/
	protected void setVisible(boolean visible) 
	{
		if(mainFrame==null) return;
		mainFrame.setResizable(false);
		mainFrame.setVisible(visible);
    }
	
	// ---------------------------
	// End Protected Methods
	// ---------------------------
}
