package org.colos.roboticsLabs.robots.utils.trajectories;
/**
 * @author Almudena Ruiz
 */

import org.colos.roboticsLabs.robots.utils.maths.*;

public class TrapezoidTrajectory extends Trajectory {

	private double[] mVqMax, mAccelMax, mVsync, tau, T, deltaQ;
	private int[] signo;

	public TrapezoidTrajectory(double[] qi, double[] qf, double duration,
		double[] vqMax, double[] accelMax) {
		super(qi, qf, duration);
		mVqMax = new double[mDOF];
		System.arraycopy(vqMax, 0, mVqMax, 0, mDOF);
		mAccelMax = new double[mDOF];
		System.arraycopy(accelMax, 0, mAccelMax, 0, mDOF);
		mVsync = synchronization(mQi, mQf);
		deltaQ = new double[mDOF];
		tau = new double[mDOF];
		T = new double[mDOF];
		signo = new int[mDOF];
		for (int i = 0; i < mDOF; i++) {
			deltaQ[i] = mQf[i] - mQi[i];
			if (deltaQ[i] >= 0) {
				signo[i] = 1;
			} else {
				signo[i] = -1;
			}
			tau[i] = mVsync[i] / mAccelMax[i];
			T[i] = signo[i] * deltaQ[i] / mVsync[i] + tau[i];
		}
		mDuration = Maths.maxValue(T);
	
	}

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
				if (t <= tau[i])
					q[i] = mQi[i] + signo[i] * (mAccelMax[i] * t * t) / 2.;
				else {
					if (t <= (T[i] - tau[i]))
						q[i] = mQi[i] - signo[i] * (mVsync[i] * mVsync[i])
								/ (2 * mAccelMax[i]) + signo[i] * mVsync[i] * t;

					else {
						if (t < T[i])
							q[i] = mQf[i]
									+ signo[i]
									* (-mAccelMax[i] * T[i] * T[i] + 2
											* mAccelMax[i] * T[i] * t - mAccelMax[i]
											* t * t) / 2.;
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
		// double l1 = mtInt1;
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

		return getPoints(t);

	}

}
