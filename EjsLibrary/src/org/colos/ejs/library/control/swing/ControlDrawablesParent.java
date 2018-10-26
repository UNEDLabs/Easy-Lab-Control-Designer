/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) Dec 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.value.ParserSuryono;
import org.opensourcephysics.display.*;
import org.opensourcephysics.display.dialogs.ScaleInspector;
import org.opensourcephysics.display.dialogs.AutoScaleInspector;
import org.opensourcephysics.tools.*;

import java.util.*;
import java.text.DecimalFormat;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputAdapter;


/**
 * A container to hold Drawables.
 * The base class for ControlDrawingParent, ControlDrawingPanel3D and ControlPlottingPanel
 * Its visual MUST be a (subclass of) DrawingPanel
 * It is prepared for interaction, if the visual is interactive
 */
public abstract class ControlDrawablesParent extends ControlSwingElement implements NeedsUpdate, NeedsFinalUpdate, ControlParentOfDrawables {

  // List of children that need to do something before repainting the panel
  protected Vector<NeedsPreUpdate> preupdateList = new Vector<NeedsPreUpdate>();
  protected boolean isZoomed = false;
  protected double minX, maxX,minY, maxY;
  protected boolean autoX, autoY;
  protected boolean xminSet=false, xmaxSet=false, yminSet=false, ymaxSet=false;

  public ControlDrawablesParent () {
    super ();
    getVisual().addKeyListener (new java.awt.event.KeyAdapter() {
      public void keyPressed  (java.awt.event.KeyEvent _e) {
        if (_e.isControlDown() && getSimulation()!=null) {
          if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_M) getPopupMenu(0,0);
          else if (_e.getKeyCode()==java.awt.event.KeyEvent.VK_P) printScreen();
        }
      }
    });
    getVisual().addMouseListener (new MouseAdapter() {
      public void mousePressed  (MouseEvent _evt) {
        if (getSimulation()==null || !OSPRuntime.isPopupTrigger(_evt)) return;  
        if (!ToolForData.getTool().isFullTool()) {
          getPopupMenu(_evt.getX(), _evt.getY());
          return;
        }
        // The simulation includes DataTools 
        final Interactive interactive = ((InteractivePanel) getVisual()).getInteractive();
        if (interactive instanceof Data) {
          JPopupMenu popupMenu = new JPopupMenu();
          for (Object entry : getDataInformationMenuEntries(getVisual(),(Data)interactive)) {
            if (entry instanceof Action) popupMenu.add((Action)entry);
            else if (entry instanceof JMenuItem) popupMenu.add((JMenuItem)entry);
            else popupMenu.add(entry.toString());
          }
          popupMenu.show(getVisual(),_evt.getX(),_evt.getY());
        }
        else getPopupMenu(_evt.getX(), _evt.getY());
      }
    });
  }
  
  public String getPropertyCommonName(String _property) {
    if (_property.equals("pressaction")) return "pressAction";
    if (_property.equals("dragaction"))  return "dragAction";
    if (_property.equals("action"))      return "releaseAction";
    return super.getPropertyCommonName(_property);
  }

  // --------------------------
  // Zooming and scaling
  // --------------------------

  public void addMenuEntries() {
    if (getMenuNameEntry()==null) return;
    getSimulation().addElementMenuEntries (getMenuNameEntry(), getExtraMenuOptions());
  }
  
  protected List<Object> getExtraMenuOptions() {
    JMenuItem scales = new JMenuItem ("tools_res:MenuItem.Scale");
    scales.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        isZoomed = true;
        ScaleInspector plotInspector = new ScaleInspector(((DrawingPanel) getVisual()));
        plotInspector.setLocationRelativeTo(getVisual());
        plotInspector.updateDisplay();
        plotInspector.setVisible(true);
      }
    });
    scales.setActionCommand("tools_res:MenuItem.Scale");

    JMenuItem zoomIn = new JMenuItem ("tools_res:MenuItem.ZoomIn");
    zoomIn.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        checkAutoscaling();
        new ZoomController();
      }
    });
    zoomIn.setActionCommand("tools_res:MenuItem.ZoomIn");

    JMenuItem zoomOut = new JMenuItem ("tools_res:MenuItem.ZoomOut");
    zoomOut.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        checkAutoscaling();
        double xmin = ((DrawingPanel) getVisual()).getXMin(), xmax = ((DrawingPanel) getVisual()).getXMax();
        double ymin = ((DrawingPanel) getVisual()).getYMin(), ymax = ((DrawingPanel) getVisual()).getYMax();
        double dx = xmax-xmin;
        double dy = ymax-ymin;
        isZoomed = true;
        if (!((DrawingPanel) getVisual()).isAutoscaleX()) ((DrawingPanel) getVisual()).setPreferredMinMaxX(xmin-dx/2, xmax+dx/2);
        if (!((DrawingPanel) getVisual()).isAutoscaleY()) ((DrawingPanel) getVisual()).setPreferredMinMaxY(ymin-dy/2, ymax+dy/2);
        update();
        //((DrawingPanel) getVisual()).render();
      }
    });
    zoomOut.setActionCommand("tools_res:MenuItem.ZoomOut");

    JMenuItem zoomCancel = new JMenuItem ("tools_res:MenuItem.ZoomCancel");
    zoomCancel.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        isZoomed = false;
        updateAutoscale();
        update();
        if (getSimulation()==null || getSimulation().isPaused()) ((DrawingPanel) getVisual()).render();
      }
    });
    zoomCancel.setActionCommand("tools_res:MenuItem.ZoomCancel");

    JMenu zoomMenu = new JMenu ("tools_res:MenuItem.Zoom");
    zoomMenu.setActionCommand("tools_res:MenuItem.Zoom");

    zoomMenu.add(scales);
    zoomMenu.add(zoomIn);
    zoomMenu.add(zoomOut);
    zoomMenu.add(zoomCancel);
    
    final JCheckBoxMenuItem textAliasItem = new JCheckBoxMenuItem("display_res:DrawingFrame.Text_checkbox_label", false); //$NON-NLS-1$
    textAliasItem.addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e){
          ((DrawingPanel) getVisual()).setAntialiasTextOn(textAliasItem.isSelected());
          Simulation sim = getSimulation();
          if (sim==null || sim.isPaused()) {
            ((DrawingPanel) getVisual()).invalidateImage();
            ((DrawingPanel) getVisual()).repaint();
          }
       }
    });
    textAliasItem.setActionCommand("display_res:DrawingFrame.Text_checkbox_label");
    final JCheckBoxMenuItem shapeAliasItem = new JCheckBoxMenuItem("display_res:DrawingFrame.Drawing_textbox_label", false); //$NON-NLS-1$
    shapeAliasItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ((DrawingPanel) getVisual()).setAntialiasShapeOn(shapeAliasItem.isSelected());
        Simulation sim = getSimulation();
        if (sim==null || sim.isPaused()) {
          ((DrawingPanel) getVisual()).invalidateImage();
          ((DrawingPanel) getVisual()).repaint();
        }
      }
    });
    shapeAliasItem.setActionCommand("display_res:DrawingFrame.Drawing_textbox_label");

    JMenu aliasMenu = new JMenu("display_res:DrawingFrame.AntiAlias_menu_title"); //$NON-NLS-1$
    aliasMenu.add(textAliasItem);
    aliasMenu.add(shapeAliasItem);
    aliasMenu.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        textAliasItem.setSelected(((DrawingPanel) getVisual()).isAntialiasTextOn());
        shapeAliasItem.setSelected(((DrawingPanel) getVisual()).isAntialiasShapeOn());
      }
    });
    aliasMenu.setActionCommand("display_res:DrawingFrame.AntiAlias_menu_title");

    JMenuItem snapshotMenuItem = new JMenuItem ("tools_res:MenuItem.Snapshot");
    snapshotMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        ((DrawingPanel) getVisual()).snapshot();
      }
    });
    snapshotMenuItem.setActionCommand("tools_res:MenuItem.Snapshot");
      
    List<Object> menuEntries = new java.util.ArrayList<Object>();
    menuEntries.add(snapshotMenuItem);
    menuEntries.add(zoomMenu);   
    menuEntries.add(aliasMenu);
    
    if (ToolForData.getTool().isFullTool()) {
      JMenuItem dataToolItem = new JMenuItem ("tools_res:DataTool.Frame.Title");
      dataToolItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { 
          ToolForData.getTool().showDataTool(getVisual(),getAllData(getDrawingPanel()));
        }
      });
      dataToolItem.setActionCommand("tools_res:DataTool.Frame.Title");

//      JMenuItem fourierToolItem = new JMenuItem ("tools_res:FourierTool.Frame.Title");
//      fourierToolItem.addActionListener(new ActionListener() {
//        public void actionPerformed(ActionEvent e) { 
//          ToolForData.getTool().showFourierTool(getVisual(),getAllData(getDrawingPanel()));
//        }
//      });
//      fourierToolItem.setActionCommand("tools_res:FourierTool.Frame.Title");

      menuEntries.add(dataToolItem);
//      menuEntries.add(fourierToolItem);
    }
    
    return menuEntries;
  }

  public DrawingPanel getDrawingPanel () { return (DrawingPanel) getVisual(); }

  private java.util.List<Data> getAllData(DrawingPanel _panel) {
    java.util.List<Data> dataList = new ArrayList<Data>();
    for (Drawable drawable : _panel.getDrawables()) {
      if (drawable instanceof Data) dataList.add((Data)drawable);
    }
    return dataList;
  }
  
  public void reset() {
    //System.out.println ("Resetting...");
    isZoomed = false;
    updateAutoscale();
    if (isUnderEjs) ( (DrawingPanel) getVisual()).setBuffered(false);
  }

  protected void updateAutoscale () {
    if (!isZoomed) {
      DrawingPanel dPanel = ((DrawingPanel) getVisual());
      dPanel.setAutoscaleX(autoX);
      dPanel.setAutoscaleY(autoY);
      updateExtrema(); 
    }
  }

  protected void updateExtrema () {
//    System.out.println ("Updating extrema to "+minX+","+maxX+" auto = "+getDrawingPanel().isAutoscaleX());
    if (!isZoomed) {
      DrawingPanel dPanel = ((DrawingPanel) getVisual());
      if (dPanel.isAutoscaleX()) {
        if (xminSet || xmaxSet) dPanel.limitAutoscaleX(minX, maxX);
      }
      else {
        dPanel.setPreferredMinMaxX(minX,maxX);
      }
      if (dPanel.isAutoscaleY()) {
        if (yminSet || ymaxSet) dPanel.limitAutoscaleY(minY, maxY);
      }
      else dPanel.setPreferredMinMaxY(minY,maxY);
      if (isUnderEjs) dPanel.render();
    }
  }

  protected void checkAutoscaling () {
    DrawingPanel dPanel = ((DrawingPanel) getVisual());
    if (dPanel.isAutoscaleX() || dPanel.isAutoscaleY()) {
      isZoomed = true;
      AutoScaleInspector plotInspector = new AutoScaleInspector(dPanel);
      plotInspector.setLocationRelativeTo(dPanel);
      plotInspector.updateDisplay();
      plotInspector.setVisible(true);
    }
  }

  // --------------------------
  // End of zooming and scaling (See ZoomControler class below)
  // --------------------------

  public boolean acceptsChild (ControlElement _child) {
    if (_child instanceof ControlDrawable) return true;
    return false;
  }

  public void update() { // Ensure it will be updated
    // prepare children that need to do something
    for (NeedsPreUpdate npu : preupdateList) npu.preupdate();
  }
  
  public void finalUpdate() {
//    if (myGroup!=null && myGroup.isCollectingData()) return;
    // Now render
    if (javax.swing.SwingUtilities.isEventDispatchThread()||Thread.currentThread().getName().equals("main")) {
//      System.out.println ("Invalidate");
      Simulation sim = getSimulation();
      if (sim==null || sim.isPaused()) {
        ((DrawingPanel) getVisual()).invalidateImage();
        ((DrawingPanel) getVisual()).repaint();
      }
    }
    else {
//      System.out.println ("render");
      ((DrawingPanel) getVisual()).render();
    }
  }
  
  /*{ old code
  javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public synchronized void run() {
      ((DrawingPanel) getVisual()).render();
    }
  });
}
*/

  //------------------------------------------------
// Implementation of ControlParentOfDrawables
//------------------------------------------------
  
  public java.awt.Component getComponent() { return getVisual(); }
  
  public void addToPreupdateList (NeedsPreUpdate _child) {
//    System.out.println ("Adding "+_child);
    preupdateList.add(_child);
  }

  public void removeFromPreupdateList (NeedsPreUpdate _child) {
    preupdateList.remove(_child);
  }

  public void addDrawable(Drawable _drawable) { ((DrawingPanel) getVisual()).addDrawable(_drawable); }

  public void addDrawableAtIndex(int _index, Drawable _drawable) { ((DrawingPanel) getVisual()).addDrawableAtIndex(_index,_drawable); }
  
  public void removeDrawable (Drawable _drawable) { ((DrawingPanel) getVisual()).removeDrawable(_drawable); }
  
// ------------------------------------------------
// This prepares it for interaction within Ejs
// ------------------------------------------------

  public ControlDrawable getSelectedDrawable() { return null; }

// -------------------------------------
// Zooming
// -------------------------------------

      class ZoomController extends MouseInputAdapter {

        ZoomController () {
          DrawingPanel dPanel = (DrawingPanel) getVisual();
          dPanel.addMouseListener(this);
          dPanel.addMouseMotionListener(this);
//          dPanel.getZoomBox().prepareZoom();
        }

         public void mousePressed(MouseEvent e) {
           ((DrawingPanel) getVisual()).getZoomBox().startZoom(e.getX(), e.getY());
         }

         public void mouseDragged(MouseEvent e) {
           ((DrawingPanel) getVisual()).getZoomBox().drag(e.getX(), e.getY());
         }

         public void mouseReleased(MouseEvent e) {
           DrawingPanel dPanel = (DrawingPanel) getVisual();
           java.awt.Rectangle rect = ((DrawingPanel) getVisual()).getZoomBox().reportZoom(); //e.getX(), e.getY());
           dPanel.removeMouseListener(this);
           dPanel.removeMouseMotionListener(this);
           if (rect!=null) {
//       System.out.println("Want to zoom to " + rect.x + "," + rect.y + "," + (rect.x + rect.width) + "," + (rect.y + rect.height));
             isZoomed = true;
             if (!dPanel.isAutoscaleX()) dPanel.setPreferredMinMaxX(rect.x,rect.x + rect.width);
             if (!dPanel.isAutoscaleY()) dPanel.setPreferredMinMaxY(rect.y,rect.y + rect.height);
             update();
             //dPanel.render();
           }
         }

         public void mouseClicked(MouseEvent e) {}

         public void mouseEntered(MouseEvent e) {}

         public void mouseExited(MouseEvent e) {}

         public void mouseMoved(MouseEvent e) {}

       }


} // End of class


class MyCoordinateStringBuilder extends org.opensourcephysics.display.axes.CoordinateStringBuilder {

  DecimalFormat xFormat = new DecimalFormat("x=0.000;x=-0.000");
  DecimalFormat yFormat = new DecimalFormat("y=0.000;y=-0.000");
  ParserSuryono parser=null;
  DecimalFormat expressionFormat = new DecimalFormat("0.000;-0.000");


  public void setXFormat (DecimalFormat format) { xFormat = format; }

  public void setYFormat (DecimalFormat format) { yFormat = format; }

  public void setExpressionFormat (DecimalFormat format) { expressionFormat = format; }

  public void setExpression (String _expression) {
    if (_expression==null) { parser = null; return; }
    parser = new ParserSuryono(2);
    parser.defineVariable(0,"x");
    parser.defineVariable(1,"y");
    parser.define(_expression);
    parser.parse();
  }

  public String getCoordinateString(DrawingPanel panel, java.awt.event.MouseEvent e) {
    String txt = "";
    double x = panel.pixToX(e.getPoint().x);
    double y = panel.pixToY(e.getPoint().y);
    if (xFormat!=null) {
      if (yFormat!=null) txt = xFormat.format(x) + " " + yFormat.format(y);
      else txt = xFormat.format(x);
    }
    else if (yFormat!=null) txt = yFormat.format(y);
    if (parser!=null) {
      parser.setVariable(0, x);
      parser.setVariable(1, y);
      if (txt.length()>0) txt += " ";
      txt += expressionFormat.format(parser.evaluate());
    }
    return txt;
  }
}






