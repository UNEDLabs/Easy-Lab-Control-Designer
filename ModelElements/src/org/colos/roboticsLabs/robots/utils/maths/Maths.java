package org.colos.roboticsLabs.robots.utils.maths;

/**
 * @author Almudena Ruiz
 */

public class Maths {
  
  /**
   * Multiplies a scalar by a vector
   * @param e, a scalar
   * @param v, a vector
   * @return the result of this multiplication
   */

 public static double[] multiplyByScalar(double e, double v[])
 {
     for(int i = 0; i < v.length; i++)
         v[i] = e * v[i];

     return v;
 }
 
 /**
  * Calculates the scalar product of two vectors
  * @param v1
  * @param v2
  * @return the result of the scalar product
  */

 public static double productEscalarV(double v1[], double v2[])
 {
     double res = 0.0D;
     if(v1.length != v2.length)
     {
         System.out.println("Vector must have the same lenght");
     } else
     {
         for(int i = 0; i < v1.length; i++)
             res += v1[i] * v2[i];

     }
     return res;
 }
 
 /**
  * Calculates the subtraction of two vectors
  * @param v1
  * @param v2
  * @return the result of this subtraction
  */

 public static double[] restV(double v1[], double v2[])
 {
     double res[] = (double[])null;
     if(v1.length != v2.length)
     {
         System.out.println("Vector must have the same lenght");
     } else
     {
         res = new double[v1.length];
         for(int i = 0; i < v1.length; i++)
             res[i] = v1[i] - v2[i];

     }
     return res;
 }
 
 /**
  * Calculates the maximum value of a vector 
  * @param v, a vector
  * @return the maximum value
  */

 public static double maxValue(double v[])
 {
     double max = v[0];
     for(int i = 1; i < v.length; i++)
     {
         double temp = v[i];
         if(temp >= max)
             max = temp;
     }

     return max;
 }
}
