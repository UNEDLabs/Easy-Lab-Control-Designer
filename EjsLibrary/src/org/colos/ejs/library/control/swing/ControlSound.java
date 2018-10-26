/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;

import java.applet.*;

/**
 * An element to play sound according to the state of an internal
 * variable. The element does not change this variable
 */
public class ControlSound extends ControlCheckBox {
  static final private int SOUND_ADDED = 2;
  static final int SOUND_VARIABLE  = ControlCheckBox.VARIABLE+SOUND_ADDED;
  static final int SOUND_SELECTED  = ControlCheckBox.SELECTED+SOUND_ADDED;

  private AudioClip clip=null;
  private String audioFile = null;
  private boolean playing=false;
  private boolean shouldLoop = true;

  public ControlSound () {
    super ();
    checkbox.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) {
          playing = checkbox.isSelected(); 
          if (playing) play();
          else stop();
        }
      }
    );
  }

  protected int getVariableIndex () { return ControlSound.SOUND_VARIABLE; }
  protected int getValueIndex () { return ControlSound.SOUND_SELECTED; }

  public void setAudioClip(String _codebase, String _audioFile) {
    if (_audioFile==null) { stop(); clip = null; return; }
    clip =  org.opensourcephysics.tools.ResourceLoader.getAudioClip(_audioFile);
//    if (clip==null) clip = getUtils().resourceClip (this.getClass(),_codebase,_audioFile);
  }

  public void destroy() {
    if (clip!=null) clip.stop();
    clip = null;
    super.destroy();
  }

  public void play() {
    if (clip==null) return;
    if (shouldLoop) clip.loop();
    else clip.play();
  }

  public void stop() { if (clip!=null) clip.stop(); }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("audiofile");
      infoList.add ("loop");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("audiofile"))  return "File|String TRANSLATABLE";
    if (_property.equals("loop"))       return "boolean";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : setAudioFile(_value.getString()); break; // audiofile
      case 1 : 
        if (shouldLoop!=_value.getBoolean()) {
          shouldLoop = _value.getBoolean();
          if (playing) { stop(); play(); }
        }
        break;
      case SOUND_VARIABLE :
        if (_value.getBoolean()!=playing) {
          playing=_value.getBoolean();
          if (playing) play();
          else stop();
        }
        super.setValue(ControlCheckBox.VARIABLE,_value);
        break;
      default: super.setValue(_index-SOUND_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : setAudioClip(null,null); audioFile = null; break;
      case 1 : 
        if (!shouldLoop) {
          shouldLoop = true;
          if (playing) { stop(); play(); }
        }
        break;
      default: super.setDefaultValue(_index-SOUND_ADDED); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : return "<none>";
      case 1 : return "true";
      default : return super.getDefaultValueString(_index-SOUND_ADDED);
    }
  }
  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : 
        return null;
      default: return super.getValue(_index-SOUND_ADDED);
    }
  }

// -------------------------------------
// private methods
// -------------------------------------

  private void setAudioFile (String _audio) {
    if (audioFile!=null && audioFile.equals(_audio)) return; // no need to do it again
    audioFile = _audio;
    if (getProperty("_ejs_codebase")!=null) setAudioClip (getProperty("_ejs_codebase"),_audio);
    else if (getSimulation()!=null && getSimulation().getCodebase()!=null)
      setAudioClip (getSimulation().getCodebase().toString(),_audio);
    else setAudioClip (null,_audio);
  }

}
