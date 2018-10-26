
/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import org.colos.ejs.osejs.*;
import org.opensourcephysics.display.OSPRuntime;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * A class to run generated applications as a separate Thread
 */
public class GeneratedUtil implements Runnable {
  static private ResourceUtil res = new ResourceUtil("Resources");

  private Osejs ejs;
  private Metadata metadata;
  private String simulationName;

  public GeneratedUtil (Osejs _ejs) {
    ejs = _ejs;
    // Read the metadata file
    File metadataFile = _ejs.getCurrentMetadataFile();
    File generationDir = metadataFile.getParentFile();
    String relativePath = FileUtils.getRelativePath(generationDir, _ejs.getOutputDirectory(), false); 
    simulationName = org.colos.ejs.library.utils.FileUtils.getPlainName(metadataFile);
    metadata = Metadata.readFile(metadataFile, relativePath);
  }

  public void run () {
    if (metadata==null) {
      String[] message=new String[]{res.getString("Generate.SimulationCanNotRun"),res.getString("Package.IncorrectMetadata")};
      JOptionPane.showMessageDialog(ejs.getMainPanel(),message,res.getString("Osejs.File.Error"),JOptionPane.WARNING_MESSAGE);
      return;
    }
    GeneratedOutput generatedOutput = null, generatedError = null;
    Process proc=null;
    try {
      ejs.getOutputArea().println(res.getString("Generate.RunningSimulation") + " "+simulationName + "...");

      final Vector<String> cmd = new Vector<String>();
      String javaHome = System.getProperty("java.home");
      if (javaHome != null) cmd.add(javaHome + java.io.File.separator + "bin" + java.io.File.separator+"java");
      else cmd.add("java");
      cmd.add("-Dosp_ejs=true");
      //      if (OSPRuntime.isMac()) cmd.add("-Xdock:name="+simulationName);
      cmd.add("-Dscreen="+OsejsCommon.getScreenNumber(ejs.getMainFrame()));
      //if (JFrame.isDefaultLookAndFeelDecorated()) cmd.add("-Dosp_defaultLookAndFeel=true");
      //cmd.add("-Dosp_lookAndFeel="+ejs.getLookAndFeel());
      String vmOptions = ejs.getOptions().vmOptions();
      if (vmOptions.length()>0) {
        StringTokenizer tkn = new StringTokenizer(vmOptions," ");
        while (tkn.hasMoreTokens()) {
          String token = tkn.nextToken();
          if (token!=null) cmd.add(token);
        }
      }
      cmd.add("-classpath");
      cmd.add(metadata.getExecpath()); //+File.pathSeparator+FileUtils.getPath(ejs.getBinDirectory())+"lookAndFeel.jar");
      cmd.add(metadata.getClassname()); //ejs.getExecutionClassname());
      NamedLookAndFeel lookAndFeel = ejs.getOptions().getRunningLookAndFeel();
      //      System.out.println ("Name ="+lookAndFeel.getName());
      if (lookAndFeel!=null && lookAndFeel.getName()!=OSPRuntime.DEFAULT_LF) {
        //        System.out.println ("classname ="+lookAndFeel.getClassname());
        //        lookAndFeel = "com.digitprop.tonic.TonicLookAndFeel";
        cmd.add("-_lookAndFeel");
        cmd.add(lookAndFeel.getClassname());
      }

      org.opensourcephysics.display.OSPRuntime.setLauncherMode(true);
      // This seems to produce a null command!!! maybe not ???
      //String[] cmdarray = (String[]) cmd.toArray(new String[0]);
      int nonNulls = 0;
      for (int i=0, n=cmd.size(); i<n; i++) { // count the number of non null arguments. There should be none!
        if (cmd.get(i)!=null) nonNulls++;
        else {
          System.out.println ("EJS warning: Element "+i+" of the command is null!");
          System.out.println ("Commands are:");
          for (int j=0; j<cmd.size(); j++) System.out.println ("Command ["+j+"] = "+cmd.get(j));
        }
      }
      String[] cmdarray = new String[nonNulls];
      for (int i=0, n=cmd.size(), counter=-1; i<n; i++) {
        if (cmd.get(i)!=null) cmdarray[++counter] = cmd.get(i);
      }
      //for (int i=0; i<nonNulls; i++) System.out.println ("Trying to run [i] = "+cmdarray[i]);
      //System.out.println ("Source dir = "+ejs.getSourceDirectory());
      proc = Runtime.getRuntime().exec(cmdarray, null, ejs.getOutputDirectory()); // ejs.getSourceDirectory()); //getCurrentHTMLFile().getParentFile());

      ejs.setRunning(simulationName,proc,true);
      generatedOutput = new GeneratedOutput(proc, false);
      Thread thread = new Thread(generatedOutput);
      thread.setPriority(java.lang.Thread.MIN_PRIORITY);
      thread.start();
      generatedError = new GeneratedOutput(proc, true);
      Thread thread2 = new Thread(generatedError);
      thread2.setPriority(java.lang.Thread.MIN_PRIORITY);
      thread2.start();
      int error = proc.waitFor();
      generatedOutput.stop();
      generatedError.stop();
      ejs.setRunning(simulationName,proc,false);
      if (error == 0) {
        ejs.getOutputArea().println(res.getString("Generate.SimulationRunsOK"));
        // ejs.getOutputArea().println(res.getString("Generate.HTMLIsReady")); // + " " + command + ".html");
      }
      else
        ejs.getOutputArea().println(res.getString("Generate.SimulationDoesNotRun")+" error ="+error);
    } catch (Exception exc) {
      exc.printStackTrace();
      ejs.getOutputArea().println(res.getString("Generate.SimulationDoesNotRun"));
      if (generatedOutput!=null) generatedOutput.stop();
      if (generatedError!=null) generatedError.stop();
      if (proc!=null) ejs.setRunning(simulationName,proc,false);
    }
  }

  static public void openBrowser(final Osejs ejs, File htmlFile) {
    String url = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(htmlFile));
    openBrowser(ejs, url);
  }
  
  static public void openBrowser(final Osejs ejs, final String url) {
    SwingUtilities.invokeLater(new Runnable () {
      public void run() {
        String previewCommand = ejs.getOptions().getPreviewCommand();
        boolean done = false; 
        if (previewCommand.length()>0) {
          try {
            String command = previewCommand + " "+ url;
            //              System.out.println ("Running command : "+command);
            String [] args = command.split("\"");
            //              for (int i=0; i<args.length; i++) System.out.println ("Running command : "+args[i]);
            java.util.ArrayList<String> list = new java.util.ArrayList<String> ();
            for (int i=0; i<args.length; i++) {
              if (i%2==0) {
                String[] pieces = args[i].split(" ");
                for (int j=0; j<pieces.length; j++) if (pieces[j].trim().length()>0) list.add(pieces[j]);
              }
              else if (args[i].trim().length()>0) list.add(args[i]);
            }
            String[] arguments = list.toArray(new String[list.size()]);
            //              for (int i=0; i<arguments.length; i++) System.out.println ("Running arguments : <"+arguments[i]+">");
            Runtime.getRuntime().exec(arguments,null,ejs.getOutputDirectory());
            done = true;
          } catch (IOException e) {
            e.printStackTrace();
            done = false;
          }
        }
        if (!done) org.opensourcephysics.desktop.OSPDesktop.displayURL(url);
      }
    });
  }

} // End of class



