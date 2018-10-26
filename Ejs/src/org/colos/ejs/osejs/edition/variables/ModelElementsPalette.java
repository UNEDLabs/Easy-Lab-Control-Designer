/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified : March 2006
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.model_elements.EJSAware;
import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.LaunchClassChooser;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.minijar.PathAndFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileSystemView;

import java.io.*;

/**
 * The class for available predefined elements 
 */
public class ModelElementsPalette extends JPanel implements DragSourceListener, DragGestureListener {
  private Osejs ejs;
  private JTextField dirField;
  private DefaultListModel<Object> listModel;
  private JList<Object> list; 
  private JEditorPane htmlArea;
  private JDialog helpDialog;
  private DragSource dragSource;

  private File modelsDir;
  private String modelsDirPath;
  private Set<ModelElementInformation> userDefinedElements = new HashSet<ModelElementInformation>();

  /**
   * Constructor
   * @param _ejs
   * @param _background
   */
  @SuppressWarnings("unchecked")
  public ModelElementsPalette(Osejs _ejs, Color _background) {
    super(new BorderLayout());
    
    ejs = _ejs;
    modelsDir = new File(ejs.getBinDirectory(), ejs.supportsJava() ? "extensions/model_elements" : "javascript/model_elements" );
    modelsDirPath = FileUtils.getPath(modelsDir);
    
    dirField = new JTextField();
    dirField.setEditable(false);
    dirField.setBackground(_background);

    // The list model contains one of:
    // - PathAndFile for files and dirs
    // - ModelElementInformation information about a model element
    // - A Set<?> meaning the set of user-defined elements (containd in model-specific JAR files)
    listModel = new DefaultListModel<Object>();
    list = new JList<Object>(listModel);
    list.setCellRenderer(new ModelElementListCellRenderer(modelsDirPath,false));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setVisibleRowCount(-1);
    list.setBackground(_background);
    list.addMouseListener(new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (evt.getClickCount()>1) { // Only for double-clicks
          Object value = list.getSelectedValue();
          if (value instanceof PathAndFile) {
            PathAndFile paf = (PathAndFile) value;
            File file = paf.getFile();
            if (file.isDirectory()) readElements(file);
            else {
              String name = file.getName().toLowerCase();
              if (isHtmlFile(name)) showHtmlFile(paf);
              else org.opensourcephysics.desktop.OSPDesktop.open(file);
            }
          }
          else if (value instanceof ModelElementInformation) ((ModelElementInformation) value).getElement().showHelp(ejs.getMainFrame()); // Show information
          else if (value.equals(userDefinedElements)) {
            listModel.removeAllElements();
            listModel.addElement(new PathAndFile("..",modelsDir));
            for (ModelElementInformation elementInfo : userDefinedElements) listModel.addElement(elementInfo);
            dirField.setText(ModelElementListCellRenderer.USER_DEFINED_TEXT);
            list.repaint();
          }
        }
      }        
    });
    JScrollPane scrollPanel = new JScrollPane(list); //setViewportView(list);
    scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPanel.setBorder(null);

    add(dirField,BorderLayout.NORTH);
    add(scrollPanel,BorderLayout.CENTER);
    setBorder(ElementsEditor.BLACK_BORDER);

    // Dragging
    dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_COPY_OR_MOVE, this);

    // Make the first read
    readElements(modelsDir);
  }
  
  public void clear() {
    for (ModelElementInformation elementInfo : userDefinedElements) ResourceLoader.removeSearchPath(elementInfo.getJarFile().getAbsolutePath());
    userDefinedElements.clear();
    readElements(modelsDir);
  }
  
  // -----------------------------------
  // Drag methods
  // -----------------------------------

  public void dragGestureRecognized(DragGestureEvent dge) {
    if (!(list.getSelectedValue() instanceof ModelElementInformation)) return;
    ModelElementInformation elementInfo = (ModelElementInformation) list.getSelectedValue();
    //  if (dge.getDragAction()!=DnDConstants.ACTION_COPY) return; // Only support drag COPY actions
    Cursor cursor = DragSource.DefaultCopyDrop;
    Transferable transferable = new ModelElementTransferable(elementInfo);
    if (DragSource.isDragImageSupported()) {
      Image image = elementInfo.getElement().getImageIcon().getImage();
      dge.startDrag(cursor, image, new Point(0,0), transferable, this);
    }
    else dge.startDrag(cursor, transferable); 
  }

  public void dragDropEnd(DragSourceDropEvent dsde) { list.clearSelection(); }

  public void dragEnter(DragSourceDragEvent dsde) { }

  public void dragExit(DragSourceEvent dse) { }

  public void dragOver(DragSourceDragEvent dsde) { }

  public void dropActionChanged(DragSourceDragEvent dsde) { }

  // -----------------------------------
  // Utility methods
  // -----------------------------------

  static private boolean isHtmlFile(String filename) {
    return filename.endsWith(".html") || filename.endsWith(".htm");
  }

  static private boolean isPdfFile(String filename) {
    return filename.endsWith(".pdf");
  }

  static ModelElementInformationComparator comparator = new ModelElementInformationComparator();
  
  /**
   * Reads the model elements in a directory and displays them in the elements panel 
   */
  private void readElements (File _directory) {
    listModel.removeAllElements();
    File files[] = FileSystemView.getFileSystemView().getFiles(_directory, false);
    
    // Possible HTML or PDF files
    for (int i = 0; i<files.length; i++) {
      if (files[i].isDirectory()) continue;
      String name = files[i].getName().toLowerCase();
      if (isHtmlFile(name) || isPdfFile(name)) listModel.addElement(new PathAndFile(org.colos.ejs.library.utils.FileUtils.getPlainName(files[i]),files[i]));
    }
    
    // The user-defined collection of elements
    if (_directory.equals(modelsDir)) {
      if (!userDefinedElements.isEmpty()) listModel.addElement(userDefinedElements);
    }
    else listModel.addElement(new PathAndFile("..",_directory.getParentFile())); // Go up icon
    
    // Subdirectories
    for (int i = 0; i<files.length; i++) {
      if (files[i].isDirectory() && !files[i].getName().startsWith("_")) {
        if (FileSystemView.getFileSystemView().getFiles(files[i], false).length>0) // i.e. it is not an empty directory
          listModel.addElement(new PathAndFile(FileUtils.getPath(files[i]),files[i]));
      }
    }
    
    // Finally, jar files with elements
    java.util.List<ModelElementInformation> arrayList = new ArrayList<ModelElementInformation>();
    for (int i = 0; i<files.length; i++) {
      if (files[i].isDirectory()) continue;
      if (files[i].getName().toLowerCase().endsWith(".jar")) arrayList.addAll(readModelElements(files[i],false));
    }
    // sort and add them all
    java.util.Collections.sort(arrayList, comparator);
    for (ModelElementInformation elementInfo : arrayList) listModel.addElement(elementInfo);
    dirField.setText(getDisplayName(_directory, modelsDirPath));
    list.repaint();
  }

  private void showHtmlFile(PathAndFile _paf) {
    if (helpDialog==null) { // create the dialog
      htmlArea = new JEditorPane ();
      htmlArea.setContentType ("text/html");
      htmlArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      htmlArea.setEditable(false);
      htmlArea.addHyperlinkListener(new HyperlinkListener() { // Make hyperlinks work
        public void hyperlinkUpdate(HyperlinkEvent e) {
          if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) OSPDesktop.displayURL(e.getURL().toString());
        }
      });
      JScrollPane helpComponent = new JScrollPane(htmlArea);
      helpComponent.setPreferredSize(new Dimension(600,500));

      helpDialog = new JDialog(ejs.getMainFrame());
      helpDialog.getContentPane().setLayout(new BorderLayout());
      helpDialog.getContentPane().add(helpComponent,BorderLayout.CENTER);
      helpDialog.setModal(false);
      helpDialog.pack();
      helpDialog.setLocationRelativeTo(ejs.getMainFrame());
    }
    
    try { // read the help for this element
      java.net.URL htmlURL = ResourceLoader.getResource(_paf.getFile().getAbsolutePath()).getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    
    helpDialog.setTitle(_paf.getPath());
    helpDialog.setVisible(true);
  }

  
  /**
   * Adds user-defined elements defined in a model-specific JAR file
   * @param _jarFile the model-specific JAR file
   */
  public void addUserDefinedElements(String _jarPath) {
    File jarFile = ejs.getRelativeFile(_jarPath);
    if (hasModelElements(jarFile)) {
      int confirm = JOptionPane.showConfirmDialog(ejs.getMainPanel(),
          Osejs.getResources().getString("ElementsEditor.JarRequired")+ " " + _jarPath+"\n\n"+
          Osejs.getResources().getString("ElementsEditor.AddUserElements"),
          Osejs.getResources().getString("Warning"), JOptionPane.YES_NO_OPTION);
      if (confirm==JOptionPane.YES_OPTION) {
        readModelElements(jarFile,true);
        readElements(modelsDir);
      }
    }
  }
  
  // -----------------------------------
  // Non-static part
  // -----------------------------------
  
  // The map from JAR file to Sets of ModelElementInformation (so that not to process the same JAR twice)
  static private final Map<File,Set<ModelElementInformation>> modelsMap = new HashMap<File,Set<ModelElementInformation>>(); 

  /**
   * Reads the Set of ModelElements inside a JAR file
   * @param _jarFile the file to read 
   * @param _jarPath if non-null, keeps the information for future instantiation of the model element 
   */
  private Set<ModelElementInformation> readModelElements(File _jarFile, boolean _isUserDefined) {
    Set<ModelElementInformation> elementSet;
    if (!_isUserDefined) { // These are stored in a static map, so that they are only read once
      elementSet = modelsMap.get(_jarFile);
      if (elementSet!=null) return elementSet;
    }

    elementSet = new HashSet<ModelElementInformation>();
    try {
      ZipInputStream input = new ZipInputStream(new FileInputStream(_jarFile));
      ZipEntry zipEntry = null;
      while ((zipEntry = input.getNextEntry())!=null) {
        if (zipEntry.isDirectory()) continue; // don't include directories
        if (zipEntry.getName().endsWith(".class")) {
//          System.err.println("Trying class "+zipEntry.getName()+" FROM "+_jarFile.getName());
          ModelElementInformation elementInfo = instantiateModelElement(zipEntry.getName(),_isUserDefined ? _jarFile : null,true);
          if (elementInfo!=null) elementSet.add(elementInfo);
        }
      }
      input.close();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      System.err.println ("ModelElementsPalette : Error when reading JAR file for model element: "+_jarFile.getAbsolutePath());
    }

    if (_isUserDefined) userDefinedElements.addAll(elementSet);
    else modelsMap.put(_jarFile, elementSet);
    
    return elementSet;
  }

  /**
   * Whether a JAR file contains any ModelElement
   */
  static public boolean hasModelElements(File _jarFile) {
    try {
      String jarPath = _jarFile.getAbsolutePath();
      ZipInputStream input = new ZipInputStream(new FileInputStream(_jarFile));
      ZipEntry zipEntry = null;
      Class<ModelElement> type = ModelElement.class; // interface implemented by the class
      while ((zipEntry = input.getNextEntry())!=null) {
        if (zipEntry.isDirectory()) continue; // don't include directories
        if (zipEntry.getName().endsWith(".class")) {
          String elementClassname = zipEntry.getName();
          elementClassname = elementClassname.substring(0,elementClassname.length()-6);
          elementClassname = elementClassname.replace('/', '.');
          Class<?> elementClass = LaunchClassChooser.getClassOfType(jarPath, elementClassname, type);
          if (elementClass!=null && !java.lang.reflect.Modifier.isAbstract(elementClass.getModifiers())) {
            input.close();
            return true;
          }
        }
      }
      input.close();
    }
    catch (Exception exc) {
      exc.printStackTrace();
      System.err.println ("Error when inspecting JAR file for model elements "+FileUtils.getPath(_jarFile));
    }
    return false;
  }

  /**
   * Instantiates a class, using its default constructor, if it implements the ModelElement interface
   * @param _elementClassname
   * @param _jarPath if non-null, keeps the information for future instantiation of the model element
   * @param _justDoIt if true, it will create the element without further conditions, if false, it will make sure the (user-defined) JAR file is allowed to create model elements 
   * @return a ModelElementInformation for the model element
   */
  public ModelElementInformation instantiateModelElement(String _elementClassname, File _jarFile, boolean _justDoIt) {
    if (_elementClassname.endsWith(".class")) _elementClassname = _elementClassname.substring(0,_elementClassname.length()-6);
    _elementClassname = _elementClassname.replace('/', '.');
    try {
//      System.err.println("Instantiate "+_elementClassname+ " from jar file "+_jarFile);
      Class<ModelElement> elementType = ModelElement.class;
      if (_jarFile==null) { // Class from the standard classpath
        Class<?> elementClass = Class.forName(_elementClassname);
        if (elementType.isAssignableFrom(elementClass) && !java.lang.reflect.Modifier.isAbstract(elementClass.getModifiers())) {
          Object obj = elementClass.newInstance();
          if (obj instanceof EJSAware) ((EJSAware) obj).setEJS(ejs);
          return new ModelElementInformation(elementType.cast(obj),null);
        }
//        System.err.println ("ModelElementPalette: Not a model element class: "+_elementClassname);
      }
      else {
        if (!_justDoIt) {
          for (ModelElementInformation elementInfo : this.userDefinedElements) {
            if (elementInfo.getJarFile().equals(_jarFile)) { // Only if this jar file has been allowed to create elements
              _justDoIt = true;
              break;
            }
          }
        }
        if (_justDoIt) {
          ResourceLoader.addSearchPath(_jarFile.getAbsolutePath()); // add the search path so that images and other resources are found
          Class<?> elementClass = LaunchClassChooser.getClassOfType(_jarFile.getAbsolutePath(), _elementClassname, elementType);
          if (elementClass==null) return null;
//          System.err.println ("Instantiating class "+_elementClassname + " from jar file : "+_jarFile.getAbsolutePath());
          ModelElement element = (ModelElement) elementClass.newInstance();
          if (element instanceof EJSAware) ((EJSAware) element).setEJS(ejs);

          return new ModelElementInformation(element,_jarFile);
        }
        JOptionPane.showMessageDialog(ModelElementsPalette.this,
            Osejs.getResources().getString("ElementsEditor.JarRequired")+ " " + ejs.getRelativePath(_jarFile)+"\n\n"+
            Osejs.getResources().getString("ElementsEditor.JarNotLoaded")+"\n  "+_elementClassname,
            Osejs.getResources().getString("Osejs.File.ReadingError"), JOptionPane.ERROR_MESSAGE);
      }
    } 
    catch (Exception exc) { // Do nothing, but return null
      System.err.println ("Error when instantiating model element "+_elementClassname);
      exc.printStackTrace();
    }
    return null;
  }

  /**
   * Gets the name of a file in a nicer way
   * @param _file
   * @param _basePath
   * @return
   */
  static public String getDisplayName(File _file, String _basePath) {
    String displayName = FileUtils.getRelativePath(_file, _basePath, false);
    if (displayName.endsWith("/")) return displayName.substring(0,displayName.length()-1);
    return displayName;
  }

  static private class ModelElementInformationComparator implements java.util.Comparator<ModelElementInformation> {

    public int compare(ModelElementInformation o1, ModelElementInformation o2) {
      return o1.getElement().getGenericName().compareToIgnoreCase(o2.getElement().getGenericName());
    }
    
  }

}



