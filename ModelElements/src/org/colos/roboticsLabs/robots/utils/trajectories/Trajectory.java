package org.colos.roboticsLabs.robots.utils.trajectories;

/**
 * @author Almudena Ruiz
 */

public abstract class Trajectory {

	protected double[] mQi, mQf;
	protected double mDuration;
	protected int mDOF;

	protected Trajectory(final double[] qi, final double[] qf, double duration) {
		if (qi.length == qf.length) {
			mDOF = qi.length;
			mDuration = duration;
			mQi = new double[mDOF];
			System.arraycopy(qi, 0, mQi, 0, mDOF);
			mQf = new double[mDOF];
			System.arraycopy(qf, 0, mQf, 0, mDOF);
		} else
			System.out.println("Data error");
	}

	/**
	 * Gets the initial position of the trajectory
	 * 
	 * @return the initial position
	 */

	final public double[] getQi() {
		return mQi;
	}

	/**
	 * Gets the final position of the trajectory
	 * 
	 * @return the final position
	 */

	final public double[] getQf() {
		return mQf;
	}

	/**
	 * Gets the duration of the trajectory
	 * 
	 * @return the duration
	 */

	final public double getDuration() {
		return mDuration;
	}
	
	/**
	 * Gets the point at time "t"
	 * @param t the time 
	 * @return the point at given time  
	 */

	abstract public double[] getPoint(double t);
	
	/**
	 * Gets the points at the given times
	 * @param t the given times
	 * @return the points at the given times
	 */

	abstract public double[][] getPoints(double[] t);
	
	/**
	 * Gets a number of points of the trajectory
	 * @param nPoints the number of points
	 * @return the points calculated of the trajectory 
	 */

	abstract public double[][] getPoints(int nPoints);

}
