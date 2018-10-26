/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see:
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.drawing3d.utils;

import java.util.*;
import java.awt.*;

import org.opensourcephysics.drawing3d.*;

/**
 *
 * <p>Title: Decoration</p>
 * <p>Description: A list of elements that decorate a drawing panel 3d</p>
 *
 * @author Francisco Esquembre
 * @author Carlos Jara (CJB)
 * @version August 2009
 */
public class Decoration {
   static private final int AXIS_DIVISIONS = 10;
 
   // Implementation variables
   private DrawingPanel3D panel;
   private java.util.List<Element> list = new ArrayList<Element>();

   // Variables for decoration
   private ElementArrow xAxis, yAxis, zAxis;
   private ElementText xText, yText, zText;
   private Element[] boxSides = new Element[12];
   
   // tracker lines for a cursor
   private int trackersVisible;
   private ElementSegment[] trackerLines = new ElementSegment[9];


   public Decoration(DrawingPanel3D _panel) {
     this.panel = _panel;
     // Create the bounding box
     Resolution axesRes = new Resolution(AXIS_DIVISIONS, 1, 1);
     for(int i = 0, n = boxSides.length; i<n; i++) {
       ElementSegment segment = new ElementSegment();
       segment.getStyle().setResolution(axesRes);
       list.add(segment);
       boxSides[i] = segment;
     }
     // Create the axes
     String[] axesLabels = panel.getVisualizationHints().getAxesLabels();
     xAxis = new ElementArrow();
     xAxis.getStyle().setResolution(axesRes);
     list.add(xAxis);
     xText = new ElementText();
     xText.setText(axesLabels[0]);
     xText.getStyle().setRelativePosition(Style.CENTERED);
     list.add(xText);

     yAxis = new ElementArrow();
     yAxis.getStyle().setResolution(axesRes);
     list.add(yAxis);
     yText = new ElementText();
     yText.setText(axesLabels[1]);
     yText.getStyle().setRelativePosition(Style.CENTERED);
     list.add(yText);

     zAxis = new ElementArrow();
     zAxis.getStyle().setResolution(axesRes);
     list.add(zAxis);
     zText = new ElementText();
     zText.setText(axesLabels[2]);
     zText.getStyle().setRelativePosition(Style.CENTERED);
     list.add(zText);

     for (int i=0; i<trackerLines.length; i++) {
       trackerLines[i] = new ElementSegment();
       trackerLines[i].setVisible(false);
       list.add(trackerLines[i]);
     }
     setCursorMode();
     
     adjustColors();
     adjustFont();
     updateType();
     reset();      
  }

   /**
    * Adjusts the colors so that they are visible
    */
   public void adjustColors() {
     Color xAxisColor, yAxisColor, zAxisColor;
     Color linesColor = panel.getVisualizationHints().getForegroundColor();
     if (isDarkBackground()) {
       xAxisColor = Color.ORANGE;
       yAxisColor = Color.YELLOW;
       zAxisColor = Color.CYAN;
     }
     else {
       xAxisColor = new Color(128, 0, 0);
       yAxisColor = new Color(0, 128, 0);
       zAxisColor = new Color(0, 0, 255);
     }
     for (Element el : boxSides) el.getStyle().setLineColor(linesColor);
     boxSides[0].getStyle().setLineColor(xAxisColor);
     boxSides[3].getStyle().setLineColor(yAxisColor);
     boxSides[8].getStyle().setLineColor(zAxisColor);

     xAxis.getStyle().setLineColor(linesColor);
     yAxis.getStyle().setLineColor(linesColor);
     zAxis.getStyle().setLineColor(linesColor);
     
     xAxis.getStyle().setFillColor(xAxisColor);
     yAxis.getStyle().setFillColor(yAxisColor);
     zAxis.getStyle().setFillColor(zAxisColor);

     xText.getStyle().setLineColor(linesColor);
     yText.getStyle().setLineColor(linesColor);
     zText.getStyle().setLineColor(linesColor);
   }

   /**
    * Adjusts the colors so that they are visible
    */
   public void adjustFont() {
     Font font = panel.getVisualizationHints().getFont();
     xText.setFont(font);
     yText.setFont(font);
     zText.setFont(font);
   }

   public java.util.List<Element> getElementList() { return this.list; }

   public void updateAxesLabels() {
     String[] labels = panel.getVisualizationHints().getAxesLabels();
     xText.setText(labels[0]);
     yText.setText(labels[1]);
     zText.setText(labels[2]);
   }

   public void updateType() {
     switch(panel.getVisualizationHints().getDecorationType()) {
       case VisualizationHints.DECORATION_NONE :
          for(int i = 0, n = boxSides.length; i<n; i++) boxSides[i].setVisible(false);
          xAxis.setVisible(false);
          yAxis.setVisible(false);
          zAxis.setVisible(false);
          xText.setVisible(false);
          yText.setVisible(false);
          zText.setVisible(false);
          break;
       case VisualizationHints.DECORATION_CENTERED_AXES :
       case VisualizationHints.DECORATION_AXES :
         for(int i = 0, n = boxSides.length; i<n; i++) boxSides[i].setVisible(false);
         xAxis.setVisible(true);
         yAxis.setVisible(true);
         zAxis.setVisible(true);
         xText.setVisible(true);
         yText.setVisible(true);
         zText.setVisible(true);
         break;
       default :
       case VisualizationHints.DECORATION_CUBE :
          for(int i = 0, n = boxSides.length; i<n; i++) boxSides[i].setVisible(true);
          xAxis.setVisible(false);
          yAxis.setVisible(false);
          zAxis.setVisible(false);
          xText.setVisible(false);
          yText.setVisible(false);
          zText.setVisible(false);
          break;
     }
    }

   public void reset() {
      double xmin = panel.getPreferredMinX(), xmax = panel.getPreferredMaxX(); 
      double ymin = panel.getPreferredMinY(), ymax = panel.getPreferredMaxY(); 
      double zmin = panel.getPreferredMinZ(), zmax = panel.getPreferredMaxZ();
      double dx = xmax-xmin, dy = ymax-ymin, dz = zmax-zmin;
      boxSides[0].setXYZ(xmin, ymin, zmin);
      boxSides[0].setSizeXYZ(dx, 0.0, 0.0);
      boxSides[1].setXYZ(xmax, ymin, zmin);
      boxSides[1].setSizeXYZ(0.0, dy, 0.0);
      boxSides[2].setXYZ(xmin, ymax, zmin);
      boxSides[2].setSizeXYZ(dx, 0.0, 0.0);
      boxSides[3].setXYZ(xmin, ymin, zmin);
      boxSides[3].setSizeXYZ(0.0, dy, 0.0);
      boxSides[4].setXYZ(xmin, ymin, zmax);
      boxSides[4].setSizeXYZ(dx, 0.0, 0.0);
      boxSides[5].setXYZ(xmax, ymin, zmax);
      boxSides[5].setSizeXYZ(0.0, dy, 0.0);
      boxSides[6].setXYZ(xmin, ymax, zmax);
      boxSides[6].setSizeXYZ(dx, 0.0, 0.0);
      boxSides[7].setXYZ(xmin, ymin, zmax);
      boxSides[7].setSizeXYZ(0.0, dy, 0.0);
      boxSides[8].setXYZ(xmin, ymin, zmin);
      boxSides[8].setSizeXYZ(0.0, 0.0, dz);
      boxSides[9].setXYZ(xmax, ymin, zmin);
      boxSides[9].setSizeXYZ(0.0, 0.0, dz);
      boxSides[10].setXYZ(xmax, ymax, zmin);
      boxSides[10].setSizeXYZ(0.0, 0.0, dz);
      boxSides[11].setXYZ(xmin, ymax, zmin);
      boxSides[11].setSizeXYZ(0.0, 0.0, dz);

      switch(panel.getVisualizationHints().getDecorationType()) {
        default :
          xAxis.setXYZ(xmin, ymin, zmin);
          xAxis.setSizeXYZ(dx, 0.0, 0.0);
          xText.setXYZ(xmax+dx*0.04, ymin, zmin); // 0.02
          yAxis.setXYZ(xmin, ymin, zmin);
          yAxis.setSizeXYZ(0.0, dy, 0.0);
          yText.setXYZ(xmin, ymax+dy*0.04, zmin);
          zAxis.setXYZ(xmin, ymin, zmin);
          zAxis.setSizeXYZ(0.0, 0.0, dz);
          zText.setXYZ(xmin, ymin, zmax+dz*0.05);
          break;          
        case VisualizationHints.DECORATION_CENTERED_AXES :
          double cx = (xmin+xmax)/2, cy = (ymin+ymax)/2, cz = (zmin+zmax)/2;
          xAxis.setXYZ(cx, cy, cz);
          xAxis.setSizeXYZ(dx/2, 0.0, 0.0);
          xText.setXYZ(xmax+dx*0.04, cy, cz); // 0.02
          yAxis.setXYZ(cx, cy, cz);
          yAxis.setSizeXYZ(0.0, dy/2, 0.0);
          yText.setXYZ(cx, ymax+dy*0.04, cz);
          zAxis.setXYZ(cx, cy, cz);
          zAxis.setSizeXYZ(0.0, 0.0, dz/2);
          zText.setXYZ(cx, cy, zmax+dz*0.05);
          break;          
      }
   }
   
   private boolean isDarkBackground() {
     if (panel.getVisualizationHints().getBackgroundImage()!=null) return false; 
     Color bkgd = panel.getVisualizationHints().getBackgroundColor();
     if (bkgd.getRed()<128 && bkgd.getGreen()<128 && bkgd.getBlue()<128) return true;
     return false;
   }

   // ----------------------------------------------------
   // Methods for the cursor
   // ----------------------------------------------------

   public void setCursorMode() {
     switch (panel.getVisualizationHints().getCursorType()) {
       case VisualizationHints.CURSOR_NONE : trackersVisible = 0; break;
       case VisualizationHints.CURSOR_CUBE : trackersVisible = 9; break;
       default :
       case VisualizationHints.CURSOR_XYZ  : trackersVisible = 3; break;
       case VisualizationHints.CURSOR_CROSSHAIR : trackersVisible = 3; break;
     }
   }

   public void positionTrackers(double[] _point) {
     boolean visibles = (_point!=null);
     for (int i = 0, n = trackerLines.length; i<n; i++) {
       if (i<trackersVisible) trackerLines[i].setVisible(visibles);
       else trackerLines[i].setVisible(false);
     }
     if (!visibles) return;
     double xmin = panel.getPreferredMinX(), xmax = panel.getPreferredMaxX();
     double ymin = panel.getPreferredMinY(), ymax = panel.getPreferredMaxY();
     double zmin = panel.getPreferredMinZ(), zmax = panel.getPreferredMaxZ();
     switch (panel.getVisualizationHints().getCursorType()) {
       case VisualizationHints.CURSOR_NONE : return;
       default :
       case VisualizationHints.CURSOR_XYZ :
         trackerLines[0].setXYZ(_point[0], ymin, zmin);
         trackerLines[0].setSizeXYZ(0, _point[1]-ymin, 0);
         trackerLines[1].setXYZ(xmin, _point[1], zmin);
         trackerLines[1].setSizeXYZ(_point[0]-xmin, 0, 0);
         trackerLines[2].setXYZ(_point[0], _point[1], zmin);
         trackerLines[2].setSizeXYZ(0, 0, _point[2]-zmin);
         break;
       case VisualizationHints.CURSOR_CUBE :
         trackerLines[0].setXYZ(xmin, _point[1], _point[2]);
         trackerLines[0].setSizeXYZ(_point[0]-xmin, 0, 0);
         trackerLines[1].setXYZ(_point[0], ymin, _point[2]);
         trackerLines[1].setSizeXYZ(0, _point[1]-ymin, 0);
         trackerLines[2].setXYZ(_point[0], _point[1], zmin);
         trackerLines[2].setSizeXYZ(0, 0, _point[2]-zmin);
         trackerLines[3].setXYZ(_point[0], ymin, zmin);
         trackerLines[3].setSizeXYZ(0, _point[1]-ymin, 0);
         trackerLines[4].setXYZ(xmin, _point[1], zmin);
         trackerLines[4].setSizeXYZ(_point[0]-xmin, 0, 0);
         trackerLines[5].setXYZ(_point[0], ymin, zmin);
         trackerLines[5].setSizeXYZ(0, 0, _point[2]-zmin);
         trackerLines[6].setXYZ(xmin, ymin, _point[2]);
         trackerLines[6].setSizeXYZ(_point[0]-xmin, 0, 0);
         trackerLines[7].setXYZ(xmin, _point[1], zmin);
         trackerLines[7].setSizeXYZ(0, 0, _point[2]-zmin);
         trackerLines[8].setXYZ(xmin, ymin, _point[2]);
         trackerLines[8].setSizeXYZ(0, _point[1]-ymin, 0);
         break;
       case VisualizationHints.CURSOR_CROSSHAIR :
         trackerLines[0].setXYZ(xmin, _point[1], _point[2]);
         trackerLines[0].setSizeXYZ(xmax-xmin, 0.0, 0.0);
         trackerLines[1].setXYZ(_point[0], ymin, _point[2]);
         trackerLines[1].setSizeXYZ(0.0, ymax-ymin, 0.0);
         trackerLines[2].setXYZ(_point[0], _point[1], zmin);
         trackerLines[2].setSizeXYZ(0.0, 0.0, zmax-zmin);
         break;
     }
   }

   // ----------------------------------------------------
   // Methods to get the axes (required by DrawingPanel to know the projection of the axes)
   // ----------------------------------------------------

   public ElementArrow getXAxis() { return xAxis; }
   
   public ElementArrow getYAxis() { return yAxis; }

   public ElementArrow getZAxis() { return zAxis; }
   
}
/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.
 *
 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
