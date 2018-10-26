/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import javax.swing.*;

import org.opensourcephysics.tools.FontSizer;

public class ProgressDialog extends JFrame {
  private static final long serialVersionUID = 1L;
  private int totalSteps;
  private int currentStep=0;
  private JLabel progressLabel = null;
  private JProgressBar progressBar = null;

  @SuppressWarnings("serial")
  public ProgressDialog (int _steps, String _title, Dimension _size, Rectangle bounds) {
    totalSteps = _steps;
    setTitle(_title);
    setSize(_size);

    getContentPane().setLayout (new java.awt.BorderLayout ());
    JPanel progressPanel = new JPanel() {
      public Insets getInsets() { return new Insets(15,10,5,10);}
    };
    progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
    getContentPane().add(progressPanel, BorderLayout.CENTER);
    Dimension d = new Dimension(_size.width, 20);
    progressLabel = new JLabel(_title);
    progressLabel.setAlignmentX(CENTER_ALIGNMENT);
    progressLabel.setMaximumSize(d);
    progressLabel.setPreferredSize(d);
    progressPanel.add(progressLabel);
    progressPanel.add(Box.createRigidArea(new Dimension(1,20)));
    progressBar = new JProgressBar(0, totalSteps);
    progressBar.setStringPainted(true);
    progressLabel.setLabelFor(progressBar);
    progressBar.setAlignmentX(CENTER_ALIGNMENT);
    progressPanel.add(progressBar);
//    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(bounds.x+(bounds.width-_size.width)/2,bounds.y+(bounds.height-_size.width)/2);
    getContentPane().add(progressPanel, BorderLayout.CENTER);
    setCursor(new Cursor(Cursor.WAIT_CURSOR));
    setVisible(true); 
  }

  public void reportProgress(final String _process) {
    currentStep++;
    progressBar.setValue(currentStep);
    progressLabel.setText(_process);
  }
  
  public void setZoomLevel(int level) {
    FontSizer.setFonts(this, level);
    this.pack();
  }
  
}
