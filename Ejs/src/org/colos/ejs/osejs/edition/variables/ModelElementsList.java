/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified : March 2006
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.model_elements.ModelElement;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;

import org.colos.ejs.osejs.utils.*;
import org.opensourcephysics.display.OSPRuntime;

import java.awt.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.io.*;

/**
 * The class for the added elements 
 */
public class ModelElementsList extends JScrollPane implements DropTargetListener, DragSourceListener, DragGestureListener, 
                                                              org.colos.ejs.model_elements.ModelElementsCollection {
  private Osejs ejs;
  private MyDefaultListModel<ModelElementInformation> listModel;
  private JList<ModelElementInformation> list;
  private JPopupMenu elementPopup, reducedPopup;
  private JLabel topLabel;
  private DragSource dragSource;
  private boolean changed = false;

  @SuppressWarnings("unchecked")
  public ModelElementsList(Osejs _ejs) {
    this.ejs = _ejs;
    listModel = new MyDefaultListModel<ModelElementInformation>();
    list = new JList<ModelElementInformation>(listModel);
    
    File modelsDir = new File(ejs.getBinDirectory(),"extensions/model_elements");
    String modelsDirPath = FileUtils.getPath(modelsDir);

    list.setCellRenderer(new ModelElementListCellRenderer(modelsDirPath, true));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setLayoutOrientation(JList.VERTICAL);
    list.setBackground(Color.WHITE);
    list.addMouseListener(new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        int index = list.locationToIndex(evt.getPoint());
        if (index<0) return;
        if (!list.getCellBounds(index,index).contains(evt.getPoint())) { // Click on an empty area
          list.clearSelection();
          if (OSPRuntime.isPopupTrigger(evt)) reducedPopup.show(evt.getComponent(),evt.getX(),evt.getY());
          return;
        }
        // The click was on a list element
        if (OSPRuntime.isPopupTrigger(evt)) {
          list.setSelectedIndex(index);
          ModelElementInformation elementInfo = listModel.elementAt(index);
          topLabel.setText(Osejs.getResources().getString("Tree.MenuFor")+" "+elementInfo.getName());
          elementPopup.show(evt.getComponent(),evt.getX(),evt.getY());
        }
        else if (evt.getClickCount()>1) {
          ModelElementInformation elementInfo = listModel.elementAt(index);
          if (index>=0) elementInfo.getElement().showEditor(elementInfo.getName(),list, ModelElementsList.this);
        }
        else if (index<0) list.clearSelection();
      }
    });
    setViewportView(list);
    setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    setBorder(ElementsEditor.BLACK_BORDER);

    createMenus();

    // Dropping
    DropTarget dropTarget = new DropTarget(list,this);
    setDropTarget(dropTarget);
    // Dragging : to reorder elements
    dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(list,DnDConstants.ACTION_COPY_OR_MOVE, this);
  }

  // ----------- Implementation of ModelElementsCollection ------------

  public String chooseFilename(Component _parentComponent, String _title, String _descriptions, String _extensions) {
    return org.colos.ejs.control.editors.EditorForFile.edit(ejs,_title,_parentComponent,_descriptions,_extensions);
  }

  public String chooseVariable(Component _parentComponent, String _typesAllowed, String _currentValue) {
    return EditorForVariables.edit (ejs.getModelEditor(), "Variables", _typesAllowed, _parentComponent, _currentValue,null);
  }
  
  public String chooseViewElement(Component _parentComponent, Class<?> _typesAllowed, String _currentValue) {
    return EditorForVariables.edit (ejs.getViewEditor(), _typesAllowed, _parentComponent, _currentValue);
  }
  
  public void reportChange (org.colos.ejs.model_elements.ModelElement _element) {
    for (int i=0, n=listModel.getSize(); i<n; i++) {
      ModelElementInformation info = listModel.elementAt(i);
      if (info.getElement()==_element) {
        listModel.refreshCell(i);
        setChanged(true); 
        return;
      }
    }
  }

  public org.colos.ejs.osejs.Osejs getEJS() { return ejs; }
  
  // -----------------------------------
  // Utility methods
  // -----------------------------------

  /**
   * Whether this variable name is already in use
   * @param _name
   * @return
   */
  public boolean nameExists(String _name) {
    for (int i = 0, n=listModel.getSize(); i<n; i++) {
      ModelElementInformation elementInfo  = listModel.getElementAt(i);
      if (_name.equals(elementInfo.getName())) return true;
    }
    return false;
  }

  public boolean isChanged() { return changed; }
  
  public void setChanged(boolean _changed) { this.changed = _changed; }

  public void clear () { 
    for (int i=0, n=listModel.getSize(); i<n; i++) {
      ModelElementInformation info = listModel.elementAt(i);
      info.getElement().clear();
    }
    listModel.removeAllElements();
    list.repaint();
    changed = false;
  }

  public java.util.List<ModelElementInformation> getElements() {
    java.util.List<ModelElementInformation> elementsList = new java.util.ArrayList<ModelElementInformation>();
    for (int i=0, n=listModel.getSize(); i<n; i++) {
      elementsList.add(listModel.elementAt(i));
    }
    return elementsList;
  }
  
  /**
   * Adds a new model element (with information) to the list
   * @param _elementInfor
   * @param _elementName the name for the new element
   * @return
   */
  public void addElement(ModelElementInformation _elementInfo, int _position) {
      if (_position<0 || _position>listModel.getSize()) listModel.add(listModel.getSize(),_elementInfo);
      else listModel.add(_position,_elementInfo);
      ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
      changed = true;
  }

  /**
   * Selects the given element, if present
   * @param _element
   * @returns the element name
   */
  public String selectElement (org.colos.ejs.model_elements.ModelElement _element) {
    for (int i=0, n=listModel.getSize(); i<n; i++) {
      ModelElementInformation info = listModel.elementAt(i);
      if (info.getElement()==_element) {
        list.setSelectedIndex(i);
        return info.getName();
      }
    }
    return null;
  }
  
//  /**
//   * Makes sure each element information contains the right position
//   */
//  public void renumberElements() {
//    for (int i=0,n=listModel.getSize(); i<n; i++) ((ModelElementInformation) listModel.elementAt(i)).setPosition(i);
//  }
  
  // -----------------------------------
  // Drag methods
  // -----------------------------------

  public void dragGestureRecognized(DragGestureEvent dge) {
    int index = list.getSelectedIndex();
    if (index<0) return;
    ModelElementInformation elementInfo = listModel.elementAt(index);
    //      if (dge.getDragAction()!=DnDConstants.ACTION_MOVE) return; // Only support drag COPY actions
    elementInfo.setPosition(index); // Make sure the element knows it position
    Cursor cursor = DragSource.DefaultMoveDrop;
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
  // Drop methods
  // -----------------------------------

  public void dragEnter(DropTargetDragEvent dtde) {
    if (dtde.isDataFlavorSupported(ModelElementTransferable.modelElementFlavor)) {
      dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE); 
      int draggedPreviousPosition = -1;
      try { 
        Transferable transferable = dtde.getTransferable();
        ModelElementInformation selection = (ModelElementInformation) transferable.getTransferData(ModelElementTransferable.modelElementFlavor);
        draggedPreviousPosition = selection.getPosition();
      }
      catch (Exception exc) { exc.printStackTrace(); }
      if (draggedPreviousPosition<0) setBorder(ElementsEditor.ACTIVE_BORDER);
    }
  }

  public void dragExit(DropTargetEvent dte) {
    setBorder(ElementsEditor.BLACK_BORDER);
    list.clearSelection();
  }

  public void drop(DropTargetDropEvent dtde) { 
    setBorder(ElementsEditor.BLACK_BORDER);
    if (dtde.isDataFlavorSupported(ModelElementTransferable.modelElementFlavor)) dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    else {
      dtde.rejectDrop();
      list.clearSelection();
      return;
    }
    ModelElementInformation selection;
    try { 
      Transferable transferable = dtde.getTransferable();
      selection = (ModelElementInformation) transferable.getTransferData(ModelElementTransferable.modelElementFlavor);
    }
    catch (Exception exc) {
      exc.printStackTrace();
      selection = null;
    }
    if (selection==null) {
      dtde.dropComplete(false);
      list.clearSelection();
      return;
    }
    int previousPosition = selection.getPosition();
    if (previousPosition<0) { // This is an addition from the elements panel
      int index = list.locationToIndex(dtde.getLocation());
      if (index>=0 && !list.getCellBounds(index,index).contains(dtde.getLocation())) index = -1;
      final ModelElement finalElement = selection.getElement();
      final File finalJarfile = selection.getJarFile();
      final int finalIndex = index;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          String newName = getNameForElement(finalElement.getGenericName(),null);
          if (newName!=null) { // Create a new element
            String classname = finalElement.getClass().getName();
            final ModelElementInformation elementInfo = ejs.getModelEditor().getElementsEditor().getPalette().instantiateModelElement(classname,finalJarfile,false);
            if (elementInfo!=null) {
              elementInfo.setName(newName);
              addElement(elementInfo,finalIndex);
            }
          }
        }
      });
      dtde.dropComplete(true);

    }
    else { // This is a change in order of the element in the panel of added elements
      int index = list.locationToIndex(dtde.getLocation());
      if (index<previousPosition) {
        listModel.remove(previousPosition);
        listModel.add(index,selection);
        changed = true;
        dtde.dropComplete(true);
      }
      else if (index>previousPosition) {
        listModel.remove(previousPosition);
        listModel.add(index,selection);
        changed = true;
        dtde.dropComplete(true);
      }
      else dtde.dropComplete(false);
    }
    list.clearSelection();
//    renumberElements();
  }

  public void dragOver(DropTargetDragEvent dtde) { 
    int index = list.locationToIndex(dtde.getLocation());
    if (index<0) return;
    if (list.getCellBounds(index,index).contains(dtde.getLocation())) list.setSelectedIndex(index);
    else list.clearSelection();
  }

  public void dropActionChanged(DropTargetDragEvent dtde) { }

  // -----------------------------------
  // Private methods
  // -----------------------------------

  /**
   * Asks the user for a unique name for an element
   */
  private String getNameForElement (String _genericName, String _proposedName) {
    if (_proposedName==null) { // A new addition, requires a brand new name
      _proposedName = (String) JOptionPane.showInputDialog(this,Osejs.getResources().getString("Tree.ProvideAName"),
          Osejs.getResources().getString("Tree.NameTitle"),JOptionPane.QUESTION_MESSAGE,null,null,getUniqueName(org.colos.ejs.osejs.OsejsCommon.beginningToLower(_genericName)));
    }
    else { // This is a renaming operation
      _proposedName = (String) JOptionPane.showInputDialog(this,Osejs.getResources().getString("Tree.RenameTo"),
          Osejs.getResources().getString("Tree.RenameTitle"),JOptionPane.QUESTION_MESSAGE,null,null,_proposedName);
    }
    if (_proposedName==null || _proposedName.trim().length()<=0) return null;
    String uniqueName = getUniqueName (_proposedName);
    if (! _proposedName.equals(uniqueName)) JOptionPane.showMessageDialog(ModelElementsList.this,Osejs.getResources().getString("Tree.NameModified"),
        Osejs.getResources().getString("Tree.Warning")+uniqueName, JOptionPane.WARNING_MESSAGE);
    return uniqueName;
  }

  private String getUniqueName (String name) {
    name = OsejsCommon.getValidIdentifier(name.trim());
    String newname = name;
    int i=1;
    while (ejs.getModelEditor().getVariablesEditor().nameExists(newname)) newname = name + (++i);
    return newname;
  }

  private void createMenus() {
    elementPopup = new JPopupMenu ();
    reducedPopup = new JPopupMenu ();
    topLabel = new JLabel (Osejs.getResources().getString("Tree.MenuFor"),SwingConstants.CENTER);
    topLabel.setBorder(new EmptyBorder(0,5,0,0));
    elementPopup.add (topLabel);

    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Tree.Edit")) {
      public void actionPerformed(ActionEvent evt) { 
        ModelElementInformation elementInfo = list.getSelectedValue();
        if (elementInfo!=null) elementInfo.getElement().showEditor(elementInfo.getName(),list, ModelElementsList.this);
      }
    });
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Tree.Rename")) {
      public void actionPerformed(ActionEvent e) { 
        ModelElementInformation elementInfo = list.getSelectedValue();
        if (elementInfo==null) return;
        String newName = getNameForElement(elementInfo.getElement().getGenericName(),elementInfo.getName());
        if (newName!=null) {
          elementInfo.setName (newName);
          reportChange(elementInfo.getElement());
          ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
          elementInfo.getElement().refreshEditor(newName);
          changed = true;
        }
      }
    });
    elementPopup.addSeparator();
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Utils.cut-to-clipboard")) {
      public void actionPerformed(ActionEvent e) { 
        int index = copyToClipboard();
        if (index>=0) {
          ModelElementInformation info = listModel.elementAt(index);
          info.getElement().clear();
          listModel.removeElementAt(index);
          ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
//          renumberElements();
          changed = true;
        }
      }
    });
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Utils.copy-to-clipboard")) {
      public void actionPerformed(ActionEvent e) { copyToClipboard(); }
    });
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Utils.paste-from-clipboard")) {
      public void actionPerformed(ActionEvent e) {
        int index = list.getSelectedIndex();
        pasteFromClipboard(index); 
      }
    });
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Tree.Remove")) {
      public void actionPerformed(ActionEvent e) { 
        int index = list.getSelectedIndex();
        if (index>=0) {
          ModelElementInformation info = listModel.elementAt(index);
          info.getElement().clear();
          listModel.removeElementAt(index);
          changed = true;
//          renumberElements();
        }
      }
    });
    elementPopup.addSeparator();
    elementPopup.add(new AbstractAction(Osejs.getResources().getString("Help.Help")) {
      public void actionPerformed(ActionEvent evt) { 
        ModelElementInformation elementInfo = list.getSelectedValue();
        if (elementInfo!=null) elementInfo.getElement().showHelp(list);
      }
    });
    elementPopup.validate();

    // The pop up for the list itself
    reducedPopup.add(new AbstractAction(Osejs.getResources().getString("Utils.paste-from-clipboard")) {
      public void actionPerformed(ActionEvent e) { 
        pasteFromClipboard(-1); 
      }
    });
    reducedPopup.addSeparator();
    reducedPopup.add(new AbstractAction(Osejs.getResources().getString("Help.Help")) {
      public void actionPerformed(ActionEvent e) {
        ejs.openWikiPage("ModelElements");
      }
    });
    reducedPopup.validate();
  }

  private int copyToClipboard() { 
    int index = list.getSelectedIndex();
    if (index<0) return -1;
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    ModelElementInformation elementInfo = listModel.elementAt(index);
    // A copy of this element is placed on the clipboard, not the element itself!
    String classname = elementInfo.getElement().getClass().getName();
    ModelElementInformation copyElementInfo = ejs.getModelEditor().getElementsEditor().getPalette().instantiateModelElement(classname,elementInfo.getJarFile(),false);
    if (copyElementInfo==null) {
      System.err.println ("Warning! Could not copy element "+elementInfo.getName()+" to the clipboard!");
      return -1;
    }
    copyElementInfo.setName(elementInfo.getName()); // This will be changed when pasting it, if needed
    ModelElementTransferable selection = new ModelElementTransferable(copyElementInfo);
    theClipboard.setContents(selection,null);
    return index;
  }

  private void pasteFromClipboard(int index) {
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = theClipboard.getContents(null);
    DataFlavor flavor = ModelElementTransferable.modelElementFlavor;
    if (contents.isDataFlavorSupported(flavor)) {
      try { 
        ModelElementInformation selection = (ModelElementInformation) contents.getTransferData(flavor);
        // Make yet another copy, in case the paste is used several times
        String classname = selection.getElement().getClass().getName();
        ModelElementInformation copyElementInfo = ejs.getModelEditor().getElementsEditor().getPalette().instantiateModelElement(classname,selection.getJarFile(),false);
        if (copyElementInfo==null) {
          System.err.println ("Warning! Could not paste element "+selection.getName()+" from the clipboard!");
          return;
        }
        String newName = getNameForElement(copyElementInfo.getElement().getGenericName(), getUniqueName(selection.getName()));
        if (newName==null) return;
        copyElementInfo.setName(newName);
        if (index<0) listModel.addElement(copyElementInfo);
        else listModel.insertElementAt(copyElementInfo, index);
        ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
//        renumberElements();
        changed = true;
      }
      catch (Exception exc) { exc.printStackTrace(); };
    }
  }

  private class MyDefaultListModel<E> extends DefaultListModel<E> {
    public void refreshCell(int row) { fireContentsChanged(this,row,row); }
  };

}

