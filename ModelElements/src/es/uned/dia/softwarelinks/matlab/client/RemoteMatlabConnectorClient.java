package es.uned.dia.softwarelinks.matlab.client;

import java.net.MalformedURLException;

import es.uned.dia.softwarelinks.matlab.common.RemoteControlProtocolL1;
import es.uned.dia.softwarelinks.rpc.JsonRpcClient;
import es.uned.dia.softwarelinks.rpc.param.RpcParam;
import es.uned.dia.softwarelinks.rpc.param.RpcParamFactory;
import es.uned.dia.softwarelinks.transport.HttpTransport;
import es.uned.dia.softwarelinks.transport.TcpTransport;
import es.uned.dia.softwarelinks.transport.Transport;

/**
 * Class to implement the RPC communication protocol with MATLAB
 * @author <a href="mailto:gfarias@bec.uned.es">Gonzalo Farias</a>
 * @author <a href="mailto:jchacon@bec.uned.es">Jesús Chacón</a> 
 *
 */
public class RemoteMatlabConnectorClient extends JsonRpcClient implements RemoteControlProtocolL1 {

	public RemoteMatlabConnectorClient(Transport transport) throws Exception {
		super(transport);
	}

	public RemoteMatlabConnectorClient(String url) throws MalformedURLException, Exception {
		super(new HttpTransport(url));
	}

	synchronized public String getMetadata() {
		RpcParam<?>[] response = (RpcParam[])invoke("getMetadata", null);
		RpcParam<String> result = (RpcParam<String>)response[0];
		return result.get();
	}
	
	@Override
	public boolean connect() {
		RpcParam<?>[] response = (RpcParam[])invoke("connect", null);
		RpcParam<Boolean> result = (RpcParam<Boolean>)response[0];
		boolean isConnected = result.get().booleanValue();
		return isConnected;
	}

	@Override
	public boolean disconnect() {
		Object[] response = (Object[])invoke("disconnect", null);
		RpcParam<Boolean> result = (RpcParam<Boolean>)response[0];
		boolean isConnected = result.get().booleanValue();
		return isConnected;
	}

	@Override
	public Object[] get(String[] name) {
		RpcParam<?>[] args = new RpcParam[] {
			RpcParamFactory.create("name", name)
		};
		RpcParam<?> result = ((RpcParam[])invoke("get", args))[0];
		RpcParam<?>[] resultArray = (RpcParam[])result.get();
		int size = resultArray.length;
		Object[] toReturn = new Object[size];
		for(int i=0; i<size; i++) {
			toReturn[i] = resultArray[i].get();
		}
		return toReturn;
	}

	@Override
	public Object get(String name) {
		RpcParam<?>[] args = new RpcParam[] {
			RpcParamFactory.create("name", name)
		};
		RpcParam<?>[] result = (RpcParam[])invoke("get", args);
		return result[0].get();
	}
	
	@Override
	public void set(String[] name, Object[] value) {
		RpcParam<?>[] args = new RpcParam[] {
			RpcParamFactory.create("name", name),
			RpcParamFactory.create("value", value),
		};
		notify("set", args);
	}

	@Override
	public void set(String name, Object value) {
		set(new String[]{name}, new Object[]{value});	
	}

	@Override
	public boolean eval(String command) {
		RpcParam<?>[] args = new RpcParam[] {
			RpcParamFactory.create("command", command)
		};
		notify("eval", args);
		return true;
	}

	
	public boolean getBoolean(String name) {
		Object value = get(name);
		return (boolean)value;
	}

	public int getInt(String name) {
		Double[] response = (Double[])get(name);
		Double result = response[0];
		return (result != null) ? result.intValue() : 0;
	}

	public double getDouble(String name) {
		Double[] response = (Double[])get(name);
		Double result = response[0];
		return result.doubleValue();
	}

	public String getString(String name) {
		Object a = get(name);
		return (String)get(name);
	}

	public double[] getDoubleArray(String name) {
		Double[] result = (Double[])get(name);
		double[] values = new double[result.length];
		for(int i=0; i<result.length; i++) {
			values[i] = result[i].doubleValue();
		}
		return values;
	}	
}