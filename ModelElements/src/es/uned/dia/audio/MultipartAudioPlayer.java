package es.uned.dia.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import sun.audio.AudioData;
import sun.audio.AudioPlayer;
import sun.audio.ContinuousAudioDataStream;

/**
 * MultipartAudioPlayer allows to play multipart audio streaming from an Axis IP cam. 
 * The audio formats supported are u-law, a-law and AAC.
 * @author Jesús Chacón Sombría <jcsombria@gmail.com>
 *
 */
public class MultipartAudioPlayer {
	private class PlayerThread extends Thread {		
		   public void run() {
				try {
					URLConnection con = url.openConnection();
					con.setConnectTimeout(5000);
					BufferedInputStream in = new BufferedInputStream(con.getInputStream());
					playing = false;
					stop = false;
					int offset = 0;
					try {
						while(!stop) {
							Thread.sleep(10);
							int bytesRead = in.read(src);
							if(bytesRead > 0) {
								if(offset + bytesRead > dst.length) {
									int left = dst.length - offset;
									System.arraycopy(src, 0, dst, offset, left);
									System.arraycopy(src, left, dst, 0, bytesRead - left);
								} else {
									System.arraycopy(src, 0, dst, offset, bytesRead);
								}
								offset = (offset + bytesRead) % dst.length;
								if(!playing && offset > (dst.length / 2)) {
									AudioPlayer.player.start(audiostream);
									playing = true;
								}
							}							
						}
					} catch (InterruptedException e) {				
						e.printStackTrace();
					}
					AudioPlayer.player.stop(audiostream);
					player = null;
					playing = false;
					stop = false;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		};

	private static final int FIRST_BUFFER_LEN = 1024;
	private static final int SECOND_BUFFER_LEN = 32768;
	private URL url;
	private byte[] src;
	private byte[] dst;
	private boolean playing = false;
	private boolean stop = false;
	private ContinuousAudioDataStream audiostream;
	PlayerThread player = new PlayerThread();

	/**
	 * Create a new MultipartAudioPlayer object
	 * @param url
	 */
	public MultipartAudioPlayer(URL url) {
		setDoubleBufferLength(FIRST_BUFFER_LEN, SECOND_BUFFER_LEN);
		setUrl(url);
	}
	
	/**
	 * Create a new MultipartAudioPlayer object
	 * @param url
	 */
	public MultipartAudioPlayer(String url) {
		setDoubleBufferLength(FIRST_BUFFER_LEN, SECOND_BUFFER_LEN);
		setUrl(url);
	}

	/**
	 * Set the size of the double buffer
	 * @param first
	 * @param second
	 */
	public void setDoubleBufferLength(int first, int second) {
		src = new byte[first];
		dst = new byte[second];
		audiostream = new ContinuousAudioDataStream(new AudioData(dst));
	}
	
	/**
	 * Set the url source
	 * @param url
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * Set the url source
	 * @param url
	 */
	public void setUrl(String url) {
		try {
			setUrl(new URL(url));
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Start playing audio
	 */
	public void start() {
		if(player == null) player = new PlayerThread();
		if(!player.isAlive()) player.start();
	}

	/**
	 * Stop playing audio
	 */
	public void stop() {
		stop = true;
	}

//	public static void main(final String[] args) {	
//		try {
////	    	URL url = new URL("http://62.204.199.193/axis-cgi/audio/receive.cgi");	
//			URL url = new URL("http://localhost:8080/audio");	
//			url.openConnection();
//			MultipartAudioPlayer player = new MultipartAudioPlayer(url);
//			System.out.println("start");
//			player.start();
//			Thread.sleep(30000);
//			System.out.println("stop");
//			player.stop();
//		} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			//		System.out.println
//			//	} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}

