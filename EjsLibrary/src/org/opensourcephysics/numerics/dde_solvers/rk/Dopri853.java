/*
 * Open Source Physics software is free software as described near the bottom of this code file.
 *
 * For additional information and documentation on Open Source Physics please see: 
 * <http://www.opensourcephysics.org/>
 */

package org.opensourcephysics.numerics.dde_solvers.rk;

import org.opensourcephysics.numerics.dde_solvers.AbstractDiscreteTimeAdaptiveSolverInterpolator;
import org.opensourcephysics.numerics.dde_solvers.interpolation.*;

/**
 * Title:        Dopri853
 * Description:  Dorman Prince 8 ODE solver with 7th order dense output
 * @author Francisco Esquembre (based on code by Andrei Goussev)
 * @author Maria Jose Cano
 * @version 1.0 May 2010
 * @version 2 Feb 2011
 */
public class Dopri853 extends AbstractDiscreteTimeAdaptiveSolverInterpolator {

  static private final double
  A_11 = 5.26001519587677318785587544488E-2,
  A_21 = 1.97250569845378994544595329183E-2, A_22 = 5.91751709536136983633785987549E-2,
  A_31 = 2.95875854768068491816892993775E-2, A_32 = 0.0, A_33 = 8.87627564304205475450678981324E-2,
  A_41 = 2.41365134159266685502369798665E-1, A_42 = 0.0, A_43 = -8.84549479328286085344864962717E-1,
  A_44 = 9.24834003261792003115737966543E-1,
  A_51 =  3.7037037037037037037037037037E-2, A_52 = 0.0, A_53 = 0.0, A_54 = 1.70828608729473871279604482173E-1,
  A_55 = 1.25467687566822425016691814123E-1,
  A_61 = 3.7109375E-2, A_62 = 0.0, A_63 = 0.0, A_64 = 1.70252211019544039314978060272E-1,
  A_65 = 6.02165389804559606850219397283E-2, A_66 = -1.7578125E-2,
  A_71 = 3.70920001185047927108779319836E-2, A_72 = 0.0, A_73 = 0.0, A_74 = 1.70383925712239993810214054705E-1,
  A_75 = 1.07262030446373284651809199168E-1, A_76 = -1.53194377486244017527936158236E-2, 
  A_77 = 8.27378916381402288758473766002E-3,
  A_81 = 6.24110958716075717114429577812E-1, A_82 = 0.0, A_83 = 0.0, A_84 = -3.36089262944694129406857109825E0,
  A_85 = -8.68219346841726006818189891453E-1, A_86 = 2.75920996994467083049415600797E1,
  A_87 =  2.01540675504778934086186788979E1, A_88 = -4.34898841810699588477366255144E1,
  A_91 = 4.77662536438264365890433908527E-1, A_92 = 0.0, A_93 = 0.0, A_94 = -2.48811461997166764192642586468E0,
  A_95 = -5.90290826836842996371446475743E-1, A_96 =  2.12300514481811942347288949897E1,
  A_97 =  1.52792336328824235832596922938E1,  A_98 = -3.32882109689848629194453265587E1,
  A_99 = -2.03312017085086261358222928593E-2,
  A_101 = -9.3714243008598732571704021658E-1, A_102 = 0.0, A_103 = 0.0, A_104 = 5.18637242884406370830023853209E0,
  A_105 = 1.09143734899672957818500254654E0,  A_106 = -8.14978701074692612513997267357E0,
  A_107 = -1.85200656599969598641566180701E1, A_108 = 2.27394870993505042818970056734E1,
  A_109 = 2.49360555267965238987089396762E0,  A_1010 = -3.0467644718982195003823669022E0,
  A_111 = 2.27331014751653820792359768449E0,    A_112 = 0.0, A_113 = 0.0, A_114 = -1.05344954667372501984066689879E1,
  A_115 = -2.00087205822486249909675718444E0, A_116 = -1.79589318631187989172765950534E1,
  A_117 = 2.79488845294199600508499808837E1,  A_118 = -2.85899827713502369474065508674E0,
  A_119 = -8.87285693353062954433549289258E0, A_1110 = 1.23605671757943030647266201528E1,
  A_1111 = 6.43392746015763530355970484046E-1;
	
	// B8 are the 11th order coefficients
  static private final double B8_1 = 5.42937341165687622380535766363E-2, B8_2 = 0.0, B8_3 = 0.0, B8_4 = 0.0, B8_5 = 0.0,
  B8_6 = 4.45031289275240888144113950566E0, B8_7 = 1.89151789931450038304281599044E0,
  B8_8 = -5.8012039600105847814672114227E0, B8_9 = 3.1116436695781989440891606237E-1,
  B8_10 = -1.52160949662516078556178806805E-1, B8_11 = 2.01365400804030348374776537501E-1,
  B8_12 = 4.47106157277725905176885569043E-2;

  static private final double AD13_1 = 5.61675022830479523392909219681E-2, AD13_2 = 0, AD13_3 = 0, AD13_4 = 0, AD13_5 = 0, AD13_6 = 0,
  AD13_7 = 2.53500210216624811088794765333E-1,  AD13_8  = -2.46239037470802489917441475441E-1,
  AD13_9 = -1.24191423263816360469010140626E-1, AD13_10 = 1.5329179827876569731206322685E-1,
  AD13_11 = 8.20105229563468988491666602057E-3, AD13_12 = 7.56789766054569976138603589584E-3,
  AD13_13 = -8.298E-3;

  static private final double AD14_1 = 3.18346481635021405060768473261E-2, AD14_2 = 0, AD14_3 = 0, AD14_4 = 0, AD14_5 = 0,
  AD14_6 = 2.83009096723667755288322961402E-2, AD14_7 = 5.35419883074385676223797384372E-2,
  AD14_8 = -5.49237485713909884646569340306E-2, AD14_9 = 0, AD14_10 = 0,
  AD14_11 = -1.08347328697249322858509316994E-4, AD14_12 = 3.82571090835658412954920192323E-4,
  AD14_13 = -3.40465008687404560802977114492E-4, AD14_14 = 1.41312443674632500278074618366E-1;

  static private final double AD15_1 = -4.28896301583791923408573538692E-1, AD15_2 = 0, AD15_3 = 0, AD15_4 = 0, AD15_5 = 0,
  AD15_6 = -4.69762141536116384314449447206E0, AD15_7 = 7.68342119606259904184240953878E0,
  AD15_8 = 4.06898981839711007970213554331E0,  AD15_9 = 3.56727187455281109270669543021E-1, AD15_10 = 0, AD15_11 = 0, AD15_12 = 0,
  AD15_13 = -1.39902416515901462129418009734E-3, AD15_14 = 2.9475147891527723389556272149E0, AD15_15 = -9.15095847217987001081870187138E0;

  static private final double D4_1 = -0.84289382761090128651353491142E+01, D4_2 = 0, D4_3 = 0, D4_4 = 0, D4_5 = 0,
  D4_6 = 0.56671495351937776962531783590E+00, D4_7 = -0.30689499459498916912797304727E+01,
  D4_8 = 0.23846676565120698287728149680E+01, D4_9 = 0.21170345824450282767155149946E+01,
  D4_10 = -0.87139158377797299206789907490E+00, D4_11 = 0.22404374302607882758541771650E+01,
  D4_12 = 0.63157877876946881815570249290E+00,  D4_13 = -0.88990336451333310820698117400E-01,
  D4_14 = 0.18148505520854727256656404962E+02,  D4_15 = -0.91946323924783554000451984436E+01,
  D4_16 = -0.44360363875948939664310572000E+01;

  static private final double D5_1 = 0.10427508642579134603413151009E+02, D5_2 = 0, D5_3 = 0, D5_4 = 0, D5_5 = 0,
  D5_6 = 0.24228349177525818288430175319E+03,  D5_7 = 0.16520045171727028198505394887E+03,
  D5_8 = -0.37454675472269020279518312152E+03, D5_9 = -0.22113666853125306036270938578E+02,
  D5_10 = 0.77334326684722638389603898808E+01, D5_11 = -0.30674084731089398182061213626E+02,
  D5_12 = -0.93321305264302278729567221706E+01, D5_13 = 0.15697238121770843886131091075E+02,
  D5_14 = -0.31139403219565177677282850411E+02, D5_15 = -0.93529243588444783865713862664E+01,
  D5_16 = 0.35816841486394083752465898540E+02;

  static private final double D6_1 = 0.19985053242002433820987653617E+02, D6_2 = 0, D6_3 = 0, D6_4 = 0, D6_5 = 0,
  D6_6 = -0.38703730874935176555105901742E+03, D6_7 = -0.18917813819516756882830838328E+03,
  D6_8 = 0.52780815920542364900561016686E+03,  D6_9 = -0.11573902539959630126141871134E+02,
  D6_10 = 0.68812326946963000169666922661E+01, D6_11 = -0.10006050966910838403183860980E+01,
  D6_12 = 0.77771377980534432092869265740E+00, D6_13 = -0.27782057523535084065932004339E+01,
  D6_14 = -0.60196695231264120758267380846E+02,D6_15 = 0.84320405506677161018159903784E+02,
  D6_16 = 0.11992291136182789328035130030E+02;

  static private final double D7_1 = -0.25693933462703749003312586129E+02, D7_2 = 0, D7_3 = 0, D7_4 = 0, D7_5 = 0,
  D7_6 = -0.15418974869023643374053993627E+03, D7_7 = -0.23152937917604549567536039109E+03,
  D7_8 = 0.35763911791061412378285349910E+03,  D7_9 = 0.93405324183624310003907691704E+02,
  D7_10 = -0.37458323136451633156875139351E+02,  D7_11 = 0.10409964950896230045147246184E+03,
  D7_12 = 0.29840293426660503123344363579E+02, D7_13 = -0.43533456590011143754432175058E+02,
  D7_14 = 0.96324553959188282948394950600E+02, D7_15 = -0.39177261675615439165231486172E+02,
  D7_16 = -0.14972683625798562581422125276E+03;

  // E3 are the 3th order error coefficients
  static private final double E3_1 = -1.898007540724076157147023288760E-01, E3_2 = 0, E3_3 = 0, E3_4 = 0, E3_5 = 0,
  E3_6 = 4.45031289275240888144113950566E+00, E3_7 = 1.89151789931450038304281599044E+00,
  E3_8 = -5.8012039600105847814672114227E+00, E3_9 = -4.22682321323791962932445679177E-01,
  E3_10 = -1.52160949662516078556178806805E-01, E3_11 = 2.01365400804030348374776537501E-01,
  E3_12 = 2.26517921983608258118062039631E-02;

  // E5 are the 5th order error coefficients
  static private final double E5_1 = 0.1312004499419488073250102996E-01, E5_2 = 0, E5_3 = 0, E5_4 = 0, E5_5 = 0,
  E5_6 = -0.1225156446376204440720569753E+01, E5_7 = -0.4957589496572501915214079952E+00,
  E5_8 = 0.1664377182454986536961530415E+01,  E5_9 = -0.3503288487499736816886487290E+00,
  E5_10 = 0.3341791187130174790297318841E+00, E5_11 = 0.8192320648511571246570742613E-01,
  E5_12 = -0.2235530786388629525884427845E-01;
	
	private double[] mRate2, mRate3, mRate4, mRate5, mRate6, mRate7, mRate8, mRate9, mRate10, mRate11, mRate12;
	private double[] mRate14, mRate15, mRate16, mAuxState;
	private double[][] mCoeffs;
	
	public Dopri853(org.opensourcephysics.numerics.ODE _ode) {
		super(_ode);
	}
	
	@Override
	protected int getNumberOfEvaluations() { return 16; }
	
	@Override
	protected void allocateOtherArrays() {
		super.allocateOtherArrays();
		mRate2 = new double[mDimension];
		mRate3 = new double[mDimension];
		mRate4 = new double[mDimension];
		mRate5 = new double[mDimension];
		mRate6 = new double[mDimension];
		mRate7 = new double[mDimension];
		mRate8 = new double[mDimension];
		mRate9 = new double[mDimension];
		mRate10 = new double[mDimension];
		mRate11 = new double[mDimension];
		mRate12 = new double[mDimension];
		mRate14 = new double[mDimension];
		mRate15 = new double[mDimension];
		mRate16 = new double[mDimension];
		mAuxState = new double[mDimension];
		mCoeffs = new double [8][mDimension];
	}

	@Override
	protected double[] computeIntermediateStep(double step, double[] state) {
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*A_11*mInitialRate[i];
    mWrapper.evaluateRate(state, mRate2);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_21*mInitialRate[i]+A_22*mRate2[i]);
    mWrapper.evaluateRate(state, mRate3);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_31*mInitialRate[i]+A_32*mRate2[i]+A_33*mRate3[i]);
    mWrapper.evaluateRate(state, mRate4);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_41*mInitialRate[i]+A_42*mRate2[i]+A_43*mRate3[i]+A_44*mRate4[i]);
    mWrapper.evaluateRate(state, mRate5);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_51*mInitialRate[i]+A_52*mRate2[i]+A_53*mRate3[i]+A_54*mRate4[i]+A_55*mRate5[i]);
    mWrapper.evaluateRate(state, mRate6);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_61*mInitialRate[i]+A_62*mRate2[i]+A_63*mRate3[i]+A_64*mRate4[i]+
	        A_65*mRate5[i]+A_66*mRate6[i]);
    mWrapper.evaluateRate(state, mRate7);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_71*mInitialRate[i]+A_72*mRate2[i]+A_73*mRate3[i]+A_74*mRate4[i]+
	        A_75*mRate5[i]+A_76*mRate6[i]+A_77*mRate7[i]);
    mWrapper.evaluateRate(state, mRate8);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_81*mInitialRate[i]+A_82*mRate2[i]+A_83*mRate3[i]+A_84*mRate4[i]+
	        A_85*mRate5[i]+A_86*mRate6[i]+A_87*mRate7[i]+A_88*mRate8[i]);
    mWrapper.evaluateRate(state, mRate9);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_91*mInitialRate[i]+A_92*mRate2[i]+A_93*mRate3[i]+A_94*mRate4[i]+
	        A_95*mRate5[i]+A_96*mRate6[i]+A_97*mRate7[i]+A_98*mRate8[i]+A_99*mRate9[i]);
    mWrapper.evaluateRate(state, mRate10);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_101*mInitialRate[i]+A_102*mRate2[i]+A_103*mRate3[i]+A_104*mRate4[i]+
	        A_105*mRate5[i]+A_106*mRate6[i]+A_107*mRate7[i]+A_108*mRate8[i]+A_109*mRate9[i]+A_1010*mRate10[i]);
    mWrapper.evaluateRate(state, mRate11);
	  for (int i=0; i<mDimension; i++) state[i] = mInitialState[i]+step*(A_111*mInitialRate[i]+A_112*mRate2[i]+A_113*mRate3[i]+A_114*mRate4[i]+
	        A_115*mRate5[i]+A_116*mRate6[i]+A_117*mRate7[i]+A_118*mRate8[i]+A_119*mRate9[i]+A_1110*mRate10[i]+A_1111*mRate11[i]);
    mWrapper.evaluateRate(state, mRate12);
	  for (int i=0; i<mTimeIndex; i++) state[i] = mInitialState[i]+step*(B8_1*mInitialRate[i]+B8_2*mRate2[i]+B8_3*mRate3[i]+B8_4*mRate4[i]+
	        B8_5*mRate5[i]+B8_6*mRate6[i]+B8_7*mRate7[i]+B8_8*mRate8[i]+B8_9*mRate9[i]+B8_10*mRate10[i]+B8_11*mRate11[i]+B8_12*mRate12[i]);
    state[mTimeIndex] = mInitialTime + step*mInitialRate[mTimeIndex];

    return state;
	}
  
	@Override
	protected IntervalData computeFinalRateAndCreateIntervalData() {
	  mWrapper.evaluateRate(mFinalState, mFinalRate);
	  // calculation of interpolation coefficients 
	  double deltaTime = mFinalTime - mInitialTime;
	  for (int i=0; i<mDimension; i++)
	    mAuxState[i] = mInitialState[i]+deltaTime*(AD13_1*mInitialRate[i]+AD13_2*mRate2[i]+AD13_3*mRate3[i]+AD13_4*mRate4[i]+
	        AD13_5*mRate5[i]+AD13_6*mRate6[i]+AD13_7*mRate7[i]+AD13_8*mRate8[i]+
	        AD13_9*mRate9[i]+AD13_10*mRate10[i]+AD13_11*mRate11[i]+AD13_12*mRate12[i]+
	        AD13_13*mFinalRate[i]);
	  mWrapper.evaluateRate(mAuxState, mRate14);
	  for (int i=0; i<mDimension; i++)
	    mAuxState[i] = mInitialState[i]+deltaTime*(AD14_1*mInitialRate[i]+AD14_2*mRate2[i]+AD14_3*mRate3[i]+AD14_4*mRate4[i]+
	        AD14_5*mRate5[i]+AD14_6*mRate6[i]+AD14_7*mRate7[i]+AD14_8*mRate8[i]+
	        AD14_9*mRate9[i]+AD14_10*mRate10[i]+AD14_11*mRate11[i]+AD14_12*mRate12[i]+
	        AD14_13*mFinalRate[i]+AD14_14*mRate14[i]);
	  mWrapper.evaluateRate(mAuxState, mRate15);
	  for (int i=0; i<mDimension; i++)
	    mAuxState[i] = mInitialState[i]+deltaTime*(AD15_1*mInitialRate[i]+AD15_2*mRate2[i]+AD15_3*mRate3[i]+AD15_4*mRate4[i]+
	        AD15_5*mRate5[i]+AD15_6*mRate6[i]+AD15_7*mRate7[i]+AD15_8*mRate8[i]+
	        AD15_9*mRate9[i]+AD15_10*mRate10[i]+AD15_11*mRate11[i]+AD15_12*mRate12[i]+
	        AD15_13*mFinalRate[i]+AD15_14*mRate14[i]+AD15_15*mRate15[i]);
	  mWrapper.evaluateRate(mAuxState, mRate16);
	  for (int i=0; i<mDimension; i++) {
	    mCoeffs[0][i] = mInitialState[i]; // i'am not sure -> Y[i]
	    mCoeffs[1][i] = mFinalState[i] - mInitialState[i];
	    mCoeffs[2][i] = deltaTime*mInitialRate[i] - mCoeffs[1][i];
	    mCoeffs[3][i] = mCoeffs[1][i] - deltaTime*mFinalRate[i] - mCoeffs[2][i];
	    mCoeffs[4][i] = deltaTime*(D4_1*mInitialRate[i]+D4_2*mRate2[i]+D4_3*mRate3[i]+D4_4*mRate4[i]+
	        D4_5*mRate5[i]+D4_6*mRate6[i]+D4_7*mRate7[i]+D4_8*mRate8[i]+
	        D4_9*mRate9[i]+D4_10*mRate10[i]+D4_11*mRate11[i]+D4_12*mRate12[i]+
	        D4_13*mFinalRate[i]+D4_14*mRate14[i]+D4_15*mRate15[i]+D4_16*mRate16[i]);
	    mCoeffs[5][i] = deltaTime*(D5_1*mInitialRate[i]+D5_2*mRate2[i]+D5_3*mRate3[i]+D5_4*mRate4[i]+
	        D5_5*mRate5[i]+D5_6*mRate6[i]+D5_7*mRate7[i]+D5_8*mRate8[i]+
	        D5_9*mRate9[i]+D5_10*mRate10[i]+D5_11*mRate11[i]+D5_12*mRate12[i]+
	        D5_13*mFinalRate[i]+D5_14*mRate14[i]+D5_15*mRate15[i]+D5_16*mRate16[i]);
	    mCoeffs[6][i] = deltaTime*(D6_1*mInitialRate[i]+D6_2*mRate2[i]+D6_3*mRate3[i]+D6_4*mRate4[i]+
	        D6_5*mRate5[i]+D6_6*mRate6[i]+D6_7*mRate7[i]+D6_8*mRate8[i]+
	        D6_9*mRate9[i]+D6_10*mRate10[i]+D6_11*mRate11[i]+D6_12*mRate12[i]+
	        D6_13*mFinalRate[i]+D6_14*mRate14[i]+D6_15*mRate15[i]+D6_16*mRate16[i]);
	    mCoeffs[7][i] = deltaTime*(D7_1*mInitialRate[i]+D7_2*mRate2[i]+D7_3*mRate3[i]+D7_4*mRate4[i]+
	        D7_5*mRate5[i]+D7_6*mRate6[i]+D7_7*mRate7[i]+D7_8*mRate8[i]+
	        D7_9*mRate9[i]+D7_10*mRate10[i]+D7_11*mRate11[i]+D7_12*mRate12[i]+
	        D7_13*mFinalRate[i]+D7_14*mRate14[i]+D7_15*mRate15[i]+D7_16*mRate16[i]);
	  }
	  return new Dopri853IntervalData(mInitialState, mFinalState, mCoeffs);
	}
	 
  @Override
  protected double getMethodOrder() { return 8; }
  
	@Override
	protected double computeApproximation(double step) {
	  computeIntermediateStep(step, mFinalState);
	  double error3 = 0, error5 = 0;
	  for(int i = 0; i < mDimension; i++) {
	    double sk = mAbsTol[i] + mRelTol[i] * Math.max(Math.abs(mFinalState[i]), Math.abs(mInitialState[i]));
	    double errorI3 = (E3_1*mInitialRate[i]+E3_2*mRate2[i]+E3_3*mRate3[i]+E3_4*mRate4[i]+
	        E3_5*mRate5[i]+E3_6*mRate6[i]+E3_7*mRate7[i]+E3_8*mRate8[i]+
	        E3_9*mRate9[i]+E3_10*mRate10[i]+E3_11*mRate11[i]+E3_12*mRate12[i])/sk;
	    error3 += errorI3*errorI3;
	    double errorI5 = (E5_1*mInitialRate[i]+E5_2*mRate2[i]+E5_3*mRate5[i]+E5_4*mRate4[i]+
	        E5_5*mRate5[i]+E5_6*mRate6[i]+E5_7*mRate7[i]+E5_8*mRate8[i]+
	        E5_9*mRate9[i]+E5_10*mRate10[i]+E5_11*mRate11[i]+E5_12*mRate12[i])/sk;
	    error5 += errorI5*errorI5;
	  }
	  double den = error5 + 0.01*error3;
	  if (den<=0.0) den = 1.0;
	  return Math.abs(step)*error5*Math.sqrt(1.0/(mDimension*den));
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
