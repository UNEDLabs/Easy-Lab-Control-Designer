/*
 *	SQLinkException.java - main exception class for SysquakeLink
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public class SQLinkException extends Exception
{
  private static final long serialVersionUID = 1L;

  public SQLinkException()
	{
		super();
	}
	
	public SQLinkException(String msg)
	{
		super(msg);
	}
}
