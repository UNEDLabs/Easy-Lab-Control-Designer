/**
 * The package contains classes to generate an applet collaborative
 * Copyright (c) Dec 2007 C. Jara and F. Candelas
 * @author C. Jara and F. Candelas (http://www.aurova.ua.es).
 */

package org.colos.ejs.library.collaborative;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


import org.colos.ejs.library.Experiment;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.ControlElement;


/**
 * A base class for a simulation collaborative
 */

public class SimulationCollaborative extends Simulation {
	
	 private ConfTeacherTool teacherTool = null;
	 private QuestionStudentTool studentTool = null;
	 private ArrayList<String> _listCol = new ArrayList<String>();
	 private boolean teacher = false;
	 private boolean isThereTeacher = false;
	 private boolean student = false;
	 private boolean chalk = false;
	 private boolean isThereChalk = false;
	 private ArrayList<Integer> arrayRequest = new ArrayList<Integer>();
	 private int counter_req = 0;
	 private ExperimentCollaborative currentExp = new ExperimentCollaborative(null);
	 private ArrayList<String> _listExpS = new ArrayList<String>();
	 //private JMenu experimentCollaborative;
	 private boolean isPlayingCol = false;
	 private String paramServer,paramPort;
	 
	 private javax.swing.Timer timerUpdate = null;
	 private boolean update150 = false;
	 private boolean waitChalk150 = false;
	 private javax.swing.Timer timerResponse = null;
	
	// ---------------------------
    // Actions for collaboration
	// ---------------------------
	
	public void actionPerformed(ActionEvent evt) {
		//super.actionPerformed(evt);
		setIsPlaying(true);
		
		/*if(timerUpdate == null){
			timerUpdate = new javax.swing.Timer(150, 
			  new ActionListener(){
			     public void actionPerformed(ActionEvent e)
				 {
			          update150 = true;
			     }});
			timerUpdate.start();
		}
		else timerUpdate.restart();*/
		
		//Code for collaboration
		if(teacher && !currentExp.isAliveThread()){
	    	teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,null,"play",null));
	    	teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,getVariables(),"expCol",null));
	    }	
	}
	
	public void pause() {
		super.pause();
		setIsPlaying(false);
		
		if(timerUpdate != null) timerUpdate.stop();

		//Code for collaboration
		if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,null,"pause",null));}
	    if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,null,"pause",null));} catch (IOException e) {}}
	}
	
	
	 public synchronized void reset(){
		super.reset();

		//Code for collaboration
	    if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,null,"reset",null));}
	    if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,null,"reset",null));} catch (IOException e) {}}
	}
	
	public void initialize() {
		super.initialize();

		//Code for collaboration
		if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,null,"initialize",null));}
	    if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,null,"initialize",null));} catch (IOException e) {}}
	}
	
	public void apply (String _variable) {
		super.apply(_variable);

		//Code for collaboration
	    if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,_variable,"variable",getVariable(_variable).toString()));}
	    if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,_variable,"variable",getVariable(_variable).toString()));} catch (IOException e) {}}
	}
	
	
	public void update() {
		super.update();

		//Code for collaboration
		if(!isPlayingCol || update150) 
		{
			if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,getVariables(),"updateAfterModelAction",null));}
			if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,getVariables(),"updateAfterModelAction",null));} catch (IOException e) {}}
			update150 = false;
		}
	}
	
	
	public void step() {
		//long t_student = System.currentTimeMillis();
		//Code for collaboration
		//Teacher
		if(teacher && !currentExp.isAliveThread()){
			try {
				for(int i=0;i<teacherTool.teacherGUI.mySession.getSizeChildren();i++)
				{
					if(!teacherTool.teacherGUI.mySession.getChildrenChalk(i)){
						teacherTool.teacherGUI.mySession.socketUDP.send(teacherTool.teacherGUI.mySession.stepSendList.get(i));
					}		
				}
			}catch (IOException e) {e.printStackTrace();}
		}
		
		//Chalk
		if(chalk && !currentExp.isAliveThread()){
			try {studentTool.getReceiver().getUDP().send(studentTool.getReceiver().getDataUDPChalk());
			}catch (IOException e) {e.printStackTrace();}
		}

		//Call superclass step
		super.step();
		//long ti = System.currentTimeMillis();
		
		//Teacher without chalk
		if(teacher && !isThereChalk && !currentExp.isAliveThread()){
			try {
				
				try {
					teacherTool.teacherGUI.mySession.socketUDP.setSoTimeout(150);
				} catch (SocketException e) {}
			
				 while (arrayRequest.size()<(teacherTool.teacherGUI.mySession.getSizeChildren())){ 
					 teacherTool.teacherGUI.mySession.socketUDP.receive(teacherTool.teacherGUI.mySession.stepReceive);
					 if(!arrayRequest.contains(counter_req)){
		  					arrayRequest.add(counter_req);
		  					counter_req++;
		  			  }
		  		  }
		  	  }catch (IOException e) {}
		  	  counter_req = 0;
		  	  arrayRequest.clear();
		  	  // System.out.println((System.currentTimeMillis()-ti)+ " milisec.");
		}
		
		//Teacher with chalk
		if(teacher && isThereChalk && !currentExp.isAliveThread()){
			try {
				try {
					teacherTool.teacherGUI.mySession.socketUDP.setSoTimeout(150);
				} catch (SocketException e) {}
				 while (arrayRequest.size()<(teacherTool.teacherGUI.mySession.getSizeChildren()-1)){
					 teacherTool.teacherGUI.mySession.socketUDP.receive(teacherTool.teacherGUI.mySession.stepReceive);
						 if(!arrayRequest.contains(counter_req)){
		  					arrayRequest.add(counter_req);
		  					counter_req++;
		  				 }
		  			}
				 }catch (IOException e) {e.printStackTrace();}
				 //Send response to chalk student
		  		 try {teacherTool.teacherGUI.mySession.socketUDP.send(teacherTool.teacherGUI.mySession.stepSendChalk);}catch (IOException e) {}
		  		 counter_req = 0;
		  		 arrayRequest.clear();
		  		 //System.out.println((System.currentTimeMillis()-ti)+ " milisec.");				
		}
		
		
		//Chalk
		if(chalk && !currentExp.isAliveThread()){
			//Timer
			if(timerResponse == null){
				timerResponse = new javax.swing.Timer(300, 
					new ActionListener(){
						public void actionPerformed(ActionEvent e)
						{
							waitChalk150 = true;
						}});
				timerResponse.start();
			}
			else timerResponse.restart();
		
			while(!studentTool.getReceiver().getStepOk() && !waitChalk150){
				System.out.println((QuestionStudentTool.spanish)?"Esperando...":"Waiting...");
			}
			studentTool.getReceiver().setStepOk(false);
	  		//Carlos 13/02/2008***
	  		waitChalk150 = false;
	  		timerResponse.stop();
	  		//System.out.println((System.currentTimeMillis()-ti)+ " milisec.");
	  		//Carlos 13/02/2008***
		}
		
		//if(student)
		//	System.out.println((System.currentTimeMillis()-t_student)+ " milisec step.");
			
		
	}
	
	
	public void updateAfterModelAction(){
		super.updateAfterModelAction();
		
		//Code for collaboration
		if(teacher && !currentExp.isAliveThread()){teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,getVariables(),"updateAfterModelAction",null));}
		if(chalk && !currentExp.isAliveThread()){try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,getVariables(),"updateAfterModelAction",null));} catch (IOException e) {}}
	}
	
	
	//Run experiment
	public void runExperiment (Experiment _experiment) {
		super.runExperiment(_experiment);
		
		currentExp.setExperiment(currentExperiment);
		
		 if(teacher){
			  String classExp = _experiment.getClass().toString();
			  char numExp = classExp.charAt(classExp.length()-6);
			  classExp = "experiment"+numExp;
			  teacherTool.teacherGUI.mySession.addBuffer(new DataSocket(null,classExp,"experiment",null));
		 }
		 if(chalk){
			  String classExp = _experiment.getClass().toString();
			  char numExp = classExp.charAt(classExp.length()-6);
			  classExp = "experiment"+numExp;
			  try {studentTool.getReceiver().getOutput().writeObject(new DataSocket(null,classExp,"experiment",null));} catch (IOException e) {}
		 }
	}
	// ---------------------------
    // End Actions for collaboration
	// ---------------------------
	
	
	
	// ---------------------------
    // Extra Actions
	// ---------------------------
	
	protected void extraAction2(){
		/*String label = null;
		if (!isThereTeacher) label = ejsRes.getString("MenuItem.CollaborativeTool");
		else label = ejsRes.getString("MenuItem.CollaborativeClass");
		 popupMenu.add(new AbstractAction(label){
	         public void actionPerformed(ActionEvent e) 
	         {
	         	if(!isThereTeacher) startConfTeacherTool(); 
	         	else startQuestionStudentTool();
	         }
	       });
		 popupMenu.addSeparator();*/
	}
	
	
	
	protected void extraAction3(){
		/*if(!this._listExpS.isEmpty() && experimentCollaborative==null)
	    {
	    	experimentCollaborative = new JMenu(ejsRes.getString("MenuItem.CollaborativeExperiments"));
	    	for(int i=0;i<_listExpS.size();i++){
	    		final int _expCol = i;
	    		AbstractAction action = new AbstractAction("Experiment "+Integer.toString(i)) {
	                public void actionPerformed (ActionEvent evt) {
	              	  runExperimentCol(_expCol);
	              	  }
	            };
	            experimentCollaborative.add(action);
	    	}
	    	popupMenu.addSeparator();
	    	popupMenu.add(experimentCollaborative);
	    }*/
	}
	// ---------------------------
    // End Extra Actions
	// ---------------------------
	
	
	
	// ---------------------------
    // Own methods
	// ---------------------------
	 public void startColMethod(){
		if(!isThereTeacher) startConfTeacherTool(); 
     	else startQuestionStudentTool();
	 }
	 
	 public void startColMoodle(){
		 if(!isThereTeacher){ //Teacher option
			 if(teacherTool==null){
				 teacherTool = ConfTeacherTool.getTool(this,paramServer,paramPort);//Pass the simulation to manage it
				 if(teacherTool.getIsCollaborative()) teacherTool.createStudentList();
				 else teacherTool=null;
			 }
		 }
		 if(this.student){   //Student option
			 if(studentTool==null){
				 studentTool = QuestionStudentTool.getTool(this,_listCol);
				 if(studentTool.getIsCollaborative()) studentTool.initReceiver();
				 else studentTool = null; 
			 }
		 }		         
	 }
	 
	 
	 
	 public boolean startConfTeacherTool(){
		  if(teacherTool==null) teacherTool = ConfTeacherTool.getTool(this,paramServer,paramPort);//Pass the simulation to manage it
		  if(!teacherTool.getIsCollaborative()) {
			  teacherTool.createGUI();
			  teacherTool.setVisible(true);
		  }
		  return true;
	  }
	  
	 public void runExperimentCol(int i){
		 String variables = this._listExpS.get(i);
		 this.reset();
		 this.setVariables(variables);
		 this.play();
	 }
	 
	 public void setExperimentCol(String _exp){
		 this._listExpS.add(_exp);
	 }
	  
	 public boolean startQuestionStudentTool(){
		 if(studentTool==null) studentTool = QuestionStudentTool.getTool(this,_listCol);
		 if(!studentTool.getIsCollaborative()) {
			 studentTool.createGUI();
			 studentTool.setVisible(true);
		 }
		 return true; 
	 }
	 
	 public void disconnectControls(){
		  ControlElement element;
		  
		  for (Enumeration<?> e = getView().getElements().elements() ; e.hasMoreElements();){
			  element = (ControlElement) e.nextElement();
			  element.setActive(false);
			  element.setProperty("enabled", "false");
		  }
		  this.getView().update();
	  }
	  
	  public void connectControls(){
		  ControlElement element;
		  
		  for (Enumeration<?> e = getView().getElements().elements() ; e.hasMoreElements();){
			  element = (ControlElement) e.nextElement();
			  element.setActive(true);
			  element.setProperty("enabled", "true");
		  }
		  this.getView().update();
	  }
	  
	  public void setTeacherSim(boolean value){
		  this.teacher = value;
	  }
	  
	  public void setStudentSim(boolean value){
		  this.student = value;
	  }
	  
	  public boolean getStudentSim() { return this.student; }
	  
	  public void setIsPlaying(boolean value){
		 this.isPlayingCol = value;
	  }
	  
	  public boolean getIsPlaying(){
		  return this.isPlayingCol;
	  }
	  
	  public boolean getChalk(){
		  return this.chalk;
	  }
	  
	  public void setChalk(boolean value){
		  this.chalk = value;
	  }
	  public void setIsThereChalk(boolean value){
		  this.isThereChalk = value;
	  }
	  
	  public void setIsThereTeacher(boolean value)
	  {
		  this.isThereTeacher = value;
	  }
	  
	  public void setArgsCol(String _argsCol[])
	  {
		  for(int i=0;i<_argsCol.length;i++)
			  _listCol.add(_argsCol[i]);
	  }
	  
	  public void setParamDialog(String paramServerP,String paramPortP){
		  this.paramServer = paramServerP;
		  this.paramPort = paramPortP;
	  }
	  
	  public String getVariables()
	  {
		  if (getModel()==null) return null;
		  String variable_list = "";
		  String _arraySep=",";
		  String _sep=";";
		  int l=0,j=0;

		  try{
			  java.lang.reflect.Field[] fields = getModel().getClass().getFields();  
			  for (int i=4; i<fields.length; i++) 
			  {
				  if(i>4){
					  if (fields[i].getType().isArray()) {
						  Object array = fields[i].get(getModel());
						  l = Array.getLength(array);
						  for (j=0; j<l; j++) {
							  if (j>0){
								  if(Array.get(array,j)==null)
									  variable_list +=_arraySep+"null";
								  else
									  variable_list +=_arraySep+Array.get(array,j).toString();
							  }
							  else{
								  if(Array.get(array,j)==null)
									  variable_list +=_sep+fields[i].getName()+"=null";
								  else
									  variable_list +=_sep+fields[i].getName()+"="+Array.get(array,j).toString();
							  }
						  }
						  
					  }
					  else if(fields[i].getType().toString().equals("class java.lang.Object")){
						  if(fields[i].get(getModel())==null)
						     variable_list +=_sep+fields[i].getName()+"=null";
						  else if(fields[i].get(getModel()).toString().equals("[]"))
							  variable_list +=_sep+fields[i].getName()+"=null";
						  else
							  variable_list +=_sep+fields[i].getName()+"="+fields[i].get(getModel()).toString();
					  }
					  else
					      variable_list +=_sep+fields[i].getName()+"="+fields[i].get(getModel()).toString();
					  
				  }
				  else{
					  if (fields[i].getType().isArray()) {
						  Object array = fields[i].get(getModel());
						  l = Array.getLength(array);
						  for (j=0; j<l; j++) {
							  if (j>0) variable_list +=_arraySep+Array.get(array,j).toString();
							  else variable_list +=fields[i].getName()+"="+Array.get(array,j).toString();
						  }
					  }
					  else
						  variable_list +=fields[i].getName()+"="+fields[i].get(getModel()).toString();
				  }
			  }
		  }catch (Exception exc) {/*exc.printStackTrace(System.err);*/ return null;}
		  
		  return variable_list;
	  }
	// ---------------------------
	// End Own methods
	// ---------------------------

}
