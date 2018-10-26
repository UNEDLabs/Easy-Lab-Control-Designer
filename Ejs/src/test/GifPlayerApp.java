package test;

import java.io.*;

import javax.swing.*;

import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.media.gif.*;

public class GifPlayerApp {

  public GifPlayerApp() {
    // create a drawing panel and frame
    DrawingPanel panel = new DrawingPanel();
    panel.setPreferredMinMax(-1,1,-1,1);

    double x = 0, y = -1;
    double w = 0.5, h = 1;
    // create a gif video 
    try {
      Video video = new GifVideo("/org/opensourcephysics/resources/controls/images/play.gif");
      video.setX(x);
      video.setY(y+h);
      video.setWidth(w);
      video.setHeight(h);
      panel.addDrawable(video);
    } catch (IOException e) {
      e.printStackTrace();
    };
    
    DrawingFrame frame = new DrawingFrame(panel);
    frame.setVisible(true); // will also work with a hidden frame
    frame.setTitle("GIFVideo test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  public static void main(String[] args) {
    new GifPlayerApp();
  }
}
