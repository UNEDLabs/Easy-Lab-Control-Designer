//******************************************************************************
//
// File:    FloydSeq.c
//
// This C source file is copyright (C) 2008 by Alan Kaminsky. All rights
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
#include <mpi.h>

/**
 * Class FloydClu is a cluster parallel program that runs Floyd's Algorithm. The
 * program runs the algorithm on an all-zero matrix of a given size, solely to
 * measure the running time.
 * <P>
 * Usage: mprun -np <I>K</I> FloydClu <I>n</I>
 * <BR><I>K</I> = Number of parallel processes
 * <BR><I>n</I> = Distance matrix size
 *
 * @author  Alan Kaminsky
 * @version 30-Jul-2008
 */

// Shared variables.

	// World communicator.
	static MPI_Comm world;
	static int size;
	static int rank;

	// Number of nodes.
	static int n;

	// Storage for distance matrix. This is one big block of n*n doubles. The
	// matrix elements are stored in row major order.
	static double *d_storage;

	// Distance matrix. This is an array of pointers to the first element in
	// each row of d_storage.
	static double **d;

	// Row slice lower bounds, upper bounds, and lengths, indexed by rank.
	// These are in terms of rows.
	static int *slicelb;
	static int *sliceub;
	static int *slicelength;

	// This process's row slice lower bound, upper bound, and length.
	static int mylb;
	static int myub;
	static int mylength;

	// Row broadcast from another process.
	static double *row_i;

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	static void usage(void)
		{
		fprintf (stderr, "Usage: mprun -np <K> FloydSeq <n>\n");
		fprintf (stderr, "<K> = Number of parallel processes\n");
		fprintf (stderr, "<n> = Distance matrix size\n");
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
	 * Compute the row slice ranges based on size, rank, and n.
	 */
	static void computeRanges()
		{
		int i;
		int sublen = n / size;
		int subrem = n % size;
		int x = 0;

		slicelb = (int *) malloc (size * sizeof(int));
		sliceub = (int *) malloc (size * sizeof(int));
		slicelength = (int *) malloc (size * sizeof(int));

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

	/**
	 * Allocate storage for the distance matrix based on n, mylb, and myub.
	 */
	static void allocateDistanceMatrix()
		{
		int i, r;
		int n_len = n*mylength;

		d_storage = (double *) malloc (n_len * sizeof(double));
		for (i = 0; i < n_len; ++ i)
			{
			d_storage[i] = 0.0;
			}

		d = (double**) malloc (n * sizeof(double*));
		for (r = 0; r < mylb; ++ r)
			{
			d[r] = NULL;
			}
		for (r = mylb; r <= myub; ++ r)
			{
			d[r] = d_storage + n*(r - mylb);
			}
		for (r = myub+1; r < n; ++ r)
			{
			d[r] = NULL;
			}
		}

	/**
	 * Abort due to an array index out of bounds.
	 */
	static void outOfBounds()
		{
		fprintf (stderr, "ArrayIndexOutOfBoundsException\n");
		exit (1);
		}

// Main program.

	/**
	 * Main program.
	 */
	int main
		(int argc,
		 char **argv)
		{
		int i, r, c, i_root;
		long long int t1, t2;

		// Initialize MPI middleware.
		MPI_Init (&argc, &argv);
		world = MPI_COMM_WORLD;
		MPI_Comm_size (world, &size);
		MPI_Comm_rank (world, &rank);

		// Parse command line arguments.
		if (argc != 2) usage();
		sscanf (argv[1], "%d", &n);

		// Divide distance matrix into equal row slices.
		computeRanges();

		// Allocate just this process's row slice of the distance matrix.
		allocateDistanceMatrix();

		// Allocate storage for row broadcast from another process.
		row_i = (double *) malloc (n * sizeof(double));

		// Run Floyd's Algorithm.
		//     for i = 0 to N-1
		//         for r = 0 to N-1
		//             for c = 0 to N-1
		//                 D[r,c] = min (D[r,c], D[r,i] + D[i,c])
		t1 = currentTimeMillis();
		i_root = 0;
		for (i = 0; i < n; ++ i)
			{
			double *d_i;
			int slicelb_i_root, sliceub_i_root;

			if (i < 0 || i >= n) outOfBounds();
			d_i = d[i];

			// Determine which process owns row i.
			if (i_root < 0 || i_root >= size) outOfBounds();
			slicelb_i_root = slicelb[i_root];
			if (i_root < 0 || i_root >= size) outOfBounds();
			sliceub_i_root = sliceub[i_root];
			if (slicelb_i_root > i || i > sliceub_i_root) ++ i_root;

			// Broadcast row i from owner process to all processes.
			if (rank == i_root)
				{
				MPI_Bcast
					(/*buffer  */ d_i,
					 /*count   */ n,
					 /*datatype*/ MPI_DOUBLE,
					 /*root    */ i_root,
					 /*comm    */ world);
				}
			else
				{
				MPI_Bcast
					(/*buffer  */ row_i,
					 /*count   */ n,
					 /*datatype*/ MPI_DOUBLE,
					 /*root    */ i_root,
					 /*comm    */ world);
				d_i = row_i;
				}

			// Inner loops over rows in my slice and over all columns.
			for (r = mylb; r <= myub; ++ r)
				{
				double *d_r;
				if (r < 0 || r >= n) outOfBounds();
				d_r = d[r];
				for (c = 0; c < n; ++ c)
					{
					double d_r_c, d_r_i, d_i_c;
					if (c < 0 || c >= n) outOfBounds();
					d_r_c = d_r[c];
					if (i < 0 || i >= n) outOfBounds();
					d_r_i = d_r[i];
					if (c < 0 || c >= n) outOfBounds();
					d_i_c = d_i[c];
					if (c < 0 || c >= n) outOfBounds();
					d_r[c] = min (d_r_c, d_r_i + d_i_c);
					}
				}
			}
		t2 = currentTimeMillis();

		// Print running time.
		printf ("%lld msec %d\n", t2-t1, rank);

		// Finalize MPI middleware.
		MPI_Finalize();
		}
