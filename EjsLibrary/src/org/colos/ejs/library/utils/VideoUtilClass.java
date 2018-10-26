/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.awt.Component;
import java.awt.image.BufferedImage;
import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.SpecialRender;
import org.opensourcephysics.media.xuggle.XuggleIO;
import org.opensourcephysics.tools.VideoCaptureTool;

/**
 * A dummy video tool
 */

public class VideoUtilClass extends VideoUtil {
  // Variables for video capture
  private VideoCaptureTool videoTool = null;
  private Component videoComponent;
  private BufferedImage videoImage=null;
  private SpecialRender videoSpecialRender=null;
  
  @Override
  public boolean isFullClass() { return true; }

  @Override
  public void takeSnapshot(Component _component) {
    try { org.opensourcephysics.display.PrintUtils.saveComponentAsEPS(_component); }
    catch (Exception exc) { exc.printStackTrace(); }
  }

  @Override
  public boolean startVideoTool (org.colos.ejs.library.View _view, String _element) {
    videoImage = null;
    videoComponent = null;
    videoSpecialRender = null;
    if (_view==null) return false;
    Component comp = _view.getComponent(_element);
    if (comp==null) {
      System.err.println("Component not found: "+_element);
      return false;
    }
    if      (comp instanceof javax.swing.JFrame)  comp = ((javax.swing.JFrame)  comp).getContentPane();
    else if (comp instanceof javax.swing.JDialog) comp = ((javax.swing.JDialog) comp).getContentPane();
    videoImage = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
    videoComponent = comp;
    ControlElement ctrlEl = _view.getElement(_element);
    if (ctrlEl instanceof SpecialRender) videoSpecialRender = (SpecialRender) ctrlEl;
    // Start the video tool
    if (videoTool==null) videoTool=new VideoCaptureTool(); //VideoGrabber.getTool();
    videoTool.setVisible(true);
    videoTool.clear();
    XuggleIO.registerWithVideoIO();
    
   //Register Xuggle video types using reflection

   try {
     String name = "org.opensourcephysics.media.xuggle.XuggleIO"; //$NON-NLS-1$
     Class<?> xuggleClass = Class.forName(name);
     java.lang.reflect.Method method=xuggleClass.getMethod("registerWithVideoIO"); //$NON-NLS-1$
     method.invoke(null, (Object[])null);
   } catch(Exception ex) { ex.printStackTrace(); 
   } catch(Error err) { err.printStackTrace();
   }
    return true;
  }

  @Override
  public void captureVideoImage () {
    if (videoImage==null) return;
    if (videoSpecialRender!=null) videoSpecialRender.render(videoImage);
    else {
      java.awt.Graphics g = videoImage.getGraphics();
      videoComponent.paint(g);
      g.dispose();
    }
    videoTool.addFrame(videoImage);
  }

  @Override
  public boolean writeGIF(java.io.OutputStream out, BufferedImage bi) {
    try {
      GIFEncoder encoder = new GIFEncoder(bi);
      encoder.Write(out);
      return true;
    }
    catch (Exception exc) { exc.printStackTrace(); return false; }
  }

} // End of class


