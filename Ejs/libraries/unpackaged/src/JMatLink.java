// package org.colos.ejs.contrib.matlab;

/*****************************************************************************
*                           JMatLink                                         *
******************************************************************************
* (c) 1999-2001 Stefan Mueller  (email: stefan@held-mueller.de)              *
******************************************************************************
* 19.01.1999  beginning (reuse of HelloWorld example, Java Tutorial)  (0.01) *
* 06.02.1999  separation from gui                                     (0.02) *
* 27.03.1999  engGetScalar, engGetVector, engGetArray                 (0.03) *
* 02.05.1999  engGetScalar already works                                     *
* 20.05.1999  deleted engPutArray (float..., int...)                  (0.04) *
* 13.08.1999  thread implementation due to ActiveX thread problem     (0.05) *
* 15.08.1999  intruduced 2 locking mechanisms to lock the engine and         *
*                wait for data to be return to caller.                (0.06) *
* 16.08.1999  wait for all engine calls to return savely              (0.06a)*
* 29.08.1999  made all engine routines "synchronized"                 (0.07) *
* 11.09.1999  get char areray from workspace                           (0.08) *
* 21.09.1999  engGetCharArray working (conversion to double matrix first)    *
*             (transfer as double throught engine and conversion             *
*             back to double) engine doesn't support char arrays      (0.09) *
* 10.10.1999  engPutArray(String name, double[][] values)             (0.10) *
* 04/07/2001  restart work on JMatLink                                (0.11) *
* 05/24/2001  engOpenSingleUse                                        (0.11) *
* 05/24/2001  engClose(int pointer)                                   (0.11) *
* 06/04/2001  engEvalString(ep, evalString)                                  *
* 06/10/2001  use of maximum number of arguments of all engine functions     *
* 07/31/2001  setDebug() to suppress messages to the console window          *
* 08/09/2001  extensive documentation                                        *
* 08/09/2001  final version. upgrade version number                   (1.00) *
******************************************************************************/

//****************************************************************************
// The matlab-engine must only be called from the SAME thread                *
//   all the time!! On Windows systems the ActiveX implementation is         *
//   supposed to be the reason for this. I don't know if that happens        *
//   on other platforms, too.                                                *
// To achieve this. All native methods of this class, all accesses to        *
//   the engine, are called from the SAME thread. Since I don't know         *
//   how to call them directly I set up a mechanism to send messages         *
//   to that thread and ask it to process all requests. Some locking         *
//   mechanism locks up the engine for each single call in order             *
//   to stay out of concurrent situations / accesses.                        *
//****************************************************************************


/** ToDo list
* engOpen is supposed to throw an exception if not successfull
* I'm not sure what happens if large matrices are passed back and
     force between this class and other applications. Maybe I
     should always clear all global arrays engGetArray1dD...
* I'm not sure if the locking mechanism is good or bad. There
     may be a problem, if two subsequent calls to two different
     engine routines occur at the same time.
* what should I do with the return values of the engine functions? Throw
     an exception?
* make engPutArray also for int,
* make engGetArray/Scalar also for int
* make engGetCharArray also for different engine pointers
* make something like engGetOutputBuffer
*/


// **** Thread programming. Notify and start and stop ****
// Look at
// file:///e%7C/jdk1.2/docs/guide/misc/threadPrimitiveDeprecation.html
// on how to suspend, start, stop, interrupt a thread savely in JDK1.2.x
// The problem is ActiveX: one only can make calls to the engine library
// (especially engEvalString) from ONE single thread.

import java.io.*;


public class JMatLink extends Thread {

   // static declarations
   // the variable "status" is used to tell the main
   //   thread what to do.
   private final static int idleI             =   0;
   private final static int engOpenI          =   1;
   private final static int engCloseI         =   2;
   private final static int engEvalStringI    =   3;
   private final static int engGetScalarI     =   4;
   private final static int engGetVectorI     =   5;
   private final static int engGetArrayI      =   6;
   private final static int engPutArray2dI    =   9;
   private final static int engOutputBufferI  =  10;
   private final static int engGetCharArrayI  =  11;
   private final static int destroyJMatLinkI  =  12;
   private final static int engOpenSingleUseI =  13;

   // All variables are global to allow all methods
   //   and the main thread to share all data
   private int              status           =   idleI;
   private String           arrayS;
   private String           engEvalStringS;
   private String           engOutputBufferS;
   private double           engGetScalarD;
   private double[]         engGetVectorD;
   private double[][]       engGetArrayD;
   private double           engPutArrayD;
   private double[]         engPutArray1dD;
   private double[][]       engPutArray2dD;
   private String[]         engGetCharArrayS;
   private int              epI;                 /* Engine pointer */
   private int              retValI;             /* return Value of eng-methods */
   private String           startCmdS;           /* start command for engOpen... */
   private int              buflenI;             /* output buffer length         */
   private boolean          debugB            =  false;

   // Locks
   private boolean          lockEngineB       =  false;
   private boolean          lockThreadB       =  false;
   private boolean          lockWaitForValueB =  false;
   private boolean          destroyJMatLinkB  =  false;

   private Thread runner;

   // ***********************  native declarations  ****************************
   // NEVER call  native methods directly, like
   //   JMatLink.engEvalStringNATIVE("a=1"). Matlab's engine has quite some
   //   thread problems.
   private native void       displayHelloWorld();
   private native void       engTestNATIVE();

   private native int        engOpenNATIVE          (String startCmdS );
   private native int        engOpenSingleUseNATIVE (String startCmdS );

   private native int        engCloseNATIVE         (int epI);

   private native int        engEvalStringNATIVE    (int epI, String evalS );

   private native double     engGetScalarNATIVE     (int epI, String nameS );
   private native double[]   engGetVectorNATIVE     (int epI, String nameS );
   private native double[][] engGetArrayNATIVE      (int epI, String nameS );
   private native String[]   engGetCharArrayNATIVE  (int epI, String nameS );

   private native void       engPutArrayNATIVE      (int epI, String matrixS, double[][] valuesDD);

   private native String     engOutputBufferNATIVE  (int epI, int buflenI );

   private native void       setDebugNATIVE         (boolean debugB       );

    // ************************ JMatLink constructor ***************************
    /** This is the constructor for the JMatLink library.
    *
    * <p>E.g.:<br>
    * <pre>
    *   <b>JMatLink</b> engine = new <b>JMatLink()</b>;
    *   engine.engOpen();
    *   engine.engEvalString("surf(peaks)");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/

    private static boolean initialized = false;
 /*
    private static String MatlabDLL = "JMatLink";

    static {
      if (System.getProperty("os.name").startsWith("Windows")) MatlabDLL = "_library/external/JMatLink";
    }

    public JMatLink() { this (MatlabDLL); }
*/
    public JMatLink(String _dll) {
      if (!initialized) {
        try { // Modified by fem@um.es for use with Ejs' directory structure
          System.load(_dll); // _dll must be the full path of the DLL
        }
        catch (UnsatisfiedLinkError e) {
          e.printStackTrace();
          System.out.println("ERROR: Could not load the JMatLink library. This error occures, if the path to");
          System.out.println("       matlab's <matlab>\\bin directory is not set properly.");
          System.out.println("       Or if JMatLink.dll (for Windows) or  libJMatlink.jnilib (for MacOSX) is not");
          System.out.println("       in the right directory!");
        }
        initialized = true;
      }
      if (debugB) System.out.println("JMatLink constructor");
      runner = new Thread(this);
      runner.start();
    }

    // **** terminate running thread ****
    public void destroy() {
      destroyJMatLinkB = true;
      notifyAll();
    }

    public void kill() {
      destroyJMatLinkB = true;
      callThread(destroyJMatLinkI);
    }

//////////////////////////////////////////////////////////////////////////////


    // *****************************   engOpen   *****************************
    /** Open engine. This command is used to open a <b>single</b> connection
    *  to matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   JMatLink engine = new JMatLink();
    *   engine.<b>engOpen()</b>;
    *   engine.engEvalString("surf(peaks)");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized  int  engOpen()
    {
        return engOpen( "" );  // return value is pointer to engine
    }


    // *****************************   engOpen   *******************************
    /** Open engine. This command is used to open a <b>single</b> connection
    *   to matlab.<p> This command is only useful on unix systems. On windows
    *   the optional parameter <b>must</b> be NULL.
    *
    * <p>E.g.:<br>
    * <pre>
    *   JMatLink engine = new JMatLink();
    *   engine.<b>engOpen("commands to start matlab")</b>;
    *   engine.engEvalString("surf(peaks)");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized  int  engOpen(String startCmdS)
    {
        // startup MATLAB and set up connection
        lockEngineLock();
        lockWaitForValue();

        this.startCmdS = startCmdS;

        callThread( engOpenI );

        WaitForValue();
        releaseEngineLock();

        return this.epI;
    }


    // **************************   engOpenSingleUse   *****************************
    /** Open engine for single use. This command is used to open
    *  <b>multiple</b> connections to matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int a,b;
    *   JMatLink engine = new JMatLink();
    *   a = engine.<b>engOpenSingleUse()</b>;   // start first matlab session
    *   b = engine.<b>engOpenSingleUse()</b>;   // start second matlab session
    *   engine.engEvalString(a, "surf(peaks)");
    *   engine.engEvalString(b, "foo=ones(10,0)");
    *   engine.engClose(a);
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized int engOpenSingleUse()
    {
        return engOpenSingleUse("");
    }

    // **************************   engOpenSingleUse   *****************************
    /** Open engine for single use. This command is used to open
    *  <b>multiple</b> connections to matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int a,b;
    *   JMatLink engine = new JMatLink();
    *   a = engine.<b>engOpenSingleUse("start matlab")</b>;   // start first matlab session
    *   b = engine.<b>engOpenSingleUse("start matlab")</b>;   // start second matlab session
    *   engine.engEvalString(a, "surf(peaks)");
    *   engine.engEvalString(b, "foo=ones(10,0)");
    *   engine.engClose(a);
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized int engOpenSingleUse(String startCmdS)
    {
        lockEngineLock();
        lockWaitForValue();

        this.startCmdS = startCmdS;

        callThread( engOpenSingleUseI );

        WaitForValue();
        releaseEngineLock();

        return this.epI;
    }


    // *****************************   engClose   *****************************
    /** Close the connection to matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *  JMatLink engine = new JMatLink();
    *  engine.engOpen();
    *  engine.engEvalString("surf(peaks)");
    *  engine.<b>engClose()</b>;
    * </pre>
    ***************************************************************************/
    public synchronized void engClose()
    {
        engClose( this.epI );
    }


    // *****************************   engClose   *****************************
    /** Close a specified connection to an instance of matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *  int a,b;
    *  JMatLink engine = new JMatLink();
    *  a = engine.engOpenSingleUse();       // start first  matlab session
    *  b = engine.engOpenSingleUse();       // start second matlab session
    *  engine.engEvalString(b, "surf(peaks)");
    *  engine.engEvalString(a, "array = randn(23)");
    *  engine.<b>engClose</b>(a);      // Close the first  connection to matlab
    *  engine.<b>engClose</b>(b);      // Close the second connection to matlab
    * </pre>
    ***************************************************************************/
    public synchronized void engClose( int epI)
    {
        // close connection and terminate MATLAB
        lockEngineLock();
        lockWaitForValue();

        this.epI = epI;

        callThread( engCloseI );

        WaitForValue();
        releaseEngineLock();

        // return retValI; Return value indicates success
    }


    // *****************************   engEvalString   *****************************
    /** Evaluate an expression in matlab's workspace.
    *
    * E.g.:<br>
    * <pre>
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.<b>engEvalString</b>("surf(peaks)");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void engEvalString(String evalS)
    {
        engEvalString( this.epI, evalS);
    }


    // *****************************   engEvalString   *************************
    /** Evaluate an expression in a specified workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *  int a,b;
    *  JMatLink engine = new JMatLink();
    *  a = engine.engOpenSingleUse();
    *  engine.<b>engEvalString</b>(a, "surf(peaks)");
    *  engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void engEvalString(int epI, String evalS)
    {
        // evaluate expression "evalS" in specified engine Ep
        //if (debugB) System.out.println("eval(ep,String) in  " + epI + " "+evalS);
        lockEngineLock();
        lockWaitForValue();

        this.epI       = epI;
        engEvalStringS = evalS;

        callThread( engEvalStringI );

        WaitForValue();
        releaseEngineLock();

        //if (debugB) System.out.println("eval(ep,String) out "+epI+" "+evalS);
        // return retValI; Return value indicates success
    }


    // *****************************   engGetScalar   **************************
    /** Get a scalar value from matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *  double a;
    *  JMatLink engine = new JMatLink();
    *  engine.engOpen();
    *  engine.engEvalString("foo = sin( 3 )");
    *  a = engine.<b>engGetScalarValue</b>("foo");
    *  engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized double engGetScalar(String arrayS)
    {
        return engGetScalar( this.epI, arrayS);
    }


    // *****************************   engGetScalar   **************************
    /** Get a scalar value from a specified workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   double a;
    *   int b;
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSigleUse();
    *   engine.engEvalString(b, "foo = sin( 3 )");
    *   a = engine.<b>engGetScalarValue</b>(b, "foo");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized double engGetScalar(int epI, String arrayS)
    {
        // Get scalar value or element (1,1) of an array from
        //   MATLAB's workspace
        // Only real values are supported right now

        lockEngineLock();
        lockWaitForValue();

        /* copy parameters to global variables */
        this.epI    = epI;
        this.arrayS = arrayS;

        callThread( engGetScalarI );


        WaitForValue();
        releaseEngineLock();

        return engGetScalarD;
    }


    // *****************************   engGetVector   **************************
    /** Get an array (1 * n) from matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   double[] array;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("array = randn(10,1);");
    *   array = engine.<b>engGetVector</b>("array");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized  double[]  engGetVector( String arrayS )
    {
        return engGetVector( this.epI, arrayS );
    }

    // *****************************   engGetVector   **************************
    /** Get an array (1 * n) from a specified workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double[] array;
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSingleUse();
    *   engine.engEvalString(b, "array = randn(10,1);");
    *   array = engine.<b>engGetVector</b>(b, "array");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized  double[]  engGetVector( int epI, String arrayS )
    {
        // only real values are supported so far
        lockEngineLock();
        lockWaitForValue();

        this.epI    = epI;
        this.arrayS = arrayS;

        callThread( engGetVectorI );

        WaitForValue();
        releaseEngineLock();

        return engGetVectorD;
    }


    // *****************************   engGetArray   ***************************
    /** Get an array from matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double[][] array;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("array = randn(10);");
    *   array = engine.<b>engGetArray</b>("array");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized  double[][]  engGetArray( String arrayS )
    {
        return engGetArray( this.epI, arrayS );
    }


    // *****************************   engGetArray   ***************************
    /** Get an array from a specified instance/workspace of matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double[][] array;
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSingleUse();
    *   engine.engEvalString(b, "array = randn(10);");
    *   array = engine.<b>engGetArray</b>(b, "array");
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized  double[][]  engGetArray( int epI, String arrayS )
    {
        // only real values are supported so far
        lockEngineLock();
        lockWaitForValue();

        this.epI    = epI;
        this.arrayS = arrayS;

        callThread( engGetArrayI );
        WaitForValue();
        releaseEngineLock();

        return engGetArrayD;
    }

    // **************************   engGetCharArray   *****************************
    /** Get an 'char' array (string) from matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   String array;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("array = 'hello world';");
    *   array = engine.<b>engCharArray</b>("array");
    *   System.out.println("output = "+ array);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized String[] engGetCharArray(String arrayS)
    {
        // convert to double array
        engEvalString( "engGetCharArrayD=double(" + arrayS +")" );

        // get double array
        double[][] arrayD = engGetArray("engGetCharArrayD");

        // delete temporary double array
        engEvalString("clear engGetCharArrayD");

        // convert double back to char
        return double2String( arrayD );
    }


    // *****************************   engPutArray   ***************************
    /** Put an array into a specified workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int array = 1;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.<b>engPutArray</b>("array", array);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void engPutArray( String arrayS, int valueI )
    {
        engPutArray( this.epI, arrayS, new Integer(valueI).doubleValue());
    }


    // *****************************   engPutArray   ***************************
   // public synchronized void   engPutArray( String arrayS, int[] valuesI )
   // {
   //     engPutArray( this.epI, arrayS, (double[])valuesI );
   // }


    // *****************************   engPutArray   ***************************
    /** Put an array into matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   double array = 1;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.<b>engPutArray</b>("array", array);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void engPutArray( String arrayS, double valueD )
    {
        engPutArray( this.epI, arrayS, valueD);
    }


    // *****************************   engPutArray   *****************************
    /** Put an array into a specified instance/workspace of matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double array = 1;
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSingleUse();
    *   engine.<b>engPutArray</b>(b, "array", array);
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized void engPutArray( int epI, String arrayS, double valueD )
    {

        double vDD[][]   = {{0.0}};
               vDD[0][0] = valueD;
        engPutArray( epI, arrayS, vDD );  // nxn dimensional
    }


    // *****************************   engPutArray   ***************************
    /** Put an array (1 dimensional) into a specified instance/workspace of
    *   matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   double[] array = {1.0 , 2.0 , 3.0};
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.<b>engPutArray</b>("array", array);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void   engPutArray( String arrayS, double[] valuesD )
    {
        engPutArray( this.epI, arrayS, valuesD );
    }


    // *****************************   engPutArray   *****************************
    /** Put an array (1 dimensional) into a specified instance/workspace of
    *   matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double[] array = {1.0 , 2.0 , 3.0};
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSingleUse();
    *   engine.<b>engPutArray</b>(b, "array", array);
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized void   engPutArray(int epI, String arrayS, double[] valuesD)
    {
        double[][] vDD = new double[1][valuesD.length]; // 1xn array

        //if (debugB) System.out.println("length  = "+valuesD.length);

        vDD[0] = valuesD; // copy row

        engPutArray( epI, arrayS, vDD );
    }


    // *****************************   engPutArray   ***************************
    /** Put an array (2 dimensional) into matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   double[][] array={{1.0 , 2.0 , 3.0},
    *                     {4.0 , 5.0 , 6.0}};
    *   JMatLink engine = new JMatLink();
    *   engine.engOpenSingleUse();
    *   engine.<b>engPutArray</b>("array", array);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized void engPutArray( String arrayS, double[][] valuesDD )
    {
        engPutArray( this.epI, arrayS, valuesDD );
    }


    // *****************************   engPutArray   ***************************
    /** Put an array (2 dimensional) into a specified instance/workspace of
    *   matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   int b;
    *   double[][] array={{1.0 , 2.0 , 3.0},
    *                     {4.0 , 5.0 , 6.0}};
    *   JMatLink engine = new JMatLink();
    *   b = engine.engOpenSingleUse();
    *   engine.engPutArray(b, "array", array);
    *   engine.engClose(b);
    * </pre>
    ***************************************************************************/
    public synchronized void engPutArray(int epI, String arrayS, double[][] valuesDD)
    {
        // send an array to MATLAB
        // only real values are supported so far
        lockEngineLock();
        lockWaitForValue();

        this.epI            = epI;
        this.arrayS         = arrayS;
        this.engPutArray2dD = valuesDD;

        callThread( engPutArray2dI );

        WaitForValue();
        releaseEngineLock();
    }


    // *****************************   engOutputBuffer   ***********************
    /** Return the outputs of previous commands from matlab's workspace.
    *
    * <p>E.g.:<br>
    * <pre>
    *   String buffer;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("surf(peaks)");
    *   buffer = engine.<b>engOutputBuffer</b>();
    *   System.out.println("workspace " + buffer);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized String  engOutputBuffer( )
    {
        return engOutputBuffer( this.epI, this.buflenI );
    }

    // *****************************   engOutputBuffer   ***********************
    /** Return the outputs of previous commands from a specified instance/
    *   workspace form matlab.
    *
    * <p>E.g.:<br>
    * <pre>
    *   String buffer;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("surf(peaks)");
    *   buffer = engine.<b>engOutputBuffer</b>();
    *   System.out.println("workspace " + buffer);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized String  engOutputBuffer( int epI )
    {
        return engOutputBuffer( epI, this.buflenI );
    }

    // *****************************   engOutputBuffer   ***********************
    /** Return the ouputs of previous commands in matlab's workspace.
    *
    * Right now the parameter <i>buflen</i> is not supported.
    *
    * <p>E.g.:<br>
    * <pre>
    *   String buffer;
    *   JMatLink engine = new JMatLink();
    *   engine.engOpen();
    *   engine.engEvalString("surf(peaks)");
    *   buffer = engine.<b>engOutputBuffer</b>();
    *   System.out.println("workspace " + buffer);
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public synchronized String  engOutputBuffer( int epI, int buflenI )
    {
        // get the output buffer from MATLAB
        //if (debugB) System.out.println("Thread in: "+Thread.currentThread().getName());

        lockEngineLock();
        lockWaitForValue();

        this.epI     = epI;
        this.buflenI = buflenI;

        callThread( engOutputBufferI );

        WaitForValue();
        releaseEngineLock();
        //if (debugB) System.out.println("Thread out: "+Thread.currentThread().getName());

        return engOutputBufferS;
    }


    // *****************************  setDebug   *******************************
    /* Switch on or disable debug information printed to standard output.
    *
    * <p>Default setting is debug info disabled.
    * <p>E.g.:<br>
    * <pre>
    *   JMatLink engine = new JMatLink();
    *   engine.engOpenSingleUse();
    *   engine.<b>setDebug(true)</b>;
    *   engine.engEvalString("a=ones(10,5);");
    *   engine.engClose();
    * </pre>
    ***************************************************************************/
    public void setDebug( boolean debugB )
    {
        this.debugB = debugB;
        setDebugNATIVE( debugB );
    }


    ////////////////////////////////////////////////////////////////////////////////
    // This method notifys the main thread to call matlab's engine
    //    Since threads don't have methods, we set a variable which
    //    contains the necessary information about what to do.
    private synchronized void callThread(int status)
    {
        this.status = status;
        lockThreadB = false;
        notifyAll();
    }


////////////////////////////////////////////////////////////////////////////////
// The run methods does ALL calls to the native methods
// The keyword "synchronized" is neccessary to block the run()
//    method as long as one command needs to get executed.
public synchronized void run()
{
    int tempRetVal;

    //if (debugB) System.out.println("JMatLink: thread is running");
    while (true) {
//        System.out.println("Number of Java-Threads: "+Thread.activeCount()+"");
//        Thread thread = Thread.currentThread();
//        System.out.println("Name of active Java-Threads: "+thread.getName()+"");
//        System.out.println("active Java-Thread is Daemon: "+thread.isDaemon()+"");
//        System.out.println("getting "+arrayS+"");


     switch (status) {
     case engOpenI:          epI = engOpenNATIVE( startCmdS );
                             releaseWaitForValue();
                             break;

     case engOpenSingleUseI: epI = engOpenSingleUseNATIVE( startCmdS );
                             releaseWaitForValue();
                             break;

     case engCloseI:         retValI = engCloseNATIVE( epI );
                             releaseWaitForValue();
                             break;

     case engEvalStringI:    retValI = engEvalStringNATIVE(epI, engEvalStringS);
                             releaseWaitForValue();
                             break;

     case engGetScalarI:     engGetScalarD  = engGetScalarNATIVE(epI, arrayS );
                             releaseWaitForValue();
                             break;

     case engGetVectorI:     engGetVectorD  = engGetVectorNATIVE(epI, arrayS );
                             releaseWaitForValue();
                             break;

     case engGetArrayI:      engGetArrayD  = engGetArrayNATIVE(epI, arrayS );
                             releaseWaitForValue();
                             break;

     case engGetCharArrayI:  engGetCharArrayS  = engGetCharArrayNATIVE(epI, arrayS );
                             releaseWaitForValue();
                             break;

     case engPutArray2dI:    engPutArrayNATIVE( epI, arrayS, engPutArray2dD );
                             releaseWaitForValue();
                             break;

     case engOutputBufferI:  engOutputBufferS = engOutputBufferNATIVE( epI, buflenI );
                             releaseWaitForValue();
                             break;


     default:                // System.out.println("thread default switch statem.");
     }
     status=0;

     lockThreadB = true;
     while (lockThreadB == true) {
       synchronized(this) {
          try { wait();} // wait until next command is available
          catch (InterruptedException e) { }
       }
     }
     //System.out.println("JMatLink: thread awoke and passed lock");
     if (destroyJMatLinkB == true) break;
   } // end while
   //if (debugB) System.out.println("JMatLink: thread terminated");
} // end run


////////////////////////////////////////////////////////////////////////////////
// The MATLAB engine is served by a thread. Threads don't have methods
//   which can be called. So we need to send messages to that thread
//   by using notifyAll. In the meantime NO OTHER methods is allowed to
//   access our thread (engine) so we lock everything up.
   private void lockEngineLock(){
      synchronized(this){
         while (lockEngineB==true){
            try { //System.out.println("lockEngineLock locked");
                  wait();} // wait until last command is finished
            catch (InterruptedException e) { }
         }
         //System.out.println("lockEngineLock released");
         //now lockEngineB is false
         lockEngineB = true;
      }
   } // end lockEngine

   private synchronized void releaseEngineLock(){
      lockEngineB = false;
      notifyAll();
   }

////////////////////////////////////////////////////////////////////////////////
// The MATLAB engine is served by a thread. Threads don't have methods
//    which can be called directly. If we send a command that returns data
//    back to the calling function e.g. engGetArray("array"), we'll notify
//    the main thread to get the data from matlab. Since the data is collected
//    in another thread, we don't know exactly when the data is available, since
//    this is a concurrent situation.
//    The solution is simple: I always use a locking-mechanism to wait for the
//    data. The main thread will release the lock and the calling method can
//    return the data.
//
//    Steps:
//    1. a method that returns data calls the locking method
//    2. notify the thread to call matlab
//    3. wait for the returned data
//    4. after the thread itself got the data it releases the locks method
//    5. return data

   private synchronized void lockWaitForValue(){
      lockWaitForValueB = true;
   }

   private void WaitForValue(){
      synchronized(this){
         while (lockWaitForValueB==true){
            try { //System.out.println("lockWaitForValue locked");
                  wait();} // wait for return value
            catch (InterruptedException e) { }
         }
      }
      //System.out.println("WaitForValue released");
   } // end waitForValue

   private synchronized void releaseWaitForValue(){
      lockWaitForValueB = false;
      notifyAll();
   }


////////////////////////////////////////////////////////////////////////////////
////                        Utility methods                                 ////


  // Convert an n*n double array to n*1 String vector
  private String[] double2String(double[][] d)
  {
      String encodeS[]=new String[d.length];  // String vector

      // for all rows
      for (int n=0; n<d.length; n++){
          byte b[] = new byte[d[n].length];
          // convert row from double to byte
          for (int i=0; i<d[n].length ;i++) b[i]=(byte)d[n][i];

          // convert byte to String
          try { encodeS[n] = new String(b, "UTF8");}
          catch (UnsupportedEncodingException e) {}
      }
      return encodeS;
  } // end double2String

} // end class JMatLink
