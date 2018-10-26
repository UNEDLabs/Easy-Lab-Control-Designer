package org.colos.ejs.model_elements.parallel_ejs;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;

import edu.rit.numeric.*;
import edu.rit.numeric.plot.Dots;
import edu.rit.numeric.plot.Plot;
import edu.rit.swing.DisplayablePanel;

/**
 * @author  Alan Kaminsky
 * @version 04-Aug-2008
 * @version Aug 2011 Adaptation by Paco from Kaminsky's original Speedup.java
 */
public class SpeedupAdapted {
  // For printing metrics.
  static private DecimalFormat FMT_0  = new DecimalFormat ("0");
  static private DecimalFormat FMT_0E = new DecimalFormat ("0E0");
  static private DecimalFormat FMT_1  = new DecimalFormat ("0.0");
//  static private DecimalFormat FMT_2  = new DecimalFormat ("0.00");
//  static private DecimalFormat FMT_3  = new DecimalFormat ("0.000");

  // Mapping from size parameter n (type Integer) to data for n (type Data).
  private Map<Integer,Data> dataMap = new TreeMap<Integer,Data>();

  // Maximum K value.
  private double K_max = Double.NEGATIVE_INFINITY;

  // Plot objects.
  private Plot T_plot = new Plot();
  private Plot Speedup_plot = new Plot();
  private Plot Eff_plot = new Plot();
  private Plot EDSF_plot = new Plot();


  /**
   * Get the data record for the given n value. Create it if necessary.
   */
  private Data getData (int n) {
    Data data = dataMap.get (n);
    if (data == null) {
      data = new Data (n);
      dataMap.put (n, data);
    }
    return data;
  }

  /**
   * Does the processing and creates the plots in a single panel
   * @param sortedMeasurements a list of (correctly ordered) measurements. For each problem size, the measurements must be ordered by ascending K 
   * @param plotSpec a series of plot specifications
   * @return a
   */
  public JComponent createPlots(java.util.List<SpeedupDataMeasurements> measurements, java.util.List<String> plotSpec) {
    try {
      // Set up plots with default attributes.
      T_plot
      .plotTitle ("Running Time vs. Processors")
      .rightMargin (72)
      .minorGridLines (true)
      .xAxisKind (Plot.LOGARITHMIC)
      .xAxisMinorDivisions (10)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisKind (Plot.LOGARITHMIC)
      .yAxisMinorDivisions (10)
      .yAxisTickFormat (FMT_0E)
      .yAxisTickScale (1000)
      .yAxisTitle ("<I>T</I> (<I>N,K</I>) (sec)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);
      Speedup_plot
      .plotTitle ("Speedup vs. Processors")
      .rightMargin (72)
      .xAxisStart (0)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisStart (0)
      .yAxisTickFormat (FMT_0)
      .yAxisTitle ("<I>Speedup</I> (<I>N,K</I>)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);
      Eff_plot
      .plotTitle ("Efficiency vs. Processors")
      .rightMargin (72)
      .xAxisStart (0)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisStart (0)
      .yAxisTickFormat (FMT_1)
      .yAxisTitle ("<I>Eff</I> (<I>N,K</I>)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);
      EDSF_plot
      .plotTitle ("EDSF vs. Processors")
      .rightMargin (72)
      .xAxisStart (0)
      .xAxisTitle ("Processors, <I>K</I>")
      .yAxisStart (0)
      .yAxisTickFormat (FMT_0)
      .yAxisTickScale (0.001)
      .yAxisTitle ("<I>EDSF</I> (<I>N,K</I>) (/1000)")
      .labelPosition (Plot.RIGHT)
      .labelOffset (6);

      // Process the data
      for (SpeedupDataMeasurements measurement : SpeedupDataMeasurements.sort(measurements)) {
        int n = measurement.getProblemSize();
        int K = measurement.getNumberOfProcessors();
        K_max = Math.max (K_max, K);
        double T_min = Double.POSITIVE_INFINITY;
        double T_max = Double.NEGATIVE_INFINITY;
        for (Long T : measurement.getTimeMeasurements()) {
          T_min = Math.min (T_min, T);
          T_max = Math.max (T_max, T);
        }
        // Record data.
        Data data = getData (n);
        if (K == 0)
        {
          data.T_seq = T_min;
          data.T_max_seq = T_max;
          data.Dev_seq = (T_max - T_min) / T_min;
        }
        else if (K == 1)
        {
          double Speedup =
            data.T_seq == 0.0 ?
                1.0 :
                  data.T_seq / T_min;
          double Eff = Speedup;
          double Dev = (T_max - T_min) / T_min;
          data.T_par_1 = T_min;
          data.K_series.add (K);
          data.T_series.add (T_min);
          data.Tseq_series.add (data.T_seq);
          data.T_max_series.add (T_max);
          data.Speedup_series.add (Speedup);
          data.Eff_series.add (Eff);
          data.Dev_series.add (Dev);
        }
        else
        {
          double Speedup =
            data.T_seq == 0.0 ?
                data.T_par_1 / T_min :
                  data.T_seq / T_min;
          double Eff = Speedup / K;
          double Dev = (T_max - T_min) / T_min;
          double EDSF = (K*T_min - data.T_par_1) / data.T_par_1 / (K-1);
          data.K_series.add (K);
          data.T_series.add (T_min);
          data.Tseq_series.add (data.T_seq);
          data.T_max_series.add (T_max);
          data.Speedup_series.add (Speedup);
          data.Eff_series.add (Eff);
          data.Dev_series.add (Dev);
          data.K_series_2.add (K);
          data.EDSF_series_2.add (EDSF);
        }
      }

      // Add ideal performance lines to plots.
      Speedup_plot
      .seriesDots (null)
      .seriesColor (new Color (0.7f, 0.7f, 0.7f))
      .xySeries (new double[] {0, K_max}, new double[] {0, K_max})
      .seriesDots (Dots.circle (5))
      .seriesColor (Color.black);
      Eff_plot
      .seriesDots (null)
      .seriesColor (new Color (0.7f, 0.7f, 0.7f))
      .xySeries (new double[] {0, K_max}, new double[] {1, 1})
      .seriesDots (Dots.circle (5))
      .seriesColor (Color.black);

      // Process the labels

      for (String command : plotSpec) processCommand(command);

      // Process data.
      //      System.out.println ("N\tK\tT\tSpdup\tEffic\tEDSF\tDevi");
      for (Data data : dataMap.values())
      {
        // Print metrics.
        if (data.K_series.isEmpty()) continue;
        //        System.out.println();
        //        String labelText = data.labelText;
        //        if (labelText.startsWith ("N = "))
        //        {
        //          labelText = labelText.substring (4);
        //        }
        //        else if (labelText.startsWith ("N="))
        //        {
        //          labelText = labelText.substring (2);
        //        }
        //        if (data.T_seq != 0.0)
        //        {
        //          System.out.println
        //          (labelText + "\tseq\t" +
        //              FMT_0.format (data.T_seq) + "\t\t\t\t" +
        //              FMT_0.format (100 * data.Dev_seq) + "%");
        //        }
        int len = data.K_series.length();
        //        System.out.println
        //        (labelText + "\t" +
        //            FMT_0.format (data.K_series.x(0)) + "\t" +
        //            FMT_0.format (data.T_series.x(0)) + "\t" +
        //            FMT_3.format (data.Speedup_series.x(0)) + "\t" +
        //            FMT_3.format (data.Eff_series.x(0)) + "\t\t" +
        //            FMT_0.format (100 * data.Dev_series.x(0)) + "%");
        //        for (int i = 1; i < len; ++ i)
        //        {
        //          System.out.println
        //          (labelText + "\t" +
        //              FMT_0.format (data.K_series.x(i)) + "\t" +
        //              FMT_0.format (data.T_series.x(i)) + "\t" +
        //              FMT_3.format (data.Speedup_series.x(i)) + "\t" +
        //              FMT_3.format (data.Eff_series.x(i)) + "\t" +
        //              FMT_3.format (data.EDSF_series_2.x(i-1)) + "\t" +
        //              FMT_0.format (100 * data.Dev_series.x(i)) + "%");
        //        }

        // Add data series to plots.
        T_plot
        .seriesDots (Dots.circle (5))
        .seriesColor (Color.black);
        T_plot
        .xySeries
        (new AggregateXYSeries
            (data.K_series, data.T_series))
            .label
            (data.labelText,
                data.K_series.x (len-1),
                data.T_series.x (len-1));
        T_plot
        .seriesDots (null)
        .seriesColor (new Color (0.7f, 0.7f, 0.7f));
        T_plot
        .xySeries
        (new AggregateXYSeries
            (data.K_series, data.Tseq_series))
            .label
            (data.labelText,
                data.K_series.x (len-1),
                data.Tseq_series.x (len-1));

        Speedup_plot
        .xySeries
        (new AggregateXYSeries
            (data.K_series, data.Speedup_series))
            .label
            (data.labelText,
                data.K_series.x (len-1),
                data.Speedup_series.x (len-1));
        Eff_plot
        .xySeries
        (new AggregateXYSeries
            (data.K_series, data.Eff_series))
            .label
            (data.labelText,
                data.K_series.x (len-1),
                data.Eff_series.x (len-1));
        EDSF_plot
        .xySeries
        (new AggregateXYSeries
            (data.K_series_2, data.EDSF_series_2))
            .label
            (data.labelText,
                data.K_series_2.x (len-2),
                data.EDSF_series_2.x (len-2));
      }

      // Display plots.
      //      T_plot.getFrame().setVisible (true);
      //      Speedup_plot.getFrame().setVisible (true);
      //      Eff_plot.getFrame().setVisible (true);
      //      EDSF_plot.getFrame().setVisible (true);

      DisplayablePanel T_panel = new DisplayablePanel (T_plot);
      DisplayablePanel Sp_panel = new DisplayablePanel (Speedup_plot);
      DisplayablePanel Eff_panel = new DisplayablePanel (Eff_plot);
      DisplayablePanel EDSF_panel = new DisplayablePanel (EDSF_plot);
      T_panel.setAutofitting(true);
      Sp_panel.setAutofitting(true);
      Eff_panel.setAutofitting(true);
      EDSF_panel.setAutofitting(true);

      JPanel panel = new JPanel (new GridLayout(2,2));
      panel.add(T_panel);
      panel.add(EDSF_panel);
      panel.add(Sp_panel);
      panel.add(Eff_panel);
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
        String labelText = getString(command);
        Data data = getData (n);
        data.N = N;
        data.labelText = labelText;
      }
      else if (keyword.equals ("time")) parsePlotSpecification (T_plot, command, tkn);
      else if (keyword.equals ("speedup")) parsePlotSpecification (Speedup_plot, command, tkn);
      else if (keyword.equals ("eff")) parsePlotSpecification (Eff_plot, command, tkn);
      else if (keyword.equals ("edsf")) parsePlotSpecification (EDSF_plot, command, tkn);
//      else System.err.println ("Ignoring unknown command: "+command);
    }
    catch (Exception exc) {
      System.err.println ("Ignoring incorrect command: "+command);
      exc.printStackTrace(System.err);
    }
  }

  static private String getString(String command)  {
    int beginIndex = command.indexOf('"');
    int endIndex = command.lastIndexOf('"');
    return command.substring(beginIndex+1,endIndex);
  }

  /**
   * Parse a plot specification line.
   */
  static public void parsePlotSpecification (Plot plot, String command, StringTokenizer tkn) throws Exception {
    String attribute = tkn.nextToken();
    if (attribute.equals ("frameTitle"))       plot.frameTitle (getString(command));
    else if (attribute.equals ("plotTitle"))   plot.plotTitle (getString(command));
    else if (attribute.equals ("margins"))     plot.margins (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("leftMargin"))  plot.leftMargin (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("topMargin"))   plot.topMargin (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("rightMargin")) plot.rightMargin (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("bottomMargin"))plot.bottomMargin (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("xAxisStart"))  plot.xAxisStart (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("xAxisEnd"))    plot.xAxisEnd (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("xAxisMajorDivisions")) plot.xAxisMajorDivisions (Integer.parseInt(tkn.nextToken()));
    else if (attribute.equals ("xAxisMinorDivisions")) plot.xAxisMinorDivisions (Integer.parseInt(tkn.nextToken()));
    else if (attribute.equals ("xAxisLength")) plot.xAxisLength (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("xAxisTitle"))  plot.xAxisTitle (getString(command));
    else if (attribute.equals ("yAxisStart"))  plot.yAxisStart (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("yAxisEnd"))    plot.yAxisEnd (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("yAxisMajorDivisions")) plot.yAxisMajorDivisions (Integer.parseInt(tkn.nextToken()));
    else if (attribute.equals ("yAxisMinorDivisions")) plot.yAxisMinorDivisions (Integer.parseInt(tkn.nextToken()));
    else if (attribute.equals ("yAxisLength")) plot.yAxisLength (Double.parseDouble(tkn.nextToken()));
    else if (attribute.equals ("yAxisTitle"))  plot.yAxisTitle (getString(command));
    else if (attribute.equals ("yAxisTickFormat")) plot.yAxisTickFormat (new DecimalFormat(getString(command)));
    else if (attribute.equals ("yAxisTitleOffset"))plot.yAxisTitleOffset (Double.parseDouble(tkn.nextToken()));
    else throw new Exception();
  }

  /**
   * Class Data is a record of data associated with a certain size parameter
   * n.
   *
   * @author  Alan Kaminsky
   * @version 12-Jun-2007
   */
  private static class Data {
    // Size parameter n.
    //public int n;

    // Smallest running time for sequential version, or 0.0 if not
    // specified.
    public double T_seq;

    // Largest running time for sequential version, or 0.0 if not specified.
    @SuppressWarnings("unused")
    public double T_max_seq;

    // T deviation value for sequential version, or 0.0 if not specified.
    @SuppressWarnings("unused")
    public double Dev_seq;

    // Smallest running time for parallel version K=1, or 0.0 if not
    // specified.
    public double T_par_1;

    // Series of K values, K >= 1 (K = number of processors).
    public ListSeries K_series = new ListSeries();

    // Series of T values, K >= 1 (T = smallest running time measurement).
    public ListSeries T_series = new ListSeries();

    // Series of T sequential values, K >= 1 (T = smallest running time measurement).
    public ListSeries Tseq_series = new ListSeries();

    // Series of T_max values, K >= 1 (T_max = largest running time
    // measurement).
    public ListSeries T_max_series = new ListSeries();

    // Series of Speedup values, K >= 1.
    public ListSeries Speedup_series = new ListSeries();

    // Series of Eff values, K >= 1.
    public ListSeries Eff_series = new ListSeries();

    // Series of T deviation values, K >= 1.
    public ListSeries Dev_series = new ListSeries();

    // Series of K values, K >= 2 (K = number of processors).
    public ListSeries K_series_2 = new ListSeries();

    // Series of EDSF values, K >= 2 (K = number of processors).
    public ListSeries EDSF_series_2 = new ListSeries();

    // Problem size N.
    @SuppressWarnings("unused")
    public double N;

    // Label text.
    public String labelText;

    // Constructor.
    public Data (int n) {
      //this.n = n;
      this.N = n;
      this.labelText = "N = " + n;
    }

  }

}
