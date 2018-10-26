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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.colos.ejs.library.Animation;
import org.opensourcephysics.display.OSPRuntime;


/**
 * Tree Interface of the students connected
 */

public class ListStudentsTree {
	
	//Interface controls
	JFrame mainframe;
	JMenuBar menuBar;
	JMenuItem menuItemConnect, menuItemDisconnect;
	JMenuItem menuItemGive, menuItemRemove, menuItemRemoveStudent;
	JPanel panel;
    JTree  treeStudents;
    JScrollPane scrollP;
    JTextArea txtArea;
    JScrollPane panelArea;
    DefaultTreeModel treeModel;
    
    //Protected controls
    protected static ListStudentsTree STUDENT_TREE = null;
	protected static Dimension defaultSize = new Dimension (300, 250);
	protected Dimension dim;
	protected SimulationCollaborative sim;
	protected int port;
	private String director_name = null;
	
	//Server and session
	protected ServerEJS server;
	protected SessionEJS mySession;
	
	
	
	/**
	* Constructor without parameters.
	*/
    public ListStudentsTree(){
    	this(defaultSize,null,50001,"director");
    }
    
    
    /**
    * Constructor with parameters
    * @param dim Dimension 
    * @param sim SimualationCollaborative Simulation for collaboration
    * @param port Integer port opened in the connection
    */
    public  ListStudentsTree(Dimension dim, SimulationCollaborative simulation, int port, String directorname) {
	    this.dim = dim;
	    this.sim = simulation;
	    this.port = port;
	    this.director_name = directorname;
	    try {
    	    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    	    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	}catch (Exception exc){System.err.println("Error loading L&F: " + exc);}
	    createGUI();
	    mySession = new SessionEJS(this);
	    server = OSPRuntime.appletMode ? new ServerEJS(mySession,this.port) : new ServerEJS(Animation.getThreadGroup(),mySession,this.port);
    }
    
    
    
	// ---------------------------
	// Methods to get the tool
	// ---------------------------
    /**
	* Method to get the interface
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param port int Port opened in the connection
	* @return ListStudentsTree Object Interface
	*/
	protected static ListStudentsTree getTool(SimulationCollaborative simulation, int port, String directorname) 
    {
	   if (STUDENT_TREE==null){STUDENT_TREE = new ListStudentsTree(defaultSize,simulation,port,directorname);}
	   return STUDENT_TREE;
	}

	
    /**
	* Method to get the interface
	* @param dim Dimension
	* @param sim SimualationCollaborative Simulation for collaboration
	* @param port int Port opened in the connection
	* @return ListStudentsTree Object Interface
	*/
	protected static ListStudentsTree getTool(Dimension dim,SimulationCollaborative simulation,int port, String directorname) 
	{
	   if (STUDENT_TREE==null){STUDENT_TREE = new ListStudentsTree(dim,simulation,port,directorname);}
		   return STUDENT_TREE;
	} 
	
	
    /**
	* Method to get the interface
	* @return ListStudentsTree Object Interface
	*/
	protected static ListStudentsTree getTool()
	{
		return STUDENT_TREE;
	}
	// ---------------------------
	// End Methods to get the tool
	// ---------------------------
	
	
	
	/**
	* Interface creation
	*/
    private void createGUI(){
    	
    	menuBar = performMenuBar();
    	panel = new JPanel(true);

    	mainframe = new JFrame((ConfTeacherTool.spanish)?"Lista de conectados":"List of connected people");
    	mainframe.getContentPane().add("Center", panel);
    	mainframe.setJMenuBar(menuBar);
    	mainframe.setBackground(Color.lightGray);
    	//mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
    	//Create the JTreeModel
    	DefaultMutableTreeNode root = createNewNode((ConfTeacherTool.spanish)?"Director de sesión("+this.director_name+")":"Session director("+this.director_name+")",null);
    	treeModel = new DefaultTreeModel(root);

    	//Create the tree
    	treeStudents = new JTree(treeModel);

    	//Enable tool tips for the tree, without this tool tips will not be picked up
    	ToolTipManager.sharedInstance().registerComponent(treeStudents);

    	//Make the tree use an instance of SampleTreeCellRenderer for drawing
    	treeStudents.setCellRenderer(new MyTreeCellRender());

    	//Make tree ask for the height of each row
    	treeStudents.setRowHeight(-1);

    	//Put the Tree in a scroller
    	scrollP = new JScrollPane();
    	scrollP.setPreferredSize(new Dimension(300, 250));
    	scrollP.getViewport().add(treeStudents);

    	//And show it
    	panel.setLayout(new BorderLayout());
    	panel.add("Center", scrollP);
    	panel.add("South", performTextArea());
    }
    
    
    
	// ---------------------------
    // Protected methods
	// ---------------------------
	/**
	* Hide the interface
	*/
    protected void finish(){
		mainframe.setVisible(false);
	}
	
    
    /**
	* Write a text in the JTextArea of the interface
	* @param text String Text to write
	*/
    protected void setText(String text)
    {
    	txtArea.append("\n"+text);
    }
    
    
	/**
	* Set the interface visible or not
	* @param visible boolean 
	*/
	protected void setVisible(boolean visible) 
	{
		mainframe.pack();
		mainframe.setVisible(visible);
    }
	
	
	/**
	* Get if the interface is visible
	* @return boolean True if it is visible or false otherwise
	*/
    protected boolean isVisible() 
    {
	    return mainframe.isVisible();
    }
    
    
	/**
	* Add a node to the tree interface
	* @param name String Student's name
	* @param _args[] String Arguments to configure a node
	*/
    protected void addNode(String name, String _args[]){
    	int newIndex;
    	DefaultMutableTreeNode parent;
		parent = (DefaultMutableTreeNode)treeModel.getRoot();
		newIndex = treeModel.getChildCount(parent);
		treeModel.insertNodeInto(createNewNode(name,_args),parent, newIndex);
    }
    
    
	/**
	* Remove a node of the tree interface
	* @param user String Student's name of the node to remove
	*/
    protected void removeNode(String user){
    	DefaultMutableTreeNode parent;
    	DefaultMutableTreeNode aux;
		parent = (DefaultMutableTreeNode)treeModel.getRoot();
    	for(int i=0;i<treeModel.getChildCount(parent);i++)
    	{
    		aux = (DefaultMutableTreeNode)treeModel.getChild(parent, i);
    		if(user.equals(((DataNode)aux.getUserObject()).toString()))
    		{
    			treeModel.removeNodeFromParent(aux);
    			return;
    		}	
    	}
    }
    
    
	/**
	* Method to perform the menu bar
	*/
    protected JMenuBar performMenuBar() 
    {
    	JMenu menu;
    	JMenuBar _menuBar = new JMenuBar();
    	JMenuItem menuItem;
    	

    	menu = new JMenu((ConfTeacherTool.spanish)?"Opciones":"Options");
    	_menuBar.add(menu);

    	menuItemDisconnect = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Desconectar":"Disconnect"));
    	menuItemConnect = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Conectar":"Connect"));   
    	menuItemConnect.setEnabled(false);
    	
    	menuItemDisconnect.addActionListener(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
        		disconnectItemAction(e);
        	    }});

    	menuItemConnect.addActionListener(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
        		//Re-connection the server
    	    	connectItemAction(e);
        	    }});
    	
    	
    	menuItem = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Salir":"Exit"));
    	menuItem.addActionListener(new ActionListener() {
    	    public void actionPerformed(ActionEvent e) {
    	    	//Close the frame
    	    	mainframe.setVisible(false);
    	    	
    	    	//Disconnecting the session
    	    	disconnectItemAction(e);
       
        		//Eliminate the object
        		STUDENT_TREE=null;
    	    }});
    	
    	
    	
    	//Tree related stuff
    	menu = new JMenu((ConfTeacherTool.spanish)?"Estudiantes":"Students");
    	_menuBar.add(menu);

    	menuItemRemoveStudent = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Borrar Alumno":"Remove Student"));
    	menuItemRemoveStudent.addActionListener(new RemoveAction());

    	menuItemGive = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Dar la tiza":"Give the chalk"));
    	menuItemRemove = menu.add(new JMenuItem((ConfTeacherTool.spanish)?"Quitar la tiza":"Remove the chalk"));
    	menuItemRemove.setEnabled(false); 
    	
    	menuItemGive.addActionListener(new GiveChalk(menuItemRemove,menuItemRemoveStudent));
    	menuItemRemove.addActionListener(new RemoveChalk(menuItemGive,menuItemRemoveStudent));

    	return _menuBar;
    }
    
    
	/**
	* Method to create a new node 
	* @return DefaultMutableTreeNode The node
	*/
    protected DefaultMutableTreeNode createNewNode(String name,String[] _args) {
    	return new MyTreeNode(new DataNode(null, Color.black, name),_args);
    }
    
    
	/**
	* Action of Connect the server
	* @param e ActionEvent 
	*/
    protected void connectItemAction(ActionEvent e)
    {
    	
    	//If thereIsChalk, connect the tree and the controls
    	activeTheTree();
		
    	menuItemConnect.setEnabled(false);
		menuItemDisconnect.setEnabled(true);
		sim.setTeacherSim(true);
	    mySession = new SessionEJS(this);
	   server = OSPRuntime.appletMode ? new ServerEJS(mySession,this.port) : new ServerEJS(Animation.getThreadGroup(),mySession,this.port);

    }
    
    
	/**
	* Action of disconnect the server
	* @param e ActionEvent
	*/
    protected void disconnectItemAction(ActionEvent e)
    {
    	menuItemConnect.setEnabled(true);
		menuItemDisconnect.setEnabled(false);
		server.disconnect();
		sim.setTeacherSim(false);
    }
    
    
	/**
	* Activation of the tree interface and remove of the chalk
	*/
    protected void activeTheTree(){
    	MyTreeCellRender aux = (MyTreeCellRender)treeStudents.getCellRenderer();
		aux.setChalk(false);
		treeStudents.setEnabled(true);
		
		//Active the controls. Normal situation
		menuItemRemove.setEnabled(false); 
		menuItemGive.setEnabled(true); 
		menuItemRemoveStudent.setEnabled(true);
    }
	// ---------------------------
    // End Protected methods
	// ---------------------------
    
    
    
	// ---------------------------
    // Private methods
	// ---------------------------
	/**
	* Perform the JTextArea in the interface
	* @return JScrollPane
	*/
    private JScrollPane performTextArea()
    {
        txtArea = new JTextArea(); 
          
        txtArea.setFont(new Font("Serif", Font.ITALIC, 14));
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        panelArea = new JScrollPane(txtArea);
        panelArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelArea.setPreferredSize(new Dimension(250, 150));

        panelArea.setBorder(
        		BorderFactory.createCompoundBorder(
        				BorderFactory.createCompoundBorder(
        						BorderFactory.createTitledBorder((ConfTeacherTool.spanish)?"Canal de comunicación":"Communication channel"),
        						BorderFactory.createEmptyBorder(5,5,5,5)),
        						panelArea.getBorder()));
        return panelArea;
    }
	// ---------------------------
    // End Private methods
	// ---------------------------
    
    
    
	// --------------------------------------------
    // Private classes to manage the control events
	// --------------------------------------------
    class RemoveAction implements ActionListener
    {

    	public void actionPerformed(ActionEvent e) {
    		 TreePath pathNodeSelec = treeStudents.getSelectionPath();
             if(pathNodeSelec == null) return;
             DefaultMutableTreeNode nodeSelec = (DefaultMutableTreeNode)pathNodeSelec.getLastPathComponent();
             if(nodeSelec.isRoot()) return;
             mySession.closeConnectionSession(nodeSelec.getUserObject().toString());
    	}
    }
    

    class GiveChalk implements ActionListener
    {
    	private JMenuItem remove;
    	private JMenuItem removeStudent;
    	
    	GiveChalk(JMenuItem remove, JMenuItem removeStudent){
    		this.remove = remove;
    		this.removeStudent = removeStudent;
    	}
    	/** Number of nodes that have been added. */
    	public void actionPerformed(ActionEvent e) {
    		TreePath pathNodeSelec = treeStudents.getSelectionPath();
    		if(pathNodeSelec == null) return;
    		DefaultMutableTreeNode nodeSelec = (DefaultMutableTreeNode)pathNodeSelec.getLastPathComponent();
    		if(nodeSelec.isRoot()) return;
    		mySession.giveChalkConnection(nodeSelec.getUserObject().toString());
    		remove.setEnabled(true);
    		removeStudent.setEnabled(false);
    		((JMenuItem)e.getSource()).setEnabled(false);
    		MyTreeCellRender aux = (MyTreeCellRender)treeStudents.getCellRenderer();
    		aux.setChalk(true);
    		treeStudents.setEnabled(false);
    		
    	}
	}
    
    class RemoveChalk implements ActionListener
    {
    	private JMenuItem give;
    	private JMenuItem removeStudent;
    	
    	RemoveChalk(JMenuItem give,JMenuItem removeStudent){
    		this.give = give;
    		this.removeStudent = removeStudent;
    	}
    	/** Number of nodes that have been added. */
    	public void actionPerformed(ActionEvent e) {
    		TreePath pathNodeSelec = treeStudents.getSelectionPath();
    		if(pathNodeSelec == null) return;
    		DefaultMutableTreeNode nodeSelec = (DefaultMutableTreeNode)pathNodeSelec.getLastPathComponent();
    		if(nodeSelec.isRoot()) return;
    		mySession.removeChalkConnection(nodeSelec.getUserObject().toString());
    		give.setEnabled(true);
    		removeStudent.setEnabled(true);
    		((JMenuItem)e.getSource()).setEnabled(false);
    		MyTreeCellRender aux = (MyTreeCellRender)treeStudents.getCellRenderer();
    		aux.setChalk(false);
    		treeStudents.setEnabled(true);
    	}
	}
	// ------------------------------------------------
    // End Private classes to manage the control events
	// -----------------------------------------------
    
}
