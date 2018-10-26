package es.uhu.ejs.hardware.phidgets;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class MotorControlHCElement extends AbstractPhidgetElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/phidgets/MotorControlHC.png"); // This icon is included in this jar

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Phidget1064"; }
  
  public String getConstructorName() { return "es.uhu.ejs.hardware.phidgets.MotorControlHCAdapter"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "allows PWM control using a 1064 Phidget module";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/phidgets/MotorControlHC.html"; }

}
