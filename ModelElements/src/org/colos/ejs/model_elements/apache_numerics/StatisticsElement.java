package org.colos.ejs.model_elements.apache_numerics;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class StatisticsElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/apache_numerics/Statistics.gif");
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Statistics"; }
  
  public String getConstructorName() { return "org.apache.commons.math3.stat.descriptive.DescriptiveStatistics"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "stores a number of double values and computes descriptive statistics out of them";
  }

  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejs/model_elements/apache_numerics/Statistics.html"; 
  }

}
