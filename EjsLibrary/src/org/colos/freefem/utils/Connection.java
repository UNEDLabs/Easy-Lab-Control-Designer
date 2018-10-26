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

import java.io.File;

import org.colos.freefem.ScriptOutput;
import org.colos.freefem.FreeFem.ErrorCode;

/**
 * Abstract class used to run the scripts
 */
public abstract class Connection {
  
  private ErrorCode mErrorCode = ErrorCode.NO_ERROR;
  private boolean mUseScriptDirectory=false;
  private File mScriptDirectory = null;
  
  /**
   * Sets userDirectoryChanged to true 
   */
  public void setOutputToScriptDirectory(boolean doIt) { mUseScriptDirectory = doIt; }
  
  public boolean getOutputToScriptDirectory() { return mUseScriptDirectory; }
  
  protected void setScriptDirectory(File directory) { mScriptDirectory = directory; }
  
  public File getLastScriptDirectory() { return mScriptDirectory; }
  
  /**
   * Clears the error code
   */
  public void clearError() { mErrorCode = ErrorCode.NO_ERROR; }

  /**
   * Sets the error code to a given error
   * @param error
   */
  void setErrorCode(ErrorCode error) { mErrorCode = error; }
  
  /**
   * Returns the error code
   * @return
   */
  public ErrorCode getErrorCode() { return mErrorCode; }

  /**
   * Runs the script using a number of processors
   * @param script the script to run
   * @param nProcessors the number of processors to use 
   * @return
   */
  abstract public ScriptOutput runScript(String script, int nProcessors);

  /**
   * Continues reading a paused script output
   * @return
   */
  abstract public ScriptOutput continueReading(ScriptOutput output);
  
}

