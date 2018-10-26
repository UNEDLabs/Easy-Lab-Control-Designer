/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.*;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.w3c.dom.*;

public class DigitalLibraryComPADRE implements DigitalLibrary {
  static private final String SERVER_TREE   ="http://www.compadre.org/osp/services/REST/osp_jars.cfm?verb=Identify&OSPType=EJS%20Model&AttachedDocument=Source%20Code";
  static private final String SERVER_RECORDS="http://www.compadre.org/osp/services/REST/osp_jars.cfm?OSPType=EJS%20Model&AttachedDocument=Source%20Code";
  static private final String SERVER_SEARCH ="http://www.compadre.org/osp/services/REST/search_v1_02.cfm?verb=Search&OSPType=EJS%20Model&Skip=0&Max=30&q=";
  
  static private final String JS_SERVER_TREE   ="http://www.compadre.org/services/rest/osp_ejss.cfm?verb=Identify&OSPType=EJSS+Model";
  static private final String JS_SERVER_RECORDS="http://www.compadre.org/osp/services/REST/osp_ejss.cfm?OSPType=EJSS+Model";
  static private final String JS_SERVER_SEARCH ="http://www.compadre.org/osp/services/REST/search_v1_02.cfm?verb=Search&OSPType=EJSS+Model&Skip=0&Max=30&q=";
  
    
  static private final Icon SEARCH_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/find.gif");      // This icon is bundled with EJS
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);
  
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final String authorTitle = res.getString("EjsOptions.Author");
  static private final String sizeTitle = res.getString("DigitalLibrary.DownloadSize");
  static private final String infoFieldTitle = res.getString("DigitalLibrary.InfoField");

  static private String sDbClickStr;
  static private String sCatStr;

  static {
    sDbClickStr = "<p>"+res.getString("DigitalLibrary.DoubleClick")+" ";
    sCatStr = res.getString("DigitalLibrary.Category");
    if (sCatStr.trim().length()>0) sCatStr = " "+ sCatStr+".</p>";
    else sCatStr = sCatStr+".</p>";
  }
  
  private Osejs mEjs;
  private JCheckBox mainClassificationOnlyCB;
  private JTextField mSearchField;
  private JPanel mPanel;
  private String mLastProgrammingLanguage=null;
  
  public DigitalLibraryComPADRE(Osejs _ejs) {
    mEjs = _ejs;
    mainClassificationOnlyCB  = new JCheckBox(res.getString("DigitalLibrary.DuplicateClassification"),true);

    ActionListener actionListener = new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String value = mSearchField.getText().trim();
        if (value.length()>0) {
//          System.err.println ("Search for <"+value+">");
          DigitalLibraryUtils.createNodes(mEjs, DigitalLibraryComPADRE.this, value);
        }
      }
    };
    
    JLabel searchLabel  = new JLabel(res.getString("SearchDialog.Search")+":",SwingConstants.RIGHT);
    searchLabel.setToolTipText(res.getString("DigitalLibrary.SearchTooltip"));
    searchLabel.setBorder(LABEL_BORDER);

    mSearchField = new JTextField();
    mSearchField.setToolTipText(res.getString("DigitalLibrary.SearchTooltip"));
    mSearchField.addActionListener(actionListener);
    
    JButton searchButton = new JButton(SEARCH_ICON);
    searchButton.addActionListener(actionListener);
    searchButton.setToolTipText(res.getString("Compadre.SearchToolTip"));

    JPanel searchPanel = new JPanel (new BorderLayout());
    searchPanel.add(searchLabel,BorderLayout.WEST);
    searchPanel.add(mSearchField,BorderLayout.CENTER);
    searchPanel.add(searchButton,BorderLayout.EAST);
    
    mPanel = new JPanel (new BorderLayout());
    mPanel.add(mainClassificationOnlyCB,BorderLayout.EAST);
    mPanel.add(searchPanel,BorderLayout.CENTER);
  }
  
  
  public String toString() { return "OSP collection in the comPADRE digital library"; }

  public javax.swing.JComponent getAdditionalComponent() {
    return mPanel; 
  }

  public void clearSearch() {
    mSearchField.setText("");
  }
  
  public DefaultMutableTreeNode getRootNode(Osejs _ejs) {
    DigitalLibraryNode node = new DigitalLibraryNode (this,"OSP Digital Library Models","OSP collection in the comPADRE digital library",null,null);
    try { node.setInfoURL(new java.net.URL("http://www.compadre.org/OSP/online_help/EjsDL/DLModels.html")); } 
    catch (Exception exc2) {}
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(node); 
    DigitalLibraryNode firstLeaf = new DigitalLibraryNode(this,"About OSP and comPADRE" ,null,null,null);
    try { firstLeaf.setInfoURL(new java.net.URL("http://www.compadre.org/OSP/online_help/EjsDL/OSPCollection.html")); }
    catch (Exception exc2) {}
    firstLeaf.setToBeExpanded(false);
    firstLeaf.setHTMLnode(true);
    rootNode.add(new DefaultMutableTreeNode(firstLeaf));
    return rootNode;
  }
  
//  static private int counter = 0;
  
  public int getCatalog(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, String _programmingLanguage) {
    int nModels=0;
    mLastProgrammingLanguage = _programmingLanguage;
    try {
//      counter = 0;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      String urlStr = mEjs.supportsJava() ? SERVER_TREE : JS_SERVER_TREE;
      String urlStr;
      if (_programmingLanguage.equals(OsejsCommon.PROGRAMMING_JAVA)) urlStr = SERVER_TREE;
      else if (_programmingLanguage.equals(OsejsCommon.PROGRAMMING_JAVASCRIPT)) urlStr = JS_SERVER_TREE;
      else urlStr = JS_SERVER_TREE;
      
      if (mainClassificationOnlyCB.isSelected()) urlStr += "&OSPPrimary=Subject";
      URL url = new URL(urlStr);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
      NodeList list = doc.getElementsByTagName("Identify");
      for (int i=0,n=list.getLength(); i<n; i++) {
        nModels += addSubtrees(_treeModel,_parentNode, list.item(i).getChildNodes(),"osp-subject",1,"");
//        if (counter>5) return true;
      }
      return nModels;
    } 
    catch(Exception e) { e.printStackTrace(); }
    return -1;
  }
    
//  public boolean canSearch() { return true; }

  public int searchCatalog(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, String _searchString, String _programmingLanguage) { 
    mLastProgrammingLanguage = _programmingLanguage;
    if (_searchString==null || _searchString.length()<0) {
//      System.err.println ("No search string <"+_searchString+">");
      return -1;
    }
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      String urlStr = ( mEjs.supportsJava() ? SERVER_SEARCH : JS_SERVER_SEARCH) + FileUtils.correctUrlString(_searchString);
      String urlStr;
      if (_programmingLanguage.equals(OsejsCommon.PROGRAMMING_JAVA)) urlStr = SERVER_SEARCH;
      else if (_programmingLanguage.equals(OsejsCommon.PROGRAMMING_JAVASCRIPT)) urlStr = JS_SERVER_SEARCH;
      else urlStr = JS_SERVER_SEARCH;
      urlStr += FileUtils.correctUrlString(_searchString);

//      System.err.println ("Real search for <"+urlStr+">");
//      if (mainClassificationOnlyCB.isSelected()) urlStr += "&OSPPrimary=Subject";
      URL url = new URL(urlStr);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
      NodeList list = doc.getElementsByTagName("record");
//      System.err.println ("Found "+list.getLength()+" records!");

      int added = 0;
      for (int i=0,n=list.getLength(); i<n; i++) {
        if (list.item(i) instanceof Element) {
          if (addRecord(_treeModel,_parentNode, (Element) list.item(i),"Computer Program")) added++;;
        }
      }
      return added;
    } 
    catch(Exception e) { e.printStackTrace(); }
    return -1;
  }
  
  /**
   * Tries to add a record
   * @param _treeModel
   * @param _parentNode
   * @param _record
   * @param _type
   * @return true if added
   */
  private boolean addRecord(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, Element _record, String _type) {

    String name = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record,"title")); 
//    System.err.println ("Record Found <"+name+">");
    String itemType = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record,"item-type"));
    if (!_type.equals(itemType)) {
//      System.err.println ("Not right type record : "+name + " : "+itemType);
      return false;
    }

    String filename = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record,"file-name"));
    String[] attachment = getAttachment ( _record.getChildNodes(),filename);
    if (attachment==null || attachment[0]==null) {
//      System.err.println ("Not source code for this record : "+name + " : "+itemType);
      return false; // No source code for this record
    }

    // Extract information
//    for (int i=0,n=attachment.length; i<n; i++) System.err.println ("Attachment["+i+"] = " +attachment[i]);
    String downloadURL = DigitalLibraryUtils.processURL(attachment[0]+"&EJSSource=1"); 
    String description = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record,"description"));
    String infoField = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record, "information-url"));
    String thumbnailURL = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(_record, "thumbnail-url"));

    String authorField = "";
    for (Node node : DigitalLibraryUtils.getAllNodes(DigitalLibraryUtils.getNode(_record,"contributors"),"contributor")) {
      Element el = (Element) node;
      if ("Author".equals(el.getAttribute("role"))) authorField += DigitalLibraryUtils.getNodeValue(node)+", ";
    }
    if (authorField.endsWith(", ")) authorField = authorField.substring(0,authorField.length()-2);
    
    // Create the tree node
    StringBuffer buffer = new StringBuffer();
    buffer.append ("<p align=\"center\"><img src=\""+thumbnailURL+"\" alt=\""+name+"\"></p>");
    buffer.append ("<h2>"+name+"</h2>");
    if (authorField.length()>0) buffer.append ("<p><b>"+authorTitle+":</b> "+ authorField+"</p>");
    if (description!=null) {
      StringTokenizer tkn = new StringTokenizer(description,"\n");
      while (tkn.hasMoreTokens()) buffer.append("<p>"+tkn.nextToken()+"</p>");
    }
    buffer.append ("<p><b>"+infoFieldTitle+"</b><br><a href=\""+infoField+"\">"+infoField+"</a></p>");
    buffer.append ("<p><b>"+sizeTitle+"</b> "+attachment[1]+" bytes</p>");
    DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(new DigitalLibraryNode(this,name,buffer.toString(),downloadURL,attachment[2]));
    _treeModel.insertNodeInto(modelNode, _parentNode, _parentNode.getChildCount());
    return true;
  }

 private int addSubtrees(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, NodeList _list,String _type, int _depth, String _serviceParameter) {
    DigitalLibraryNode dlNode = (DigitalLibraryNode) _parentNode.getUserObject();
    int nModels = 0;
    
    for (int i=0,n=_list.getLength(); i<n;i++) {
//      if (counter>5) return;

      if (! (_list.item(i) instanceof Element)) continue;
      Element node = (Element) _list.item(i);
//      System.out.println ("Depth = "+_depth+" node = "+node.getNodeName()+ " type = "+node.getAttribute("type"));
      if (node.getNodeName().equals("sub-tree-set") && _type.equals(node.getAttribute("type")) ) {
        List<Node> subTrees = DigitalLibraryUtils.getAllNodes(node, "sub-tree"); //node.getChildNodes();
        StringBuffer listBuffer = null;
        if (subTrees.size()>0) {
          listBuffer = new StringBuffer();
          listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfSubcategories")+" "+dlNode+":</p>\n");
          listBuffer.append("<ul>\n");
        }
        String unclassifiedNodeURL = null;
        if (listBuffer!=null) for (int j=0,m=subTrees.size();j<m;j++) {
          if (! (subTrees.get(j) instanceof Element)) continue;
          Element subtree = (Element) subTrees.get(j);
          String name = subtree.getAttribute("name");
          String serviceParam = subtree.getAttribute("service-parameter");
          serviceParam = _serviceParameter+"&"+FileUtils.correctUrlString(serviceParam);
          DigitalLibraryNode nodeInfo = new DigitalLibraryNode (this,name,name,null,null);
          if (name.equals("Unclassified")) { // The unclassified node is processed last and adds its models to the parent
//            System.out.println ("Unclassified in "+_parentNode);
            unclassifiedNodeURL = serviceParam;
            continue;
          }
          listBuffer.append("<li>"+name+"</li>\n");
          nodeInfo.setIsTree(true);
          String description = DigitalLibraryUtils.getNodeValue(subtree,"description");
          if (description!=null) nodeInfo.setUserString("<p>"+description+"</p>");
          DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(nodeInfo);
          if (DigitalLibraryUtils.getAllNodes(subtree,"sub-tree-set").size()<=0) { // has no subtree: i.e. it is a final node with models
            nodeInfo.setToBeExpanded(true);
            if (nodeInfo.getUserString()==null) nodeInfo.setDescription(sDbClickStr+name+sCatStr);
            else nodeInfo.setDescription(nodeInfo.getUserString()+sDbClickStr+name+sCatStr);
            nodeInfo.setExpansionInfo(serviceParam);
            if (DigitalLibraryUtils.EXPAND_ALL) {
              DigitalLibraryUtils.expandNode(childNode);
//              counter++;
            }
          }
          _treeModel.insertNodeInto(childNode, _parentNode, _parentNode.getChildCount());
          nModels++;
          nModels += addSubtrees(_treeModel,childNode,subtree.getChildNodes(),_type+"-detail",_depth+1,serviceParam);
        }
        if (listBuffer!=null) listBuffer.append("</ul>\n");
        if (unclassifiedNodeURL!=null) {
          dlNode.setToBeExpanded(true);
          dlNode.setExpansionInfo(unclassifiedNodeURL);
          if (DigitalLibraryUtils.EXPAND_ALL) {
            DigitalLibraryUtils.expandNode(_parentNode);
//            counter++;
          }
        }
        if (listBuffer!=null) {
          String newUserString = dlNode.getUserString()==null? listBuffer.toString() :  dlNode.getUserString()+listBuffer.toString();
          dlNode.setUserString(newUserString);
          if (unclassifiedNodeURL!=null) dlNode.setDescription(newUserString+sDbClickStr+dlNode.toString()+sCatStr);
          else dlNode.setDescription(newUserString);
        }
      } 
    }
    return nModels;
  }
    
  public void expandNode(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _treeNode, javax.swing.JEditorPane _htmlEditor) {
    DigitalLibraryNode dlNode = (DigitalLibraryNode) _treeNode.getUserObject();
    if (!dlNode.isToBeExpanded()) return;
    dlNode.setToBeExpanded(false);
    // Read the nodes and add them to this node
    List<DefaultMutableTreeNode> modelList = getModels (_treeModel, _treeNode, dlNode.getExpansionInfo()); 
    if (modelList.size()>0) { // Create an html page with a listing of models for this node
      StringBuffer listBuffer = new StringBuffer();
      if (dlNode.getUserString()!=null) listBuffer.append(dlNode.getUserString());
      String catStr = res.getString("DigitalLibrary.Category");
      if (catStr.trim().length()>0) catStr = " "+ catStr;
      listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfModels")+" "+dlNode.toString()+catStr+":</p>\n");
      listBuffer.append("<ul>\n");
      for (DefaultMutableTreeNode model : modelList) listBuffer.append("<li>"+model.toString()+"</li>\n");
      listBuffer.append("</ul>\n");
      dlNode.setDescription(listBuffer.toString());
    }
    else { // default html page
      if (dlNode.getUserString()!=null) dlNode.setDescription(dlNode.getUserString());
      else dlNode.setDescription(dlNode.toString());
    }
    _htmlEditor.setText(dlNode.getDescription());
  }

  /**
   * Returns a list of tree nodes for the models at a given location
   * @param _parentNode The node at which the model will be added
   * @param _urlString The url that gives the list of models
   * @return
   */
  private List<DefaultMutableTreeNode> getModels (javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, String _urlString) {
    List<DefaultMutableTreeNode> modelList = new ArrayList<DefaultMutableTreeNode>();
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//      String urlBase = mEjs.supportsJava() ? SERVER_RECORDS : JS_SERVER_RECORDS;
      String urlBase;
      if (mLastProgrammingLanguage.equals(OsejsCommon.PROGRAMMING_JAVA)) urlBase = SERVER_RECORDS;
      else if (mLastProgrammingLanguage.equals(OsejsCommon.PROGRAMMING_JAVASCRIPT)) urlBase = JS_SERVER_RECORDS;
      else urlBase = JS_SERVER_RECORDS;

      if (mainClassificationOnlyCB.isSelected()) urlBase += "&OSPPrimary=Subject";
//System.out.println ("Command is "+urlBase+_urlString);
      URL url = new URL(urlBase+_urlString);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());

      // Construct the full list location of the model
      String parentList = "";
      javax.swing.tree.TreeNode rootNode = _parentNode;
      while (rootNode!=null) {
        if (rootNode.getParent()!=null) parentList = rootNode.toString()+": " + parentList;
        rootNode = rootNode.getParent();
      }
      
      NodeList list = doc.getElementsByTagName("record");
      for (int i=0,n=list.getLength(); i<n; i++) { // Process records
        Node record = list.item(i);
        String filename = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"file-name"));
        NodeList childrenList = record.getChildNodes();
        String[] attachment = getAttachment (childrenList,filename);
        if (attachment==null || attachment[0]==null) continue; // No source code for this record
        // Extract information
        String name = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"title")); 
        String downloadURL = DigitalLibraryUtils.processURL(attachment[0]); 
        String description = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"description"));
        String infoField = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"information-url"));
        String thumbnailURL = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"thumbnail-url"));
        String authorField = "";
        for (Node node : DigitalLibraryUtils.getAllNodes(DigitalLibraryUtils.getNode(record,"contributors"),"contributor")) {
          Element el = (Element) node;
          if ("Author".equals(el.getAttribute("role"))) authorField += DigitalLibraryUtils.getNodeValue(node)+", ";
        }
        if (authorField.endsWith(", ")) authorField = authorField.substring(0,authorField.length()-2);
        
        for (Node node : DigitalLibraryUtils.getAllNodes(DigitalLibraryUtils.getNode(record,"attached-document"),"file-type")) {
          Element el = (Element) node;
          if ("Source Code".equals(el.getAttribute("role"))) authorField += DigitalLibraryUtils.getNodeValue(node)+", ";
        }
        
        // Create the tree node
        StringBuffer buffer = new StringBuffer();
        buffer.append ("<p align=\"center\"><img src=\""+thumbnailURL+"\" alt=\""+name+"\"></p>");
        buffer.append ("<p><b>"+parentList+"</b></p>");
        buffer.append ("<h2>"+name+"</h2>");
        if (authorField.length()>0) buffer.append ("<p><b>"+authorTitle+":</b> "+ authorField+"</p>");
        if (description!=null) {
          StringTokenizer tkn = new StringTokenizer(description,"\n");
          while (tkn.hasMoreTokens()) buffer.append("<p>"+tkn.nextToken()+"</p>");
        }
        buffer.append ("<p><b>"+infoFieldTitle+"</b><br><a href=\""+infoField+"\">"+infoField+"</a></p>");
        buffer.append ("<p><b>"+sizeTitle+"</b> "+attachment[1]+" bytes</p>");
        DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(new DigitalLibraryNode(this,name,buffer.toString(),downloadURL,attachment[2]));
//        _parentNode.add(modelNode);  // Using this causes redisplay problems
        _treeModel.insertNodeInto(modelNode, _parentNode, _parentNode.getChildCount());
//        System.out.println ("Added node "+modelNode+" to parent "+_parentNode);
        modelList.add(modelNode);

      }
    }
    catch(Exception e) { e.printStackTrace(); }
    return modelList;
  }
  
  /**
   * Returns two strings with the source code attachment or null if no such attachment is found
   * @param childrenList
   * @return
   */
  static private String[] getAttachment (NodeList _childrenList, String correctFilename) {
    if (correctFilename!=null) {
      if (correctFilename.startsWith("ejs_model_")) correctFilename = correctFilename.substring(10);
      else if (correctFilename.startsWith("ejss_model_")) correctFilename = correctFilename.substring(11);
      else correctFilename = null;
    }
//    System.err.println ("Correct filename = "+correctFilename);
    for (int i=0,n=_childrenList.getLength(); i<n; i++) {
      Node child = _childrenList.item(i);
      if (!child.getNodeName().equals("attached-document")) continue;
      Node fileTypeNode = DigitalLibraryUtils.getNode(child,"file-type");
      if (fileTypeNode!=null && "Source Code".equals(DigitalLibraryUtils.getNodeValue(fileTypeNode))) {
        Node urlNode = DigitalLibraryUtils.getNode(child,"download-url");
        if (urlNode==null) urlNode = DigitalLibraryUtils.getNode(child,"access-url");
        if (urlNode!=null) {
          String attachmentURL = DigitalLibraryUtils.getNodeValue(urlNode);
          Element filenameNode = (Element) DigitalLibraryUtils.getNode(child,"file-name");
          if (filenameNode!=null) {
            String filename = DigitalLibraryUtils.getNodeValue(filenameNode);
            if (correctFilename!=null) {
              if (!filename.endsWith(correctFilename)) {
//                System.err.println("Attachment "+filename+" does not match!");
                continue;  
              }
//              System.err.println("Attachment "+filename+" DOES match!");
            }
            return new String[] { attachmentURL, filenameNode.getAttribute("file-size"), filename };
          }
          return new String[] { attachmentURL,null,null } ;
        }
      }
    }
    return null;
  }

//  /**
//   * Returns two strings with the source code attachment or null if no such attachment is found
//   * @param childrenList
//   * @return
//   */
//  static private String[] getSearchAttachment (Element _record) {
//    Node attachmentsNode = DigitalLibraryUtils.getNode(_record,"attachedDocuments");
//    if (attachmentsNode==null) return null;
//    NodeList childrenList = attachmentsNode.getChildNodes();
//
//    for (int i=0,n=childrenList.getLength(); i<n; i++) {
//      Node child = childrenList.item(i);
//      if (!child.getNodeName().equals("document")) continue;
//      Node fileTypeNode = DigitalLibraryUtils.getNode(child,"type");
//      if (fileTypeNode!=null && "Source Code".equals(DigitalLibraryUtils.getNodeValue(fileTypeNode))) {
//        Node urlNode = DigitalLibraryUtils.getNode(child,"access-url");
//        if (urlNode!=null) {
//          String attachmentURL = DigitalLibraryUtils.getNodeValue(urlNode)+"&EJSSource=1";
//          Element filenameNode = (Element) DigitalLibraryUtils.getNode(child,"file-name");
//          if (filenameNode!=null) {
//            String filename = DigitalLibraryUtils.getNodeValue(filenameNode);
//            return new String[] { attachmentURL, filenameNode.getAttribute("file-size"), filename }; // filenameNode.getAttribute("file-size")
//          }
//          return new String[] { attachmentURL, "???", null } ;
//        }
//      }
//    }
//    return null;
//  }

}

