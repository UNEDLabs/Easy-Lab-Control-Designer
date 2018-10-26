package org.opensourcephysics.tools;

import java.awt.Component;
import javax.swing.JOptionPane;
import org.colos.ejs.library.Simulation;
import org.opensourcephysics.display.Data;

public class ToolForData {

  static private ToolForData toolForData = new ToolForData();

  static public void setTool(ToolForData _tool) { toolForData = _tool; }
  
  static public ToolForData getTool() { return toolForData; }

  public boolean isFullTool() { return false; }
  
  public Object showTable (Component _component, Data... _data) { 
    JOptionPane.showMessageDialog(_component,Simulation.ejsRes.getString("DataInformation.NotAvailable"), 
        Simulation.ejsRes.getString("Error"), JOptionPane.ERROR_MESSAGE);
    return null; 
  }

  final public Object showTable (Data... _data) {
    return showTable(null,_data);
  }

  public Object showDataTool (Component _component, java.util.List<Data> _data) { 
    JOptionPane.showMessageDialog(_component,Simulation.ejsRes.getString("DataInformation.NotAvailable"), 
        Simulation.ejsRes.getString("Error"), JOptionPane.ERROR_MESSAGE);
    return null; 
  }

  public Object showDataTool (Component _component, Data... _data) { 
    JOptionPane.showMessageDialog(_component,Simulation.ejsRes.getString("DataInformation.NotAvailable"), 
        Simulation.ejsRes.getString("Error"), JOptionPane.ERROR_MESSAGE);
    return null; 
  }

  final public Object showDataTool (Data... _data) {
    return showDataTool(null,_data);
  }
  
  public void clearDataTool () {}

  public Object getDataTool() { return null; }
  
  public String openData(String _filename) { return openData(null, _filename); }
  
  public String openData(Component _component, String _filename) { return null; }
  
//  public Object showFourierTool (Component _component, java.util.List<Data> _data) { 
//    JOptionPane.showMessageDialog(_component,Simulation.ejsRes.getString("DataInformation.NotAvailable"), 
//        Simulation.ejsRes.getString("Error"), JOptionPane.ERROR_MESSAGE);
//    return null; 
//  }
//
//  public Object showFourierTool (Component _component, Data... _data) { 
//    JOptionPane.showMessageDialog(_component,Simulation.ejsRes.getString("DataInformation.NotAvailable"), 
//        Simulation.ejsRes.getString("Error"), JOptionPane.ERROR_MESSAGE);
//    return null; 
//  }
//
//  final public Object showFourierTool (Data... _data) {
//    return showFourierTool(null,_data);
//  }
//  
//  public void clearFourierTool () {}
//  
//  public Object getFourierTool() { return null; }
//  
//  public String openFourierData(String _filename) { return openFourierData(null, _filename); }
//  
//  public String openFourierData(Component _component, String _filename) { return null; }

}
