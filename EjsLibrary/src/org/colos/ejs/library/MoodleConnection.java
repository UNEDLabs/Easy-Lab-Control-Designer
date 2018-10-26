/**
 * The package contains the protocol communication with a Moodle course
 * Copyright (c) May 2011 F. Esquembre and Luis de la Torre
 * @author F. Esquembre (http://fem.um.es) and Luis de la Torre.
 * @version 2 adapted from EmersionConnection
 */

package org.colos.ejs.library;

import java.net.*;

import javax.swing.*;

import java.awt.*;

import javax.swing.Timer;

import java.awt.event.*;

import org.colos.ejs.library.utils.SwingWorker;

import java.io.StringReader;

import javax.json.*;

/**
 * All that is needed to communicate with Moodle
 */
public class MoodleConnection implements MoodleLink {
  static javax.swing.Icon moodleIcon =  org.opensourcephysics.tools.ResourceLoader.getIcon("org/colos/ejs/library/resources/Moodle.png");

  private URL moodle_upload_file = null; //url to the upload_file.php file
  private URI send_files_list = null; //url to the send_files_list.php file
  private String ejsapp_id = null;  //ejsapp identificator in Moodle's database
  private String user_id = null;  //user identificator in Moodle's database
  private String context_id = null; //Context_ID in the Moodle session
  private String moodle_lang = null; //Selected language in Moodle
  private String moodle_user = null; //User's name in Moodle
  private String moodle_pass = null; //User's encrypted password in Moodle
  //private String moodle_prefix = null; //Moodle's prefix for the sql tables
  private java.awt.Component parentComponent = null;
  private Simulation simulation=null; // In case Moodle belongs to a simulation
  private SavePanel savePanel = new SavePanel();
  private ReadPanel readPanel = new ReadPanel();
  private ProgressDialog progressMonitor;
  private LongTask task;
  private Timer timer;
  private int previous;

  public MoodleConnection (javax.swing.JApplet _applet, Simulation _simulation) {
    this.simulation = _simulation;
    String urlSaveStr = _applet.getParameter("moodle_upload_file");
    int fin = urlSaveStr.indexOf("upload_file.php");
    String urlLoadStr = urlSaveStr.substring(0,fin)+"send_files_list.php";
    try {
      moodle_upload_file = new URL(urlSaveStr);   
      context_id = _applet.getParameter("context_id");
      user_id = _applet.getParameter("user_id");
      ejsapp_id = _applet.getParameter("ejsapp_id");
      moodle_lang = _applet.getParameter("language");
      moodle_user = _applet.getParameter("user_moodle");
      moodle_pass = _applet.getParameter("password_moodle");
      _simulation.setLocale(moodle_lang);
      System.out.println ("Connected to Moodle\n");
    }
    catch (MalformedURLException mue) {
      if (moodle_upload_file!=null) System.out.println ("Malformed URL exception for URL = <"+urlSaveStr+">");
      moodle_upload_file = null;
    }
    try {
      send_files_list = new URI(urlLoadStr);
    }
    catch (URISyntaxException mue) {
      if (send_files_list!=null) System.out.println ("URI syntax exception for URL = <"+urlLoadStr+">");
      send_files_list = null;
    }
    if (context_id==null) System.out.println("context_id parameter was not found");
    if (user_id==null) System.out.println("user_id parameter was not found");
    if (ejsapp_id==null) System.out.println("ejsapp_id parameter was not found");
    if (moodle_lang==null) System.out.println("language parameter was not found");
    if (moodle_user==null) System.out.println("username parameter was not found");
    if (moodle_pass==null) System.out.println("password parameter was not found");
  }


// ----------------------------------------
// Utilities
// ----------------------------------------

  /**
   * Whether there is a valid connection to a server.
   * @return boolean
   */
  public boolean isConnected () {
    //TODO: Use moodle_user and moodle_pass in MoodleConnection to check whether the user really has access to upload_file.php and send_files_list.php
    //Right now, we only tried to establish a url connection to them without checking anything else and, if it works, then we understand that the applet is connected to Moodle.
    return (moodle_upload_file!=null) && (send_files_list!=null);
  }

  /**
   * Sets the parent component of any subsequent message window such as
   * a JOptionPane.
   * @param _component java.awt.Component
   */
  public void setParentComponent (java.awt.Component _component) { parentComponent = _component; }

  /**
   * Sets the name label for any message window
   * @param _label String
   */
  public void setNameLabel (String _label) { savePanel.nameLabel.setText (_label); }

  /**
   * Sets the annotation label for any message window
   * @param _label String
   */
  public void setAnnotationLabel (String _label) { savePanel.annotationLabel.setText (_label); }


// ----------------------------------------
// Saving
// ----------------------------------------

  private void delay(int mseconds) {
    try {
        //java.lang.Thread.sleep(mseconds);
        Thread.sleep(mseconds);    // Inserted delay
    }
    catch (InterruptedException e) {
        //do nothing!
        System.out.println("Delay interrupted!");
    }
  }

  /**
   * Saves a binary data to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _data byte[]
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveBinary (String _filename, String _annotation, byte[] _data) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;

    task = new LongTask(_filename, _annotation, _data);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves an image to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _image Image
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveImage (String _filename, String _annotation, java.awt.Image _image) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    int index = _filename.lastIndexOf('.');
    if (index==0) _filename += ".gif";
    if (index>0) _filename = _filename.substring(0,index)+"_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+_filename.substring(index);
    else _filename += "_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+".gif";
    if (_filename.length()<=0) return null;

    task = new LongTask(_filename, _annotation, _image);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves a text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _text String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveText (String _filename, String _annotation, String _text) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    int index = _filename.lastIndexOf('.');
    if (index==0) {
      if(_annotation.contains("record")) _filename += ".rec";
      if(_annotation.contains("controller")) _filename += ".cnt";
      else _filename += ".txt";
    }
    if (index>0) {
      _filename = _filename.substring(0,index)+"_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+_filename.substring(index);
      if(_annotation.contains("record")) {
        index = _filename.lastIndexOf('.');
        _filename = _filename.substring(0,index)+".rec";
      }
      if(_annotation.contains("controller")) {
        index = _filename.lastIndexOf('.');
        _filename = _filename.substring(0,index)+".cnt";
      }
    } else {
      if(_annotation.contains("record")) _filename += "_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+".rec";
      else if(_annotation.contains("controller")) _filename += "_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+".cnt";
      else _filename += "_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+".txt";
    }
    if (_filename.length()<=0) return null;

    task = new LongTask(_filename, _annotation, _text, true);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves a XML text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _xml String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveXML (String _filename, String _annotation, String _xml) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;
    int index = _filename.lastIndexOf('.');
    if (index==0) _filename += ".xml";
    if (index>0) _filename = _filename.substring(0,index)+"_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+_filename.substring(index);
    else _filename += "_context_id_"+context_id+"_user_id_"+user_id+"_ejsapp_id_"+ejsapp_id+".xml";
    if (_filename.length()<=0) return null;

    task = new LongTask(_filename, _annotation, _xml, false);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }


// ----------------------------------------
// Reading
// ----------------------------------------

  public byte[] readBinary (String _ext) {
    if (!isConnected()) return null;
    //Obtain list of saved files which can be loaded by the EJS lab.
    //String fileToLoad = readPanel.chooseFile("fileNumber1.xml;fileNumber2.xml");   
    //return("url:"+fileToLoad);
    return null;
  }

  private static String readAll(java.io.Reader rd) throws java.io.IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }

  public String readText (String _fileName, String _type) {
    if (!isConnected()) return null;
    String fileToLoad = "";
    if (_fileName=="") {
      String query;
      if (_type.equals("record")) query = "ejsapp_id="+ejsapp_id+"&type=.rec";
      else if (_type.equals("controller")) query = "ejsapp_id="+ejsapp_id+"&type=.cnt";
      else query = "ejsapp_id="+ejsapp_id+"&type="+_type;
      try {
        URI send_files_list_params = new URI(send_files_list.getScheme(), send_files_list.getAuthority(),
                send_files_list.getPath(), query, send_files_list.getFragment());
        java.io.InputStream is = send_files_list_params.toURL().openStream();
        try {
          java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JsonReader jsonReader = Json.createReader(new StringReader(jsonText));
          JsonObject json = jsonReader.readObject();
          jsonReader.close();
          fileToLoad = "url:"+ readPanel.chooseFile(json);
        } finally {
          is.close();
        }
      } catch (Exception e) {System.out.println("CAN'T OPEN STREAM CONNECTION: "+e);}
    } else fileToLoad = _fileName; //TODO
    return(fileToLoad);
  }

  public String readText (String _fileName) {
    if (!isConnected()) return null;
    String fileToLoad = "";
    if (_fileName=="") {
      try {
        URI send_files_list_params = new URI(send_files_list.getScheme(), send_files_list.getAuthority(),
                send_files_list.getPath(), "ejsapp_id="+ejsapp_id+"&type=text", send_files_list.getFragment());
        java.io.InputStream is = send_files_list_params.toURL().openStream();
        try {
          java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JsonReader jsonReader = Json.createReader(new StringReader(jsonText));
          JsonObject json = jsonReader.readObject();
          jsonReader.close();
          fileToLoad = "url:"+ readPanel.chooseFile(json);
        } finally {
          is.close();
        }
      } catch (Exception e) {System.out.println("CAN'T OPEN STREAM CONNECTION: "+e);}
    } else fileToLoad = _fileName; //TODO
    return(fileToLoad);
  }

  public String readXML (String _fileName) {
    if (!isConnected()) return null;
    String fileToLoad = "";
    if (_fileName=="") {
      try {
        URI send_files_list_params = new URI(send_files_list.getScheme(), send_files_list.getAuthority(),
                send_files_list.getPath(), "ejsapp_id="+ejsapp_id+"&type=.xml", send_files_list.getFragment());
        java.io.InputStream is = send_files_list_params.toURL().openStream();
        try {
          java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JsonReader jsonReader = Json.createReader(new StringReader(jsonText));
          JsonObject json = jsonReader.readObject();
          jsonReader.close();
          fileToLoad = "url:"+ readPanel.chooseFile(json);
        } finally {
          is.close();
        }
      } catch (Exception e) {System.out.println("CAN'T OPEN STREAM CONNECTION: "+e);}
    } else fileToLoad = _fileName; //TODO
    return(fileToLoad);
  }

  public java.awt.Image readImage (String _ext) {
    if (!isConnected()) return null;
    //Obtain list of saved files which can be loaded by the EJS lab.
    //String fileToLoad = readPanel.chooseFile("fileNumber1.xml;fileNumber2.xml");   
    //return("url:"+fileToLoad);
    return null;
  }


// ---------------------------------
// Private classes
// ---------------------------------
  
//This method returns a buffered image with the contents of an image
  private java.awt.image.BufferedImage toBufferedImage(Image image) {
      if (image instanceof java.awt.image.BufferedImage) {
          return (java.awt.image.BufferedImage)image;
      }

      // This code ensures that all the pixels in the image are loaded
      image = new ImageIcon(image).getImage();

      // Determine if the image has transparent pixels
      boolean hasAlpha = hasAlpha(image);

      // Create a buffered image with a format that's compatible with the screen
      java.awt.image.BufferedImage bimage = null;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      try {
          // Determine the type of transparency of the new buffered image
          int transparency = Transparency.OPAQUE;
          if (hasAlpha) transparency = Transparency.BITMASK;
          // Create the buffered image
          GraphicsDevice gs = ge.getDefaultScreenDevice();
          GraphicsConfiguration gc = gs.getDefaultConfiguration();
          bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
      } catch (HeadlessException e) {
          // The system does not have a screen
      }

      if (bimage == null) {
          // Create a buffered image using the default color model
          int type = java.awt.image.BufferedImage.TYPE_INT_RGB;
          if (hasAlpha) {
              type = java.awt.image.BufferedImage.TYPE_INT_ARGB;
          }
          bimage = new java.awt.image.BufferedImage(image.getWidth(null), image.getHeight(null), type);
      }

      // Copy image to buffered image
      Graphics g = bimage.createGraphics();

      // Paint the image onto the buffered image
      g.drawImage(image, 0, 0, null);
      g.dispose();

      return bimage;
  }
  
  //Determining if an image has transparent pixels
  public static boolean hasAlpha(Image image) {
    // If buffered image, the color model is readily available
    if (image instanceof java.awt.image.BufferedImage) {return ((java.awt.image.BufferedImage)image).getColorModel().hasAlpha();}

    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    java.awt.image.PixelGrabber pg = new java.awt.image.PixelGrabber(image, 0, 0, 1, 1, false);
    try {pg.grabPixels();} catch (InterruptedException e) {}

    // Get the image's color model
    return pg.getColorModel().hasAlpha();
  }

  
//---------------------------------
//Private classes
//--------------------------------- 
  
  private void httpRequest (String _filename, String _textToSend, byte[] _data, java.awt.Image _image) {
    
    //Sending ejsapp_id by GET method:
    /*try {
      String urlSaveStr = moodle_upload_file + "?" + "ejsapp_id=" + ejsapp_id;
      URL url = new URL(urlSaveStr);
      URLConnection urlConnectionGET = url.openConnection ();
      //Get the response
      java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(urlConnectionGET.getInputStream()));
      StringBuffer sb = new StringBuffer();
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
      rd.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }*/
    
    //Sending files by POST multipart form:
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "***232404jkg4220957934FW**";
    
    try {
      URLConnection urlConnection = moodle_upload_file.openConnection();
      ((HttpURLConnection)urlConnection).setRequestMethod("POST");
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.setUseCaches(false);
      urlConnection.setRequestProperty("Connection", "Keep-Alive");
      urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
   
      // Prepare to send the file
      java.io.DataOutputStream dos = new java.io.DataOutputStream(urlConnection.getOutputStream() );
      dos.writeBytes(twoHyphens + boundary + lineEnd);
      dos.writeBytes("Content-Disposition: form-data; name=\"user_file\";" + " filename=\"" + _filename +"\"" + lineEnd);
      dos.writeBytes(lineEnd);

      // Data to write
      if (_textToSend!=null) dos.writeBytes(_textToSend);
      if (_data!=null) dos.write(_data);
      if (_image!=null){
        //Converting _image to an array of bytes:
        java.awt.image.BufferedImage bufferedImage = toBufferedImage(_image);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "gif" /* "png" "jpeg" ... format desired */, baos);
        baos.flush();
        byte[] resultImageAsRawBytes = baos.toByteArray();
        baos.close();
        //Writing the byte-form image:
        dos.write(resultImageAsRawBytes);
      }
   
      // Send multipart form data necessary after file data
      dos.writeBytes(lineEnd);
      dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
    
      // Read the server response (debugging only)
      //System.out.println("Server response is: \n");
      java.io.DataInputStream inStream = new java.io.DataInputStream (urlConnection.getInputStream() );
      //String str;
      //while (( str = inStream.readLine()) != null) {
      //  System.out.println(str);
      //  System.out.println("");
      //}
      inStream.close();
      //System.out.println("\nEND Server response  ");
      
      // Close streams
      dos.flush();
      dos.close();
    }
    catch (MalformedURLException ex) {
      System.out.println("CLIENT REQUEST: "+ex);
    }
    catch (java.io.IOException ioe) {
      System.out.println("CLIENT REQUEST: "+ioe);
    }
    
  }
  
  
//---------------------------------
//Private classes
//--------------------------------- 
  
  private java.awt.Component getParentComponent() {
    if (simulation != null) return simulation.getParentComponent();
    return parentComponent;
  }

  private void showException (Exception _exc) {
    _exc.printStackTrace();
    System.out.println ("A dialog should appear now");
    JOptionPane.showMessageDialog(getParentComponent(),_exc.getLocalizedMessage());
  }

  
// ---------------------------------
// Private classes
// ---------------------------------
  
  /**
   * {@link javax.swing.JOptionPane} that can be modified for creating resizable
   * dialogs and so on. Default implementation of {@link javax.swing.JOptionPane}
   * creates allways unresizable dialog.
   */
  private class ModifiableJOptionPane extends JOptionPane {
    
    private boolean resizable;

//    public ModifiableJOptionPane() {
//      super();
//    }

//    /**
//     * @param message
//     */
//    public ModifiableJOptionPane(Object message) {
//      super(message);
//    }

//    /**
//     * @param message
//     * @param messageType
//     */
//    public ModifiableJOptionPane(Object message, int messageType) {
//      super(message, messageType);
//    }

//    /**
//     * @param message
//     * @param messageType
//     * @param optionType
//     */
//    public ModifiableJOptionPane(Object message, int messageType, int optionType) {
//      super(message, messageType, optionType);
//    }

    /**
     * @param message
     * @param messageType
     * @param optionType
     * @param icon
     */
    public ModifiableJOptionPane(Object message, int messageType, int optionType, Icon icon) {
      super(message, messageType, optionType, icon);
    }

//    /**
//     * @param message
//     * @param messageType
//     * @param optionType
//     * @param icon
//     * @param options
//     */
//    public ModifiableJOptionPane(Object message, int messageType, int optionType,
//                                Icon icon, Object[] options) {
//      super(message, messageType, optionType, icon, options);
//    }

//    /**
//     * @param message
//     * @param messageType
//     * @param optionType
//     * @param icon
//     * @param options
//     * @param initialValue
//     */
//    public ModifiableJOptionPane(Object message, int messageType, int optionType,
//                                Icon icon, Object[] options, Object initialValue) {
//      super(message, messageType, optionType, icon, options, initialValue);
//    }

    
    /**
     * @see javax.swing.JOptionPane#createDialog(java.awt.Component, java.lang.String)
     */
    public JDialog createDialog(Component parentComp, String title) throws HeadlessException {
      JDialog dialog = super.createDialog(parentComp, title);
      dialog.setResizable(isResizable());
      return dialog;
    }
    
    
    /**
     * @see javax.swing.JOptionPane#createInternalFrame(java.awt.Component, java.lang.String)
     */
    public JInternalFrame createInternalFrame(Component parentComp, String title) {
      JInternalFrame frame = super.createInternalFrame(parentComp, title);
      frame.setResizable(isResizable());
      return frame;
    }
    
    public void setResizable(boolean b) {
      this.resizable = b;
    }
    
    public boolean isResizable() {
      return resizable;
    }
  } //End of ModifiableJOptionPane private class

  private class SavePanel extends JPanel {
    //private static final long serialVersionUID = 1L;
    String saveFile = Simulation.ejsRes.getString("MoodleConnection.Save");
    JLabel nameLabel, annotationLabel;
    JPanel leftPanel, rightPanel;
    JTextField nameField, annotationField;

    SavePanel () {    
      nameLabel = new JLabel ("Name");
      nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
      nameLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
      nameField = new JTextField();
      nameField.setColumns(15);

      annotationLabel = new JLabel ("Annotation");
      annotationLabel.setHorizontalAlignment(SwingConstants.CENTER);
      annotationLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
      annotationField = new JTextField();
      annotationField.setColumns(15);

      leftPanel = new JPanel(new java.awt.GridLayout(0,1));
      leftPanel.add(nameLabel);
      //leftPanel.add(annotationLabel);

      rightPanel = new JPanel(new java.awt.GridLayout(0,1));
      rightPanel.add(nameField);
      //rightPanel.add(annotationField);

      setLayout(new BorderLayout());
      add(leftPanel,BorderLayout.WEST);
      add(rightPanel,BorderLayout.CENTER);
    }

    boolean showSaveOptions (String _name, String _ann) {
      if (_name!=null) nameField.setText(_name);
      if (_ann!=null) annotationField.setText(_ann);
      
      ModifiableJOptionPane myModJOptPane = new ModifiableJOptionPane(this,ModifiableJOptionPane.PLAIN_MESSAGE, ModifiableJOptionPane.OK_CANCEL_OPTION, moodleIcon);
      myModJOptPane.setResizable(true);
      
      JDialog myDialog = myModJOptPane.createDialog(getParentComponent(), saveFile);
      myDialog.setResizable(true);
      myDialog.setContentPane(myModJOptPane);
      myDialog.setVisible(true);
      
      Object choice = myModJOptPane.getValue();
      Object acceptOpt = 0;
      return (choice==acceptOpt);
    }

  } // End of SavePanel private class

  private class ReadPanel extends JPanel {
    //private static final long serialVersionUID = 1L;
    String filesLabel = Simulation.ejsRes.getString("MoodleConnection.Files");
    String loadFile = Simulation.ejsRes.getString("MoodleConnection.Load");
    JLabel filesJLabel;
    DefaultListModel listModel;
    JList list;

    ReadPanel () {
      filesJLabel = new JLabel (filesLabel);
      
      listModel = new DefaultListModel();
      list = new JList(listModel);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      JPanel topLabelPanel = new JPanel(new java.awt.GridLayout(0,1));
      topLabelPanel.add(filesJLabel);
      JScrollPane scrollPanel = new JScrollPane(list);
      
      setLayout(new BorderLayout());
      add(scrollPanel,BorderLayout.CENTER);
      add(topLabelPanel,BorderLayout.NORTH);
    }

    String chooseFile (JsonObject filesList) throws Exception {
      //Create a list with the files saved by the EJS lab. Only one is selected for loading.
      String selectedFile="";
      listModel.clear();
      
      ModifiableJOptionPane myModJOptPane = new ModifiableJOptionPane(this, ModifiableJOptionPane.PLAIN_MESSAGE, ModifiableJOptionPane.OK_CANCEL_OPTION, moodleIcon);
      myModJOptPane.setResizable(true);

      if (filesList!=null) {
        JsonArray pathsArray = filesList.getJsonArray("file_name");
        JsonArray namesArray = filesList.getJsonArray("file_path");
        String [] urlListArr = new String[namesArray.size()/2];
        for (int i = 0; i <= namesArray.size()/2; i++) {
          listModel.add(i,namesArray.getJsonObject(i).getString("file_name"));
          urlListArr[i] = pathsArray.getJsonObject(i).getString("file_path");
        }
        JDialog myDialog = myModJOptPane.createDialog(getParentComponent(), loadFile);
        myDialog.setResizable(true);
        myDialog.setContentPane(myModJOptPane);
        myDialog.setVisible(true);
        
        Object choice = myModJOptPane.getValue();
        Object acceptOpt = 0;
        int selectedIndex = list.getSelectedIndex();
        if(choice==acceptOpt && selectedIndex>=0) selectedFile = urlListArr[selectedIndex];
      } else {
        JDialog myDialog = myModJOptPane.createDialog(getParentComponent(), loadFile);
        myDialog.setResizable(true);
        myDialog.setContentPane(myModJOptPane);
        myDialog.setVisible(true);
      }
      
      return selectedFile;
    }

  } // End of ReadPanel private class

  private class LongTask {
    private int lengthOfTask;
    private int current = 0;
    private String statMessage;
    private String filename;
    private String annotation;
    private java.awt.Image image;
    private String txt;
    private byte[] data;
    private boolean isText;
    private String kindFragment;
    private boolean isOk = false;

    LongTask (String _filename, String _annotation, java.awt.Image _image) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "image";
        this.filename = _filename;
        this.annotation = _annotation;
        this.image = _image;
        lengthOfTask = 100;
    }

    LongTask (String _filename, String _annotation, String _txt, boolean _isText) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "text";
        this.filename = _filename;
        this.annotation = _annotation;
        this.txt = _txt;
        this.isText = _isText;
        lengthOfTask = 100;
    }

    LongTask (String _filename, String _annotation, byte[] _data) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "data";
        this.filename = _filename;
        this.annotation = _annotation;
        this.data = _data;
        lengthOfTask = 100;
    }
    
    void go() { //called from ProgressBarDemo to start the task
        current = 0;
        new SwingWorker() {
            public Object construct() {
              if (kindFragment.equals("image")){
                return new ActualTask(filename, annotation, image);
              }else if(kindFragment.equals("text")){
                return new ActualTask(filename, annotation, txt, isText);
              }else{
                return new ActualTask(filename, annotation, data);
              }
            }
        };
    }

    //called from ProgressBarDemo to find out how much work needs to be done
    //int getLengthOfTask() {
    //  return lengthOfTask;
    //}

    //called from ProgressBarDemo to find out how much has been done
    int getCurrent() {
        return current;
    }

    void stop() {
        current = lengthOfTask;
    }

    boolean getStatus(){
      return isOk;
    }

    //called from ProgressBarDemo to find out if the task has completed
    boolean done() {
        if (current >= lengthOfTask) return true;
        return false;
    }

    String getMessage() {
        return statMessage;
    }

    class ActualTask { //the actual long running task, this runs in a SwingWorker thread
      
        ActualTask (String _filename, String _annotation, java.awt.Image _image) {
            //fake a long task: make a random amount of progress every second
            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + 100 + "%.";
                }
                httpRequest (_filename, null, null, _image);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }
        }
        
        ActualTask (String _filename, String _annotation, String _txt, boolean _isText) {
            //fake a long task: make a random amount of progress every second
            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                }
                httpRequest (_filename, _txt, null, null);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }

        }
        
        ActualTask (String _filename, String _annotation, byte[] _data) {
            //fake a long task: make a random amount of progress every second
            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                }
                httpRequest (_filename, null, _data, null);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }

        }
        
     } // End of ActualTask
    
  } // End of LongTask

  class TimerListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
        if (task.done()) {
            if (task.getStatus()){
              progressMonitor.setProgress(100);
              progressMonitor.setNote("Completed " + "100" + "% out of 100%.");
              progressMonitor.setBarString("Successful Delivery. Refresh your EJSApp File Browser block");
            }else{
              progressMonitor.setBarString("Sending with problems. Try again!!");
              progressMonitor.setNote(task.getMessage());
            }
            task.stop();
            Toolkit.getDefaultToolkit().beep();
            timer.stop();
        } else {
            int actual = task.getCurrent();
            if ( actual == previous){
              progressMonitor.setBarIndeterminate(true);
              progressMonitor.setBarString("Sending file... Please wait");
            }else{
              progressMonitor.setBarIndeterminate(false);
              progressMonitor.setBarString("Progress");
            }
            previous = actual;
            progressMonitor.setNote(task.getMessage());
            progressMonitor.setProgress(actual);
        }
    }
  }

  class ProgressDialog extends JDialog {

    private JProgressBar progressBar;
    private JLabel msg;
    private JLabel note;

    public ProgressDialog(){
      super(JOptionPane.getFrameForComponent(getParentComponent()),"Progress");
      progressBar = new JProgressBar(0, 100);
      progressBar.setValue(0);
      progressBar.setStringPainted(true);

      msg = new JLabel("Monitoring the Delivery");
      note = new JLabel("Initializing progress...");

      JPanel panel = new JPanel(new GridLayout(3,0));
      panel.add(msg);
      panel.add(note);
      panel.add(progressBar);
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      setContentPane(panel);
      setSize(380,120);
      setLocation(300,300);
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          dispose();
          progressMonitor.dispose();
          progressMonitor = null;
        }
      });

    }

    public void setProgress(int value){
      progressBar.setValue(value);
    }

    public void setNote(String _note){
      note.setText(_note);
    }

    public void showProgressDialog(boolean _value){
      setVisible(_value);
    }

    public void setBarIndeterminate(boolean _value){
      progressBar.setIndeterminate(_value);
    }

    public void setBarString(String _value){
      progressBar.setString(_value);
    }

  } // End of ProgressDialog

} // End of class MoodleConecction
