package demo;

import processing.core.*;
import codeanticode.gsvideo.*;

public class GSVideoTest extends PApplet {  
  private static final long serialVersionUID = 1L;
  
//  GSMovie movie;
  GSCapture cam;
  
  public void setup() {
    size(640, 480);
    background(0);
    
    System.err.println ("home="+System.getProperty("user.home"));
    System.err.println ("dir="+System.getProperty("user.dir"));
    String userDir = System.getProperty("user.dir");
    
//    for (String device : GSCapture.list()) {
//      System.err.println ("Device = "+device);
//    }
    // Use this variable (before instantiating any GSVideo object) to force GSVideo to use a specific location to look for the
    // gstreamer native libs:
//    GSVideo.localGStreamerPath = userDir+"/lib/GSVideo/gstreamer/macosx64";
    
    // Load and play the video in a loop
//    movie = new GSMovie(this, "station.mov");
//    movie.loop();
    
    cam = new GSCapture(this,640,480,"0");
    cam.start();
  }

//  public void movieEvent(GSMovie movie) {
//    movie.read();
//  }

  public void draw() {
//    tint(255, 20);
//    image(movie, mouseX-movie.width/2, mouseY-movie.height/2);
    if (cam.available() == true) {
      cam.read();
      image(cam, 0, 0);
    }
  }

  public static void main(String args[]) {
    System.err.println ("HELLO ____________");
    PApplet.main(new String[] { GSVideoTest.class.getName() });
  }    
}
