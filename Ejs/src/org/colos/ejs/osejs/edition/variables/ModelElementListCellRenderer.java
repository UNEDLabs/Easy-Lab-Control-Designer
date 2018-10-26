/**
 * The view package contains tools to create a view made of graphic elements
 * Copyright (c) December 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Modified : March 2006
 */

package org.colos.ejs.osejs.edition.variables;

import org.colos.ejs.osejs.Osejs;
import org.opensourcephysics.tools.ResourceLoader;
import org.opensourcephysics.tools.minijar.PathAndFile;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A class to render a given model element on a list
 */
@SuppressWarnings("rawtypes")
public class ModelElementListCellRenderer extends JPanel implements ListCellRenderer {
  static private final String GOTO_DIR                    = Osejs.getResources().getString("ElementsEditor.ClickToGoToDir")+" ";
//  static private final String DRAG_OR_DOUBLE_CLICK        = ": "+Osejs.getResources().getString("ElementsEditor.DragOrDoubleClick");
  static private final String DOUBLE_CLICK_OR_RIGHT_CLICK = ": "+Osejs.getResources().getString("ElementsEditor.DoubleClickOrRightClick");
  static         final String USER_DEFINED_TEXT           = Osejs.getResources().getString("ElementsEditor.UserDefined");
  static private final Border SMALL_BORDER = new EmptyBorder(2,2,2,2);

  private boolean addedList=false;
  private String modelsDirPath;
  private JLabel textLabel;
  private JLabel iconLabel;

  public ModelElementListCellRenderer(String _modelsDirPath, boolean _addedList) {
    super(new BorderLayout());
    this.modelsDirPath = _modelsDirPath;
    this.addedList = _addedList;

    // Create the label for the text
    textLabel = new JLabel();
    textLabel.setBorder(SMALL_BORDER);
    
    // create the label for the icon
    iconLabel = new JLabel();
    iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
    iconLabel.setVerticalAlignment(SwingConstants.CENTER);
    
    // Put them together
    setBorder(ElementsEditor.EMPTY_BORDER);
    if (addedList) {
      textLabel.setHorizontalAlignment(SwingConstants.LEFT);
      textLabel.setVerticalAlignment(SwingConstants.BOTTOM);
      add(textLabel,BorderLayout.CENTER);
      add(iconLabel,BorderLayout.WEST);
    }
    else {
      textLabel.setHorizontalAlignment(SwingConstants.CENTER);
      textLabel.setVerticalAlignment(SwingConstants.CENTER);
//      iconLabel.setPreferredSize(new Dimension(32,32));
//        textLabel.setFont(SMALL_FONT);
      add(textLabel,BorderLayout.SOUTH);
      add(iconLabel,BorderLayout.CENTER);
      //        setPreferredSize(new Dimension(100,50)); // This is needed if you want to limit the size of the names
    }
  }

  // Implementation of ListCellRenderer
  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    if (isSelected) setBorder(ElementsEditor.ACTIVE_BORDER);
    else setBorder(ElementsEditor.EMPTY_BORDER);
    if (addedList) { // This is for elements already added to the model
      ModelElementInformation elementInfo = (ModelElementInformation) value;
      iconLabel.setIcon(elementInfo.getElement().getImageIcon());
      String info = elementInfo.getElement().getDisplayInfo();
      if (info!=null && info.trim().length()>0) textLabel.setText(elementInfo.getName() + " "+info);
      else textLabel.setText(elementInfo.getName());
      setToolTipText(elementInfo.getElement().getGenericName()+DOUBLE_CLICK_OR_RIGHT_CLICK);
    }
    else { // This may be a folder, an HTML, or a possible element
      if (value instanceof ModelElementInformation) {
        ModelElementInformation elementInfo = (ModelElementInformation) value;
        iconLabel.setIcon(elementInfo.getElement().getImageIcon());
        textLabel.setText(elementInfo.getElement().getGenericName());
        setToolTipText(elementInfo.getElement().getGenericName()+" " + elementInfo.getElement().getTooltip()); //+ " "+DRAG_OR_DOUBLE_CLICK);
      }
      else if (value instanceof PathAndFile) {
        PathAndFile paf = ((PathAndFile) value);
        if (paf.getFile().isDirectory()) {
          if (paf.getPath().equals("..")) { // It is the up directory
            iconLabel.setIcon(ElementsEditor.DIR_UP_ICON);
            textLabel.setText("..");
          }
          else {
            Icon dirIcon = ResourceLoader.getIcon(paf.getPath()+"DirIcon.png");
            if (dirIcon==null) dirIcon = ResourceLoader.getIcon(paf.getPath()+"DirIcon.gif");
            if (dirIcon==null) iconLabel.setIcon(ElementsEditor.DIR_ICON);
            else iconLabel.setIcon(dirIcon);
            textLabel.setText(paf.getFile().getName());
          }
          setToolTipText(GOTO_DIR+ModelElementsPalette.getDisplayName(paf.getFile(), modelsDirPath));
        }
        else { // It is an HTML or PDF file
          iconLabel.setIcon(ElementsEditor.HTML_ICON);
          textLabel.setText(paf.getPath());
          setToolTipText(null);
        }
      }
      else if (value instanceof java.util.Set<?>) { // It is the user-defined set of model elements
        iconLabel.setIcon(ElementsEditor.USER_DEFINED_ICON);
        textLabel.setText(USER_DEFINED_TEXT);
        setToolTipText(null);
      }
    }
    setOpaque(false);
    return this;
  }

}


