package org.colos.ejs.model_elements.parallel_ejs;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class PJSpeedupPlotElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/parallel_ejs/PJSpeedupPlot.gif");
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "pjPlots"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.parallel_ejs.PJSpeedupPlot"; }
  
  public String getDestructionCode(String _name) { return _name+".disposePlots();"; }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() { return "stores information from parallel runs and displays speed up plots"; }
  
  @Override
  protected String getHtmlPage() { return "org/colos/ejs/model_elements/parallel_ejs/PJSpeedupPlot.html"; }
  
}
