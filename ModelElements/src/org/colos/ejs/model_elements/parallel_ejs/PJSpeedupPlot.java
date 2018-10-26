package org.colos.ejs.model_elements.parallel_ejs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JDialog;

import org.colos.ejs.library.Model;

/**
 * Records running time data and plots results.
 * After instantiation: 
 * <pre>PJSpeedupPlot plot = new PJSpeedupPlot();</pre>
 * one adds measurements in any order, such as these:
 * <pre>
 * plot.addMeasurement(20,1,1234);
 * plot.addMeasurement(21,1,2234);
 * plot.addMeasurement(20,2,834);
 * plot.addMeasurement(21,2,1234);
 * plot.addMeasurement(20,1,1334);
 * </pre>
 * and/or plot specifications (also, in any order), such as these:
 * <pre>
 * plot.addPlotSpecification("n 20 1048576 \"N = 1M\"");
 * plot.addPlotSpecification("n 21 2097152 \"N = 2M\"");
 * plot.addPlotSpecification("time rightMargin 54");
 * </pre>
 * When done, create and make visible a JDialog with the plots invoking:
 * <pre>
 * showPlot(null); // or any other parent component
 * </pre>
 * You can clear data, specs or both using the methods:
 * <pre>
 * clearMeasurements();
 * clearPlotSpecifications();
 * clear();
 * </pre>
 * respectively.
 * @author Francisco Esquembre
 * @version 1.0, August 2011
 *
 */
public class PJSpeedupPlot {
  private Hashtable<RunningData,SpeedupDataMeasurements> mData = new Hashtable<RunningData,SpeedupDataMeasurements>(); //
  private ArrayList<String> mSpeedupPlotSpecs = new ArrayList<String>(); // a list of formating commands for the speedup plot
  private ArrayList<JDialog> mDialogList = new ArrayList<JDialog>();
  
  /**
   * Adds data to the list
   * @param size the size of the problem
   * @param processors 0 means sequential run, >=1indicates the number of processors in a parallel run
   * @param time
   * @return true if the data is acceptable (and therefore is added)
   */
  public boolean addMeasurement(int size, int processors, long time) {
    if (processors<0 || time<0) return false;
    RunningData rData = RunningData.getInstance(size,processors);
    SpeedupDataMeasurements measurements = mData.get(rData);
    if (measurements==null) {
      measurements = new SpeedupDataMeasurements(rData.mSize,rData.mProcessors);
      mData.put(rData,measurements);
    }
    measurements.addTimeMeasurement(time);
    return true;
  }

  /**
   * Sets the label for a given size
   * @param size
   * @param label
   */
  public void addPlotSpecification(String plotSpec) {
    mSpeedupPlotSpecs.add(plotSpec);
  }

  /**
   * Clears all data
   */
  public void clearMeasurements() {
    for (Enumeration<SpeedupDataMeasurements> e = mData.elements(); e.hasMoreElements(); ) e.nextElement().clear();
    mData.clear();
    RunningData.clear();
  }

  /**
   * Clears all commands
   */
  public void clearPlotSpecifications() {
    mSpeedupPlotSpecs.clear();
  }

  /**
   * Clears everything
   */
  public void clear() {
    clearMeasurements();
    clearPlotSpecifications();
  }

  /**
   * Sorts and prints all data measurements through the given output.
   * @param output One of:
   *   <ul>
   *     <li>a Model (e.g. <i>printMeasurements(this);</i></li>
   *     <li>a java.io.PrintStream (e.g. <i>printMeasurements(System.err);</i></li>
   *     <li>any other object or <i>null</i> produces no output.
   *  </ul>
   */
  public void printMeasurements(Object output) {
    ArrayList<SpeedupDataMeasurements> measurements = new ArrayList<SpeedupDataMeasurements>(mData.values());
    if (output instanceof Model) {
      Model model = (Model) output;
      for (SpeedupDataMeasurements measurement : SpeedupDataMeasurements.sort(measurements)) model._println (measurement.toString());
      return;
    }
    if (output instanceof java.io.PrintStream) {
      java.io.PrintStream stream = (java.io.PrintStream) output;
      for (SpeedupDataMeasurements measurement : SpeedupDataMeasurements.sort(measurements)) stream.println (measurement);
    }
  }
  
  public void disposePlots() {
    for (JDialog dialog : mDialogList) dialog.dispose();
  }

  public JDialog showPlot(java.awt.Component parentComponent) {
    return showSpeedupPlot(parentComponent);
  }

  public JDialog showSpeedupPlot(java.awt.Component parentComponent) {
    // Create the panel with the plots 
    JComponent panel = new SpeedupAdapted().createPlots(new ArrayList<SpeedupDataMeasurements>(mData.values()),mSpeedupPlotSpecs);
    if (panel==null) return null;
    
    // Place the panel into a JDialog
    JDialog dialog = new JDialog();
    dialog.setTitle("Speedup plots");
    dialog.setModal(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.getContentPane().setLayout(new java.awt.BorderLayout());
    dialog.getContentPane().add(panel,java.awt.BorderLayout.CENTER);
    dialog.pack();
    dialog.setVisible(true);
    return dialog;
  }

  public JDialog showSizeupPlot(java.awt.Component parentComponent) {
    // Create the panel with the plots 
    JComponent panel = new SizeupAdapted().createPlots(new ArrayList<SpeedupDataMeasurements>(mData.values()),mSpeedupPlotSpecs);
    if (panel==null) return null;
    
    // Place the panel into a JDialog
    JDialog dialog = new JDialog();
    dialog.setTitle("Sizeup plots");
    dialog.setModal(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.getContentPane().setLayout(new java.awt.BorderLayout());
    dialog.getContentPane().add(panel,java.awt.BorderLayout.CENTER);
    dialog.pack();
    dialog.setVisible(true);
    return dialog;
  }
  
  /**
   * Runs a test of the given PJProblem. For it, it:
   * <ol>
   * <li>runs nTimes the sequential (K=0) problem</li> 
   * <li>runs nTimes the parallel problems (from K=1 to K=maxThreads)</li>
   * <li>accumulates data of all this</li>
   * </ol>
   * However, it does not show the plots. You must do this explicity using the showSpeedupPlot or showSizeupPlot methods. 
   * @param nTimes the number of times to run the priblems 
   * @param maxThreads th emaximum number of threads for the parallel problem
   * @param problem the problem
   * @param output One of:
   *   <ul>
   *     <li>a Model (e.g. <i>printMeasurements(this);</i></li>
   *     <li>a java.io.PrintStream (e.g. <i>printMeasurements(System.err);</i></li>
   *     <li>any other object or <i>null</i> produces no output
   *  </ul>
   */
  public void runTest (int nTimes, int maxThreads, PJProblem problem, Object output) {
    Model model = null;
    java.io.PrintStream stream = null;
    if (output instanceof Model) model = (Model) output;
    else if (output instanceof java.io.PrintStream) stream = (java.io.PrintStream) output;
    int size = problem.getSize();
    for (int i=0; i<nTimes; i++) {
      if (model!=null) model._print("Running sequential...");
      else if (stream!=null) stream.print ("Running sequential...");
      long time = problem.runSequential();
      if (model!=null) model._println("done in (ms): "+time);
      else if (stream!=null) stream.println (" done in (ms): "+time);
      addMeasurement(size,0,time);
    }
    for (int K=1; K<=maxThreads; K++) {
      for (int i=0; i<nTimes; i++) {
        if (model!=null) model._print("Running parallel on "+K+" threads...");
        else if (stream!=null) stream.print ("Running parallel on "+K+" threads...");
        long time = problem.runParallel(K);
        if (model!=null) model._println("done in (ms): "+time);
        else if (stream!=null) stream.println (" done in (ms): "+time);
        addMeasurement(size,K,time);
      }
    }
  }

  public void runTest (int nTimes, int maxThreads, PJProblem problem) {
    runTest (nTimes, maxThreads, problem, null);
  }
  
  public void runTest (Object target, int nTimes, int maxThreads) {
    runTest (target, nTimes, maxThreads, null);
  }

  public String runTest (final Object target, int nTimes, int maxThreads, Object output) {
    final Method sizeMethod = resolveMethod (target, "getSize", new Class[] { });
    if (sizeMethod==null || !sizeMethod.getReturnType().equals(int.class)) { // Check for correct return types
      return ("Incorrect or missing method: int getSize(void)");
    }
    final Method sequentialMethod = resolveMethod (target, "runSequential", new Class[] { });
    if (sequentialMethod==null || !sequentialMethod.getReturnType().equals(long.class)) {
      return ("Incorrect or missing method: long runSequential(void)");
    }
    final Method parallelMethod = resolveMethod (target, "runParallel", new Class[] { int.class });
    if (parallelMethod==null || !parallelMethod.getReturnType().equals(long.class)) {
      return ("Incorrect or missing method: long runParallel(int nThreads)");
    }
    PJProblem problem = new PJProblem() {
      public int getSize() { 
        try { return (Integer) sizeMethod.invoke(target, new Object[] {}); }
        catch (Exception exc) { exc.printStackTrace(); return -1; }
      }
      public long runSequential() {
        try { return (Long) sequentialMethod.invoke(target, new Object[] {}); }
        catch (Exception exc) { exc.printStackTrace(); return -1L; }
      }
      public long runParallel(int nThreads) {
        try { return (Long) parallelMethod.invoke(target, new Object[] { nThreads }); }
        catch (Exception exc) { exc.printStackTrace(); return -1L; }
      }
    };
    runTest(nTimes, maxThreads, problem, output);
    return null;
  }
  
  static public Method resolveMethod (Object _target, String _name, Class<?>[] _classList) {
    java.lang.reflect.Method[] allMethods = _target.getClass().getMethods();
    for (int i = 0; i < allMethods.length; i++) {
      if (!allMethods[i].getName().equals(_name)) continue;
      Class<?>[] parameters = allMethods[i].getParameterTypes();
      if (parameters.length!=_classList.length) continue;
      boolean fits=true;
      for (int j=0; j<parameters.length; j++) {
        if (!parameters[j].isAssignableFrom(_classList[j])) { fits = false; break; }
      }
      if (fits) return allMethods[i];
    }
    return null;
}

  // ----------- private classes

  static private class RunningData {
    int mSize;
    int mProcessors;
    static private ArrayList<RunningData> instanceList = new ArrayList<RunningData>();

    static RunningData getInstance(int size, int processors) {
      for (RunningData data : instanceList) {
        if (data.mSize==size && data.mProcessors==processors) return data;
      }
      RunningData data = new RunningData(size,processors);
      instanceList.add(data);
      return data;
    }
    
    static void clear() {
      instanceList.clear();
    }
    
    private RunningData (int size, int processors) {
      mSize = size;
      mProcessors = processors;
    }
    
  }

}
