package org.colos.ejs.library.server.drawing2d;

import org.colos.ejs.library.server.*;

public class ElementTrail extends SocketViewElement {

  
  public void clear() {
    System.out.println(getName()+": wants to clear");
    executeMethodVoid("clear");
  }

  public void addPoint(double x, double y) {
    System.out.println(getName()+": wants to add point ("+x+","+y+")");
    executeMethodWithObject("addPoint",new double[]{x,y});
  }

  public void moveToPoint(double x, double y) {
    System.out.println(getName()+": wants to move to point ("+x+","+y+")");
    executeMethodWithObject("moveToPoint",new double[]{x,y});
  }

  public void addPoint(double[] point) {
    System.out.println(getName()+": wants to add point array");
    executeMethodWithObject("addPoint",point);
  }

  public void moveToPoint(double[] point) {
    System.out.println(getName()+": wants to move to point array");
    executeMethodWithObject("moveToPoint",point[0]);
  }

}
