/**
 * XmlRpcTcpStreamTransport
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
package es.uned.dia.softwarelinks.labview.protocol.xmlrpc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcStreamTransport;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestProcessor;
import org.xml.sax.SAXException;

public class XmlRpcTcpStreamTransport extends XmlRpcStreamTransport {
//	private LocalStreamConnection conn;
    private XmlRpcRequest request; 
    private Socket jilTCP;
    private DataInputStream bufferInputTCP;
    private DataOutputStream bufferOutputTCP;
	
	/** Creates a new instance.
	 * @param pClient The client, which is controlling the transport.
	 * @param pServer An instance of {@link XmlRpcStreamRequestProcessor}.
	 */
	public XmlRpcTcpStreamTransport(XmlRpcClient pClient) {
		super(pClient);	
		
        try {
            jilTCP = new java.net.Socket();
            jilTCP.connect(new InetSocketAddress("127.0.0.1", 2055), 5000);
            bufferInputTCP = new DataInputStream(new BufferedInputStream(jilTCP.getInputStream()));
            bufferOutputTCP = new DataOutputStream(new BufferedOutputStream(jilTCP.getOutputStream()));
            jilTCP.setSoTimeout(20000);
            jilTCP.setTcpNoDelay(true);
          }catch (IOException ioe) {
            System.err.println("connect() method message: IOException = " + ioe.getMessage());
          }catch (Exception e) {
            System.err.println("connect() method message: Exception = " + e.getMessage());
          } 
        System.out.println("XmlRpcTcpStreamTransport created...");
	}
	
	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
		return pConfig.isGzipRequesting();
	}

	protected void close() throws XmlRpcClientException {
	}

	protected InputStream getInputStream() throws XmlRpcException {
		return bufferInputTCP;
	}

	protected ReqWriter newReqWriter(XmlRpcRequest pRequest)
            throws XmlRpcException, IOException, SAXException {
	    request = pRequest;
        return super.newReqWriter(pRequest);
    }

    protected void writeRequest(ReqWriter pWriter)
            throws XmlRpcException, IOException, SAXException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		pWriter.write(baos); 
		bufferOutputTCP.writeInt(baos.toByteArray().length);
		bufferOutputTCP.write(baos.toByteArray());
		bufferOutputTCP.flush();
	}
    
    @Override
    protected Object readResponse(XmlRpcStreamRequestConfig pConfig, InputStream pStream) throws XmlRpcException {
    	return super.readResponse(pConfig, pStream);
    }
}
