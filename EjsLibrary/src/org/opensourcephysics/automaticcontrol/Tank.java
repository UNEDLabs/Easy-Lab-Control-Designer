/*
 * The display3D package contains 3D drawing classes and drawables
 * @author F. Esquembre (http://fem.um.es).
 * Last version : July 2003
 */


package org.opensourcephysics.automaticcontrol;

import org.colos.ejs.library.Function;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.DrawingPanel;
import java.awt.Color;

public class Tank extends GroupDrawable  implements InteractionListener {
  static private final boolean insideConnected[] = { true, true, true, true, true};
  static private final boolean insideEnabled[] = { false, true, true, true, false};
  static private final boolean sidesEnabled[] = { true, true, true, true, false, false};
  static private final boolean profileEnabled[] = { false, true, true, true};

  // Configuration variables
  protected double height = 0.5, width = 0.2;
  protected boolean interactive = true, movable = false, resizable = false,
                    profilable = false, showProfiles=false;
  protected boolean showLevelOne = true, showLevelTwo = false;
  protected boolean interactiveLevelOne = false, interactiveLevelTwo = false;
  protected double levelOne = 0.2, levelTwo=0.0;

  // Implementation variables
  protected boolean hasChanged=false;
  protected InteractivePoligon sides, inside;
  protected InteractiveArrow level_1, level_2;
  private double [][] data, insideData;
  private boolean sidesConnected[]  = {true, false, true, true, true, true};
  protected InteractivePoligon profileLeft, profileRight;
  private double[] profile = { 1.0, 0.25, 0.25, 0.25 };
  private double p1y=1,p2x=0.25,p2y=0.25,p3x=0.25;
  private double[][] profileData = { {0.5,0.0},{0.5,p1y},{p2x,p2y},{p3x,0.0} };
  public Function diameterFunction;

  public Tank () {
    sides = new InteractivePoligon();
    inside = new InteractivePoligon();
    updateCoordinates();

    sides.setConnections(sidesConnected);
    sides.setClosed(false);
    sides.setPointSizeEnableds(sidesEnabled);
    sides.addListener(this);

    inside.setConnections(insideConnected);
    inside.setClosed(true);
    inside.getStyle().setEdgeColor(java.awt.Color.BLUE);
    inside.getStyle().setFillPattern(java.awt.Color.BLUE);
    inside.setPointSizeEnableds(insideEnabled);
    inside.addListener(this);

    level_1 = new InteractiveArrow(InteractiveArrow.SEGMENT);
    level_2 = new InteractiveArrow(InteractiveArrow.SEGMENT);
    level_2.setVisible (false);

    setXYZ(0.0,0.0,0.0);
    setSizeXYZ(1.0,1.0,1.0);

    profileLeft = new InteractivePoligon();               profileRight = new InteractivePoligon();
    updateProfiles();
    profileLeft.setClosed(true);                          profileRight.setClosed(true);
    profileLeft.getStyle().setFillPattern(Color.GRAY);    profileRight.getStyle().setFillPattern(Color.GRAY);
    profileLeft.setPointSizeEnableds(profileEnabled);     profileRight.setPointSizeEnableds(profileEnabled);
    profileLeft.addListener(this);                        profileRight.addListener(this);
    super.add(inside);
    super.add(profileRight);
    super.add(profileLeft);
    super.add(sides);

    setEnabled(interactive);
    setMovable (movable);
    setResizable (resizable);
    setProfilable (profilable);
    setShowProfiles (showProfiles);
    diameterFunction = new DiameterFunction();
    hasChanged = true;
  }

// -------------------------------------
// New configuration methods
// -------------------------------------

  public void setWidth (double _width) {
    if (_width==width) return;
    width = _width;
    hasChanged = true;
  }
  public double getWidth () { return width; }

  public void setHeight (double _height) {
    if (_height==height) return;
    height = _height;
    hasChanged = true;
  }
  public double getHeight () { return height; }

  public void setFilled (boolean _filled) {
    inside.setVisible(_filled);
  }

  public boolean isMovable () { return movable; }

  public void setMovable (boolean _movable) {
    sides.setEnabled(InteractiveElement.TARGET_POSITION,_movable);
    movable = _movable;
  }

  public boolean isEnabled () { return interactive; }

  public void setEnabled (boolean _enabled) {
    inside.setEnabled(InteractiveElement.TARGET_SIZE,_enabled);
    interactive = _enabled;
  }

  public boolean isResizable () { return resizable; }

  public void setResizable (boolean _enabled) {
    sides.setEnabled(InteractiveElement.TARGET_SIZE,_enabled);
    resizable = _enabled;
  }

  public void setClosedOnTop (boolean _closed) {
    if (sidesConnected[1]==_closed) return;
    sidesConnected[1] = _closed;
    sides.setConnections(sidesConnected);
  }

  public void setLineColor (java.awt.Color _color) {
    sides.getStyle().setEdgeColor(_color);
    profileLeft.getStyle().setEdgeColor(_color);
    profileRight.getStyle().setEdgeColor(_color);
  }

  public void setLineStroke (java.awt.Stroke _stroke) {
    sides.getStyle().setEdgeStroke(_stroke);
  }

  public void setFillColor (java.awt.Color _color) {
    inside.getStyle().setEdgeColor(_color);
    inside.getStyle().setFillPattern(_color);
  }

  public void setLevel (double _level) {
    if (levelOne==_level) return;
    insideData[1][1] = insideData[2][1] = insideData[3][1] = levelOne = _level;
    inside.setData(insideData);
  }
  public double getLevel () {  return this.levelOne; }

  public boolean isProfilable () { return profilable; }

  public void setProfilable (boolean _enabled) {
    profileLeft.setEnabled(InteractiveElement.TARGET_SIZE,_enabled);
    profileRight.setEnabled(InteractiveElement.TARGET_SIZE,_enabled);
    profilable = _enabled;
  }

  public boolean getShowProfiles () { return showProfiles; }

  public void setShowProfiles (boolean _show) {
    boolean value = isVisible() && _show;
    profileLeft.setVisible(value);
    profileRight.setVisible(value);
    if (showProfiles!=_show) updateProfiles();
    showProfiles = _show;
  }

  public void setVisible (boolean _show) {
    super.setVisible(_show);
    boolean value = _show && showProfiles;
    profileLeft.setVisible(value);
    profileRight.setVisible(value);
  }

  public void setProfileColor (java.awt.Color _color) {
    profileLeft.getStyle().setFillPattern(_color);
    profileRight.getStyle().setFillPattern(_color);
  }

  public void setProfile (double[] p) {
    if (p==null || p.length<4) { p1y = 1; p2x = 0.25; p2y = 0.25; p3x = 0.25; }
    else { p1y = p[0]; p2x = p[1]; p2y = p[2]; p3x = p[3]; }
    updateProfiles();
  }

  public double[] getProfile () {
    profile[0] = p1y; profile[1] = p2x; profile[2] = p2y; profile[3] = p3x;
    return profile;
  }

// -------------------------------------
//  Private methods
// -------------------------------------

  private void updateCoordinates () {
    data = new double[][] { {-width/2,0.0},{-width/2, height},{width/2, height}, {width/2,0.0}, {0.0,0.0}, {-width/2,0.0} };
    insideData = new double[][] { {-width/2,0.0},{-width/2, levelOne},{0.0, levelOne},{width/2, levelOne}, {width/2,0.0} };
    sides.setData(data);
    inside.setData(insideData);
    sides.setData(data);
    if (showProfiles) updateProfiles();
    hasChanged = false;
  }

  private void updateProfiles () {
    profileData = new double[][] { {width/2,0.0},{width/2,p1y*height},{width*p2x,height*p2y},{width*p3x,0.0} };
    profileRight.setData(profileData);
    for (int i=0; i<4; i++) profileData[i][0] = -profileData[i][0];
    profileLeft.setData(profileData);
  }

  // Overwriting methods from parent

  public Object3D[] getObjects3D(DrawingPanel3D _panel) {
    if (hasChanged) updateCoordinates();
    return super.getObjects3D(_panel);
  }

  public void drawQuickly (DrawingPanel3D _panel, java.awt.Graphics2D _g2) {
    if (hasChanged) updateCoordinates();
    super.drawQuickly(_panel,_g2);
  }

  public void draw (DrawingPanel _panel, java.awt.Graphics _g) {
    if (hasChanged) updateCoordinates();
    super.draw(_panel,_g);
  }

  public void interactionPerformed(InteractionEvent _event) {
    if (_event.getSource()==profileRight && _event.getTarget() instanceof InteractionTargetPoligonPoint) {
      InteractionTargetPoligonPoint polEvent = (InteractionTargetPoligonPoint) _event.getTarget();
      int index = polEvent.getPointIndex();
      switch (index) {
        case 1 :
          if (height>0) p1y = profileRight.getData()[1][index]/height; else p1y = 0.0;
          if (p1y<p2y) p2y = p1y;
          break;
        case 2 :
          if (width>0)  p2x = profileRight.getData()[0][index]/width;  else p2x = 0.0;
          if (height>0) p2y = profileRight.getData()[1][index]/height; else p2y = 0.0;
          if (p1y<p2y) p1y = p2y;
          if (p2x<p3x) p3x = p2x;
          break;
        case 3 :
          if (width>0)  p3x = profileRight.getData()[0][index]/width;  else p3x = 0.0;
          if (p2x<p3x) p2x = p3x;
          break;
      }
    }
    else if (_event.getSource()==profileLeft && _event.getTarget() instanceof InteractionTargetPoligonPoint) {
      InteractionTargetPoligonPoint polEvent = (InteractionTargetPoligonPoint) _event.getTarget();
      int index = polEvent.getPointIndex();
      switch (index) {
        case 1 :
          if (height>0) p1y = profileLeft.getData()[1][index]/height; else p1y = 0.0;
          if (p1y<p2y) p2y = p1y;
          break;
        case 2 :
          if (width>0)  p2x = -profileLeft.getData()[0][index]/width;  else p2x = 0.0;
          if (height>0) p2y = profileLeft.getData()[1][index]/height; else p2y = 0.0;
          if (p1y<p2y) p1y = p2y;
          if (p2x<p3x) p3x = p2x;
          break;
        case 3 :
          if (width>0)  p3x = -profileLeft.getData()[0][index]/width;  else p3x = 0.0;
          if (p2x<p3x) p2x = p3x;
          break;
      }
    }
    else if (_event.getSource()==sides && _event.getTarget() instanceof InteractionTargetPoligonPoint) {
      InteractionTargetPoligonPoint polEvent = (InteractionTargetPoligonPoint) _event.getTarget();
      int index = polEvent.getPointIndex();
      switch (index) {
        case 0 : width = -2*sides.getData()[0][index]; if (width<0) width = 0.0;  break;
        case 3 : width =  2*sides.getData()[0][index]; if (width<0) width = 0.0; break;
        case 1 : case 2 : height =  sides.getData()[1][index]; if (height<0) height = 0.0; break;
      }
      data = new double[][] { {-width/2,0.0},{-width/2, height},{width/2, height}, {width/2,0.0}, {0.0,0.0}, {-width/2,0.0} };
      sides.setData(data);
      insideData = new double[][] { {-width/2,0.0},{-width/2, levelOne},{0.0, levelOne},{width/2, levelOne}, {width/2,0.0} };
      inside.setData(insideData);
    }
    else if (_event.getSource()==inside && _event.getTarget() instanceof InteractionTargetPoligonPoint) {
      InteractionTargetPoligonPoint polEvent = (InteractionTargetPoligonPoint) _event.getTarget();
      levelOne = inside.getData()[1][polEvent.getPointIndex()];
      if (levelOne<0) levelOne = 0;
      else if (levelOne>height) levelOne = height;
      insideData = new double[][] { {-width/2,0.0},{-width/2, levelOne},{0.0, levelOne},{width/2, levelOne}, {width/2,0.0} };
      inside.setData(insideData);
    }
    if (showProfiles) {
      if (p1y>1) p1y = 1; else if (p1y<0) p1y = 0;
      if (p2y>1) p2y = 1; else if (p2y<0) p2y = 0;
      if (p2x>0.5) p2x = 0.5; else if (p2x<0) p2x = 0;
      if (p3x>0.5) p3x = 0.5; else if (p3x<0) p3x = 0;
      updateProfiles();
    }
    invokeActions(_event);
  }

  private class DiameterFunction extends Function {
    // returns the diameter of the tank for a given height
    public double eval (double _y) {
      if (showProfiles) {
        if (_y>=p1y*height) return width;
        if (_y>=p2y*height) return width + width*(2*p2x-1)*(_y-p1y*height)/((p2y-p1y)*height);
        return 2*(p2x*width + width*(p3x-p2x)*(p2y*height-_y)/(p2y*height));
      }
      return width;
    }

  }

}
