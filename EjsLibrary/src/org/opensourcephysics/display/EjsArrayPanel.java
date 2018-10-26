package org.opensourcephysics.display;

import javax.swing.event.TableModelEvent;

public class EjsArrayPanel extends ArrayPanel {

  private int lastRow=-1, lastColumn=-1;

  public void saveLastEdit(TableModelEvent tme) {
    lastRow = tme.getFirstRow();
    lastColumn = tme.getColumn();
  }

  /**
   * Returns the row of the cell that was last edited
   * @return
   */
  public int getRowEdited() { return lastRow; }
  
  /**
   * Returns the column of the cell that was last edited
   * @return
   */
  public int getColumnEdited() { return lastColumn; }

}
