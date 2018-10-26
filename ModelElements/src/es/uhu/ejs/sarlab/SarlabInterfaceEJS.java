package es.uhu.ejs.sarlab;

import java.util.Hashtable;

import clientsarlab.EJSClientSarlab;
import org.colos.ejs.library.Model;

/**
 * @author Francisco Esquembre
 * @author Andres Mejias
 * @author Marco Marquez
 * @version december 2012
 */
public class SarlabInterfaceEJS extends EJSClientSarlab {
    
    static private Hashtable<String,SarlabInterfaceEJS> sClientTable = new Hashtable<String,SarlabInterfaceEJS>();
    
    private Model mModel;
    private boolean mConnected;
    
   
   static public SarlabInterfaceEJS createConnection(Model model, String ipServer, String portServer, String idExp) {
      String keyword = ipServer+":"+portServer;
      SarlabInterfaceEJS client = sClientTable.get(keyword);
      if (client==null) {
        client = new SarlabInterfaceEJS(model,ipServer, portServer, idExp);
        sClientTable.put(keyword, client);
      }
      return client;
    }
   
   private SarlabInterfaceEJS(Model model, String ipServer, String portServer, String idExp) {
     super (ipServer, portServer, idExp);
     mModel = model;
     mConnected = false;
   }
   
    @Override
    public void connect() {
      if (mConnected) {
        System.out.println ("Already connected!");
        return;
      }
      System.out.println ("Connecting...");
      if(mModel!=null){
        String s1=mModel._getParameter("user");
        String s2=mModel._getParameter("passwd");
        String s3=mModel._getParameter("ipserver");
        String s4=mModel._getParameter("portserver");
        String s5=mModel._getParameter("idExp");
        if(s1!=null ){
          super.setUser(s1);
          System.out.println("User "+ s1);          
        }else{
          System.out.println("Parameter null user");
        }
        if(s2!=null ){          
          super.setPassword(s2);
          System.out.println("Password "+ s2); 
        }else{
          System.out.println("Parameter null password");
        }
        if(s3!=null ){
          super.setIpServer(s3);
          System.out.println("ipServer "+ s3);          
        }else{
          System.out.println("Parameter null ipServer");
        }
        if(s4!=null ){
          super.setPortServer(s4);
          System.out.println("portServer "+ s4);          
        }else{
          System.out.println("Parameter null portServer");
        }
        if(s5!=null ){
          super.setIdExp(s5);
          System.out.println("idExp "+ s5);          
        }else{
          System.out.println("Parameter null idExp");
        }
        
      }
      super.connect();
      System.out.println ("Connected!");
      mConnected = true;
    }    
}
