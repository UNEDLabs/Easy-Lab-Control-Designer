package org.colos.ejs.model_elements.apache_numerics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.border.Border;

import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.OsejsCommon;

public class RealFunctionElement extends AbstractModelElement {
  static private ImageIcon ELEMENT_ICON = AbstractModelElement.createImageIcon("org/colos/ejs/model_elements/apache_numerics/RealFunction.gif"); // This icon is included in this jar
  static private Border LABEL_BORDER = BorderFactory.createEmptyBorder(0,4,0,2);

  static private final String BEGIN_SOLVER_HEADER = "<Solver><![CDATA["; // Used to delimit my XML information
  static private final String END_SOLVER_HEADER = "]]></Solver>";        // Used to delimit my XML information
  static private final String BEGIN_INTEGRATOR_HEADER = "<Integrator><![CDATA["; // Used to delimit my XML information
  static private final String END_INTEGRATOR_HEADER = "]]></Integrator>";        // Used to delimit my XML information

  static private final String DEFAULT_CODE = "public double value(double x) { // Define the 'value' function here\n  return x;\n}";

  // -------------------------------
  // Instance variables
  // -------------------------------
  
  private String mSolverStr     = RealFunction.SOLVER_BISECTION;   // The actual solver
  private String mIntegratorStr = RealFunction.INTEGRATOR_ROMBERG; // The actual integrator

  private ModelElementEditor mCodeEditor = new ModelElementEditor(this,null);  // The editor for the code
  private JComboBox<String> mSolverCB; // Comboboxes to select the solver
  private JComboBox<String> mIntegratorCB; // Comboboxes to select the integrator methods 
  
  {
    mCodeEditor.setName("RealFunctionElement");
    mCodeEditor.readPlainCode(DEFAULT_CODE);  
  }
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Function"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.apache_numerics.RealFunction"; }
  
  @Override
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_name + " = new " + getConstructorName() + "(new org.apache.commons.math3.analysis.UnivariateFunction() {\n");
    buffer.append(mCodeEditor.generateCode(_name, "  "));
    buffer.append("\n});\n");
    buffer.append(_name + ".setSolver(\""+getSolver()+"\");\n");
    buffer.append(_name + ".setIntegrator(\""+getIntegrator()+"\");\n");
    return buffer.toString();
  }
  
  @Override
  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(mCodeEditor.saveStringBuffer());
    buffer.append(BEGIN_SOLVER_HEADER+getSolver()+END_SOLVER_HEADER + "\n");
    buffer.append(BEGIN_INTEGRATOR_HEADER+getIntegrator()+END_INTEGRATOR_HEADER);
    return buffer.toString();
  }

  @Override
  public void readfromXML(String _inputXML) {
    mCodeEditor.readXmlString(_inputXML);
    mSolverStr = OsejsCommon.getPiece(_inputXML,BEGIN_SOLVER_HEADER,END_SOLVER_HEADER,false);
    if (mSolverCB!=null) mSolverCB.setSelectedItem(mSolverStr);
    mIntegratorStr = OsejsCommon.getPiece(_inputXML,BEGIN_INTEGRATOR_HEADER,END_INTEGRATOR_HEADER,false);
    if (mIntegratorCB!=null) mIntegratorCB.setSelectedItem(mIntegratorStr);
  }

  private String getSolver() {
    if (mSolverCB!=null)  return mSolverCB.getSelectedItem().toString();
    return mSolverStr;
  }
  
  private String getIntegrator() {
    if (mIntegratorCB!=null)  return mIntegratorCB.getSelectedItem().toString();
    return mIntegratorStr;
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  @Override
  public String getTooltip() { return "defines a y = f(x) function with some utilities"; }
  
  @Override
  public void setFont(Font font) { mCodeEditor.setFont(font); }

  protected String getHtmlPage() { return "org/colos/ejs/model_elements/apache_numerics/RealFunction.html"; }
  
  @Override
  protected Component createEditor(String name, Component parentComponent, final ModelElementsCollection collection) {
    JLabel solverLabel = new JLabel(RES.getString("ModelElement.Function.SolverAlgorithm"),SwingConstants.RIGHT);
    solverLabel.setBorder(LABEL_BORDER);

    JLabel integratorLabel = new JLabel(RES.getString("ModelElement.Function.IntegrationAlgorithm"),SwingConstants.RIGHT);
    integratorLabel.setBorder(LABEL_BORDER);

    // Make both labels the same dimension
    int maxWidth  = solverLabel.getPreferredSize().width;
    int maxHeight = solverLabel.getPreferredSize().height;
    maxWidth  = Math.max(maxWidth,  integratorLabel.getPreferredSize().width);
    maxHeight = Math.max(maxHeight, integratorLabel.getPreferredSize().height);
    Dimension dim = new Dimension (maxWidth,maxHeight);
    solverLabel.setPreferredSize(dim);
    integratorLabel.setPreferredSize(dim);

    ItemListener itemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent arg0) { collection.reportChange(RealFunctionElement.this); }
    };

    mSolverCB = new JComboBox<String>();
    mSolverCB.addItem(RealFunction.SOLVER_BISECTION);
    mSolverCB.addItem(RealFunction.SOLVER_BRENT_DEKKER);
    mSolverCB.addItem(RealFunction.SOLVER_SECANT);
    mSolverCB.addItem(RealFunction.SOLVER_MULLER);
    mSolverCB.setSelectedItem(mSolverStr);
    mSolverCB.addItemListener(itemListener);

    mIntegratorCB = new JComboBox<String>();
    mIntegratorCB.addItem(RealFunction.INTEGRATOR_ROMBERG);
    mIntegratorCB.addItem(RealFunction.INTEGRATOR_SIMPSON);
    mIntegratorCB.addItem(RealFunction.INTEGRATOR_TRAPEZOID);
    mIntegratorCB.addItem(RealFunction.INTEGRATOR_LEGENDRE_GAUSS);
    mIntegratorCB.setSelectedItem(mIntegratorStr);
    mIntegratorCB.addItemListener(itemListener);

    JPanel solverPanel = new JPanel(new BorderLayout());
    solverPanel.add(solverLabel, BorderLayout.WEST);
    solverPanel.add(mSolverCB, BorderLayout.CENTER);
    
    JPanel integratorPanel = new JPanel(new BorderLayout());
    integratorPanel.add(integratorLabel, BorderLayout.WEST);
    integratorPanel.add(mIntegratorCB, BorderLayout.CENTER);

    JPanel topPanel = new JPanel(new GridLayout(0,1));
    topPanel.add(solverPanel);
    topPanel.add(integratorPanel);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setPreferredSize(new Dimension(500,400));
    mainPanel.add(topPanel,BorderLayout.NORTH);
    mainPanel.add(mCodeEditor.getComponent(collection),BorderLayout.CENTER);
    
    return mainPanel;
  }
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, String name, ModelElementsCollection collection) {
    return mCodeEditor.search(info, searchString, mode, name, collection);
  }

}
