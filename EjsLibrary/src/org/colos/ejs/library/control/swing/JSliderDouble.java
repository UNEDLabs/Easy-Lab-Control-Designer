/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import javax.swing.border.*;
import javax.swing.JSlider;

import org.colos.ejs.library.control.ConstantParser;

import java.text.DecimalFormat;
import java.awt.*;
import java.util.*;

/**
 * A slider to display double values. 
 */
public class JSliderDouble extends JSlider {
  static public final int RESOLUTION=100000;

  private boolean interactiveChange=true, outOfLimits=false;
  private int ticks=0;
  private double scale, minimum, maximum=Double.NaN;
  private TitledBorder titledBorder;
  private EtchedBorder etchedBorder;
  private java.text.DecimalFormat format=null, ticksFormat=null;
  private Color currentColor = null;
  private double doubleValue;
  javax.swing.event.ChangeListener listener;

  public JSliderDouble(javax.swing.event.ChangeListener _additionalChangeListener) {
    super();
    listener = _additionalChangeListener;
    setPaintLabels(false);
    setPaintTicks(false);
    setPaintTrack(true);
    setMinimum (0);
    setMaximum (RESOLUTION);
    setValue (0);
    doubleValue = 0.0;
    setForeground(Color.BLACK);
    etchedBorder = new EtchedBorder(EtchedBorder.LOWERED);
    titledBorder = new TitledBorder (etchedBorder,"");
    titledBorder.setTitleJustification (TitledBorder.CENTER);
    setBorder (etchedBorder);

    minimum = 0.0;
    setDoubleMaximum(1.0);

    addChangeListener (new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
//        System.out.println ("Slider at : "+getValue());
        if (!outOfLimits) doubleValue = minimum + getValue()/scale;
//        System.out.println ("value is : "+doubleValue);
        if (format!=null) {
          titledBorder.setTitle (format.format(doubleValue));
          repaint();
        }
        listener.stateChanged(evt);
      }
    });
    
  }

  //------------------------------------------------
  // Set and Get the values of the properties
  //------------------------------------------------

  /**
   * Returns true if the change of the slider has been caused by user interaction.
   * False if it was caused by a programmatic call to setDoubleValue().
   */
  public boolean isInteractiveChange()  { return interactiveChange; }
  
  public void setDoubleValue (double value) {
//    interactiveChange = false;
//    System.out.println (" Slider setting to "+value+" .Value = "+(int) ((value-minimum)*scale));
    doubleValue = value;
    int intValue = (int) ((value-minimum)*scale);
    if (intValue<0 || intValue>RESOLUTION)  {
      outOfLimits = true;
      setValue (intValue);
      outOfLimits = false;
    }
    else setValue (intValue);
//    interactiveChange = true;
//    if (format!=null) {
//      titledBorder.setTitle (format.format(value));
//      repaint();
//    }
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public double getDoubleMinimum () { return minimum; }

  public void setDoubleMinimum (double val) {
    if (val==minimum) return;
//    System.out.println (getName()+" Slider setting min to "+val);
    minimum = val;
//    if (minimum>=maximum) maximum = minimum+1.0; // This caused problems
    scale = 1.0*RESOLUTION/(maximum-minimum);
    recomputeTicks ();
    interactiveChange = false;
    setDoubleValue (doubleValue);
    interactiveChange = true;
  }

  public double getDoubleMaximum () { return maximum; }

  public void setDoubleMaximum (double val) {
    if (val==maximum) return;
//    System.out.println (getName()+" Slider setting max to "+val);
    maximum = val;
//    if (minimum>=maximum) minimum = maximum-1.0; // This caused problems
    scale = 1.0*RESOLUTION/(maximum-minimum);
    recomputeTicks ();
    interactiveChange = false;
    setDoubleValue (doubleValue);
    interactiveChange = true;
  }

  public void setTicksFormat(DecimalFormat _format) {
    if (_format==null) {
      ticksFormat = null;
      setPaintLabels(false);
    }
    else {
      if (_format.equals(ticksFormat)) return;
      ticksFormat = _format;
      setPaintLabels(true);
    }
    recomputeTicks();
  }

  public void setNumberOfTicks (int _ticks) {
    if (_ticks==ticks) return;
    ticks = _ticks;
    recomputeTicks();
  }

  public void setTitleFormat(DecimalFormat _format) {
    if (_format==null) {
      format = null;
      setBorder (etchedBorder);
      return;
    }
    if (_format.equals(format)) return;
    format = _format;
    titledBorder.setTitle (format.format (getDoubleValue()));
    setBorder (titledBorder);
    repaint();
  }

  public void setTitleFormat (String _formatStr) {
    setTitleFormat((DecimalFormat) ConstantParser.formatConstant(_formatStr).getObject());
  }
  
  public void setTitleFont (Font aFont) {
    titledBorder.setTitleFont(aFont);
    /*
    Dictionary labels = slider.getLabelTable();
    if (labels!=null) for (Enumeration e = labels.elements(); e.hasMoreElements(); ) {
      Object object = e.nextElement();
      if (object instanceof javax.swing.JLabel) {
        ( (javax.swing.JLabel) object).setFont(aFont);
        ( (javax.swing.JLabel) object).validate();
      }
    }
    */
//    currentFont = aFont;
  }

  public void setTitleForeground (Color aColor) {
    titledBorder.setTitleColor(aColor);
    Dictionary<?, ?> labels = getLabelTable();
    if (labels!=null) for (Enumeration<?> e = labels.elements(); e.hasMoreElements(); ) {
      Object object = e.nextElement();
      if (object instanceof javax.swing.JLabel) ((javax.swing.JLabel) object).setForeground(aColor);
    }
    currentColor = aColor;
  }

  // -------------------------------------
  // Private methods
  // -------------------------------------

  private void recomputeTicks () {
    if (ticks<2) { setPaintTicks(false); return; }
    int spacing = RESOLUTION/(ticks-1);
    setMinorTickSpacing (spacing);
    setMajorTickSpacing (2*spacing);
    setPaintTicks(true);
    if (ticksFormat!=null) {
      java.util.Hashtable<Integer,javax.swing.JLabel> table = new java.util.Hashtable<Integer,javax.swing.JLabel> ();
      for (int i=0; i<=RESOLUTION; i+=2*spacing)
        table.put (new Integer(i),new javax.swing.JLabel(ticksFormat.format(minimum+i/scale)));
      setLabelTable (table);
    }
    if (currentColor!=null) setTitleForeground(currentColor);
  }

} // End of class
