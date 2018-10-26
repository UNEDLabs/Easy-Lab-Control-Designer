//******************************************************************************
//
// File:    FloydSeq.c
//
// This C source file is copyright (C) 2007 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This source file is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option) any
// later version.
//
// This source file is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html or by writing to the Free
// Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA.
//
//******************************************************************************

#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <omp.h>

/**
 * Class FloydSeq is a sequential program that uses Floyd's Algorithm to
 * calculate the length of the shortest path from each node to every other node
 * in a network, given the distance from each node to its adjacent nodes.
 * <P>
 * Floyd's Algorithm's running time is <I>O</I>(<I>N</I><SUP>3</SUP>), where
 * <I>N</I> is the number of nodes. The algorithm is as follows. On input,
 * <I>D</I> is an <I>N</I>x<I>N</I> matrix where <I>D[i,j]</I> is the distance
 * from node <I>i</I> to adjacent node <I>j</I>; if node <I>j</I> is not
 * adjacent to node <I>i</I>, then <I>D[i,j]</I> is infinity. On output,
 * <I>D[i,j]</I> has been replaced by the length of the shortest path from node
 * <I>i</I> to node <I>j</I>; if there is no path from node <I>i</I> to node
 * <I>j</I>, then <I>D[i,j]</I> is infinity.
 * <PRE>
 *     for i = 0 to N-1
 *         for r = 0 to N-1
 *             for c = 0 to N-1
 *                 D[r,c] = min (D[r,c], D[r,i] + D[i,c])
 * </PRE>
 * <P>
 * Usage: FloydSeq <I>infile</I> <I>outfile</I>
 * <BR><I>infile</I> = Input distance matrix file
 * <BR><I>outfile</I> = Output distance matrix file
 * <P>
 * The input file (<I>infile</I>) is a plain text file containing the initial
 * distance matrix. First comes one integer, the number <I>N</I>. Then come
 * <I>N</I>*<I>N</I> doubles, the distance matrix elements in row major order.
 * <TT>"inf"</TT> stands for infinity.
 * <P>
 * The output file (<I>outfile</I>) is a plain text file containing the distance
 * matrix after running Floyd's Algorithm. First comes one integer, the number
 * <I>N</I>. Then come <I>N</I>*<I>N</I> doubles, the distance matrix elements
 * in row major order. <TT>"inf"</TT> stands for infinity.
 * <P>
 * The computation is performed sequentially in a single processor. The program
 * measures the total running time (including I/O) and the computation's running
 * time (excluding I/O). This establishes a benchmark for measuring the running
 * time on a parallel processor.
 *
 * @author  Alan Kaminsky
 * @version 14-Feb-2007
 */

// Shared variables.

	// Number of nodes.
	static int n;

	// Distance matrix.
	static double **d;

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	static void usage(void)
		{
		fprintf (stderr, "Usage: FloydSeq <infile> <outfile>\n");
		fprintf (stderr, "<infile> = Input distance matrix file\n");
		fprintf (stderr, "<outfile> = Output distance matrix file\n");
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
	 * Returns the minimum of x and y.
	 * Java equivalent: java.lang.Math.min()
	 */
	static double min
		(double x,
		 double y)
		{
		return x < y ? x : y;
		}

	/**
	 * Reads an int from the given plain text file.
	 * Java equivalent: java.io.DataInputStream.readInt()
	 */
	static int readInt
		(FILE *file)
		{
		int result;
		if (fscanf (file, "%d", &result) != 1)
			{
			fprintf (stderr, "Error in readInt()\n");
			exit (1);
			}
		return result;
		}

	/**
	 * Reads a double from the given plain text file.
	 * Java equivalent: java.io.DataInputStream.readDouble()
	 */
	static double readDouble
		(FILE *file)
		{
		double result;
		if (fscanf (file, "%lf", &result) != 1)
			{
			fprintf (stderr, "Error in readDouble()\n");
			exit (1);
			}
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
		int i, r, c;
		long long int t1, t2, t3, t4;
		char *infile, *outfile;
		FILE *in, *out;

		// Start timing.
		t1 = currentTimeMillis();

		// Parse command line arguments.
		if (argc != 3) usage();
		infile = argv[1];
		outfile = argv[2];

		// Read distance matrix from input file.
		in = fopen (infile, "r");
		if (in == NULL)
			{
			fprintf (stderr, "Error opening input file \"%s\"\n", infile);
			exit (1);
			}
		n = readInt (in);
		d = (double**) malloc (n * sizeof(double*));
		for (r = 0; r < n; ++ r)
			{
			double *d_r = (double*) malloc (n * sizeof(double));
			d[r] = d_r;
			for (c = 0; c < n; ++ c)
				{
				d_r[c] = readDouble (in);
				}
			}
		fclose (in);

		// Run Floyd's Algorithm.
		//     for i = 0 to N-1
		//         for r = 0 to N-1
		//             for c = 0 to N-1
		//                 D[r,c] = min (D[r,c], D[r,i] + D[i,c])
		t2 = currentTimeMillis();
		for (i = 0; i < n; ++ i)
			{
			double *d_i = d[i];
			for (r = 0; r < n; ++ r)
				{
				double *d_r = d[r];
				for (c = 0; c < n; ++ c)
					{
					d_r[c] = min (d_r[c], d_r[i] + d_i[c]);
					}
				}
			}
		t3 = currentTimeMillis();

		// Write distance matrix to output file.
		out = fopen (outfile, "w");
		if (out == NULL)
			{
			fprintf (stderr, "Error opening output file \"%s\"\n", outfile);
			exit (1);
			}
		fprintf (out, "%d\n", n);
		for (r = 0; r < n; ++ r)
			{
			double *d_r = d[r];
			for (c = 0; c < n; ++ c)
				{
				fprintf (out, "%e ", d_r[c]);
				}
			fprintf (out, "\n");
			}
		fclose (out);

		/* Stop timing. */
		t4 = currentTimeMillis();
		printf ("%lld msec pre\n", t2-t1);
		printf ("%lld msec calc\n", t3-t2);
		printf ("%lld msec post\n", t4-t3);
		printf ("%lld msec total\n", t4-t1);
		}
