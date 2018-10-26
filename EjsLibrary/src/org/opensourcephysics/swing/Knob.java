package org.opensourcephysics.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;

public class Knob extends javax.swing.JPanel {
  static private Dimension DEFAULT_SIZE = new Dimension(20,20);
  
  private RectangularShape outerCircle = new Ellipse2D.Float(0, 0, 1, 1);
  private RectangularShape bigCircle = new Ellipse2D.Float(0, 0, 1, 1);
  private RectangularShape smallCircle = new Ellipse2D.Float(0, 0, 1, 1);
  private Paint outerPaint, bigPaint;
  private double lastAngle;

  private double angle=0;
  private double minAngle=-Math.PI*0.75;
  private double maxAngle=Math.PI*0.75;
  private double minValue=-1;
  private double maxValue=1;
  private double currentValue = Double.NaN;
  
  private ActionListener listener=null;
  private ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");

  public Knob() {
    super();
    setForeground(Color.LIGHT_GRAY);
    setPreferredSize(DEFAULT_SIZE);
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) { lastAngle = Math.atan2(e.getY()-getHeight()/2, e.getX()-getWidth()/2); }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        if (!isEnabled()) return; 
        double newAngle = translateToSameInterval(lastAngle,Math.atan2(e.getY()-getHeight()/2, e.getX()-getWidth()/2));
        angle += (newAngle-lastAngle);
        currentValue = getValue();
        lastAngle = newAngle;
        applyAngle();
        if (listener!=null) listener.actionPerformed(event);
      }
    });
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
//        preparePaint();
        repaint();
      }
    });
  }
  
  /**
   * Adds an action listener that would listen to changes in the angle position
   * @param _listener
   */
  public void addActionListener(ActionListener _listener) { this.listener = _listener; }

  // --------------------------
  // Operation
  //--------------------------
  
  /**
   * Sets the maximum angle for the knob to rotate (in radians)
   * Default is Pi*0.4
   */
  public void setMaxAngle(double _angle) {
    if (maxAngle!=_angle) {
      maxAngle = _angle;
      applyAngle();
    }
  }

  /**
   * Sets the minimum angle for the knob to rotate (in radians)
   * Default is -Pi*0.4
   */
  public void setMinAngle(double _angle) { 
    if (minAngle!=_angle) {
      minAngle = _angle;
      applyAngle();
    }
  }

  /**
   * Sets the current value of the variable
   * @return
   */
  public double getValue() {
    return minValue + (angle - minAngle)*(maxValue-minValue)/(maxAngle-minAngle);
  }
  
  /**
   * Sets the current value of the variable. Cannot exceed the limits
   * @param _value
   */
  public void setValue(double _value) {
    if (_value==currentValue) return;
    currentValue = _value;
    applyValue();
  }
  
  /**
   * Sets the value that corresponds to the maximum rotation of the knob. Default is 1
   * @param _value
   */
  public void setMaxValue(double _value) {
    if (maxValue!=_value) {
      maxValue = _value;
      applyValue();
    }
  }

  /**
   * Sets the value that corresponds to the minimum rotation of the knob. Default is -1
   * @param _value
   */
  public void setMinValue(double _value) { 
    if (minValue!=_value) {
      minValue = _value; 
      applyValue();
    }
  }

  private void applyValue() {
    if (currentValue<minValue) currentValue = minValue;
    else if (currentValue>maxValue) currentValue = maxValue;
    angle = minAngle + (currentValue-minValue)*(maxAngle-minAngle)/(maxValue-minValue);
    applyAngle();
  }
  
  /**
   * Checks the limits and repaints
   */
  private void applyAngle() {
    if (angle<minAngle) angle = minAngle;
    else if (angle>maxAngle) angle = maxAngle;
//    preparePaint();
    repaint();
  }
  
  // --------------------------
  // Graphic stuff
  //--------------------------
  
//  @Override
//  public void setForeground(Color _color) {
//    super.setForeground(_color);
//    preparePaint();
//  }

  @Override
  public void paintComponent(Graphics _g) {
    super.paintComponent(_g);
    preparePaint();
    Graphics2D g2 = (Graphics2D) _g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Paint outer shade
    g2.setPaint(outerPaint);
    g2.fill(outerCircle);
    // Paint big circle
    g2.setPaint(bigPaint);
    g2.fill(bigCircle);
    // Position small circle...
    int width = getWidth(), height = getHeight();
    int size = Math.min(width,height);
    int centerX = width/2, centerY = height/2;
    float tmpSize = size*0.25f;
    double radius = size*0.25f;
    double displayAngle = angle-Math.PI/2;
    float x1 = (float) (centerX + radius*Math.cos(displayAngle) - tmpSize/2.0f);
    float y1 = (float) (centerY + radius*Math.sin(displayAngle) - tmpSize/2.0f);
    smallCircle.setFrame(x1, y1, tmpSize,tmpSize);
    // ...and paint it
    Color color = getForeground();
    g2.setPaint(new GradientPaint(x1, y1, color, x1+tmpSize,y1+tmpSize,color.brighter()));
//    g2.setPaint(smallPaint);
    g2.fill(smallCircle);

//    posTransform.setToTranslation(radius*Math.cos(angle), radius*Math.sin(angle));
//    g2.setTransform(posTransform);
//    // ...and paint it
//    g2.setPaint(smallPaint);
//    g2.fill(smallCircle);
  }

  private void preparePaint() {
    int width = getWidth(), height = getHeight();
    int size = Math.min(width,height);
    if (size<=0) return;
    int centerX = width/2, centerY = height/2;
    Color color = getForeground();
    Color brighterColor = color.brighter();
    Color darkerColor = color.darker();

    // The big outer shape
    float tmpSize = size;
    float x1 = centerX - tmpSize/2, y1 = centerY-tmpSize/2;
    outerCircle.setFrame(x1,y1, tmpSize,tmpSize);
    outerPaint = new GradientPaint(x1, y1, brighterColor.brighter(), x1+tmpSize,y1+tmpSize,darkerColor.darker());
    bigPaint = new GradientPaint(x1, y1, color, x1+tmpSize,y1+tmpSize,darkerColor);

    tmpSize = size*0.9f;
    x1 = centerX - tmpSize/2f;
    y1 = centerY - tmpSize/2f;
    bigCircle.setFrame(x1,y1, tmpSize,tmpSize);
    
//    tmpSize = size*0.25f;
//    x1 = centerX - tmpSize/2f;
//    y1 = centerY - tmpSize/2f;
//    smallCircle.setFrame(x1, y1, tmpSize,tmpSize);
//    smallPaint = new GradientPaint(x1, y1, color, x1+tmpSize,y1+tmpSize,brighterColor);
  }

  /**
   * Converts the source angle to the 2PI-congruent angle in the same [-pi,+pi] interval as reference
   * @return
   */
  static private double translateToSameInterval(double _reference, double _angle) {
    double twoPi = 2*Math.PI;
    if (_angle>_reference) {
      double top = _reference+Math.PI;
      while (_angle>top) _angle -= twoPi;
      return _angle;
    }
    double bottom = _reference-Math.PI;
    while (_angle<bottom) _angle += twoPi;
    return _angle;
  }

}
