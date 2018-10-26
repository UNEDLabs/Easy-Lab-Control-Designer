package es.uhu.ejs.hardware.phidgets;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class TemperatureSensor4InputsElement extends AbstractPhidgetElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/phidgets/TemperatureSensor4Inputs.png"); // This icon is included in this jar

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Phidget1048"; }
  
  public String getConstructorName() { return "es.uhu.ejs.hardware.phidgets.TemperatureSensor4InputsAdapter"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "allows reading up to four temperatures using a 1048 Phidget module";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/phidgets/TemperatureSensor4Inputs.html"; }

}
