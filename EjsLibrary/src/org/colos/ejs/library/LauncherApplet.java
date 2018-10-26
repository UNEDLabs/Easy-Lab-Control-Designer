/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import java.awt.*;
import javax.swing.*;
import org.opensourcephysics.display.DrawingFrame;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * A utility class to launch simulations as applets
 */
public abstract class LauncherApplet extends JApplet {
  private static final long serialVersionUID = 1L;

  private JFrame _parentFrame=null;
  private java.awt.Component mainComponent=null;

  public Model _model=null;
  public Simulation _simulation=null;
  public View _view=null;

  static { 
    org.opensourcephysics.display.OSPRuntime.appletMode = true;
  }

  public LauncherApplet () {
    super();
    org.opensourcephysics.display.OSPRuntime.applet = this;
  }

  // ------------- Utility methods -----------------


  public void init () {
    super.init();
//    String localeParameter = this.getParameter("locale");
//    System.err.println ("locale param = "+localeParameter);
//      try {
//        String language=null, country=null;
//        java.util.StringTokenizer tkn = new java.util.StringTokenizer(localeParameter," _");
//        if (tkn.hasMoreTokens()) language = tkn.nextToken();
//        if (tkn.hasMoreTokens()) country = tkn.nextToken();
//        if (language!=null) {
//          LocaleItem.getLocaleItem(_language);
//          Locale locale = (country==null) ? new Locale(language) : new Locale(language,country);
//       System.err.println ("Setting locale to "+locale);
//          Locale.setDefault(locale);
//          JComponent.setDefaultLocale(locale);
//          _simulation.setLocale(localeParameter);
//        }
//      }
//      catch (Exception exc) { } //exc.printStackTrace(); }
//    }
    String bqParameter = this.getParameter("bqServer");
    if (bqParameter!=null) ResourceLoader.addSearchPath(bqParameter);
    String lookAndFeel = this.getParameter("lookandfeel");
    if (lookAndFeel!=null) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win") || osName.contains("nix") || osName.contains("nux") || osName.indexOf("aix")>0) {
            boolean decorated = true;
            if (this.getParameter("not_decorated") != null) decorated = false;
            org.opensourcephysics.display.OSPRuntime.setLookAndFeel(decorated, lookAndFeel);
        }
    }
  }
  

  public void _play() { _simulation.play(); }
  public void _pause() { _simulation.pause(); }
  public void _step() { _simulation.step(); }
  public void _setFPS(int _fps) { _simulation.setFPS(_fps); }
  public void _setDelay(int _delay) { _simulation.setDelay(_delay); }

  public void _reset() { _model._reset(); }
  public void _initialize() { _model._initialize(); }

  public boolean _isPlaying() { return _simulation.isPlaying(); }
  public boolean _isPaused()  { return _simulation.isPaused(); }

  public void _setParentComponent (String _parent) { _simulation.setParentComponent (_parent); }
  public boolean _saveImage (String _filename, String _element) { return _simulation.saveImage (_filename, _element); }
  public boolean _saveState (String _filename) { return _simulation.saveState (_filename); }
  public boolean _saveVariables (String _filename, String _varList) { return _simulation.saveVariables (_filename,_varList); }
  public boolean _saveVariables (String _filename, java.util.List<String> _varList) { return _simulation.saveVariables (_filename,_varList); }
  public boolean _saveText (String _filename, String _text) { return _simulation.saveText (_filename, _text); }
  public boolean _saveText (String _filename, String _annotation, String _text) { return _simulation.saveText (_filename, _annotation, _text); }
  public boolean _saveText (String _filename, StringBuffer _text) { return _simulation.saveText (_filename, _text); }
  public boolean _readState (String _filename) { return _simulation.readState (_filename); }
  public boolean _readVariables (String _filename,String _varList) { return _simulation.readVariables (_filename,(java.net.URL) null,_varList); }
  public boolean _readVariables (String _filename,java.util.List<String>  _varList) { return _simulation.readVariables (_filename,getCodeBase(),_varList); }
  public String  _readText (String _filename) { return _simulation.readText (_filename); }
  public String  _readText (String _filename, String _type) { return _simulation.readText (_filename, _type); }
  public boolean _setVariables (String _command, String _delim, String _arrayDelim) { return _simulation.setVariables (_command,_delim,_arrayDelim); }
  public boolean _setVariables (String _command) { return _simulation.setVariables (_command); }
  public String _getVariables (String _varName) { return _simulation.getVariable (_varName); }
  public Object _getUserData (String _name) {return _model.getUserData (_name);}

  public void _clearView() { if (_view!=null) _view.initialize(); }
  public void _resetView() { if (_view!=null) { _view.reset(); _view.initialize(); } }


  public void _alert(String _panel, String _title, String _message) { _model._alert (_panel, _title, _message); }
  public void _print(String _txt)   { if (_view!=null) _view.print(_txt);   else System.out.print (_txt); }
  public void _println(String _txt) { if (_view!=null) _view.println(_txt); else System.out.print (_txt); }
  public void _println() { if (_view!=null) _view.println(); else System.out.println (); }
  public void _clearMessages() { if (_view!=null) _view.clearMessages(); }

  public String _format(double _value, String _pattern) {
    java.text.DecimalFormat _tmp_format = new java.text.DecimalFormat (_pattern);
    return _tmp_format.format(_value);
  }

  // ------ 

  protected java.awt.Frame getParentFrame() {
    java.awt.Container parent = getParent();
    while (parent!=null) {
      if (parent instanceof java.awt.Frame) return (java.awt.Frame) parent;
      parent = parent.getParent();
    }
    return null;
  }

  public Component captureWindow (Model _aModel, String _aWindow){
    if (_aWindow==null) return null;
    RootPaneContainer root;
    if (_parentFrame!=null) root = _parentFrame;
    else root = this;
    Component comp = _aModel._getView().getComponent(_aWindow);
    if (comp==null) return null;
//    Dimension size = comp.getSize();
    if (comp instanceof DrawingFrame) {
      comp.setVisible (true);
      Container contentPane = ((RootPaneContainer) comp).getContentPane ();
      contentPane.setVisible (true);
      root.setContentPane(contentPane);
      Component glassPane = ((RootPaneContainer) comp).getGlassPane ();
      root.setGlassPane (glassPane);
      glassPane.setVisible (true);
      ((DrawingFrame) comp).setKeepHidden (true);
      ((DrawingFrame) comp).setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
    }
    else if (comp instanceof JDialog) {
      comp.setVisible (true);
      Container contentPane = ((RootPaneContainer) comp).getContentPane ();
      contentPane.setVisible (true);
      root.setContentPane(contentPane);
      Component glassPane = ((RootPaneContainer) comp).getGlassPane ();
      root.setGlassPane (glassPane);
      glassPane.setVisible (true);
      ((JDialog) comp).dispose();
    }
    else {
      root.getContentPane().setLayout (new java.awt.BorderLayout());
      root.getContentPane().add(comp,java.awt.BorderLayout.CENTER);
      root.getContentPane().validate();
      Container oldParent = comp.getParent();
      if (oldParent!=null) oldParent.validate();
    }
    if (_parentFrame!=null) _parentFrame.pack();
    mainComponent = root.getContentPane();
    _aModel._getSimulation().setParentComponent(mainComponent);
    return mainComponent;
  }

  public java.awt.Component _getMainComponent() { return mainComponent; }

//*** FKH 20060326 for applet mode: _saveImage or saveState
  private byte imageByteArray[]=null;
  public void setImageByteArray(byte b[]){
    imageByteArray=b;
  }
//  public byte[] getImageByteArray(){//IE would not work
//    return imageByteArray;
//  }
  private String byteArray2String(byte b[]){
   if(b.length<1)return "";
   String s=""+b[0];
   for(int i=1;i<b.length;i++){
     s+="_"+b[i];
   }
   return s;
  }
  public String imageByteData(){
    return byteArray2String(imageByteArray);
  }
  private byte stateByteArray[]=null;
  public void setStateByteArray(byte b[]){
    stateByteArray=b;
  }
//  public byte[] getStateByteArray(){// netscape can accept bytearray, but IE can not
//    return stateByteArray;
//  }
  public String stateByteData(){//
    return byteArray2String(stateByteArray);
  }
  public String frameName(){// testing (did not return top frame in applet mode???
    return _getMainComponent().getName();
  }
  //***END FKH 20060415

} // End of class

