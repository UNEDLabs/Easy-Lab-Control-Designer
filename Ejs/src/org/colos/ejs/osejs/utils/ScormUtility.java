package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;

public class ScormUtility {
  static public enum SCORM_VERSION { NONE, VERSION_1_2, VERSION_2004_4 }; 
  
  static public class ScormOptions {
    SCORM_VERSION mVersion = SCORM_VERSION.VERSION_2004_4;
    boolean mIncludeDescritionPages = true;    
  }

  // --------------------
  // private static variables
  //--------------------

  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final String   sSCRIPT_LOCATION = "_scorm/scripts/";
  static private final String[] sSCRIPTS = { "APIWrapper.js", "uja_scorm_rte.js" };

  static private final String sHEADER_1_2 = 
      "<manifest identifier = \"EJS_SCORM_1_2_package\" version = \"1.0\" \n" +
      "  xmlns = \"http://www.imsproject.org/xsd/imscp_rootv1p1p2\" \n" +
      "  xmlns:adlcp = \"http://www.adlnet.org/xsd/adlcp_rootv1p2\" \n" +
      "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
      "  xsi:schemaLocation = \"http://www.imsproject.org/xsd/imscp_rootv1p1p2 _scorm/imscp_rootv1p1p2.xsd \n" +
      "                        http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 _scorm/imsmd_rootv1p2p1.xsd \n" +
      "                        http://www.adlnet.org/xsd/adlcp_rootv1p2 _scorm/adlcp_rootv1p2.xsd\"  > \n" +
      "  <metadata>\n" +
      "    <schema>ADL SCORM</schema> \n" +
      "    <schemaversion>1.2</schemaversion> \n" +
      "  </metadata> \n" +
      "  <organizations default=\"ejs_default\"> \n" + 
      "    <organization identifier=\"ejs_default\" > \n";

  static private final String sOPTIONS_1_2 =  
      "        <adlcp:maxtimeallowed>00:30:00</adlcp:maxtimeallowed> \n" +
      "        <adlcp:timelimitaction>exit,no message</adlcp:timelimitaction> \n" +
      "        <adlcp:masteryscore>2</adlcp:masteryscore> \n";
                                
  static private final String sHEADER_2004_4 = 
    "<manifest identifier = \"EJS_SCORM_2004_4_package\" version = \"1.0\" \n" +
    "  xmlns = \"http://www.imsglobal.org/xsd/imscp_v1p1\" \n" +
    "  xmlns:adlcp = \"http://www.adlnet.org/xsd/adlcp_v1p3\" \n" +
    "  xmlns:adlseq = \"http://www.adlnet.org/xsd/adlseq_v1p3\" \n" +
    "  xmlns:adlnav = \"http://www.adlnet.org/xsd/adlnav_v1p3\" \n" +
    "  xmlns:imsss = \"http://www.imsglobal.org/xsd/imsss\" \n" +
    "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
    "  xsi:schemaLocation = \"http://www.imsglobal.org/xsd/imscp_v1p1 _scorm/imscp_v1p1.xsd \n" +
    "                        http://www.adlnet.org/xsd/adlcp_v1p3 _scorm/adlcp_v1p3.xsd \n" +
    "                        http://www.adlnet.org/xsd/adlseq_v1p3 _scorm/adlseq_v1p3.xsd \n" +
    "                        http://www.adlnet.org/xsd/adlnav_v1p3 _scorm/adlnav_v1p3.xsd \n" +
    "                        http://www.imsglobal.org/xsd/imsss _scorm/imsss_v1p0.xsd\"  > \n" +
    "  <metadata>\n" +
    "    <schema>ADL SCORM</schema> \n" +
    "    <schemaversion>2004 4th Edition</schemaversion> \n" +
    "  </metadata> \n" +
    "  <organizations default=\"ORG-AAE040C6-5D31-FF80-7A74-C93346D3DE42\"> \n" + 
    "    <organization identifier=\"ORG-AAE040C6-5D31-FF80-7A74-C93346D3DE42\" structure=\"hierarchical\"> \n";

  static private final String sOPTIONS_2004_4 =  
      "        <adlcp:completionThreshold completedByMeasure = \"true\" minProgressMeasure=\"0.75\" />\n" +
      "        <adlcp:dataFromLMS>SCO_Prueba-JS</adlcp:dataFromLMS>\n" +
      "        <adlcp:timeLimitAction>exit,no message</adlcp:timeLimitAction>\n" +
      "        <imsss:sequencing>\n" +
      "          <imsss:rollupRules rollupObjectiveSatisfied=\"true\" rollupProgressCompletion=\"true\" objectiveMeasureWeight=\"0\" />\n" +
      "          <imsss:deliveryControls objectiveSetByContent = \"true\"/>\n" +
      "          <imsss:limitConditions attemptLimit=\"2\" attemptAbsoluteDurationLimit=\"PT1H30M\"/>\n" + 
      "          <imsss:objectives>\n" +
      "            <imsss:primaryObjective objectiveID = \"PRIMARYOBJ\" satisfiedByMeasure = \"true\">\n" +
      "              <imsss:minNormalizedMeasure>0.6</imsss:minNormalizedMeasure>\n" +
      "              <imsss:mapInfo targetObjectiveID = \"obj_module_1\" readNormalizedMeasure = \"false\" writeSatisfiedStatus = \"true\" />\n" +
      "            </imsss:primaryObjective>\n" +
      "          </imsss:objectives>\n" +
      "        </imsss:sequencing>\n" +
      "        <adlcp:data>\n" +
      "          <adlcp:map targetID = \"test_data_store\" />\n" +
      "        </adlcp:data>\n";
  
  // --------------------
  // public methods
  //--------------------
  
  static public String getDestinationPath() { return "_scorm/"; }

  static public String getLibraryPath(ScormOptions options) {
    switch (options.mVersion) {
      default : 
      case VERSION_2004_4 : return "javascript/SCORM/2004_4";
      case VERSION_1_2    : return "javascript/SCORM/1.2";
    }
  }
  
  static public TwoStrings createScormManifest(File _zipFolder, String _name, ArrayList<TwoStrings> _metaFile, ScormOptions _options) {
    StringBuffer buffer = new StringBuffer();
    StringBuffer resourcesBuffer = new StringBuffer();
    
    SCORM_VERSION version = _options.mVersion;
    boolean includeDescriptionPages = _options.mIncludeDescritionPages;
    
    buffer.append("<?xml version = \"1.0\" standalone = \"no\"?>\n");
    switch(version) {
      case VERSION_1_2    : buffer.append(sHEADER_1_2);    break;
      default : 
      case VERSION_2004_4 : buffer.append(sHEADER_2004_4); break;   
    }
    buffer.append("      <title>"+_name+"</title>\n");
    
    // Process HTML pages 
    int counter = 0;
    String pagename ="Untitled";
    for (TwoStrings ts : _metaFile) {
      if (ts.getFirstString().equals("main-simulation")) {
        File file = new File(_zipFolder,ts.getSecondString());
        EPub.removeMetadataDiv(file, file, OsejsCommon.getUTF8());
        if (!includeDescriptionPages) {
          if (addScripts(_zipFolder,ts.getSecondString(), version)) { // Processing HTML file succeeded
            addHTMLfile (_name, ts.getSecondString(), 1, version, buffer, resourcesBuffer);
          } 
          break;
        }
      }
      else if (includeDescriptionPages) {
        if (ts.getFirstString().equals("page-title")) pagename = ts.getSecondString();
        else if (ts.getFirstString().equals("page-index")) {
          if (addScripts(_zipFolder,ts.getSecondString(), version)) { // Processing HTML file succeeded
            counter++;
            addHTMLfile (pagename, ts.getSecondString(), counter, version, buffer, resourcesBuffer);
          }
        } // for each page
      }
    } // for each manifest entry
    
    // finish the XML definition
    if (version==SCORM_VERSION.VERSION_2004_4)  {
      buffer.append("      <imsss:sequencing> \n");
      buffer.append("        <imsss:controlMode flow=\"true\" choice=\"true\"/> \n");
      buffer.append("        <imsss:rollupRules rollupObjectiveSatisfied=\"true\" rollupProgressCompletion=\"true\" objectiveMeasureWeight=\"0\"/> \n");
      buffer.append("      </imsss:sequencing> \n");
    }
    buffer.append("    </organization> \n");
    buffer.append("  </organizations> \n");
    buffer.append("  <resources> \n");
    buffer.append(resourcesBuffer);
//    for (int i=0; i<sSCRIPTS.length; i++) {
//      buffer.append("    <resource identifier=\"API-DEP1\" adlcp:scormType=\"asset\" xml:base=\""+sSCRIPT_LOCATION+"\" type=\"webcontent\" > \n");
//      buffer.append("      <file href=\""+ sSCRIPTS[i]+"\" /> \n");
//      buffer.append("    </resource> \n");
//    }
    buffer.append("  </resources> \n");
    buffer.append("</manifest> \n");

    return new TwoStrings("imsmanifest.xml",buffer.toString());
  }

  
  static private void addHTMLfile (String pagename, String filename, int counter, SCORM_VERSION version, StringBuffer buffer, StringBuffer resourcesBuffer) {
    buffer.append("      <item identifier=\"SCO"+counter+"\" identifierref=\"RES"+counter+"\" isvisible=\"true\"> \n");
    buffer.append("        <title>"+pagename+"</title>\n");
    if (counter==1) { // Commented options
      buffer.append("        <!-- Uncomment and/or modify the following only if you knwo what you are doing >\n");
      switch(version) {
        case VERSION_1_2    : buffer.append(sOPTIONS_1_2); break;
        default : 
        case VERSION_2004_4 : buffer.append(sOPTIONS_2004_4); break;   
      }
      buffer.append("        -->\n");
    } // end of commented options
    else { // second and later SCOs
      // if (version==SCORM_VERSION.VERSION_1_2) buffer.append("        <adlcp:prerequisites type=\"aicc_script\">SCO"+(counter-1)+"</adlcp:prerequisites> \n");
    }
    buffer.append("      </item> \n");
    // Add to resources buffer for later inclusion
    resourcesBuffer.append("    <resource identifier=\"RES"+counter+"\" href=\""+filename+"\" adlcp:scormType=\"sco\" type=\"webcontent\"> \n");
    resourcesBuffer.append("      <file href=\""+filename+"\" /> \n");
//    resourcesBuffer.append("      <dependency identifierref=\"API-DEP1\" /> \n");
//    resourcesBuffer.append("      <dependency identifierref=\"API-DEP2\" /> \n");
    resourcesBuffer.append("    </resource> \n"); 
  }

  static public boolean addScripts(File folder, String filename, SCORM_VERSION _version) {
    System.err.println("Adding SCORM information to HTML file : "+filename);
    File file = new File(folder,filename);
    String contents = FileUtils.readTextFile(file,null);
    if (contents==null) {
      System.err.println("Error: HTML file not found:" + filename);
      return false;
    }
    String lowercase = contents.toLowerCase();
    int index = lowercase.indexOf("</head>");
    if (index<0) index = lowercase.indexOf("</head ");
    if (index<0) {
      System.err.println("Error: HTML file without </head> tag:" + filename);
      return false;
    }
    String location = sSCRIPT_LOCATION;
    for (int i=0, l=filename.length(); i<l; i++) {
      char c = filename.charAt(i);
      if (c=='/'|| c=='\\') location = "../" + location;
    }
    StringBuffer buffer = new StringBuffer();
    buffer.append(contents.substring(0,index));
    buffer.append("\n");
    for (int i=0; i<sSCRIPTS.length; i++) {
      buffer.append("    <script src=\""+location+sSCRIPTS[i]+"\"></script>\n");
    }

//    buffer.append("    <script type=\"text/javascript\">\n");
//    switch(_version) {
//      default:
//      case VERSION_2004_4 : buffer.append("_scorm = new RTE(\"2004\");\n"); break;
//      case VERSION_1_2    : buffer.append("_scorm = new RTE(\"1.2\");\n");  break; 
//    }
//    buffer.append("_scorm = new RTE(\"1.2\");\n");
//    buffer.append("var _scorm_terminated = false;\n\n");
//
//    buffer.append("function _scorm_loadPage() {\n");
//    buffer.append("  var result = _scorm.initialize();\n");
//    buffer.append("  _scorm.setCompletionStatus(\"incomplete\");\n");
//    buffer.append("}\n");
//        
//    buffer.append("function _scorm_unloadPage() {\n");
//    buffer.append("  if (_scorm_terminated) return;\n");
//    buffer.append("  _scorm.setExit(\"\");\n");
//    buffer.append("  _scorm.setSessionTime();\n");
//    buffer.append("  _scorm.terminate();\n");
//    buffer.append("  _scorm_terminated = true;\n");
//    buffer.append("}\n");
//    buffer.append("    </script>\n\n  ");
      
    contents = contents.substring(index);
    lowercase = lowercase.substring(index);
    index = lowercase.indexOf("<body>");
    if (index<0) index = lowercase.indexOf("<body ");
    if (index<0) index = lowercase.indexOf("< body ");
    if (index<0) {
      System.err.println("Error: HTML file without <body> tag:" + filename);
      return false;
    }
    index = lowercase.indexOf('>', index) + 1; // end of body tag
    
    buffer.append(contents.substring(0,index)+"\n"); // index + "<body>" tag
    buffer.append("  <script>\n");
    switch(_version) {
      default:
      case VERSION_2004_4 : buffer.append("    window.onload = _scorm_loadPage('2004');\n");   break;
      case VERSION_1_2    : buffer.append("    window.onload = _scorm_loadPage(\"1.2\");\n");  break; 
    }
    buffer.append("    window.onbeforeunload = function(e) { _scorm_unloadPage(); }\n");
    buffer.append("  </script>\n");
    buffer.append(contents.substring(index));   // everything after the body tag

//    buffer.append(contents.substring(0,index+5)); // index + "<body".length()
//    buffer.append(" onLoad=\"_scorm_loadPage()\" onbeforeunload=\"_scorm_unloadPage()\"");
//    buffer.append(contents.substring(index+5));   // index + "<body".length()

    try{
      FileUtils.saveToFile (file, null, buffer.toString());
    }
    catch(Exception exc) {
      System.err.println("Error: Could not overwrite HTML file :" + filename);
      return false;
    }
    return true;
  }
  
  // --------------------------------
  // Dialog to choose Scorm options
  // --------------------------------
  
  static private class ReturnValue {
    boolean value = false;
  }

  static public ScormOptions chooseOptions (Osejs _ejs) {
    final JDialog dialog=new JDialog(_ejs.getMainFrame(),res.getString("Package.PackageScorm.Title"),true);
    final ReturnValue returnValue=new ReturnValue();

    Insets nullInset = new Insets(0,0,0,0);
    EmptyBorder labelBorder = new EmptyBorder(0,5,0,5);
    EmptyBorder panelBorder = new EmptyBorder(10,0,10,0);
   
   JButton okButton = new JButton (res.getString("EditorFor.Ok"));
   okButton.addActionListener (new ActionListener() {
     public void actionPerformed (java.awt.event.ActionEvent evt) {
       returnValue.value = true;
       dialog.setVisible (false);
     }
   });

   JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
   cancelButton.addActionListener (new ActionListener() {
     public void actionPerformed (java.awt.event.ActionEvent evt) {
       returnValue.value = false;
       dialog.setVisible (false);
     }
   });
   JPanel buttonsPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
   buttonsPanel.add (cancelButton);
   buttonsPanel.add (okButton);
   
   // -------------------------
   // Version
   // -------------------------
   
   JLabel versionLabel = new JLabel(res.getString("Package.PackageScorm.Version"));
   versionLabel.setBorder(labelBorder);
   versionLabel.setHorizontalAlignment(SwingConstants.LEFT);

   JRadioButton version12Button = new JRadioButton("1.2",true);
   version12Button.setRequestFocusEnabled(false);
   version12Button.setMargin(nullInset);

   JRadioButton version2004Button = new JRadioButton("2004 v4",false);
   version2004Button.setRequestFocusEnabled(false);
   version2004Button.setMargin(nullInset);

   ButtonGroup group = new ButtonGroup();
   group.add(version12Button);
   group.add(version2004Button);
   
   JPanel versionPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
   versionPanel.add (versionLabel);
   versionPanel.add (version12Button);
   versionPanel.add (version2004Button);

   // -------------------------
   // Include Description pages
   // -------------------------
   
   JCheckBox includeDescriptionCB = new JCheckBox(res.getString("Package.PackageScorm.IncludeDescription"));
   includeDescriptionCB.setSelected(true);
   
   JPanel includeDescriptionPanel=new JPanel (new BorderLayout());
   includeDescriptionPanel.add(includeDescriptionCB,BorderLayout.WEST);

   // -------------------------
   // Options panel
   // -------------------------
   
   Box optionsPanel = Box.createVerticalBox();
   optionsPanel.setBorder(panelBorder);

   optionsPanel.add (versionPanel);
   optionsPanel.add (includeDescriptionPanel);

   // -------------------------
   // All together
   // -------------------------
  
   JPanel centerPanel = new JPanel (new BorderLayout());
   centerPanel.add(optionsPanel,BorderLayout.NORTH);

   JPanel bottomPanel = new JPanel (new BorderLayout());
   bottomPanel.add(new JSeparator(),BorderLayout.NORTH);
   bottomPanel.add(buttonsPanel,BorderLayout.CENTER);

   dialog.getContentPane().setLayout(new BorderLayout());
   dialog.getContentPane().add(centerPanel,BorderLayout.CENTER);
   dialog.getContentPane().add(bottomPanel,BorderLayout.SOUTH);

   dialog.setResizable(true);
   dialog.validate();
   dialog.pack();
   dialog.setLocationRelativeTo(_ejs.getMainPanel());

   returnValue.value = false;
   dialog.setVisible(true);
   if (!returnValue.value) return null;
   
   ScormOptions options = new ScormOptions();
   if (version12Button.isSelected()) options.mVersion = SCORM_VERSION.VERSION_1_2;
   else options.mVersion = SCORM_VERSION.VERSION_2004_4;
   options.mIncludeDescritionPages = includeDescriptionCB.isSelected();

   return options;
   
  }



}
