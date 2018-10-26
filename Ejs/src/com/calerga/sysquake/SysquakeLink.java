/*
 *	SysquakeLink.java - call Sysquake from Java
 *	(on Windows with OLE Automation or on unix with XML-RPC)
 *	Java code is the same, only JNI differs.
 *
 *	Copyright 2005-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

import java.net.*;

public class SysquakeLink implements Runnable
{
        private static class VarListeners
        {
                private SQLinkVariableListener [] listeners = null;
                private int [] instanceId = null;
                private int n = 0;
                public synchronized int count()
                {
                        return n;
                }
                public synchronized void add(int id, SQLinkVariableListener listener)
                {
                        // ensure enough room
                        if (instanceId == null)
                        {
                                listeners = new SQLinkVariableListener[16];
                                instanceId = new int[16];
                        }
                        else if (n >= instanceId.length)
                        {
                                int count = instanceId.length;
                                SQLinkVariableListener[] vtmp = new SQLinkVariableListener[count + 16];
                                for (int i = 0; i < count; i++)
                                        vtmp[i] = listeners[i];
                                listeners = vtmp;
                                int [] itmp = new int[count + 16];
                                for (int i = 0; i < count; i++)
                                        itmp[i] = instanceId[i];
                                instanceId = itmp;
                        }
                        // add
                        instanceId[n] = id;
                        listeners[n] = listener;
                        n++;
                }
                private int findById(int id)
                {
                        if (instanceId == null)
                                return -1;
                        for (int i = 0; i < n; i++)
                                if (instanceId[i] == id)
                                        return i;
                        return -1;
                }
                public synchronized void remove(int id)
                {
                        int i = findById(id);
                        for (int j = i; j + 1 < n; j++)
                        {
                                instanceId[j] = instanceId[j + 1];
                                listeners[j] = listeners[j + 1];
                        }
                        n--;
                }
                public synchronized SQLinkVariableListener getListener(int id)
                {
                        int i = findById(id);
                        return i >= 0 ? listeners[i] : null;
                }
        };


  private static boolean initialized = false;

  public SysquakeLink(String _dll) {
    if (!initialized && _dll!=null) {
      try { // Modified by fem@um.es for use with Ejs' directory structure
        System.load(_dll); // _dll must be the full path to the dll
      }
      catch (UnsatisfiedLinkError e) {
        e.printStackTrace();
        System.err.println("ERROR: Could not load the SysquakeLink library. This error occures if");
        System.err.println("       Sysquake is not installed in the system.");
        System.err.println("       Or if SysquakeLink.dll (for Windows) in the right directory!");
      }
      initialized = true;
    }
  }

  /*
        static
        {
                String lib;
                if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0)
                        lib = "com/calerga/sysquake/SysquakeLink_MacOSX.so";
                else if (System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0)
                        lib = "com/calerga/sysquake/SysquakeLink_Linux.so";
                else
                        lib = "com/calerga/sysquake/SysquakeLink_Win32.dll";
                URL libUrl = SysquakeLink.class.getClassLoader().getResource(lib);
                String path;
                try
                {
                        path = libUrl.toURI().getPath();
                }
                catch (URISyntaxException e)
                {
                        path = "-";
                }
                System.err.println ("Path to sysquake is "+path);
                System.load(path);
        }

        */
        private static int notificationPort = -1;

        /** Connect to Sysquake, which must already be running. */
        public static native void connect()
                throws SQLinkConnectionException;

        /** Disconnect from Sysquake. */
        public static native void disconnect();

        /** Check if the connection with Sysquake has been successfully established.
                @return true if the connected with Sysquake */
        public static native boolean isConnected();

        /** Get the version of Sysquake.
                @return Sysquake version as a string
                @throws SQLinkConnectionException if there is a communication error */
        public static native String version()
                throws SQLinkConnectionException, SQLinkInstanceIDException;

        /** Execute a command as if it was typed in the command window.
                @param cmd LME command to execute
                @throws SQLinkConnectionException if there is a communication error */
        public static native void execute(String cmd)
                throws SQLinkConnectionException;

        /** Open an SQ or SQD file.
                @param path File path, absolute or relative to Sysquake
                @return instance ID of the opened file
                @throws SQLinkConnectionException if there is a communication error */
        public static native int open(String path)
                throws SQLinkConnectionException;

        /** Reload an SQ or SQD file.
                @param instanceId instance ID of the file to reload
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid */
        public static native void reload(int instanceId)
                throws SQLinkConnectionException, SQLinkInstanceIDException;

        /** Maximize Sysquake main window
                (no effect on all platforms).
                @throws SQLinkConnectionException if there is a communication error */
        public static native void maximize()
                throws SQLinkConnectionException;

        /** Minimize Sysquake main window
                (no effect on all platforms).
                @throws SQLinkConnectionException if there is a communication error */
        public static native void minimize()
                throws SQLinkConnectionException;

        /** Restore size of Sysquake main window
                (no effect on all platforms).
                @throws SQLinkConnectionException if there is a communication error */
        public static native void restoreSize()
                throws SQLinkConnectionException;

        /** Show Sysquake
                (no effect on all platforms).
                @throws SQLinkConnectionException if there is a communication error */
        public static native void show()
                throws SQLinkConnectionException;

        /** Quit Sysquake. The connection is then lost.
                @throws SQLinkConnectionException if there is a communication error */
        public static native void quit()
                throws SQLinkConnectionException;

        /** Get the name of all variables of a given instance.
                @param instanceId instance ID
                @return name of all variables of the specified instance as an array of strings
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid */
        public static native String[] variableNames(int instanceId)
                throws SQLinkConnectionException, SQLinkInstanceIDException;

        /** Get the value of a variable given by name for a given instance.
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                @param instanceId instance ID
                @param name name of the variable
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object variableValue(int instanceId, String name)
                throws SQLinkConnectionException, SQLinkInstanceIDException,
                        SQLinkUnsupportedDataTypeException, SQLinkVariableException;

        /** Get the value of a variable given by index (0 corresponds to
                the first variable given by variableNames()) for a given instance.
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                @param instanceId instance ID
                @param index 0-based index of the variable
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object variableValue(int instanceId, int index)
                throws SQLinkConnectionException, SQLinkInstanceIDException,
                        SQLinkUnsupportedDataTypeException, SQLinkVariableException;

        /** Get the value of all the variables specified by name for a given instance.
                The result is an array whose entries correspond to the entries of names[].
                @param instanceId instance ID
                @param names array of names of the variables
                @return array of values corresponding to the requested variables
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object[] variableValue(int instanceId, String[] names)
                throws SQLinkConnectionException, SQLinkInstanceIDException,
                        SQLinkUnsupportedDataTypeException, SQLinkVariableException;

        /** Get the value of multiple variables for a given SQ instance.
                The result is an array whose entries correspond to the entries of indices[].
                @param instanceId instance ID
                @param indices array of 0-based variable indices
                @return array of values corresponding to the requested variables
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object[] variableValue(int instanceId, int[] indices)
                throws SQLinkConnectionException, SQLinkInstanceIDException,
                        SQLinkUnsupportedDataTypeException, SQLinkVariableException;

        /** Change the value of a single variable and force a redraw.
                @param instanceId instance ID
                @param name variable name
                @param value new value
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkVariableException if the variable is invalid */
        public static native void setVariableValue(int instanceId, String name, Object value)
                throws SQLinkConnectionException, SQLinkInstanceIDException, SQLinkVariableException;

        /** Change the value of a single variable and force a redraw.
                @param instanceId instance ID
                @param index variable index
                @param value new value
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkVariableException if the variable is invalid */
        public static native void setVariableValue(int instanceId, int index, Object value)
                throws SQLinkConnectionException, SQLinkInstanceIDException, SQLinkVariableException;

        /** Change the value of multiple variables and force a redraw.
                @param instanceId instance ID
                @param names variable names (array of strings)
                @param values new values (array of objects)
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkVariableException if the variable is invalid */
        public static native void setVariableValue(int instanceId, String[] names, Object[] values)
                throws SQLinkConnectionException, SQLinkInstanceIDException, SQLinkVariableException;

        /** Change the value of multiple variables and force a redraw.
                @param instanceId instance ID
                @param indices variable names (array of int)
                @param values new values (array of objects)
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid
                @throws SQLinkVariableException if the variable is invalid */
        public static native void setVariableValue(int instanceId, int[] indices, Object[] values)
                throws SQLinkConnectionException, SQLinkInstanceIDException, SQLinkVariableException;

        /** Set a variable change notification for executing LME code.
                @param instanceId instance ID
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid */
        public static native void setVariableChangeNotification(int instanceId, String src)
                throws SQLinkConnectionException, SQLinkInstanceIDException;

        /** Get the name of all variables defined in the context of the Command window.
                @return name of all variables as an array of strings
                @throws SQLinkConnectionException if there is a communication error */
        public static native String[] lmeVariableNames()
                throws SQLinkConnectionException;

        /** Get the value of a variable given by name in the context of the command window.
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                @param name name of the variable
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object lmeVariableValue(String name)
                throws SQLinkConnectionException, SQLinkUnsupportedDataTypeException,
                        SQLinkVariableException;

        /** Get the value of a variable given by index in the context of the command window.
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                @param index 0-based index of the variable
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object lmeVariableValue(int index)
                throws SQLinkConnectionException, SQLinkUnsupportedDataTypeException,
                        SQLinkVariableException;

        /** Get the value of all the variables specified by name in the context of the command
                window. The result is an array whose entries correspond to the entries of names[].
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                Existing variables with undefined value (such as ans after Sysquake has been
                launched) have a null entry.
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object[] lmeVariableValue(String[] names)
                throws SQLinkConnectionException, SQLinkUnsupportedDataTypeException,
                        SQLinkVariableException;

        /** Get the value of multiple variables given by index in the context of the command window.
                Scalar numbers are given as boxed native types; character arrays are
                given as strings; arrays are given as arrays of native types.
                @param indices array of 0-based variable indices
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkUnsupportedDataTypeException if the data type is unsupported
                @throws SQLinkVariableException if the variable is invalid */
        public static native Object[] lmeVariableValue(int[] indices)
                throws SQLinkConnectionException, SQLinkUnsupportedDataTypeException,
                        SQLinkVariableException;

        private static VarListeners varListeners = null;
        private static volatile Thread varChangedThread = null;

        /** Set a variable change notification.
                @param instanceId instance ID
                @param vcl object whose variableChange method is called whenever a variable of
                the specified SQ instance is changed in Sysquake
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid */
        public static void setVariableChangeNotification(int instanceId, SQLinkVariableListener vcl)
                throws SQLinkConnectionException, SQLinkInstanceIDException
        {
                if (varChangedThread == null)
                {
                        varListeners = new VarListeners();
                        varListeners.add(instanceId, vcl);
                        varChangedThread = new Thread(new SysquakeLink(null));
                        varChangedThread.start();
                }
                while (notificationPort < 0)
                        try
                        {
                                Thread.sleep(20);
                        }
                        catch (InterruptedException e)
                        {
                        }
                setVariableChangeNotification(instanceId,
                                "_fd_varChangedSQLinkNot=socketnew('localhost',"
                                                + notificationPort
                                                + ",socketset('Proto','udp'));"
                                        + "fprintf(_fd_varChangedSQLinkNot,'%d',"
                                                + instanceId
                                                + ");"
                                        + "fclose(_fd_varChangedSQLinkNot);");
        }

        /** Reset the variable change notification.
                @param instanceId instance ID
                @throws SQLinkConnectionException if there is a communication error
                @throws SQLinkInstanceIDException if the instance ID is invalid */
        public static void resetVariableChangeNotification(int instanceId)
                throws SQLinkConnectionException, SQLinkInstanceIDException
        {
                if (varListeners!=null) varListeners.remove(instanceId); // Paco added the if check
                setVariableChangeNotification(instanceId, "");

                if (varListeners!=null && varListeners.count() == 0) // Paco added the if check
                {
                        Thread moribund = varChangedThread;
                        varChangedThread = null;
                        moribund.interrupt();
                        varListeners = null;
                }
        }

        /** Notification thread (private) */
        public void run()
        {
                try
                {
                        DatagramSocket socket = new DatagramSocket();
                        socket.setSoTimeout(100);
                        notificationPort = socket.getLocalPort();
                        byte buffer[] = new byte[256];
                        while (varChangedThread != null)
                        {
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                while (varChangedThread != null)
                                        try
                                        {
                                                socket.receive(packet);
                                                break;
                                        }
                                        catch (SocketTimeoutException e)
                                        {
                                        }
                                if (varChangedThread == null)
                                        break;

                                // get instance ID
                                int instanceId;
                                try
                                {
                                        String datagram = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                                        instanceId = Integer.decode(datagram);
                                } catch (Exception e)
                                {
                                        instanceId = -1;
                                }

                                SQLinkVariableListener listener = varListeners.getListener(instanceId);
                                if (listener != null)
                                        listener.variableChange(instanceId);
                        }
                        socket.close();
                        notificationPort = -1;
                } catch (Exception e)
                {
                        // System.err.println("Notification thread: " + e);
                }
        }
}
