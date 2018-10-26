package es.uhu.ejs.hardware.phidgets;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;

import es.uhu.hardware.phidgets.AbstractPhidget;
import es.uhu.hardware.phidgets.MotorControlHC;

/**
 * Encapsulates access to OSP's ResourceLoader
 * @author Francisco Esquembre
 * @author Marco Marquez
 * @author Andres Mejias
 * @version June 2012
 */
public class MotorControlHCAdapter extends MotorControlHC {
  private Model mModel;
  private String mIP, mPortNumber, mSerialNumber, mPassword;

  static {
    AbstractPhidget.setVerbose(false);
  }

  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public MotorControlHCAdapter(Model model, String serialNumber, 
      String ipAddress, String portNumber, String password) {
    super();
    mModel = model;
    mIP = ipAddress.trim();
    mPortNumber = portNumber.trim();
    mSerialNumber = serialNumber.trim();
    mPassword = password;
  }

  public boolean connect() {
    if (mIP.length()>0) {
      String ipAddress = ModelElementsUtilities.getValue(mModel,mIP);
      int portNumber = ModelElementsUtilities.getIntegerValue(mModel,mPortNumber);
      if (portNumber==0) portNumber = 5001;
      int serialNumber = ModelElementsUtilities.getIntegerValue(mModel,mSerialNumber);
      String password = ModelElementsUtilities.getValue(mModel,mPassword);
      return connect(serialNumber,ipAddress,portNumber,password);
    }
    return connect(ModelElementsUtilities.getIntegerValue(mModel,mSerialNumber));
  }

}
