/*
 *	SQLinkConnectionException.java - exception class for SysquakeLink
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public class SQLinkConnectionException extends SQLinkException
{
  private static final long serialVersionUID = 1L;

  public SQLinkConnectionException()
	{
		super();
	}
	
	public SQLinkConnectionException(String msg)
	{
		super(msg);
	}
}
