package org.colos.roboticsLabs.robots.utils.trajectories;
/**
 * @author Almudena Ruiz
 */
import org.colos.roboticsLabs.robots.utils.maths.*;

public class Trapezoid2Trajectory extends Trajectory {

	private double[] mVqMax, mVsync, mAccelMax, mAccel, tau, delta1, delta2;
	protected double[] mQInt;
	protected double mtInt;

	public Trapezoid2Trajectory(double[] qi, double[] qInt, double[] qf,
			double tInt, double duration, double[] vqMax) {
		super(qi, qf, duration);
		mtInt = tInt;
		mQInt = new double[mDOF];
		System.arraycopy(qInt, 0, mQInt, 0, mDOF);
		mVqMax = mAccelMax = mAccel = tau = delta1 = delta2 = new double[mDOF];
		System.arraycopy(vqMax, 0, mVqMax, 0, mDOF);
		mVsync = synchronization(mQi, mQf);

		for (int i = 0; i < mDOF; i++) {
			delta1[i] = mQInt[i] - mQi[i];
			delta2[i] = mQf[i] - mQInt[i];
			tau[i] = mVsync[i] / mAccelMax[i];
			mAccel[i] = (mtInt * delta2[i] - (mDuration - mtInt) * delta1[i])
					/ (2 * mtInt * (mDuration - mtInt) * tau[i]);
		}

	}
	
	/**
   * Gets the intermediate position of the trajectory
   * @return the intermediate position
   */

  final public double [] getQInt() {return mQInt;}
  
  /**
   * Gets the intermediate time of the trajectory
   * @return the intermediate time
   */
  
  final public double getIntermediateTime() {return mtInt;}
	
	/**
	 * Calculates a synchronization velocity for the joints
	 * @param qi the initial point of the trajectory
	 * @param qf the final point of the trajectory
	 * @return the new values of the synchronized velocity
	 */

	private double[] synchronization(double qi[], double qf[]) {
		double[] vSync = new double[mVqMax.length];
		double time[] = new double[mVqMax.length];
		double tmax = 0.0D;
		for (int i = 0; i < mVqMax.length; i++)
			time[i] = Math.abs(qf[i] - qi[i]) / mVqMax[i];

		tmax = Maths.maxValue(time);
		if (tmax <= 0.0001D) {
			for (int j = 0; j < mVqMax.length; j++)
				vSync[j] = 0.0D;

		} else {
			vSync = Maths.multiplyByScalar(1.0D / tmax, Maths.restV(qf, qi));
		}
		return vSync;
	}

	public double[] getPoint(double t) {
		double[] q = new double[mDOF];
		if (t <= 0)
			q = mQi;
		else {
			for (int i = 0; i < mDOF; i++) {
				if (t <= (mtInt - tau[i]))
					q[i] = mQi[i] + delta1[i] * t / mtInt;
				else {
					if (t <= (mtInt + tau[i]))
						q[i] = mQInt[i] + (delta1[i] * (t - mtInt)) / mtInt
								+ mAccel[i] * (t - mtInt + tau[i])
								* (t - mtInt + tau[i]) / 2.;

					else {
						if (t < mDuration)
							q[i] = mQInt[i] + delta2[i] * (t - mtInt)
									/ (mDuration - mtInt);

						else
							q[i] = mQf[i];
					}
				}
			}
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
		for (int i = 0; i < nPoints; i++) {
			t[i] = i * deltaT;
		}
		// double l1 = mtInt1 - mti;
		// int n1 = (int) Math.round(1 + l1 / deltaT);
		// double deltaT1 = l1 / (n1 - 1);
		// double l2 = mtInt2 - mtInt1;
		// int n2 = (int) Math.round(l2 / deltaT);
		// double deltaT2 = l2 / n2;
		// double l3 = mtf - mtInt2;
		// int n3 = nPoints - (n1 + n2);
		// //int n3 = (int) Math.round(l3 / deltaT);
		// double deltaT3 = l3 / n3;
		//
		// for (int i = 0; i < n1; i++) {
		// t[i] = mti + (i * deltaT1);
		// }
		// for (int i = 0; i < n2; i++) {
		// t[n1 + i] = mtInt1 + (i + 1) * (deltaT2);
		// }
		// for (int i = 0; i < n3; i++) {
		// t[n1 + n2 + i] = mtInt2 + (i + 1) * (deltaT3);
		// }
		// return getPoints(t);
		return getPoints(t);

	}

}
