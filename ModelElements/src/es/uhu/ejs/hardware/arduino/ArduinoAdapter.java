package es.uhu.ejs.hardware.arduino;

import java.io.File;

import javax.swing.JOptionPane;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.Simulation;
import org.colos.ejs.library.utils.ModelElementsUtilities;

import es.uhu.hardware.arduino.Arduino;

/**
 * Encapsulates access to an Arduino object
 *
 * @author Andres Mejias
 * @author Marco A. Marquez
 * @author Francisco Esquembre
 * @version 1.0 December 2012
 */
public class ArduinoAdapter extends Arduino {
  private Model mModel;
  private String mCommId, mCommParam;
  
  static void extractLibrary() {
    String filename=null, destination=null;
    if (org.opensourcephysics.display.OSPRuntime.isMac()) filename = destination = "librxtxSerial.jnilib";
    else if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      destination = "rxtxSerial.dll";
      filename = "rxtxSerial"+org.opensourcephysics.display.OSPRuntime.getVMBitness()+".dll";
    }
    if (filename!=null) {
      File libFile = new File(filename);
      if (!libFile.exists()) Simulation.extractAs("es/uhu/hardware/utils/dlls/"+filename, destination, true);
    }
  }
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   */
  public ArduinoAdapter(Model model) {
    this(model, "", "");
  }

  /**
   * Standard constructor to be called by the simulation
   * @param _model
   */
  public ArduinoAdapter(Model model, String comId, String commParam) {
    super();
    mModel = model;
    mCommId = comId.trim();
    mCommParam = commParam.trim();
    extractLibrary();
  }

  public boolean connect() {
    if (mCommId.length()>0) {
      String commId = ModelElementsUtilities.getValue(mModel,mCommId);
      int commParam = 0;
      if (mCommParam.length()>0) commParam = ModelElementsUtilities.getIntegerValue(mModel,mCommParam);
      return super.connectEthernet(commId,commParam);
    }
    JOptionPane.showMessageDialog(null,"No IP information provided.\nPlease, use the connectSerial() or connectEthernet() methods.",
        "Arduino Error",JOptionPane.ERROR_MESSAGE);
    return false;
  }

}
