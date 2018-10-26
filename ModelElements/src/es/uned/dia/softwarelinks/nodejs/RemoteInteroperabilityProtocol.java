/**
 * LowLevelProtocol
 * author: Jesús Chacón <jcsombria@gmail.com>
 *
 * Copyright (C) 2013 Jesús Chacón
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
package es.uned.dia.softwarelinks.nodejs;

import java.util.List;

/**
* Interface to implement the low-level communication protocol
*/
public interface RemoteInteroperabilityProtocol {
	// Connection methods
	public boolean connect();
	public boolean disconnect();
	
	public RIPServerInfo info();
	
	// Execution control methods
	public boolean open(String element);
	public boolean run();
	public boolean stop();
	public boolean close();
	public boolean sync();

	public Object get(String name);
	public void set(String name, Object value);
	public Object get(String[] name);
  public void set(String[] name, Object[] value);
  
}
