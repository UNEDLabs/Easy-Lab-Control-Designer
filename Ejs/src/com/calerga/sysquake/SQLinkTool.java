/*
 *	SQLinkTool.java
 *
 *	Copyright 2006-2007, Calerga Sarl
 *	All rights reserved
 */

package com.calerga.sysquake;

import com.calerga.sysquake.SysquakeLink;
import com.calerga.sysquake.SQLinkVariableListener;
import java.lang.reflect.*;
import java.util.*;

public class SQLinkTool implements SQLinkVariableListener
{
	private String notifVar;
	
	public SQLinkTool(String var)
	{
		notifVar = var;
	}
	
	public void variableChange(int instanceId)
	{
		try
		{
			SysquakeLink.connect();
			Object value = SysquakeLink.variableValue(instanceId, notifVar);
			SysquakeLink.disconnect();
			displayObject(value, true);
			System.out.println();
		} 
		catch (SQLinkException e)
		{
			System.err.println(e.toString());
		}
	}
	
	private static int decodeNum(String str)
		// convert string to integer, or -1 if error
	{
		try
		{
			return Integer.decode(str);
		} catch (NumberFormatException e)
		{
			return -1;
		}
	}
	
	private static Object decodeObject(String str)
		// convert str to a numerical object (Double, double[][], Boolean, boolean[][])
		//  using LME syntax, or keep str
	{
		try
		{
			if (str.charAt(0) == '[')
			{
				// try empty array
				if (str.length() >= 2 && str.charAt(1) == ']')
					return new double[0][0];
				
				// try boolean
				if (str.substring(1).trim().startsWith("true")
						|| str.substring(1).trim().startsWith("false"))
				{
					List<boolean []> a = new ArrayList<boolean []>();
					boolean [] row = null;
					int m = 0, n = 0;	// 2d array size
					int i = 0;	// current column
				loop:
					for (int k = 1; ; )
					{
						// skip white space
						while (k < str.length() && Character.isSpaceChar(str.charAt(k)))
							k++;
						if (k >= str.length())
							throw new NumberFormatException();
						if (str.charAt(k) == ']')
							break;
						
						// get boolean value
						boolean val = str.startsWith("true", k);
						if (!val && !str.startsWith("false", k))
							throw new NumberFormatException();
						k += val ? 4 : 5;
						
						// add it to current row
						if (m > 0 && i > n)
							throw new NumberFormatException();
						if (row == null)
							row = new boolean[m == 0 ? 10 : n];
						else if (i >= row.length)
						{
							boolean [] row1 = new boolean[2 * row.length];
							for (int j = 0; j < n; j++)
								row1[j] = row[j];
							row = row1;
						}
						row[i++] = val;
						
						// skip white space
						while (k < str.length() && Character.isSpaceChar(str.charAt(k)))
							k++;
						if (k >= str.length())
							throw new NumberFormatException();
						
						// find next separator (comma, semicolon or right bracket)
						switch (str.charAt(k))
						{
							case ';':
							case ']':
								if (m > 0)
								{
									// check row length
									if (i != n)
										throw new NumberFormatException();
								}
								else
									n = i;
								if (n != row.length)
								{
									// resize row
									boolean [] row1 = new boolean[n];
									for	(int j = 0; j < n; j++)
										row1[j] = row[j];
									row = row1;
								}
								// add row to array a
								a.add(row);
								
								if (str.charAt(k) == ']')	// last row
									return a.toArray();
								
								// continue with next row
								m++;
								i = 0;
								break loop;
							case ',':
								// skip comma
								k++;
								break;
						}
					}
				}
				
				// try double
				StringTokenizer tok = new StringTokenizer(str, " [,;]", true);
				String token;
				List<double []> a = new ArrayList<double []>();
				double [] row = null;
				int m = 0, n = 0;	// 2d array size
				int i = 0;	// current column
				tok.nextToken();	// skip initial bracket
			loop:
				while (tok.hasMoreTokens())
				{
					// next token, skipping white space
					do
						token = tok.nextToken();
					while (token.equals(" "));
					if (token.equals(",") || token.equals(";") || token.equals("]"))
						break;	// unexpected here
					
					// get double value
					double val = Double.valueOf(token);
					
					// add it to current row
					if (m > 0 && i > n)
						throw new NumberFormatException();
					if (row == null)
						row = new double[m == 0 ? 10 : n];
					else if (i >= row.length)
					{
						double [] row1 = new double[2 * row.length];
						for (int j = 0; j < n; j++)
							row1[j] = row[j];
						row = row1;
					}
					row[i++] = val;
					
					// next token, skipping white space
					do
						token = tok.nextToken();
					while (token.equals(" "));
					if (!token.equals(",") && !token.equals(";") && !token.equals("]"))
						break;	// unexpected here
					
					// process separator (comma, semicolon or right bracket)
					switch (token.charAt(0))
					{
						case ';':
						case ']':
							if (m > 0)
							{
								// check row length
								if (i != n)
									throw new NumberFormatException();
							}
							else
								n = i;
							if (n != row.length)
							{
								// resize row
								double [] row1 = new double[n];
								for	(int j = 0; j < n; j++)
									row1[j] = row[j];
								row = row1;
							}
							// add row to array a
							a.add(row);
							
							if (token.charAt(0) == ']')	// last row
								return a.toArray();
							
							// continue with next row
							m++;
							i = 0;
							break loop;
					}
				}
				
				// unrecognized: return original string
				return str;
			}
			return Double.valueOf(str);
		} catch (NumberFormatException e)
		{
			return str;
		}
	}
	
	/**
	Display an object coming from Sysquake.
	@param obj object to display
	@param brakets true if arrays are displayed inside brackets, false otherwise
	*/
	private static void displayObject(Object obj, boolean brackets)
	{
		if (obj.getClass().isArray())
		{
			boolean is2dimArray = obj instanceof double[][]
					|| obj instanceof boolean[][]
					|| obj instanceof int[][];
			if (brackets)
				System.out.print("[");
			for (int i = 0; i < Array.getLength(obj); i++)
			{
				if (i > 0)
					System.out.print(is2dimArray ? ";" : ",");
				if (obj instanceof Object [])
					displayObject(((Object [])obj)[i], !is2dimArray);
				else if (obj instanceof double [])
					System.out.print(((double [])obj)[i]);
				else if (obj instanceof int [])
					System.out.print(((int [])obj)[i]);
				else if (obj instanceof boolean [])
					System.out.print(((boolean [])obj)[i]);
				else
					System.out.print("#");
			}
			if (brackets)
				System.out.print("]");
		}
		else
			System.out.print(obj);
	}
	
	/**
	Main method of SQLinkTool, to be called from a terminal.
	*/
	public static void main(String[] args)
	{
		int sqID = -1;
		String notifVar = null;	// name of variable whose changes are notified
		int duration = 5000;
		
		try
		{
			for (int i = 0; i < args.length; i++)
				if (args[i].equals("-h"))
				{
					System.err.println("Usage: java -classpath . SQLinkTest args");
					System.err.println("-c name      display a variable value everytime it changes in Sysquake");
					System.err.println("-d time      duration for -c in milliseconds");
					System.err.println("-e cmd       execute command");
					System.err.println("-h           display help");
					System.err.println("-i id|\"lme\"  specify SQ instance ID or LME");
					System.err.println("-g name|ix   display value of a single SQ instance or LME variable");
					System.err.println("-G vlist     display value of SQ instance or LME variables");
					System.err.println("-l path      load an SQ or SQD file");
					System.err.println("-n           display name of SQ instance or LME variables");
					System.err.println("-r id        reload an SQ file instance");
					System.err.println("-s name|ix v set value of a single SQ instance");
					System.err.println("-S slist     set value of SQ instance or LME variables");
					System.err.println("-v           display Sysquake version");
					System.err.println("\"vlist\" is a list of names or indices ending with the last argument");
					System.err.println("or with a hyphen (\"-\").");
					System.err.println("\"slist\" is a list of pairs (name or index)/value ending with the last argument");
					System.err.println("or with a hyphen (\"-\").");
				}
				else if (i + 1 < args.length && args[i].equals("-e"))
				{
					SysquakeLink.connect();
					SysquakeLink.execute(args[i + 1]);
					SysquakeLink.disconnect();
					i++;
				}
				else if (i + 1 < args.length && args[i].equals("-l"))
				{
					SysquakeLink.connect();
					int id = SysquakeLink.open(args[i + 1]);
					SysquakeLink.disconnect();
					System.out.println(id);
					i++;
				}
				else if (i + 1 < args.length && args[i].equals("-r") && decodeNum(args[i + 1]) >= 0)
				{
					SysquakeLink.reload(decodeNum(args[i + 1]));
					i++;
				}
				else if (i + 1 < args.length && args[i].equals("-i")
						&& (args[i + 1].equals("lme") || decodeNum(args[i + 1]) >= 0))
				{
					sqID = decodeNum(args[i + 1]);
					i++;
				}
				else if (args[i].equals("-n"))
				{
					SysquakeLink.connect();
					String [] name;
					if (sqID < 0)
						name = SysquakeLink.lmeVariableNames();
					else
						name = SysquakeLink.variableNames(sqID);
					SysquakeLink.disconnect();
					for (int j = 0; j < name.length; j++)
						System.out.println(name[j]);
				}
				else if (i + 1 < args.length && args[i].equals("-g"))
				{
					SysquakeLink.connect();
					Object value;
					if (sqID < 0)
						if (decodeNum(args[i + 1]) >= 0)
							value = SysquakeLink.lmeVariableValue(decodeNum(args[i + 1]));
						else
							value = SysquakeLink.lmeVariableValue(args[i + 1]);
					else
						if (decodeNum(args[i + 1]) >= 0)
							value = SysquakeLink.variableValue(sqID, decodeNum(args[i + 1]));
						else
							value = SysquakeLink.variableValue(sqID, args[i + 1]);
					SysquakeLink.disconnect();
					if (value != null)
					{
						displayObject(value, true);
						System.out.println();
					}
					else
						System.out.println("No value.");
					i++;
				}
				else if (args[i].equals("-G"))
				{
					// count elements and check whether they're all numeric
					int count;
					boolean isNumeric;
					for (count = 0, isNumeric = true;
							i + 1 + count < args.length && !args[i + 1 + count].equals("-");
							count++)
						if (decodeNum(args[i + 1 + count]) < 0)
							isNumeric = false;
					
					SysquakeLink.connect();
					
					// make a list
					int [] indices = null;
					String [] names = null;
					if (isNumeric)
					{
						indices = new int[count];
						for (int j = 0; j < count; j++)
							indices[j] = decodeNum(args[i + 1 + j]);
					}
					else
					{
						names = new String[count];
						for (int j = 0; j < count; j++)
							names[j] = args[i + 1 + j];
					}
					
					// get values from Sysquake
					Object [] values;
					if (sqID < 0)
						if (isNumeric)
							values = SysquakeLink.lmeVariableValue(indices);
						else
							values = SysquakeLink.lmeVariableValue(names);
					else
						if (isNumeric)
							values = SysquakeLink.variableValue(sqID, indices);
						else
							values = SysquakeLink.variableValue(sqID, names);
					SysquakeLink.disconnect();
					
					// display values
					for (int j = 0; j < count; j++)
						if (values[j] != null)
						{
							displayObject(values[j], true);
							System.out.println();
						}
						else
							System.err.println("No value for variable " + args[i + 1 + j] + ".");
					
					// skip arguments
					i += count;
					if (i + 1 < args.length && args[i + 1].equals("-"))
						i++;
				}
				else if (i + 2 < args.length && args[i].equals("-s"))
				{
					if (sqID < 0)
						System.err.println("No instance ID specified.");
					else
					{
						SysquakeLink.connect();
						if (decodeNum(args[i + 1]) >= 0)
							SysquakeLink.setVariableValue(sqID,
									decodeNum(args[i + 1]),
									decodeObject(args[i + 2]));
						else
							SysquakeLink.setVariableValue(sqID,
									args[i + 1],
									decodeObject(args[i + 2]));
						SysquakeLink.disconnect();
					}
					i += 2;
				}
				else if (args[i].equals("-S"))
				{
					// count elements and check whether they're all numeric
					int count;
					boolean isNumeric;
					for (count = 0, isNumeric = true;
							i + 1 + 2 * count < args.length && !args[i + 1 + 2 * count].equals("-");
							count++)
						if (decodeNum(args[i + 1 + 2 * count]) < 0)
							isNumeric = false;
					
					SysquakeLink.connect();
					
					// make a list
					int [] indices = null;
					String [] names = null;
					Object [] values = new Object[count];
					if (isNumeric)
					{
						indices = new int[count];
						for (int j = 0; j < count; j++)
						{
							indices[j] = decodeNum(args[i + 1 + 2 * j]);
							values[j] = decodeObject(args[i + 1 + 2 * j + 1]);
						}
					}
					else
					{
						names = new String[count];
						for (int j = 0; j < count; j++)
						{
							names[j] = args[i + 1 + 2 * j];
							values[j] = decodeObject(args[i + 1 + 2 * j + 1]);
						}
					}
					
					// send request to Sysquake
					if (isNumeric)
						SysquakeLink.setVariableValue(sqID, indices, values);
					else
						SysquakeLink.setVariableValue(sqID, names, values);
					SysquakeLink.disconnect();
					
					// skip arguments
					i += 2 * count;
					if (i + 1 < args.length && args[i + 1].equals("-"))
						i++;
				}
				else if (args[i].equals("-v"))
				{
					SysquakeLink.connect();
					String vers = SysquakeLink.version();
					SysquakeLink.disconnect();
					System.out.println(vers);
				}
				else if (i + 1 < args.length && args[i].equals("-c"))
				{
					notifVar = args[i + 1];
					i++;
				}
				else if (i + 1 < args.length && args[i].equals("-d"))
				{
					duration = decodeNum(args[i + 1]);
					i++;
				}
				else
				{
					System.err.println("Unknown argument (-h for help)");
					return;
				}
		}
		catch (SQLinkException e)
		{
			System.err.println(e.toString());
		}
		
		if (sqID >= 0 && notifVar != null)
			try
			{
				SQLinkTool varChangeListener = new SQLinkTool(notifVar);
				SysquakeLink.connect();
				SysquakeLink.setVariableChangeNotification(sqID, varChangeListener);
				SysquakeLink.disconnect();
				
				try
				{
          Thread.sleep(duration);
				}
				catch (InterruptedException e)
				{
				}
				
				SysquakeLink.connect();
				SysquakeLink.resetVariableChangeNotification(sqID);
				SysquakeLink.disconnect();
			}
			catch (SQLinkException e)
			{
				System.err.println(e.toString());
			}
		
		SysquakeLink.disconnect();	// just in case we're still connected
	}
}
