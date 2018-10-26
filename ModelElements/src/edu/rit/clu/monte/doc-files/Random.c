//******************************************************************************
//
// File:    Random.c
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

#include "Random.h"

/**
 * Class DefaultRandom provides a default pseudorandom number generator (PRNG)
 * designed for use in parallel scientific programming. To create an instance of
 * class DefaultRandom, either use the <TT>DefaultRandom()</TT> constructor, or
 * use the static <TT>getInstance(long)</TT> method in class {@linkplain
 * Random}.
 * <P>
 * Class DefaultRandom generates random numbers by hashing successive counter
 * values. The seed initializes the counter. The hash function is defined in W.
 * Press et al., <I>Numerical Recipes: The Art of Scientific Computing, Third
 * Edition</I> (Cambridge University Press, 2007), page 352. The hash function
 * applied to the counter value <I>i</I> is:
 * <P>
 * <I>x</I> := 3935559000370003845 * <I>i</I> + 2691343689449507681 (mod 2<SUP>64</SUP>)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> right-shift 21)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> left-shift 37)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> right-shift 4)
 * <BR><I>x</I> := 4768777513237032717 * <I>x</I> (mod 2<SUP>64</SUP>)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> left-shift 20)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> right-shift 41)
 * <BR><I>x</I> := <I>x</I> xor (<I>x</I> left-shift 5)
 * <BR>Return <I>x</I>
 * <P>
 * (The shift and arithmetic operations are all performed on unsigned 64-bit
 * numbers.)
 *
 * @author  Alan Kaminsky
 * @version 11-Aug-2008
 */

#define D_2_POW_NEG_64 5.4210108624275221700e-20

// Hidden operations.

	/**
	 * Return the hash of the given value.
	 */
	static unsigned long long int hash
		(unsigned long long int x)
		{
		x = 3935559000370003845L * x + 2691343689449507681L;
		x = x ^ (x >> 21);
		x = x ^ (x << 37);
		x = x ^ (x >> 4);
		x = 4768777513237032717L * x;
		x = x ^ (x << 20);
		x = x ^ (x >> 41);
		x = x ^ (x << 5);
		return x;
		}

	/**
	 * Return the next 64-bit pseudorandom value in this PRNG's sequence.
	 *
	 * @return  Pseudorandom value.
	 */
	static unsigned long long int next
		(Random *this)
		{
		++ this->seed;
		return hash (this->seed);
		}

	/**
	 * Return the 64-bit pseudorandom value the given number of positions ahead
	 * in this PRNG's sequence.
	 *
	 * @param  skip  Number of positions to skip, assumed to be &gt; 0.
	 *
	 * @return  Pseudorandom value.
	 */
	static unsigned long long int next2
		(Random *this,
		 long long int skip)
		{
		this->seed += skip;
		return hash (this->seed);
		}

// Exported operations.

	/**
	 * Set this PRNG's seed. Any seed value is allowed.
	 *
	 * @param  seed  Seed.
	 */
	void setSeed
		(Random *this,
		 unsigned long long int seed)
		{
		this->seed = hash (seed);
		}

	/**
	 * Skip the given number of positions ahead in this PRNG's sequence. If
	 * <TT>skip</TT> &lt;= 0, the <TT>skip()</TT> method does nothing.
	 *
	 * @param  skip  Number of positions to skip.
	 */
	void skip
		(Random *this,
		 long long int skip)
		{
		if (skip > 0L) next2 (this, skip);
		}

	/**
	 * Return the double precision floating point value from the next
	 * pseudorandom value in this PRNG's sequence. The returned numbers have a
	 * uniform distribution in the range 0.0 (inclusive) to 1.0 (exclusive).
	 *
	 * @return  Double value.
	 */
	double nextDouble
		(Random *this)
		{
		// Next random number is in the range 0 .. 2^64 - 1.
		// Divide by 2^64 yielding a number in the range 0.0 .. 1.0.
		return (double) (next (this)) * D_2_POW_NEG_64;
		}
