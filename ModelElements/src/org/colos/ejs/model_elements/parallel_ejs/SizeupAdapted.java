package org.colos.ejs.model_elements.parallel_ejs;

import edu.rit.numeric.Interpolation;
import edu.rit.numeric.ListXYSeries;

import edu.rit.numeric.plot.Dots;
import edu.rit.numeric.plot.Plot;
import edu.rit.swing.DisplayablePanel;

import java.awt.Color;
import java.awt.GridLayout;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author  Alan Kaminsky
 * @version 20-Sep-2008
 * @version Aug 2011 Adaptation by Paco from Kaminsky's original Sizeup.java
 */
public class SizeupAdapted {

  // For printing metrics.
  static private DecimalFormat FMT_0  = new DecimalFormat ("0");
  static private DecimalFormat FMT_0E = new DecimalFormat ("0E0");
  static private DecimalFormat FMT_1  = new DecimalFormat ("0.0");
  //	static private DecimalFormat FMT_2  = new DecimalFormat ("0.00");
  //	static private DecimalFormat FMT_3  = new DecimalFormat ("0.000");

  // Mapping from number of processors K (type Integer) to problem size vs.
  // running time data (type ListXYSeries). The X values are the running
  // times. The Y values are the problem sizes.
  private Map<Integer,ListXYSeries> nvstMap =	new TreeMap<Integer,ListXYSeries>();

  // Mapping from number of processors K (type Integer) to problem size vs.
  // running time interpolation object (type Interpolation). The X values are
  // the running times. The Y values are the problem sizes.
  private Map<Integer,Interpolation> interpolationMap =	new TreeMap<Integer,Interpolation>();

  // Mapping from problem size specification n (type Integer) to problem size
  // N (type Double).
  private Map<Integer,Double> ntoNMap = new TreeMap<Integer,Double>();

  // List of T values (msec) (type Double).
  private List<Double> tList = new ArrayList<Double>();

  // Maximum K value.
  private int K_max = Integer.MIN_VALUE;

  private int nCurves = 5;

  // Plot objects.
  private Plot NvsT_plot = new Plot();
  private Plot N_plot = new Plot();
  private Plot Sizeup_plot = new Plot();
  private Plot SizeupEff_plot = new Plot();

  /**
   * Does the processing and creates the plots in a single panel
   * @param sortedMeasurements a list of (correctly ordered) measurements. For each problem size, the measurements must be ordered by ascending K 
   * @param plotSpec a series of plot specifications
   * @return a
   */
  public JComponent createPlots(java.util.List<SpeedupDataMeasurements> measurements, java.util.List<String> plotSpec) {
    try { 
      // Set up plots with default attributes.
      NvsT_plot
      .plotTitle ("Problem Size vs. Running Time")
      .rightMargin (84)
      .minorGridLines (true)
      .xAxisKind (Plot.LOGARITHMIC)
      .xAxisMinorDivisions (10)
      .xAxisTickScale (1000)
      .xAxisTickFormat (FMT_0E)
      .xAxisTitle ("Running time, <I>T</I> (sec)")
      .yAxisKind (Plot.LOGARITHMIC)
      .yAxisMinorDivisions (10)
      .yAxisTickFormat (FMT_0E)
      .yAxisTitle ("Problem size, <I>N</I>")
      .labelPosition (Plot.ABOVE+Plot.ROTATE_LEFT)
      .labelOffset (6);
      N_plot
      .plotTitle ("Problem Size vs. Processors")
      .rightMargin (84)
      .minorGridLines (true)
      .xAxisKind (Plot.LOGARITHMIC)
      .xAxisMinorDivisions (10)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisKind (Plot.LOGARITHMIC)
      .yAxisMinorDivisions (10)
      .yAxisTickFormat (FMT_0E)
      .yAxisTitle ("<I>N</I> (<I>T,K</I>)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);
      Sizeup_plot
      .plotTitle ("Sizeup vs. Processors")
      .rightMargin (84)
      .xAxisStart (0)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisStart (0)
      .yAxisTickFormat (FMT_0)
      .yAxisTitle ("<I>Sizeup</I> (<I>T,K</I>)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);
      SizeupEff_plot
      .plotTitle ("Sizeup Efficiency vs. Processors")
      .rightMargin (84)
      .xAxisStart (0)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisStart (0)
      .yAxisTickFormat (FMT_1)
      .yAxisTitle ("<I>SizeupEff</I> (<I>T,K</I>)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);

      // Process the data
      double minT_min = Double.POSITIVE_INFINITY;
      double maxT_min = Double.NEGATIVE_INFINITY;
      for (SpeedupDataMeasurements measurement : SpeedupDataMeasurements.sort(measurements)) {
        int n = measurement.getProblemSize();
        int K = measurement.getNumberOfProcessors();
        double T_min = Double.POSITIVE_INFINITY;
        //	    double T_max = Double.NEGATIVE_INFINITY;
        K_max = Math.max (K_max, K);
        for (Long T : measurement.getTimeMeasurements()) {
          T_min = Math.min (T_min, T);
          //	      T_max = Math.max (T_max, T);
        }
        minT_min = Math.min (minT_min, T_min);
        maxT_min = Math.max (maxT_min, T_min);

        // Record data.
        ListXYSeries nvst = nvstMap.get (K);
        if (nvst == null)
        {
          nvst = new ListXYSeries();
          nvstMap.put (K, nvst);
        }
        nvst.add (T_min, n);
      }

      // Add ideal performance lines to plots.
      Sizeup_plot
      .seriesDots (null)
      .seriesColor (new Color (0.7f, 0.7f, 0.7f))
      .xySeries (new double[] {0, K_max}, new double[] {0, K_max})
      .seriesDots (Dots.circle (5))
      .seriesColor (Color.black);
      SizeupEff_plot
      .seriesDots (null)
      .seriesColor (new Color (0.7f, 0.7f, 0.7f))
      .xySeries (new double[] {0, K_max}, new double[] {1, 1})
      .seriesDots (Dots.circle (5))
      .seriesColor (Color.black);

      for (int i=0; i<nCurves; i++) {
        double T = minT_min + i*(maxT_min-minT_min)/(nCurves-1);
        tList.add (T);
      }
      // Process the labels

      for (String command : plotSpec) processCommand(command);

      // Process data for each K value.
      //		System.out.println ("K\tT\tN");
      for (Map.Entry<Integer,ListXYSeries> entry : nvstMap.entrySet())
      {
        //			System.out.println();
        Integer K = entry.getKey();
        //	    			String Kstring = K == 0 ? "seq" : K.toString();
        ListXYSeries nvst = entry.getValue();

        // Convert n vs. T to N vs. T.
        ListXYSeries Nvst = new ListXYSeries();
        for (int i = 0; i < nvst.length(); ++ i)
        {
          double T = nvst.x(i);
          int n = (int) nvst.y(i);
          Double N = ntoNMap.get (n);
          if (N == null)
          {
            Nvst.add (T, n);
            //	        					System.out.println
            //	        						(Kstring + "\t" +
            //	        						 FMT_0.format (T) + "\t" +
            //	        						 n);
          }
          else
          {
            Nvst.add (T, N);
            //	        					System.out.println
            //	        						(Kstring + "\t" +
            //	        						 FMT_0.format (T) + "\t" +
            //	        						 FMT_0.format (N));
          }
        }

        // Add N vs. T series to plot.
        if (K >= 1)
        {
          NvsT_plot
          .xySeries (Nvst)
          .label
          ("<I>K</I> = " + K,
              Nvst.x (Nvst.length()-1),
              Nvst.y (Nvst.length()-1));
        }

        // Set up interpolation objects.
        interpolationMap.put (K, new Interpolation (Nvst));
      }

      // Process data for each T value.
      //		System.out.println();
      //		System.out.println ("T\tK\tN\tSizeup\tSzEff");
      for (double T : tList)
      {

        // Calculate and print metrics.
        double N = 0.0;
        double N_seq = 0.0;
        double sizeup = 0.0;
        double sizeupeff = 0.0;
        ListXYSeries nvskSeries = new ListXYSeries();
        ListXYSeries sizeupvskSeries = new ListXYSeries();
        ListXYSeries sizeupeffvskSeries = new ListXYSeries();
        for (int K = 0; K <= K_max; ++ K)
        {
          Interpolation interp = interpolationMap.get (K);
          if (interp == null)
          {
          }
          else if (K == 0)
          {
            N = interp.f (T);
            N_seq = N;
            //					System.out.println
            //						(FMT_0.format (T) + "\t" +
            //						 K1 + "\t" +
            //						 FMT_0.format (N) + "\t\t");
          }
          else
          {
            N = interp.f (T);
//            System.err.println ("Interpolation for T="+T+ " = "+N);
            if (!Double.isNaN(N)) {
              if ((Double.isNaN(N_seq) || N_seq == 0.0) && K == 1) N_seq = N;
              //	          System.err.println ("Adding K="+K+", N="+N);
              sizeup = N / N_seq;
              sizeupeff = sizeup / K;
              nvskSeries.add (K, N);
              sizeupvskSeries.add (K, sizeup);
              sizeupeffvskSeries.add (K, sizeupeff);
            }
            //					System.out.println
            //						(FMT_0.format (T) + "\t" +
            //						 K1 + "\t" +
            //						 FMT_0.format (N) + "\t" +
            //						 FMT_3.format (sizeup) + "\t" +
            //						 FMT_3.format (sizeupeff));
          }
        }

        // Add data series to plots.
        try {
          N_plot
          .xySeries (nvskSeries)
          .label
          ("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
              nvskSeries.x (nvskSeries.length()-1),
              nvskSeries.y (nvskSeries.length()-1));
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
        try {
          Sizeup_plot
          .xySeries (sizeupvskSeries)
          .label
          ("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
              sizeupvskSeries.x (sizeupvskSeries.length()-1),
              sizeupvskSeries.y (sizeupvskSeries.length()-1));
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
        try {
          SizeupEff_plot
          .xySeries (sizeupeffvskSeries)
          .label
          ("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
              sizeupeffvskSeries.x (sizeupeffvskSeries.length()-1),
              sizeupeffvskSeries.y (sizeupeffvskSeries.length()-1));
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
      }

      //		// Display plots.
      //		NvsT_plot.getFrame().setVisible (true);
      //		N_plot.getFrame().setVisible (true);
      //		Sizeup_plot.getFrame().setVisible (true);
      //		SizeupEff_plot.getFrame().setVisible (true);

      DisplayablePanel NvsT_panel = new DisplayablePanel (NvsT_plot);
      DisplayablePanel N_panel = new DisplayablePanel (N_plot);
      DisplayablePanel Sizeup_panel = new DisplayablePanel (Sizeup_plot);
      DisplayablePanel SizeupEff_panel = new DisplayablePanel (SizeupEff_plot);
      NvsT_panel.setAutofitting(true);
      N_panel.setAutofitting(true);
      Sizeup_panel.setAutofitting(true);
      SizeupEff_panel.setAutofitting(true);

      JPanel panel = new JPanel (new GridLayout(2,2));
      panel.add(N_panel);
      panel.add(NvsT_panel);
      panel.add(SizeupEff_panel);
      panel.add(Sizeup_panel);
      return panel;
    }
    catch (Exception exc) {
//      exc.printStackTrace();
      return null;
    }
  }

  /**
   * Processes the given command
   * @param command
   */
  private void processCommand(String command) {
    //      System.out.println ("Processing command: "+command);
    StringTokenizer tkn = new StringTokenizer(command," \t");
    String keyword = tkn.nextToken();
    try {
      if (keyword.equals("n")) {
        int n = Integer.parseInt(tkn.nextToken());
        double N = Double.parseDouble(tkn.nextToken());
        // Record contents.
        ntoNMap.put (n, N);
      }
      else if (keyword.equals ("T")) {
        // A running time specification line. Parse contents.
        long T = Long.parseLong(tkn.nextToken());
        // Record contents.
        tList.add ((double) T);
      }
      else if (keyword.equals("Tcurves")) {
        int n = Integer.parseInt(tkn.nextToken());
        nCurves = Math.max(0,n);
      }
			else if (keyword.equals ("nvst")) SpeedupAdapted.parsePlotSpecification (NvsT_plot,  command, tkn);
			else if (keyword.equals ("size")) SpeedupAdapted.parsePlotSpecification (N_plot,  command, tkn);
			else if (keyword.equals ("sizeup")) SpeedupAdapted.parsePlotSpecification (Sizeup_plot,  command, tkn);
			else if (keyword.equals ("sizeupeff")) SpeedupAdapted.parsePlotSpecification (SizeupEff_plot,  command, tkn);
//			else System.err.println ("Ignoring unknown command: "+command);
    }
    catch (Exception exc) {
      System.err.println ("Ignoring incorrect command: "+command);
      exc.printStackTrace(System.err);
    }
  }

}
