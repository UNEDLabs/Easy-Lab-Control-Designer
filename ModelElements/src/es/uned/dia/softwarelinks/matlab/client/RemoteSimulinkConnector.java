/**
 * SimulinkConnector
 * author: Jesús Chacón <jcsombria@gmail.com>
 *
 * Copyright (C) 2014 Jesús Chacón
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uned.dia.softwarelinks.matlab.client;

import es.uned.dia.softwarelinks.matlab.common.RemoteControlProtocolL1;
import es.uned.dia.softwarelinks.rpc.param.RpcParam;
import es.uned.dia.softwarelinks.rpc.param.RpcParamFactory;

public class RemoteSimulinkConnector implements RemoteControlProtocolL1 {
	protected String initCommand = null;
	private RemoteMatlabConnectorClient matlab;

	public RemoteSimulinkConnector(RemoteMatlabConnectorClient matlab) {
		this.matlab = matlab;
	}

	/**
	 * Starts the connection with the external application  
	 * @return boolean true if the connection was successful
	 */
	@Override
	public boolean connect() {
		boolean isConnected = matlab.connect();
		return isConnected;
	}

	/**
	 * Finishes the connection with the external application
	 */
	@Override
	public boolean disconnect() {
		return matlab.disconnect();
	}

	/**
	 * Opens the Simulink model 
	 */
	public void open(String model) {
		RpcParam<?>[] params = new RpcParam[] {
			RpcParamFactory.create("model", model),
		};
		matlab.notify("open", params);
	}

	/**
	 * Advance the Simulink model exactly one step.
	 */
	public void step() {
		RpcParam<?>[] params = new RpcParam[] {
			RpcParamFactory.create("step", 1),
		};
		matlab.notify("step", params);
	}

	/**
	 * Steps the Simulink model a number of times.
	 * @param dt double indicates the number of times to step the Simulink model.
	 */
	public void step (double dt) {
		RpcParam<?>[] params = new RpcParam[] {
			RpcParamFactory.create("step", dt),
		};
		matlab.notify("step", params);
	}

	@Override
	public boolean eval(String command) {
		boolean result = (boolean)matlab.eval(command);
		return result;
	}

	@Override
	public void set(String name, Object value) {
		matlab.set(name, value);
	}

	@Override
	public Object get(String name) {
		return matlab.get(name);
	}

	@Override
	public void set(String[] name, Object[] value) {
		matlab.set(name, value);
	}

	@Override
	public Object[] get(String[] name) {
		return matlab.get(name);
	}

	public void get() {}
	public void set() {}
}