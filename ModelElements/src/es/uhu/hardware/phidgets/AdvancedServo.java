/*
 * Copyright (C) 2012 Francisco Esquembre / Marco A. Marquez / Andres Mejias  
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


package es.uhu.hardware.phidgets;

/*
 */
import com.phidgets.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class AdvancedServo extends AbstractPhidget {
    private AdvancedServoPhidget mDevice;
    HashMap<String,Integer>    servoTypes;

    protected Phidget createPhidget() throws PhidgetException {
      servoTypes = new HashMap<String, Integer>();
      servoTypes.put("DEFAULT", AdvancedServoPhidget.PHIDGET_SERVO_DEFAULT);
      servoTypes.put("RAW_us_MODE", AdvancedServoPhidget.PHIDGET_SERVO_RAW_us_MODE);
      servoTypes.put("HITEC_HS322HD", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS322HD);
      servoTypes.put("HITEC_HS5245MG", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS5245MG);
      servoTypes.put("HITEC_805BB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_805BB);
      servoTypes.put("HITEC_HS422", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS422);
      servoTypes.put("TOWERPRO_MG90", AdvancedServoPhidget.PHIDGET_SERVO_TOWERPRO_MG90);
      servoTypes.put("HITEC_HSR1425CR", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HSR1425CR);
      servoTypes.put("HITEC_HS785HB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS785HB);
      servoTypes.put("HITEC_HS485HB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS485HB);
      servoTypes.put("HITEC_HS645MG", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS645MG);
      servoTypes.put("HITEC_815BB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_815BB);
      return mDevice = new AdvancedServoPhidget();
    }


    // -----------------------------------
    // Methods particular of this Phidget
    // -----------------------------------

//    AdvancedServoPhidget mDevice;
//    String hostPhidget;
//    int portPhidget;
//    int serialNumber;   
//    int numberServos;
//    
//    String passwd;
//    String DeviceName;
//    String LibraryVersion;
//    HashMap<String,Integer>    servoTypes;
    

//    public AdvancedServo() {
//        try {
//            servo = new AdvancedServoPhidget();
//            LibraryVersion=servo.getLibraryVersion();
//            servoTypes = new HashMap();
//            servoTypes.put("DEFAULT", AdvancedServoPhidget.PHIDGET_SERVO_DEFAULT);
//            servoTypes.put("RAW_us_MODE", AdvancedServoPhidget.PHIDGET_SERVO_RAW_us_MODE);
//            servoTypes.put("HITEC_HS322HD", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS322HD);
//            servoTypes.put("HITEC_HS5245MG", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS5245MG);
//            servoTypes.put("HITEC_805BB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_805BB);
//            servoTypes.put("HITEC_HS422", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS422);
//            servoTypes.put("TOWERPRO_MG90", AdvancedServoPhidget.PHIDGET_SERVO_TOWERPRO_MG90);
//            servoTypes.put("HITEC_HSR1425CR", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HSR1425CR);
//            servoTypes.put("HITEC_HS785HB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS785HB);
//            servoTypes.put("HITEC_HS485HB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS485HB);
//            servoTypes.put("HITEC_HS645MG", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS645MG);
//            servoTypes.put("HITEC_815BB", AdvancedServoPhidget.PHIDGET_SERVO_HITEC_815BB);
//
//        } catch (PhidgetException ex) {
//            JOptionPane.showMessageDialog(null, "PhidgetException",
//					     "Loading Error",
//					     JOptionPane.ERROR_MESSAGE); 
//        }
//    }
         
//    public void clear() {
//        try {
//            mDevice.close();
//        } catch (PhidgetException ex) {
//            JOptionPane.showMessageDialog(null, "PhidgetException",
//					     "Close Error",
//					     JOptionPane.ERROR_MESSAGE);
//        }
//
//   }
    
//     public void dispose() {
//     
//      try { mDevice.close(); } catch (PhidgetException ex) { ex.printStackTrace(); }
//    }
    
//    public void setup(String hostPhidget,int portPhidget,int serialNumber,String passwd){
//    try {
//        this.hostPhidget=hostPhidget;
//        this.portPhidget=portPhidget;
//        this.serialNumber=serialNumber;
//        this.passwd=passwd;
//        servo.open(serialNumber,hostPhidget,portPhidget,passwd);
//        servo.waitForAttachment();
//        numberServos=servo.getMotorCount();
//        DeviceName=servo.getDeviceName();
//    } catch (PhidgetException ex) {
//        JOptionPane.showMessageDialog(null, "PhidgetException",
//                                         "Setup Error",
//                                         JOptionPane.ERROR_MESSAGE);
//    }
//}

    public boolean setServoType(int ind,int indType){
        try {
            mDevice.setServoType(indType, indType);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo type");
          return false;      
        }
    }
    
    
    public boolean setServoType(int ind,String stringType){    
        try {
            int indType; 
            indType=servoTypes.get(stringType);
            mDevice.setServoType(ind, indType);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo type");
          return false;   
        }
    }
 
    
    public int getServoType(int ind){
        try {
          return mDevice.getServoType(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting servo type");
          return -1;
        }     
     }  
        
    public int getMotorCount(){
      try {
        return mDevice.getMotorCount();
      } catch (PhidgetException ex) {
        if (isVerbose()) errorMessage("Error getting number of motors");
        return -1;
      }     
   }  
    
    public String getDeviceName(){
      try {
        return mDevice.getDeviceName();
      } catch (PhidgetException ex) {
        if (isVerbose()) errorMessage("Error getting device name");
        return null;
      }     
   }  
    
    public boolean setPosition(int ind,double  value){
        try {
            mDevice.setPosition(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo position");
          return false; 
        }
    }
           
    
    public boolean setPositionMax(int ind,double  value){
        try {
            mDevice.setPositionMax(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo maximum position");
          return false; 
        }
    }
     
     
    public boolean setPositionMin(int ind,double  value){
        try {
            mDevice.setPositionMin(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo minimum position");
          return false; 
        }
    }
          
    
    double getPosition(int ind){
        try {
            return mDevice.getPosition(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting servo position");
          return Double.NaN;
        }     
     }   
         
    
    
    double getPositionMax(int ind){
        try {
            return mDevice.getPositionMax(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting maximum position");
          return Double.NaN;
        }     
     }
             
    double getPositionMin(int ind){
        try {
            return mDevice.getPositionMin(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting minimum position");
          return Double.NaN;
        }     
     }
         
    
    public boolean setAcceleration(int ind,double  value){
        try {
            mDevice.setAcceleration(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo acceleration");
          return false; 
        }
    }
              
    
    double getAcceleration(int ind){
        try {
            return mDevice.getAcceleration(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting acceleration");
          return Double.NaN;
        }     
     }   
         
    double getAccelerationMax(int ind){
        try {
            return mDevice.getAccelerationMax(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting maximum acceleration");
          return Double.NaN;
        }     
     }
             
    double getAccelerationMin(int ind){
        try {
            return mDevice.getAccelerationMin(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting minimum acceleration");
          return Double.NaN;
        }     
     }
    
    double getVelocity(int ind){
        try {
            return mDevice.getVelocity(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting velocity");
          return Double.NaN;
           
        }     
     }   
    
    public boolean setVelocityLimit(int ind,double  value){
        try {
            mDevice.setVelocityLimit(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo velocity limit");
          return false; 
        }
    }
    
    double getVelocityLimit(int ind){
        try {
            return mDevice.getVelocityLimit(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting velocity limit");
          return Double.NaN;
        }     
     }   
    
    double getVelocityMax(int ind){
        try {
            return mDevice.getVelocityMax(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting maximum velocity");
          return Double.NaN;
        }     
     } 
    
        
    double getVelocityMin(int ind){
        try {
            return mDevice.getVelocityMin(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting minimum velocity");
          return Double.NaN;
        }     
     } 
    
    
    public double getCurrent(int ind){
        try {
            return mDevice.getCurrent(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting current");
          return Double.NaN;                  
        }
    }

    
    public boolean setSpeedRamping(int ind,boolean en){
        try {
            mDevice.setSpeedRampingOn(ind, en);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo speed ramping");
          return false; 
        }
    }
 
       
    public boolean getSpeedRampingOn(int ind){
        try {
            return mDevice.getSpeedRampingOn(ind);
            
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting speed ramping status");
             return false;                    
        }
    }
     
    
     
    public boolean enableServo(int ind, boolean value){
        try {
            mDevice.setEngaged(ind, value);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error enabling servo");
          return false; 
        }
    } 
    
     public boolean getEnable(int ind){
        try {
            return mDevice.getEngaged(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting enable status");
            return false;                    
        }
    }
     
    public boolean getStopped(int ind){
        try {
            return mDevice.getStopped(ind);
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error getting stopped status");
            return false;                    
        }
    } 
    
    public boolean setServoParameters(int ServoIndex, double MinUs, double MaxUs, double Degrees, double VelocityMax){
        try {
            mDevice.setServoParameters(ServoIndex, MinUs, MaxUs, Degrees,VelocityMax);
            return true;
        } catch (PhidgetException ex) {
          if (isVerbose()) errorMessage("Error setting servo parameters");
          return false; 
        }
    }
    
    
    
    
    
    ///////////////////////////////////
    
//    public static final void test(String args[]) throws Exception {
//		AdvancedServoPhidget servo;
//                
//
//		System.out.println(Phidget.getLibraryVersion());
//
//
//		servo = new AdvancedServoPhidget();
//		servo.addAttachListener(new AttachListener() {
//			public void attached(AttachEvent ae) {
//				System.out.println("attachment of " + ae);
//			}
//		});
//		servo.addDetachListener(new DetachListener() {
//			public void detached(DetachEvent ae) {
//				System.out.println("detachment of " + ae);
//			}
//		});
//		servo.addErrorListener(new ErrorListener() {
//			public void error(ErrorEvent ee) {
//				System.out.println("error event for " + ee);
//			}
//		});
//		servo.addServoPositionChangeListener(new ServoPositionChangeListener()
//		{
//			public void servoPositionChanged(ServoPositionChangeEvent oe)
//			{
//				System.out.println(oe);
//			}
//		});
//
//		servo.openAny();
//		System.out.println("waiting for AdvancedServo attachment...");
//		servo.waitForAttachment();
//
//		System.out.println("Serial: " + servo.getSerialNumber());
//		System.out.println("Servos: " + servo.getMotorCount());
//
//                //Initialize the Advanced Servo
//                servo.setEngaged(0, false);
//                servo.setSpeedRampingOn(0, false);
//                
//                servo.setPosition(0, 50);
//                servo.setEngaged(0, true);
//                Thread.sleep(500);
//                
//                System.out.println();
//                System.out.println("Start Position: " + servo.getPosition(0));
//                
//                servo.setSpeedRampingOn(0, true);
//                servo.setAcceleration(0,servo.getAccelerationMin(0));
//                servo.setVelocityLimit(0, 200);
//                servo.setPosition(0, 150);
//                
//		System.out.println("Outputting events.  Press Enter to stop");
//		System.in.read();
//                
//		System.out.print("closing...");
//                System.out.println();
//		servo.close();
//		servo = null;
//		System.out.println(" ok");
//	}
  /////////////////////////////////////////////////////////////////////////////////////////////  
  
    
    public static final void main(String args[]) throws Exception {  
        int numberServos;
        String DeviceName;
        AdvancedServo servo=new AdvancedServo();
        servo.connect(177653);// edit with your serial number
        numberServos = servo.getMotorCount();
        DeviceName = servo.getDeviceName();
        System.out.println("* * Phidgets Advanced Servo * *");
        System.out.println("Device Name: " + DeviceName);
        System.out.println("Number of motors: " + numberServos);
        //System.out.println(AdvancedServoPhidget.PHIDGET_SERVO_HITEC_HS422);
        //System.out.println(servo.servoTypes.get("HITEC_HS322HD"));
        System.out.println(servo.servoTypes.keySet());
        //Set<String> set =servo.servoTypes.keySet();
        //int ind=servo.servoTypes.get("HITEC_HS422"); 
        
        Iterator<String> iterator = servo.servoTypes.keySet().iterator();
        while(iterator.hasNext()){        
            System.out.println(iterator.next());
        }
        Iterator<Integer> iterator1 = servo.servoTypes.values().iterator();
        while(iterator1. hasNext()){        
            System.out.println(iterator1.next());
        }
        Iterator<Entry<String, Integer>> iterator2 = servo.servoTypes.entrySet().iterator();
        while(iterator2.hasNext()){        
            System.out.println(iterator2.next());
        }
        for(int i=0;i<numberServos;i++){
            servo.setServoType(i,"HITEC_HS422");  
            servo.enableServo(i, true);      
        }
        System.out.println("Device name: " + DeviceName);
        System.out.println("SERVO N. 0");
        servo.setPositionMax(0, 180);
        //servo.setAcceleration(0, 30);
        servo.setPosition(0, 0);
        System.out.println("Position: " + servo.getPosition(0));
        System.out.println("Max Position: " + servo.getPositionMax(0));
        System.out.println("Min Position: " + servo.getPositionMin(0));
        System.out.println("Acceleration: " + servo.getAcceleration(0));
        System.out.println("Max acceleration: " + servo.getAccelerationMax(0));
        System.out.println("Min acceleration: " + servo.getAccelerationMin(0));
        System.out.println("Max velocity: " + servo.getVelocityMax(0));
        System.out.println("Min Velocity: " + servo.getVelocityMin(0));
        servo.setPosition(0,180);
        System.out.println("New Position: " + servo.getPosition(0));
        servo.close();
    }     
        
}
