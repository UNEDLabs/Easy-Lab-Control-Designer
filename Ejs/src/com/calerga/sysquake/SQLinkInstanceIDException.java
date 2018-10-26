/*
 *	SQLinkInstanceIDException.java - exception class for SysquakeLink
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public class SQLinkInstanceIDException extends SQLinkException
{
  private static final long serialVersionUID = 1L;

  public SQLinkInstanceIDException()
	{
		super();
	}
	
	public SQLinkInstanceIDException(String msg)
	{
		super(msg);
	}
}
