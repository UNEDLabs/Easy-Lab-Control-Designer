package org.colos.ejss.html_view;

public class Property {

  private String name;
  private String info;
  private String def;
  
  Property(String name, String info, String def) {
    this.setName(name);
    this.setInfo(info);
    this.setDef(def);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getDef() {
    return def;
  }

  public void setDef(String def) {
    this.def = def;
  }
  
}
