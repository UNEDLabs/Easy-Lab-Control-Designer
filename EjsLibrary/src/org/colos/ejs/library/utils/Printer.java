/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.utils;

import java.awt.*;
import java.io.PrintWriter;

/**
 * A dummy video tool
 */

public class Printer {

  static public void print (String title, String output) {
    print (title, output, 10, 0.5, 0.5, 0.5, 0.5); 
  }

  static public void print (String title, String output, int fontSize) {
    print (title, output, fontSize, 0.5, 0.5, 0.5, 0.5); 
  }
  
  static public void print (String title, String output, int fontSize, double top, double left, double bottom, double right)  { 
    Frame frame = new Frame(title);
    frame.setSize(200, 50);
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
    frame.setLocation(bounds.x+(bounds.width - frame.getSize().width)/2, bounds.y+(bounds.height - frame.getSize().height)/2);
    frame.setVisible(true);
    HardcopyWriter hw;
    try {
      hw = new HardcopyWriter(frame,title,fontSize, left, right, top, bottom);
      PrintWriter out = new PrintWriter(hw);
      out.println (output);
      out.close();
    }
    catch(HardcopyWriter.PrintCanceledException e) {}
    frame.setVisible(false);
    frame.dispose(); 
  }
  
} // End of class


