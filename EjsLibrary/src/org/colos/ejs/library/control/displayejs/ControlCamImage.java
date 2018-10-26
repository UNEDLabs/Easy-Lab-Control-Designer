/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import com.hector.*;
import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;


/**
 * An WebCam image interactive
 */

public class ControlCamImage extends ControlInteractiveElement {
  static protected final int CAM_IMAGE_ADDED = 4; // Number of new properties
  static protected final int MY_SIZE_X = ControlInteractiveElement.SIZE_X+CAM_IMAGE_ADDED;
  static protected final int MY_SIZE_Y = ControlInteractiveElement.SIZE_Y+CAM_IMAGE_ADDED;

  protected InteractiveImage image;
  protected Contenedor c = new Contenedor();
  protected VideoClass videoClass = new VideoClass(c);
  protected UpdateImage updateImage = new UpdateImage(c);
  protected String _url = null; // guarda el valor de la URL anterior
  protected boolean _mjpeg = false; // guarda el valor del formato de video anterior
  protected boolean enabled = true; // Enable/disable the connection
  protected int _delay = 20; // guarda el valor del retardo anterior


  public ControlCamImage () { super (); enabledEjsEdit = true; }

  protected Drawable createDrawable () {
      image = new InteractiveImage();
      image.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
      image.setSizeXY(1,1);
    return image;
  }

  protected int getPropertiesDisplacement () { return CAM_IMAGE_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------


  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("url");
      infoList.add ("mjpeg");
      infoList.add ("delay");
      infoList.add ("enabled");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo (String _property) {
    if (_property.equals("url"))      return "String";
    if (_property.equals("mjpeg"))    return "boolean";
    if (_property.equals("delay"))    return "int";
    if (_property.equals("enabled"))  return "boolean";
    return super.getPropertyInfo(_property);

  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    //System.out.println("Pasa por setValue");
    switch (_index) {
      case  0 : if (!_value.getString().equals(videoClass.getURL())) setConnection(_value.getString(), videoClass.getMJPEG(), videoClass.getDelay()); break;
      case  1 : if (_value.getBoolean()!=videoClass.getMJPEG()) setConnection(videoClass.getURL(), _value.getBoolean(), videoClass.getDelay()); break;
      case  2 : if (_value.getInteger()!=videoClass.getDelay()) setConnection(videoClass.getURL(), videoClass.getMJPEG(), _value.getInteger()); break;
      case  3 : if (enabled!=_value.getBoolean()) {
        enabled = _value.getBoolean();
        setConnection(videoClass.getURL(), videoClass.getMJPEG(), videoClass.getDelay()); 
      }
      break;
      default: super.setValue(_index-CAM_IMAGE_ADDED,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    //System.out.println("Pasa por setDefaultValue");
    switch (_index) {
      case  0 : setConnection(null,false,20); break;
      case  1 : videoClass.setMJPEGFormat(false); break;
      case  2 : videoClass.setImageDelay(20); break;
      case  3 : enabled = true; setConnection(videoClass.getURL(), videoClass.getMJPEG(), videoClass.getDelay()); break;
      default: super.setDefaultValue(_index-CAM_IMAGE_ADDED); break;
      case MY_SIZE_X : sizeValues[0].value=1.0; myElement.setSizeX(sizeValues[0].value*scalex); break;
      case MY_SIZE_Y : sizeValues[1].value=1.0; myElement.setSizeY(sizeValues[1].value*scaley); break;
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3: 
        return null;
      default: return super.getValue(_index-CAM_IMAGE_ADDED);
    }

  }

// -------------------------------------
// private methods
// -------------------------------------

  private void setConnection (String url, boolean mjpeg, int delay){
    if (!enabled) {
      if (videoClass!=null) videoClass.stopRunning();
      if (updateImage!=null) updateImage.stopRunning = true;
      return;
    }
    if (url==null) return;
    _url = url;
    _mjpeg = mjpeg;
    _delay = delay;
    videoClass.stopRunning(); //stop();
    updateImage.stopRunning = true; // stop();

    // wait until stop thread
    try {
      videoClass.join();
      updateImage.join();
    }
    catch (InterruptedException ex) {}
    videoClass = new VideoClass(c);
    updateImage = new UpdateImage(c);
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    videoClass.setPriority(Thread.NORM_PRIORITY);
    updateImage.setPriority(Thread.MIN_PRIORITY);
    videoClass.setURL(url);
    videoClass.setMJPEGFormat(mjpeg);
    videoClass.setImageDelay(delay);
    videoClass.start();
    updateImage.start();
  }

  public class UpdateImage extends Thread {

    private Contenedor contenedor;
    private boolean stopRunning=false;

    public UpdateImage(Contenedor c){
      contenedor = c;
    }

    public void run(){
//      System.out.println("el run() de UpdateImage ha empezado");
      while (true){
        image.getStyle().setDisplayObject(contenedor.get());
        if (stopRunning) return;
      }
      //System.out.println("el run() de UpdateImage ha finalizado");
    }
  }

} // Fin de la clase ControlCamImage


