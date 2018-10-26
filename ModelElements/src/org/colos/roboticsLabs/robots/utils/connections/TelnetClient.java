package org.colos.roboticsLabs.robots.utils.connections;

/**
 * @author Almudena Ruiz
 */

import java.net.*;
import java.io.*;

public class TelnetClient{
	
	private static final int BUFFERSIZE = 64;	
	private String mHostIP, mUser, mPassword;
	private int mPortNumber = 23;
	private Socket mSocket;
	private InputStream mIn;
	private OutputStream mOut;

	
	public TelnetClient (String hostIP, int portNumber, String user, String password){
		this.mHostIP = hostIP;
		this.mPortNumber = portNumber;
		this.mUser = user;
		this.mPassword = password;
	}
	
	/**
	 * Establishes a Telnet connection
	 * @return true if the connection is established and false in otherwise
	 */
	
	public boolean createTelnetConnection (){
		try {
			mSocket = new Socket(mHostIP, mPortNumber);
			mIn = mSocket.getInputStream();
			mOut = mSocket.getOutputStream();
			
			@SuppressWarnings("unused")
      int recvMsgSize = 0;
			byte [] byteBuffer = new byte[BUFFERSIZE];
			
			recvMsgSize = mIn.read(byteBuffer, 0, 3);
			//System.out.println(recvMsgSize);
			//System.out.println(new String(byteBuffer));
			for (int i=0; i<BUFFERSIZE; i++){byteBuffer[i] = 0;}
			
			recvMsgSize = mIn.read(byteBuffer, 0, 7);
			//System.out.println(recvMsgSize);
			//System.out.println(new String(byteBuffer));
			for (int i=0; i<BUFFERSIZE; i++){byteBuffer[i] = 0;}
			
			mOut.write((mUser+ "\r\n").getBytes());
			mOut.flush();
			
			recvMsgSize = mIn.read(byteBuffer, 0, (mUser + "\r\n").length());
			//System.out.println(recvMsgSize);
			//System.out.println("echo: " + new String(byteBuffer));
			for (int i=0; i<BUFFERSIZE; i++){byteBuffer[i] = 0;}
			
			recvMsgSize = mIn.read(byteBuffer, 0, 10);
			//System.out.println(recvMsgSize);
			//System.out.println(new String(byteBuffer));
			for (int i=0; i<BUFFERSIZE; i++){byteBuffer[i] = 0;}
			
			mOut.write((mPassword + "\r\n").getBytes());
			mOut.flush();
			
			recvMsgSize = mIn.read(byteBuffer, 0, (mPassword + "\r\n").length());
			//System.out.println(recvMsgSize);
			//System.out.println("echo: " + new String(byteBuffer));
			for (int i=0; i<BUFFERSIZE; i++){byteBuffer[i] = 0;}
			
			byteBuffer = new byte[BUFFERSIZE];
			String line = "";
			while (mIn.read(byteBuffer, 0, 1) > 0){
				if (byteBuffer[0] == 0x0a) break;
				line = line + new String(byteBuffer);
			}
			
		} catch (UnknownHostException e) {
			System.out.println("Host " + mHostIP + " on port " + mPortNumber + " not found.");
			e.printStackTrace();
			return  false;
		} catch (IOException e) {
			System.out.println("Error found while creating socket or getting input and output stream.");
			try {
				mSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Closes the Telnet connection 
	 * @return true if the connection is closed and false in otherwise
	 */
	public boolean closeTelnetConnection (){
		try {
			mOut.write(("LOGOUT\r\n").getBytes());
			mOut.flush();		
			mSocket.close();
		} catch (IOException e) {
			System.out.println("Error found while closing.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Reads a line of a buffer
	 * @return the line read
	 */
	
	public String read (){
		byte [] byteBuffer = new byte[1];
		String line = "";
		try {
			while (mIn.read(byteBuffer, 0, 1) > 0){
				if (byteBuffer[0] == 0x0a) break;
				line = line + new String(byteBuffer);
			}
		} catch (IOException e) {
			System.out.println("Error found while reading.");
			e.printStackTrace();
			return null;
		}		
		return line;
	}
	
	/**
	 * Writes a command in the buffer
	 * @param command the command written
	 * @return true if the command has been written and false in otherwise
	 */
	
	public boolean write (String command){
		try {
			byte [] byteBuffer = new byte[BUFFERSIZE];
			mOut.write((command + "\r\n").getBytes());
			mOut.flush();
			if (mIn.read(byteBuffer, 0, (command + "\r\n").length()) != (command + "\r\n").length()) return false;
			else return true;
		} catch (IOException e) {
			System.out.println("Error found while writing.");
			e.printStackTrace();
			return false;
		}
	}
}

