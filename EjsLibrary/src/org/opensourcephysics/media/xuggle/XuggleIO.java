package org.opensourcephysics.media.xuggle;

import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.media.core.VideoFileFilter;
import org.opensourcephysics.media.core.VideoIO;

/**
 * This registers Xuggle with VideoIO so it can be used to open and record videos.
 *
 * @author Wolfgang Christian, Douglas Brown
 * @version 1.0
 */
public class XuggleIO {
	
	  /**
	   * Registers Xuggle video types with VideoIO class.
	   */
	  static public void registerWithVideoIO(){ // add Xuggle video types, if available
	    String xugglehome = System.getenv("XUGGLE_HOME"); //$NON-NLS-1$
	    if (xugglehome!=null) {
	      try {
	    	  VideoIO.addVideoEngine(new XuggleVideoType());
	        // add common video types shared with QuickTime
	      	for (String ext: VideoIO.VIDEO_EXTENSIONS) { // {"mov", "avi", "mp4"}
	        	VideoFileFilter filter = new VideoFileFilter(ext, new String[] {ext});
	        	XuggleVideoType xuggleType = new XuggleVideoType(filter);
	        	// pig mov and mp4 not recordable with Xuggle v5.4.0--fix this
	          if (ext.equals("mov") || ext.equals("mp4")) { //$NON-NLS-1$ //$NON-NLS-2$
	          	xuggleType.setRecordable(false);
	          }
	          VideoIO.addVideoType(xuggleType);
	      	} 
	      	// add additional xuggle types
	        VideoFileFilter filter = new VideoFileFilter("flv", new String[] {"flv"}); //$NON-NLS-1$ //$NON-NLS-2$
	        VideoIO.addVideoType(new XuggleVideoType(filter));
	      	filter = new VideoFileFilter("asf", new String[] {"wmv"}); //$NON-NLS-1$ //$NON-NLS-2$
	        VideoIO.addVideoType(new XuggleVideoType(filter));
	      	filter = new VideoFileFilter("dv", new String[] {"dv"}); //$NON-NLS-1$ //$NON-NLS-2$
	      	XuggleVideoType vidType = new XuggleVideoType(filter);
	      	vidType.setRecordable(false);
	      	VideoIO.addVideoType(vidType);
	      	filter = new VideoFileFilter("mts", new String[] {"mts"}); //$NON-NLS-1$ //$NON-NLS-2$
	      	vidType = new XuggleVideoType(filter);
	      	vidType.setRecordable(false);
	      	VideoIO.addVideoType(vidType);
	      }
	      catch (Exception ex) { // Xuggle not working
	      	OSPLog.config("Xuggle exception: "+ex.toString()); //$NON-NLS-1$
	      }    	
	      catch (Error er) { // Xuggle not working
	      	OSPLog.config("Xuggle error: "+er.toString()); //$NON-NLS-1$
	      }    	
	    }
	    else {
	    	OSPLog.config("Xuggle not installed? (XUGGLE_HOME not found)"); //$NON-NLS-1$
	    }
	  }
}
