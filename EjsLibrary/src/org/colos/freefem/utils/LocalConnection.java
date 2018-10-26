package org.colos.freefem.utils;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import org.colos.freefem.FreeFem;
import org.colos.freefem.ScriptOutput;

public class LocalConnection extends Connection {	

//  @Override
//  public void setOutputToScriptDirectory(boolean doIt) { 
//    super.setOutputToScriptDirectory(doIt); 
//    try { // write the script to a temporary file and open the output file
//      File edpFile = File.createTempFile("FreeFem","script.edp");
//      setScriptDirectory(edpFile.getParentFile());
//      edpFile.delete();
//    } catch (Exception exc) {
//      exc.printStackTrace();
//      setErrorCode(FreeFem.ErrorCode.INPUT_OUTPUT_ERROR);
//    }
//  }

  /**
   * Determines if OS is Unix like
   *
   * @return true if Mac of linux
   */
  static public boolean isUnixLike() {
    try {                                                                        // system properties may not be readable in some environments
      String osName = System.getProperty("os.name", "").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
      if (osName.startsWith("mac")) return true; //$NON-NLS-1$
      if (osName.startsWith("linux")) return true; //$NON-NLS-1$
    } catch (SecurityException ex) { }
    return false;
  }


  @Override
  public ScriptOutput runScript(String script, int nProcessors) {
    File edpFile = null;
    File outputFile = null;
    try { // write the script to a temporary file and open the output file
      edpFile = File.createTempFile("FreeFem","script.edp",FreeFem.getTempDirectory());
      edpFile.deleteOnExit();
      setScriptDirectory(edpFile.getParentFile());
      BufferedWriter out = new BufferedWriter(new FileWriter(edpFile));
      out.write(script);
      out.close();
      outputFile = File.createTempFile("FreeFem","output",FreeFem.getTempDirectory());
      outputFile.deleteOnExit();
//      System.out.println("Temp directory is "+FreeFem.getTempDirectory());
//      System.out.println("Script on "+edpFile.getAbsolutePath());
//      System.out.println("Output on "+outputFile.getAbsolutePath());
    } catch (Exception exc) {
      exc.printStackTrace();
      setErrorCode(FreeFem.ErrorCode.INPUT_OUTPUT_ERROR);
      return null;
    }

    try { // run the local program
      String command = "FreeFem++";
      if (LocalConnection.isUnixLike()) {
    	  String fullCommand = "/usr/local/bin/" + command;
    	  if ((new File(fullCommand)).exists()) command = fullCommand; 
      }
      if(this.getOutputToScriptDirectory()){
        command += " -cd -nowait -fglut "+outputFile +" "+ edpFile;
//        System.out.println(command);
      }
      else command += " -nowait -fglut "+outputFile +" "+ edpFile;
//      System.err.println (command);
      Process a = Runtime.getRuntime().exec(command);
// Works on Windows XP      BufferedReader in = new BufferedReader(new InputStreamReader(a.getInputStream())); 
//      String line = null; 
//      while ((line = in.readLine()) != null) { 
//          System.out.println(line); 
//      }  
     a.waitFor();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      setErrorCode(FreeFem.ErrorCode.CONNECTION_ERROR);
      return null;
    }

    DataInputStream dis = null;
    try {
      dis = new DataInputStream(new FileInputStream(outputFile));
    }
    catch (Exception exc) {
      exc.printStackTrace();
      setErrorCode(FreeFem.ErrorCode.INPUT_OUTPUT_ERROR);
      return null;
    }	

    // Read file header
    byte[] firstlinearray = new byte[16];
    try {
      dis.readFully(firstlinearray);
      String firstline = new String(firstlinearray);
      //		   if (FreeFem.DEBUG) System.out.println(firstline);
      if (!firstline.contains(Visudata.FFFILEMAGIC)) {
        //		     if (FreeFem.DEBUG) System.err.println("Error! different version of ffglut");
        dis.close();
        setErrorCode(FreeFem.ErrorCode.INCORRECT_VERSION_ERROR);
        return null;
      }
    }
    catch (Exception exc) {
      exc.printStackTrace();
      setErrorCode(FreeFem.ErrorCode.FIRST_LINE_ERROR);
      return null;
    } 

    ScriptOutput output = new ScriptOutput(dis);
    output.setHasDifferentEndianness(true);
    edpFile.delete();
    outputFile.delete();
    return continueReading(output); // Now read the rest
    // read blocks from the pipe containing different plots
  }

  @Override
  public ScriptOutput continueReading(ScriptOutput output) {
    DataInputStream dis = (DataInputStream) output.getCommObject();
    try {
      while (dis.available() != 0) {
        PlotOutput plotOutput = new PlotOutput();
        if (!Visudata.readBlock(dis, plotOutput, output.getDiffEndianness())) {
          setErrorCode(FreeFem.ErrorCode.PROCESSING_INPUT_ERROR);
          return null;
        }
        output.addPlotOutput(plotOutput);
        if (plotOutput.wantsToPause()) {
          //System.out.println("WANTS TO PAUSE");
          output.setWaiting(true);
          break;
        }
        
      }
    if(dis.available()==0){
        dis.close();
        output.setWaiting(false);
    }
      return output;
    } catch (Exception exc) {
      exc.printStackTrace();
      setErrorCode(FreeFem.ErrorCode.PROCESSING_INPUT_ERROR);
      return null;
    }
  }

}

