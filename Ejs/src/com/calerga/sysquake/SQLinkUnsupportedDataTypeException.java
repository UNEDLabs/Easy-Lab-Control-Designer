/*
 *	SQLinkUnsupportedDataTypeException.java - exception class for SysquakeLink
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

public class SQLinkUnsupportedDataTypeException extends SQLinkException
{
  private static final long serialVersionUID = 1L;

  public SQLinkUnsupportedDataTypeException()
	{
		super();
	}
	
	public SQLinkUnsupportedDataTypeException(String msg)
	{
		super(msg);
	}
}
