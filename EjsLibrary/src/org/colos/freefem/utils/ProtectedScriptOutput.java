package org.colos.freefem.utils;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author María José Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.util.ArrayList;

/**
 * ScriptOutput encapsulates the result of successfully running a FreeFem script.
 */
public class ProtectedScriptOutput {
  
  protected ArrayList<PlotOutput> mList = new ArrayList<PlotOutput>();
  protected boolean mIsWaiting = false;
  protected Object mCommObject;
  private boolean differentEndianness = true;
  
  protected ProtectedScriptOutput(Object commObject) { 
    mCommObject = commObject; 
  }
  
  protected void setWaiting(boolean waiting) { mIsWaiting = waiting; }
  
  protected void addPlotOutput(PlotOutput plot) { mList.add(plot); }
  
  protected Object getCommObject() { return mCommObject; }
  
  protected boolean getDiffEndianness() { return differentEndianness; }
  
  protected void setHasDifferentEndianness(boolean hasDifferentEndianness) { differentEndianness = hasDifferentEndianness; }
  
}
