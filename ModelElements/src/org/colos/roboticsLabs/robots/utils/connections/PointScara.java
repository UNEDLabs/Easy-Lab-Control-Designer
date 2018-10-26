package org.colos.roboticsLabs.robots.utils.connections;

/**
 * @author Almudena Ruiz
 */
import java.util.StringTokenizer;

public class PointScara {

	public String name;
	public double X, Y, Z, R, A, B;
	
	public PointScara(String name, double[] coord) {
		this.name = name;
		this.X = coord[0];
		this.Y = coord[1];
		this.Z = coord[2];
		this.R = coord[3];
		this.A = 0;
		this.B = 0;
	}
	
	public PointScara(int pointNumber, int[] coord) {
		this.name = "P" + pointNumber;
		this.X = coord[0];
		this.Y = coord[1];
		this.Z = coord[2];
		this.R = coord[3];
		this.A = 0;
		this.B = 0;
	}

	public PointScara(int pointNumber, double[] coord) {
		this.name = "P" + pointNumber;
		this.X = coord[0];
		this.Y = coord[1];
		this.Z = coord[2];
		this.R = coord[3];
		this.A = 0;
		this.B = 0;
	}

	/**
	 * Defines a Point Scara
	 * @param line
	 * @return
	 */
	public static PointScara createPoint(String line) {
		PointScara point = null;
		StringTokenizer st = new StringTokenizer(line, "= ");
		String name = st.nextToken();
		//System.out.println(name);
		double[] coord = new double[4];
		for (int i = 0; i < coord.length; i++) {
			coord[i] = Integer.parseInt(st.nextToken());
			//System.out.println(coord[i]);
		}
		point = new PointScara(name, coord);
		return point;
	}

}
