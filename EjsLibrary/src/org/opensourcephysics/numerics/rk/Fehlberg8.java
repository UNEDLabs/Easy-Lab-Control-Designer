/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.rk;

/**
 * Title:        Fehlberg8
 * Description:  8rd order fixed step RK solver by Fehlberg with Hermite interpolation
 * @author Francisco Esquembre
 * @version 1.0 January 2009
 */

public class Fehlberg8 extends AbstractExplicitRKSolverInterpolator {

public Fehlberg8(org.opensourcephysics.numerics.ODE _ode) {
    this.ode = _ode;
  }

  private double[] rate2, rate3, rate4, rate5, rate6, rate7, rate8, rate9, rate10, rate11, rate12, rate13;
  
  @Override
  protected void allocateOtherArrays() {
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
  }
  
  @Override
  protected int getNumberOfEvaluations() { return 13; }
  
      
  @Override
  protected double[] computeIntermediateStep(double _step, double[] _state){
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*Fehlberg78.A_11*initialRate[i];
    ode.getRate(_state, rate2);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_21*initialRate[i]+Fehlberg78.A_22*rate2[i]);
    ode.getRate(_state, rate3);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_31*initialRate[i]+Fehlberg78.A_33*rate3[i]);
    ode.getRate(_state, rate4);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_41*initialRate[i]+Fehlberg78.A_43*rate3[i]+Fehlberg78.A_44*rate4[i]);
    ode.getRate(_state, rate5);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_51*initialRate[i]+Fehlberg78.A_54*rate4[i]+Fehlberg78.A_55*rate5[i]);
    ode.getRate(_state, rate6);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_61*initialRate[i]+Fehlberg78.A_64*rate4[i]+Fehlberg78.A_65*rate5[i]+Fehlberg78.A_66*rate6[i]);
    ode.getRate(_state, rate7);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_71*initialRate[i]+Fehlberg78.A_75*rate5[i]+Fehlberg78.A_76*rate6[i]+Fehlberg78.A_77*rate7[i]);
    ode.getRate(_state, rate8);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_81*initialRate[i]+Fehlberg78.A_84*rate4[i]+
        Fehlberg78.A_85*rate5[i]+Fehlberg78.A_86*rate6[i]+Fehlberg78.A_87*rate7[i]+Fehlberg78.A_88*rate8[i]);
    ode.getRate(_state, rate9);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_91*initialRate[i]+Fehlberg78.A_94*rate4[i]+
        Fehlberg78.A_95*rate5[i]+Fehlberg78.A_96*rate6[i]+Fehlberg78.A_97*rate7[i]+Fehlberg78.A_98*rate8[i]+Fehlberg78.A_99*rate9[i]);
    ode.getRate(_state, rate10);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_101*initialRate[i]+Fehlberg78.A_104*rate4[i]+
        Fehlberg78.A_105*rate5[i]+Fehlberg78.A_106*rate6[i]+Fehlberg78.A_107*rate7[i]+Fehlberg78.A_108*rate8[i]+Fehlberg78.A_109*rate9[i]+Fehlberg78.A_1010*rate10[i]);
    ode.getRate(_state, rate11);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_111*initialRate[i]+
        Fehlberg78.A_116*rate6[i]+Fehlberg78.A_117*rate7[i]+Fehlberg78.A_118*rate8[i]+Fehlberg78.A_119*rate9[i]+Fehlberg78.A_1110*rate10[i]);
    ode.getRate(_state, rate12);
    for (int i=0; i<dimension; i++) _state[i] = initialState[i]+_step*(Fehlberg78.A_121*initialRate[i]+Fehlberg78.A_124*rate4[i]+
        Fehlberg78.A_125*rate5[i]+Fehlberg78.A_126*rate6[i]+Fehlberg78.A_127*rate7[i]+Fehlberg78.A_128*rate8[i]+Fehlberg78.A_129*rate9[i]+Fehlberg78.A_1210*rate10[i]+Fehlberg78.A_1212*rate12[i]);
    ode.getRate(_state, rate13);
    
    for (int i=0; i<dimension; i++) {
      _state[i] = initialState[i]+_step*(Fehlberg78.B8_6*rate6[i]+Fehlberg78.B8_7*rate7[i]+Fehlberg78.B8_8*rate8[i]+Fehlberg78.B8_9*rate9[i]
                                        +Fehlberg78.B8_10*rate10[i]+Fehlberg78.B8_12*rate12[i]+Fehlberg78.B8_13*rate13[i]);
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
