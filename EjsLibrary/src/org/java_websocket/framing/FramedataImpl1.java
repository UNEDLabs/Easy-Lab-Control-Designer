package org.java_websocket.framing;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.util.Charsetfunctions;

public class FramedataImpl1 implements FrameBuilder {
	protected static byte[] emptyarray = {};
	protected boolean fin;
	protected Opcode optcode;
	private ByteBuffer unmaskedpayload;
	protected boolean transferemasked;

	public FramedataImpl1() {
	}

	public FramedataImpl1( Opcode op ) {
		this.optcode = op;
		unmaskedpayload = ByteBuffer.wrap( emptyarray );
	}

	/**
	 * Helper constructor which helps to create "echo" frames.
	 * The new object will use the same underlying payload data.
	 **/
	public FramedataImpl1( Framedata f ) {
		fin = f.isFin();
		optcode = f.getOpcode();
		unmaskedpayload = f.getPayloadData();
		transferemasked = f.getTransfereMasked();
	}

	public boolean isFin() {
		return fin;
	}

	public Opcode getOpcode() {
		return optcode;
	}

	public boolean getTransfereMasked() {
		return transferemasked;
	}

	public ByteBuffer getPayloadData() {
		return unmaskedpayload;
	}

	public void setFin( boolean fin ) {
		this.fin = fin;
	}

	public void setOptcode( Opcode optcode ) {
		this.optcode = optcode;
	}

	public void setPayload( ByteBuffer payload ) throws InvalidDataException {
		unmaskedpayload = payload;
	}

	public void setTransferemasked( boolean transferemasked ) {
		this.transferemasked = transferemasked;
	}

	public void append( Framedata nextframe ) throws InvalidFrameException {
		ByteBuffer b = nextframe.getPayloadData();
		if( unmaskedpayload == null ) {
			unmaskedpayload = ByteBuffer.allocate( b.remaining() );
			b.mark();
			unmaskedpayload.put( b );
			b.reset();
		} else {
			b.mark();
			unmaskedpayload.position( unmaskedpayload.limit() );
			unmaskedpayload.limit( unmaskedpayload.capacity() );

			if( b.remaining() > unmaskedpayload.remaining() ) {
				ByteBuffer tmp = ByteBuffer.allocate( b.remaining() + unmaskedpayload.capacity() );
				unmaskedpayload.flip();
				tmp.put( unmaskedpayload );
				tmp.put( b );
				unmaskedpayload = tmp;

			} else {
				unmaskedpayload.put( b );
			}
			unmaskedpayload.rewind();
			b.reset();
		}
		fin = nextframe.isFin();
	}

	@Override
	public String toString() {
		return "Framedata{ optcode:" + getOpcode() + ", fin:" + isFin() + ", payloadlength:" + unmaskedpayload.limit() + ", payload:" + Arrays.toString( Charsetfunctions.utf8Bytes( new String( unmaskedpayload.array() ) ) ) + "}";
	}

}
