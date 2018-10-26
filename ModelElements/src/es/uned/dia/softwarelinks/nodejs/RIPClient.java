/**
 * RIPClient
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

import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import es.uned.dia.ejss.softwarelinks.utils.RIPConfigurationModel;
import es.uned.dia.softwarelinks.rpc.JsonRpcClient;
import es.uned.dia.softwarelinks.rpc.RpcClient;
import es.uned.dia.softwarelinks.rpc.param.RpcParam;
import es.uned.dia.softwarelinks.rpc.param.RpcParamFactory;
import es.uned.dia.softwarelinks.transport.HttpTransport;
import es.uned.dia.softwarelinks.transport.Transport;

/**
 * Class to interact with a JIL Server
 * @author Jesús Chacón <jcsombria@gmail.com> 
 */
public class RIPClient implements RemoteInteroperabilityProtocol {
  //The names of the methods
  private static final String CONNECT = "connect";
  private static final String INFO = "info";
  private static final String SET = "set";
  private static final String GET = "get";
  private static final String OPEN = "open"; 
  private static final String RUN = "run"; 
  private static final String STOP = "stop"; 
  private static final String CLOSE = "close"; 
  private static final String DISCONNECT = "disconnect";

  private Transport transport;
  private RpcClient client;	
  private RIPExperienceInfo metadata;
  private RIPServerInfo explist;

  public class RIPException extends Exception {
    public RIPException(Throwable e) {
      super(e);
    }
  }

  /**
   * Create a new RIPClient 
   * @param url The url of the server
   * @throws Exception 
   */
  public RIPClient(Transport transport) throws RIPException {
    try {
      this.transport = transport;
      client = new JsonRpcClient(transport);
    } catch (Exception e) {
      throw new RIPException(e);
    }
  }
	
  /**
   * Create a new RIPClient
   * @param url The url of the server
   * @throws Exception 
   */
  public RIPClient(String serverURL) throws RIPException {
    try {
      this.transport = new HttpTransport(serverURL);
      client = new JsonRpcClient(transport);
    } catch (Exception e) {
      throw new RIPException(e);
    }
  }

  @Override
  public boolean connect() {
    RpcParam<?>[] response = (RpcParam[])client.invoke(CONNECT, null);
    if(response == null) {
      return false;
    }
    // TO DO: do something with server response
    return true;
  }

  @Override
  public boolean disconnect() {
    RpcParam<?>[] response = (RpcParam[])client.invoke(DISCONNECT, null);
    if(response == null) {
      return false;
    }
    return true;
  }

  @Override
  public Object get(String[] name) {
    RpcParam<?>[] args = new RpcParam[] {
        RpcParamFactory.create("name", name)
    };
    RpcParam<?>[] result = (RpcParam[])client.invoke(GET, args);
    return result[0].get();
  }
    
  @Override
  public void set(String[] name, Object[] value) {
    RpcParam<?>[] args = new RpcParam[] {
        RpcParamFactory.create("name", name),
        RpcParamFactory.create("value", value),
    };
    client.notify(SET, args);
  }

  @Override
  public Object get(String name) {
    return get(new String[]{name});
  }

  @Override
  public void set(String name, Object value) {
    set(new String[]{name}, new Object[]{value});
  }

  @Override
  public RIPServerInfo info() {
    if(metadata == null) {
      metadata = new RIPExperienceInfo();
      RpcParam[] response = (RpcParam[])client.invoke(INFO, null);
      if(response != null) {
        RpcParam[] list = (RpcParam[])response[0].get();
        String[] experiences = new String[list.length];
        for(int i=0; i<list.length; i++) {
          experiences[i] = list[i].get().toString();
        }        
        explist = new RIPServerInfo(experiences);
      }
    }
    return this.explist;
  }

  public RIPExperienceInfo info(String expid) {
    if(metadata == null) {
      metadata = new RIPExperienceInfo();
      RpcParam<?>[] params = new RpcParam[] {
          RpcParamFactory.create("expId", expid),
      };
      RpcParam<String>[] response = (RpcParam<String>[])client.invoke(INFO, params);
      if(response != null) {
        String result = response[0].get().toString();
        metadata.parse(result);
      }
    }
    return metadata;
  }

//  public static void main(String[] args) {
//    try {
//      String url = "http://62.204.199.224/SARLABV8.0/proxy?key=123&url=http://10.192.38.71:8080/RIP";
////      String url = "http://localhost:8080/RIP/POST/";
//      HttpTransport transport = new HttpTransport(url);
//      String info = transport.get("").toString();
////      System.out.println(info);
//      RIPServerInfo explist = new RIPServerInfo(info);
//      System.out.println(explist.getMethods().toString());
//
//      info = transport.get("?expId=TestOK").toString();
//      RIPExperienceInfo expInfo = new RIPExperienceInfo(info);
////      System.out.println(info);
//      System.out.println(expInfo.toString());
//      RIPConfigurationModel configuration = new RIPConfigurationModel();
//      configuration.setServer("http://62.204.199.224/SARLABV8.0/proxy?key=123&url=http://10.192.38.71:8080/RIP");
//      System.out.println(configuration.getServer());
//    } catch (MalformedURLException | RIPException e) {
//      System.out.println(e.getMessage());
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//    }
//    
//  }
//  
  @Override
  public boolean open(String element) {
    RpcParam<?>[] params = new RpcParam[] {
        RpcParamFactory.create("model", element),
    };
    RpcParam[] result = (RpcParam[])client.invoke(OPEN, params);
    return (boolean)result[0].get();
  }

  @Override
  public boolean run() {
    RpcParam[] result = (RpcParam[])client.invoke(RUN, null);
    return (boolean)result[0].get();
  }

  @Override
  public boolean stop() {
    RpcParam[] result = (RpcParam[])client.invoke(STOP, null);
    return (boolean)result[0].get();
  }

  @Override
  public boolean close() {
    RpcParam[] result = (RpcParam[])client.invoke(CLOSE, null);
    return (boolean)result[0].get();
  }

  @Override
  public boolean sync() {
    // TODO Auto-generated method stub
    return false;
  }
}