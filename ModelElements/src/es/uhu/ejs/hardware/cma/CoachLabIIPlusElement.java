package es.uhu.ejs.hardware.cma;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class CoachLabIIPlusElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/cma/CoachLabIIPlus.gif"); // This icon is included in this jar
    
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "CoachLab"; }
  
  public String getConstructorName() { return "es.uhu.hardware.cma.CoachLabIIPlus"; }

  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_name + " = new " + getConstructorName() + "();\n");
    buffer.append(_name + ".setTargetObject(this);\n");
    return buffer.toString();
  }
  
  public String getPackageList() { return "gnu/io/++"; }
  
  public String getDestructionCode(String _name) { return _name+".close();"; }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to a CoachLab2+ module connected to the USB port";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/cma/CoachLabIIPlus.html"; }
  
}
