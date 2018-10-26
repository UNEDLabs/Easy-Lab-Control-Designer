/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified: March 2006
 */

package org.colos.ejs.osejs.edition.html_view;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.library.utils.LocaleListener;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejss.xml.Emulator;
import org.colos.ejss.xml.EmulatorJFX;
import org.colos.ejss.xml.EmulatorWebSocket;
import org.colos.ejss.xml.SimulationXML;
import org.colos.ejss.xml.SimulationXML.INFORMATION;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.*;

import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Element;

public class OneView implements Editor, LocaleListener {
  static private ResourceUtil res = new ResourceUtil ("Resources");
//  static private Icon sOnIcon  = ResourceLoader.getIcon("/org/opensourcephysics/resources/controls/images/power_on.png");
//  static private Icon sOffIcon = ResourceLoader.getIcon("/org/opensourcephysics/resources/controls/images/power_off.png");

//  private boolean mVisible=true;
  private String mName="HtmlView";
  private JComponent mMainPanel;
  private JTextField mWidthField, mHeightField;
  private JCheckBox mHiddenButton;
  private JComboBox<SizeOption> mSizeCombo;
  private ElementsTree mTree;
  private Emulator mEmulator;
  private org.colos.ejs.library.utils.LocaleSelector mLocaleSelector;
  private LocaleItem mCurrentLocale = LocaleItem.getDefaultItem();
  private Osejs mEjs;
  private int mX=0, mY=0;
  
  public OneView (org.colos.ejs.osejs.Osejs _ejs, Palette palette) {
    mEjs = _ejs;
    mTree = new ElementsTree(this,palette);

    JLabel widthLabel = new JLabel(res.getString("EditorForPoint.Width"));
    mWidthField = new JTextField("800");
    mWidthField.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) { 
        setChanged(true); 
        resizeEmulator(); 
      }
    });
    JPanel widthPanel = new JPanel(new BorderLayout());
    widthPanel.add(widthLabel,BorderLayout.WEST);
    widthPanel.add(mWidthField,BorderLayout.CENTER);
    
    JLabel heightLabel = new JLabel(res.getString("EditorForPoint.Height"));
    mHeightField = new JTextField("600");
    mHeightField.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) { 
        setChanged(true); 
        resizeEmulator(); 
      }
    });
    JPanel heightPanel = new JPanel(new BorderLayout());
    heightPanel.add(heightLabel,BorderLayout.WEST);
    heightPanel.add(mHeightField,BorderLayout.CENTER);
    
    mSizeCombo = new JComboBox<SizeOption>();
    mSizeCombo.addItem(new PredefinedSizeOption("Custom",800,600));
    mSizeCombo.addItem(new PredefinedSizeOption("iPadHorizontal",1024,768));
    mSizeCombo.addItem(new PredefinedSizeOption("iPadVertical",768,1024));
    mSizeCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange()==ItemEvent.DESELECTED) return;
        SizeOption option = (SizeOption) e.getItem();
        //System.out.println("Setting size to "+option.getWidth()+","+option.getHeight());
        mWidthField.setText(""+option.getWidth());
        mHeightField.setText(""+option.getHeight());
        if (mEmulator!=null) resizeEmulator();
        boolean isCustom = (mSizeCombo.getSelectedIndex()==0);
        mWidthField.setEditable(isCustom);
        mHeightField.setEditable(isCustom);
        setChanged(true);
      }
    });
    mSizeCombo.setSelectedIndex(0);
    
    JPanel sizePanel = new JPanel(new GridLayout());
    sizePanel.add(widthPanel);
    sizePanel.add(heightPanel);

    final JPanel sizeOptionPanel = new JPanel(new BorderLayout());
    sizeOptionPanel.add(sizePanel,BorderLayout.CENTER);
    sizeOptionPanel.add(mSizeCombo,BorderLayout.WEST);

    // Locale selector
    mLocaleSelector = new org.colos.ejs.library.utils.LocaleSelector (this);
    mLocaleSelector.setMasterSelector(mEjs.getTranslationEditor().getLocaleSelector());
    mLocaleSelector.setEnabled(mEjs.getSimInfoEditor().addTranslatorTool());
    
    mHiddenButton = new JCheckBox(res.getString("Tree.KeepPreviewHidden"),false);
    mHiddenButton.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        if (mEmulator!=null) {
          boolean show = e.getStateChange()!=ItemEvent.SELECTED;
          mEmulator.setVisible(show);
          if (show) refreshEmulator();
        }
        setChanged(true);
       }
    });

//    mHiddenButton = new JCheckBox(sOffIcon, true);
//    mHiddenButton.setText(res.getString("HtmlView.ShowPreview"));
//    mHiddenButton.addItemListener(new ItemListener(){
//      public void itemStateChanged(ItemEvent e) {
//        setChanged(true);
//        if (mEmulator==null) return;
//        boolean show = e.getStateChange()==ItemEvent.SELECTED;
//        mEmulator.getWindow().setVisible(show);
//        mHiddenButton.setIcon(show ? sOffIcon : sOnIcon);
//       }
//    });

    final javax.swing.border.Border border = BorderFactory.createEmptyBorder(1,1,1,1);// createEtchedBorder(javax.swing.border.EtchedBorder.RAISED); 
    final javax.swing.border.Border clickedBorder = BorderFactory.createLineBorder(new Color (128,64,255),1);
    JLabel refreshButton = new JLabel(ResourceLoader.getIcon("/org/opensourcephysics/resources/controls/images/reset2.gif"));
    refreshButton.setBorder(border);
//    Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//    refreshButton.setCursor(handCursor);
    refreshButton.setText(" ");
    refreshButton.setToolTipText(res.getString("HTMLEditor.Refresh"));
//    refreshButton.addMouseActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        // mShowPreviewButton.setSelected(true);
//        refreshEmulator();
//      }
//    });
    refreshButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(final MouseEvent _evt) {
        ((JComponent) _evt.getComponent()).setBorder(clickedBorder);
        javax.swing.Timer timer = new javax.swing.Timer(Osejs.DELAY_FOR_LABEL_BORDERS,new ActionListener(){
          public void actionPerformed(ActionEvent _actionEvent) { ((JComponent) _evt.getComponent()).setBorder(border); }
        });
        timer.setRepeats(false);
        timer.start();
        if (SwingUtilities.isLeftMouseButton(_evt)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() { 
              refreshEmulator();
            }
          });
        }
      }
    });
    
    
    final JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(refreshButton,BorderLayout.WEST);
    rightPanel.add(mLocaleSelector.getComponent(),BorderLayout.CENTER);
    
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add (mHiddenButton,BorderLayout.WEST);
    if (mEjs.getOptions().useBrowserForPreview()) {
      bottomPanel.add (rightPanel,BorderLayout.EAST);
      bottomPanel.add (sizeOptionPanel,BorderLayout.SOUTH);
    }

    mMainPanel = new JPanel(new BorderLayout());
    mMainPanel.add (bottomPanel,BorderLayout.SOUTH);
    mMainPanel.add (mTree.getComponent(),BorderLayout.CENTER);

    if (mEjs.getOptions().useBrowserForPreview()) {
      mEmulator = new EmulatorWebSocket (mEjs, OneView.this, 100, 100);
      resizeEmulator();
      mEjs.getHtmlViewEditor().checkForShowingFirstPage();
      refreshEmulator();
    }
    else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { 
          JPanel accesoryPanel = new JPanel(new BorderLayout());
          accesoryPanel.add (sizeOptionPanel,BorderLayout.CENTER);
          accesoryPanel.add (rightPanel,BorderLayout.EAST);
          mEmulator = new EmulatorJFX (mEjs, OneView.this, 100, 100, accesoryPanel);
          resizeEmulator();
          Window emulatorWindow = mEmulator.getWindow(); 
          emulatorWindow.setVisible(!mHiddenButton.isSelected());
          emulatorWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              mHiddenButton.setSelected(true);
            }
          });
          int defaultScreen = OsejsCommon.getScreenNumber(mEjs.getMainFrame());
          Rectangle bounds = OsejsCommon.getScreenBounds(defaultScreen);
          int emulatorScreen = OsejsCommon.getScreenNumber(emulatorWindow);
          // Make sure the emulator screen is not larger than the target screen
          Dimension emulatorSize = emulatorWindow.getSize();
          // make sure the emulator window is in the same screen
          if (emulatorScreen!=defaultScreen) {
            mX = bounds.x;
            mY = bounds.y;
          }
          else { // if in the same screen, make sure it is visible
            if (mY<bounds.y) mY = bounds.y;
            else if ((mY+100)>(bounds.y+bounds.height)) mY = bounds.y+bounds.height-emulatorSize.height;
            if (mX<bounds.x) mX = bounds.x;
            else if ((mX+100)>(bounds.x+bounds.width)) mX = bounds.x+bounds.width-emulatorSize.width;
          }
          emulatorWindow.setLocation(mX, mY);
          //            System.err.println ("Emulator position (x,y) = "+mX+","+mY);
          mEjs.addDependentWindows(emulatorWindow);
          mEjs.getHtmlViewEditor().checkForShowingFirstPage();
          mEmulator.adjustBorder();
          refreshEmulator();
        }
      });
    }
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(mMainPanel, level);
  }
  
  public void setViewHidden () {
    mHiddenButton.setSelected(true);
  }
  
  public String getPreferredWidth() {
    return mWidthField.getText().trim();
  }

  public String getPreferredHeight() {
    return mHeightField.getText().trim();
  }
  
  private void resizeEmulator() {
    if (mEmulator==null) return;
//    System.out.println("Resizing emulator");
    try {
      int width = Integer.parseInt(mWidthField.getText());
      int height = Integer.parseInt(mHeightField.getText());
      mEmulator.setSize(width,height);
    }
    catch (Exception exc) {
    JOptionPane.showMessageDialog(mMainPanel,res.getString("HtmlView.InvalidSize")+": ("+mWidthField.getText()+","+mHeightField.getText()+")",
        res.getString("HtmlView.Error"),JOptionPane.ERROR_MESSAGE);
    }
  }
  
  public void setLocaleItem(LocaleItem _item) {
    mCurrentLocale = _item;
    refreshEmulator();
  }
  
  public LocaleItem getLocale() { return mCurrentLocale; }
  
  Osejs getEjs() { return mEjs; }
  
  Emulator getEmulator() { return mEmulator; }
  
  public ElementsTree getTree() { return mTree; }
  
  public void showWindows (boolean show) {
    if (mEmulator!=null) mEmulator.setVisible(show && !mHiddenButton.isSelected());
  }
  
  // ---------------------- Implementation of Editor

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    return mTree.search(_info, _searchString, _mode);
  }

  public void setName(String _name) { mName = _name;  }
  
  public String getName() { return mName; }

  public void clear () {
    mTree.clear ();
    mLocaleSelector.setMasterSelector(null);
    if (mEmulator!=null) mEmulator.clear();
  }

  public Component getComponent () { return mMainPanel; }

  public void setColor (Color _color) { mTree.setColor (_color); }

  public void setFont (Font _font) { mTree.setFont (_font); }

  public void setEditable (boolean _editable) { mTree.setEditable(_editable); }

  public void refresh (boolean _hiddensToo) { }

//  public void setVisible (boolean _visible) { mVisible = _visible; }
//
//  public boolean isVisible () { return mVisible; }

  public boolean isChanged () { return mTree.isChanged(); }

  public void setChanged (boolean _ch) { mTree.setChanged(_ch); }

  public void setActive (boolean _active) { }

  public boolean isActive () { return true; }

  public boolean isInternal() {
    return false;
  }

  public void setInternal(boolean _advanced) {  }
  
  public StringBuffer generateCode (int _type, String _info) { 
    StringBuffer buffer = new StringBuffer();
    mTree.generateCode(buffer,_type);
    return buffer;
  }

  public void fillSimulationXML(SimulationXML _simulation) { // Do nothing, for the moment
    if (!isActive()) return;
    Element view = _simulation.addView(mName, mWidthField.getText(), mHeightField.getText());
    mTree.fillSimulationXML(_simulation,view);
  }
  
  public StringBuffer saveStringBuffer () {
    if (mEmulator!=null && mEmulator.getWindow()!=null) {
      mX = mEmulator.getWindow().getX();
      mY = mEmulator.getWindow().getY();
    }
    StringBuffer save = new StringBuffer();
    save.append("<SizeOption>"+mSizeCombo.getSelectedIndex()+"</SizeOption>\n");
    save.append("<X>"+mX+"</X>\n");
    save.append("<Y>"+mY+"</Y>\n");
    save.append("<Width>"+mWidthField.getText().trim()+"</Width>\n");
    save.append("<Height>"+mHeightField.getText().trim()+"</Height>\n");
    save.append("<KeepHidden>"+(mHiddenButton.isSelected())+"</KeepHidden>\n");
    save.append("<RootProperties>\n");
    save.append(mTree.saveRootProperties());
    save.append("</RootProperties>\n");
    save.append("<Tree>\n"+mTree.saveStringBuffer("HtmlView")+"</Tree>\n");
    return save;
  }

  public void readString (String _input) {
    boolean readSize=true;
    try {
      int option = Integer.parseInt(OsejsCommon.getPiece(_input,"<SizeOption>","</SizeOption>",false));
      mSizeCombo.setSelectedIndex(option);
      readSize = (option==0); // Custom size
    }
    catch (Exception exc) {
      System.err.println ("Incorrect size option for HtmlView");
      exc.printStackTrace();
      mSizeCombo.setSelectedIndex(0);
    }
    try {
      mX = Integer.parseInt(OsejsCommon.getPiece(_input,"<X>","</X>",false));
      mY = Integer.parseInt(OsejsCommon.getPiece(_input,"<Y>","</Y>",false));
    }
    catch (Exception exc) {
//      System.err.println ("Incorrect position for HtmlView");
//      exc.printStackTrace();
      mX = 0;
      mY = 0;
    }
    if (readSize) {
      String width = OsejsCommon.getPiece(_input,"<Width>","</Width>",false);
      if (width!=null) mWidthField.setText(width.trim());
      String height = OsejsCommon.getPiece(_input,"<Height>","</Height>",false);
      if (height!=null) mHeightField.setText(height.trim());
      resizeEmulator();
    }
    String showText = OsejsCommon.getPiece(_input,"<KeepHidden>","</KeepHidden>",false);
    mHiddenButton.setSelected("true".equals(showText) || mEjs.getOptions().forceKeepPreviewHidden());

    mTree.readRootProperties(OsejsCommon.getPiece(_input,"<RootProperties>","</RootProperties>",false));

    mTree.readString(OsejsCommon.getPiece(_input,"<Tree>","</Tree>",false),-1);
    setChanged(false);
  }

  static private class PredefinedSizeOption extends SizeOption {
    
    public PredefinedSizeOption(String name, int width, int height) {
      super(name,width,height);
    }
    
    public String toString() {
      return res.getString("HtmlView.SizeOption."+super.toString());
    }
    
  }

// ------ End of implementation of Editor

  public void refreshEmulator() {
    if (mEmulator==null || !mEmulator.isVisible()) return;
    File currentFile = mEjs.getCurrentXMLFile();
    SimulationXML simulation = new SimulationXML("TestSimulation");
    mEjs.getSimInfoEditor().fillXMLSimulation(simulation);
    String name = FileUtils.getPlainNameAndExtension(currentFile).getFirstString();
    simulation.setInformation(INFORMATION.TITLE, name+" ("+mName+")");
    if (mEjs.supportsJava()) simulation.setViewOnly(null);
    else mEjs.getModelEditor().fillSimulationXMLForHtmlView(simulation);
    Element view = simulation.addView(mName, null,null);
    mTree.fillSimulationXML(simulation,view);

    File cssFile;
    {
    String cssFilename = mEjs.getSimInfoEditor().getCSSFile();
    if (cssFilename.length()>0) cssFile = new File(mEjs.getCurrentDirectory(),cssFilename);
    else cssFile = new File(mEjs.getOutputDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
    }

    File javascriptDir = new File(mEjs.getBinDirectory(),"javascript/lib");
    String cssFilename = FileUtils.getPath(cssFile.getAbsoluteFile());
    String libPath = FileUtils.getPath(javascriptDir);
    String htmlPath = FileUtils.getPath(currentFile.getParentFile());
    mEmulator.setSimulationPaths(cssFilename,libPath, htmlPath);
    LocaleItem locale = getLocale();
    if (locale.isDefaultItem()) mEmulator.setSimulation(getEjs(), simulation,"HtmlView",SimulationXML.sDEFAULT_LOCALE);
    else mEmulator.setSimulation(getEjs(),simulation,"HtmlView",locale.getKeyword());
  }
  
} // end of class
