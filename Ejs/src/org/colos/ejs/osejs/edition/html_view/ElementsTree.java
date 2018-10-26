/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.html_view;

import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.view.TreeOfElements;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.edition.translation.TranslatableProperty;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.display.OSPRuntime;
import org.w3c.dom.Element;

import java.awt.datatransfer.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.tree.*;
import javax.swing.border.*;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejss.xml.SimulationXML;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.io.*;

//--------------------

public class ElementsTree {//implements InteractiveMouseHandler {
  static private ResourceUtil res    = new ResourceUtil ("Resources");

  private boolean changed=false, editable=true;
  private Palette mPalette;
  private DefaultMutableTreeNode rootNode, currentNode=null;
  private OneView mHtmlView;

  private JPanel mainPanel;
  private JPopupMenu popup,reducedPopup;
  private JLabel topLabel;
  private JMenuItem reparentMi, renameMi, upMi, downMi, removeMi , copyMi, cutMi, pasteMi;
  private TitledBorder titleBorder;
  private JTree tree;
  private DefaultTreeModel treeModel;

  private Hashtable<String, String> pasteList=null;
  private Font myFont=InterfaceUtils.font(null,res.getString("Osejs.DefaultFont"));

  public ElementsTree (OneView htmlView, Palette palette) {
    mHtmlView = htmlView;
    mPalette = palette;
    mPalette.setTree(this);
    createMenu();

    rootNode  = createNewNode ("Tree.Main",res.getString("Tree.Main"));
    treeModel = new DefaultTreeModel (rootNode);
    tree      = new JTree(treeModel);
    tree.setEditable(false);
    tree.setFont(myFont);
    tree.setRootVisible(true);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.putClientProperty("JTree.lineStyle", "Angled");
    tree.setCellRenderer(new MyRenderer());
    tree.setSelectionRow (0);
    tree.setToggleClickCount(3);
    tree.addMouseListener (new TreeMouseListener());
    
    tree.setDragEnabled(true);
    tree.setDropMode(DropMode.ON_OR_INSERT);
    tree.setTransferHandler(new ElementTreeTransferHandler(this));

    tree.addKeyListener(
      new java.awt.event.KeyAdapter() {
        public void keyPressed (java.awt.event.KeyEvent _e) {
          if (_e.getKeyCode()==KeyEvent.VK_F1) { 
            if (currentNode==null || currentNode==rootNode) mHtmlView.getEjs().openWikiPage("HtmlView");
            else showLocalHelp(viewOf(currentNode).getClassname());
          }
        }
      }
    );
    tree.addMouseMotionListener(
      new java.awt.event.MouseMotionAdapter() {
        public void mouseMoved (java.awt.event.MouseEvent _e) { 
          if (mPalette.hasIconSelected()) mPalette.requestFocus(); }
      }
    );
    JScrollPane scrollPanel = new JScrollPane(tree);
    
    titleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),res.getString("Tree.TreeOfElements"));
    titleBorder.setTitleJustification (TitledBorder.LEFT);
    titleBorder.setTitleFont (InterfaceUtils.font(null,res.getString("Editor.TitleFont")));

    mainPanel = new JPanel (new BorderLayout ());
    mainPanel.add(scrollPanel,BorderLayout.CENTER);
    mainPanel.setBorder (titleBorder);
    mainPanel.setPreferredSize(new Dimension(200,300)); // Any size - not too big - will do
    clear();
  }

  Osejs getEjs() { return mHtmlView.getEjs(); }
  
//  JTree getTree() { return tree; }
    
  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) { titleBorder.setTitleColor (_color); }

  public void setFont (Font _font) {
    myFont = _font;
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

  public void updateProperties(boolean showErrors) {
    traverseUpdateProperties(rootNode,showErrors);
//    for (int i=0; i<rootNode.getChildCount(); i++) 
//      traverseUpdateProperties ((DefaultMutableTreeNode) rootNode.getChildAt(i),showErrors);
  }

  public void clear() {
    traverseClear (rootNode);
    rootNode.removeAllChildren();
    treeModel.reload();
    tree.setSelectionRow (0);
    currentNode = rootNode;
    refreshEmulator();
    setChanged(false);
  }

  public void setEditable (boolean _edit) {
    editable = _edit;
    mPalette.setEditable (_edit);
  }

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    traverseSearch (rootNode,list,_info,_searchString,_mode);
//    for (int i=0; i<rootNode.getChildCount(); i++)
//      traverseSearch ((DefaultMutableTreeNode) rootNode.getChildAt(i),list,_info,_searchString,_mode);
    return list;
  }

  public void showLocalHelp(String classname) {
    int index = classname.lastIndexOf('.');
    if (index>0) classname = classname.substring(index+1);
    mPalette.showHtmlFile(classname,null);
  }
  
  void refreshEmulator() {
    mHtmlView.refreshEmulator();
  }
//    if (mHtmlView.getEmulator()==null) return;
//    File currentFile = getEjs().getCurrentXMLFile();
//    SimulationXML simulation = new SimulationXML("TestSimulation");
//    getEjs().getSimInfoEditor().fillXMLSimulation(simulation);
//    String name = FileUtils.getPlainNameAndExtension(currentFile).getFirstString();
//    simulation.setInformation(INFORMATION.TITLE, name+" ("+mHtmlView.getName()+")");
//    if (getEjs().supportsJava()) simulation.setViewOnly(null);
//    else getEjs().getModelEditor().fillSimulationXMLForHtmlView(simulation);
//    Element view = simulation.addView(mHtmlView.getName(), null,null);
//    fillSimulationXML(simulation,view);
//    
//    File ejslibraryCssDir=null;
//    String cssPath = getEjs().getSimInfoEditor().getCSSFolder();
//    if (cssPath.length()>0) ejslibraryCssDir = new File(getEjs().getCurrentDirectory(),cssPath);
//    if (ejslibraryCssDir==null || !ejslibraryCssDir.exists()) ejslibraryCssDir = new File(getEjs().getOutputDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH+"/css");
//    File javascriptDir = new File(mHtmlView.getEjs().getBinDirectory(),"javascript/lib");
//    cssPath = FileUtils.getPath(ejslibraryCssDir);
//    String libPath = FileUtils.getPath(javascriptDir);
//    String htmlPath = FileUtils.getPath(currentFile.getParentFile());
//    mHtmlView.getEmulator().setSimulationPaths(cssPath,libPath, htmlPath);
//    LocaleItem locale = mHtmlView.getLocale();
//    if (locale.isDefaultItem()) mHtmlView.getEmulator().setSimulation(getEjs(), simulation,"HtmlView",SimulationXML.sDEFAULT_LOCALE);
//    else mHtmlView.getEmulator().setSimulation(getEjs(),simulation,"HtmlView",locale.getKeyword());
  
  void fillSimulationXML(SimulationXML _simulation, Element _view) {
    traversefillSimulationXML(rootNode,_simulation,_view);
  }

  void generateCode(StringBuffer _buffer, int _type) {
    traverseGenerateCode(rootNode,_buffer,_type);
  }

  public Set<String> getResourcesNeeded(String _keyword) {
    Set<String> set = new java.util.HashSet<String>();
    traverseResourcesNeeded(rootNode,set, _keyword);
    return set;
  }

  public StringBuffer saveRootProperties() { return viewOf(rootNode).saveStringBuffer(null); }

  public StringBuffer saveStringBuffer (String _blockName) { return traverseSaveStringBuffer(rootNode,_blockName); }
  
  public boolean parentAcceptsChild (DefaultMutableTreeNode _parent, DefaultMutableTreeNode _child) {
    return (viewOf(_parent).acceptsChild(viewOf(_child)));
  }

  public boolean parentAcceptsChild (DefaultMutableTreeNode _parent, String _childClass) {
    if (!_childClass.startsWith("Elements.")) _childClass = "Elements."+_childClass;
    if (_childClass.startsWith("Elements.UserDefined.")) return true;
    //System.out.println ("Creating dummy editor for "+_childClass);
    ElementEditor dummy = new ElementEditor(_childClass);
    return (viewOf(_parent).acceptsChild(dummy));
  }

  /**
   * reads an input string
   * @param _input
   * @param _refPosition
   * @return the first child created
   */
  public DefaultMutableTreeNode readString (String _input, int _refPosition) {
//    System.out.println ("Reading with blockname "+_blockName+"\n"+_input);
    int begin = _input.indexOf("<HtmlView.Element>");
    int length = "<HtmlView.Element>".length();
    Vector<TreePath> expandedList = new Vector<TreePath>();
    DefaultMutableTreeNode firstChild=null;
    while (begin>=0) {
      DefaultMutableTreeNode parent=null,child;
      int end = _input.indexOf("</HtmlView.Element>");
      String piece = _input.substring(begin+length,end).trim();
      String classname  = OsejsCommon.getPiece(piece,"<Type>","</Type>",false);
//      System.err.println("Reading "+classname);
      String name       = OsejsCommon.getPiece(piece,"<Name><![CDATA[","]]></Name>",false);
      String parentName = OsejsCommon.getPiece(piece,"<Parent><![CDATA[","]]></Parent>",false);
      String expanded   = OsejsCommon.getPiece(piece,"<Expanded>","</Expanded>",false);
      if (pasteList!=null) {  // Pasting: Check for new names
        // Change parent, if necessary
//        System.err.println ("Parent name = "+parentName);
        String newNameForParent;
        if (parentName==null) newNameForParent = pasteList.get(rootNode.toString());
        else newNameForParent = pasteList.get(parentName);
//        System.err.println ("New name for Parent = "+newNameForParent);
        if (newNameForParent != null) parentName = newNameForParent;
        // Change name, if necessary
        if (nameExists(name)) {
          String uniqueName = getUniqueName(name);
          pasteList.put(name,uniqueName);
          name = uniqueName;
        }
      }
      if (parentName!=null) parent = findNode(parentName);
      if (parent==null) parent = rootNode;
      child = createNewNode (classname,name);
      viewOf(child).setEditable(editable);
      if (firstChild==null) firstChild = child;

      int _position = parent.getChildCount();
      if (_refPosition>=0) {
        _position = _refPosition;
        _refPosition = -1; // only the first time
      }
      if (pasteList!=null) { // Check validity of parent and child
//        if (parent==rootNode) {
//          JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
//          return;
//        }
        if (!viewOf(parent).acceptsChild(viewOf(child))) {
          DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) parent.getParent();
          if (grandParent==null || viewOf(grandParent)==null) return null;
          if (viewOf(grandParent).acceptsChild(viewOf(child))) {
            _position = grandParent.getIndex(parent);
            parent = grandParent;
//            System.out.println ("Add "+child +" to "+parent+ " at pos ="+position);
          }
          else {
            JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
            refreshEmulator();
            return null;
          }
        }
      }
      treeModel.insertNodeInto (child, parent, _position);

      viewOf(child).setParent(viewOf(parent));

      // Now process the properties
      viewOf(child).readString(piece);
      if (expanded!=null) {
        if (expanded.startsWith("true")) expandedList.add(new TreePath(child.getPath()));
      }
      // Go to next element
      _input = _input.substring(end+length+1).trim(); // +1 due to the extra /
      begin = _input.indexOf("<HtmlView.Element>\n");
    }

    int size = expandedList.size();
    for (int i=0; i<size; i++) {
      TreePath path = expandedList.elementAt(size-i-1);
      tree.expandPath(path);
    }
    tree.expandPath(new TreePath(rootNode.getPath()));
//    System.out.println ("Control update after reading");
    refreshEmulator();
    setChanged(false);
    return firstChild;
  }

  public void readRootProperties(String _input) { if (_input!=null) viewOf(rootNode).readString(_input); }

//  private void add (String _classname, String _name) { add (currentNode,_classname,_name,getComponent()); }

  private void readCompound (DefaultMutableTreeNode _parent, String _classname) {
    String filename = OsejsCommon.CUSTOM_HTML_ELEMENTS_DIR_PATH+"/"+_classname.substring(_classname.lastIndexOf('.')+1)+".xml";
    File file = new File(mHtmlView.getEjs().getConfigDirectory(),filename);
    if (!file.exists()) file = new File(mHtmlView.getEjs().getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/"+filename);
    String input = FileUtils.readTextFile(file,null);
    if (input==null) {
      JOptionPane.showMessageDialog(null, res.getString("Osejs.File.ReadError")+" "+filename,
          res.getString("Osejs.File.ReadingError"),JOptionPane.INFORMATION_MESSAGE);
      return;
    }      
    String fragment = OsejsCommon.getPiece(input,"<HtmlViewFragment>","</HtmlViewFragment>",true);
    if (fragment!=null) {
      pasteString(_parent,fragment,-1);
      return;
    }
    mHtmlView.getEjs().readString((File) null,input,true);
  }

  private void add (DefaultMutableTreeNode _parent, String _classname, String _name, Component _component, int _position) {
    if (!_classname.startsWith("Elements.")) _classname = "Elements."+_classname;
    if (_classname.startsWith("Elements.UserDefined.")) {
      readCompound (_parent, _classname);
      return;
    }
    int pos = _position;
    if (_parent==null) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.ProvideAParent"),res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
//    System.out.println ("Add "+_name +" of class = "+_classname +" to "+_parent.toString());
    _name = (String) JOptionPane.showInputDialog(_component,res.getString("Tree.ProvideAName"),res.getString("Tree.NameTitle"),JOptionPane.QUESTION_MESSAGE,null,null,_name);
    if (_name==null || _name.trim().length()<=0) return;
    _name = getUniqueName(_name);
    DefaultMutableTreeNode node = createNewNode (_classname,_name.trim());
    if (node==null) {
      JOptionPane.showMessageDialog(_component,res.getString("Tree.CantCreateElement")+_classname,res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    viewOf(node).setEditable(editable);
    viewOf(node).setTextProperty();
    viewOf(node).setCreationDefaults();

    if (!viewOf(_parent).acceptsChild(viewOf(node))) { // && _parent!=rootNode) {
      boolean refused = true;
      if (_parent!=rootNode) {
        DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) _parent.getParent();
        if (viewOf(grandParent).acceptsChild(viewOf(node))) {
          pos = grandParent.getIndex(_parent);
          _parent = grandParent;
//          System.out.println ("Add "+node +" to "+_parent+ " at pos ="+pos);
          refused = false;
        }
      }
      if (refused) {
        JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.ParentDoesNotAcceptThis"),res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
    addNode (node,_parent,_component, pos);  
  }

  public DefaultMutableTreeNode findNode (String _name) {
    if (_name==null) return null;
    return traverseFind (rootNode,_name);
  }

  public void setCursor (Cursor _cursor) {
    mainPanel.setCursor (_cursor);
  }


  // --------- Private methods for operation

  public ElementEditor viewOf(DefaultMutableTreeNode _node) { return (ElementEditor) (_node.getUserObject()); }

  private DefaultMutableTreeNode createNewNode (String _classname, String _name) {
    DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(new ElementEditor(mHtmlView.getEjs(),_classname,_name,this));
    viewOf(newnode).setFont(myFont);
    return newnode;
  }

  private void addNode (DefaultMutableTreeNode _child, DefaultMutableTreeNode _parent, Component _component, int _position) {
//   System.out.println ("Adding "+_child +" to "+_parent);
    if (_position<0) _position = _parent.getChildCount();
    treeModel.insertNodeInto (_child, _parent, _position);
    viewOf(_child).setParent(viewOf(_parent));
    tree.scrollPathToVisible(new TreePath(_child.getPath()));
    refreshEmulator();
    changed = true;
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
      viewOf((DefaultMutableTreeNode) _node.getChildAt(i)).setParent(viewOf(_node));
    }
    TreePath path = new TreePath(_node.getPath());
    treeModel.valueForPathChanged (path,viewOf(_node));
    getEjs().getTranslationEditor().refresh();
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
    addNode (_child,parent,getComponent(),-1);
  }

  private void moveUpAndDown (DefaultMutableTreeNode _child, boolean _up) {
    if (_child==null) return;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _child.getParent();
    if (parent==null) return;
    int pos = treeModel.getIndexOfChild (parent, _child);
    if (_up) { if (pos<=0) return; pos--; }
    else { if (pos>=(treeModel.getChildCount (parent)-1)) return;  pos++; }
    moveTo (_child,parent,pos);
    TreePath path = new TreePath(_child.getPath());
    tree.setSelectionPath(path);
  }

  void moveTo (DefaultMutableTreeNode _child, DefaultMutableTreeNode _parent, int _position) {
    boolean wasExpanded = !tree.isCollapsed(new TreePath(_child.getPath()));
    treeModel.removeNodeFromParent (_child);
    if (_position<0 || _position>_parent.getChildCount()) _position = _parent.getChildCount();
    treeModel.insertNodeInto (_child, _parent, _position);
    viewOf(_child).setParent(viewOf(_parent));
    if (wasExpanded) tree.expandPath(new TreePath(_child.getPath()));
    refreshEmulator();
    changed = true;
  }

  void remove(DefaultMutableTreeNode _child) {
    if (_child==null) return;
//    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) _child.getParent();
    treeModel.removeNodeFromParent(_child);
    currentNode = null;
    this.traverseClear(_child);
//    viewOf(parent).remove(viewOf(_child));
    getEjs().getTranslationEditor().refresh();
    refreshEmulator();
    changed = true;
  }

  Transferable createTransferable(DefaultMutableTreeNode _node) {
    // clipboardParent = _node.getParent().toString();
    StringBuffer text = traverseSaveStringBuffer(_node, "HtmlView");
    StringBuffer fragment = new StringBuffer();
    fragment.append("<HtmlViewFragment>\n");
    fragment.append("  <HtmlViewFragment.Parent>"+_node.getParent().toString() +"</HtmlViewFragment.Parent>\n");
    fragment.append("  <HtmlViewFragment.Code>\n");
    fragment.append(text);
    fragment.append("  </HtmlViewFragment.Code>\n");
    fragment.append("</HtmlViewFragment>\n");
    return new Selection(fragment.toString());
  }

  
  private void copy (DefaultMutableTreeNode _node) {
    Transferable selection = createTransferable(_node);
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    theClipboard.setContents(selection,null);
    OSPLog.finest("Copying:");
    OSPLog.finest("  - Flavor:"+Selection.viewFlavor.toString());
    try { OSPLog.finest("  - Contents follow -:\n"+selection.getTransferData(Selection.viewFlavor)); }
    catch (Exception exc) { exc.printStackTrace(); };
    OSPLog.finest("  - End of contents -");
  }

  boolean isFragment(String _code) {
    String parent = OsejsCommon.getPiece(_code,"<HtmlViewFragment.Parent>","</HtmlViewFragment.Parent>",false);
    return parent!=null;
  }
  
  void paste (final DefaultMutableTreeNode _node, Transferable _transferable, final int _position) {
//    System.out.println("Pasting to :"+_node +" at position : "+_position);
    OSPLog.finest("Pasting to :"+_node +" at position : "+_position);
    OSPLog.finest("  - Flavor: "+Selection.viewFlavor.toString());
    OSPLog.finest("  - Flavor supported: "+_transferable.isDataFlavorSupported(Selection.viewFlavor));
    if (_transferable.isDataFlavorSupported(Selection.viewFlavor)) {
      try { 
        final String code = (String) _transferable.getTransferData(Selection.viewFlavor);
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
              add (_node,code,getUniqueName(mPalette.getNameToCreate()),getComponent(),_position);
            }
          });
        }
        return;
      }
      catch (Exception exc) { exc.printStackTrace(); };
    }
  }
  
  private void paste (DefaultMutableTreeNode _node) {
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    paste (_node,theClipboard.getContents(null),-1);
  }

  private DefaultMutableTreeNode pasteString (DefaultMutableTreeNode _node, String _text, int _position) {
    if (_text==null) return null;
    String parent = OsejsCommon.getPiece(_text,"<HtmlViewFragment.Parent>","</HtmlViewFragment.Parent>",false);
    String code   = OsejsCommon.getPiece(_text,"<HtmlViewFragment.Code>","</HtmlViewFragment.Code>",false).trim();
//        System.err.println("Paste Parent = "+parent);
//        System.err.println("Code = "+code);
    pasteList = new Hashtable<String, String>();
    pasteList.put(parent,_node.toString());
    DefaultMutableTreeNode child = readString (code,_position);
    pasteList = null;
    if (child!=null) {
      changed = true;
      getEjs().getModelEditor().getVariablesEditor().updateControlValues(false);
      getEjs().getTranslationEditor().refresh();
      refreshEmulator();
    }
    return child; 
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

  // ----------------------------------------
  // Translation
  // ----------------------------------------
  
  public java.util.List<TranslatableProperty> getTranslatableProperties() {
    java.util.List<TranslatableProperty> list = new ArrayList<TranslatableProperty>();
    traverseGetTranslatableProperties (rootNode,list);
//    for (int i=0; i<rootNode.getChildCount(); i++)
//      traverseGetTranslatableProperties ((DefaultMutableTreeNode) rootNode.getChildAt(i),list);
    return list;
  }
  
  private void traverseGetTranslatableProperties (DefaultMutableTreeNode node, java.util.List<TranslatableProperty> list) {
    list.addAll(viewOf(node).getTranslatableProperties());
    for (int i=0,n=node.getChildCount(); i<n; i++)
      traverseGetTranslatableProperties ((DefaultMutableTreeNode) node.getChildAt (i),list);
  }

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

//  private void traverseListNodes(DefaultMutableTreeNode _node, java.util.List<HtmlViewElement> _list) {
//    _list.add(viewOf(_node));
//    for (int i=0,n=_node.getChildCount(); i<n; i++)
//      traverseListNodes ((DefaultMutableTreeNode)_node.getChildAt(i),_list);
//  }

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
    //if (_node!=rootNode) 
      viewOf(_node).setFont (_font);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseSetFont ((DefaultMutableTreeNode) _node.getChildAt (i),_font);
  }

  private StringBuffer traverseSaveStringBuffer (DefaultMutableTreeNode _node, String _blockName) {
    StringBuffer txt = new StringBuffer();
    String prefix=null;
    if (_node!=rootNode) {
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

  private void traversefillSimulationXML(DefaultMutableTreeNode _node, SimulationXML simulation, Element view) {
    //if (_node!=rootNode) 
    viewOf(_node).fillSimulationXML (simulation,view);
    for (int i=0,n=_node.getChildCount(); i<n; i++) 
      traversefillSimulationXML ((DefaultMutableTreeNode) _node.getChildAt (i),simulation,view);
  }

  private void traverseResourcesNeeded (DefaultMutableTreeNode _node, Set<String> _set, String _keyword) {
//    if (_node!=rootNode) 
      viewOf(_node).addResourcesNeeded(_set, _keyword);
    for (int i=0,n=_node.getChildCount(); i<n; i++)
      traverseResourcesNeeded ((DefaultMutableTreeNode) _node.getChildAt (i),_set, _keyword);
  }
  
  private void traverseGenerateCode(DefaultMutableTreeNode _node, StringBuffer _buffer, int _type) {
//    if (_node!=rootNode) 
      viewOf(_node).generateCode (_buffer,_type);
    for (int i=0,n=_node.getChildCount(); i<n; i++) 
      traverseGenerateCode ((DefaultMutableTreeNode) _node.getChildAt (i),_buffer,_type);
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
    popup.addSeparator();
    JLabel positionLabel = new JLabel (res.getString("Tree.ChangePosition"),SwingConstants.CENTER);
    positionLabel.setBorder(new EmptyBorder(0,5,0,0));
//    positionLabel.setFont(positionLabel.getFont().deriveFont(Font.BOLD));
    popup.insert (positionLabel,6);
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

    JMenuItem localHelpMi = new JMenuItem (res.getString("Help.Help"));
    localHelpMi.setActionCommand("LocalHelp"); 
    localHelpMi.addActionListener(al); 
    popup.add(localHelpMi);
    
    JMenuItem helpMi = new JMenuItem ("Wiki "+ res.getString("Help.Help"));
    helpMi.setActionCommand("Help"); 
    helpMi.addActionListener(al); 
    popup.add(helpMi);

    popup.validate();
//    popup.setVisible (true);
//    popup.setVisible (false);

    JLabel rootTopLabel = new JLabel (res.getString("Tree.MenuFor")+ " " + res.getString("Tree.Main"),SwingConstants.CENTER);
    rootTopLabel.setBorder(new EmptyBorder(0,5,0,0));
    reducedPopup.insert (rootTopLabel,0);

    createMenuItem (reducedPopup,"Edit",al);
    reducedPopup.addSeparator();

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

//  private JCheckBoxMenuItem createCheckBoxMenuItem (JPopupMenu _popup, String _key, ActionListener _al) {
//    JCheckBoxMenuItem mi = new JCheckBoxMenuItem (res.getString("Tree."+_key));
//    mi.setActionCommand (_key);
//    mi.addActionListener (_al);
//    _popup.add (mi);
//    return mi;
//  }

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
//    System.err.println ("Action = "+cmd);
//    System.err.println ("Current node = root node = "+(currentNode==rootNode));
    if (currentNode==rootNode) {
      if (cmd.equals("Edit")) { viewOf(currentNode).edit (); return; }
      else if (cmd.equals("Paste")) paste (currentNode);
      else if (cmd.equals("Help")) getEjs().openWikiPage("HtmlView"); 
      else JOptionPane.showMessageDialog(getComponent(),res.getString("Tree.MainDoesNotAcceptThis")+": "+cmd,
        res.getString("Tree.ErrorMessage"),JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (cmd.equals("Edit")) { viewOf(currentNode).edit (); return; }
    if (cmd.equals("Rename")) rename (currentNode);
    else if (cmd.equals("Reparent")) {
      String txt = JOptionPane.showInputDialog(getComponent(),res.getString("Tree.ReparentTo"),
        res.getString("Tree.ReparentTitle"),JOptionPane.QUESTION_MESSAGE);
      if (txt!=null && txt.trim().length()>0) reparent (currentNode,txt.trim());
    }
    else if (cmd.equals("MoveUp"))   moveUpAndDown(currentNode,true);
    else if (cmd.equals("MoveDown")) moveUpAndDown(currentNode,false);
    else if (cmd.equals("Remove")) remove (currentNode);
    else if (cmd.equals("Cut")) { copy (currentNode); remove (currentNode); }
    else if (cmd.equals("Copy")) copy (currentNode);
    else if (cmd.equals("Paste")) paste (currentNode);
    else if (cmd.equals("Help")) getEjs().openWikiPage("HtmlView"+viewOf(currentNode).getClassname());
    else if (cmd.equals("LocalHelp")) showLocalHelp(viewOf(currentNode).getClassname());

  }

// --------------------- Inner classes -------------------

  class TreeMouseListener extends MouseAdapter {
    public void mousePressed (MouseEvent _e) {
      TreePath selPath = tree.getPathForLocation(_e.getX(), _e.getY());
      if (selPath==null) {
        currentNode = null;
        mPalette.clear();
        return;
      }
      currentNode = (DefaultMutableTreeNode) selPath.getLastPathComponent();

      if (OSPRuntime.isPopupTrigger(_e)) { //SwingUtilities.isRightMouseButton(_e)) {
        mPalette.clear();
        if (currentNode==rootNode) {
          reducedPopup.show (_e.getComponent(),35, _e.getY());
          return;
        }
        reparentMi.setEnabled (true);
//        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();
        topLabel.setText (res.getString("Tree.MenuFor")+" "+currentNode);
        if (!editable) {
          renameMi.setEnabled(false);   reparentMi.setEnabled(false);
          upMi.setEnabled(false);
          downMi.setEnabled(false);
          removeMi.setEnabled(false);
          cutMi.setEnabled(false); copyMi.setEnabled(false); pasteMi.setEnabled(false);
        }
//        creationPanel.clear();
        popup.show (_e.getComponent(),35, _e.getY());
      }
      else if ((_e.getModifiers() | InputEvent.BUTTON1_MASK) != 0) {
        if (_e.getClickCount()>1) {
          mPalette.clear();
          viewOf (currentNode).edit();
        }
        else if (mPalette.getClassToCreate()!=null) {
          boolean discardAfterCreation = !_e.isAltDown();
          add (currentNode,mPalette.getClassToCreate(),getUniqueName(mPalette.getNameToCreate()),getComponent(),-1);
          if (discardAfterCreation) mPalette.clear(); // disable the magic wand after creation
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
      if (node==rootNode) icon = TreeOfElements.rootIcon;
      else icon = Palette.getElementIcon(classname);
      if (icon!=null) setIcon (icon);
      return this;
    }
  }

//  class MyTreeExpansionListener implements TreeExpansionListener {
//    public void treeCollapsed(TreeExpansionEvent _event) {
//      DefaultMutableTreeNode node =
//        (DefaultMutableTreeNode) _event.getPath().getPathComponent(_event.getPath().getPathCount()-1);
//      if (node==rootNode) {
//        tree.expandRow(0);
//      }
//    }
//    public void treeExpanded(TreeExpansionEvent _event) {
//      DefaultMutableTreeNode node = (DefaultMutableTreeNode) _event.getPath().getPathComponent(_event.getPath().getPathCount()-1);
//    }
//
//  }

} // end of class
