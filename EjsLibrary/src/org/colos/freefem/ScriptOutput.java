package org.colos.freefem;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author María José Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.colos.freefem.utils.PlotOutput;
import org.colos.freefem.utils.ProtectedScriptOutput;

/**
 * ScriptOutput encapsulates the result of successfully running a FreeFem script.
 */
public class ScriptOutput extends ProtectedScriptOutput {
  
  /**
   * Constructor. Not to be used directly
   * @param commObject
   */
  public ScriptOutput(Object commObject) {
    super(commObject); 
  }
  
  /**
   * Whether the output is waiting because of a wait=1 clause 
   * @return
   */
  public boolean isWaiting() { return mIsWaiting; }

  /**
   * Cleans the list of plotOutputs
   * @return
   */
  public void clear() { mList.clear(); }
  
  /**
   * Close any pending connection
   */
  public void close() {
    if (mCommObject instanceof DataInputStream) {
      try { ((DataInputStream) mCommObject).close(); } 
      catch (IOException e) { }
    }
    else if (mCommObject instanceof Socket) { 
      try { ((Socket) mCommObject).close(); } 
      catch (IOException e) { }
    }
  }
  
  /**
   * The number of PLOT commands found in the script
   * @return
   */
  public int getPlotCount(){
    return mList.size();
  }
  
  /**
   * Return the number of Data objects produced by a given PLOT command
   * @param plotNumber
   * @return
   */
  public int getDataCount(int plotNumber) {
    if (plotNumber<0 || plotNumber>=mList.size()) return 0;
    return mList.get(plotNumber).getDataCount();
  }
  
  /**
   * Gets a given PlotOutput object produced 
   * @param plotNumber
   * @return
   */
  public PlotOutput getPlot(int plotNumber){
    if (plotNumber<0 || plotNumber>=mList.size()) return null;
    return mList.get(plotNumber);
  }
  
  /**
   * Gets a given Data object produced by the given PLOT command 
   * @param plotNumber
   * @param dataNumber
   * @return
   */
  public PDEData getData(int plotNumber, int dataNumber){
    if (plotNumber<0 || plotNumber>=mList.size()) return null;
    return mList.get(plotNumber).getData(dataNumber);
  }
  
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    int plotCount = getPlotCount();
    buffer.append("Output has " + plotCount + " plots.\n");   
    for (int i=0; i<plotCount; i++) {
      PlotOutput plot = mList.get(i);
      int dataCount = plot.getDataCount();
      buffer.append("  - Plot #" + i + " has " + dataCount +" data sets.\n");
      for (int j=0; j<dataCount; j++) {
        PDEData data = plot.getData(j);
        PDEData.DataType type = data.getType();
        buffer.append("    + Data #"+j+" of Plot #"+i+" has type " + type+".\n");
      }
    }
    return buffer.toString();
  }
  
}
