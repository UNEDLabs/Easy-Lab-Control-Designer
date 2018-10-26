/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.view;

import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.swing.*;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.datatransfer.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.*;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;

//--------------------

public class TreeOfElements {//implements InteractiveMouseHandler {
  static private ResourceUtil res    = new ResourceUtil ("Resources");
  static private ResourceUtil sysRes = new ResourceUtil ("SystemResources");

  static public final Icon linkIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("View.Link.Icon"));
  static public final Icon unlinkIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("View.Unlink.Icon"));
  static public final Icon editIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("View.Edit.Icon"));
  static public final Icon writeIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("View.Write.Icon"));
  static public final Icon actionIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("View.Action.Icon"));
  static public final Icon rootIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("Tree.Main.Icon"));
  static private final Icon captureIcon=org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("Tree.Capture.Icon"));

  static private String[] possiblePositions = { "center", "north", "south", "west", "east" };
  static private String[] possiblePositionsTranslated;

  static private String positionFromTranslated(String _txt) {
    _txt = _txt.trim();
    for (int i=0; i<possiblePositionsTranslated.length; i++) {
      if (possiblePositionsTranslated[i].equals(_txt)) return possiblePositions[i];
    }
    return _txt;
  }

  static private String translatePosition(String _txt) {
    String translated = res.getOptionalString("View.Positions."+_txt);
    if (translated!=null) return translated;
    return _txt;
  }

  static {
    int l = possiblePositions.length;
    possiblePositionsTranslated = new String [l];
    for (int i = 0; i<l ; i++) possiblePositionsTranslated[i] = translatePosition(possiblePositions[i]);
  }

  //  private boolean showDeprecatedFields = false;
  private boolean changed=false, editable=true;
  private int codeCounter=0; // Used to split a big createControl() method in smaller pieces
  private CreationPanelRow creationPanel;
  private DefaultMutableTreeNode rootNode, currentNode=null, mainWindowNode=null;
  private EjsControl control;
  private Osejs ejs;

  private JPanel mainPanel;
  private JPopupMenu popup,reducedPopup;
  private JLabel topLabel,positionLabel;
  private JMenuItem positionMi, reparentMi, renameMi, upMi, downMi, removeMi , copyMi, cutMi, pasteMi, helpMi;
  private JCheckBoxMenuItem mainWindowMi;
  private TitledBorder titleBorder;
  JTree tree;
  private DefaultTreeModel treeModel;
  //  private Cursor currentCursor;
  private Hashtable<String, String> pasteList=null;
  private String pasteParent = null;
  private Font myFont=InterfaceUtils.font(null,res.getString("Osejs.DefaultFont"));

  private JCheckBox hiddenButton;
  private boolean keepHidden = false;

  public TreeOfElements (Osejs _ejs, CreationPanelRow _cp) {
    ejs = _ejs;
    creationPanel = _cp;
    control = new EjsControl();
    control.addTarget("_ejs_",this);
    control.setValue("_isPlaying",false);
    control.setValue("_isPaused",true);
    control.setValue("_Ejs_ShowDeprecated_",false);
    EjsControl.setDefaultScreen(OsejsCommon.getScreenNumber(ejs.getMainFrame()));
    createMenu();

    rootNode  = createNewNode ("Tree.Main",res.getString("Tree.Main"),false);
    treeModel = new DefaultTreeModel (rootNode);
    tree      = new JTree(treeModel);
    tree.setEditable(false);
    tree.setFont(myFont);
    tree.setRootVisible(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    //    tree.addTreeSelectionListener (new TSL());
    //    treeModel.addTreeModelListener(new MyTreeModelListener());
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.setCellRenderer(new MyRenderer());
    tree.setSelectionRow (0);
    tree.setToggleClickCount(3);
    tree.addMouseListener (new TreeMouseListener());
    tree.addTreeExpansionListener(new MyTreeExpansionListener());
    
    tree.setDragEnabled(true);
    tree.setDropMode(DropMode.ON_OR_INSERT);
    tree.setTransferHandler(new TreeTransferHandler(this));
    
    JScrollPane scrollPanel = new JScrollPane(tree);
    //    tree.setFocusable(true);
    tree.addKeyListener(
        new java.awt.event.KeyAdapter() {
          public void keyPressed (java.awt.event.KeyEvent _e) {
            //          if (_e.getKeyCode()==KeyEvent.VK_ESCAPE) creationPanel.clear();
            //          else if (_e.getKeyCode()==KeyEvent.VK_DELETE && currentNode!=null) {
            //            if (currentNode!=rootNode) remove(currentNode);
            //          }
            //          else
            if (_e.getKeyCode()==KeyEvent.VK_F1) { 
              ejs.openWikiPage((currentNode==null || currentNode==rootNode) ? "View" : viewOf(currentNode).getClassname());
            }
          }
        }
        );

    tree.addMouseMotionListener(
        new java.awt.event.MouseMotionAdapter() {
          public void mouseMoved (java.awt.event.MouseEvent _e) { 
            if (creationPanel.hasIconSelected()) creationPanel.requestFocus(); }
        }
        );

    hiddenButton = new JCheckBox(res.getString("Tree.KeepPreviewHidden"),keepHidden);
    hiddenButton.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        keepHidden = e.getStateChange()==ItemEvent.SELECTED;
        if (ejs.supportsJava()) ControlWindow.setKeepHidden(keepHidden);
        showWindows(!keepHidden);
      }
    });

    titleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),res.getString("Tree.TreeOfElements"));
    titleBorder.setTitleJustification (TitledBorder.LEFT);
    titleBorder.setTitleFont (InterfaceUtils.font(null,res.getString("Editor.TitleFont")));
    mainPanel = new JPanel (new BorderLayout ());
    mainPanel.add(scrollPanel,BorderLayout.CENTER);
    mainPanel.add(hiddenButton,BorderLayout.SOUTH);
    mainPanel.setBorder (titleBorder);
    mainPanel.setPreferredSize(new Dimension(200,300)); // Any size - not too big - will do
    //    mainPanel.validate();
    clear();
  }

  Osejs getEjs () { return ejs; }

  URL getCodebase () { return null; }

  public boolean keepPreviewHidden() { return keepHidden; }
  public void setKeepPreviewHidden(boolean hidden) { keepHidden = hidden; }

  public EjsControl getControl () { return control; }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) { titleBorder.setTitleColor (_color); }

  public boolean isEmpty() { return rootNode.getChildCount()<=0; }

  public void setFont (Font _font) {
    myFont = _font;
    //tree.setFont (_font);
    //treeModel.reload ();
    traverseSetFont (rootNode,_font);
  }

  public boolean isChanged () {
    if (changed) return true;
    return traverseHasChanged(rootNode);
  }

  public void setChanged (boolean _changed) {
    changed = _changed;
    traverseSetChanged(rootNode,_changed);
  }

  //  public boolean isShowDeprecatedFields() { return showDeprecatedFields; }

  //  public void setShowDeprecatedFields() {
  //    showDeprecatedFields = true;
  //    control.setValue("_Ejs_ShowDeprecated_",true);
  //  }

  //  public void showDeprecatedFields() { if (showDeprecatedFields) traverseShowDeprecated(rootNode); }

  public void updateProperties(boolean showErrors) { //traverseUpdateProperties(rootNode); }
    for (int i=0; i<rootNode.getChildCount(); i++) 
      traverseUpdateProperties ((DefaultMutableTreeNode) rootNode.getChildAt(i),showErrors);
  }

  public void clear() {
    //    showDeprecatedFields = false;
    traverseClear (rootNode);
    rootNode.removeAllChildren();
    treeModel.reload();
    tree.setSelectionRow (0);
    currentNode = rootNode;
    control.clear();
    control.setValue("_isPlaying",false);
    control.setValue("_isPaused",true);
    control.setValue("_Ejs_ShowDeprecated_",false);
    mainWindowNode=null;
    hiddenButton.setSelected(keepHidden = ejs.getOptions().forceKeepPreviewHidden());
    setChanged(false);
  }

  public void setEditable (boolean _edit) {
    editable = _edit;
    //    tree.setEditable(_edit);
    creationPanel.setEditable (_edit);
  }

  public StringBuffer generateCode (int _type) {
    StringBuffer text = new StringBuffer();
    if (rootNode.getChildCount()==0) { // Make sure that you get at least a window to stop the simulation
      if (_type==Editor.GENERATE_CODE) {
        text.append("    addElement(new org.colos.ejs.library.control.swing.ControlFrame(),\"EmptyFrame\")\n");
        text.append("      .setProperty(\"exit\",\"true\")\n");
        //        text.append("      .setProperty(\"onExit\",\"_model._onExit()\")\n");
        text.append("      .setProperty(\"waitForReset\",\"true\")\n");
        text.append("      .setProperty(\"title\",\"???\")\n");
        text.append("      .setProperty(\"visible\",\"true\")\n");
        text.append("      .setProperty(\"background\",\"red\")\n");
        text.append("      .setProperty(\"size\",\"300,300\");\n");
      }
      else if (_type==Editor.GENERATE_CAPTURE_WINDOW) {
        text.append("        capture=\"EmptyFrame\" width=\"300\" height=\"300\"");
      }
      return text;
    }
    if (_type==Editor.GENERATE_CODE) {
      text.append("    // This first frame is added due to what I consider a bug in Java (Paco)\n");
      text.append("    addElement( new org.colos.ejs.library.control.swing.ControlFrame(),\"_TOP_SECRET_\")\n");
      text.append("      .setProperty(\"waitForReset\",\"true\")\n");
      text.append("      .setProperty(\"visible\",\"false\")\n");
      text.append("      .setProperty(\"background\",\"green\")\n");
      text.append("      .setProperty(\"size\",\"100,100\");\n");
    }
    codeCounter = 0;
    text.append(traverseGenerateCode(rootNode, _type));
    return text;
  }

  /**
   * Returns a String with the main window dimension
   * @return
   */
  public Dimension getMainWindowDimension () {
    for (int i=0,n=rootNode.getChildCount(); i<n; i++) {
      Dimension dim = viewOf((DefaultMutableTreeNode)rootNode.getChildAt (i)).getMainWindowDimension();
      if (dim!=null) return dim;
    }
    return null;
  }

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    for (int i=0; i<rootNode.getChildCount(); i++)
      traverseSearch ((DefaultMutableTreeNode) rootNode.getChildAt(i),list,_info,_searchString,_mode);
    return list;
  }

  public StringBuffer saveStringBuffer (String _blockName) { return traverseSaveStringBuffer(rootNode,_blockName); }

  public boolean parentAcceptsChild (DefaultMutableTreeNode _parent, DefaultMutableTreeNode _child) {
    return (viewOf(_parent).acceptsChild(viewOf(_child)));
  }
  
  public boolean parentAcceptsChild (DefaultMutableTreeNode _parent, String _childClass) {
    if (_childClass.startsWith("Elements.UserDefined.")) return true;
//    System.out.println ("Creating dummy element for "+_childClass);
    ViewElement dummy = new ViewElement(_childClass);
//    System.out.println ("Parent "+_parent+" accepts "+dummy+" = "+(viewOf(_parent).acceptsChild(dummy)));
    return (viewOf(_parent).acceptsChild(dummy));
  }
  
  public void readString (String _input, String _blockName, int _refPosition) {
    //    System.out.println ("Reading with blockname "+_blockName+"\n"+_input);
    int begin = _input.indexOf("<"+_blockName+".Element>");
    int l = _blockName.length();
    //    int row = 0;
    //    TreePath firstNodePath=null;
    Vector<TreePath> expandedList = new Vector<TreePath>();
    StringBuffer deprecatedBuffer = new StringBuffer();
    while (begin>=0) {
      DefaultMutableTreeNode parent=null,child;
      int end = _input.indexOf("</"+_blockName+".Element>");
      String piece = _input.substring(begin+l+10,end).trim();
      String classname  = OsejsCommon.getPiece(piece,"<Type>","</Type>",false);
      //      System.out.println("Reading "+classname);
      String name       = OsejsCommon.getPiece(piece,"<Property name=\"name\">","</Property>",false);
      String parentName = OsejsCommon.getPiece(piece,"<Property name=\"parent\">","</Property>",false);
      String expanded   = OsejsCommon.getPiece(piece,"<Expanded>","</Expanded>",false);
      boolean choosePosition = false;
      if (parentName!=null && parentName.equals(pasteParent)) choosePosition=true;
      //      String visible    = TabbedEditor.getPiece(piece,"<Visible>","</Visible>",false);
      if (pasteList!=null) {  // Pasting: Check for new names
        // Change parent, if necessary
        if (parentName!=null) {
          String newNameForParent = pasteList.get(parentName);
          if (newNameForParent != null) parentName = newNameForParent;
        }
        // Change name, if necessary
        if (nameExists(name)) {
          String uniqueName = getUniqueName(name);
          pasteList.put(name,uniqueName);
          name = uniqueName;
        }
      }
      if (parentName!=null) parent = findNode(parentName);
      if (parent==null) parent = rootNode;
      child = createNewNode (classname,name,true);
      viewOf(child).setEditable(editable);
      boolean isMenuBar = (viewOf(parent).getElement() instanceof ControlWindow) && viewOf(child).getElement().getObject() instanceof JMenuBar;

      int _position = parent.getChildCount();
      if (_refPosition>=0) {
        _position = _refPosition;
        _refPosition = -1; // only the first time
      }
      
      if (pasteList!=null) { // Check validity of parent and child
        if (viewOf(child).isWindow()) parent = rootNode;
        else if (parent==rootNode) {
          JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),
              res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
          return;
        }

        if (!viewOf(parent).acceptsChild(viewOf(child))) {
          DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) parent.getParent();
          if (viewOf(grandParent).acceptsChild(viewOf(child))) {
            _position = grandParent.getIndex(parent);
            parent = grandParent;
          }
          else {
            JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),
                res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
            return;
          }
        }

        if (isMenuBar && (viewOf(parent).getElement() instanceof ControlWindow)) { // Check that it doesn't have already a menubar
          if (((ControlWindow) viewOf(parent).getElement()).getJMenuBar()!=null) {
            JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.HasJMenuBar"), res.getString("Tree.ErrorMessage"), JOptionPane.ERROR_MESSAGE);
            return; // No free slot!
          }
        }
      }
      String position = OsejsCommon.getPiece(piece,"<Property name=\"position\">","</Property>",false);
//      System.out.println ("Position is "+position);
      if (choosePosition) {
        if (isMenuBar); // Do nothing
        else if (viewOf(parent).hasBorderLayout()) {
          if (!choosePosition(parent, child, position)) return; // No free slot, null for "use ANY free slot as default"
        }
      }
      else {
        if (position!=null) viewOf(child).getElement().setProperty("position",position);
      }
      treeModel.insertNodeInto (child, parent, _position);
      //      if (pasteList==null) { tree.expandRow(row); row++; }
      //      else {
      //        if (firstNodePath==null) firstNodePath = new TreePath(child.getPath());
      //      }
      if (!viewOf(child).isWindow()) viewOf(parent).add(viewOf(child),-1);
      if (viewOf(child).getElement() instanceof ControlSwingElement) // getVisual()!=null)
        ((ControlSwingElement) viewOf(child).getElement()).getVisual().addMouseListener(new ElementListener(child));
      // Now process the properties
      viewOf(child).readString(piece,deprecatedBuffer);
      if ("true".equals(viewOf(child).getElement().getProperty("_ejs_mainWindow"))) {
        if (mainWindowNode==null || mainWindowNode==child) setMainWindow(child);
        else {
          viewOf(child).getElement().setProperty("_ejs_mainWindow",null);
          viewOf(child).getElement().setProperty("exit",null);
        }
      }
      if (expanded!=null) {
        if (expanded.startsWith("true")) expandedList.add(new TreePath(child.getPath()));
        else {
          ControlElement element = viewOf(child).getElement();
          if (element instanceof ControlWindow) ((ControlWindow) element).hide();
        }
      }
      // Go to next element
      _input = _input.substring(end+l+11).trim();
      begin = _input.indexOf("<"+_blockName+".Element>\n");
    }
    //    if (firstNodePath!=null) tree.expandPath(firstNodePath);
    int size = expandedList.size();
    for (int i=0; i<size; i++) {
      TreePath path = expandedList.elementAt(size-i-1);
      tree.expandPath(path);
    }
    tree.expandPath(new TreePath(rootNode.getPath()));
    //    System.out.println ("Control update after reading");
    control.update();
    control.finalUpdate();
    //    if (showDeprecatedFields) traverseShowDeprecated(rootNode);
    setChanged(false);
    String deprecatedString = deprecatedBuffer.toString();
    if (deprecatedString.length()>0) {
      JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("Tree.DeprecatedPropertiesDetected")+"\n\n"+deprecatedString,
          res.getString("Warning"),JOptionPane.WARNING_MESSAGE);
    }
  }

  //  private void add (String _classname, String _name) { add (currentNode,_classname,_name,getComponent()); }

  private void readCompound (DefaultMutableTreeNode _parent, String _classname) {
    String filename = OsejsCommon.CUSTOM_ELEMENTS_DIR_PATH+"/"+_classname.substring(_classname.lastIndexOf('.')+1)+".xml";
    File file = new File(ejs.getConfigDirectory(),filename);
    if (!file.exists()) file = new File(ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/"+filename);
    String input = FileUtils.readTextFile(file,null);
    if (input==null) {
      JOptionPane.showMessageDialog(null, res.getString("Osejs.File.ReadError")+" "+filename,
          res.getString("Osejs.File.ReadingError"),JOptionPane.INFORMATION_MESSAGE);
      return;
    }      
    String fragment = OsejsCommon.getPiece(input,"<ViewFragment>","</ViewFragment>",true);
    if (fragment!=null) {
      pasteString(_parent,fragment,-1);
      return;
    }
    ejs.readString((File) null,input,true);
  }

  private void add (DefaultMutableTreeNode _parent, String _classname, String _name, Component _component, int _position) {
    if (_classname.startsWith("Elements.UserDefined.")) {
      readCompound (_parent, _classname);
      return;
    }
    int pos = _position;
    //System.out.println ("Add "+_name +" to "+_parent.toString());
    if (_parent==null) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.ProvideAParent"),
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    _name = (String) JOptionPane.showInputDialog(_component,res.getString("Tree.ProvideAName"),
        res.getString("Tree.NameTitle"),JOptionPane.QUESTION_MESSAGE,null,null,_name);
    if (_name==null || _name.trim().length()<=0) return;
    _name = getUniqueName(_name);
    DefaultMutableTreeNode node = createNewNode (_classname,_name.trim(),false);
    if (node==null) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.CantCreateElement")+_classname,
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    viewOf(node).setEditable(editable);

    if (!viewOf(_parent).acceptsChild(viewOf(node)) && _parent!=rootNode) {
      DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) _parent.getParent();
      if (viewOf(grandParent).acceptsChild(viewOf(node))) {
        pos = grandParent.getIndex(_parent);
        _parent = grandParent;
      }
      else {
        JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),
            res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    if (viewOf(node).getElement() != null) { // This must be here and not in ViewElement
      //      if (viewOf(node).getElement().getComponent()!=null) // This causes no harm
      viewOf(node).getElement().setProperty("text","\""+_name+"\"");
      viewOf(node).getElement().setProperty("title","\""+_name+"\"");
    }

    if (viewOf(node).isWindow() && _parent!=rootNode) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.CantAddWindow"),
          res.getString("Tree.Warning"),JOptionPane.ERROR_MESSAGE);
      ((ControlWindow) viewOf(node).getElement()).disposeWindow();
      addNode (node,rootNode,_component,-1);
      return;
    }
    if (!viewOf(node).isWindow() && _parent==rootNode) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.CantAddNotWindow"),
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    //    System.out.println ("Add "+node +" to "+_parent);
    addNode (node,_parent,_component, pos);
    //    if (showDeprecatedFields) traverseShowDeprecated(rootNode);
    //    creationPanel.nameToCreate =  getUniqueName(creationPanel.nameToCreate);
  }

  public DefaultMutableTreeNode findNode (String _name) {
    if (_name==null) return null;
    return traverseFind (rootNode,_name);
  }

  public void setCursor (Cursor _cursor) {
    //    currentCursor = _cursor;
    mainPanel.setCursor (_cursor);
    traverseSetCursor (rootNode,_cursor);
  }

  public void execute (String _command) {
    //    System.out.println ("Action = "+_command);
    if (_command.equals("_ejsUpdate")) { control.update(); control.finalUpdate(); }
  }

  private ArrayList<DefaultMutableTreeNode> windowsShowing = new ArrayList<DefaultMutableTreeNode>();

  public void showWindows (boolean _show) {
    if (!_show) windowsShowing.clear();
    for (int i=0,n=rootNode.getChildCount(); i<n; i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
      if (!(viewOf(node).getElement() instanceof ControlWindow)) continue;
      ControlWindow window = (ControlWindow) viewOf(node).getElement();
      if (_show) {
        if (windowsShowing.contains(node)) {
          tree.expandPath(new TreePath(node.getPath()));
          window.show();
        }
      }
      else {
        TreePath path = new TreePath(node.getPath());
        if (window.isVisible()) {
          windowsShowing.add(node);
        }
        if (!keepHidden) tree.collapsePath(path);
        window.hide();
      }
    }
  }

  public java.util.List<ViewElement> getViewElements () {
    java.util.List<ViewElement> list = new ArrayList<ViewElement>();
    for (int i=0,n=rootNode.getChildCount(); i<n; i++) traverseListNodes((DefaultMutableTreeNode) rootNode.getChildAt(i),list);
    return list;
  }


  // --------- Private methods for operation

  public ViewElement viewOf(DefaultMutableTreeNode _node) { return (ViewElement) (_node.getUserObject()); }

  private DefaultMutableTreeNode findFirstFrame () {
    for (int i=0; i<rootNode.getChildCount(); i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
      if (viewOf(node).getElement() instanceof ControlFrame) return node;
    }
    return null;
  }

  private DefaultMutableTreeNode createNewNode (String _classname, String _name, boolean _reading) {
    DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(new ViewElement(ejs,control,_classname,_name,this,_reading));
    viewOf(newnode).setFont(myFont);
    if (viewOf(newnode).getElement() instanceof ControlFrame) {
      // Check if this is the first Frame to make it the mainFrame
      if (findFirstFrame()==null) setMainWindow (newnode);
//      JFrame frame = ((ControlFrame) viewOf(newnode).getElement()).getJFrame();
//            ejs.setMenuBar(frame);
    }
    return newnode;
  }

  /**
   * Lets the user select one of the free slots to position
   * a child in a parent with a Border layout.
   * @return boolean true if succesful, false otherwise
   */
  private boolean choosePosition(DefaultMutableTreeNode _parent, DefaultMutableTreeNode _child, String _currentPosition) {
    // Get the list of free slots
    ArrayList<String> freePositions = new ArrayList<String>();
    for (int i=0,n=possiblePositions.length; i<n; i++) freePositions.add(possiblePositions[i]);
    ControlElement childElement = viewOf(_child).getElement();
    Vector<ControlElement> children = ((ControlContainer) viewOf(_parent).getElement()).getChildren();
    for (Enumeration<ControlElement> childList = children.elements(); childList.hasMoreElements(); ) {
      ControlElement element = childList.nextElement();
      if (element==childElement) continue;
      String position = element.getProperty("position");
      if (position!=null) freePositions.remove(position);
    }
    if (freePositions.size()<=0) {
      JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.NoFreePositions"),
          res.getString("Tree.Warning"), JOptionPane.WARNING_MESSAGE);
      return false; // No free slot!
    }
    // Now choose one of the free slots
    Object [] freePosTxt = freePositions.toArray();
    String [] freePosTranslated = new String[freePosTxt.length];
    for (int i=0,n=freePosTxt.length; i<n; i++) freePosTranslated[i] = translatePosition((String)freePosTxt[i]);
    if (_currentPosition==null) _currentPosition = freePosTranslated[0];
    else _currentPosition = translatePosition(_currentPosition);
    String txt = (String) JOptionPane.showInputDialog(getComponent(),res.getString("Tree.PositionTo"),
        res.getString("Tree.PositionTitle"),JOptionPane.INFORMATION_MESSAGE, null,
        freePosTranslated,_currentPosition);
    if (txt==null || txt.trim().length()<=0) return false; // Canceled by user?
    viewOf(_child).getElement().setProperty("position",positionFromTranslated(txt));
    changed = true;
    return true;
  }

  private boolean addNode (DefaultMutableTreeNode _child, DefaultMutableTreeNode _parent, Component _component, int _position) {
    //   System.out.println ("Adding "+_child +" to "+_parent);
    boolean isJMenuBar;
    try { isJMenuBar = viewOf(_child).getElement().getVisual() instanceof JMenuBar; }
    catch (Exception exc) { isJMenuBar = false; }
    if (currentNode!=null )
      if (isJMenuBar && (viewOf(_parent).getElement() instanceof ControlWindow)) { // Check that it doesn't have already a menubar
        if (((ControlWindow) viewOf(_parent).getElement()).getJMenuBar()!=null) {
          JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.HasJMenuBar"),
              res.getString("Tree.ErrorMessage"), JOptionPane.ERROR_MESSAGE);
          return false; // No free slot!
        }
      }
      else if (viewOf(_parent).hasBorderLayout()) {
        if (!choosePosition(_parent, _child, null)) return false; // No free slot, null for "use ANY free slot as default"
      }
    if (_position<0 || _position>_parent.getChildCount()) treeModel.insertNodeInto (_child, _parent, _parent.getChildCount());
    else treeModel.insertNodeInto (_child, _parent, _position);
    tree.scrollPathToVisible(new TreePath(_child.getPath()));
    if (!viewOf(_child).isWindow()) viewOf(_parent).add(viewOf(_child),_position);
    if (viewOf(_child).getElement() instanceof ControlSwingElement) // getVisual()!=null)
      ((ControlSwingElement) viewOf(_child).getElement()).getVisual().addMouseListener(new ElementListener(_child));
    changed = true;
    return true;
  }

  private void rename (DefaultMutableTreeNode _node) {
    String name = (String) JOptionPane.showInputDialog(getComponent(),res.getString("Tree.RenameTo"),
        res.getString("Tree.RenameTitle"),JOptionPane.QUESTION_MESSAGE,null,null,viewOf(_node).getName());
    if (name==null || name.trim().length()<=0) return;
    name = name.trim();
    String uniqueName = getUniqueName (name);
    if (! name.equals(uniqueName)) JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.NameModified"),
        res.getString("Tree.Warning")+uniqueName, JOptionPane.WARNING_MESSAGE);
    viewOf(_node).setName (uniqueName);
    for (int i=0; i<_node.getChildCount(); i++) {
      viewOf((DefaultMutableTreeNode) _node.getChildAt(i)).getElement().setProperty("parent",uniqueName);
    }
    TreePath path = new TreePath(_node.getPath());
    treeModel.valueForPathChanged (path,viewOf(_node));
    ejs.getTranslationEditor().refresh();
    changed = true;
  }

  private void reparent (DefaultMutableTreeNode _child, String _parentName) {
    DefaultMutableTreeNode parent = traverseFind (rootNode,_parentName);
    if (parent==null) {
      JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotExist"),
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (!viewOf(parent).acceptsChild(viewOf(_child))) {
      JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    treeModel.removeNodeFromParent (_child);
    viewOf(parent).unparent(viewOf(_child));
    addNode (_child,parent,getComponent(),-1);
  }

  private void reposition (DefaultMutableTreeNode _child) {
    if (_child==null) return;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _child.getParent();
    if (parent!=null) choosePosition(parent, _child, viewOf(_child).getPosition());
  }

  private void moveUpAndDown (DefaultMutableTreeNode _child, boolean _up) {
    if (_child==null) return;
    boolean wasExpanded = !tree.isCollapsed(new TreePath(_child.getPath()));
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _child.getParent();
    if (parent==null) return;
    int pos = treeModel.getIndexOfChild (parent, _child);
    if (_up) { if (pos<=0) return; pos--; }
    else { if (pos>=(treeModel.getChildCount (parent)-1)) return;  pos++; }
    treeModel.removeNodeFromParent (_child);
    treeModel.insertNodeInto (_child, parent, pos);
    viewOf(parent).unparent(viewOf(_child));
    viewOf(parent).add(viewOf(_child),pos);
    if (wasExpanded) tree.expandPath(new TreePath(_child.getPath()));
    TreePath path = new TreePath(_child.getPath());
    tree.setSelectionPath(path);
    changed = true;
  }

  void moveTo (final DefaultMutableTreeNode _child, final DefaultMutableTreeNode _parent, final int _position) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        int pos = _position;
        boolean wasExpanded = !tree.isCollapsed(new TreePath(_child.getPath()));
        if (pos<0 || pos>_parent.getChildCount()) pos = _parent.getChildCount();
        DefaultMutableTreeNode currentParent = (DefaultMutableTreeNode) _child.getParent();
        int currentPosition = treeModel.getIndexOfChild (currentParent, _child);
        treeModel.removeNodeFromParent (_child);
        viewOf(currentParent).unparent(viewOf(_child));
        if (_parent==currentParent) {
          treeModel.insertNodeInto (_child, _parent, pos);
          viewOf(_parent).add(viewOf(_child),pos);
        }
        else {
          boolean added = addNode (_child,_parent,getComponent(),pos);
          if (!added) {
            treeModel.insertNodeInto (_child, currentParent, currentPosition);
            viewOf(currentParent).add(viewOf(_child),currentPosition);
          }
        }
        if (wasExpanded) tree.expandPath(new TreePath(_child.getPath()));
      }
    });
  }
  
  private void remove(DefaultMutableTreeNode _child) {
    if (_child==null) return;
    //    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _child.getParent();
    treeModel.removeNodeFromParent(_child);
    if (_child==mainWindowNode) {
      // Try to find a new mainWindow
      if (findFirstFrame()!=null) setMainWindow (findFirstFrame());
    }
    currentNode = null;
    this.traverseClear(_child);
    //    viewOf(parent).remove(viewOf(_child));
    ejs.getTranslationEditor().refresh();
    changed = true;
  }

  Transferable createTransferable(DefaultMutableTreeNode _node) {
    // clipboardParent = _node.getParent().toString();
    StringBuffer text = traverseSaveStringBuffer(_node, "View");
    StringBuffer fragment = new StringBuffer();
    fragment.append("<ViewFragment>\n");
    fragment.append("  <ViewFragment.Parent>"+_node.getParent().toString() +"</ViewFragment.Parent>\n");
    fragment.append("  <ViewFragment.Code>\n");
    fragment.append(text);
    fragment.append("  </ViewFragment.Code>\n");
    fragment.append("</ViewFragment>\n");
    return new ViewSelection(fragment.toString());
  }
  
  private void copy (DefaultMutableTreeNode _node) {
    Transferable selection = createTransferable(_node);
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    theClipboard.setContents(selection,null);
    OSPLog.finest("Copying:");
    OSPLog.finest("  - Flavor:"+ViewSelection.viewFlavor.toString());
    try { OSPLog.finest("  - Contents follow -:\n"+selection.getTransferData(ViewSelection.viewFlavor)); }
    catch (Exception exc) { exc.printStackTrace(); };
    OSPLog.finest("  - End of contents -");
  }

  boolean isFragment(String _code) {
    String parent = OsejsCommon.getPiece(_code,"<ViewFragment.Parent>","</ViewFragment.Parent>",false);
    return parent!=null;
  }

  void paste (final DefaultMutableTreeNode _node, Transferable _transferable, final int _position) {
    OSPLog.finest("Pasting:");
    OSPLog.finest("  - Flavor: "+ViewSelection.viewFlavor.toString());
    OSPLog.finest("  - Flavor supported: "+_transferable.isDataFlavorSupported(ViewSelection.viewFlavor));
    if (_transferable.isDataFlavorSupported(ViewSelection.viewFlavor)) {
      try { 
        final String code = (String) _transferable.getTransferData(ViewSelection.viewFlavor);
        if (isFragment(code))  {
          OSPLog.finest("  - Contents follow -:\n"+code); 
          OSPLog.finest("  - End of contents -");
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              pasteString(_node,code,_position);
            }
          });
        }
        else {
          OSPLog.finest("  - create class of type -:\n"+code); 
          OSPLog.finest("  - End of drag -");
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              add (_node,code,getUniqueName(creationPanel.getNameToCreate()),getComponent(),_position);
            }
          });
        }
      }
      catch (Exception exc) { exc.printStackTrace(); };
    }
  }

  private void paste (DefaultMutableTreeNode _node) {
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    paste (_node,theClipboard.getContents(null),-1);
  }

  private void pasteString (DefaultMutableTreeNode _node, String _text, int _position) {
    if (_text==null) return;
    String parent = OsejsCommon.getPiece(_text,"<ViewFragment.Parent>","</ViewFragment.Parent>",false);
    String code   = OsejsCommon.getPiece(_text,"<ViewFragment.Code>","</ViewFragment.Code>",false).trim();
    //        System.out.println("Parent = "+parent);
    //        System.out.println("Code = "+code);
    pasteList = new Hashtable<String, String>();
    pasteList.put(parent,_node.toString());
    pasteParent = parent;
    readString (code,"View",_position);
    pasteParent = null;
    pasteList = null;
    changed = true;
    ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
    ejs.getTranslationEditor().refresh();
  }

  private boolean nameExists (String name) { return (traverseFind (rootNode, name) != null); }

  public String getUniqueName (String name) {
    name = OsejsCommon.getValidIdentifier(name.trim());
    //    name = name.trim().replace (' ','_');
    String newname = name;
    int i=1;
    while (nameExists(newname)) newname = name + (++i);
    return newname;
  }

  private void setMainWindow (DefaultMutableTreeNode _node) {
    if (mainWindowNode!=null && mainWindowNode!=_node) {
      viewOf(mainWindowNode).getElement().setProperty("_ejs_mainWindow",null);
      viewOf(mainWindowNode).getElement().setProperty("exit",null);
    }
    //    if (_node==mainWindowNode); // There must be a main Window!!! mainWindowNode = null;
    //    else {
    viewOf(_node).getElement().setProperty("_ejs_mainWindow","true");
    viewOf(_node).getElement().setProperty("exit","true");
    mainWindowNode = _node;
    //    }
    tree.repaint();
    changed = true;
  }

  // ----------------------------------------
  // Translation
  // ----------------------------------------

  //  public java.util.Set<LocaleItem> getAvailableTranslations() {
  //    java.util.Set<LocaleItem> trSet = new HashSet<LocaleItem>();
  //    for (int i=0; i<rootNode.getChildCount(); i++)
  //      traverseGetAvailableTranslations ((DefaultMutableTreeNode) rootNode.getChildAt(i),trSet);
  //    return trSet;
  //  }
  //
  //  private void traverseGetAvailableTranslations (DefaultMutableTreeNode node, java.util.Set<LocaleItem> trSet) {
  //    trSet.addAll(viewOf(node).getAvailableTranslations());
  //    for (int i=0,n=node.getChildCount(); i<n; i++)
  //      traverseGetAvailableTranslations ((DefaultMutableTreeNode) node.getChildAt (i),trSet);
  //  }

  public java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>();
    for (int i=0; i<rootNode.getChildCount(); i++)
      traverseGetTranslatableProperties ((DefaultMutableTreeNode) rootNode.getChildAt(i),list);
    return list;
  }

  private void traverseGetTranslatableProperties (DefaultMutableTreeNode node, java.util.List<TranslatableProperty> list) {
    list.addAll(viewOf(node).getTranslatableProperties());
    for (int i=0,n=node.getChildCount(); i<n; i++)
      traverseGetTranslatableProperties ((DefaultMutableTreeNode) node.getChildAt (i),list);
  }

  //  public void removeTranslation (LocaleItem _locale) {
  //    for (int i=0; i<rootNode.getChildCount(); i++)
  //      traverseRemoveTranslation ((DefaultMutableTreeNode) rootNode.getChildAt(i),_locale);
  //  }
  //
  //  private void traverseRemoveTranslation (DefaultMutableTreeNode node, LocaleItem _locale) {
  //    viewOf(node).removeTranslation(_locale);
  //    for (int i=0,n=node.getChildCount(); i<n; i++)
  //      traverseRemoveTranslation ((DefaultMutableTreeNode) node.getChildAt (i),_locale);
  //  }

  //--------------------- private Node methods -------------------

  private void traverseSearch (DefaultMutableTreeNode node, java.util.List<SearchResult> list, String _info, String _searchString, int _mode) {
    list.addAll(viewOf(node).search(_info, _searchString, _mode));
    for (int i=0,n=node.getChildCount(); i<n; i++)
      traverseSearch ((DefaultMutableTreeNode) node.getChildAt (i),list, _info, _searchString, _mode);
  }


  private DefaultMutableTreeNode traverseFind (DefaultMutableTreeNode node, String name) {
    if (name.equals(node.toString())) return node;
    for (int i=0,n=node.getChildCount(); i<n; i++) {
      DefaultMutableTreeNode found = traverseFind ((DefaultMutableTreeNode) node.getChildAt (i),name);
      if (found!=null) return found;
    }
    return null;
  }

  /*
  private DefaultMutableTreeNode traverseFindDrawable(DefaultMutableTreeNode node, Drawable _drawable) {
    if (viewOf(node).getElement() instanceof ControlDrawable) {
      if (((ControlDrawable) viewOf(node).getElement()).getDrawable()==_drawable) return node;
    }
    for (int i=0; i<node.getChildCount(); i++) {
      DefaultMutableTreeNode found = traverseFindDrawable ((DefaultMutableTreeNode) node.getChildAt (i),_drawable);
      if (found!=null) return found;
    }
    return null;
  }
   */
  private void traverseClear (DefaultMutableTreeNode _node) {
    viewOf(_node).clear();
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseClear ((DefaultMutableTreeNode)_node.getChildAt(i));
  }

  private void traverseUpdateProperties (DefaultMutableTreeNode _node, boolean showErrors) {
    viewOf(_node).updateProperties(showErrors);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseUpdateProperties ((DefaultMutableTreeNode)_node.getChildAt(i),showErrors);
  }

  //  private void traverseShowDeprecated (DefaultMutableTreeNode _node) {
  //    viewOf(_node).showDeprecated();
  //    for (int i=0,n=_node.getChildCount(); i<n; i++)
  //      traverseShowDeprecated ((DefaultMutableTreeNode)_node.getChildAt(i));
  //  }

  private void traverseListNodes(DefaultMutableTreeNode _node, java.util.List<ViewElement> _list) {
    _list.add(viewOf(_node));
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseListNodes ((DefaultMutableTreeNode)_node.getChildAt(i),_list);
  }

  private boolean traverseHasChanged (DefaultMutableTreeNode _node) {
    if (viewOf(_node).hasChanged()) return true;
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      if (traverseHasChanged((DefaultMutableTreeNode)_node.getChildAt(i))) return true;
    return false;
  }

  private void traverseSetChanged (DefaultMutableTreeNode _node, boolean _changed) {
    viewOf(_node).setChanged(_changed);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseSetChanged ((DefaultMutableTreeNode)_node.getChildAt(i),_changed);
  }

  private void traverseSetFont (DefaultMutableTreeNode _node, Font _font) {
    if (_node!=rootNode) viewOf(_node).setFont (_font);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseSetFont ((DefaultMutableTreeNode) _node.getChildAt (i),_font);
  }

  private void traverseSetCursor (DefaultMutableTreeNode _node, Cursor _cursor) {
    if (_node!=rootNode) viewOf(_node).setCursor(_cursor);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseSetCursor ((DefaultMutableTreeNode) _node.getChildAt (i),_cursor);
  }

  private StringBuffer traverseSaveStringBuffer (DefaultMutableTreeNode _node, String _blockName) {
    StringBuffer txt = new StringBuffer();
    String prefix=null;
    if (_node!=rootNode) {
      //      if (tree.isVisible(new TreePath(_node.getPath()))) prefix = "<Visible>true</Visible>\n";
      //      else prefix = "<Visible>false</Visible>\n";
      if (_node.getChildCount()>0) {
        if (tree.isCollapsed(new TreePath(_node.getPath()))) prefix = "<Expanded>false</Expanded>\n";
        else prefix = "<Expanded>true</Expanded>\n";
      }
      txt.append("<"+_blockName+".Element>\n");
      txt.append(viewOf(_node).saveStringBuffer(prefix));
      txt.append("</"+_blockName+".Element>\n");
    }
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      txt.append(traverseSaveStringBuffer ((DefaultMutableTreeNode) _node.getChildAt (i),_blockName));
    return txt;
  }

  private StringBuffer traverseGenerateCode (DefaultMutableTreeNode _node, int _type) {
    codeCounter++;
    StringBuffer txt = new StringBuffer();
    if ((_type==Editor.GENERATE_CODE || _type==Editor.GENERATE_SERVER_CODE || _type==Editor.GENERATE_SERVER_DUMMY_CODE) && codeCounter%50==0) {
      txt.append("    createControl"+codeCounter+"();\n");
      txt.append("  }\n\n");
      txt.append("  private void createControl"+codeCounter+"() {\n");
    }
    if (_node!=rootNode) txt.append(viewOf(_node).generateCode(_type));
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      txt.append(traverseGenerateCode ((DefaultMutableTreeNode) _node.getChildAt (i),_type));
    return txt;
  }

  // -------------- Other private methods --------------

  private void createMenu() {
    popup = new JPopupMenu ();
    reducedPopup = new JPopupMenu ();
    ActionListener al = new ActionListener() {
      public void actionPerformed (ActionEvent _e) { myActionPerformed (_e); }
    };
    topLabel = new JLabel (res.getString("Tree.MenuFor"),SwingConstants.CENTER);
    topLabel.setBorder(new EmptyBorder(0,5,0,0));
    //    topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD));
    popup.insert (topLabel,0);
    createMenuItem (popup,"Edit",al);
    renameMi      = createMenuItem (popup,"Rename",al);
    reparentMi    = createMenuItem (popup,"Reparent",al);
    mainWindowMi  = createCheckBoxMenuItem (popup,"MainWindow",al);
    popup.addSeparator();
    positionLabel = new JLabel ("",SwingConstants.CENTER);
    positionLabel.setBorder(new EmptyBorder(0,5,0,0));
    //    positionLabel.setFont(positionLabel.getFont().deriveFont(Font.BOLD));
    popup.insert (positionLabel,6);
    positionMi    = createMenuItem (popup,"ChangePosition",al);
    upMi          = createMenuItem (popup,"MoveUp",al);
    downMi        = createMenuItem (popup,"MoveDown",al);
    popup.addSeparator();
    //    popup.insert (new JLabel (res.getString("Tree.Utils"),SwingConstants.CENTER),11);

    // Cut and Paste Added Nov 2004
    cutMi = new JMenuItem (res.getString("Utils.cut-to-clipboard"));
    cutMi.setActionCommand("Cut"); cutMi.addActionListener(al); popup.add(cutMi);

    copyMi = new JMenuItem (res.getString("Utils.copy-to-clipboard"));
    copyMi.setActionCommand("Copy"); copyMi.addActionListener(al); popup.add(copyMi);

    pasteMi = new JMenuItem (res.getString("Utils.paste-from-clipboard"));
    pasteMi.setActionCommand("Paste"); pasteMi.addActionListener(al); popup.add(pasteMi);

    removeMi      = createMenuItem (popup,"Remove",al);

    popup.addSeparator();

    helpMi = new JMenuItem (res.getString("Help.Help"));
    helpMi.setActionCommand("Help"); helpMi.addActionListener(al); popup.add(helpMi);

    popup.validate();
    //    popup.setVisible (true);
    //    popup.setVisible (false);

    JMenuItem reducedPasteMi = new JMenuItem (res.getString("Utils.paste-from-clipboard"));
    reducedPasteMi.setActionCommand("Paste"); reducedPasteMi.addActionListener(al);
    reducedPopup.add(reducedPasteMi);

    reducedPopup.addSeparator();

    JMenuItem reducedHelpMi = new JMenuItem (res.getString("Help.Help"));
    reducedHelpMi.setActionCommand("Help"); reducedHelpMi.addActionListener(al);
    reducedPopup.add(reducedHelpMi);

    reducedPopup.validate();
  }

  private JMenuItem createMenuItem (JPopupMenu _popup, String _key, ActionListener _al) {
    JMenuItem mi = new JMenuItem (res.getString("Tree."+_key));
    mi.setActionCommand (_key);
    mi.addActionListener (_al);
    _popup.add (mi);
    return mi;
  }
  private JCheckBoxMenuItem createCheckBoxMenuItem (JPopupMenu _popup, String _key, ActionListener _al) {
    JCheckBoxMenuItem mi = new JCheckBoxMenuItem (res.getString("Tree."+_key));
    mi.setActionCommand (_key);
    mi.addActionListener (_al);
    _popup.add (mi);
    return mi;
  }

  //  private String stringReplace (String str, String word1, String word2) {
  //    StringTokenizer words = new StringTokenizer(str.trim()," "); // \t\n\r\f ",true);
  //    String word = "", newStr="";
  //    while (words.hasMoreTokens()) {
  //      word = words.nextToken();
  //      if (word.equals(word1)) newStr += word2 + " ";
  //      else newStr += word + " ";
  //    }
  //    return newStr;
  //  }

  private void myActionPerformed (ActionEvent evt) {
    if (currentNode==null) return;
    String cmd=evt.getActionCommand();
    if (currentNode==rootNode) {
      if (cmd.equals("Paste")) paste (currentNode);
      else if (cmd.equals("Help")) ejs.openWikiPage("View"); 
      else JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.MainDoesNotAcceptThis"),
          res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (cmd.equals("Edit")) { viewOf(currentNode).edit (false); return; }
    if (cmd.equals("Rename")) rename (currentNode);
    else if (cmd.equals("Reparent")) {
      String txt = JOptionPane.showInputDialog(getComponent(),res.getString("Tree.ReparentTo"),
          res.getString("Tree.ReparentTitle"),JOptionPane.QUESTION_MESSAGE);
      if (txt!=null && txt.trim().length()>0) reparent (currentNode,txt.trim());
    }
    else if (cmd.equals("ChangePosition")) reposition (currentNode);
    else if (cmd.equals("MoveUp"))   moveUpAndDown(currentNode,true);
    else if (cmd.equals("MoveDown")) moveUpAndDown(currentNode,false);
    else if (cmd.equals("MainWindow")) setMainWindow (currentNode);
    else if (cmd.equals("Remove")) remove (currentNode);
    else if (cmd.equals("Cut")) { copy (currentNode); remove (currentNode); }
    else if (cmd.equals("Copy")) copy (currentNode);
    else if (cmd.equals("Paste")) paste (currentNode);
    else if (cmd.equals("Help")) ejs.openWikiPage(viewOf(currentNode).getClassname());
  }

  // --------------------- Inner classes -------------------

  class ElementListener extends MouseAdapter {
    DefaultMutableTreeNode node;
    ElementListener (DefaultMutableTreeNode _node) { node = _node; }
    public void mousePressed (MouseEvent _e) {
      DefaultMutableTreeNode ienode=null;
      if (viewOf(node).getElement() instanceof ControlDrawablesParent) {
        ControlDrawable ie = ((ControlDrawablesParent) viewOf(node).getElement()).getSelectedDrawable();
        if (ie!=null) {
          ienode = findNode (ie.getProperty("name"));
          //          System.out.println ("Found an IE = "+ie.toString());
        }
      }
      if (ienode!=null) tree.setSelectionPath(new TreePath(ienode.getPath()));
      else tree.setSelectionPath(new TreePath(node.getPath()));
      if (OSPRuntime.isPopupTrigger(_e)) { //SwingUtilities.isRightMouseButton(_e)) {
        if (ienode!=null) {
          viewOf(ienode).edit(((ControlDrawable)viewOf(ienode).getElement()).getParent().getDrawingPanel());
        }
        else viewOf(node).edit(true);
        creationPanel.clear();
      }
      else if ((_e.getModifiers() | InputEvent.BUTTON1_MASK) != 0) {
        if (_e.getClickCount()==1) {
          if (creationPanel.getClassToCreate()!=null) {
            boolean discardAfterCreation = !_e.isAltDown();
            add (node,creationPanel.getClassToCreate(),getUniqueName(creationPanel.getNameToCreate()),viewOf(node).getElement().getVisual(),-1);
            if (discardAfterCreation) creationPanel.clear(); // disable the magic wand after creation
          }
        }
      }
    }
  }

  class TreeMouseListener extends MouseAdapter {
    public void mousePressed (MouseEvent _e) {
      TreePath selPath = tree.getPathForLocation(_e.getX(), _e.getY());
      if (selPath==null) {
        currentNode = null;
        creationPanel.clear();
        return;
      }
      currentNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();

      if (OSPRuntime.isPopupTrigger(_e)) { //SwingUtilities.isRightMouseButton(_e)) {
        creationPanel.clear();
        if (currentNode==rootNode) {
          reducedPopup.show (_e.getComponent(),35, _e.getY());
          return;
        }
        positionLabel.setText ("");
        mainWindowMi.setSelected(currentNode==mainWindowNode);
        mainWindowMi.setVisible(viewOf(currentNode).isFrame());
        if (viewOf(currentNode).isWindow()) {
          reparentMi.setEnabled (false);
          positionMi.setEnabled (false);
          positionLabel.setText (res.getString("Tree.IsWindow"));
        }
        else {
          reparentMi.setEnabled (true);
          positionMi.setEnabled (false);
          positionLabel.setText (res.getString("Tree.PositionImposed"));
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
          if (parent==null) { } // leave it as it is
          else if ((viewOf(parent).getElement() instanceof ControlWindow) && viewOf(currentNode).getElement().getObject() instanceof JMenuBar) { } // leave it as it is
          else if (viewOf(parent).hasBorderLayout()) {
            positionLabel.setText (res.getString("Tree.HasPosition")+" "+translatePosition(viewOf(currentNode).getPosition()));
            positionMi.setEnabled (true);
          }
        }
        topLabel.setText (res.getString("Tree.MenuFor")+" "+currentNode);
        if (!editable) {
          renameMi.setEnabled(false);   reparentMi.setEnabled(false);
          positionMi.setEnabled(false); upMi.setEnabled(false);
          downMi.setEnabled(false);     mainWindowMi.setEnabled(false);
          removeMi.setEnabled(false);
          cutMi.setEnabled(false); copyMi.setEnabled(false); pasteMi.setEnabled(false);
        }
        //        creationPanel.clear();
        popup.show (_e.getComponent(),35, _e.getY());
      }
      else if ((_e.getModifiers() | InputEvent.BUTTON1_MASK) != 0) {
        if (_e.getClickCount()>1) {
          creationPanel.clear();
          viewOf (currentNode).edit(false);
        }
        else if (creationPanel.getClassToCreate()!=null) {
          boolean discardAfterCreation = !_e.isAltDown();
          add (currentNode,creationPanel.getClassToCreate(),getUniqueName(creationPanel.getNameToCreate()),getComponent(),-1);
          if (discardAfterCreation) creationPanel.clear(); // disable the magic wand after creation
        }
      }
    }
  }

  //  class TSL implements TreeSelectionListener {
  //    public void valueChanged (TreeSelectionEvent _tse) {
  //      System.out.println ("Value changed in Tree");
  //      TreePath currentSelection = tree.getSelectionPath();
  //      if (currentSelection==null) {
  //        currentNode = null;
  //        creationPanel.clear();
  //      }
  //      else {
  //        currentNode = (DefaultMutableTreeNode) currentSelection.getLastPathComponent();
  //        if (creationPanel.classToCreate!=null) {
  //          System.out.println ("Create class "+creationPanel.classToCreate+" in parent "+currentNode);
  //          creationPanel.clear();
  //        }
  //      }
  //    }
  //  }

  //  class MyTreeModelListener implements TreeModelListener {
  //    public void treeNodesChanged(TreeModelEvent _e) { changed = true; }
  //    public void treeNodesInserted(TreeModelEvent _e) { changed = true; }
  //    public void treeNodesRemoved(TreeModelEvent _e)  { changed = true; }
  //    public void treeStructureChanged(TreeModelEvent _e) { changed = true; }
  //  }

  class MyRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;
    Icon containerIcon;
    public MyRenderer() { containerIcon = this.getClosedIcon(); }
    public Component getTreeCellRendererComponent (JTree _tree,Object _value,
        boolean _sel, boolean _expanded, boolean _leaf, int _row, boolean _hasFocus) {
      super.getTreeCellRendererComponent(_tree, _value, _sel, _expanded, _leaf, _row, _hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) _value;
      String classname = viewOf(node).getClassname();
      Icon icon;
      if (node==rootNode) icon = rootIcon;
      else if (viewOf(node).getElement().getProperty("_ejs_mainWindow")!=null) icon = captureIcon;
      else icon = CreationPanelRow.getElementIcon(classname);
      if (icon!=null) setIcon (icon);
      else {
        //        System.out.println ("icon null for  "+classname);
        if (viewOf(node).isContainer()) setIcon(containerIcon);
      }
      return this;
    }
  }

  class MyTreeExpansionListener implements TreeExpansionListener {
    public void treeCollapsed(TreeExpansionEvent _event) {
      DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) _event.getPath().getPathComponent(_event.getPath().getPathCount()-1);
      if (node==rootNode) {
        tree.expandRow(0);
      }
      if (viewOf(node).getElement() instanceof ControlWindow)  ((ControlWindow)  viewOf(node).getElement()).hide();

    }
    public void treeExpanded(TreeExpansionEvent _event) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) _event.getPath().getPathComponent(_event.getPath().getPathCount()-1);
      if (viewOf(node).getElement() instanceof ControlWindow)  ((ControlWindow)  viewOf(node).getElement()).show();
    }

  }

} // end of class
