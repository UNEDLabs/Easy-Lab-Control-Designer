package com.cdsc.eje.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

  public class ClassWizard extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    private JScrollPane scroll;
    @SuppressWarnings("rawtypes")
    private JList list;
    EJEArea area;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ClassWizard(EJEArea anArea, JavaInterface javaInterface) {
      area = anArea;
      list = new JList(javaInterface);
      list.setCellRenderer(javaInterface.new ClassWizardCellRenderer());
      this.setLayout(new BorderLayout());
      scroll = new JScrollPane(list);
      this.add(scroll, BorderLayout.CENTER);
      this.setOpaque(true);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.getSelectionModel().setAnchorSelectionIndex(4);
      list.addMouseListener(new MouseAdapter () {
        public void mousePressed (java.awt.event.MouseEvent evt) {
          if (evt.getClickCount()>1) area.popupListener.fireReturnPressedWithClassWizardVisible();
          }
      });
    }

    public Dimension getPreferredSize() {
      int length = list.getModel().getSize();
//      Dimension d = new Dimension(super.getPreferredSize().width, length < 8 ? length * 19 :super.getPreferredSize().height);
      Dimension d = new Dimension(Math.min(super.getPreferredSize().width,400),
                                  Math.min(200,length < 8 ? length * 19 : super.getPreferredSize().height));
      return d;
    }

    public void selectNextIndex() {
      int index = list.getSelectedIndex();
      int visibleIndexGap = 3;
      int lastIndex = getListSize() - 1; // Last useful index
      index += 1; // new index
      int gap = lastIndex - index;
      if (gap < 3) {
        visibleIndexGap = gap + 1;
        if (gap < 0) {
          index = lastIndex;
          visibleIndexGap = 0;
        }
      }
      highlightIndex(index, index + visibleIndexGap);
    }

    public void selectPreviousIndex() {
      int index = list.getSelectedIndex();
      int visibleIndexGap = 3;
      index -= 1;
      if (index < 0) {
        index = 0;
        visibleIndexGap = 0;
      }
      highlightIndex(index, index - visibleIndexGap);
    }

    public void selectNextPageIndex() {
      int index = list.getSelectedIndex();
      int visibleIndexGap = 3;
      int lastIndex = getListSize() - 1; // Last useful index
      index += 8; // new index
      int gap = lastIndex - index;
      if (gap < 3) {
        visibleIndexGap = gap + 1;
        if (gap < 0) {
          index = lastIndex;
          visibleIndexGap = 0;
        }
      }
      highlightIndex(index, index + visibleIndexGap);
    }

    public void selectPreviousPageIndex() {
      int index = list.getSelectedIndex();
      int visibleIndexGap = 3;
      index -= 8;
      if (index < 0) {
        index = 0;
        visibleIndexGap = 0;
      }
      highlightIndex(index, index - visibleIndexGap);
    }

    public void selectHomeIndex() {
      highlightIndex(0, 0);
    }

    public void selectEndIndex() {
      int lastIndex = getListSize() - 1; // Last useful index
      highlightIndex(lastIndex, lastIndex);
    }

    private int getListSize() {
      @SuppressWarnings("rawtypes")
      ListModel lm = list.getModel();
      return lm.getSize();
    }

    public void highlightIndex(int index, int visibleIndex) {
      list.setSelectedIndex(index);
      list.ensureIndexIsVisible(visibleIndex);
    }

    public void setSelectedMember(String memberName) {
      @SuppressWarnings("rawtypes")
      ListModel lm = list.getModel();
      int size = lm.getSize();
      String item = "";
      for (int i = 0; i < size; ++i) {
        item = (String) lm.getElementAt(i);
        if (item.startsWith(memberName)) {
          break;
        }
      }
      list.setSelectedValue(item, true);
    }

    public String getSelectedValue() {
      return (String) list.getSelectedValue();
    }

}
