package org.opensourcephysics.tools;

import java.awt.Component;
import java.util.List;
import java.util.ArrayList;

import javax.swing.WindowConstants;

import org.opensourcephysics.display.Data;
import org.opensourcephysics.frames.ArrayFrame;

public class ToolForDataFull extends ToolForData {
  static private DataTool dataTool=null; // single instance
//  static private FourierTool fourierTool=null; // single instance

  /**
   * Creates a list of all children Data with actual 2D data,
   * excluding groups of data with no real data
   * @param _data
   * @return
   */
  static private List<Data> getRealData2D (Data... _data) {
    ArrayList<Data> selfContained = new ArrayList<Data>();
    for (Data next: _data) selfContained.addAll(DataTool.getSelfContainedData(next));
    return selfContained;
  }

  static private String[] completeColumnNames(String[] names) {
    if (names==null) return new String [] { "#", "1", "2", "3" };
    int n = names.length;
    String[] newNames = new String[n+1];
    newNames[0] = "#";
    System.arraycopy(names, 0, newNames, 1, n);
    return newNames;
  }
  
  @Override
  public boolean isFullTool() { return true; }

  @Override
  public Object showTable (Component _component, Data... _data) {
    List<Data> realData = getRealData2D(_data);
    int size = realData.size();
    if (size<=0) return null;
    ArrayFrame frame;
    if (size==1) {
      Data firstData = realData.get(0);
      frame = new ArrayFrame(firstData.getData2D(),firstData.getName());
      frame.setColumnNames(completeColumnNames(firstData.getColumnNames()));
    }
    else {
      double[][][] doubleArray = new double[size][][];
      for (int i=0; i<size; i++) doubleArray[i] = realData.get(i).getData2D();
      frame = new ArrayFrame(doubleArray);
      String[][] allColumnNames = new String[size][];
      for (int i=0; i<size; i++) allColumnNames[i] = completeColumnNames(realData.get(i).getColumnNames());
      frame.setColumnNames(allColumnNames);
    }
//    frame.setRowNumberVisible(false);
    frame.setTransposed(true);
    frame.setNumericFormat("0.000");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setLocationRelativeTo(_component);
    frame.setKeepHidden(false);
    frame.setVisible(true);
    return frame;
  }

  @Override
  public Object showDataTool (Component _component, java.util.List<Data> _data) {
    if (dataTool==null) {
      dataTool = new DataTool();
      dataTool.setKeepHidden(false);
    }
    if (_data!=null) for (Data oneData : _data) dataTool.loadData(oneData);
    if (!dataTool.isVisible()) {
      dataTool.setLocationRelativeTo(_component);
      dataTool.setVisible(true);
    }
    return dataTool;
  }

  @Override
  public Object showDataTool (Component _component, Data... _data) {
    if (dataTool==null) {
      dataTool = new DataTool();
      dataTool.setKeepHidden(false);
    }
    if (_data!=null) dataTool.loadData(_data);
    if (!dataTool.isVisible()) {
      dataTool.setLocationRelativeTo(_component);
      dataTool.setVisible(true);
    }
    return dataTool;
  }

  @Override
  public void clearDataTool () {
    if (dataTool!=null) dataTool.clearData();
  }

  @Override
  public Object getDataTool() { 
    if (dataTool==null) {
      dataTool = new DataTool();
      dataTool.setKeepHidden(false);
    }
    return dataTool; 
  }
  
  @Override
  public String openData(Component _component, String _filename) { 
    if (dataTool==null) {
      dataTool = new DataTool();
      dataTool.setKeepHidden(false);
    }
    String value = dataTool.open(_filename);
    if (!dataTool.isVisible()) {
      dataTool.setLocationRelativeTo(_component);
      dataTool.setVisible(true);
    }
    return value;
  }
  
//  @Override
//  public Object showFourierTool (Component _component, java.util.List<Data> _data) {
//    if (fourierTool==null) {
//      fourierTool = new FourierTool();
//      fourierTool.setKeepHidden(false);
//    }
//    if (_data!=null) for (Data oneData : _data) fourierTool.loadData(oneData);
//    if (!fourierTool.isVisible()) {
//      fourierTool.setLocationRelativeTo(_component);
//      fourierTool.setVisible(true);
//    }
//    return fourierTool;
//  }
//
//  @Override
//  public Object showFourierTool (Component _component, Data... _data) {
//    if (fourierTool==null) {
//      fourierTool = new FourierTool();
//      fourierTool.setKeepHidden(false);
//    }
//    if (_data!=null) fourierTool.loadData(_data);
//    if (!fourierTool.isVisible()) {
//      fourierTool.setLocationRelativeTo(_component);
//      fourierTool.setVisible(true);
//    }
//    return fourierTool;
//  }
//
//  @Override
//  public void clearFourierTool () {
//    if (fourierTool!=null) fourierTool.clearData();
//  }
//  
//  @Override
//  public Object getFourierTool() { 
//    if (fourierTool==null) {
//      fourierTool = new FourierTool();
//      fourierTool.setKeepHidden(false);
//    }
//    return fourierTool;
//  }
//
//  @Override
//  public String openFourierData(Component _component, String _filename) { 
//    if (fourierTool==null) {
//      fourierTool = new FourierTool();
//      fourierTool.setKeepHidden(false);
//    }
//    String value = fourierTool.open(_filename);
//    if (!fourierTool.isVisible()) {
//      fourierTool.setLocationRelativeTo(_component);
//      fourierTool.setVisible(true);
//    }
//    return value;
//  }

}
