package demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class OneBall_WSServer extends WebSocketServer {
	private static float ballX;

	public OneBall_WSServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		
		ballX = 0;
	}

	public OneBall_WSServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " client for the model!" );
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {		
		System.out.println( conn + " has left the model!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		System.out.println( conn + ": on " + message );
		// parse message and maybe send a response
		String[] temp = message.split(" ");
		ballX = Float.parseFloat(temp[1]);
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocket.DEBUG = false;
		int port = 8887; 
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		OneBall_WSServer s = new OneBall_WSServer( port );
		s.start();
		System.out.println( "ModelServer started on port: " + s.getPort() );

		double dx = 0.01;
		while ( true ) {
		  ballX += dx;
		  if (ballX>1 || ballX<-1) dx = -dx;
			Set<WebSocket> con = s.connections();
			synchronized ( con ) {
				for( WebSocket c : con ) {					
					System.out.println( c + ": out " + "ballX " + ballX);
					c.send("ballX " + ballX);
				}
			}
			try{ Thread.sleep(10); } catch(Exception e){};
		}
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
	}
}
