package test;

import org.opensourcephysics.tools.minijar.MiniJar;

import java.awt.*;
import java.util.*;

import javax.swing.JFrame;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class PackageTest {
  
  static final String TEST = 
  "-o ../test.jar "+
  " -s bin -c distribution/bin/osp.jar"+
  " test/Test.class";
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    GraphicsEnvironment ge = 
      GraphicsEnvironment.getLocalGraphicsEnvironment();
     GraphicsDevice[] gs = ge.getScreenDevices();
     for (int j = 0; j < gs.length; j++) {
      GraphicsDevice gd = gs[j];
      GraphicsConfiguration[] gc = 
           gd.getConfigurations();
        for (int i=0; i < gc.length; i++) {
             JFrame f = 
              new JFrame(gs[j].getDefaultConfiguration());
             GCCanvas c = new GCCanvas(gc[i]);
             Rectangle gcBounds = gc[i].getBounds();
             int xoffs = gcBounds.x;
             int yoffs = gcBounds.y;
             f.getContentPane().add(c);
             f.setTitle("Screen# "+Integer.toString(j)+", GC# "+Integer.toString(i));
             f.setSize(300, 150);
             f.setLocation((i*50)+xoffs, (i*60)+yoffs);
             f.setVisible(true);
        }
     }
//    processCommand (TEST);
//    System.out.println ("Packaging completed!");
//    System.exit(0);
  }

  static void processCommand (String command) {
    System.out.println ("Processing "+command);
    String[] args = command.split(" ");
    // Remove ';' that should be blanks (needed for class paths in manifests) 
    for (int i=0; i<args.length; i++) args[i] = args[i].replace(';', ' ');
    MiniJar sj = new MiniJar(args);
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+command+"\n");
  }

  static class GCCanvas extends Canvas {

    GraphicsConfiguration gc;
    Rectangle bounds;

    public GCCanvas(GraphicsConfiguration gc) {
        super(gc);
        this.gc = gc;
        bounds = gc.getBounds();
    }

    public Dimension getPreferredSize() {
        return new Dimension(300, 150);
    }

    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(0, 0, 100, 150);
        g.setColor(Color.green);
        g.fillRect(100, 0, 100, 150);
        g.setColor(Color.blue);
        g.fillRect(200, 0, 100, 150);
        g.setColor(Color.black);
        g.drawString("ScreenSize="+
            Integer.toString(bounds.width)+
            "X"+ Integer.toString(bounds.height), 10, 15);
        g.drawString(gc.toString(), 10, 30);
    }

  }
  
}
