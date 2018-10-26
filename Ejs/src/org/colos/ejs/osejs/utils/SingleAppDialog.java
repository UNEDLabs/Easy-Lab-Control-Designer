/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.colos.ejs.osejs.Osejs;

public class SingleAppDialog {
  static private org.colos.ejs.osejs.utils.ResourceUtil res = new org.colos.ejs.osejs.utils.ResourceUtil("Resources");

  static public final int LOCK_PORTRAIT = 0;
  static public final int LOCK_LANDSCAPE = 1;
  static public final int LOCK_NOTHING = 2;

  static public final int TEMPLATE_SIDE = 0;
  static public final int TEMPLATE_TABS = 1;
  static public final int TEMPLATE_SLIDES = 2;
  static public final int TEMPLATE_CARD = 3;

  static public final int IONIC_V1 = 0;
  static public final int IONIC_V2 = 1;
  
  static public class SingleAppOptions {
    private int framework = IONIC_V2;
    private int template = TEMPLATE_SIDE;
    private boolean fullScreen = false;
    private boolean simulationFirst = false;
    private int locking = LOCK_NOTHING;
//    boolean noCoverPage = false;

    public int getFramework() { return framework; }
    public int getTemplate() { return template; }
    public boolean isFullScreen() { return fullScreen; }
    public boolean isSimulationFirst() { return simulationFirst; } 
    public int getLocking() { return locking; }
  }
  
  static private class ReturnValue {
    boolean value = false;
  }
  
  static public SingleAppOptions getSingleAppOptions (final Osejs ejs, final Component parentComponent) {
    final ReturnValue returnValue=new ReturnValue();
    final JDialog dialog=new JDialog();

    //--------------- Choose Framework
    
    JLabel frameworkLabel = new JLabel(res.getString("Package.App.Framework"),SwingConstants.LEFT);
//    frameworkLabel.setFont(frameworkLabel.getFont().deriveFont(Font.BOLD));

    JRadioButton ionic1RB = new JRadioButton (res.getString("Package.App.Ionic1"),false);
    ionic1RB.setRequestFocusEnabled(false);
    
    JRadioButton ionic2RB = new JRadioButton (res.getString("Package.App.Ionic2"),true);
    ionic2RB.setRequestFocusEnabled(false);

    ButtonGroup frameworkGroup = new ButtonGroup();
    frameworkGroup.add(ionic1RB);
    frameworkGroup.add(ionic2RB);

    FlowLayout frameworkFlow = new FlowLayout(FlowLayout.CENTER);
    frameworkFlow.setVgap(0);
    JPanel frameworkOptionsPanel = new JPanel (frameworkFlow);
    frameworkOptionsPanel.add(ionic2RB);
    frameworkOptionsPanel.add(ionic1RB);

    JPanel frameworkPanel = new JPanel (new BorderLayout());
    frameworkPanel.add (frameworkLabel,BorderLayout.NORTH);
    frameworkPanel.add (frameworkOptionsPanel,BorderLayout.CENTER);    
    
    // --------------- Choose template
    
    JLabel templateLabel = new JLabel(res.getString("Package.App.Template"),SwingConstants.LEFT);
//    templateLabel.setFont(templateLabel.getFont().deriveFont(Font.BOLD));

    JRadioButton sideTemplateRB = new JRadioButton (res.getString("Package.App.SideTemplate"),true);
    sideTemplateRB.setRequestFocusEnabled(false);
    JRadioButton tabsTemplateRB = new JRadioButton (res.getString("Package.App.TabsTemplate"),false);
    tabsTemplateRB.setRequestFocusEnabled(false);
    JRadioButton slidesTemplateRB = new JRadioButton (res.getString("Package.App.SlidesTemplate"),false);
    slidesTemplateRB.setRequestFocusEnabled(false);
    JRadioButton cardTemplateRB = new JRadioButton (res.getString("Package.App.CardTemplate"),false);
    cardTemplateRB.setRequestFocusEnabled(false);
    
    ButtonGroup templateGroup = new ButtonGroup();
    templateGroup.add(sideTemplateRB);
    templateGroup.add(tabsTemplateRB);
    templateGroup.add(slidesTemplateRB);
    templateGroup.add(cardTemplateRB);

    JPanel templateOptionsPanel = new JPanel (new GridLayout(2,2)); // FlowLayout(FlowLayout.CENTER)); // GridLayout(1,0));
    templateOptionsPanel.add(sideTemplateRB);
    templateOptionsPanel.add(tabsTemplateRB);
    templateOptionsPanel.add(slidesTemplateRB);
    templateOptionsPanel.add(cardTemplateRB);
    
    JPanel templateCenterPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    templateCenterPanel.add(templateOptionsPanel);

    JPanel templatePanel = new JPanel (new BorderLayout());
    templatePanel.setBorder(new EmptyBorder(5,0,0,0));
    templatePanel.add(templateLabel,BorderLayout.NORTH);
    templatePanel.add(templateCenterPanel,BorderLayout.CENTER);

    // --------------- Lock radiobuttons
    
    JLabel lockLabel = new JLabel(res.getString("Package.App.LockingType"),SwingConstants.LEFT);
//    lockLabel.setFont(lockLabel.getFont().deriveFont(Font.BOLD));

    JRadioButton lockPortraitRB = new JRadioButton (res.getString("Package.App.LockPortrait"),false);
    lockPortraitRB.setRequestFocusEnabled(false);
    JRadioButton lockLandscapeRB = new JRadioButton (res.getString("Package.App.LockLandscape"),false);
    lockLandscapeRB.setRequestFocusEnabled(false);
    JRadioButton noLockRB = new JRadioButton (res.getString("Package.App.LockNothing"),true);
    noLockRB.setRequestFocusEnabled(false);

    ButtonGroup lockGroup = new ButtonGroup();
    lockGroup.add(noLockRB);
    lockGroup.add(lockPortraitRB);
    lockGroup.add(lockLandscapeRB);

    FlowLayout lockFlow = new FlowLayout(FlowLayout.CENTER);
    lockFlow.setVgap(0);
    JPanel lockOptionsPanel = new JPanel (lockFlow);
    lockOptionsPanel.add(noLockRB);
    lockOptionsPanel.add(lockPortraitRB);
    lockOptionsPanel.add(lockLandscapeRB);

    JPanel lockPanel = new JPanel (new BorderLayout());
    lockPanel.setBorder(new EmptyBorder(5,0,0,0));
    lockPanel.add (lockLabel,BorderLayout.NORTH);
    lockPanel.add (lockOptionsPanel,BorderLayout.CENTER);    

    // --------------- Features checkboxes
    
    JCheckBox fullScreenCB = new JCheckBox (res.getString("Package.App.FullScreen"),false);
    fullScreenCB.setRequestFocusEnabled(false);

    JCheckBox simulationFirstCB = new JCheckBox (res.getString("Package.App.SimulationFirst"),false);
    simulationFirstCB.setRequestFocusEnabled(false);

    JPanel otherPanel=new JPanel (new java.awt.GridLayout(0,1));
    otherPanel.setBorder(new EmptyBorder(10,5,5,0));
    otherPanel.add(fullScreenCB);
    otherPanel.add(simulationFirstCB);

//    JLabel featuresLabel = new JLabel(res.getString("Package.App.OtherProperties"));

    // --------------------- Main Buttons

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.addActionListener (new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        returnValue.value = true;
        dialog.setVisible (false);
      }
    });

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addActionListener (new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        returnValue.value = false;
        dialog.setVisible (false);
      }
    });
    JPanel buttonsPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonsPanel.add (cancelButton);
    buttonsPanel.add (okButton);

    // ------------- Put everything together ---------------
    
    Box optionsBox = Box.createVerticalBox();
    optionsBox.setBorder(new EmptyBorder(10,10,2,10));
    optionsBox.add(frameworkPanel);
    optionsBox.add(templatePanel);
    optionsBox.add(lockPanel);
    optionsBox.add(otherPanel);
    
    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.add(new JSeparator(),BorderLayout.NORTH);
    bottomPanel.add(buttonsPanel,BorderLayout.CENTER);
    
    // Put everything in the dialog
    dialog.getContentPane().setLayout (new BorderLayout(5,0));
    dialog.getContentPane().add (optionsBox,BorderLayout.NORTH);
    dialog.getContentPane().add (bottomPanel,BorderLayout.SOUTH);
    
    dialog.validate();
    dialog.pack();
    dialog.setModal(true);
    dialog.setTitle (res.getString("Package.App.SingleAppTitle"));
    dialog.setLocationRelativeTo (parentComponent);

    returnValue.value = false;
    dialog.setVisible(true);
    if (!returnValue.value) return null;

    SingleAppOptions info = new SingleAppOptions();
    if (ionic1RB.isSelected()) info.framework = IONIC_V1;
    else                       info.framework = IONIC_V2;                              
    // template
    if      (sideTemplateRB.isSelected())    info.template = TEMPLATE_SIDE;
    else if (tabsTemplateRB.isSelected())    info.template = TEMPLATE_TABS;
    else if (slidesTemplateRB.isSelected())  info.template = TEMPLATE_SLIDES;
    else                                      info.template = TEMPLATE_CARD;                    
    // checkbox
    info.fullScreen = fullScreenCB.isSelected();
    info.simulationFirst = simulationFirstCB.isSelected();
    // locking
    if (lockPortraitRB.isSelected())        info.locking = LOCK_PORTRAIT;
    else if (lockLandscapeRB.isSelected())  info.locking = LOCK_LANDSCAPE;
    else                                    info.locking = LOCK_NOTHING;          

    return info;
  }

  
}
