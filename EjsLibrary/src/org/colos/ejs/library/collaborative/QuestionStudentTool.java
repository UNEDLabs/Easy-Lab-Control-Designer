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
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.colos.ejs.library.Animation;
import org.opensourcephysics.display.OSPRuntime;


/**
 * Connection interface in the student applet
 */

public class QuestionStudentTool {
	
	//Actions
	private static final String strAccept = "Accept";
	private static final String strCancel = "Cancel";

	//Object and default dimension
	private static QuestionStudentTool QUESTION_TOOL=null;
	private static Dimension defaultSize = new Dimension (300, 200);

	//Dimension frame
	private Dimension dim;
	
	//Simulation Object
	protected SimulationCollaborative sim;
	
	//Swing Components
	protected JFrame mainFrame = null;
	protected JButton acceptButton;
	protected JButton cancelButton;
	protected JLabel question, userLabel;
	protected JTextField userField;
	protected JPanel panel,panelUser;
    protected JTextArea statusArea;
    protected JScrollPane panelArea;
    
    //Connection Parameters
	protected ArrayList<String> _listParam = new ArrayList<String>();
    
    
    //Thread connection
    protected Receiver client;
    protected Vector<DataSocket> vector = new Vector<DataSocket>(); 
    
    //Added **12/04/2011
    protected String username = null; 
    private boolean isCollaborative = false; 
    protected static boolean spanish = false; 
    private boolean connected = false;
    javax.swing.Timer timer = null;
    private int connections = 1;
    
   /**
   * Constructor.
   */
    public QuestionStudentTool() {
	    this(defaultSize,null,null);
	}

    
    /**
    * Constructor with parameters
    * @param dim Dimension 
    * @param sim SimualationCollaborative Simulation for collaboration
    * @param _listP ArrayList List of parameters to connect with the master
    */
	public QuestionStudentTool(Dimension dim, SimulationCollaborative sim, ArrayList<String> _listP) {
	    this.dim = dim;
	    this.sim = sim;
	    this._listParam = _listP;
	    try{
	    	if(sim.getModel()._getParameter("is_collaborative")!=null && sim.getModel()._getParameter("is_collaborative").equals("true")) this.isCollaborative = true;
	    	if(this.isCollaborative){
	    		if(sim.getModel()._getParameter("username")!=null) this.username = sim.getModel()._getParameter("username");
	    		else this.username = "carlos";
	    		if(sim.getModel()._getParameter("language")!=null){
	    			if(sim.getModel()._getParameter("language").equals("es")) spanish = true;
	    		}
	    	}
	    }catch(Exception e){System.err.println((spanish)?"No hay parámetros para el applet":"Null Student Applet Parameters");}
	    //if(!this.isCollaborative) createGUI();
    }
	
	
	
	// ---------------------------
	// Methods to get the tool
	// ---------------------------
	/**
	* Method to get the interface
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param _listP ArrayList List of parameters to connect with the master
	* @return QuestionStudentTool Object Interface
	*/
	protected static QuestionStudentTool getTool(SimulationCollaborative sim, ArrayList<String> _listP) 
    {
	   if (QUESTION_TOOL==null){QUESTION_TOOL = new QuestionStudentTool(defaultSize,sim,_listP);}
	   return QUESTION_TOOL;
	}

	
	/**
	* Method to get the interface
	* @param dim Dimension
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param _listP ArrayList List of parameters to connect with the master
	* @return QuestionStudentTool Object Interface
	*/
	protected static QuestionStudentTool getTool(Dimension dim, SimulationCollaborative sim, ArrayList<String> _listP) 
	{
	   if (QUESTION_TOOL==null) QUESTION_TOOL = new QuestionStudentTool(dim,sim,_listP);
		   return QUESTION_TOOL;
	} 
	// ---------------------------
	// End Methods to get the tool
	// ---------------------------
	
	
	
	/**
	* Interface creation
	*/
	protected void createGUI() 
	{
		//Frame
		JFrame.setDefaultLookAndFeelDecorated(false);
		mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(this.dim);
        mainFrame.setResizable(false);
        
        //Buttons
        acceptButton = new JButton("Accept");
        acceptButton.setHorizontalAlignment(JButton.CENTER);
        acceptButton.setActionCommand(strAccept);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setHorizontalAlignment(JButton.CENTER);
        cancelButton.setActionCommand(strCancel);
		cancelButton.setEnabled(false);
        
        //Labels
        question = new JLabel();
        question.setForeground(new Color (255,0,0));
        question.setFont(new Font("Times", Font.ITALIC, 13));
        question.setText("Do you want to take a virtual class?");
        question.setHorizontalAlignment(JLabel.CENTER);
        userLabel = new JLabel("User");
        userLabel.setHorizontalAlignment(JLabel.CENTER);
        //TextField
        userField = new JTextField();
        
        //Panel and Layout
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        //Add the controls to the panel
        panel.add(question, BorderLayout.CENTER);
        
        //PanelUser
        panelUser = new JPanel();
        
        panelUser.setLayout(new GridLayout(2,2));
        panelUser.add(userLabel);
        panelUser.add(userField);
        panelUser.add(acceptButton);
        panelUser.add(cancelButton);
        panelUser.setBorder(
        	      BorderFactory.createCompoundBorder(      
              		      		BorderFactory.createEmptyBorder(10,10,10,10),
              		      			panelUser.getBorder()));
        
        //Text Area
        statusArea = new JTextArea();
        statusArea.setFont(new Font("Courier", Font.BOLD, 10));
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        
        //ScrollPane
        panelArea = new JScrollPane(statusArea);
        panelArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelArea.setPreferredSize(new Dimension(150,80));
        panelArea.setBorder(
  	      BorderFactory.createCompoundBorder(
    		       BorderFactory.createCompoundBorder(
      		         BorderFactory.createTitledBorder("Communication channel"),
        		      BorderFactory.createEmptyBorder(5,5,5,5)),
          		 panelArea.getBorder()));
       
        //Control Listener
        MyActionListener myListener = new MyActionListener(mainFrame,statusArea,userField);
        acceptButton.addActionListener(myListener);
        cancelButton.addActionListener(myListener);
        
        mainFrame.getContentPane().add(panel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(panelUser, BorderLayout.CENTER);
        mainFrame.getContentPane().add(panelArea, BorderLayout.SOUTH);
        
        Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((d.width-defaultSize.width)/2,(d.height-defaultSize.height)/2);
	}

	
	// ---------------------------
	// Protected Methods
	// ---------------------------
	/**
	* Set the text in the JTextArea of the interface
	* @param text String
	*/
	protected void setText(String text){
		if(mainFrame==null){
			System.err.println(text);
			return;
		}
		statusArea.append("\n");
		statusArea.append(text);
	}
	
	protected boolean isThereInterface(){
		if(mainFrame==null) return false;
		return true;
	}
	
	
	/**
	* Get the Client Thread
	* @return Receiver The Thread receiver
	*/
	protected Receiver getReceiver(){
		return client;
	}
	
	
	/**
	* Set visible the interface
	* @param boolean True: visible the interface, False: not visible the interface
	*/
	protected void setVisible(boolean visible) 
	{
		if(mainFrame==null) return;
		mainFrame.setVisible(visible);
    }
	// ---------------------------
	// End Protected Methods
	// ---------------------------
	
	
	
	// --------------------------------------------
    // Private classes to manage the control events
	// --------------------------------------------
	private class MyActionListener implements ActionListener
	{
	    JTextArea _panelArea;
	    String identification = null;
	    
	    MyActionListener(JFrame frame, JTextArea area, JTextField userField){
	    	this._panelArea = area;
	    }
	    public void actionPerformed(ActionEvent e) 
	    {
	    	String action = e.getActionCommand();
	    	if(action.equals(QuestionStudentTool.strAccept)){
	    		if((userField.getText().equals(""))){
	    			JOptionPane.showMessageDialog(mainFrame,"Student name","Error in the identification", JOptionPane.ERROR_MESSAGE);
	   			}
	    		else{
    				_panelArea.setText("Connecting with the teacher..");
    				acceptButton.setEnabled(false);
    				cancelButton.setEnabled(true);
    				userField.setEditable(false);
    				identification = userField.getText();
    				sim.disconnectControls();
    				client = OSPRuntime.appletMode ? new Receiver(vector, identification, QuestionStudentTool.QUESTION_TOOL) : new Receiver(Animation.getThreadGroup(),vector, identification, QuestionStudentTool.QUESTION_TOOL);

    				client.connect(_listParam.get(0), Integer.valueOf(_listParam.get(1)));
    				System.out.println(_listParam.get(0)+ " "+Integer.valueOf(_listParam.get(1)));
    				client.setPriority(Thread.MAX_PRIORITY);
    				client.start();
	   			}
	   		}
	   		else if(action.equals(QuestionStudentTool.strCancel)){
	   			_panelArea.append("\nDisconnecting...");
    			client.disconnect();
    			userField.setEditable(true);
				cancelButton.setEnabled(false);
				acceptButton.setEnabled(true);
				//Chalk
				if(sim.getChalk()) sim.setChalk(false);
				sim.connectControls();
	    	}
	   	}
	}
	// --------------------------------------------
    // Private classes to manage the control events
	// --------------------------------------------
	
	//Moodle implementation
	protected boolean getIsCollaborative(){
		return this.isCollaborative;
	}
	
	protected void initReceiver(){
		//client = new Receiver(vector, username, QuestionStudentTool.QUESTION_TOOL);
		if(timer==null){
			timer = new javax.swing.Timer(4000, new ActionListener(){
			
			public void actionPerformed(ActionEvent e){	
			try{
			if(!connected){
        client = OSPRuntime.appletMode ? new Receiver(vector, username, QuestionStudentTool.QUESTION_TOOL) : new Receiver(Animation.getThreadGroup(),vector, username, QuestionStudentTool.QUESTION_TOOL);

				boolean state = client.connect(_listParam.get(0), Integer.valueOf(_listParam.get(1)));
				System.out.println(_listParam.get(0)+ " "+Integer.valueOf(_listParam.get(1))+" "+connections);
				if(state){
					sim.disconnectControls();
					client.setPriority(Thread.MAX_PRIORITY);
					client.setActive(true); 
					client.start();
					connections = 1;
					JOptionPane.showMessageDialog(null, (spanish)?"Conectado a la sesión colaborativa\nCierre la ventana para continuar":"Connected to the collab session\nClose the window to continue", (spanish)?"Información":"Information", JOptionPane.INFORMATION_MESSAGE);
				}
				else{ 
					if(client.getActive()) client.setActive(false); 
					sim.connectControls();
					connections++;
					if(connections>15){ stopTimer(); connections = 1;} 
				}
				connected = state;
			}
			}catch(Exception ep){ep.printStackTrace();}
		}});
		timer.start();
		}
		
	}
	
	protected void stopTimer(){
		if(timer!=null){
			timer.stop();
			System.out.println((spanish)?"El temporizador ha expirado":"The timer has expired");
		}
	}
	
	protected void setStateConnection(boolean _state){
		this.connected = _state;
	}
	
	protected void finishReceiver(){
		if(client!=null){
			client.disconnect();
			sim.connectControls();
		}
	}
	
}
