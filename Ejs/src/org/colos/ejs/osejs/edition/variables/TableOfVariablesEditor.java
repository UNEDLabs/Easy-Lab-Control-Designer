/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.SearchResult;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.library.control.*;
import org.colos.ejs.library.control.value.*;
import org.colos.ejs.library.server.utils.models.MetadataActuatorModel;
import org.colos.ejs.library.server.utils.models.MetadataSensorModel;
import org.colos.ejs.library.server.utils.models.MetadataValueModel;
import org.colos.ejs.library.server.utils.models.MetadataSensorActuatorModel;
import org.colos.ejss.xml.SimulationXML;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.FontSizer;
import org.w3c.dom.Element;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * This class provides a page with a table to edit model variables
 * @author Francisco Esquembre
 * @version 2.0 August 2010
 *
 */
public class TableOfVariablesEditor implements Editor {
  static private final Color ERROR_COLOR = InterfaceUtils.color(Osejs.getResources().getString("EditorForVariables.ErrorColor"));
  static private final String[] COLUMN_NAMES = {"Name", "Value", "Type", "Dimension", "Domain"};
  static private String[] COLUMN_NICKNAMES;
  static private int NAME_COLUMN  = 0;
  static private int VALUE_COLUMN = 1;
  static private int TYPE_COLUMN = 2;
  static private int DIMENSION_COLUMN = 3;
  static private int DOMAIN_COLUMN = 4;

  static {
    COLUMN_NICKNAMES = new String[COLUMN_NAMES.length];
    for (int i=0; i<COLUMN_NAMES.length; i++) COLUMN_NICKNAMES[i] = Osejs.getResources().getString("VariableEditor."+COLUMN_NAMES[i]);
  }

  // Instance variables
  private Osejs ejs;
  private boolean changed = false, visible=true;
  private int activeRow = -1;  // The current active row of the table
  private String name="";
  private java.util.List<VariableData> variablesList=new ArrayList<VariableData>();

  private AbstractTableModel tableModel; // The table model
  private JTable table;  // The table itself
  private JScrollPane scrollPanel; // The scroll panel that holds th etable 
  private JTextField commentField;  // The comment field for each variable
  private JTextField pageCommentField; // The comment field for the page
  private JPanel fullPanel; // The final complete panel
  private TableColumn domainColumn;
  private JPopupMenu popupMenu;
  
  /**
   * Constructor
   * @param _ejs
   */
  public TableOfVariablesEditor (Osejs _ejs) {
    ejs = _ejs; // save the EJS instance for future use

    // The table and its scroll panel
    tableModel = new AbstractTableModel() { // Create a table model suited for our purposes
      public Class<?> getColumnClass(int columnIndex) { return String.class; } 
      public int getColumnCount() { return COLUMN_NAMES.length; } 
      public String getColumnName(int columnIndex) { return COLUMN_NICKNAMES[columnIndex]; }  
      public int getRowCount() { return variablesList.size(); } 
      public Object getValueAt(int rowIndex, int columnIndex) { return variablesList.get(rowIndex).data[columnIndex]; }  
      public boolean isCellEditable(int rowIndex, int columnIndex) { return true; }
      public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String text = aValue.toString();
        if (columnIndex==NAME_COLUMN) text = text.trim();
        variablesList.get(rowIndex).data[columnIndex] = text;
        if (columnIndex==NAME_COLUMN && rowIndex==(variablesList.size()-1)) addEmptyRow(false);
        changed = true;
      }
    };
    
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.getSelectionModel().addListSelectionListener (new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent lse) {
        ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
        int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
        if (min<0 || min!=max) setNoActiveRow (); // No selection or multiple selection
        else { // single selection
          activeRow = min;
          commentField.setText (variablesList.get(min).comment);
          commentField.setEditable (true);
        }
      }
    });
//    if (OSPRuntime.isMac() && _ejs.isSystemLookAndFeel()) table.setGridColor(Color.LIGHT_GRAY);
    if (OSPRuntime.isMac()) table.setGridColor(Color.LIGHT_GRAY);
    
    scrollPanel = new JScrollPane (table);

    // Configure cell editors
    int maxSize = Osejs.getResources().getDimension("VariablesEditor.ColumnsSize").width;
    ActionListener actionListener = new ActionListener () {
      public void actionPerformed (ActionEvent e) { 
        ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
        repaintTable();
      }
    };
    FocusListener focusListener = new FocusAdapter() {
      public void focusLost (FocusEvent e) {
        Component comp = table.getEditorComponent();
        if (comp instanceof JTextField) {
          String text = ((JTextField) comp).getText();
          tableModel.setValueAt(text, table.getEditingRow(), table.getEditingColumn());
//          System.err.println ("Focus lost while editing cell "+table.getEditingRow()+","+table.getEditingColumn()+ " for text:"+text);
        }
        ejs.getModelEditor().getVariablesEditor().updateControlValues(false); 
        repaintTable();
      }
    };
    for (int i=0; i<COLUMN_NAMES.length; i++) {
      if (i==TYPE_COLUMN) {
        JComboBox<String> combobox = new JComboBox<String>();
        combobox.setEditable(true);
        combobox.setFont(InterfaceUtils.font(null,Osejs.getResources().getString("Osejs.DefaultFont")));
        combobox.addActionListener(actionListener);
        String[] types = ResourceUtil.tokenizeString(Osejs.getSystemResources().getString("VariablesEditor.VariableTypes"));
        for (int j=0; j<types.length; j++) combobox.addItem(types[j]);
        table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(combobox));
        table.getColumnModel().getColumn(i).setPreferredWidth(maxSize);
      }
      else if (i==DOMAIN_COLUMN){
        JComboBox<String> combobox = new JComboBox<String>();
        combobox.setEditable(false);
        combobox.setFont(InterfaceUtils.font(null,Osejs.getResources().getString("Osejs.DefaultFont")));
        combobox.addActionListener(actionListener);
        String[] domains = ResourceUtil.tokenizeString(Osejs.getSystemResources().getString("VariablesEditor.VariableDomains"));
        for (int j=0; j<domains.length; j++) combobox.addItem(domains[j]);
        table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(combobox));
        table.getColumnModel().getColumn(i).setPreferredWidth(maxSize);
        domainColumn = table.getColumnModel().getColumn(i);
      }
      else {
        JTextField textField = new JTextField();
        TableCellRenderer labelRenderer;
        if (i==VALUE_COLUMN) labelRenderer = new TableCellRenderer() {
          public Component getTableCellRendererComponent(JTable _table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (variablesList.get(row)==null) return null;
            JLabel label = variablesList.get(row).renderer;
            if (value!=null) label.setText(value.toString());
            else label.setText("");
            if (variablesList.get(row).correctSyntax) {
              if (isSelected) label.setBackground(_table.getSelectionBackground());
              else label.setBackground(_table.getBackground());
            }
            else label.setBackground(ERROR_COLOR);
            return label;
          }
        };
        else labelRenderer = new DefaultTableCellRenderer();
        textField.getDocument().addDocumentListener (new MyDocumentListener(textField));
        textField.addFocusListener(focusListener);
        table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
        table.getColumnModel().getColumn(i).setCellRenderer(labelRenderer);
        if (i!=VALUE_COLUMN) table.getColumnModel().getColumn(i).setPreferredWidth(maxSize);
      }
    }


    // The popup menu for the table
    popupMenu = new JPopupMenu(); // The popup menu for the table
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("VariablesEditor.Insert")){
      public void actionPerformed(ActionEvent e) { addEmptyRow(true); }
    });
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("VariablesEditor.Add")){
      public void actionPerformed(ActionEvent e) { addEmptyRow(false); }
    });
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("Tree.MoveUp")){
      public void actionPerformed(ActionEvent e) { moveUp(); }
    });
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("Tree.MoveDown")){
      public void actionPerformed(ActionEvent e) { moveDown(); }
    });
    popupMenu.addSeparator();
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("Utils.cut-to-clipboard")){
      public void actionPerformed(ActionEvent e) { copySelection(true); }
    });
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("Utils.copy-to-clipboard")){
      public void actionPerformed(ActionEvent e) { copySelection(false); }
    });
    popupMenu.add(new AbstractAction(Osejs.getResources().getString("Utils.paste-from-clipboard")){
      public void actionPerformed(ActionEvent e) { pasteSelection(); }
    });
//    popupMenu.addSeparator();
//    popupMenu.add(new AbstractAction(Osejs.getResources().getString("VariablesEditor.Remove")){
//      public void actionPerformed(ActionEvent e) { removeRows(); }
//    });
    
    MouseListener ma = new MouseAdapter() {
      public void mousePressed (MouseEvent _evt) {
        if (OSPRuntime.isPopupTrigger(_evt) && table.isEnabled ()) popupMenu.show(_evt.getComponent(), _evt.getX(), _evt.getY());
      }
    };
    table.addMouseListener (ma); // Inside the table elements...
    scrollPanel.addMouseListener (ma); // ...and on the empty space

    // Comment fields
    commentField = new JTextField();
    commentField.setEditable (false);
    commentField.setFont(InterfaceUtils.font(null,Osejs.getResources().getString("Osejs.DefaultFont")));
    commentField.getDocument().addDocumentListener (new DocumentListener(){
      public void changedUpdate(DocumentEvent e) { reflectChange(); }
      public void insertUpdate(DocumentEvent e)  { reflectChange(); }
      public void removeUpdate(DocumentEvent e)  { reflectChange(); }
      private void reflectChange() {
        changed = true;
        if (activeRow!=-1) variablesList.get(activeRow).comment = commentField.getText();
      }
    });

    JLabel pageCommentLabel = new JLabel (Osejs.getResources().getString ("Editor.PageComment"));
    pageCommentLabel.setBorder(new EmptyBorder(0,0,0,3));
    pageCommentLabel.setFont (InterfaceUtils.font(null,Osejs.getResources().getString("Editor.DefaultFont")));

    pageCommentField = new JTextField();
    pageCommentField.setEditable (true);
    pageCommentField.setFont(InterfaceUtils.font(null,Osejs.getResources().getString("Osejs.DefaultFont")));
    pageCommentField.getDocument().addDocumentListener (new DocumentListener(){
      public void changedUpdate(DocumentEvent e) { reflectChange(); }
      public void insertUpdate(DocumentEvent e)  { reflectChange(); }
      public void removeUpdate(DocumentEvent e)  { reflectChange(); }
      private void reflectChange() {
        changed = true;
      }
    });

    JLabel commentLabel = new JLabel (Osejs.getResources().getString ("Editor.Comment"));
    commentLabel.setBorder(new EmptyBorder(0,0,0,3));
    commentLabel.setFont (InterfaceUtils.font(null,Osejs.getResources().getString("Editor.DefaultFont")));

    JPanel varCommentPanel = new JPanel (new BorderLayout());
    varCommentPanel.add (commentLabel,BorderLayout.WEST);
    varCommentPanel.add (commentField,BorderLayout.CENTER);

    JPanel pageCommentPanel = new JPanel (new BorderLayout());
    pageCommentPanel.add (pageCommentLabel,BorderLayout.WEST);
    pageCommentPanel.add (pageCommentField,BorderLayout.CENTER);

    JPanel commentPanel = new JPanel (new GridLayout(0,1));
    commentPanel.add(varCommentPanel);
    commentPanel.add(pageCommentPanel);
  
    // Hide the Domain column according to EjsOptions
    if (domainColumn!=null && ejs.getOptions().showVariablesDomain()==false) table.removeColumn(domainColumn);

    // All together now
    fullPanel = new JPanel (new BorderLayout());
//    fullPanel.setBorder (new EmptyBorder(10,0,0,0));
    fullPanel.add (scrollPanel,BorderLayout.CENTER);
    fullPanel.add (commentPanel,BorderLayout.SOUTH);

    // end of cnstructor
    setZoomLevel(FontSizer.getLevel());
  }

  public void setZoomLevel (int level) {
    FontSizer.setFonts(fullPanel, level);
    FontSizer.setFonts(popupMenu, level);
  }
  
  /**
   * Sets the visibility of the column Domain ∫
   */
  public void showDomainColumn (boolean show) {
    if (domainColumn==null) return;
    if (show) {
      table.addColumn(domainColumn);
    }
    else {
      table.removeColumn(domainColumn);
    }
    table.validate();
  }

  // ---------------------------------
  // Implementation of Editor
  // ---------------------------------

  public void setName (String _name) { name = _name; }

  public String getName () { return name; }

  public void clear () {
    variablesList.clear();
    table.getSelectionModel().clearSelection();
    pageCommentField.setText("");
    setNoActiveRow ();
    addEmptyRow(false);
  }

  public Component getComponent () { return fullPanel; }

  public void setColor (Color _color) { } // No color effects

  public void setFont (Font _font) {
    for (int i=0; i<COLUMN_NAMES.length; i++) {
      if (i==TYPE_COLUMN || i==DOMAIN_COLUMN) continue; // Not these two
      TableCellEditor editor = table.getColumnModel().getColumn(i).getCellEditor();
      if (editor instanceof DefaultCellEditor) {
        Component comp = ((DefaultCellEditor) editor).getComponent();
        if (comp!=null) comp.setFont(_font);
      }
    }
    for (VariableData varData : variablesList) varData.renderer.setFont(_font);
    //if (externalField!=null) externalField.setFont(_font);
    table.setFont(_font);
    int height = table.getFontMetrics(_font).getHeight();
    if (org.opensourcephysics.display.OSPRuntime.isMac() || ejs.isNimbusLookAndFeel()) height += 4;
    table.setRowHeight(height);
    repaintTable();
    //commentField.setFont (_font);
  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _changed) { changed = _changed; }

  public void setEditable (boolean _active) { table.setEnabled (_active); }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public void refresh (boolean _hiddensToo) { scrollPanel.setVisible (visible || _hiddensToo); }

  public void setActive (boolean _active) {
    table.setEnabled (_active);
    activeEditor=_active;
  }
  private boolean activeEditor=true, advanced=false;
  public boolean isActive () { return activeEditor;}

  public boolean isInternal() {
    return advanced;
  }

  public void setInternal(boolean _advanced) {
    advanced = _advanced;
  }
  
  static private StringBuffer printOneVariable (VariableData _varData) {
    StringBuffer buffer = new StringBuffer();  
    buffer.append("<Variable>\n");
    for (int j=0; j<COLUMN_NAMES.length; j++)
      buffer.append("<"+COLUMN_NAMES[j]+"><![CDATA["+_varData.data[j]+"]]></"+COLUMN_NAMES[j]+">\n");
    buffer.append("<Comment><![CDATA["+_varData.comment+"]]></Comment>\n");
    buffer.append("</Variable>\n");
    return buffer;
  }
  
  private void readOneVariable (String _input, int _position) {
    VariableData varData = new VariableData(table,COLUMN_NAMES.length);
    for (int i=0; i<COLUMN_NAMES.length; i++) {
      String text = OsejsCommon.getPiece(_input,"<"+COLUMN_NAMES[i]+">","</"+COLUMN_NAMES[i]+">",false);
      // backwards compatibility
      if (text != null) {
        if (text.startsWith("<![CDATA[")) text = text.substring(9, text.length() - 3); // Remove the CDATA characters
      } else if (COLUMN_NAMES[i] == "Domain"){
        text = "public";
      }
      varData.data[i] = text;
    }
    varData.comment = OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false);
    if (_position<0 || _position>=variablesList.size()) variablesList.add(varData);
    else variablesList.add(_position, varData);
  }
  
  public void fillSimulationXML(SimulationXML _simXML) {
    Element pageElement = _simXML.addModelVariablesPage(getName(),pageCommentField.getText());
    _simXML.setEnabled(pageElement, isActive());
    for (int row=0,n=variablesList.size(); row<n; row++) {
      VariableData varData = variablesList.get(row);
      String varName = varData.data[NAME_COLUMN].toString();
      if (varName.trim().length()<=0) continue;
      String type      = varData.data[TYPE_COLUMN].toString();
      String dimension = varData.data[DIMENSION_COLUMN].toString();
      String value     = varData.data[VALUE_COLUMN].toString().trim();
      String domain    = varData.data[DOMAIN_COLUMN].toString();
      _simXML.addModelVariable(pageElement, varName, type, dimension, value, varData.comment, domain);
    }
  }
  
  public StringBuffer saveStringBuffer () {
    StringBuffer txt = new StringBuffer();
    txt.append("<PageComment><![CDATA["+pageCommentField.getText()+"]]></PageComment>\n");
    for (VariableData varData : variablesList) txt.append(printOneVariable(varData));
    return txt;
  }
  
  public void readString (String _input) {
    String pageComment = OsejsCommon.getPiece(_input,"<PageComment><![CDATA[","]]></PageComment>",false);
    if (pageComment==null) pageCommentField.setText("");
    else pageCommentField.setText(pageComment);
    int begin = _input.indexOf("<Variable>");
    while (begin>=0) {
      int end = _input.indexOf("</Variable>");
      readOneVariable (_input.substring(begin+10,end),-1);
      _input = _input.substring(end+11);
      begin = _input.indexOf("<Variable>\n");
    }
    setNoActiveRow ();
    repaintTable();
  }

  public StringBuffer generateCode (int _type, String _info) {        
    if (!isActive()) return new StringBuffer();
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) return new StringBuffer();
    boolean first = true;
    StringBuffer code = new StringBuffer();
    if (_info==null) _info = "";
    else _info=Osejs.getResources().getString(_info);
    if (_type==Editor.GENERATE_JSON_VARIABLES_PUBLIC ||
        _type==Editor.GENERATE_JSON_VARIABLES_IN ||
        _type==Editor.GENERATE_JSON_VARIABLES_OUT) 
      code.append("[");
    for (int row=0,n=variablesList.size(); row<n; row++) {
      VariableData varData = variablesList.get(row);
      String varName = varData.data[NAME_COLUMN].toString();
      if (varName.trim().length()<=0) continue;
      String type      = varData.data[TYPE_COLUMN].toString();
      String dimension = varData.data[DIMENSION_COLUMN].toString();
      String value     = varData.data[VALUE_COLUMN].toString().trim();
      String domain = varData.data[DOMAIN_COLUMN].toString();
      
      if(domain.equals("input") || domain.equals("output")){
        if (_type==Editor.GENERATE_DECLARATION){
          domain = "public"; 
        }
      }
      
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(dimension,"[] ");
      int dim = tkn.countTokens();
      java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(varName,"[] ");
      int dimIndex = tknIndexes.countTokens();
      String lineOfIndexes = null;
      if (dimIndex>1) {
        varName = tknIndexes.nextToken();
        lineOfIndexes = tknIndexes.nextToken();
        while (tknIndexes.hasMoreTokens()) lineOfIndexes += ","+tknIndexes.nextToken();
        if ((dimIndex-1)!=dim) ejs.getOutputArea().println ("Syntax error in variable name "+varData.data[NAME_COLUMN].toString());
      }
      StringBuffer lineBuf = new StringBuffer();
      if (_type==Editor.GENERATE_LIST_VARIABLES) {
        lineBuf.append(type);
        for (int k=0; k<dim; k++) lineBuf.append("[]");
        lineBuf.append(":"+varName+" : "+type+dimension+" : "+domain+" : "+varData.comment+"\n");
      }
      else if (_type == Editor.GENERATE_JSON_VARIABLES_PUBLIC){
        if (domain.contains("public")){
          MetadataSensorActuatorModel jsonFill = new MetadataSensorActuatorModel("ModelVar",varName);
          MetadataValueModel jsonValue = new MetadataValueModel(true,varName,type,"",0,"",0.0,0.0);
          jsonFill.loadActuator("ModelVar",varName,varData.comment,"text",false,"application/json","application/json",
              Json.createArrayBuilder().add(jsonValue.getJSON()).build(),
              Json.createArrayBuilder().build(),
              Json.createObjectBuilder().build());
          if (!first)   code.append(",");
          else          first = false;
          lineBuf.append(jsonFill.getJSONModel());
        }
      }
      else if (_type == Editor.GENERATE_JSON_VARIABLES_IN){
        if (domain.contains("input")){
          MetadataActuatorModel jsonFill = new MetadataActuatorModel("ModelVar",varName);
          MetadataValueModel jsonValue = new MetadataValueModel(true,varName,type,"",0,"",0.0,0.0);
          jsonFill.loadActuator("ModelVar",varName,varData.comment,"text",false,"application/json","application/json",
              Json.createArrayBuilder().add(jsonValue.getJSON()).build(),
              Json.createArrayBuilder().build(),
              Json.createObjectBuilder().build());
          if (!first)   code.append(",");
          else          first = false;
          lineBuf.append(jsonFill.getJSONModel());
        }
      }
      else if (_type == Editor.GENERATE_JSON_VARIABLES_OUT){
        if (domain.contains("output")){
          MetadataSensorModel jsonFill = new MetadataSensorModel("ModelVar",varName,varData.comment,"text",false,"application/json");
          MetadataValueModel jsonValue = new MetadataValueModel(true,varName,type,"",0,"",0.0,0.0);
          jsonFill.loadSensor("ModelVar",varName,varData.comment,"text",false,"application/json",Json.createArrayBuilder().add(jsonValue.getJSON()).build(),
              Json.createArrayBuilder().build(),Json.createObjectBuilder().build());
          if (!first)   code.append(",");
          else          first = false;
          lineBuf.append(jsonFill.getJSONModel());
        }
      }
      else if (_type==Editor.GENERATE_DECLARATION) {
        lineBuf.append(" " + domain + " " + type +" "+varName+" ");
        if (dim>0) {
          for (int k = 0; k < dim; k++) lineBuf.append("[]");
        }
        else {
          if (value.length()>0) lineBuf.append(" = "+value);
          else if (type.equals("String")) lineBuf.append(" = \"\"");
          else if (type.equals("Object")) lineBuf.append(" = null");
        }
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_INITIALIZE) {
        lineBuf.append("    setValue(\""+varName+"\",_model."+varName+")");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_UPDATE_BOOLEANS) {
        lineBuf.append("  private boolean __"+varName+"_canBeChanged__ = true");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_BLOCK_VARIABLES) {
        lineBuf.append("    if (\""+varName+"\".equals(_variable)) __"+varName+"_canBeChanged__ = false");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_RESET) {
        lineBuf.append("    __"+varName+"_canBeChanged__ = true");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_UPDATE) {
        lineBuf.append("    if(__"+varName+"_canBeChanged__) setValue(\""+varName+"\",_model."+varName+")");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_LISTENERS) {
        lineBuf.append("    addListener(\""+varName+"\")"); // ,\"apply(\\\""+varName+"\\\")\")");
        lineBuf.append("; // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_VIEW_READ || _type==Editor.GENERATE_VIEW_READ_ONE) {
        if (_type==Editor.GENERATE_VIEW_READ_ONE) lineBuf.append("    if (\""+varName+"\".equals(_variable)) ");
        else lineBuf.append("    ");
        if (dim>0) {  // This is necessary to make sure that model and view
          // keep separate, independent data (specially arrays)
          String dimTxt = ""; for (int k=0; k<dim; k++) dimTxt += "[]";
          String space = "      ";
          lineBuf.append("{\n" + space+type+dimTxt+" _data = (" + type+dimTxt+") getValue(\"" + varName + "\").getObject();\n");
          dimTxt = "";
          for (int c=0; c<dim; c++, dimTxt += "[_i"+(c-1)+"]" ) {
            lineBuf.append(space+ "int _n"+c+" = _data"+dimTxt+".length;\n");
            lineBuf.append(space+ "if (_n"+c+">_model."+varName+dimTxt+".length) _n"+c+" = _model."+varName+dimTxt+".length;\n");
            lineBuf.append(space+ "for (int _i"+c+"=0; _i"+c+"<_n"+c+"; _i"+c+"++) {\n");
            space += "  ";
          }
          lineBuf.append(space+"_model."+varName+dimTxt+" = _data"+dimTxt+";\n");
          for (int k=0; k<dim; k++) {
            if (space.length()>2) space = space.substring(2);
            lineBuf.append(space+"}\n");
          }
          if (space.length()>2) space = space.substring(2);
          lineBuf.append(space+"  __"+varName+"_canBeChanged__ = true;\n");
          lineBuf.append(space+"}");
        }
        else {
          lineBuf.append("{\n");
          lineBuf.append("      _model."+varName+" = ");
          if      (type.equals("boolean")) lineBuf.append("getBoolean");
          else if (type.equals("char"))    lineBuf.append("(char) getInt");
          else if (type.equals("byte"))    lineBuf.append("(byte) getInt");
          else if (type.equals("short"))   lineBuf.append("(short) getInt");
          else if (type.equals("int"))     lineBuf.append("getInt");
          else if (type.equals("long"))    lineBuf.append("(long) getDouble");
          else if (type.equals("float"))   lineBuf.append("(float) getDouble");
          else if (type.equals("double"))  lineBuf.append("getDouble");
          else if (type.equals("String"))  lineBuf.append("getString");
          else if (type.equals("Object"))  lineBuf.append("getObject");
          else lineBuf.append("("+type+") getObject");
          lineBuf.append("(\""+varName+"\"); // "+ _info + "." + getName() + ":" + (row+1)+"\n");
          lineBuf.append("      __"+varName+"_canBeChanged__ = true;\n");
          lineBuf.append("    }");
        }
      }
      else if (_type==Editor.GENERATE_CODE) {
        if (dim==0) {
          if (value.length()>0) lineBuf.append("    "+varName +" = "+value + "; // "+ _info + "." + getName() + ":" + (row+1));
          if (value.length()>0) lineBuf.append("    "+varName +" = "+value + "; // "+ _info + "." + getName() + ":" + (row+1));
        }
        else { // it is an array
          String comment =  _info + "." + getName() + ":" + (row + 1);
          lineBuf.append("    "+varName+CodeInterpreterForJava.initCodeForAnArray (comment, lineOfIndexes, varName, type, dimension, value, ejs));
        }
      }
      else if (_type==Editor.GENERATE_DESTRUCTION) {
        if (dim>0) lineBuf.append("    " + varName + " = null;  // "+ _info + "." + getName() + ":" + (row+1));
      }
      else if (_type==Editor.GENERATE_SAVE_STATE) {
        if      (type.equals("boolean")) lineBuf.append("    _output.writeBoolean("+varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("char"))    lineBuf.append("    _output.writeChar("   +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("byte"))    lineBuf.append("    _output.writeByte("   +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("short"))   lineBuf.append("    _output.writeShort("  +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("int"))     lineBuf.append("    _output.writeInt("    +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("long"))    lineBuf.append("    _output.writeLong("   +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("float"))   lineBuf.append("    _output.writeFloat("  +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("double"))  lineBuf.append("    _output.writeDouble(" +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("String"))  lineBuf.append("    _output.writeUTF("    +varName+"); // "+ _info + "." + getName() + ":" + (row+1));
        // Objects are NOT saved
        //        else if (type.equals("Object"))  lineBuf.append("    _output.writeUTF("+name+".toString())");
        //        else                             lineBuf.append("    _output.writeUTF("+name+".toString())");
        //        lineBuf.append("; // "+ _info + "." + getName() + ":" + (i+1);
      }
      else if (_type==Editor.GENERATE_READ_STATE) {
        if      (type.equals("boolean")) lineBuf.append("    " + varName + " = _input.readBoolean(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("char"))    lineBuf.append("    " + varName + " = _input.readChar(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("byte"))    lineBuf.append("    " + varName + " = _input.readByte(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("short"))   lineBuf.append("    " + varName + " = _input.readShort(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("int"))     lineBuf.append("    " + varName + " = _input.readInt(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("long"))    lineBuf.append("    " + varName + " = _input.readLong(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("float"))   lineBuf.append("    " + varName + " = _input.readFloat(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("double"))  lineBuf.append("    " + varName + " = _input.readDouble(); // "+ _info + "." + getName() + ":" + (row+1));
        else if (type.equals("String"))  lineBuf.append("    " + varName + " = _input.readUTF(); // "+ _info + "." + getName() + ":" + (row+1));
        // Objects are NOT read
        //        else if (type.equals("Object"))  lineBuf.append("    " + name + " = _input.readUTF()");
        //        else                             lineBuf.append("    " + name + " = _input.readUTF()");
        //        lineBuf.append("; // "+ _info + "." + getName() + ":" + (i+1);
      }
      String line = lineBuf.toString();
      if (line.trim().length()>0 && (_type!=Editor.GENERATE_JSON_VARIABLES_PUBLIC  &&
          _type!=Editor.GENERATE_JSON_VARIABLES_IN  &&
          _type!=Editor.GENERATE_JSON_VARIABLES_OUT) ) code.append(line +"\n");
      else if (line.trim().length()>0 && 
          (_type==Editor.GENERATE_JSON_VARIABLES_PUBLIC ||
          _type==Editor.GENERATE_JSON_VARIABLES_IN ||
          _type==Editor.GENERATE_JSON_VARIABLES_OUT) ) code.append(line);
    }
    if ((_type==Editor.GENERATE_JSON_VARIABLES_PUBLIC ||
        _type==Editor.GENERATE_JSON_VARIABLES_IN ||
        _type==Editor.GENERATE_JSON_VARIABLES_OUT) ) code.append("]");
    return code;
  }


  // -----------------------------------
  // Evaluation of variables
  // -----------------------------------

  /**
   * Returns the initial value for a variable, if it exists. Null otherwise
   */
  public String getInitialValue (String _name) {        
    if (!isActive()) return null;
    for (int row=0,n=variablesList.size(); row<n; row++) {
      VariableData varData = variablesList.get(row);
      String varName = varData.data[NAME_COLUMN].toString();
      if (varName.trim().length()<=0) continue;
      if (_name.equals(varName)) return varData.data[VALUE_COLUMN].toString().trim();
    }
    return null;
  }

  /**
   * Whether this variable name is already in use
   * @param _name
   * @return
   */
    public boolean nameExists(String _name) {
      for (int row=0,n=variablesList.size(); row<n; row++) {
        VariableData varData = variablesList.get(row);
        String varName = varData.data[NAME_COLUMN].toString().trim();
        if (varName.equals(_name)) return true;
      }
      return false;
    }
    
  /**
   * Checks the syntax, evaluates, and passes to the control all the values of the variables in this editor
   */
  public void evaluateVariables () {
    if (!isActive()) return;
    for (int row=0,n=variablesList.size(); row<n; row++) { // I need to know the row
      VariableData varData = variablesList.get(row);
      String varName = varData.data[NAME_COLUMN].toString().trim();
      if (varName.length()<=0) continue;
      String type = varData.data[TYPE_COLUMN].toString().trim();
      String valueStr = varData.data[VALUE_COLUMN].toString().trim();
      String dimension = varData.data[DIMENSION_COLUMN].toString().trim();
      String domain = varData.data[DOMAIN_COLUMN].toString().trim();
      Value value = ejs.getModelEditor().getVariablesEditor().checkVariableValue(varName,valueStr,type,dimension);
//      if (value!=null) System.out.println ("Value of "+name+" is "+value.getClass()+" = "+value.toString());
      varData.correctSyntax = TableOfVariablesEditor.isValidType(value,type,dimension,domain);
      if (varData.correctSyntax) ejs.getViewEditor().getTree().getControl().setValue(varName,value,true); // true = is a model variable 
      tableModel.setValueAt(valueStr,row,VALUE_COLUMN); // refresh color
    }
  }

  /**
   * Updates a variable of the model with the given value. 
   * Triggered by interaction of the user with the control.
   * Returns whether the variable was found in this editor.
   */
  public boolean updateVariableInTable (final PropertyEditor _editor, final String _variable, final String _value, final Value _theValue) {
    if (!isActive()) return false;
//    if (ejs.isReading()) return false;
    for (int row=0,n=variablesList.size(); row<n; row++) {
      final VariableData varData = variablesList.get(row);
      String varName = varData.data[NAME_COLUMN].toString().trim();
      if (_variable.equals(varName)) {
        final int rowFound = row;
        Runnable runnable = new Runnable() { // If not, it could block the system if two variables are changed in the same interaction
          public void run() {
            if (Value.parseConstantOrArray(varData.data[VALUE_COLUMN].toString().trim(),true)==null) {
              return; // Expressions can not be overwritten
            }
            if (!varData.changeAllowed) return; // If the user said no, then it is no
            if (!varData.changeWarned) { // && !TabbedVariablesEditor.isAConstant (null,null,varData.data[VALUE_COLUMN].toString())) {
              varData.changeWarned = true;
              Object[] options =  { Osejs.getResources().getString("EditorFor.Ok"), Osejs.getResources().getString("EditorFor.Cancel")};
              String element = _editor.getName();
              String text = element==null ?  "" : element+": ";
              text += Osejs.getResources().getString("VariablesEditor.ValueIsExpression")+" "+_variable;
              Component comp = fullPanel.getWidth()<=0 ? null : fullPanel;
              int option = JOptionPane.showOptionDialog(comp,text,Osejs.getResources().getString("Warning"),
                  JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
              if (option==1) {
                varData.changeAllowed = false;
                return; // true;
              }
              varData.changeAllowed = true;
            }
            String varType = varData.data[TYPE_COLUMN].toString().trim();
            if (varType.equals("int")) tableModel.setValueAt(Integer.toString(_theValue.getInteger()),rowFound,VALUE_COLUMN);
            else tableModel.setValueAt (_value,rowFound,VALUE_COLUMN);
            repaintTable();
          }
        };
        if (javax.swing.SwingUtilities.isEventDispatchThread()) runnable.run();
        else try {
          SwingUtilities.invokeAndWait(runnable); // If not, it could block the system if two variables are changed in the same interaction
        } catch (Exception exc) { exc.printStackTrace(); }
        return true;
      }
    }
    return false;
  }

  /**
   * Whether the given Value is of the correct type
   */
  static public boolean isValidType (Value value, String type, String _dimension, String _domain) {
    if (value==null) return false;
    boolean isCorrect;
    //if (_domain.equals("protected") || _domain.equals("public")) {
      if (_dimension.indexOf("[") >= 0) isCorrect = value instanceof ObjectValue;
      else if (type.equals("boolean")) isCorrect = value instanceof BooleanValue;
      else if (type.equals("int")) isCorrect = value instanceof IntegerValue;
      else if (type.equals("char")) isCorrect = value instanceof IntegerValue;
      else if (type.equals("double")) isCorrect = value instanceof IntegerValue || value instanceof DoubleValue;
      else if (type.equals("String")) isCorrect = value instanceof StringValue;
      else if (type.equals("Object")) isCorrect = value instanceof ObjectValue;
      else if (type.equals("byte")) isCorrect = value instanceof IntegerValue;
      else if (type.equals("short")) isCorrect = value instanceof IntegerValue;
      else if (type.equals("long")) isCorrect = value instanceof IntegerValue;
      else if (type.equals("float")) isCorrect = value instanceof IntegerValue || value instanceof DoubleValue;
      else isCorrect = value instanceof ObjectValue;
//    } else isCorrect = false;
    return isCorrect;
  }

  // -----------------------------------
  // Private methods
  // -----------------------------------

  private void repaintTable() {
    ListSelectionModel lsm = table.getSelectionModel();
    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
    tableModel.fireTableDataChanged();  
    table.getSelectionModel().setSelectionInterval(min,max);
  }
  
  private void setNoActiveRow () {
    activeRow = -1;
    commentField.setText ("");
    commentField.setEditable (false);
  }
  
  private void addEmptyRow (boolean before) {
    VariableData varData = new VariableData(table,COLUMN_NAMES.length);
    ListSelectionModel lsm = table.getSelectionModel();
    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
    if (max>=0 && max<variablesList.size()) {
      varData.data[TYPE_COLUMN] = variablesList.get(max).data[TYPE_COLUMN];
      varData.data[DOMAIN_COLUMN] = variablesList.get(max).data[DOMAIN_COLUMN];
    }
    synchronized(variablesList) {
      if (before) {
        if (min<0) min = 0;
        variablesList.add(min,varData);
        max = min;
      }
      else {
        max++;
        variablesList.add(max,varData);
        min = max;
      }
    }
    activeRow = min;
    commentField.setText (variablesList.get(activeRow).comment);
    commentField.setEditable (true);
    lsm.setSelectionInterval(min, max);
    repaintTable();
  }

  private void moveUp () {
    ListSelectionModel lsm = table.getSelectionModel();
    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
    if (min<1) return; // No rows selected or first row
    VariableData varData = variablesList.get(min-1);
    variablesList.add(max+1,varData);
    variablesList.remove(min-1);
    table.getSelectionModel().setSelectionInterval(min-1,max-1);
    ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
    repaintTable();
  }

  private void moveDown () {
    ListSelectionModel lsm = table.getSelectionModel();
    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
    if (min<0) return; // No rows selected
    if (max>=variablesList.size()-1) return; // last row
    VariableData varData = variablesList.get(max+1);
    variablesList.remove(max+1);
    variablesList.add(min,varData);
    table.getSelectionModel().setSelectionInterval(min+1,max+1);
    ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
    repaintTable();
  }

  private void copySelection (boolean _remove) {
    ListSelectionModel lsm = table.getSelectionModel();
    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
    if (min<0) return; // No rows selected
    StringBuffer fragment = new StringBuffer();
    fragment.append("<VariablesFragment>\n");
    for (int row=min; row<=max; row++) fragment.append(printOneVariable(variablesList.get(row)));
    fragment.append("</VariablesFragment>\n");
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    VariablesSelection selection = new VariablesSelection(fragment.toString());
    theClipboard.setContents(selection,null);
    if (_remove) {
      for (int row=max; row>=min; row--) variablesList.remove(row);
      lsm.clearSelection();
      setNoActiveRow ();
      ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
      repaintTable();
    }
  }

//  private void removeRows () {
//    ListSelectionModel lsm = table.getSelectionModel();
//    int min = lsm.getMinSelectionIndex(), max = lsm.getMaxSelectionIndex();
//    if (min<0 || max>tableModel.getRowCount()) return; // No rows selected
//    for (int i=max; i>=min; i--) variablesList.remove(i);
//    lsm.clearSelection();
//    setNoActiveRow ();
//    ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
//    repaintTable();
//  }
  
  private void pasteSelection () {
    Clipboard theClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = theClipboard.getContents(null);
    DataFlavor flavor = VariablesSelection.variablesFlavor;
    if (contents.isDataFlavorSupported(flavor)) {
      try {
        String input = (String) contents.getTransferData(flavor); 
        ListSelectionModel lsm = table.getSelectionModel();
        int max = lsm.getMaxSelectionIndex();
        if (max<0) max = tableModel.getRowCount();
        else max++;
        int min = max;
        int begin = input.indexOf("<Variable>");
        while (begin>=0) {
          int end = input.indexOf("</Variable>");
          readOneVariable (input.substring(begin+10,end),max);
          input = input.substring(end+11);
          begin = input.indexOf("<Variable>\n");
          max++;
        }
        table.getSelectionModel().setSelectionInterval(min, max-1);
        setNoActiveRow ();
        ejs.getModelEditor().getVariablesEditor().updateControlValues(false);
        repaintTable();
      }
      catch (Exception exc) { exc.printStackTrace(); };
    }
  }

  // ---------------------------------
  // Searching
  // ---------------------------------

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    if (_info==null) _info = "";
    else _info=Osejs.getResources().getString(_info);//FKH 021020
    for (int col=0, numCols=COLUMN_NAMES.length; col<numCols; col++) {
      if (col==TYPE_COLUMN || col==DOMAIN_COLUMN) continue;
      String addInfo = getName()+" ("+COLUMN_NICKNAMES[col]+")";
      for (int row=0,numRows=variablesList.size(); row<numRows; row++) {
        String text = variablesList.get(row).data[col].toString();
        SearchResult sr = searchInString(_info+"."+addInfo, _searchString,toLower,text,row,col);
        if (sr!=null) list.add(sr);
      }
    }
    return list;
  }

  private SearchResult searchInString(String _info, String _searchString, boolean toLower, String line, int row, int column)  {
    int index;
    if (toLower) index = line.toLowerCase().indexOf(_searchString);
    else index = line.indexOf(_searchString);
    if (index>=0) return new RowSearchResult(_info,line.trim(),row,column);
    return null;
  }


  private class RowSearchResult extends SearchResult {
    // lineNumber is the row number
    // caretPosition is the column
    public RowSearchResult (String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation,aText,null,aLineNumber,aCaretPosition);
    }

    public void show () {
      ejs.getModelEditor().getVariablesEditor().showPage(TableOfVariablesEditor.this);
      table.requestFocusInWindow();
      table.setRowSelectionInterval(lineNumber,lineNumber);
      table.setColumnSelectionInterval(caretPosition,caretPosition);
    }
  }

  // -----------------------------------
  // Private classes
  // -----------------------------------

  static private class VariableData {
    Object[] data;
    String comment;
    JLabel renderer;
    boolean correctSyntax;
    boolean changeWarned=false;
    boolean changeAllowed=true;

    VariableData (JTable _table, int _columns) {
      data = new Object[_columns];
      for (int i=0; i<_columns; i++) data[i] = "";
      data[TYPE_COLUMN] = "double";
      data[DOMAIN_COLUMN] = "public";
      comment = "";
      renderer = new DefaultTableCellRenderer();
      renderer.setFont(_table.getFont());
      correctSyntax = true;
    }
    
  }

  private class MyDocumentListener implements DocumentListener {
    JComponent component;
    
    MyDocumentListener(JComponent comp) { component = comp; }
    public void changedUpdate(DocumentEvent e) { changed = true; component.requestFocus(); }
    public void insertUpdate(DocumentEvent e)  { changed = true; component.requestFocus(); }
    public void removeUpdate(DocumentEvent e)  { changed = true; component.requestFocus(); }
  };

} // end of class

