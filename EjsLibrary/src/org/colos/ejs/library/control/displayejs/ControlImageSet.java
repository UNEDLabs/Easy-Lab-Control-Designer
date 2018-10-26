/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.*;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;
//import misc.SplitImage;//FKH20060727
import java.awt.image.ImageProducer;//FKH20060727
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.FilteredImageSource;
import java.awt.image.CropImageFilter;
/**
 * An interactive set of particles
 */
public class ControlImageSet extends ControlElementSet {
  static private final int IMAGE_SET_ADDED=1;
  static private final int MY_IMAGE=IMAGE+IMAGE_SET_ADDED;

  private String imageFile=null;

  protected Drawable createDrawable () {
      elementSet = new ElementSet(1, InteractiveImage.class);
      elementSet.setEnabled(InteractiveElement.TARGET_POSITION,true);  // Backwards compatibility
    return elementSet;
  }

  protected int getPropertiesDisplacement () { return IMAGE_SET_ADDED; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("trueSize");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("image"))            return "File|String|String[] TRANSLATABLE";
    if (_property.equals("trueSize"))         return "boolean|boolean[]";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Variable properties
// ------------------------------------------------
int splitnx=0,splitny=0;//FKH20060728
  public void setValue (int _index, Value _value) {
    switch (_index) {
      case  0 :
        if (_value.getObject() instanceof boolean[]) {
          boolean[] tv = (boolean[]) _value.getObject();
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveImage)elementSet.elementAt(i)).setTrueSize(tv[i]);
        }
        else {
          for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
            ((InteractiveImage)elementSet.elementAt(i)).setTrueSize(_value.getBoolean());
        }
        break;
      default: super.setValue(_index-IMAGE_SET_ADDED,_value); break;
      case MY_IMAGE :
        if (_value instanceof ObjectValue && _value.getObject() instanceof String[]) setImages((String[])_value.getObject());
        else setImage(_value.getString());
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case  0 :
        for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++)
          ((InteractiveImage)elementSet.elementAt(i)).setTrueSize(false);
        break;
      default : super.setDefaultValue (_index-IMAGE_SET_ADDED); return;
      case MY_IMAGE : setImage(null); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "false";
      default : return super.getDefaultValueString(_index-IMAGE_SET_ADDED);
      case MY_IMAGE : return "<none>";
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : return null;
      default: return super.getValue(_index-IMAGE_SET_ADDED);
    }
  }

// -------------------------------------
// private methods
// -------------------------------------
//FKH 2007 if _image end with |nx,ny where nx,ny are integer, then the image will be splited to nx*ny parts
 private String splitImageFile="";
  private void setImage (String _image_) {
    if (_image_==null) {
      return;
    }
    if(_image_.equals(splitImageFile))return;
    String _image = null;
    if (imageFile != null && imageFile.equals(_image_))return; // no need to do it again
    imageFile = _image_;
    int nx=0,ny=0;
    int p=_image_.indexOf('|');
    if(p>0){// find | check for nx, ny
      String param = _image_.substring(p + 1);
      _image = _image_.substring(0, p);
      //      if(splitImageFile==null || _image.compareTo(splitImageFile)!=0){//not the same splited image file
      //       splitnx=splitny=0;
      //      }
      try {
        java.util.StringTokenizer t = new java.util.StringTokenizer(param,",");
        nx = Integer.parseInt(t.nextToken());
        ny = Integer.parseInt(t.nextToken());
      }
      catch (Exception exc) {
        System.out.println("Incorrect integer values for resolution");
        exc.printStackTrace();
      }
    }
    else _image=_image_;
    
    java.awt.Image theImage =  org.opensourcephysics.tools.ResourceLoader.getImage(_image);
/*
    if (icon==null) {
      if (getProperty("_ejs_codebase")!=null) icon = getUtils().resourceIcon(this.getClass(),getProperty("_ejs_codebase"), _image);
      else if (getSimulation()!=null && getSimulation().getCodebase()!=null) icon = getUtils().resourceIcon(this.getClass(),getSimulation().getCodebase().toString(), _image);
      else icon = getUtils().resourceIcon(this.getClass(), (String)null, _image);
    }
 */
    if (theImage!=null) {
      //      if(splitnx*splitny>0 && elementSet.getNumberOfElements()>=splitnx*splitny)splitImage(image,splitnx,splitny);
      //(nx!=splitnx || ny!=splitny||
      if(nx*ny>0 &&  _image_.compareTo(splitImageFile)!=0 ){//FKH20070627 on Thai airplane
        splitnx=nx;
        splitny=ny;
        splitImageFile=new String(_image_);
        splitImage(theImage,nx,ny);
      }
      else for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) elementSet.elementAt(i).getStyle().setDisplayObject(theImage);
    }
  }

  private void setImages (String[] _images) {
    for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {
      java.awt.Image theImage = org.opensourcephysics.tools.ResourceLoader.getImage(_images[i]);
      if (theImage!=null) elementSet.elementAt(i).getStyle().setDisplayObject(theImage);
    }
/*
    String codebase = getProperty("_ejs_codebase");
    if (codebase!=null) {
      for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {
        javax.swing.ImageIcon icon = getUtils().resourceIcon(this.getClass(),codebase,_images[i]);
        if (icon!=null) elementSet.elementAt(i).getStyle().setDisplayObject(icon.getImage());
      }
    }
    else if (getSimulation()!=null && getSimulation().getCodebase()!=null) {
      codebase = getSimulation().getCodebase().toString();
      for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {
        javax.swing.ImageIcon icon = getUtils().resourceIcon(this.getClass(),codebase,_images[i]);
        if (icon!=null) elementSet.elementAt(i).getStyle().setDisplayObject(icon.getImage());
      }
    }
    else {
      for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {
        javax.swing.ImageIcon icon = getUtils().resourceIcon(this.getClass(),(String) null,_images[i]);
        if (icon!=null) elementSet.elementAt(i).getStyle().setDisplayObject(icon.getImage());
      }
    }
 */
  }

//FKH20060727
 private void splitImage(Image image,int nx,int ny){
   Image nimage=null;
   ImageProducer producer=null;
   java.awt.Canvas canvas=new java.awt.Canvas();
   SplitImage splitter;
   splitter=new SplitImage(image,nx,ny);
   try{
//     System.out.println("split image="+nx+":"+ny);
     for (int i=0, n=elementSet.getNumberOfElements(); i<n; i++) {//
       producer = splitter.getImageProducer(i%nx, i/ny);
         nimage = canvas.createImage(producer);
         javax.swing.ImageIcon icon = new javax.swing.ImageIcon(nimage);
         elementSet.elementAt(i).getStyle().setDisplayObject(icon.getImage());
       }
   }
   catch(Exception ex){
       System.out.println("Error loading image "+ex.toString());
   }

 }

 class SplitImage implements ImageObserver
 {
         /**
           * This class splits an image in different parts.
           * @param image the image to split
           * @param rows number of images per column. It has to be greater than 0
           * @param columns number of images per row. It has to be greater than 0
           */
         public SplitImage(Image image, int rows, int columns)
         {
                 this.rows=rows;
                 this.columns=columns;
                 this.image=image;

                 //arguments checking
                 if (rows<1 || columns<1)
                         error=readen=true;
                 else
                 {
                         height=image.getHeight(this);
                         width=image.getWidth(this);
                         checkIfStart();
                 }
         }

         /**
           * Gives the height of each of the splitted images.
           * @return the height of each of the splitted images, or -1 if there has been
           * an error (this error may have been produced in the initialization, if the
           * number of rows of columns was not valid).
           * @exception InterruptedException
           */
         public synchronized int getHeight() throws InterruptedException
         {
                 while (!readen)
                         wait();
                 return finalHeight;
         }

         /**
           * Gives the width of each of the splitted images.
           * @return the width of each of the splitted images, or -1 if there has been
           * an error (this error may have been produced in the initialization, if the
           * number of rows of columns was not valid).
           * @exception InterruptedException
           */
         public synchronized int getWidth() throws InterruptedException
         {
                 while (!readen)
                         wait();
                 return finalWidth;
         }

         /**
           * Generates an imageProducer for the selected portion of the image.
           * @return the ImageProducer generated, or null if there has been an error.
           * The error may have been generated because (1) bad initialization, wrong number
           * of rows or columns (2) error reading the image (3) wrong parameter in the call
           * to the method, row and/or col are not valids
           * @param row the row of the portion. It must be a number between 0 and the number of rows - 1
           * @param col the column of the portion. It must be a number between 0 and the number of columns - 1
           * @exception InterruptedException
           */
         public synchronized ImageProducer getImageProducer(int row, int col) throws InterruptedException
         {
                 while (!readen)
                         wait();
                 ImageProducer imageProducer=null;
                 if (!error && row>=0 && col>=0 && row<rows && col < columns)
                         imageProducer=new FilteredImageSource(image.getSource(), new CropImageFilter(col*finalWidth,row*finalHeight, finalWidth, finalHeight));
                 return imageProducer;
         }

         /**
           *	ImageObserver Method
           */
         public synchronized boolean imageUpdate(Image img, int flags, int x, int y, int w, int h)
         {
                 boolean ret;
                 if ((flags&ImageObserver.ERROR)!=0)
                 {
                         ret=false;
                         error=readen=true;
                         notifyAll();
                 }
                 else
                 {
                         if ((flags&ImageObserver.WIDTH)!=0)
                                 width=w;
                         if ((flags&ImageObserver.HEIGHT)!=0)
                                 height=h;
                         ret=!checkIfStart();
                 }
                 return ret;
         }

         /**
           * Checks if the height and the width are already available, and -if so-, it
           * creates the needed filters;
           * @return true if the checking has been positive
           */
         synchronized boolean checkIfStart()
         {
                 boolean ret=width!=-1 && height!=-1;
                 if (ret)
                 {
                         finalWidth=width/columns;
                         finalHeight=height/rows;
                         readen=true;
                         notifyAll();
                 }
                 return ret;
         }

         /**
           * Identifies any error given by the Image Producer
           */
         boolean error=false;
         /**
           * Identifies when the image has been readed (in fact, when the height and
           * width are known)
           */
         boolean readen=false;
         /**
           * The image being splitting
           */
         Image image;
         /**
           * Number of rows and columns used to split the image
           */
         int rows, columns;
         /**
           * Dimensions of the original image
           */
         int width=-1, height=-1;
         /**
           * Dimensions of the final image
           */
         int finalWidth=-1, finalHeight=-1;
 }

} // End of class
