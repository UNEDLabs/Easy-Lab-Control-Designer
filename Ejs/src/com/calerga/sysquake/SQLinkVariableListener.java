/*
 *	SQLinkVariableListener.java - interface for receiving variable change notifications
 *
 *	Copyright 2005-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public interface SQLinkVariableListener
{
	/** Called whenever a variable is changed.
		@param instanceId instance ID
		@see com.calerga.sysquake.SysquakeLink#setVariableChangeNotification(int,SQLinkVariableListener) */
	public void variableChange(int instanceId);
}
