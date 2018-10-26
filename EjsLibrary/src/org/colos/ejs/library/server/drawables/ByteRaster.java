package org.colos.ejs.library.server.drawables;

import java.util.HashMap;
import java.util.Map;

import org.colos.ejs.library.server.*;

public class ByteRaster extends SocketViewElement {

  public void setIndexedColor(int index, java.awt.Color color) {
    System.out.println(getName()+": wants to set indexed color ("+index+" = "+color);
    Map<String, Object> dataMap = new HashMap<String, Object>();
    dataMap.put("index", index);
    dataMap.put("color", new double[]{color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha()/255.0});
    executeMethodWithMap("setIndexedColor",dataMap);
  }
  
}
