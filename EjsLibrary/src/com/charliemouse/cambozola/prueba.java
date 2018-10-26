package com.charliemouse.cambozola;

import javax.swing.*;
import java.awt.*;

public class prueba {

  public static void main(String[] args) {

   Viewer video = new Viewer();
   JFrame frame = new JFrame();


   frame.getContentPane().setLayout (new BorderLayout());
   frame.getContentPane().add(BorderLayout.CENTER,video);

  //  Formato MJPEG
  //video.setParameterValue("url", "http://62.204.199.110/cgi-bin/fullsize.jpg?motion=0");
  //  Formato JPEG
  video.setParameterValue("url", "http://62.204.199.108/IMAGE.JPG");
  //  Formato JPEG
  // video.setParameterValue("url", "http://62.204.199.110/cgi-bin/fullsize.jpg");


   video.setImageDelay (0);
   video.setMJPEGFormat (false);
   video.init();
   video.start();


   frame.setSize(352, 320);
   frame.setVisible(true);



 }



}
