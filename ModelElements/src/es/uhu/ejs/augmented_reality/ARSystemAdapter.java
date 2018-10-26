package es.uhu.ejs.augmented_reality;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.drawing3d.DrawingPanel3D;

/**
 * Encapsulates access to OSP's ResourceLoader
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version September 2012
 */
public class ARSystemAdapter extends ARSystem {
  private Model mModel;
  private String mURL, mXRes, mYRes, mConfigurationFile, mUsername, mPassword, mDrawingPanel3D;

  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @see #setFilename(String)
   */
  public ARSystemAdapter(Model model, String url, String xRes, String yRes, 
      String configurationFile, String username, String password, String drawingPanel3D) {
    mModel = model;
    mURL = url.trim();
    mXRes = xRes.trim();
    mYRes = yRes.trim();
    mConfigurationFile = configurationFile.trim();
    mUsername = username;
    mPassword = password;
    mDrawingPanel3D = drawingPanel3D.trim();
  }

  public boolean connect() {
    String url = ModelElementsUtilities.getValue(mModel,mURL);
    if (url.isEmpty()) { // The user must set the camera directly
      super.connect();
      return false;
    }
    int xRes = ModelElementsUtilities.getIntegerValue(mModel,mXRes);
    int yRes = ModelElementsUtilities.getIntegerValue(mModel,mYRes);
    String configurationFile = mConfigurationFile.isEmpty() ? null : ModelElementsUtilities.getValue(mModel,mConfigurationFile);
    System.err.println("Setup : "+url+","+ xRes+","+yRes+","+configurationFile);
    boolean ok = (configurationFile==null) ? super.setCamera(url, xRes, yRes) : super.setCamera(url, xRes, yRes, configurationFile);
    if (!ok) return false;
    String username = mUsername.isEmpty() ? null : ModelElementsUtilities.getValue(mModel,mUsername);
    String password = mPassword.isEmpty() ? null : ModelElementsUtilities.getValue(mModel,mPassword);
    super.setUserAndPassword(username, password);
    System.err.println("User/password : "+username+","+ password);
    DrawingPanel3D panel3D = (DrawingPanel3D) ModelElementsUtilities.getViewElement(mModel,mDrawingPanel3D);
    System.err.println("panel : "+panel3D);
    super.setDrawingPanel3D(panel3D);
    super.connect();
    return true;
  }

}
