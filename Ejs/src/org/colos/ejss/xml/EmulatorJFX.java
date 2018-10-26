package org.colos.ejss.xml;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.net.URI;


import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import netscape.javascript.JSObject;

import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.html_view.OneView;
import org.colos.ejs.osejs.utils.FileUtils;
import org.colos.ejss.xml.SimulationXML;
import org.w3c.dom.Element;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class EmulatorJFX extends Emulator {

  // see http://docs.oracle.com/javafx/2/webview/jfxpub-webview.htm
  // http://docs.oracle.com/javafx/2/swing/swing-fx-interoperability.htm#CHDIEEJE
  // https://blogs.oracle.com/javafx/entry/communicating_between_javascript_and_javafx
  
  private JFrame mDialog;
  private JFXPanel mFxPanel; 
  private WebEngine mEngine;
  private Component mAccesoryComponent;
  private OneView mHtmlView;

  public EmulatorJFX (Osejs ejs, OneView htmlView, final int width, final int height, Component accesoryComponent) {
    mHtmlView = htmlView;
    // This method is invoked on the EDT thread
    mFxPanel = new JFXPanel();
    Platform.runLater(new Runnable() {
      public void run() {
        mFxPanel.setScene(new Scene(createWebView()));
      }
    });
    mFxPanel.setPreferredSize(new java.awt.Dimension(width,height));

    mDialog = new JFrame(MAIN_TITLE);
    ejs.setMenuBar(mDialog);
    mDialog.getContentPane().setLayout(new BorderLayout());
    mAccesoryComponent = accesoryComponent;
    if (accesoryComponent!=null) mDialog.getContentPane().add(accesoryComponent, BorderLayout.NORTH);
    mDialog.getContentPane().add(mFxPanel, BorderLayout.CENTER);
    mDialog.pack();
    mDialog.setVisible(true);
  }

  // -----------------------------
  // Interface
  //-----------------------------
  
  @Override
  public void adjustBorder() {
    int borderWidth = EjsControl.getBorderWidthAroundWindows();
    JRootPane rootPane = mDialog.getRootPane();
    if (borderWidth!=0) {
      if (borderWidth<0) borderWidth = -1;
      Icon icon = EjsControl.getBorderIconAroundWindows();
      Color color = EjsControl.getBorderColorAroundWindows();
      rootPane.setOpaque(true);
      javax.swing.border.MatteBorder border;
      if (icon==null) border = BorderFactory.createMatteBorder(borderWidth,borderWidth,borderWidth,borderWidth,color);
      else border = BorderFactory.createMatteBorder(borderWidth,borderWidth,borderWidth,borderWidth,icon);
      String title = EjsControl.getBorderTitleAroundWindows();
      if (title==null) rootPane.setBorder(border);
      else {
        javax.swing.border.TitledBorder titledBorder = BorderFactory.createTitledBorder(border," "+title+" ");
        titledBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
        titledBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
        titledBorder.setTitleColor(EjsControl.getBorderTitleColorAroundWindows());
        rootPane.setBorder(titledBorder);
      }
    }
    mDialog.validate();
    mDialog.pack();
  }
  
  @Override
  public void setVisible (boolean visible) {
    if (mHtmlView!=null) {
      if (!mDialog.isVisible()) mHtmlView.refreshEmulator();
    }
    mDialog.setVisible(visible);
  }
  
  @Override
  public boolean isVisible () {
    return mDialog.isVisible();
  }
  
  @Override
  public Window getWindow() {
    return mDialog;
  }

  @Override
  public void clear() {
    Platform.runLater(new Runnable() {
      public void run() {
        mEngine.load(null);
      }
    });
    mDialog.setVisible(false);
  }
  
  @Override
  public void setSize(int width, int height) {
    int extraHeight = (mAccesoryComponent==null) ? 0 : mAccesoryComponent.getHeight(); 
    mFxPanel.setPreferredSize(new java.awt.Dimension(width,height));
    mDialog.setSize(new java.awt.Dimension(width,height+extraHeight));
    mDialog.validate();
    mDialog.pack();
  }
  
  @Override
  public void setSimulation(Osejs ejs, SimulationXML sim, String viewDesired, String locale) {
    Element viewSelected = sim.getViewSelected(viewDesired);
    XMLTransformerJava transformer = super.lintSimulation(ejs, sim, viewSelected, locale);

    final boolean DEBUG = false;
    
    try {
      int width = Integer.parseInt(BasicElement.evaluateNode(viewSelected,"width"));
      int height = Integer.parseInt(BasicElement.evaluateNode(viewSelected,"height"));
      setSize(width, height);
    }
    catch (Exception exc) { } // Do not complain
//    final String report = lint.getErrorReport();
    final String htmlCode = transformer.toSimulationHTMLForEmulator(viewSelected,locale,XMLTransformerJava.getCssFilenameList(mCssFilename,sim), mLibPath, mHTMLPath);
    String outputFilepath = sim.getName()+"_debug.html";
    final File outputFile = new File (outputFilepath);
    if (DEBUG) {
      try {  // save it for debugging purposes
        XMLTransformerJava.saveToFile(outputFile, htmlCode);
        ejs.getOutputArea().println("Debug output file generated "+outputFile.getAbsolutePath());
        SwingUtilities.invokeLater(new Runnable () {
          public void run() {
            String localPage = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(outputFile));
            if (Desktop.isDesktopSupported()) {
              try {
                Desktop.getDesktop().browse(new URI(localPage));
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
            //org.opensourcephysics.desktop.OSPDesktop.displayURL(localPage);
          }
        });
      } 
      catch (Exception e) {
        e.printStackTrace();
      }

    }
    Platform.runLater(new Runnable() {
      public void run() {
        String localPage = "file:///"+FileUtils.correctUrlString(FileUtils.getPath(outputFile));
        if (DEBUG) {
          System.err.println ("Loading page : "+localPage);
          mEngine.load("http://fem.um.es/tmp/TestSimulation_debug.html");
        }
        else { 
//          System.err.println ("Loading code : "+htmlCode);
          mEngine.loadContent(htmlCode);
//        mEngine.loadContent(report);
        }
      }
    });
  }

  public void loadURL(final String url) {
    Platform.runLater(new Runnable() {
      public void run() {
        String tmp = toURL(url);
        if (tmp == null) {
          tmp = toURL("http://" + url);
        }
        System.err.println ("loading "+tmp);
        mEngine.load(tmp);
      }
    });
  }

  private WebView createWebView() {
    WebView view = new WebView();
    mEngine = view.getEngine();

    mEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
      public void handle(final WebEvent<String> event) {
        SwingUtilities.invokeLater(new Runnable() {
         public void run() {
           System.err.println ("Error = "+event.getData());
            // lblStatus.setText(event.getData());
          }
        });
      }
    } );
    
    mEngine.titleProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() { 
//            String currentTitle = mDialog.getTitle();
            String newTitle = MAIN_TITLE+" : "+ newValue;
//            if (!newTitle.equals(currentTitle)) 
            mDialog.setTitle(newTitle);
          }
        });
      }
    });

  
    mEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
      public void handle(final WebEvent<String> event) {
        SwingUtilities.invokeLater(new Runnable() {
         public void run() {
//           System.err.println ("Status is now "+event.getData());
            // lblStatus.setText(event.getData());
          }
        });
      }
    });

    mEngine.locationProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
        SwingUtilities.invokeLater(new Runnable() {
         public void run() {
//           System.err.println ("Changed to "+newValue);
            // txtURL.setText(newValue);
          }
        });
      }
    });

    mEngine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
        JSObject win = (JSObject) mEngine.executeScript("window");
        win.setMember("console", new ConsoleApp());
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
//            System.err.println (" Ok to "+newValue.intValue());
            // progressBar.setValue(newValue.intValue());
          }
        });
      }
    });

    mEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
      public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
        if (mEngine.getLoadWorker().getState() == FAILED) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JOptionPane.showMessageDialog(mDialog, (value != null) ? mEngine.getLocation() + "\n" + value.getMessage() :
                mEngine.getLocation() + "\nUnexpected error.", "Loading error...", JOptionPane.ERROR_MESSAGE);
            }
          });
        }
      }
    });

//    mEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
//      public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
//        if (newState == State.SUCCEEDED) {
//          JSObject win = (JSObject) mEngine.executeScript("window");
//          win.setMember("console", Emulator.this);
//        }
//      }
//    });       
    

    return view;
  }
  
  // JavaScript interface object
  public class ConsoleApp {

      public void exit() {
          Platform.exit();
      }

      public void log(String message) {
          System.out.println(message);
      }
  }

  
}
