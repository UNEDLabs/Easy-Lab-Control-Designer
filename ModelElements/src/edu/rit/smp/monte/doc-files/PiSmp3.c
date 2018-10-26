//******************************************************************************
//
// File:    PiSmp3.c
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
#include "Random.h"

/**
 * PiSmp3 is an SMP parallel program that calculates an approximate value for
 * &pi; using a Monte Carlo technique. The program generates a number of random
 * points in the unit square (0,0) to (1,1) and counts how many of them lie
 * within a circle of radius 1 centered at the origin. The fraction of the
 * points within the circle is approximately &pi;/4.
 * <P>
 * Usage: PiSmp3 <I>seed</I> <I>N</I>
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

	// Command line arguments.
	static long long int seed;
	static long long int N;

	// Pseudorandom number generator.
	static Random prng;

	// Number of points within the unit circle.
	static long long int count;

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	static void usage(void)
		{
		fprintf (stderr, "Usage: PiSmp3 <seed> <N>\n");
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

		// Validate command line arguments.
		if (argc != 3) usage();
		sscanf (argv[1], "%lld", &seed);
		sscanf (argv[2], "%lld", &N);

		#pragma omp parallel private(prng,x,y) reduction(+:count)
			{
			// Set up PRNG.
			setSeed (&prng, seed);

			// Generate n random points in the unit square, count how many are
			// in the unit circle.
			count = 0;
			#pragma omp for schedule(static)
			for (i = 0; i < N; ++ i)
				{
				x = nextDouble (&prng);
				y = nextDouble (&prng);
				if (x*x + y*y <= 1.0) ++ count;
				}
			}

		// Stop timing.
		time += currentTimeMillis();

		// Print results.
		printf ("pi = 4 * %lld / %lld = %.20f\n", count, N, 4.0 * count / N);
		printf ("%lld msec\n", time);
		}
