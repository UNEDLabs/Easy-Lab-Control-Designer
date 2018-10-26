package es.uned.dia.softwarelinks.matlab.test;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient;
import es.uned.dia.softwarelinks.transport.TcpTransport;
import es.uned.dia.softwarelinks.transport.Transport;

public class Test {

	/**
	 * @param args
	 */
//	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException
//	{
//		Test test = new Test();
//
//		test.testRemoteMatlab();
//	}

	public void testRemoteMatlab() {
		RemoteMatlabConnectorClient rmcc = null;
		try {
			Transport transport = new TcpTransport("tcp://localhost:2055");
			rmcc = new RemoteMatlabConnectorClient(transport);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int time = 10, expected = 10;
		if(rmcc.connect()) {
			try{ Thread.sleep(10000); } catch(InterruptedException e){}
			rmcc.set("t", 10.0);
			rmcc.set("u", new double[]{0.0, 1.0, 2.0});
			rmcc.set("x", "Prueba");
			rmcc.set("y", 1);
			
			String xx = rmcc.getString("x");
			int yy = rmcc.getInt("y");
			double tt = rmcc.getDouble("t");
			double[] uu = rmcc.getDoubleArray("u");
			
			String uu_string = "[";
			for(int i=0; i<uu.length; i++) {
				if(i==0) {
					uu_string += uu[i];
				} else {
					uu_string += "," + uu[i];  
				}
			}
			uu_string += "]";

			System.out.println("t: "+tt);
			System.out.println("u: "+uu_string);
			System.out.println("x: "+xx);
			System.out.println("y: "+yy);
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rmcc.disconnect();
	}

	public double t;
	public double dt;
	public double w;
	public double y;
	// Declare local variables
	public double time=0, position=10, velocity=15;
}
