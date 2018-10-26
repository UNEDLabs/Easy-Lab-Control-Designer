/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        Fehlberg78
 * Description:  7rd order with 8th order error control RK solver by Fehlberg
 * @author Francisco Esquembre
 * @version 1.0 January 2009
 */

public class Fehlberg78 extends AbstractAdaptiveRKSolverInterpolator {

  static final double 
  A_11 =   2.0/27.0,
  A_21 =   1.0/36.0, A_22 =  1.0/12.0,
  A_31 =   1.0/24.0,                   A_33 =  1.0/8.0,
  A_41 =   5.0/12.0,                   A_43 =-25.0/16.0, A_44 =  25.0/16.0,
  A_51 =   1.0/20.0,                                     A_54 =  1.0/4.0,  A_55 =   1.0/5.0,
  A_61 = -25.0/108.0,                                    A_64 =125.0/108.0, A_65 =-65.0/27.0, A_66 = 125.0/54.0,
  A_71 =  31.0/300.0,                                                       A_75 =  61.0/225.0, A_76 =  -2.0/9.0,   A_77 = 13.0/900.0,
  A_81 =   2.0,                                          A_84 =-53.0/6.0,   A_85 = 704.0/45.0,  A_86 =-107.0/9.0,   A_87 = 67.0/90.0 , 
    A_88 = 3.0,
  A_91 = -91.0/108.0,                                    A_94 = 23.0/108.0, A_95 = -976.0/135.0,A_96 = 311.0/54.0,  A_97 = -19.0/60.0,
    A_98 = 17.0/6.0,  A_99 = -1.0/12.0,
  A_101= 2383.0/4100.0,                                  A_104=-341.0/164.0,A_105= 4496.0/1025.0,A_106=-301.0/82.0, A_107 =2133.0/4100.0,
    A_108=45.0/82.0, A_109=45.0/164.0, A_1010 = 18.0/41.0,
  A_111= 3./205.0,                                                                               A_116=-6.0/41.0,   A_117 = -3.0/205.0,
    A_118=-3.0/41.0, A_119=3.0/41.0,   A_1110 = 6.0/41.0,
  A_121 = -1777.0/4100.0,                                A_124=-341.0/164.0,A_125=4496.0/1025.0, A_126=-289.0/82.0, A_127= 2193.0/4100.0,
    A_128=51.0/82.0, A_129=33.0/164.0, A_1210=12.0/41.0,                    A_1212=1.0;
  
  // 7th order method
  static private final double 
    B7_1 = 41.0/840.0, B7_6 = 34.0/105.0, B7_7 = 9.0/35.0, B7_8 = 9.0/35.0, B7_9 = 9.0/280.0, B7_10 = 9.0/280.0, B7_11 = 41.0/840.0;
  // 8th order error control
  static final double 
    B8_6 = 34.0/105.0, B8_7 = 9.0/35.0, B8_8 = 9.0/35.0, B8_9 = 9.0/280.0, B8_10 = 9.0/280.0, B8_12 = 41.0/840.0, B8_13 = 41.0/840.0;

  
//  private int counter = 0;
  
public Fehlberg78(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  private double[] rate2, rate3, rate4, rate5, rate6, rate7, rate8, rate9, rate10, rate11, rate12, rate13, order8;
  
  @Override
  protected void allocateOtherArrays() {
    super.allocateOtherArrays();
    rate2 = new double[dimension];
    rate3 = new double[dimension];
    rate4 = new double[dimension];
    rate5 = new double[dimension];
    rate6 = new double[dimension];
    rate7 = new double[dimension];
    rate8 = new double[dimension];
    rate9 = new double[dimension];
    rate10 = new double[dimension];
    rate11 = new double[dimension];
    rate12 = new double[dimension];
    rate13 = new double[dimension];
    order8 = new double[dimension];
  }
  
  @Override
  protected double getMethodOrder() { return 7; }

  @Override
  protected int getNumberOfEvaluations() { return 13; }
  
  @Override
  protected double computeApproximations(double _step) {
    computeIntermediateStep(_step, finalState);
    for (int i=0; i<dimension; i++) {
      order8[i] = initialState[i]+_step*(B8_6*rate6[i]+B8_7*rate7[i]+B8_8*rate8[i]+B8_9*rate9[i]+B8_10*rate10[i]+B8_12*rate12[i]+
          +B8_13*rate13[i]);
    }
    return super.computeError(order8);
  }

  @Override
  protected void computeFinalRate() {
    ode.getRate(finalState, finalRate);
  }
  
  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state){
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*A_11*initialRate[i];
    ode.getRate(_state, rate2);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_21*initialRate[i]+A_22*rate2[i]);
    ode.getRate(_state, rate3);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_31*initialRate[i]+A_33*rate3[i]);
    ode.getRate(_state, rate4);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_41*initialRate[i]+A_43*rate3[i]+A_44*rate4[i]);
    ode.getRate(_state, rate5);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_51*initialRate[i]+A_54*rate4[i]+A_55*rate5[i]);
    ode.getRate(_state, rate6);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_61*initialRate[i]+A_64*rate4[i]+A_65*rate5[i]+A_66*rate6[i]);
    ode.getRate(_state, rate7);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_71*initialRate[i]+A_75*rate5[i]+A_76*rate6[i]+A_77*rate7[i]);
    ode.getRate(_state, rate8);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_81*initialRate[i]+A_84*rate4[i]+
        A_85*rate5[i]+A_86*rate6[i]+A_87*rate7[i]+A_88*rate8[i]);
    ode.getRate(_state, rate9);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_91*initialRate[i]+A_94*rate4[i]+
        A_95*rate5[i]+A_96*rate6[i]+A_97*rate7[i]+A_98*rate8[i]+A_99*rate9[i]);
    ode.getRate(_state, rate10);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_101*initialRate[i]+A_104*rate4[i]+
        A_105*rate5[i]+A_106*rate6[i]+A_107*rate7[i]+A_108*rate8[i]+A_109*rate9[i]+A_1010*rate10[i]);
    ode.getRate(_state, rate11);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_111*initialRate[i]+
        A_116*rate6[i]+A_117*rate7[i]+A_118*rate8[i]+A_119*rate9[i]+A_1110*rate10[i]);
    ode.getRate(_state, rate12);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(A_121*initialRate[i]+A_124*rate4[i]+
        A_125*rate5[i]+A_126*rate6[i]+A_127*rate7[i]+A_128*rate8[i]+A_129*rate9[i]+A_1210*rate10[i]+A_1212*rate12[i]);
    ode.getRate(_state, rate13);
    
    for (int i=0; i<dimension; i++) {
      _state[i] = initialState[i]+_step*(B7_1*initialRate[i]+B7_6*rate6[i]+B7_7*rate7[i]+B7_8*rate8[i]+B7_9*rate9[i]+B7_10*rate10[i]+
          +B7_11*rate11[i]);
    }
    return _state;
  }

  @Override
  public double[] interpolate(double _time, boolean useLeftApproximation, double[] _state) { 
    return super.interpolateBootstrap2(_time, _state);
  }
  
}

/*
 * Open Source Physics software is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License (GPL) as
 * published by the Free Software Foundation; either version 2 of the License,
 * or(at your option) any later version.

 * Code that uses any portion of the code in the org.opensourcephysics package
 * or any subpackage (subdirectory) of this package must must also be be released
 * under the GNU GPL license.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2007  The Open Source Physics project
 *                     http://www.opensourcephysics.org
 */
