/*
 * Copyright (C) 2012 Francisco Esquembre / Andres Mejias / Marco A. Marquez   
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * This software is based on Arduino library for Processing 
 * (Processing code to communicate with the ArduinoRPU Firmata 2 firmware) by David A. Mellis
 */
package es.uhu.hardware.utils;

import java.io.IOException;

/**
 * Interface for a connection to hardware
 * @author Andres Mejias
 * @author Marco A. Marquez
 * @author Francisco Esquembre
 * @version 1.0 December 2012
 */
public interface Connection {

  /**
   * Returns the list of possible connection channels
   * @return
   */
  public String[] listConnections();

  /**
   * Effectively opens the connection
   * @param commId
   * @param commParameter
   * @throws Exception
   */
  public void openConnection(String commId, Object commParameter) throws Exception;

  /**
   * Closes the connection
   * @throws Exception
   */
  public void closeConnection() throws Exception;

  /**
   * Writes an integer to the board
   * @param data
   */
  public void writeInt(int data) throws IOException;

  /**
   * Returns the number of bytes available for reading on the board
   * @return Number of bytes available
   */
  public int available();

  /**
   * Reads an integer from the board
   * @return
   */
  public int readInt();

}
