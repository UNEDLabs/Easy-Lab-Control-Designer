package org.colos.ejs.model_elements.apache_numerics;

import javax.swing.ImageIcon;

import org.colos.ejs.model_elements.AbstractModelElement;

public class SummaryStatisticsElement extends AbstractModelElement {
  static ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/apache_numerics/SummaryStatistics.gif"); // This icon is included in this jar
    
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "SummStats"; }
  
  public String getConstructorName() { return "org.apache.commons.math3.stat.descriptive.SummaryStatistics"; }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "computes summary statistics of a number of double values without storing them";
  }
  
  @Override
  protected String getHtmlPage() { 
    return "org/colos/ejs/model_elements/apache_numerics/SummaryStatistics.html"; 
  }
  
}
