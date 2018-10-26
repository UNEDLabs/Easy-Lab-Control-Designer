package org.colos.ejs.osejs.edition.html_view;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class ElementTreeTransferHandler extends TransferHandler {
  ElementsTree mElementsTree;
  DefaultMutableTreeNode mNode;

  public ElementTreeTransferHandler(ElementsTree elementsTree) {
    mElementsTree = elementsTree;
  }

  public boolean canImport(TransferHandler.TransferSupport support) {
    if (!support.isDrop()) return false;
    if (!support.isDataFlavorSupported(Selection.viewFlavor)) return false;
    support.setShowDropLocation(true);
    JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
    TreePath dest = dl.getPath();
    if (dest==null) return true;
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
    try { 
      String code = (String) support.getTransferable().getTransferData(Selection.viewFlavor);
      if (mElementsTree.isFragment(code)) {
        if (mNode==null) return true;
        return mElementsTree.parentAcceptsChild(parent, mNode);
      }
      return mElementsTree.parentAcceptsChild(parent, code);
    }
    catch (Exception exc) { return true; } // probably importing from another EjsS 
  }

  protected Transferable createTransferable(JComponent c) {
    TreePath[] paths = ((JTree) c).getSelectionPaths();
    if (paths != null) {
      mNode = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
      return mElementsTree.createTransferable(mNode);
    }
    mNode = null;
    return null;
  }

  protected void exportDone(JComponent source, Transferable data, int action) {
      mNode = null;
//      if ((action & MOVE) == MOVE) mElementsTree.remove(mNode);
    }

  public java.awt.Image getDragImage() {
    if (mNode!=null) return Palette.getElementImage(mElementsTree.getEjs(),mElementsTree.viewOf(mNode).getPlainClassname());
    return super.getDragImage();
  }

  public java.awt.Point getDragImageOffset() {
    return new java.awt.Point (15,0);
  }

  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  public boolean importData(TransferHandler.TransferSupport support) {
    if (!canImport(support)) return false;
    JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
    TreePath dest = dl.getPath();
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
    if ((mNode!=null) && (support.getDropAction() & MOVE) == MOVE) {
      int targetIndex = dl.getChildIndex();
//      System.out.println ("Move to parent "+parent+" at position : "+targetIndex);
      int index = parent.getIndex(mNode);
      if (index>=0 && index<targetIndex) {
        targetIndex--;
//        System.out.println ("position corrected to : "+targetIndex);
      }
      mElementsTree.moveTo(mNode, parent, targetIndex);
    }
    else mElementsTree.paste(parent, support.getTransferable(),dl.getChildIndex());
    return true;
  }

  public String toString() {
    return getClass().getName();
  }

}