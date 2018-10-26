//******************************************************************************
//
// File:    PiClu.c
//
// This C source file is copyright (C) 2008 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This C source file is part of the Parallel Java Library ("PJ"). PJ is free
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

#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <mpi.h>
#include "Random.h"

/**
 * PiClu is a cluster parallel program that calculates an approximate value for
 * &pi; using a Monte Carlo technique. The program generates a number of random
 * points in the unit square (0,0) to (1,1) and counts how many of them lie
 * within a circle of radius 1 centered at the origin. The fraction of the
 * points within the circle is approximately &pi;/4.
 * <P>
 * Usage: mprun -np <I>K</I> PiClu <I>seed</I> <I>N</I>
 * <BR><I>K</I> = Number of parallel processes
 * <BR><I>seed</I> = Random seed
 * <BR><I>N</I> = Number of random points
 * <P>
 * The computation is performed in parallel in multiple processors. The program
 * uses class edu.rit.util.Random for its pseudorandom number generator. The
 * program measures the computation's running time.
 *
 * @author  Alan Kaminsky
 * @version 11-Aug-2008
 */

// Program shared variables.

	// World communicator.
	static MPI_Comm world;
	static int size;
	static int rank;

	// Command line arguments.
	static long long int seed;
	static long long int N;

	// Pseudorandom number generator.
	static Random prng;

	// Number of points within the unit circle.
	static long long int count;
	static long long int gblcount;

	// Row slice lower bounds, upper bounds, and lengths, indexed by rank.
	// These are in terms of rows.
	static long long int *slicelb;
	static long long int *sliceub;
	static long long int *slicelength;

	// This process's row slice lower bound, upper bound, and length.
	static long long int mylb;
	static long long int myub;
	static long long int mylength;

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	static void usage(void)
		{
		fprintf (stderr, "Usage: mprun -np <K> PiClu <seed> <N>\n");
		fprintf (stderr, "<K> = Number of parallel processes\n");
		fprintf (stderr, "<seed> = Random seed\n");
		fprintf (stderr, "<N> = Number of random points\n");
		exit (1);
		}

	/**
	 * Returns the current wall clock time in milliseconds.
	 * Java equivalent: java.lang.System.currentTimeMillis()
	 */
	static long long int currentTimeMillis(void)
		{
		struct timeval tv;
		long long int result;
		gettimeofday (&tv, NULL);
		result = tv.tv_sec;
		result *= 1000;
		result += tv.tv_usec / 1000;
		return result;
		}

	/**
	 * Compute the row slice ranges based on size, rank, and n.
	 */
	static void computeRanges()
		{
		long long int i;
		long long int sublen = N / size;
		long long int subrem = N % size;
		long long int x = 0;

		slicelb = (long long int *) malloc (size * sizeof(long long int));
		sliceub = (long long int *) malloc (size * sizeof(long long int));
		slicelength = (long long int *) malloc (size * sizeof(long long int));

		++ sublen;
		for (i = 0; i < subrem; ++ i)
			{
			slicelb[i] = x;
			x += sublen;
			sliceub[i] = x - 1;
			slicelength[i] = sublen;
			}

		-- sublen;
		for (i = subrem; i < size; ++ i)
			{
			slicelb[i] = x;
			x += sublen;
			sliceub[i] = x - 1;
			slicelength[i] = sublen;
			}

		mylb = slicelb[rank];
		myub = sliceub[rank];
		mylength = slicelength[rank];
		}

// Main program.

	/**
	 * Main program.
	 */
	int main
		(int argc,
		 char **argv)
		{
		long long int time, i;
		double x, y;

		// Start timing.
		time = -currentTimeMillis();

		// Initialize MPI middleware.
		MPI_Init (&argc, &argv);
		world = MPI_COMM_WORLD;
		MPI_Comm_size (world, &size);
		MPI_Comm_rank (world, &rank);

		// Validate command line arguments.
		if (argc != 3) usage();
		sscanf (argv[1], "%lld", &seed);
		sscanf (argv[2], "%lld", &N);

		// Compute ranges.
		computeRanges();

		// Set up PRNG.
		setSeed (&prng, seed);
		skip (&prng, 2*mylb);

		// Generate n random points in the unit square, count how many are in
		// the unit circle.
		count = 0;
		for (i = mylb; i <= myub; ++ i)
			{
			x = nextDouble (&prng);
			y = nextDouble (&prng);
			if (x*x + y*y <= 1.0) ++ count;
			}

		// Reduce counts into process 0.
		MPI_Reduce (count, gblcount, 1, MPI_LONG_LONG_INT, MPI_SUM, 0, world);

		// Stop timing.
		time += currentTimeMillis();

		// Print results.
		if (rank == 0)
			{
			printf ("pi = 4 * %lld / %lld = %.20f\n",
				gblcount, N, 4.0 * gblcount / N);
		printf ("%lld msec %d\n", time, rank);

		// Finalize MPI middleware.
		MPI_Finalize();
		}
