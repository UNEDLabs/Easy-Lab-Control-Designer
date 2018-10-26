/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified : March 2006
 */

package org.colos.ejs.osejs.edition.html_view;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejss.html_view.JSONDocAccess;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.*;
import java.util.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.io.*;

/**
 * Creates a panel with groups (and subgroups) of buttons, each of which can create
 * a View element.
 */
public class Palette implements DragSourceListener, DragGestureListener {
  static private final Hashtable<String,Icon> iconTable = new Hashtable<String,Icon>(); // The cache of icons already loaded
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final ResourceUtil sysRes = new ResourceUtil("SystemResources");
  static private final ResourceUtil elRes = new ResourceUtil("HtmlViewResources");
  static private final ResourceUtil sTipsRes = new ResourceUtil("ElementTips");

  static private final int ELS_PER_ROW = res.getInteger("View.ElementsPerRow");
  //static private final Insets zeroMargin = new Insets(0,0,0,0);
  static public final Border zeroBorder = new EmptyBorder(2,2,2,2);//(1,1,1,1); //(2,2,2,2);
  static public final Border selectedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);//BorderFactory.createLineBorder(Color.BLACK, 1); //
  static private final GridLayout gridLayout = new GridLayout(0,1,0,0);
  static private Color selectedColor = new Color (128,64,255);

  private boolean editable=true;
  private String classToCreate=null, nameToCreate=null;
  private Cursor defaultCursor=null, selectedCursor=null;
  private JPanel mainPanel;
  private JComponent selected=null;
  private ElementsTree treeOfElements;
  private org.colos.ejs.osejs.Osejs ejs;
  private Set<TitledBorder> titleBorderSet = new HashSet<TitledBorder>(); // LiSetst of title borders so that I can change their colors
  private Set<JComponent> tabbedPanelList = new HashSet<JComponent>(); // Set of tabbed panels so that I can change their colors
  private JTabbedPane compoundTabbedPanel=null;
  private JComponent compoundComponent=null;
  private DragSource dragSource;
  
  private JDialog helpDialog;
  private JEditorPane htmlArea;
  private JSONDocAccess jsonDoc;
  private String mCurrentLink=null;


  /**
   * This method is used to centralize and cache all the icons loaded for each view element type.
   * We need this simple cache mechanism because the Tree CellRenderer will use these icons intensively.
   * @param _elementType String The type of the element, as in "Element.Panel";
   * @return ImageIcon
   */
  static Icon getElementIcon (String _elementType) {
    if (iconTable.get(_elementType)!=null) return iconTable.get(_elementType);
    
    //ResourceUtil privateRes = new ResourceUtil (elRes.getString(_elementType+".properties"),false);
    //String iconName = privateRes.getOptionalString("Icon");
    //if (iconName==null) iconName = sysRes.getString(_elementType+".Icon");
        
    String iconName = sTipsRes.getString(_elementType+".Icon");
    Icon icon = ResourceLoader.getIcon(iconName);
    if (icon!=null) { 
      iconTable.put (_elementType,icon); 
      return icon; 
    }
    System.out.println ("Could not read icon for "+_elementType); 
    return null; 
  }

  static Image getElementImage (Osejs _ejs, String _elementType) {
    if (iconTable.get(_elementType)!=null) {
      Icon icon = iconTable.get(_elementType);
      if (icon instanceof ImageIcon) return ((ImageIcon) icon).getImage();
    }
//    System.err.println ("Icon not ImageIcon for "+_elementType); 

    if (_elementType.startsWith("UserDefined.")) {
      String filename = _elementType.substring(_elementType.lastIndexOf('.')+1);
      File fileIcon = new File(new File(_ejs.getConfigDirectory(),OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH),filename+".gif");
      if (fileIcon.exists()) return ResourceLoader.getImage(fileIcon.getAbsolutePath());
      fileIcon = new File(new File(_ejs.getBinDirectory(),"config/"+OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH),filename+".gif");
      if (fileIcon.exists()) return ResourceLoader.getImage(fileIcon.getAbsolutePath());
    }
//    ResourceUtil privateRes = new ResourceUtil (elRes.getString(_elementType+".properties"),false);
//    String iconName = privateRes.getOptionalString("Icon");
//    if (iconName==null) iconName = sysRes.getString(_elementType+".Icon");
    String iconName = sTipsRes.getString(_elementType+".Icon");
    if (iconName!=null && !iconName.startsWith("data/")) iconName = "data/icons/Elements/"+iconName;
    //System.err.println ("Trying icon "+iconName); 
    return ResourceLoader.getImage(iconName);
  }

  void showHtmlFile(String classToCreate, String linkBack) {
    if (jsonDoc==null) jsonDoc = new JSONDocAccess(ejs);
    if (helpDialog==null) { // create the dialog
      htmlArea = new JEditorPane ();
      htmlArea.setContentType ("text/html");
      htmlArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      htmlArea.setEditable(false);
      htmlArea.addHyperlinkListener(new HyperlinkListener() { // Make hyperlinks work
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
//            System.err.println ("Link is "+e.getDescription());
            if (e.getURL()==null) showHtmlFile(e.getDescription(), mCurrentLink);
            else OSPDesktop.displayURL(e.getURL().toString());
          }
        }
      });
      JScrollPane helpComponent = new JScrollPane(htmlArea);
      helpComponent.setPreferredSize(new Dimension(600,500));
      helpComponent.setBorder(new EmptyBorder(10,10,10,10));

      helpDialog = new JDialog(ejs.getMainFrame());
      helpDialog.getContentPane().setLayout(new BorderLayout());
      helpDialog.getContentPane().add(helpComponent,BorderLayout.CENTER);
      helpDialog.setModal(false);
      helpDialog.pack();
      helpDialog.setLocationRelativeTo(ejs.getMainFrame());
    }
    
    try { // read the help for this element
      if (classToCreate.indexOf('.')<0) {
        String fullClass = elRes.getString("Elements."+classToCreate);
        int index = fullClass.lastIndexOf('.');
        String packageName = fullClass.substring(0,index+1);
        String classname = fullClass.substring(index+1);
        mCurrentLink = packageName+  classname.toUpperCase().charAt(0) + classname.substring(1);
      }
      else mCurrentLink = classToCreate;
      
      // Add all the files in the library directory 
      File cssFile = new File (ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
      if (!cssFile.exists()) {
        File binConfigDir = new File (ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
        cssFile = new File (binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css/ejss.css");
      }
      String cssURL = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(cssFile));
      htmlArea.setText(jsonDoc.getHTML(classToCreate,mCurrentLink,linkBack,cssURL));      
      htmlArea.setCaretPosition(0);
    } catch(Exception exc) { exc.printStackTrace(); }
    
    helpDialog.setTitle(classToCreate);
    helpDialog.setVisible(true);
  }
  
//  ImageIcon getUserIcon (String _elementType) {
//    if (!_elementType.startsWith("Elements.UserDefined.")) return Palette.getElementIcon(_elementType); 
//    String filename = _elementType.substring(_elementType.lastIndexOf('.')+1);
//    File fileIcon = new File(new File(ejs.getConfigDirectory(),OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH),filename+".gif");
//    if (fileIcon.exists()) return ResourceLoader.getIcon(fileIcon.getAbsolutePath());
//    return null;
//  }
  
  public Palette (org.colos.ejs.osejs.Osejs _ejs) {
    ejs = _ejs;
    mainPanel = new JPanel (new BorderLayout());
    
    // Prepare icons and fonts
    Image selectedImage = ResourceLoader.getImage(sysRes.getString("Tree.Create.Icon"));
    Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
    selectedCursor = Toolkit.getDefaultToolkit().createCustomCursor(selectedImage,new Point(5,1),"Create");
    if (selectedCursor==null) selectedCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    defaultCursor = mainPanel.getCursor();

    // Dragging
    dragSource = DragSource.getDefaultDragSource();

    // Get the list of elements to offer
    JPanel topPanel = new JPanel (gridLayout);
    ArrayList<TwoStrings> groups = readElements(_ejs);
    for (TwoStrings groupPair : groups) {
      if (groupPair.getFirstString().endsWith(".group")) continue; // Subgroups are taken care of in ElementGroup
      ElementGroup elementGroup = new ElementGroup (groupPair,groups);
      TitledBorder titleBorder = new TitledBorder(new LineBorder(Color.black,1,true));
      titleBorder.setTitleJustification (TitledBorder.LEFT);
      titleBorder.setTitleFont (font);
      titleBorder.setTitle (res.getString("View.Elements."+groupPair.getFirstString()));
      elementGroup.component.setBorder (titleBorder);
      titleBorderSet.add(titleBorder);
      topPanel.add (elementGroup.component);
    }
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(topPanel,BorderLayout.NORTH);
    panel.addMouseListener( new MouseAdapter() { public void mousePressed  (MouseEvent evt) { clear(); } });

    // Final start-up
    TitledBorder titleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),res.getString("View.Elements"));
    titleBorder.setTitleJustification (TitledBorder.LEFT);
    titleBorder.setTitleFont (InterfaceUtils.font(null,res.getString("Editor.TitleFont")));
    titleBorderSet.add(titleBorder);

    //    JScrollPane scrollPanel = new JScrollPane (panel);
    //    scrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    //    mainPanel.add (scrollPanel,BorderLayout.CENTER);
    mainPanel.add (panel,BorderLayout.CENTER);
    mainPanel.setBorder (titleBorder);
    mainPanel.setFocusable(true);
    //    if (OSPRuntime.isMac()) mainPanel.setPreferredSize(new Dimension (res.getInteger("MAC.View.CreationPanelWidth"),0));
    //    else mainPanel.setPreferredSize(new Dimension (res.getInteger("View.CreationPanelWidth"),0));
    mainPanel.addKeyListener(
        new java.awt.event.KeyAdapter() {
          public void keyPressed (java.awt.event.KeyEvent _e) {
            if (_e.getKeyCode()==KeyEvent.VK_ESCAPE) clear();
            else if (_e.getKeyCode()==KeyEvent.VK_F1) {
              // ejs.openWikiPage((classToCreate==null) ? "HtmlView" : classToCreate);
              if (classToCreate==null) ejs.openWikiPage("HtmlView");
              else {
                if (classToCreate.indexOf(".UserDefined")>=0) ejs.openWikiPage(classToCreate);
                else showHtmlFile(classToCreate.substring(classToCreate.indexOf('.')+1),null);
              }
            }
          }
        }
        );
    mainPanel.addMouseMotionListener(
        new java.awt.event.MouseMotionAdapter() {
          public void mouseMoved (java.awt.event.MouseEvent _e) { if (selected!=null) mainPanel.requestFocus(); }
        }
        );
    
  }
  
  // -----------------------------------
  // Drag methods
  // -----------------------------------

  public void dragGestureRecognized(DragGestureEvent dge) {
    if (classToCreate==null) return;
    String classToDrag = classToCreate.substring(classToCreate.indexOf('.')+1);
//    System.err.println ("Class to create = "+classToCreate); 
//    System.err.println ("Class to drag = "+classToDrag); 
    nameToCreate = org.colos.ejs.osejs.OsejsCommon.firstToLower(dge.getComponent().getName());
    clear();
    Transferable transferable = new Selection(classToDrag);
    if (DragSource.isDragImageSupported()) {
      Component c = dge.getComponent();
      Image image = null;
      if (c instanceof JLabel) {
        Icon icon = ((JLabel) c).getIcon();
        if (icon instanceof ImageIcon) image = ((ImageIcon) icon).getImage();
      }
      if (image==null) image = Palette.getElementImage(ejs,classToDrag);
      dge.startDrag(null, image, new Point(10,0), transferable, this);
    }
    else dge.startDrag(null, transferable); 
  }

  public void dragDropEnd(DragSourceDropEvent dsde) {
    clear(); 
  }

  public void dragEnter(DragSourceDragEvent dsde) { }

  public void dragExit(DragSourceEvent dse) { }

  public void dragOver(DragSourceDragEvent dsde) { }

  public void dropActionChanged(DragSourceDragEvent dsde) { }

  // -----------------------------------
  // Utility methods
  // -----------------------------------

  public String getClassToCreate () { return classToCreate; }
  public String getNameToCreate  () { return nameToCreate; }
  public void setTree (ElementsTree _tree) { this.treeOfElements = _tree; }
  void requestFocus() { mainPanel.requestFocus(); }
  boolean hasIconSelected() { return selected!=null; }

  public Component getComponent () { return mainPanel; }

  public void setEditable (boolean _edit) { editable = _edit; }

  public void setColor (Color _color) {
    for (TitledBorder titleBorder : titleBorderSet) titleBorder.setTitleColor(_color);
    for (JComponent subgroupPanel : tabbedPanelList) subgroupPanel.setForeground(_color.darker());
  }

  public void clear () {
    if (treeOfElements!=null) treeOfElements.setCursor (defaultCursor);
    mainPanel.setCursor (defaultCursor);
    if (selected!=null) selected.setBorder(zeroBorder);
    selected=null;
    classToCreate = null;
  }

  public void setFont (Font _font) { }

  /**
   * Reads the list of elements to display in the View panel.
   * Each entry in the list is a pair of Strings with the group name and the element list
   */
  private ArrayList<TwoStrings> readElements (org.colos.ejs.osejs.Osejs _ejse) {
    ArrayList<TwoStrings> groups = new ArrayList<TwoStrings>();
    String[] bigGroupsList = ResourceUtil.tokenizeString(elRes.getString("Elements"));
    for (int i=0; i<bigGroupsList.length; i++) {
      String bigGroupElements = elRes.getString(bigGroupsList[i]);
      groups.add(new TwoStrings(bigGroupsList[i],bigGroupElements));
      StringTokenizer tkn = new StringTokenizer(bigGroupElements," \t");
      while (tkn.hasMoreTokens()) {
        String subgroup = tkn.nextToken();
        groups.add(new TwoStrings(subgroup,elRes.getString(subgroup)));
      }
    }
    return groups;
  }

  // ----- Internal classes

  static private void addToComponent (JComponent component, ArrayList<JComponent> list) {
    //for (int j=list.size(); j<=ELS_PER_ROW; j++) list.add(addOneElement ("EMPTY"));
    Box line = Box.createHorizontalBox();
    int counter = 0;
    for (JComponent button : list) {
      line.add(button); 
      line.add(Box.createHorizontalStrut(4));
      counter++;
      if (counter>=ELS_PER_ROW) { 
        component.add(line);
        line = Box.createHorizontalBox();
        counter = 0;
      }
    }
    if (counter>0) component.add(line);
  }

  /**
   * Processes a group of elements as defined in the ElementsOrdered file.
   */
  private class ElementGroup {
    JComponent component;
    int numElements;

    ElementGroup (TwoStrings _groupPair, ArrayList<TwoStrings> _groups) {
      String[] element = ResourceUtil.tokenizeString(_groupPair.getSecondString());
      numElements = element.length;
      boolean hasSubgroups = false;
      for (int i=0; i<element.length; i++) if (element[i].endsWith(".group")) { hasSubgroups = true; break; }
      if (!hasSubgroups) { // It is a plain group of elements
        component = new JPanel(new GridLayout(2,1)); // 2,ELS_PER_ROW,0,0));
        component.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
          public void mouseMoved (java.awt.event.MouseEvent _e) { if (selected!=null) mainPanel.requestFocus(); }
        });
        component.addMouseListener( new MouseAdapter() { public void mousePressed  (MouseEvent evt) { clear(); } });
        // Fill it with the elements
        ArrayList<JComponent> list = new ArrayList<JComponent>(); 
        for (int i=0; i<element.length; i++) list.add(addOneElement(element[i]));
        addToComponent(component,list);
        return;
      }
      // It has subgroups
      JTabbedPane tabpanel = new JTabbedPane (SwingConstants.NORTH);
      tabbedPanelList.add(tabpanel);
      component = tabpanel;
      component.addMouseListener( new MouseAdapter() { public void mousePressed  (MouseEvent evt) { clear(); } });

      for (int i=0; i<element.length; i++) {
        TwoStrings pairFound = null;
        for (TwoStrings pair : _groups) if (pair.getFirstString().equals(element[i])) { pairFound = pair; break; }
        if (pairFound==null) {
          System.out.println ("Error: group "+element[i]+" not found in the list");
          continue;
        }
        ElementGroup subgroup = new ElementGroup (pairFound,_groups);
        int total = subgroup.numElements;
        if (pairFound.getFirstString().equals("Compound.group")) {
          compoundTabbedPanel = tabpanel;
          compoundComponent = subgroup.component;
          total += readCompoundElements (subgroup.component);
        }
        if (total<=0) continue; // do not add this panel
        String tabHeader = element[i].substring(0,element[i].indexOf('.'));
        String iconName = sysRes.getOptionalString("View.Elements.Groups."+tabHeader+".Icon");
        if (iconName!=null) {
          Icon groupIcon = ResourceLoader.getIcon(iconName);
          String toolTipText = res.getOptionalString("View.Elements.Groups."+tabHeader+".ToolTip");
          tabpanel.add (subgroup.component);
          tabpanel.setIconAt(tabpanel.getTabCount()-1,groupIcon);
          tabpanel.setToolTipTextAt(tabpanel.getTabCount()-1,toolTipText);
        }
        else {
          String txt = res.getOptionalString("View.Elements.Groups."+tabHeader);
          if (txt!=null) tabHeader = txt;
          tabpanel.add(tabHeader, subgroup.component);
        }
      }  
    }

    /**
     * Creates a panel with all compound elements
     * @param _component
     * @return the number of elements read
     */
    private int readCompoundElements (final JComponent _component) {
      ArrayList<JComponent> list = addUserElements(new File(ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/"+OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH),false);
      list.addAll(addUserElements(new File(ejs.getConfigDirectory(),OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH),true));
      if (list.size()>0) addToComponent (_component,list);
      int elementsRead = list.size();
      _component.setToolTipText(res.getString("View.RightClickToAddCustomElement"));

      _component.addMouseListener(new MouseAdapter(){
        public void mousePressed(final MouseEvent _evt) {
          if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
            Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final Transferable contents = theClipboard.getContents(null);
            final DataFlavor flavor = Selection.viewFlavor;
            if (!contents.isDataFlavorSupported(flavor)) { 
              JOptionPane.showMessageDialog(mainPanel, res.getString("View.FirstCopyTree"),res.getString("Warning"),JOptionPane.INFORMATION_MESSAGE);
              return;
            }
            try { 
              if (((String) contents.getTransferData(flavor)).length()<=0) {
                JOptionPane.showMessageDialog(mainPanel, res.getString("View.FirstCopyTree"),res.getString("Warning"),JOptionPane.INFORMATION_MESSAGE);
                return;
              }
            }
            catch (Exception exc) { 
              exc.printStackTrace(); 
              return; 
            };
            JPopupMenu addPopup = new JPopupMenu();
            addPopup.add(new AbstractAction(res.getString("View.AddCustomElement")){
              private static final long serialVersionUID = 1L;
              public void actionPerformed(java.awt.event.ActionEvent e) {
                // Choose a name
                String filename = JOptionPane.showInputDialog(getComponent(),res.getString("Tree.ProvideAName"),
                    res.getString("Tree.NameTitle"),JOptionPane.QUESTION_MESSAGE);
                if (filename==null || filename.trim().length()<=0) return;

                //String iconfile = ImagesAccesory.chooseImage(mainPanel,ejs.getSourceDirectory());

                String iconfile;
                if (ejs.useNativeFileChooser()) {
                  iconfile = FileChooserUtil.chooseFilename(ejs, new File(ejs.getBinDirectory(),"icons"), "GIF", new String[]{"gif"}, false);
                }
                else {
                  JFileChooser fileChooser = OSPRuntime.createChooser("GIF",new String[]{"gif"},ejs.getSourceDirectory().getParentFile());
                  org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(fileChooser);
                  fileChooser.setCurrentDirectory(new File(ejs.getBinDirectory(),"icons"));

                  fileChooser.setFileView(new javax.swing.filechooser.FileView() {
                    public String getDescription(File f) { return null; }
                    public Icon getIcon(File f) {
                      if (f.getName().endsWith(".gif")) {
                        Image image = ResourceLoader.getImage(f.getAbsolutePath());
                        if (image==null) return null;
                        ImageIcon imageIcon = new ImageIcon(image);
                        if (imageIcon.getIconWidth()>24 || imageIcon.getIconHeight()>24)
                          imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT));
                        return imageIcon;
                      }
                      return null; 
                    }
                    public String getName(File f) { return null; }
                    public String getTypeDescription(File f) { return null; }
                    public Boolean isTraversable(File f) { return f.isDirectory(); }
                  });
                  //fileChooser.setAccessory(new com.hexidec.ekit.component.ImageFileChooserPreview(fileChooser));
                  iconfile = OSPRuntime.chooseFilename(fileChooser,mainPanel, false);
                }
                if (iconfile==null) return;
                try {
                  File dir = new File(ejs.getConfigDirectory(),OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH);
                  // Remove any / character
                  filename.replace('\\', '/');
                  int index = filename.lastIndexOf('/');
                  if (index>=0) filename = filename.substring(index+1,filename.length());
                  // remove any extension
                  index = filename.lastIndexOf('.');
                  if (index>=0) filename = filename.substring(0,index);
                  FileUtils.saveToFile (new File(dir,filename+".xml"), null, (String) contents.getTransferData(flavor));
                  FileUtils.copy(ResourceLoader.getResource(iconfile).openInputStream(),new File (dir,filename+".gif"));
                }
                catch (Exception exc) { exc.printStackTrace(); };
                // Now refresh
                addCompoundElementsTab ();
              }
            });
            addPopup.show(_evt.getComponent(),_evt.getX(),_evt.getY());
          }
        }
      }); // End of addMouseListener

      return elementsRead;
    }

    private void addCompoundElementsTab () {
      ElementGroup subgroup = new ElementGroup (new TwoStrings("Compound.group",""),null);
      readCompoundElements (subgroup.component);
      int index = compoundTabbedPanel.indexOfComponent(compoundComponent);
      compoundTabbedPanel.remove(index);
      compoundTabbedPanel.add(subgroup.component,index);
      compoundComponent = subgroup.component;
      compoundTabbedPanel.setSelectedIndex(index);
      String iconName = sysRes.getOptionalString("View.Elements.Groups.Compound.Icon");
      Icon groupIcon = ResourceLoader.getIcon(iconName);
      compoundTabbedPanel.setIconAt(index,groupIcon);
      compoundTabbedPanel.setToolTipTextAt(index,res.getOptionalString("View.Elements.Groups.Compound.ToolTip"));
    }


    private ArrayList<JComponent> addUserElements (final File compoundElementsDirectory, final boolean canBeDeleted) {
      ArrayList<JComponent> list = new ArrayList<JComponent>(); 
      if (!compoundElementsDirectory.exists()) return list;
      try {
        File files[] = javax.swing.filechooser.FileSystemView.getFileSystemView().getFiles(compoundElementsDirectory, false);
        for (int i = 0; i < files.length; i++) {
          if (! (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".xml")) ) continue; // Only ".xml" files
          String filename = files[i].getName().substring(0,files[i].getName().indexOf('.'));
          File fileIcon = new File(compoundElementsDirectory,filename+".gif");
          Icon theIcon = null;
          if (fileIcon.exists()) theIcon = ResourceLoader.getIcon(fileIcon.getAbsolutePath());
          if (theIcon==null) theIcon = ResourceLoader.getIcon("data/icons/root.gif");
          String tooltip = filename;
          int index = tooltip.indexOf("_"); // TO allow for ordering like this : 1_MyFavourite, 2_AnyOther
          if (index>=0) tooltip = tooltip.substring(index+1);
          JLabel button;
          if (theIcon!=null && theIcon.getIconHeight()>0) button = new JLabel(theIcon);
          else button = new JLabel (filename);
          button.setName (filename);
          button.setToolTipText (tooltip);
          button.setHorizontalAlignment(SwingConstants.CENTER);
          button.setBorder (zeroBorder);
          //button.setMargin (zeroMargin);
          //button.setFocusPainted(false);
          button.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
          button.addMouseMotionListener(
              new java.awt.event.MouseMotionAdapter() {
                public void mouseMoved (java.awt.event.MouseEvent _e) { if (selected!=null) mainPanel.requestFocus(); }
              }
              );
          button.addMouseListener(new MyMouseListener("Elements.UserDefined."+filename));
          button.addMouseListener(new MouseAdapter() {
            public void mousePressed (java.awt.event.MouseEvent _evt) {
              if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(evt)) {
                JPopupMenu popup = new JPopupMenu ();
                if (canBeDeleted) {
                  final String componentFilename = _evt.getComponent().getName();
                  popup.add(new AbstractAction(res.getString("VariablesEditor.Remove")+ " " + componentFilename){
                    private static final long serialVersionUID = 1L;
                    public void actionPerformed(ActionEvent e) {
                      new File(compoundElementsDirectory,componentFilename+".xml").delete();
                      new File (compoundElementsDirectory,componentFilename+".gif").delete();
                      addCompoundElementsTab (); // refresh
                    }
                  });
                }
                popup.add(new AbstractAction(res.getString("Help.HelpOnElement")+ " " + _evt.getComponent().getName()){
                  private static final long serialVersionUID = 1L;
                  public void actionPerformed(ActionEvent e) { ejs.openWikiPage("HtmlViewElementsUserDefined"); }
                });
                popup.show(_evt.getComponent(),_evt.getX(),_evt.getY());
              }
            }
          });
          dragSource.createDefaultDragGestureRecognizer(button, DnDConstants.ACTION_COPY_OR_MOVE, Palette.this);
          list.add(button);
        }
      }
      catch (Exception _exc) { _exc.printStackTrace(); } // Could not access the local file system
      return list;
    }

    private JComponent addOneElement (final String _elementName) {
      String name = null, tip = null, icon = null;      
      if (_elementName.equalsIgnoreCase("EMPTY")) icon = sysRes.getString("Elements.EMPTY.Icon");
      else {
        // Try the combined file first
        name = sTipsRes.getOptionalString(_elementName+".Name");
        tip  = sTipsRes.getOptionalString(_elementName+".ToolTip");
        icon = sTipsRes.getOptionalString(_elementName+".Icon");
        if (icon!=null && !icon.startsWith("data/")) icon = "data/icons/Elements/"+icon;
        if (name==null || tip==null || icon==null) {
          System.err.println ("Name not found for "+_elementName);
          return null;
        }
      }
      Icon theIcon = ResourceLoader.getIcon(icon);
      JLabel button;
      if (theIcon!=null && theIcon.getIconHeight()>0) {
        button = new JLabel(theIcon);
        iconTable.put ("Elements."+_elementName,theIcon);
      }
      else {
        System.out.println ("Could not read icon " + icon + " for "+_elementName);
        button = new JLabel (_elementName);
      }
      button.setHorizontalAlignment(SwingConstants.CENTER);
      button.setBorder (zeroBorder); //new EmptyBorder(1,1,1,1));
      //button.setMargin (zeroMargin);
      //button.setFocusPainted(false);
      button.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
        public void mouseMoved (java.awt.event.MouseEvent _e) { if (selected!=null) mainPanel.requestFocus(); }
      });
      if (_elementName.equals("EMPTY")) button.setEnabled(false);
      else {
        button.setName (name);
        button.setToolTipText (name+": "+tip);
        button.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        button.addMouseListener(new MyMouseListener("Elements."+_elementName));
        final String translatedName = name;
        button.addMouseListener(new MouseAdapter() {
          public void mousePressed (java.awt.event.MouseEvent _evt) {
            if (OSPRuntime.isPopupTrigger(_evt)) { //SwingUtilities.isRightMouseButton(_evt)) {
              JPopupMenu popup = new JPopupMenu ();
              popup.add(new AbstractAction(res.getString("Help.HelpOnElement")+ " " + translatedName){
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) {
//                  ejs.openWikiPage("HtmlViewElements."+_elementName); 
                  showHtmlFile(_elementName,null);
                }
              });
              popup.add(new AbstractAction("Wiki " + res.getString("Help.HelpOnElement")+ " " + translatedName){
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent e) { ejs.openWikiPage("HtmlViewElements."+_elementName); }
              });
              popup.show(_evt.getComponent(),_evt.getX(),_evt.getY());
            }
          }
        });
      }
      dragSource.createDefaultDragGestureRecognizer(button, DnDConstants.ACTION_COPY_OR_MOVE, Palette.this);
      return button;
    }

    private class MyMouseListener extends MouseAdapter {
      private String classname=null;
      MyMouseListener (String _classname) { classname = _classname; }

      public void mousePressed(MouseEvent  evt) {
        if (OSPRuntime.isPopupTrigger(evt) //SwingUtilities.isRightMouseButton(evt) 
            || !editable) return;
        mainPanel.requestFocus();
        JComponent button = (JComponent) evt.getComponent();
        if (selected!=null && selected!=button) selected.setBorder(zeroBorder);
        if (button.getBorder()==zeroBorder) { // was not selected
          button.setBorder(selectedBorder);
          button.setBackground(selectedColor);
          nameToCreate = org.colos.ejs.osejs.OsejsCommon.firstToLower(button.getName());
          classToCreate = classname;
          selected = button;
          if (treeOfElements!=null) treeOfElements.setCursor (selectedCursor);
          mainPanel.setCursor (selectedCursor);
        }
        else {
          button.setBorder(zeroBorder);
          button.setBackground(mainPanel.getBackground());
          button.setBackground(button.getParent().getBackground());
          selected = null;
          clear();
        }
      }
    }  // End of class MYCL

  } // end of class ElementGroup

}