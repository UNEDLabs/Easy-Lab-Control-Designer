package es.uhu.ejs.hardware.mindwave;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class MindWaveElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("es/uhu/ejs/hardware/mindwave/MindWave.png"); // This icon is included in this jar
    
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "MindWave"; }
  
  public String getConstructorName() { return "es.uhu.hardware.mindwave.MindWave"; }

  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_name + " = new " + getConstructorName() + "();\n");
    return buffer.toString();
  }
  
  public String getPackageList() { return "org/json/++"; }
  
  public String getDestructionCode(String _name) { return _name+".close();"; }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "provides access to a NeuroSky MindWave EEG headset";
  }
  
  @Override
  protected String getHtmlPage() { return "es/uhu/ejs/hardware/mindwave/MindWave.html"; }
  
}
