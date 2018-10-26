package org.colos.roboticsLabs.robots.utils.trajectories;

/**
 * @author Almudena Ruiz
 */

public class Spline2Trajectory extends Trajectory {

	protected double[] mQInt1, mQInt2;
	protected double mtInt1, mtInt2;
	private double[][] mVqi, mCoef1, mCoef2, mCoef3;

	public Spline2Trajectory(double[] qi, double[] qInt1, double[] qInt2,
			double[] qf, double tInt1, double tInt2, double duration) {
		super(qi, qf, duration);
		mtInt1 = tInt1;
		mtInt2 = tInt2;
		mQInt1 = new double[mDOF];
		System.arraycopy(qInt1, 0, mQInt1, 0, mDOF);
		mQInt2 = new double[mDOF];
		System.arraycopy(qInt2, 0, mQInt2, 0, mDOF);
		mVqi = new double[mDOF][4];
		mCoef1 = new double[mDOF][4];
		mCoef2 = new double[mDOF][4];
		mCoef3 = new double[mDOF][4];
		for (int i = 0; i < mDOF; i++) {
			mVqi[i] = velqi(mQi[i], mQInt1[i], mQInt2[i], mQf[i], mtInt1,
					mtInt2, mDuration);

			mCoef1[i] = coefPolSpline(mQi[i], mQInt1[i], mVqi[i][0],
					mVqi[i][1], 0, mtInt1);

			mCoef2[i] = coefPolSpline(mQInt1[i], mQInt2[i], mVqi[i][1],
					mVqi[i][2], mtInt1, mtInt2);

			mCoef3[i] = coefPolSpline(mQInt2[i], mQf[i], mVqi[i][2],
					mVqi[i][3], mtInt2, mDuration);

		}
	}
	
  /**
	 * Gets the first intermediate position of the trajectory
	 * @return the first intermediate position
	 */

	final public double[] getQInt1() {return mQInt1;}

	/**
	 * Gets the first intermediate time of the trajectory
	 * @return the first intermediate time
	 */

	final public double getIntermediateTime1() {return mtInt1;}

	/**
	 * Gets the second intermediate position of the trajectory
	 * @return the second intermediate position
	 */

	final public double[] getQInt2() {return mQInt2;}

	/**
	 * Gets the second intermediate time of the trajectory
	 * @return the second intermediate time
	 */

	final public double getIntermediateTime2() {return mtInt2;}
	
	/**
	 * Calculates the velocity of the trajectory
	 * @param qi the initial point
	 * @param q1 the first intermediate point
	 * @param q2 the second intermediate point
	 * @param qf the final point
	 * @param t1 the first intermediate time
	 * @param t2 the second intermediate time
	 * @param tf the final time
	 * @return the velocity in these points
	 */
	
	private double[] velqi(double qi, double q1, double q2, double qf,double t1, double t2, double tf) {
		double[] vqi = new double[4];
		vqi[0] = vqi[3] = 0; // vqi = vqf = 0;
		double K = (3 * (t1 * t1 * (q2 - q1) + t2 * t2 * (q1 - qi)))
				/ (t1 * t2);
		double L = (3 * (t2 * t2 * (qf - q2) + tf * tf * (q2 - q1)))
				/ (t2 * tf);
		vqi[2] = (2 * (t1 + t2) * L - K * tf)
				/ (4 * (t1 + t2) * (t2 + tf) - t1 * tf);
		vqi[1] = (K - t1 * vqi[2]) / (2 * (t1 + t2));

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
		double T = tf - ti; // coef T
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
		return coef[0] + coef[1] * (t - ti) + coef[2] * (t - ti) * (t - ti) + coef[3] * (t - ti) * (t - ti) * (t - ti);
	}

	public double[] getPoint(double t) {
		double[] q = new double[mDOF];
		if (t <= 0)
			q = mQi;
		else {
			if (t <= mtInt1) {
				// Polinomio en (qi, qInt1]
				for (int i = 0; i < mDOF; i++) {
					q[i] = evalPol(mCoef1[i], 0, t);
				}
			} else if (t <= mtInt2) {
				// Polinomio en (qInt1, qInt2]
				for (int i = 0; i < mDOF; i++) {
					q[i] = evalPol(mCoef2[i], mtInt1, t);
				}
			} else

			if (t < mDuration) {
				// Polinomio en (qInt2, qf)
				for (int i = 0; i < mDOF; i++) {
					q[i] = evalPol(mCoef3[i], mtInt2, t);
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
		double l1 = mtInt1;
		int n1 = (int) Math.round(1 + l1 / deltaT);
		double deltaT1 = l1 / (n1 - 1);
		double l2 = mtInt2 - mtInt1;
		int n2 = (int) Math.round(l2 / deltaT);
		double deltaT2 = l2 / n2;
		double l3 = mDuration - mtInt2;
		int n3 = nPoints - (n1 + n2);
		// int n3 = (int) Math.round(l3 / deltaT);
		double deltaT3 = l3 / n3;

		for (int i = 0; i < n1; i++) {
			t[i] = i * deltaT1;
			System.out.println("t" + (i + 1) + " = " + t[i]);
		}
		for (int i = 0; i < n2; i++) {
			t[n1 + i] = mtInt1 + (i + 1) * (deltaT2);
			System.out.println("t" + (n1 + i + 1) + " = " + t[n1 + i]);
		}
		for (int i = 0; i < n3; i++) {
			t[n1 + n2 + i] = mtInt2 + (i + 1) * (deltaT3);
			System.out
					.println("t" + (n1 + n2 + i + 1) + " = " + t[n1 + n2 + i]);
		}
		return getPoints(t);
	}
}
