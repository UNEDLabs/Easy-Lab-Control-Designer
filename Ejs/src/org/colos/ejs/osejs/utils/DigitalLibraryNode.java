/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import java.net.URL;

import javax.swing.tree.DefaultMutableTreeNode;

public class DigitalLibraryNode {
    private DigitalLibrary digitalLibrary;
    private String description, name, downloadURL, expansionInfo, userString, filename;
    private URL infoURL;
    private boolean toBeExpanded, isTree, isHTMLdocument;
    
    DigitalLibraryNode (DigitalLibrary _dl, String _name, String _descr, String _url, String _filename) {
      digitalLibrary = _dl;
      this.name = _name; 
      this.description = _descr;
      this.downloadURL = _url; 
      this.filename = _filename;
      toBeExpanded = false;
    }

    /**
     * 
     */
    public DigitalLibrary getDigitalLibrary() { return digitalLibrary; }
    
    /**
     * Sets the info URL for this node
     * @param _url
     */
    public void setInfoURL(URL _url) { infoURL = _url; }

    /**
     * The name that will appear on the node
     */
    public String toString() { return name; }

    /**
     * The URL of the page that will appear when the node is selected
     * @return
     */
    public URL getInfoURL() { return infoURL; }

    /**
     * Sets a user string 
     * @param _desc
     */
    public void setUserString(String _string) { this.userString = _string; }

    /**
     * Returns the user string
     * @return
     */
    public String getUserString() { return userString; }

    /**
     * Returns the file name
     * @return
     */
    public String getFilename() { return filename; }

    
    /**
     * Sets the description text for this node
     * @param _desc
     */
    public void setDescription(String _desc) { this.description = _desc; }

    /**
     * Alternative description if the URL is not valid
     * @return
     */
    public String getDescription() { return description; }

    /**
     * Download URL for a final node
     * @return
     */
    public String getDownloadURL() { return downloadURL; }
    
    /**
     * Whether this node is a tree that needs to be expanded
     * @param _expand
     */
    public void setToBeExpanded(boolean _expand) { toBeExpanded = _expand; }

    /**
     * Whether this node is a tree that needs to be expanded
     */
    public boolean isToBeExpanded() { return toBeExpanded; }
    
    /**
     * Whether this node is not a model, but an informative html node
     * @param _expand
     */
    public void setHTMLnode(boolean _html) { isHTMLdocument = _html; }

    /**
     * Whether this node is a tree that needs to be expanded
     */
    public boolean isHTMLnode() { return isHTMLdocument; }

    /**
     * Expands a node not previously read
     */
    public void expand(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _node, javax.swing.JEditorPane _htmlEditor) { 
      this.digitalLibrary.expandNode(_treeModel, _node,_htmlEditor); }

    /**
     * Information required to expand the node
     */
    public String getExpansionInfo() { return this.expansionInfo; }

    /**
     * Information required to expand the node
     */
    public void setExpansionInfo(String _info) { this.expansionInfo = _info; }

    /**
     * Whether it is a tree node
     * @param _tree
     */
    public void setIsTree(boolean _tree) { isTree = _tree; }

    /**
     * Whether it is a tree node
     * @return boolean
     */
    public boolean isTree() { return isTree; }


}

