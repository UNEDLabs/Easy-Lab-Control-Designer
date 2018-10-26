//******************************************************************************
//
// File:    PrimeCountFunctionHyb.java
// Package: edu.rit.hyb.prime
// Unit:    Class edu.rit.hyb.prime.PrimeCountFunctionHyb
//
// This Java source file is copyright (C) 2012 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package edu.rit.hyb.prime;

import edu.rit.mp.LongBuf;

import edu.rit.mp.buf.LongItemBuf;

import edu.rit.pj.Comm;
import edu.rit.pj.LongForLoop;
import edu.rit.pj.LongSchedule;
import edu.rit.pj.ParallelRegion;
import edu.rit.pj.ParallelTeam;
import edu.rit.pj.WorkerLongForLoop;
import edu.rit.pj.WorkerRegion;
import edu.rit.pj.WorkerTeam;

import edu.rit.pj.reduction.LongOp;
import edu.rit.pj.reduction.SharedLong;

import java.io.File;

/**
 * Class PrimeCountFunctionHyb is a hybrid parallel program that calculates the
 * prime counting function &pi;(<I>x</I>). &pi;(<I>x</I>) is the number of
 * primes less than or equal to <I>x</I>. The program uses a list of 32-bit
 * primes stored in a file. The prime file must be generated by the {@linkplain
 * Prime32File} program. To find the primes, the program calculates a series of
 * sieves. Each sieve consists of one million numbers.
 * <P>
 * The program runs with one process per node and multiple threads per process.
 * The program uses the master-worker pattern for load balancing. Each process
 * in the program is an independent worker process. Each worker process
 * calculates a series of groups of sieves, as assigned by the master thread.
 * Within each group of sieves, the threads of the worker process calculate the
 * individual sieves in parallel.
 * <P>
 * The groups of sieves are determined by the <TT>pj.schedule</TT> property
 * specified on the command line; the default is to divide the sieves evenly
 * among the worker processes (i.e. no load balancing). For further information
 * about the <TT>pj.schedule</TT> property, see class {@linkplain
 * edu.rit.pj.PJProperties PJProperties}.
 * <P>
 * Within each group of sieves, the individual sieves are partitioned among the
 * threads of the worker process using the parallel loop schedule specified by
 * the last command line argument. If this argument is missing, the default is
 * to divide the individual sieves evenly among the threads (i.e. no load
 * balancing). For further information, see the <TT>parse()</TT> method in class
 * {@linkplain edu.rit.pj.IntegerSchedule IntegerSchedule}.
 * <P>
 * Usage: java -Dpj.np=<I>Kp</I> -Dpj.nt=<I>Kt</I> [
 * -Dpj.schedule=<I>procschedule</I> ] edu.rit.hyb.prime.PrimeCountFunctionHyb
 * <I>x</I> <I>primefile</I> [ <I>thrschedule</I> ]
 * <BR><I>Kp</I> = Number of parallel processes
 * <BR><I>Kt</I> = Number of parallel threads per process
 * <BR><I>procschedule</I> = Load balancing schedule for processes
 * <BR><I>x</I> = Argument of prime counting function, 0 &lt;= <I>x</I> &lt;=
 * 2<SUP>63</SUP>-1
 * <BR><I>primefile</I> = Prime file name
 * <BR><I>thrschedule</I> = Load balancing schedule for threads
 * <P>
 * The computation is performed in parallel in multiple processors. The program
 * measures the total running time.
 *
 * @author  Alan Kaminsky
 * @version 19-Mar-2012
 */
public class PrimeCountFunctionHyb
	{

// Prevent construction.

	private PrimeCountFunctionHyb()
		{
		}

// Shared global variables.

	// Sieve in one-million-number chunks.
	static final int CHUNK = 1000000;

	// World communicator.
	static Comm world;
	static int rank;

	// Command line arguments.
	static long x;
	static File primeFile;
	static LongSchedule thrSchedule;

	// Parallel team.
	static ParallelTeam team;

	// Per-thread sieves.
	static Sieve[] sieves;

	// List of 32-bit primes.
	static Prime32List primeList;

	// Per-process prime counter.
	static SharedLong primeCount = new SharedLong (0);

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Start timing.
		long t1 = System.currentTimeMillis();

		// World communicator.
		Comm.init (args);
		world = Comm.world();
		rank = world.rank();

		// Parse command line arguments.
		if (args.length < 2 || args.length > 3) usage();
		x = Long.parseLong (args[0]);
		if (x < 0) usage();
		primeFile = new File (args[1]);
		thrSchedule =
			args.length == 3 ?
				LongSchedule.parse (args[2]) :
				LongSchedule.fixed();

		// Set up parallel team and per-thread sieves.
		team = new ParallelTeam();
		sieves = new Sieve [team.getThreadCount()];
		for (int i = 0; i < sieves.length; ++ i)
			{
			sieves[i] = new Sieve (0, CHUNK);
			}

		// Set up list of 32-bit primes.
		primeList = new Prime32List (primeFile);

		// Compute sieves in parallel using a two-level schedule for load
		// balancing. First-level schedule controlled by -Dpj.schedule.
		new WorkerTeam().execute (new WorkerRegion()
			{
			public void run() throws Exception
				{
				// Determine number of sieves to calculate.
				long ns = (x + CHUNK - 1)/CHUNK;
				execute (0, ns - 1, new WorkerLongForLoop()
					{
					public void run (final long lb, final long ub)
						throws Exception
						{
						team.execute (new ParallelRegion()
							{
							public void run() throws Exception
								{
								execute (lb, ub, new LongForLoop()
									{
									// Per-thread variables plus extra padding.
									Sieve thrSieve;
									long thrPrimeCount;
									long p0, p1, p2, p3, p4, p5, p6, p7;
									long p8, p9, pa, pb, pc, pd, pe, pf;

									// Second-level schedule controlled by last
									// command line argument.
									public LongSchedule schedule()
										{
										return thrSchedule;
										}

									// Initialize per-thread variables.
									public void start()
										{
										thrSieve = sieves[getThreadIndex()];
										thrPrimeCount = 0;
										}

									// Calculate all sieves.
									public void run (long first, long last)
										throws Exception
										{
										for (long lb = first; lb <= last; ++ lb)
											{
											// Get an iterator for the odd
											// primes.
											LongIterator iter =
												primeList.iterator();

											// Calculate the sieve.
											thrSieve.lb (lb*CHUNK);
											thrSieve.initialize();
											thrSieve.sieveOut (iter);

											// Count primes <= x left in the
											// sieve.
											iter = thrSieve.iterator();
											long p;
											while ((p = iter.next()) != 0 &&
													p <= x)
												{
												++ thrPrimeCount;
												}
											}
										}

									// Reduce per-thread prime count into
									// per-process prime count.
									public void finish()
										{
										primeCount.addAndGet (thrPrimeCount);
										}
									});
								}
							});
						}
					});
				}
			});

		// Reduce per-process prime counts into process 0.
		LongItemBuf buf = LongBuf.buffer (primeCount.longValue());
		world.reduce (0, buf, LongOp.SUM);

		// Stop timing.
		long t2 = System.currentTimeMillis();

		// Print the answer. (Add 1 because 2 is a prime.)
		if (rank == 0) System.out.println ("pi("+x+") = "+(buf.item + 1));
		System.out.println ((t2-t1)+" msec "+rank);
		}

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java -Dpj.np=<Kp> -Dpj.nt=<Kt> [ -Dpj.schedule=<procschedule> ] edu.rit.hyb.prime.PrimeCountFunctionHyb <x> <primefile> [ <thrschedule> ]");
		System.err.println ("<Kp> = Number of parallel processes");
		System.err.println ("<Kt> = Number of parallel threads per process");
		System.err.println ("<procschedule> = Load balancing schedule for processes");
		System.err.println ("<x> = Argument of prime counting function, 0 <= <x> <= 2^63-1");
		System.err.println ("<primefile> = Prime file name");
		System.err.println ("<thrschedule> = Load balancing schedule for threads System.exit (1);");
		}

	}