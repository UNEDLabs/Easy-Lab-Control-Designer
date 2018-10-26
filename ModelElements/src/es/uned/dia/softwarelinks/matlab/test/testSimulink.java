package es.uned.dia.softwarelinks.matlab.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import es.uned.dia.softwarelinks.matlab.client.RemoteMatlabConnectorClient;
import es.uned.dia.softwarelinks.matlab.client.RemoteSimulinkConnectorV1;
import es.uned.dia.softwarelinks.matlab.common.MatlabConnector;
import es.uned.dia.softwarelinks.matlab.common.SimulinkConnector;
import es.uned.dia.softwarelinks.transport.TcpTransport;
import es.uned.dia.softwarelinks.transport.Transport;

public class testSimulink {
	public double time = 0;
	public double position = 10;
	public double velocity = 0;

//	@Test
	public void testSimulinkConnector() {
		SimulinkConnector simulink = new SimulinkConnector();
		simulink.setModel("./bounce.mdl");
		simulink.setContext(this);
		simulink.linkVariables("position", "bounce/Position", "out", "1");
	    simulink.linkVariables("velocity", "bounce/Velocity", "out", "1");
	    simulink.linkVariables("time", "bounce", "param", "time");
	    simulink.connect();
	
		boolean firstReset = true;
	    do {
	    	simulink.step(1);
	    	//reset at time=10
	    	if (time>=10 && firstReset){
	    		position = 10;
	    		velocity = 0;
//	    		simulink.synchronize();
	    		firstReset = false;
	    	}
	    	String message = "time: %f, position: %f, velocity: %f";
	    	System.out.println(String.format(message, time, position, velocity));
	    } while (time < 10);
		
		simulink.disconnect();		
	}	

	@Test
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
			rmcc.set("b", true);
			
			String xx = rmcc.getString("x");
			int yy = rmcc.getInt("y");
			double tt = rmcc.getDouble("t");
			double[] uu = rmcc.getDoubleArray("u");
			boolean a = rmcc.getBoolean("b");
			
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

//	@Test
	public void testRemoteSimulinkConnectorClient() {		
		RemoteMatlabConnectorClient rmcc = null;
		try {
			Transport transport = new TcpTransport("tcp://localhost:2055");
			rmcc = new RemoteMatlabConnectorClient(transport);
		} catch (Exception e) {
			fail("Cannot connect to Matlab Server.");
			e.printStackTrace();
		}
		RemoteSimulinkConnectorV1 simulink = new RemoteSimulinkConnectorV1(rmcc);
		simulink.setModel("./bounce.mdl");
		simulink.setContext(this);
		simulink.linkVariables("position", "bounce/Position", "out", "1");
	    simulink.linkVariables("velocity", "bounce/Velocity", "out", "1");
	    simulink.linkVariables("time", "bounce", "param", "time");
	    simulink.connect();

	    boolean firstReset = true;
	    do {
	    	simulink.step(1);
	    	//reset at time=10
	    	if (time>=10 && firstReset){
	    		position = 10;
	    		velocity = 0;
//	    		simulink.synchronize();
	    		firstReset = false;
	    	}
	    	String message = "time: %f, position: %f, velocity: %f";
	    	System.out.println(String.format(message, time, position, velocity));
	    } while (time < 10);
		
		simulink.disconnect();		
	}	

//	@Test
	public void testMatlabConnector() {
		MatlabConnector matlab = new MatlabConnector();
		matlab.connect();
		matlab.set("b", true);
		Object b = ((boolean[])matlab.get("b"))[0];
		System.out.println(b.getClass().getCanonicalName());

		matlab.disconnect();	
	}	

}