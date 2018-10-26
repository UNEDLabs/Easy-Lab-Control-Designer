package org.colos.roboticsLabs.robots.utils.trajectories;

/**
 * @author Almudena Ruiz
 */

public class LinearTrajectory extends Trajectory {

	private double[] mDelta;

	public LinearTrajectory(double[] qi, double[] qf, double duration) {
		super(qi, qf, duration);
		mDelta = new double[mDOF];
		for (int i = 0; i < qi.length; i++) {
			mDelta[i] = (mQf[i] - mQi[i]) / mDuration;
		}
	}

	public double[] getPoint(double t) {
		double[] q = new double[mDOF];
		if (t <= 0) System.arraycopy(mQi,0,q,0,mDOF);
		else {
			if (t < mDuration) {
				for (int i = 0; i < mDOF; i++)
					q[i] = mQi[i] + mDelta[i] * t;

			} 
			else System.arraycopy(mQf,0,q,0,mDOF);
		}
		return q;
	}

	public double[][] getPoints(double[] t) {
		double[][] points = new double[t.length][mDOF];
		double[] q;
		for (int i = 0; i < t.length; i++) {
			q = getPoint(t[i]);
			for (int j = 0; j < mDOF; j++) {
				points[i][j] = q[j];
			}
		}
		return points;
	}

	public double[][] getPoints(int nPoints) {
		double[] t = new double[nPoints];
		double deltaT = mDuration / (nPoints - 1);
		for (int i = 0; i < nPoints; i++) {
			t[i] = i * deltaT;
		}
		return getPoints(t);
	}

}
