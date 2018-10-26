package test;

import org.opensourcephysics.controls.XMLControlElement;
import org.opensourcephysics.tools.ResourceLoader;

public class SavingArrayBugApp {

  public int problemArray[][] = new int[2][1];
  
  public SavingArrayBugApp() {
    String filename = "test.xml";
    System.err.println ("Array length = "+problemArray.length);
    System.err.println ("Array[0] length = "+problemArray[0].length);
    save(filename);
    System.err.println ("file save OK "+filename);
    System.err.println ("Reading file "+filename+" ...");
    read(filename);
    System.err.println ("Array length = "+problemArray.length);
    System.err.println ("Array[0] length = "+problemArray[0].length);
  }

  private void save(String filename) {
    try {
      XMLControlElement control = new XMLControlElement(this.getClass());
      control.setValue("testArray", problemArray);
      java.io.Writer writer = new java.io.FileWriter (filename);
      control.write(writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void read(String filename) {
    try {
      String xmlString = ResourceLoader.getString(filename);
      XMLControlElement control = new XMLControlElement(this.getClass());
      if (!control.readXMLForClass(xmlString,this.getClass())) {
        System.err.println ("Error reading "+filename);
        return;
      }
      Object object = control.getObject("testArray");
      if (object != null) problemArray = (int[][]) object;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new SavingArrayBugApp();
  }
}
