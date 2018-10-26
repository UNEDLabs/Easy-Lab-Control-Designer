package es.uhu.ejs.hardware.phidgets;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class TextLCDElement extends AbstractPhidgetElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/phidgets/TextLCD.png"); // This icon is included in this jar

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "PhidgetTextLCD"; }
  
  public String getConstructorName() { return "es.uhu.ejs.hardware.phidgets.TextLCDAdapter"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "allows access to a TextLCD Phidget module";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/phidgets/TextLCD.html"; }

}
