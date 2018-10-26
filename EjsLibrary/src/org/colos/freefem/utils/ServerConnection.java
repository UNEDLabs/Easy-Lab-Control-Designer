package org.colos.freefem.utils;

/* The org.colos.freefem package contains Java classes that allow interfacing with a loal or server
 * FreeFem++ installation.
 * 
 * @author Mar�a Jos� Cano, Universidad de Murcia, Murcia, Spain
 * @author Francisco Esquembre, Universidad de Murcia, Murcia, Spain <fem@um.es>
 * @author Invaluable help provided by Antoine Le Hyaric (Lab Jacques-Louis Lions, Paris, France)
 * @version 0.0 May 2012
 * @version 1.0 Dec 2012
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.colos.freefem.FreeFem;
import org.colos.freefem.ScriptOutput;

public class ServerConnection extends Connection {

  // Commands received from the server
  static private final char CMD_ARGV        = 'b';
  static private final char CMD_BIG_ENDIAN  = 'g';
  static private final char CMD_FFG         = 'r';
  static private final char CMD_FFGEND      = 'w';
  static private final char CMD_FFGINIT     = 's';
  static private final char CMD_NBOFPROC    = 'd';
  static private final char CMD_PROGRAM     = 'a';
  static private final char CMD_SETDIR      = 'v';
  static private final char CMD_SERVER_DONE = 'q';
  static private final char CMD_STDOUT      = 'n';
  static private final char CMD_USER        = 'e';
  static private final char CMD_VERSION     = 'c';
  static private final char CMD_PING        = 'h';
  static private final char CMD_PONG        = 'i';

  private FreeFem mFreeFem;

  public ServerConnection(FreeFem freeFem) {
    mFreeFem = freeFem;
  }
  
  @Override
  public ScriptOutput runScript(String script, int nProcessors) {
    Socket socket;
    try {
      socket = new Socket(mFreeFem.getServerURL(), mFreeFem.getServerPortNumber());
      socket.setSoTimeout(50000);
    }
    catch (UnknownHostException e) {
      setErrorCode(FreeFem.ErrorCode.UNKNWON_HOST_ERROR);
      return null;
    }
    catch (Exception e) { 
      setErrorCode(FreeFem.ErrorCode.CONNECTION_ERROR);
      return null;
    }

    boolean[] hasReadHeader = { false };
    boolean hasDifferentEndianness = true;
    ScriptOutput scriptOutput = new ScriptOutput(socket);
    try {    	   
      DataInputStream dis = new DataInputStream(socket.getInputStream());
      DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
      CommandReader reader = new CommandReader(scriptOutput, dis, dos, hasReadHeader);

      boolean bigEndian = false;
      while (true) { // Until the end of connection is found
        char command = Readpipe.readChar(dis);
        //System.out.println("Reading command "+command);
        switch (command) {
          case CMD_BIG_ENDIAN :
            bigEndian = (Readpipe.readBool(dis)==false); // I think FF sends isLittleEndian
            scriptOutput.setHasDifferentEndianness((getLocalBigEndianness()!=bigEndian));
            break;
          case CMD_VERSION :
            int version = Readpipe.readInt(dis, hasDifferentEndianness);
            int dialogVersion = getDialogVersion(); 
            if (version!=dialogVersion) {
              setErrorCode(FreeFem.ErrorCode.INCORRECT_VERSION_ERROR);
              return null;
            }
            sendToServer(dos,hasDifferentEndianness,dialogVersion);
            break;
          case CMD_USER :
            int challenge = Readpipe.readInt(dis, hasDifferentEndianness);
            sendToServer(dos,hasDifferentEndianness,mFreeFem.getUserName());
            sendToServer(dos,hasDifferentEndianness,encrypt(mFreeFem.getUserPassword() + Integer.toString(challenge)));
            break;
          case CMD_NBOFPROC : sendToServer(dos,hasDifferentEndianness,nProcessors); break;
          case CMD_SETDIR   : sendToServer(dos,hasDifferentEndianness,mFreeFem.getUserDirectory()); break;
          case CMD_PROGRAM  : sendToServer(dos,hasDifferentEndianness,script); break;
          case CMD_ARGV     :  sendToServer(dos,hasDifferentEndianness,""); break;
          default : 
            if (reader.readCommand(command, hasDifferentEndianness)) return scriptOutput;
            break;
        }
        // Maybe you want to add a check to terminate this loop if there are too many reads without an CMD_SERVER_DONE
        // if (tooManyChars) serverDone = true;
      } // end of while
    } catch (Exception exc) { 
      exc.printStackTrace(); 
      setErrorCode(FreeFem.ErrorCode.INPUT_OUTPUT_ERROR);
      return null;
    }
  }

  @Override
  public ScriptOutput continueReading(ScriptOutput scriptOutput){
    try {
      boolean[] hasReadHeader = { true }; 
      boolean hasDifferentEndianness = scriptOutput.getDiffEndianness();
      Socket socket = (Socket) scriptOutput.getCommObject();
      DataInputStream dis = new DataInputStream(socket.getInputStream());
      if (dis.available()==0) {
        setErrorCode(FreeFem.ErrorCode.INPUT_OUTPUT_ERROR);
        return null;
      }
      scriptOutput.setWaiting(false);
      DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
      CommandReader reader = new CommandReader(scriptOutput, dis, dos , hasReadHeader);
      while (true) { // Until the end of connection is found
        if (reader.readCommand(Readpipe.readChar(dis), hasDifferentEndianness)) return scriptOutput;
      }
    } catch (Exception exc) { 
      exc.printStackTrace(); 
      setErrorCode(FreeFem.ErrorCode.PROCESSING_INPUT_ERROR);
      return null;  
    }
  }

  // --------------------------------------
  // Private methods
  //--------------------------------------

  /**
   * Reads the commands common to runScript and continueReading
   */
  static private class CommandReader {
    private ScriptOutput mScriptOutput;
    private DataInputStream mDis;
    private DataOutputStream mDos;
    private boolean[] mHasReadHeader;

    // Implementation
    private ArrayList<Byte> mMessagesList = new ArrayList<Byte>();

    private CommandReader (ScriptOutput scriptOutput, DataInputStream dis, DataOutputStream dos, boolean[] hasReadHeader) {
      mScriptOutput = scriptOutput;
      mDis = dis;
      mDos = dos;
      mHasReadHeader = hasReadHeader;
    }

    /**
     * Read the command
     * @param command
     * @param hasDifferentEndianness
     * @return true if the reading should return a complete scriptOutput
     * @throws Exception if there is any error
     */
    boolean readCommand(char command, boolean hasDifferentEndianness) throws Exception {
      switch (command) {
        case CMD_STDOUT : 
          String message = Readpipe.readString(mDis, hasDifferentEndianness);
          System.out.println(message); // TODO: decide what to do with this
          break;
        case CMD_FFGINIT :  // Do nothing, FFG version's things
          break;
        case CMD_FFG : // read output
          int chunkLength = Readpipe.readInt(mDis, hasDifferentEndianness);
         // System.out.println("Reading a chunk of length "+chunkLength);
          byte[] chunk = new byte[chunkLength];
          mDis.readFully(chunk);
         // System.out.println("Done reading a chunk of length "+chunkLength);
          for (int i=0; i<chunk.length;i++) mMessagesList.add(chunk[i]);               
          break;
        case CMD_FFGEND : 
          byte[] messagesArray = new byte[mMessagesList.size()];
          for (int i=0, n=mMessagesList.size(); i<n; i++) messagesArray[i] = mMessagesList.get(i);
          PlotOutput plotOutput = readData(mHasReadHeader,hasDifferentEndianness,messagesArray);
          if (plotOutput==null) throw new Exception();
          mScriptOutput.addPlotOutput(plotOutput);
          if (plotOutput.wantsToPause()) {
            mScriptOutput.setWaiting(true);
            return true;
          }
          mMessagesList.clear();
          break;
        case CMD_SERVER_DONE :
          sendToServer(mDos,hasDifferentEndianness,"ok");
          mScriptOutput.close();
          return true;
        case CMD_PING:
          sendCharToServer(mDos, CMD_PONG);
          break;
        default : 
          System.err.println("FreeFemConnection : Ignoring unknown command: "+command); 
          break;
      }
      return false; // Not finished
    }

  }

  // --------------------------------------
  // Private utility methods
  //--------------------------------------

  static private PlotOutput readData(boolean[] hasReadHeader, boolean hasDifferentEndianness, byte[] messagesArray) throws IOException {
    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(messagesArray));
    if (!hasReadHeader[0]) { // read header
      hasReadHeader[0] = true;
      byte[] firstlinearray = new byte[16];
      dis.readFully(firstlinearray);
      String firstline = new String(firstlinearray);
      if (!firstline.contains(Visudata.FFFILEMAGIC)) throw new IOException();
    }
    PlotOutput plotOutput = new PlotOutput();
    // read blocks from the pipe containing different plots
    if (!Visudata.readBlock(dis, plotOutput,hasDifferentEndianness)) return null;
    return plotOutput;
  }

  static private int setEndianness(boolean hasDifferentEndianness, int value){
    if (hasDifferentEndianness) return((value&0xff)<<24)+((value&0xff00)<<8)+((value&0xff0000)>>8)+((value>>24)&0xff);
    return value;
  }

  static private void sendToServer(DataOutputStream dos, boolean hasDifferentEndianness, int value) throws IOException {	  
    dos.writeInt(setEndianness(hasDifferentEndianness,value));
    dos.flush();
  }
  static private void sendCharToServer(DataOutputStream dos, char value) throws IOException {   
    dos.writeByte(value);//It is waiting for only one byte
    dos.flush();
  }
  static private void sendToServer(DataOutputStream dos, boolean hasDifferentEndianness, String value) throws IOException {
    dos.writeInt(setEndianness(hasDifferentEndianness,value.length()));
    dos.write(value.getBytes());
    dos.flush();
  }

  static private int getDialogVersion() {
    return 1;
  }

  static private boolean getLocalBigEndianness() { // check local Endianness
    return java.nio.ByteOrder.nativeOrder().toString().contains("BIG");
  } 

  static private String encrypt(String message){
    byte[]digest = null;
    byte[]buffer = message.getBytes();
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");          
      md.reset();
      md.update(buffer);
      digest = md.digest();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return toHexadecimal(digest);
  }

  static private String toHexadecimal(byte[]digest){
    String hash = "";
    for(byte aux:digest){
      int b = aux & 0xff;
      if(Integer.toHexString(b).length()==1)hash +="0";
      hash += Integer.toHexString(b);
    }
    return hash;
  }

}

