/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.displayejs;

import java.awt.*;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;

/*
 * This is an example of a Drawable and Measurable element
 * Absolutely inspired by org.opensourcephysics.display2d.VectorField
 * @author F. Esquembre (http://fem.um.es).
 */
public class VectorField3D extends ElementSet {

  // Configuration variables
  protected int levels=0;
  protected int invisibleLevel = -1;
  protected boolean autoscaleMagnitude = false;
  protected double minMagnitude=-1.0, maxMagnitude=1.0;
  protected Color maxColor=null,minColor=null;
  protected boolean visibility = true;

  // Implementation variables
  protected boolean hasData = false;
  protected double[][][] data2D = null;
  protected double[][][][] data3D = null;
  protected double zoom = 1.0, magConstant = 0.0;
  protected double[] magnitude=new double[] {0.0};
  protected Color[] colors = null;

  public VectorField3D () {
    super (1,InteractiveArrow.class);
    maxColor = Color.red;
    minColor = Color.blue;
    setNumberOfLevels (16);
    setAutoscaleMagnitude(true);
  }

  public void setVisible(boolean _visible) { // Overwrite its parent
    visibility = _visible;
  }

  public void setNumberOfLevels (int _lev) {
    if (_lev<=0) { levels = 0; return; }
    if (_lev!=levels) {
      levels = _lev;
      colors = new Color[levels];
      initColors();
      magConstant = levels/(maxMagnitude-minMagnitude);
      setInvisibleLevel (invisibleLevel);
    }
  }

  public void setMinColor (Color _aColor) {
    if (!_aColor.equals(minColor)) {
      minColor = _aColor;
      initColors();
    }
  }

  public void setMaxColor (Color _aColor) {
    if (!_aColor.equals(maxColor)) {
      maxColor = _aColor;
      initColors();
    }
  }

  /**
   * Set a level below which arrows are not drawn.
   * This helps speed the view when there are a lot of small values.
   * @param _lev The level below which (inclusive) arrows are not shown.
   */
  public void setInvisibleLevel (int _lev) {
    invisibleLevel = _lev;
  }

  public void setZoom (double _scale) {
    zoom = _scale;
    if (data2D!=null) {
      for (int el=0, i=0,m=data2D.length; i<m; i++) {
        for (int j=0, n=data2D[0].length; j<n; j++, el++) {
          InteractiveElement element = this.elementAt(el);
          element.setSizeX(data2D[i][j][2] * zoom);
          element.setSizeY(data2D[i][j][3] * zoom);
        }
      }
    }
    else if (data3D!=null) {
      for (int el=0, i=0,m=data3D.length; i<m; i++) {
        for (int j=0, n=data3D[0].length; j<n; j++) {
          for (int k = 0, p = data3D[0][0].length; k < p; k++, el++) {
            InteractiveElement element = this.elementAt(el);
            element.setSizeX(data3D[i][j][k][3] * zoom);
            element.setSizeY(data3D[i][j][k][4] * zoom);
            element.setSizeZ(data3D[i][j][k][5] * zoom);
          }
        }
      }
    }
  }

  public void setAutoscaleMagnitude (boolean _auto){
    autoscaleMagnitude = _auto;
    if (autoscaleMagnitude) computeMagnitudeExtrema();
  }

  public void setColorExtrema (double min, double max){
    autoscaleMagnitude = false;
    minMagnitude = min;
    maxMagnitude = max;
    if (maxMagnitude==minMagnitude) maxMagnitude = minMagnitude + 1.0;
    magConstant = levels/(maxMagnitude-minMagnitude);
  }


  public void setDataArray(double[][][][] _data) {
    if (_data==null) {
      hasData = false;
      return;
    }
    data3D = _data; data2D = null;
    hasData = true;
    int num = data3D.length*data3D[0].length*data3D[0][0].length;
    if (this.getNumberOfElements()!=num) {
      magnitude = new double[num];
      setNumberOfElements (num);
    }
    for (int el=0, i=0,m=data3D.length; i<m; i++) {
      for (int j=0, n=data3D[0].length; j<n; j++) {
        for (int k = 0, p = data3D[0][0].length; k < p; k++, el++) {
          InteractiveElement element = this.elementAt(el);
          element.setX(_data[i][j][k][0]);
          element.setY(_data[i][j][k][1]);
          element.setZ(_data[i][j][k][2]);
          element.setSizeX(_data[i][j][k][3] * zoom);
          element.setSizeY(_data[i][j][k][4] * zoom);
          element.setSizeZ(_data[i][j][k][5] * zoom);
          magnitude[el] = _data[i][j][k][6];
        }
      }
    }
    if (autoscaleMagnitude) computeMagnitudeExtrema();
  }

  public void setDataArray(double[][][] _data) {
    if (_data==null) {
      hasData = false;
      return;
    }
    data2D = _data; data3D = null;
    hasData = true;
    int num = data2D.length*data2D[0].length;
    if (this.getNumberOfElements()!=num) {
      magnitude = new double[num];
      setNumberOfElements (num);
      setZoom (zoom);
    }
    for (int el=0, i=0,m=data2D.length; i<m; i++) {
      for (int j=0, n=data2D[0].length; j<n; j++, el++) {
        InteractiveElement element = this.elementAt(el);
        element.setX(data2D[i][j][0]);
        element.setY(data2D[i][j][1]);
        element.setSizeX(data2D[i][j][2] * zoom);
        element.setSizeY(data2D[i][j][3] * zoom);
        magnitude[el] = data2D[i][j][4];
      }
    }
    if (autoscaleMagnitude) computeMagnitudeExtrema();
  }

  // -------------------- Implementation methods

  public Interactive findInteractive(DrawingPanel _panel, int _xpix, int _ypix){
    return null;
  }

  /* Drawable3D */
  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (!(visibility && hasData)) return null;
    list3D.clear();
    for (int i=0,n=getNumberOfElements(); i<n; i++) {
      InteractiveElement element = this.elementAt(i);
      elementAt(i).setVisible(true);
      if (levels>0) {
        Color color = magToColor(magnitude[i]);
        if (color==null) continue;
        element.getStyle().setEdgeColor(color);
        element.getStyle().setFillPattern(color);
      }
      else {
        element.getStyle().setEdgeColor(minColor);
        element.getStyle().setFillPattern(minColor);
      }
      Object3D[] objects = element.getObjects3D(_panel);
      if (objects!=null) for (int j=0; j<objects.length; j++) list3D.add(objects[j]);
    }
    if (list3D.size()==0) return null;
    return list3D.toArray(minimalObjects);
  }

  public void drawQuickly (DrawingPanel3D _panel, Graphics2D _g) {
    if (!(visibility && hasData)) return;
    if (levels>0) for (int i=0,n=getNumberOfElements(); i<n; i++) {
        Color color = magToColor(magnitude[i]);
        if (color==null) { elementAt(i).setVisible(false); continue; }
        elementAt(i).setVisible(true);
        elementAt(i).getStyle().setEdgeColor(color);
        elementAt(i).getStyle().setFillPattern(color);
      }
    else for (int i=0,n=getNumberOfElements(); i<n; i++) {
        elementAt(i).setVisible(true);
        elementAt(i).getStyle().setEdgeColor(minColor);
        elementAt(i).getStyle().setFillPattern(minColor);
      }
    super.drawQuickly(_panel,_g);
  }

  public void draw (DrawingPanel _panel, Graphics _g) {
    if (!(visibility && hasData)) return;
   if (levels>0) for (int i=0,n=getNumberOfElements(); i<n; i++) {
       Color color = magToColor(magnitude[i]);
       if (color==null) { elementAt(i).setVisible(false); continue; }
       elementAt(i).setVisible(true);
       elementAt(i).getStyle().setEdgeColor(color);
       elementAt(i).getStyle().setFillPattern(color);
     }
   else for (int i=0,n=getNumberOfElements(); i<n; i++) {
       elementAt(i).setVisible(true);
       elementAt(i).getStyle().setEdgeColor(minColor);
       elementAt(i).getStyle().setFillPattern(minColor);
     }
   super.draw(_panel,_g);
 }

  // ------------------ Private or protected methods

  protected void initColors () {
//    System.out.println ("Recreating colors");
    int redStart   = minColor.getRed();
    int greenStart = minColor.getGreen();
    int blueStart  = minColor.getBlue();
    int redEnd     = maxColor.getRed();
    int greenEnd   = maxColor.getGreen();
    int blueEnd    = maxColor.getBlue();
    for (int i = 0; i<levels; i++) {
      int r = (int) (redStart   + ((redEnd-redStart)*i*1.0f)/(levels-1) );
      int g = (int) (greenStart + ((greenEnd-greenStart)*i*1.0f)/(levels-1) );
      int b = (int) (blueStart  + ((blueEnd-blueStart)*i*1.0f)/(levels-1) );
      colors[i] = new Color(r, g, b);
    }
  }

  protected void computeMagnitudeExtrema() {
    double[] temp=magnitude;  // copy array reference in case the data array changes
    if (temp==null) return;
    minMagnitude=temp[0];
    maxMagnitude=temp[0];
    for (int i = 0; i <temp.length; i++) {
      double v=temp[i];
      if (v>maxMagnitude) maxMagnitude=v;
      if (v<minMagnitude) minMagnitude=v;
    }
    magConstant = levels/(maxMagnitude-minMagnitude);
//    System.out.println ("Extrema are "+minMagnitude+" "+maxMagnitude);
  }

  protected Color magToColor (double mag) {
    int index = (int)(magConstant*(mag - minMagnitude));
    if (index<=invisibleLevel) return null;
    if (index <= 0) return colors[0];
    if (index >= levels) return colors[levels-1];
    return  colors[index];
  }

}