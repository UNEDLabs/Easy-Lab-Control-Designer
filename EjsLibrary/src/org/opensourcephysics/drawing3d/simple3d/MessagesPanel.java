package org.opensourcephysics.drawing3d.simple3d;

import java.awt.Component;
import java.awt.Graphics;
import org.opensourcephysics.drawing3d.DrawingPanel3D;
import org.opensourcephysics.display.OSPLayout;
import org.opensourcephysics.display.TextPanel;

/**
 * A panel that hosts messages in its four corners
 * 
 * @author Francisco Esquembre
 * @version August 2009
 */
public class MessagesPanel extends javax.swing.JPanel {

  private OSPLayout glassPanelLayout = new OSPLayout();
  private TextPanel trMessageBox = new TextPanel(); // text box in top right hand corner for message
  private TextPanel tlMessageBox = new TextPanel(); // text box in top left hand corner for message
  private TextPanel brMessageBox = new TextPanel(); // text box in lower right hand corner for message
  private TextPanel blMessageBox = new TextPanel(); // text box in lower left hand corner for mouse coordinates

  public MessagesPanel() {
    setLayout(glassPanelLayout);
    add(trMessageBox, OSPLayout.TOP_RIGHT_CORNER);
    add(tlMessageBox, OSPLayout.TOP_LEFT_CORNER);
    add(brMessageBox, OSPLayout.BOTTOM_RIGHT_CORNER);
    add(blMessageBox, OSPLayout.BOTTOM_LEFT_CORNER);
    setOpaque(false);
  }

  /**
   * Shows a message in a yellow text box in the lower right hand corner.
   *
   * @param msg
   */
  public void setMessage(String msg) {
     brMessageBox.setText(msg); // the default message box
  }

  /**
   * Shows a message in a yellow text box.
   * The location must be one of the following:
   * <ul>
   *   <li> BOTTOM_LEFT;
   *   <li> BOTTOM_RIGHT;
   *   <li> TOP_RIGHT;
   *   <li> TOP_LEFT;
   * </ul>
   * @param msg
   * @param location
   */
  public void setMessage(String msg, int location) {
    switch(location) {
      case DrawingPanel3D.BOTTOM_LEFT :  blMessageBox.setText(msg); break;
      default :
      case DrawingPanel3D.BOTTOM_RIGHT : brMessageBox.setText(msg); break;
      case DrawingPanel3D.TOP_RIGHT :    trMessageBox.setText(msg); break;
      case DrawingPanel3D.TOP_LEFT :     tlMessageBox.setText(msg); break;
    }
  }

  @Override
  public void setBounds(java.awt.Rectangle rect) {
    super.setBounds(rect);
    glassPanelLayout.checkLayoutRect(this, rect);
  }


  public void render(Graphics g) {
    Component[] c = glassPanelLayout.getComponents();
    for(int i = 0, n = c.length; i<n; i++) {
      if(c[i]==null) {
        continue;
      }
      g.translate(c[i].getX(), c[i].getY());
      c[i].print(g);
      g.translate(-c[i].getX(), -c[i].getY());
    }
  }

}
