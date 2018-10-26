package org.colos.ejs.model_elements.input_output;

import org.colos.ejs.library.Model;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.display.DataFile;

/**
 * Encapsulates access to a DataFile by connecting it to a Model object
 * @author Francisco Esquembre
 * @version 1.0, August 2010
 *
 */
public class DataReader {
  private Model model;
  private String filename;
  private DataFile dataFile;
  
  /**
   * Standard constructor to be called by the simulation
   * @param _model
   * @param _filename The filename to read
   * @see #setFilename(String)
   */
  public DataReader(Model _model, String _filename) {
    this.model = _model;
    this.dataFile = new DataFile(null);
    setFilename(_filename);
  }

  /**
   * Sets the filename to read to a constant String (such as "./myFile.txt")
   * or links it to a String model variable (such as "%myStringVariable%")
   * @return
   */
  public void setFilename(String _filename) {
    this.filename = _filename;
  }

  /**
   * Returns the file name to read
   * @return
   */
  public String getFilename() {
    return ModelElementsUtilities.getValue(model,filename);
  }

  /**
   * Reads the file and returns the double[][] array in it
   * @return null if failed
   */
  public double[][] readData() {
    if (filename==null) return null;
    dataFile.open(getFilename());
    return dataFile.getData2D();
  }

  /**
   * Reads the prescribed file and returns the double[][] array in it
   * @return null if failed
   */
  public double[][] readData(String path) {
    if (path==null) return null;
    dataFile.open(path);
    return dataFile.getData2D();
  }
  
  /**
   * Returns the name of the last data read
   * @return null if failed
   */
  public String getDataName() { 
    return dataFile.getName();
  }

  /**
   * Returns the column names array of the last data read
   * @return
   */
  public String[] getColumnNames(String _firstColumnName) {
    String[] names = dataFile.getColumnNames();
    if (names==null) names = new String[0];
    if (_firstColumnName==null) return names; 
    String[] colNames = new String[names.length+1];
    colNames[0] = _firstColumnName;
    for (int i=0;i<names.length; i++)  colNames[i+1]=names[i];
    return colNames; 
  }
  
}
