/*
 *	SQLinkVariableException.java - exception class for SysquakeLink
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public class SQLinkVariableException extends SQLinkException
{
  private static final long serialVersionUID = 1L;

  public SQLinkVariableException()
	{
		super();
	}
	
	public SQLinkVariableException(String msg)
	{
		super(msg);
	}
}
