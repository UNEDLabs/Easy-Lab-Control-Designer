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

import java.io.DataInputStream;
import java.io.IOException;

public class Readpipe {	

  static public long readLong(DataInputStream _dis, boolean changeEndianness) throws IOException{
    byte[] array = new byte[8];
    _dis.readFully(array);
    long code=0;
    if (changeEndianness)  
      for (int i=7; i>=0; i--) code = (code << 8) + (array[i] & 0xFF);
    else 
      for (int i=0; i<8; i++) code = (code << 8) + (array[i] & 0xFF);
    return code;
  }

  static public int readInt(DataInputStream _dis, boolean changeEndianness) throws IOException{
    byte[] array = new byte[4];
    _dis.readFully(array);
    int code=0;
    if (changeEndianness)	
      for (int i=3; i>=0; i--) code = (code << 8) + (array[i] & 0xFF);
    else 
      for (int i=0; i<4;  i++) code = (code << 8) + (array[i] & 0xFF);
    return code;
  }

  static public char readChar(DataInputStream _dis) throws IOException{
    byte[] array = new byte[1];
    _dis.readFully(array);	
    return (char) array[0];
  }

//  static public float readFloat(DataInputStream _dis, boolean changeEndianness) throws IOException{
//    byte[] array = new byte[8];
//    _dis.readFully(array);
//    long code=0;
//    if (changeEndianness)	
//      for (int i=7; i>=0; i--) code |= ( (long)( array[i] & 0xff ) ) << 8*i;
//    else 
//      for (int i=0; i<8;  i++) code |= ( (long)( array[i] & 0xff ) ) << 8*i;
//    return (float) (Double.longBitsToDouble(code));
//  }

  static public double readDouble(DataInputStream _dis, boolean changeEndianness) throws IOException{
    byte[] array = new byte[8];
    _dis.readFully(array);
    long code=0;
    if(changeEndianness)	
      for (int i=7; i>=0; i--) code |= ( (long)( array[i] & 0xff ) ) << 8*i;
    else 
      for (int i=0; i<8;  i++) code |= ( (long)( array[i] & 0xff ) ) << 8*i;
    return Double.longBitsToDouble(code);
  }

  static public boolean readBool(DataInputStream _dis) throws IOException{
    byte[] array = new byte[1];
    _dis.readFully(array);
    return array[0]!=0;
  }
  
  static public String readString(DataInputStream _dis, boolean changeEndianness) throws IOException{
    int l = readInt(_dis, changeEndianness);
    byte[] linearray = new byte[l];
    _dis.readFully(linearray);
    return new String(linearray);
  }

  static public double[] readDoubleArray(DataInputStream _dis, boolean changeEndianness)throws IOException{
    long n = readLong(_dis, changeEndianness);
    if (n<0) throw new IOException();
    double[] v = new double[(int) n];
    for (int i=0; i<n; i++) v[i] = readDouble(_dis, changeEndianness);
    return v;
  }

  static public double[][] readDoubleArray2D(DataInputStream _dis, boolean changeEndianness, int dimension) throws IOException{
    long n = readLong(_dis, changeEndianness);
    if (n<0) throw new IOException();
    double[][] v = new double[(int) n][dimension];
    for (int i=0; i<n; i++) {
      double[] v_i = v[i];
      for (int j=0; j<dimension;j++) v_i[j] = readDouble(_dis, changeEndianness);
    }
    return v;
  }
  static public double[][] readValues(DataInputStream _dis, boolean changeEndianness, int dimension) throws IOException{
	    long n = readLong(_dis, changeEndianness);
	    if (n<0) throw new IOException();
	    double[][] v = new double[(int) n/dimension][dimension];
	    for (int i=0; i<v.length; i++) {
	      double[] v_i = v[i];
	      for (int j=0; j<dimension;j++) v_i[j] = readDouble(_dis, changeEndianness);
	    }
	    return v;
	  }
  static public int[][] readIntArray2D(DataInputStream _dis, boolean changeEndianness, int dimension) throws IOException{
    long n = readLong(_dis, changeEndianness);
    if (n<0) throw new IOException();
    int numberOfElem = (int) n / (dimension+1);
    int[][] v = new int[numberOfElem][dimension+1];
    for (int i=0; i<numberOfElem; i++) {
      int[] v_i = v[i];
      for (int j=0; j<=dimension;j++)  v_i[j] = readInt(_dis, changeEndianness);
    }
    return v;
  }

  static public void check(int number, DataInputStream _dis, boolean changeEndianness) throws IOException {
    int integerFlag = readInt(_dis, changeEndianness);
    if (integerFlag != number) {
      System.err.println("Readpipe check error: Trying to read something different than received");
      throw new IOException();
    }
  }

  static public boolean checkAndReadBoolean(DataInputStream _dis, boolean changeEndianness) throws IOException{
    check(1, _dis, changeEndianness);
    return Readpipe.readBool(_dis);	
  }
  
  static public long checkAndReadLong(DataInputStream _dis, boolean changeEndianness) throws IOException{
    check(2, _dis, changeEndianness);
    return Readpipe.readLong(_dis, changeEndianness);	
  }
  
  static public String checkAndReadString(DataInputStream _dis, boolean changeEndianness) throws IOException{
    check(6, _dis, changeEndianness);
    return Readpipe.readString(_dis, changeEndianness);	
  }

  static public double checkAndReadDouble(DataInputStream _dis, boolean changeEndianness) throws IOException{
    check(5, _dis, changeEndianness);
    return Readpipe.readDouble(_dis, changeEndianness); 	
  }

  static public double[] checkAndReadArray(DataInputStream _dis, boolean changeEndianness) throws IOException{
    check(10, _dis, changeEndianness);
    if (readInt(_dis, changeEndianness) != 8) System.err.println("checkreadpipe: wrong type for array elements");
    return Readpipe.readDoubleArray(_dis, changeEndianness);  
  }

//  static public double[][] checkAndReadArray2D(DataInputStream _dis, boolean changeEndianness, int dimension) throws IOException{
//    check(10, _dis, changeEndianness);
//    if (readInt(_dis, changeEndianness) != 8) System.err.println("checkreadpipe: wrong type for array elements");
//    return Readpipe.readDoubleArray2D(_dis, changeEndianness, dimension);	
//  }

}
