package es.uhu.ejs.hardware.phidgets;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class AdvancedServoElement extends AbstractPhidgetElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/phidgets/AdvancedServo.png"); // This icon is included in this jar

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "PhidgetAdvancedServo"; }
  
  public String getConstructorName() { return "es.uhu.ejs.hardware.phidgets.AdvancedServoAdapter"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "allows access to an AdvancedServo Phidget module";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/phidgets/AdvancedServo.html"; }

}
