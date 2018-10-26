package org.colos.ejs.osejs.utils;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.library.utils.LocaleListener;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.html_view.SizeOption;
import org.colos.ejss.xml.EmulatorJFX;
import org.colos.ejss.xml.SimulationXML;
import org.w3c.dom.Element;

public class EmulatorRun implements LocaleListener{
  static private ResourceUtil res = new ResourceUtil ("Resources");

  private Osejs mEjs;
  private SimulationXML mSimulation;
  private EmulatorJFX mEmulator;
  private JTextField mWidthField;
  private JTextField mHeightField;
  private JComboBox<SizeOption> mSizeCombo;
  private org.colos.ejs.library.utils.LocaleSelector mLocaleSelector;
  private LocaleItem mLocale = LocaleItem.getDefaultItem();

  public EmulatorRun(Osejs ejs, SimulationXML simulation, final String cssFilename, final String libPath, final String htmlPath) {
    mEjs = ejs;
    mSimulation = simulation;

    JLabel widthLabel = new JLabel(res.getString("EditorForPoint.Width"));
    mWidthField = new JTextField("0");
    mWidthField.setEditable(false);

    JPanel widthPanel = new JPanel(new BorderLayout());
    widthPanel.add(widthLabel,BorderLayout.WEST);
    widthPanel.add(mWidthField,BorderLayout.CENTER);

    JLabel heightLabel = new JLabel(res.getString("EditorForPoint.Height"));
    mHeightField = new JTextField("0");
    mHeightField.setEditable(false);

    JPanel heightPanel = new JPanel(new BorderLayout());
    heightPanel.add(heightLabel,BorderLayout.WEST);
    heightPanel.add(mHeightField,BorderLayout.CENTER);

    mSizeCombo = new JComboBox<SizeOption>();
    for (Element view : simulation.getViews()) {
      String name = simulation.getViewName(view);
      int width=100, height=100;
      try { width = Integer.parseInt(simulation.getViewWidth(view)); }
      catch (Exception exc) { width = 800; }
      try { height = Integer.parseInt(simulation.getViewHeight(view)); }
      catch (Exception exc) { height = 600; }
      mSizeCombo.addItem(new SizeOption(name,width,height));
    }
    mSizeCombo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        SizeOption option = (SizeOption) e.getItem();
//        System.err.println ("Setting size to "+option.getWidth()+","+option.getHeight());
        mWidthField.setText(""+option.getWidth());
        mHeightField.setText(""+option.getHeight());
        refreshEmulator();
      }
    });
    mSizeCombo.setSelectedIndex(0);
    {
    SizeOption option = (SizeOption) mSizeCombo.getSelectedItem();
    mWidthField.setText(""+option.getWidth());
    mHeightField.setText(""+option.getHeight());
    }

    JPanel sizePanel = new JPanel(new GridLayout());
    sizePanel.add(widthPanel);
    sizePanel.add(heightPanel);

    JPanel sizeOptionPanel = new JPanel(new BorderLayout());
    sizeOptionPanel.add(sizePanel,BorderLayout.CENTER);
    sizeOptionPanel.add(mSizeCombo,BorderLayout.WEST);

    // Locale selector
    mLocaleSelector = new org.colos.ejs.library.utils.LocaleSelector (this);
    mLocaleSelector.setMasterSelector(mEjs.getTranslationEditor().getLocaleSelector());
    mLocaleSelector.setEnabled(mEjs.getSimInfoEditor().addTranslatorTool());

    JPanel rightPanel = new JPanel(new BorderLayout());
//    rightPanel.add(refreshButton,BorderLayout.WEST);
    rightPanel.add(mLocaleSelector.getComponent(),BorderLayout.CENTER);

    final JPanel accesoryPanel = new JPanel(new BorderLayout());
    accesoryPanel.add (sizeOptionPanel,BorderLayout.CENTER);
    accesoryPanel.add (rightPanel,BorderLayout.EAST);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() { 
        mEmulator = new EmulatorJFX (mEjs, null, 100, 100, accesoryPanel);
        mEmulator.setSimulationPaths(cssFilename,libPath, htmlPath);
        if (mEmulator.getWindow()!=null) {
          mEmulator.getWindow().addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              kill();
            }
          });
        }
        refreshEmulator();
      }
    });

  }

  public void kill() {
//    System.err.println("Killing "+this);
    if (mEmulator.getWindow()!=null) mEmulator.getWindow().dispose();
    mEmulator.clear();
    mEjs.setRunning("dummy", EmulatorRun.this, false);
  }
  
  public void setLocaleItem(LocaleItem locale) {
    mLocale = locale;
    refreshEmulator();
  }

  public void setLocaleInMenu(LocaleItem locale) {
      mLocaleSelector.setLocaleItem(locale);
  }

  public void refreshEmulator() {
    if (mEmulator==null) return;
    SizeOption option = (SizeOption) mSizeCombo.getSelectedItem();
    mEmulator.setSize(option.getWidth(),option.getHeight());
    mEmulator.setSimulation(mEjs,mSimulation,option.toString(), mLocale.isDefaultItem() ? SimulationXML.sDEFAULT_LOCALE : mLocale.getKeyword());
  }
  
}
