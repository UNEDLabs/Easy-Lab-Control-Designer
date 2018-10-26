package org.colos.ejs.model_elements.external;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.colos.freefem.FreeFem;
import org.colos.freefem.PDEData;
import org.colos.freefem.PDEMesh;
import org.colos.freefem.ScriptOutput;

/**
 * Encapsulates access to a FreeFem object
 * @author Francisco Esquembre
 * @version 1.0, December 2012
 *
 */
abstract public class FreeFemAdapter extends FreeFem {
  private Model mModel;

  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public FreeFemAdapter(Model model) {
    super();
    mModel = model;
  }

  /**
   * Place holder for subclasses
   * @return
   */
  abstract public String getScript();

  /**
   * Runs the script created by getScript()
   * @return
   */
  public ScriptOutput runScript() { return runScript(getScript()); }

  /**
   * Runs the script created by getScript() using the given number of processes
   * @param nProcesses
   * @return
   */
  public ScriptOutput runScript(int nProcesses) { return runScript(getScript(),nProcesses); }

  /**
   * Runs the script and returns the given output data of a given PLOT command
   * @param plotNumber
   * @param dataNumber
   * @return
   */
  public PDEData solveAndGet(int plotNumber, int dataNumber) {
    ScriptOutput output = runScript();
    if (output!=null) return output.getData(plotNumber,dataNumber);
    return null;
  }

  /**
   * Runs the script using a given number of processes and returns the given output data of a given PLOT command
   * @param nProcesses
   * @param plotNumber
   * @param dataNumber
   * @return
   */
  public PDEData solveAndGet(int nProcesses, int plotNumber, int dataNumber) {
    ScriptOutput output = runScript(nProcesses);
    if (output!=null) return output.getData(plotNumber,dataNumber);
    return null;
  }
  
  @Override
  public String getServerURL() {
    return ModelElementsUtilities.getValue(mModel,super.getServerURL());
  }

  @Override
  public String getUserName() {
    return ModelElementsUtilities.getValue(mModel,super.getUserName());
  }

  @Override
  public String getUserPassword() {
    return ModelElementsUtilities.getValue(mModel,super.getUserPassword());
  }

}
