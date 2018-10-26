package org.colos.ejs.model_elements.external;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.TreeMap;

import org.colos.ejs.library.Model;

import org.opensourcephysics.display.DatasetManager;
import org.opensourcephysics.media.core.DataTrackSupport;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * Encapsulates access to a DataTrackSupport object
 * @author Francisco Esquembre
 * @version 1.0, June 2015
 *
 */
public abstract class TrackerConnection {
  private Model mModel;

  private int mID = hashCode();
  private String mNameSent;
  private boolean mConnected=false;
  private DatasetManager mManager;
  private TreeMap<String, Object> mDeleteMessage;

  abstract protected void configMessage(TreeMap<String, Object> message); // to be overwritten by children 
  abstract protected String getDataName(); // to be overwritten by children 
  
  private PropertyChangeListener mListener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent e) {
      if (e.getPropertyName().equals("tracker_ready")) {
        mConnected = true;
      }
      else if (e.getPropertyName().equals("tracker_exited")) {
        mConnected = false;
      }
      // send initial data and video specs
      // extracting video from jar is handled by DataTrackTool (Tracker)
      if (mConnected) {     
//        System.err.println("I am about to sendMessage");
        // Clear all data if any
        if (mDeleteMessage!=null) {
          DataTrackSupport.sendMessage(mID, mDeleteMessage);
          mDeleteMessage = null;
        }
        // Add new data
        TreeMap<String, Object> message = new TreeMap<String, Object>();
        mNameSent = getDataName();
        mManager.setName(mNameSent);
        configMessage(message);
        message.put("data", mManager);
//        System.out.println ("Message = "+message);
        DataTrackSupport.sendMessage(mID, message);
      }
    }
  } ;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public TrackerConnection(Model model) { 
    mModel = model;
    mManager = new DatasetManager();
    mManager.setXYColumnNames(0, "t", "x");
    mManager.setXYColumnNames(1, "t", "y");
  }

  public boolean connect() {
    if (mConnected) {
//      System.err.println("I am sending data");
      return DataTrackSupport.sendData(mID, mManager);
    }
//    System.err.println("I am connecting");
    return DataTrackSupport.connect(mID, mListener);
  }
  
//  public void disconnect() {
//    mConnected = false;
//  }

  public void clear() {
    if (mNameSent!=null) {
      mDeleteMessage = new TreeMap<String, Object>();
      mDeleteMessage.put("deleteTracks", new String[] { mNameSent });
      if (mConnected) { // else, it will be sent when reconnecting
//        System.err.println("I am sending delete data");
        DataTrackSupport.sendMessage(mID, mDeleteMessage);
        mDeleteMessage = null;
      }
    }
    mManager.clear();
  }
  
  public boolean isConnected() { return mConnected; } 

  public void append(double t, double x, double y) {
    mManager.append(0, t, x);
    mManager.append(1, t, y);
//    System.err.println ("Point added t="+t+", x="+x+", y="+y);
    update();
  }

  public void append(double[] t, double[] x, double[] y) {
    mManager.append(0, t, x);
    mManager.append(1, t, y);
    update();
  }
  
  public boolean update() {
    if (!mConnected) return false;
//    System.err.println("I am sending appended data");
    return DataTrackSupport.sendAppendedData(mID, mManager);
  }

  //public void clear() { mManager.clear(); }

  public DatasetManager getDatasetManager() { return mManager; }
  
//  public Dataset getDataset(int datasetIndex) { return mManager.getDataset(datasetIndex); }
  
  // ----------------------------------
  // protected methods
  //----------------------------------

  protected void addResourceToMessage(TreeMap<String, Object> message, String key, String value) { //, boolean isResource) {
    if (mModel.getSimulation().isUnderEjs()) {
      Resource resource = ResourceLoader.getResource(value);
      if (resource!=null) {
        message.put(key, resource.getAbsolutePath());
        return;
      }
    }
    message.put(key, value);
  }
    
}
