package org.colos.ejs.model_elements.external;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.colos.ejs.model_elements.AbstractModelElement;
import org.colos.ejs.model_elements.ModelElementSearch;
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.library.utils.ModelElementsUtilities;

public class TrackerElement extends AbstractModelElement {
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/external/Tracker.png"); // This icon is included in this jar
  static private ImageIcon LINK_ICON = AbstractModelElement.createImageIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static private ImageIcon FILE_ICON = AbstractModelElement.createImageIcon("data/icons/openSmall.gif"); // This icon is bundled with EJS
  
  static private final String NAME_KEYWORD = "Name";
  static private final String VIDEO_KEYWORD = "Clip";
  static private final String TRK_KEYWORD  = "TRK";
  static private final String START_FRAME_KEYWORD   = "StartFrame";
  static private final String END_FRAME_KEYWORD     = "EndFrame";
  static private final String STEP_SIZE_KEYWORD     = "StepSize";
  static private final String USE_DATA_TIME_KEYWORD = "UseDataTime";
  static private final String TIME_KEYWORD = "Time";
  static private final String X_KEYWORD = "XInput";
  static private final String Y_KEYWORD = "YInput";

  private JTextField mNameField = new JTextField();  // needs to be created to avoid null references
  private JTextField mVideoField = new JTextField();  // needs to be created to avoid null references
  private JTextField mTrkField = new JTextField();  // needs to be created to avoid null references
  private JTextField mStartFrameField = new JTextField();  // needs to be created to avoid null references
  private JTextField mEndFrameField = new JTextField();  // needs to be created to avoid null references
  private JTextField mStepSizeField = new JTextField();  // needs to be created to avoid null references
  private JTextField mUseDataTimeField = new JTextField();  // needs to be created to avoid null references
  private JTextField mTimeField = new JTextField();  // needs to be created to avoid null references
  private JTextField mXField = new JTextField();  // needs to be created to avoid null references
  private JTextField mYField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Tracker"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.external.TrackerConnection"; }

  public String getInitializationCode(String name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("if ("+name + "!=null) "+name+".clear();\n");
    buffer.append("else "+ name + " = new " + getConstructorName()+"(this) {\n"); 
//      ModelElementsUtilities.getQuotedValue(mNameField)+","+ 
//      ModelElementsUtilities.getQuotedValue(mVideoField)+","+ 
//      ModelElementsUtilities.getQuotedValue(mTrkField)+","+ 
//      ModelElementsUtilities.getQuotedValue(mStartFrameField)+","+
//      ModelElementsUtilities.getQuotedValue(mEndFrameField)+","+ 
//      ModelElementsUtilities.getQuotedValue(mStepSizeField)+","+ 
//      ModelElementsUtilities.getQuotedValue(mUseDataTimeField)+");");
//    buffer.append(") {\n"); 
      buffer.append("  protected String getDataName() {\n"); 
      { String fieldStr = getString(mNameField);
      if (fieldStr!=null) buffer.append("    return "+fieldStr+";\n");
      else buffer.append("    return \"Unnamed\";\n");
      }
      buffer.append("  } // end of getDataName\n"); 
    buffer.append("  protected void configMessage(java.util.TreeMap<String, Object> _message) {\n"); 
    { String fieldStr = getString(mVideoField);
      if (fieldStr!=null) buffer.append("    addResourceToMessage(_message, \"video\","+fieldStr+");\n");
    }
    { String fieldStr = getString(mTrkField);
      if (fieldStr!=null) buffer.append("    addResourceToMessage(_message, \"trk\","+fieldStr+");\n");
    }
    { String fieldStr = getString(mStartFrameField);
      if (fieldStr!=null) buffer.append("    _message.put(\"videoStartFrame\","+fieldStr+");\n");
    }
    { String fieldStr = getString(mEndFrameField);
      if (fieldStr!=null) buffer.append("    _message.put(\"videoEndFrame\","+fieldStr+");\n");
    }
    { String fieldStr = getString(mStepSizeField);
      if (fieldStr!=null) buffer.append("    _message.put(\"videoStepSize\","+fieldStr+");\n");
    }
    { String fieldStr = getString(mUseDataTimeField);
      if (fieldStr!=null) buffer.append("    _message.put(\"useDataTime\","+fieldStr+");\n");
    }
    buffer.append("  } // end of config\n"); 
    buffer.append("}; // end of TrackerCOnnection modified constructor\n"); 
//      buffer.append(_name + " = new " + getConstructorName()+"(this," + dataName+","+ clip+","+ trk+");");
    return buffer.toString();
  }

  static public String getString(JTextField field) {
    String value = field.getText().trim();
    if (value.length()<=0) return null;
    return ModelElementsUtilities.removeEnclosingString(value,"%");
  }



  public String getResourcesRequired() {
    String allFiles=null;
    String clip = mVideoField.getText().trim();
    if (!ModelElementsUtilities.isLinkedToVariable(clip)) allFiles = ModelElementsUtilities.getPureValue(clip);
    String trk = mTrkField.getText().trim();
    if (!ModelElementsUtilities.isLinkedToVariable(trk)) {
      if (allFiles==null) allFiles = ModelElementsUtilities.getPureValue(trk);
      else allFiles += ";" + ModelElementsUtilities.getPureValue(trk);
    }
    return allFiles;
  }
  
  // Code to read from view the position of the ParticleDataTrack after the view has been updated
  public String getReadFromView(String name) { 
    String timeStr = mTimeField.getText().trim();
    if (timeStr.length()<=0) return null;
    String xStr = mXField.getText().trim();
    if (xStr.length()<=0) return null;
    String yStr = mYField.getText().trim();
    if (yStr.length()<=0) return null;
    StringBuffer buffer = new StringBuffer();
    buffer.append(name + ".append("+timeStr+","+xStr+","+yStr+"); // Generated by Tracker model element "+name);
    return buffer.toString();  
  } 

  public String getDisplayInfo() {
    String value = mNameField.getText().trim();
    if (value.length()<=0) return "(no data)";
    return "("+value+")";
  }

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(AbstractModelElement.writeField(NAME_KEYWORD,  mNameField)+"\n");
    buffer.append(AbstractModelElement.writeField(VIDEO_KEYWORD, mVideoField)+"\n");
    buffer.append(AbstractModelElement.writeField(TRK_KEYWORD,   mTrkField)+"\n");
    buffer.append(AbstractModelElement.writeField(START_FRAME_KEYWORD,   mStartFrameField)+"\n");
    buffer.append(AbstractModelElement.writeField(END_FRAME_KEYWORD,     mEndFrameField)+"\n");
    buffer.append(AbstractModelElement.writeField(STEP_SIZE_KEYWORD,     mStepSizeField)+"\n");
    buffer.append(AbstractModelElement.writeField(USE_DATA_TIME_KEYWORD, mUseDataTimeField)+"\n");
    buffer.append(AbstractModelElement.writeField(TIME_KEYWORD, mTimeField)+"\n");
    buffer.append(AbstractModelElement.writeField(X_KEYWORD,    mXField)+"\n");
    buffer.append(AbstractModelElement.writeField(Y_KEYWORD,    mYField));
    return buffer.toString();
  }

  public void readfromXML(String inputXML) {
    AbstractModelElement.readInput(inputXML, NAME_KEYWORD,  mNameField);
    AbstractModelElement.readInput(inputXML, VIDEO_KEYWORD, mVideoField);
    AbstractModelElement.readInput(inputXML, TRK_KEYWORD,   mTrkField);
    AbstractModelElement.readInput(inputXML, START_FRAME_KEYWORD,   mStartFrameField);
    AbstractModelElement.readInput(inputXML, END_FRAME_KEYWORD,     mEndFrameField);
    AbstractModelElement.readInput(inputXML, STEP_SIZE_KEYWORD,     mStepSizeField);
    AbstractModelElement.readInput(inputXML, USE_DATA_TIME_KEYWORD, mUseDataTimeField);
    AbstractModelElement.readInput(inputXML, TIME_KEYWORD, mTimeField);
    AbstractModelElement.readInput(inputXML, X_KEYWORD,    mXField);
    AbstractModelElement.readInput(inputXML, Y_KEYWORD,    mYField);
  }
  
  // -------------------------------
  // Help and edition
  // -------------------------------

//  protected void addListeners(JTextField field, final ModelElementsCollection collection) {
//    field.getDocument().addDocumentListener (new DocumentListener() {
//      public void changedUpdate(DocumentEvent e) { collection.reportChange(DataReaderElement.this); }
//      public void insertUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
//      public void removeUpdate(DocumentEvent e)  { collection.reportChange(DataReaderElement.this); }
//    });
//  }

  public String getTooltip() {
    return "allows passing video and data from your model to Tracker video analysis tool";
  }
  
  public void setFont(Font font) { 
    mNameField.setFont(font); 
    mVideoField.setFont(font); 
    mTrkField.setFont(font); 
    mStartFrameField.setFont(font); 
    mEndFrameField.setFont(font); 
    mStepSizeField.setFont(font); 
    mUseDataTimeField.setFont(font); 
    mTimeField.setFont(font); 
    mXField.setFont(font); 
    mYField.setFont(font); 
  }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/external/Tracker.html"; }

  private JPanel createButtonsPanel(JButton fileButton, final ModelElementsCollection collection, final JTextField field, final String type) {
    JButton linkButton = new JButton(LINK_ICON);
    linkButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = field.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(field, type, value);
        if (variable!=null) {
          if (type.indexOf("String")>=0) field.setText("%"+variable+"%");
          else field.setText(variable);
        }
      }
    });
    JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
    if (fileButton!=null) buttonsPanel.add(fileButton);
    buttonsPanel.add(linkButton);
    return buttonsPanel;
  }

    
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JLabel nameLabel         = new JLabel(" Name:",SwingConstants.RIGHT);
    nameLabel.setToolTipText("A String. Use inverted commas if you enter a constant string");

    JLabel clipLabel         = new JLabel(" Video File:",SwingConstants.RIGHT);
    JLabel trkLabel          = new JLabel(" TRK File:",SwingConstants.RIGHT);
    JLabel startFrameLabel   = new JLabel(" Start Frame:",SwingConstants.RIGHT);
    JLabel endFrameLabel     = new JLabel(" End Frame:",SwingConstants.RIGHT);
    JLabel stepSizeLabel     = new JLabel(" Step Size:",SwingConstants.RIGHT);
    JLabel useDataTimeLabel  = new JLabel(" Use Data Time:",SwingConstants.RIGHT);
    JLabel timeLabel         = new JLabel(" t:",SwingConstants.RIGHT);
    JLabel xLabel            = new JLabel(" x:",SwingConstants.RIGHT);
    JLabel yLabel            = new JLabel(" y:",SwingConstants.RIGHT);

    java.util.Set<JComponent> firstSet = new java.util.HashSet<JComponent>();
    firstSet.add(nameLabel);
    firstSet.add(clipLabel);
    firstSet.add(trkLabel);
    firstSet.add(startFrameLabel);
    firstSet.add(endFrameLabel);

    java.util.Set<JComponent> secondSet = new java.util.HashSet<JComponent>();
    secondSet.add(stepSizeLabel);
    secondSet.add(useDataTimeLabel);
    
    org.colos.ejs.osejs.utils.InterfaceUtils.makeSameDimension(firstSet);
    org.colos.ejs.osejs.utils.InterfaceUtils.makeSameDimension(secondSet);

    DocumentListener listener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) { collection.reportChange(TrackerElement.this); }
      public void insertUpdate(DocumentEvent e)  { collection.reportChange(TrackerElement.this); }
      public void removeUpdate(DocumentEvent e)  { collection.reportChange(TrackerElement.this); }
    };
    mNameField.getDocument().addDocumentListener (listener);
    mNameField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String fieldStr = mNameField.getText().trim();
        if (fieldStr.charAt(0)!='%' && fieldStr.charAt(0)!='"') {
          //System.err.println ("Correcting name to "+"\""+fieldStr+"\"");
          mNameField.setText("\""+fieldStr+"\"");
        }
      }
    });
    mVideoField.getDocument().addDocumentListener (listener);
    mTrkField.getDocument().addDocumentListener (listener);
    mStartFrameField.getDocument().addDocumentListener (listener);
    mEndFrameField.getDocument().addDocumentListener (listener);
    mStepSizeField.getDocument().addDocumentListener (listener);
    mUseDataTimeField.getDocument().addDocumentListener (listener);
    mTimeField.getDocument().addDocumentListener (listener);
    mXField.getDocument().addDocumentListener (listener);
    mYField.getDocument().addDocumentListener (listener);

/*
    new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mNameField.getText().trim();
        if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
        else value = ModelElementsUtilities.getPureValue(value);
        String variable = collection.chooseVariable(mNameField,"String", value);
        if (variable!=null) mNameField.setText("%"+variable+"%");
//        String variable = collection.chooseViewElement(mNameField, org.opensourcephysics.drawing2d.Element.class, value);
//        if (variable!=null) {
//          mNameField.setText(variable);
//          Object viewElement = collection.getEJS().getViewElement(variable);
//          if (!org.opensourcephysics.drawing2d.Element.class.isInstance(viewElement)) {
//            JOptionPane.showMessageDialog(mNameField, "This element is not a valid (x,y) position provider", "Tracker element error", JOptionPane.ERROR_MESSAGE);
//          }
//        }
      }
    });
    */
    
    JButton clipFileButton = new JButton(FILE_ICON);
    clipFileButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String filename = collection.chooseFilename(mVideoField, getGenericName()+" video clip", 
            res.getString("View.FileDescription.videofile"), sysRes.getString("View.FileExtension.videofile"));
        if (filename!=null) mVideoField.setText("\""+filename+"\"");
      }
    });
    
    JButton trkFileButton = new JButton(FILE_ICON);
    trkFileButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String filename = collection.chooseFilename(mTrkField, getGenericName()+" trk file", "TRK file", "trk");
        if (filename!=null) mTrkField.setText("\""+filename+"\"");
      }
    });

    JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.add(nameLabel, BorderLayout.WEST);
    namePanel.add(mNameField, BorderLayout.CENTER);
    namePanel.add(createButtonsPanel(null,collection, mNameField, "String"), BorderLayout.EAST);

    JPanel clipPanel = new JPanel(new BorderLayout());
    clipPanel.add(clipLabel, BorderLayout.WEST);
    clipPanel.add(mVideoField, BorderLayout.CENTER);
    clipPanel.add(createButtonsPanel(clipFileButton,collection, mVideoField, "String"), BorderLayout.EAST);

    JPanel trkPanel = new JPanel(new BorderLayout());
    trkPanel.add(trkLabel, BorderLayout.WEST);
    trkPanel.add(mTrkField, BorderLayout.CENTER);
    trkPanel.add(createButtonsPanel(trkFileButton,collection, mTrkField, "String"), BorderLayout.EAST);

    JPanel startFramePanel = new JPanel(new BorderLayout());
    startFramePanel.add(startFrameLabel, BorderLayout.WEST);
    startFramePanel.add(mStartFrameField, BorderLayout.CENTER);
    startFramePanel.add(createButtonsPanel(null,collection, mStartFrameField, "int"), BorderLayout.EAST);

    JPanel endFramePanel = new JPanel(new BorderLayout());
    endFramePanel.add(endFrameLabel, BorderLayout.WEST);
    endFramePanel.add(mEndFrameField, BorderLayout.CENTER);
    endFramePanel.add(createButtonsPanel(null,collection, mEndFrameField, "int"), BorderLayout.EAST);

    JPanel stepSizePanel = new JPanel(new BorderLayout());
    stepSizePanel.add(stepSizeLabel, BorderLayout.WEST);
    stepSizePanel.add(mStepSizeField, BorderLayout.CENTER);
    stepSizePanel.add(createButtonsPanel(null,collection, mStepSizeField, "int"), BorderLayout.EAST);

    JPanel useDataTimePanel = new JPanel(new BorderLayout());
    useDataTimePanel.add(useDataTimeLabel, BorderLayout.WEST);
    useDataTimePanel.add(mUseDataTimeField, BorderLayout.CENTER);
    useDataTimePanel.add(createButtonsPanel(null,collection, mUseDataTimeField, "boolean"), BorderLayout.EAST);

    JPanel timePanel = new JPanel(new BorderLayout());
    timePanel.add(timeLabel, BorderLayout.WEST);
    timePanel.add(mTimeField, BorderLayout.CENTER);
    timePanel.add(createButtonsPanel(null,collection, mTimeField, "double|double[]"), BorderLayout.EAST);

    JPanel xPanel = new JPanel(new BorderLayout());
    xPanel.add(xLabel, BorderLayout.WEST);
    xPanel.add(mXField, BorderLayout.CENTER);
    xPanel.add(createButtonsPanel(null,collection, mXField, "double|double[]"), BorderLayout.EAST);

    JPanel yPanel = new JPanel(new BorderLayout());
    yPanel.add(yLabel, BorderLayout.WEST);
    yPanel.add(mYField, BorderLayout.CENTER);
    yPanel.add(createButtonsPanel(null,collection, mYField, "double|double[]"), BorderLayout.EAST);

    
    JPanel row1Panel = new JPanel(new GridLayout(1,0));
    row1Panel.add(startFramePanel);
    row1Panel.add(stepSizePanel);
    
    JPanel row2Panel = new JPanel(new GridLayout(1,0));
    row2Panel.add(endFramePanel);
    row2Panel.add(useDataTimePanel);

    JPanel row3Panel = new JPanel(new GridLayout(1,0));
    row3Panel.add(timePanel);
    row3Panel.add(xPanel);
    row3Panel.add(yPanel);

    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(namePanel);
    topPanel.add(clipPanel);
    topPanel.add(trkPanel);
    topPanel.add(row1Panel);
    topPanel.add(row2Panel);
    topPanel.add(row3Panel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(createHelpComponent(),BorderLayout.CENTER);
    mainPanel.setPreferredSize(new Dimension(500,400));
    return mainPanel;
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    addToSearch(list,mNameField,       info,searchString,mode,this,name,collection);
    addToSearch(list,mVideoField,      info,searchString,mode,this,name,collection);
    addToSearch(list,mTrkField,        info,searchString,mode,this,name,collection);
    addToSearch(list,mStartFrameField, info,searchString,mode,this,name,collection);
    addToSearch(list,mEndFrameField,   info,searchString,mode,this,name,collection);
    addToSearch(list,mStepSizeField,   info,searchString,mode,this,name,collection);
    addToSearch(list,mUseDataTimeField,info,searchString,mode,this,name,collection);
    addToSearch(list,mTimeField,       info,searchString,mode,this,name,collection);
    addToSearch(list,mXField,          info,searchString,mode,this,name,collection);
    addToSearch(list,mYField,          info,searchString,mode,this,name,collection);
    return list;
  }
  
}
