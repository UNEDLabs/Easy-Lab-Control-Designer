package org.colos.ejs.model_elements;

import org.colos.ejs.osejs.Osejs;

public interface EJSAware {

  /**
   * Sets the EJS instance for this element
   * @return
   */
  public void setEJS(Osejs ejs);
  
  public void readCompleted();
  
}
