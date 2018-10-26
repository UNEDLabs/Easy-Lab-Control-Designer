//******************************************************************************
//
// File:    Random.h
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

typedef struct
	{
	// Seed for this PRNG.
	unsigned long long int seed;

	// 128 bytes of extra padding to avert cache interference.
	long long int p0, p1, p2, p3, p4, p5, p6, p7;
	long long int p8, p9, pa, pb, pc, pd, pe, pf;
	}
	Random;

// Exported operations.

	/**
	 * Set this PRNG's seed. Any seed value is allowed.
	 *
	 * @param  seed  Seed.
	 */
	void setSeed
		(Random *this,
		 unsigned long long int seed);

	/**
	 * Skip the given number of positions ahead in this PRNG's sequence. If
	 * <TT>skip</TT> &lt;= 0, the <TT>skip()</TT> method does nothing.
	 *
	 * @param  skip  Number of positions to skip.
	 */
	void skip
		(Random *this,
		 long long int skip);

	/**
	 * Return the double precision floating point value from the next
	 * pseudorandom value in this PRNG's sequence. The returned numbers have a
	 * uniform distribution in the range 0.0 (inclusive) to 1.0 (exclusive).
	 *
	 * @return  Double value.
	 */
	double nextDouble
		(Random *this);
