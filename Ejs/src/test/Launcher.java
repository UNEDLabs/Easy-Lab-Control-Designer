package test;

import javax.swing.JFrame;

public class Launcher {
  public static void main(String[] args) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hello World!");
    JFrame jframe = new JFrame();
    jframe.setSize(500,300);
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jframe.setVisible(true);
  }
  
}