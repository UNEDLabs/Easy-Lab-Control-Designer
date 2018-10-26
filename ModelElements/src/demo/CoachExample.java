package demo;

public class CoachExample {
  static final public int SEND_ID_STRING = 4;
  static final public int COACH_DEFAULT_BAUDRATE = 300;

  static public void main(String[] args) {
    System.out.println ("Library path = "+System.getProperty("java.library.path"));
    for (int i=0; i<8; i++) System.out.println ("output for "+i+"  is "+((byte)(1 << i)));

    java.util.List<String> modulesFound = LocateCoachLabModules();
    if (modulesFound.isEmpty()) System.out.println ("No CoachLab module found!");
    for (String port : modulesFound) System.out.println ("CoachLab 2+ found at port "+port);
  }

  /**
   * Returns a java.util.List<String> of ports connected to a CoachLab module
   * @return
   */
  static public java.util.List<String> LocateCoachLabModules(){
    java.util.List<String> found = new java.util.ArrayList<String>();
    String[] ports = Serial.list();
    int nPorts = ports.length;
    System.out.println("Ports found: "+nPorts);
    for(int i=0;i<nPorts;i++) System.out.println("Port "+i+" = "+ ports[i]);
    for(int i=0; i<nPorts; i++) {
      Serial port = new Serial(ports[i], COACH_DEFAULT_BAUDRATE, 'N', 8, 1);
      System.out.println(" Checking port "+i+" = "+ port);  
      port.write(SEND_ID_STRING); 
      delay(100);
      String reply = port.readString();
      if (reply!=null && reply.startsWith("-CoachLab")) found.add(ports[i]);
      System.out.println(" - Port ["+i+"] = "+ reply);  
      port.stop();
      port.dispose();
      System.out.println(" End of reading from  port "+i+" = "+ port);  
    }
    return found;
  }
  
  /**
   * Specifies a delay (in milliseconds)
   * @param t time (milliseconds)
   */
  static public void delay(int t) {
    try {
      Thread.sleep(t);
    } catch (InterruptedException ex) {
      System.out.println("Delay error");
    }
  }
  
}