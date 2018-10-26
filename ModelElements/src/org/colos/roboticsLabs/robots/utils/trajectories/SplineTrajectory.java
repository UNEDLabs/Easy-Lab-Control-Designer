package org.colos.roboticsLabs.robots.utils.trajectories;

/**
 * @author Almudena Ruiz
 */

public class SplineTrajectory extends Trajectory {

	protected double[] mQInt;
	protected double mtInt;
	private double[][] mVqi, mCoef1, mCoef2;

	public SplineTrajectory(double[] qi, double[] qInt, double[] qf, double tInt, double duration) {
		super(qi, qf, duration);
		mtInt = tInt;
		mQInt = new double[mDOF];
		System.arraycopy(qInt, 0, mQInt, 0, mDOF);
		mVqi = new double[mDOF][3];
		mCoef1 = mCoef2 = new double[mDOF][4];
		for (int i = 0; i < mDOF; i++) {
			mVqi[i] = velqi(mQi[i], mQInt[i], mQf[i], mtInt, mDuration);
			mCoef1[i] = coefPolSpline(mQi[i], mQInt[i], mVqi[i][0], mVqi[i][1], 0, mtInt);
			mCoef2[i] = coefPolSpline(mQInt[i], mQf[i], mVqi[i][1], mVqi[i][2], mtInt, mDuration);
		}
	}
	
	/**
   * Gets the intermediate position of the trajectory 
   * @return the intermediate position
   */

  final public double[] getQInt() {return mQInt;}

  /**
   * Gets the intermediate time of the trajectory
   * @return the intermediate time
   */

  final public double getIntermediateTime() {return mtInt; }
  
  /**
   * Calculates the velocity of the trajectory
   * @param qi the initial point
   * @param qInt the intermediate point
   * @param qf the final point
   * @param tInt the intermediate time
   * @param tf the final time
   * @return the velocity in these points
   */
  private double[] velqi(double qi, double qInt, double qf, double tInt, double tf) {
    double[] vqi = new double[3];
    vqi[0] = vqi[2] = 0; // vqi = vqf = 0;
    vqi[1] = (3 * (tInt * tInt * (qf - qInt) + tf * tf * (qInt - qi)))
        / (2 * (tInt + tf) * tInt * tf);
    return vqi;
  }
  
  /**
   * Calculates the coefficient of the Spline polynomial 
   * @param qi the initial point
   * @param qf the final point
   * @param vqi the initial point velocity 
   * @param vqf the final point velocity
   * @param ti the initial time
   * @param tf the final time
   * @return the Spline polynomial coefficients 
   */
  private double[] coefPolSpline(double qi, double qf, double vqi, double vqf, double ti, double tf) {
    double[] coef = new double[4]; // coef a, b, c, d,
    double T = tf - ti;
    coef[0] = qi; // coef a
    coef[1] = vqi; // coef b
    coef[2] = ((3 * (qf - qi)) / (T * T)) - (2 * vqi + vqf) / (T * T); // coef c
    coef[3] = ((-2 * (qf - qi)) / (T * T * T)) + ((vqf + vqi) / (T * T)); // coef d
    return coef;
  }

  /**
   * Calculates the value of a given coefficient
   * @param coef the coefficient
   * @param ti the initial time
   * @param t the time
   * @return the coefficient value at given times
   */

  private double evalPol(double[] coef, double ti, double t) {
    return coef[0] + coef[1] * (t - ti) + coef[2] * (t - ti)* (t - ti) + coef[3] * (t - ti) * (t - ti) * (t - ti);
  }


  public double[] getPoint(double t) {
    double[] q = new double[mDOF];
    if (t <= 0)
      q = mQi;
    else {
      if (t < mtInt) {
        // Polinomio en (qi, qInt1)
        for (int i = 0; i < mDOF; i++) {
          q[i] = evalPol(mCoef1[i], 0, t);
        }
      } else if (t < mDuration) {
        // Polinomio en [qInt1, qf)
        for (int i = 0; i < mDOF; i++) {
          q[i] = evalPol(mCoef2[i], mtInt, t);
        }
      } else
        q = mQf;
    }
    return q;
  }

	
	public double[][] getPoints(double[] t) {
		double[][] points = new double[t.length][mDOF];
		for (int i = 0; i < t.length; i++) {
			points[i] = getPoint(t[i]);
		}
		return points;
	}

	public double[][] getPoints(int nPoints) {

		double[] t = new double[nPoints];
		double deltaT = mDuration / (nPoints - 1);
		double l1 = mtInt;
		int n1 = (int) (Math.round(1 + l1 / deltaT));
		double deltaT1 = l1 / (n1 - 1);
		double l2 = mDuration - mtInt;
		int n2 = nPoints - n1;
		// int n2 = (int)Math.round(l2/deltaT);
		double deltaT2 = l2 / n2;
		// nPoints = n1 + n2;
		System.out.println("n1 = " + n1 + "  n2 = " + n2);
		for (int i = 0; i < n1; i++) {
			t[i] = i * deltaT1;
			System.out.println("t" + (i + 1) + " = " + t[i]);
		}
		for (int i = 0; i < n2; i++) {
			t[n1 + i] = mtInt + (i + 1) * (deltaT2);
			System.out.println("t" + (n1 + i + 1) + " = " + t[n1 + i]);
		}
		return getPoints(t);
	}
}
