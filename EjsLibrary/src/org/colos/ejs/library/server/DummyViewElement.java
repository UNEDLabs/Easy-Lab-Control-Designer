package org.colos.ejs.library.server;

import org.colos.ejs.library.ConfigurableElement;

public class DummyViewElement implements ConfigurableElement {

  public ConfigurableElement setProperty(String property, String value) {
    return this;
  }

}
