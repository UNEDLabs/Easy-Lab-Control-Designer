/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import javax.swing.*;
import org.opensourcephysics.display.*;

public class NamedLookAndFeel {
  static private final ResourceUtil res    = new ResourceUtil("Resources");

  private String name, humanName, classname;
  private boolean decorateWindows=true;

  static private NamedLookAndFeel[] installedLookAndFeels;

  static {
    javax.swing.UIManager.LookAndFeelInfo[] info=UIManager.getInstalledLookAndFeels();
    int count = info.length;
    installedLookAndFeels = new NamedLookAndFeel[count+3];
    for(int i=0; i<count; i++) installedLookAndFeels[i] = new NamedLookAndFeel(info[i].getName(),info[i].getClassName());
    installedLookAndFeels[count+0] = new NamedLookAndFeel(OSPRuntime.SYSTEM_LF,res.getString("EjsConsole.LookAndFeel.SYSTEM"),
        UIManager.getSystemLookAndFeelClassName());
    installedLookAndFeels[count+1] = new NamedLookAndFeel(OSPRuntime.CROSS_PLATFORM_LF,res.getString("EjsConsole.LookAndFeel.CROSS_PLATFORM"),
        UIManager.getCrossPlatformLookAndFeelClassName());
    installedLookAndFeels[count+2] = new NamedLookAndFeel(OSPRuntime.DEFAULT_LF,res.getString("EjsConsole.LookAndFeel.DEFAULT"),
        UIManager.getLookAndFeel().getClass().getName());
    //      LookAndFeel tlf = new net.sourceforge.napkinlaf.NapkinLookAndFeel();
    //      installedLookAndFeels[count+3] = new NamedLookAndFeel("NAPKIN","NAPKIN",tlf.getClass().getName());
  }

  static public NamedLookAndFeel[] getInstalledLookAndFeels() { return installedLookAndFeels; }
  
  static public NamedLookAndFeel getLookAndFeel(String _name) {
    for (NamedLookAndFeel laf : installedLookAndFeels) {
      if (laf.name.equals(_name)) return laf;
    }
    return installedLookAndFeels[installedLookAndFeels.length-1]; // default
  }

  public NamedLookAndFeel (String _name, String _classname) {
    this.name = _name;
    this.humanName = _name;
    this.classname = _classname;
  }

  public NamedLookAndFeel (String _name, String _humanName, String _classname) {
    this.name = _name;
    this.humanName = _humanName;
    this.classname = _classname;
  }

  public String toString() { return humanName; }

  public String getName() { return name; }

  public boolean isDecorateWindows() { return this.decorateWindows; }

  public String getClassname() { return this.classname; }

}
