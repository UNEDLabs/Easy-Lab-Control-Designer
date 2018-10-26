package org.opensourcephysics.numerics.rk.irk;

/**
 * Non objective software for solving linear equations
 * see org.opensourcephysics.numerics.LUPdecomposition
 */


public class IRKLinearAlgebra {
    /**
     * -----------------------------------------------------------------------
     *  MATRIX TRIANGULARIZATION BY GAUSSIAN ELIMINATION.
     *  INPUT..
     *     N = ORDER OF MATRIX.
     *     NDIM = DECLARED DIMENSION OF ARRAY  A .
     *     A = MATRIX TO BE TRIANGULARIZED.
     *  OUTPUT..
     *     A[I][J], I <= J = UPPER TRIANGULAR FACTOR, U .
     *     A[I][J], I > J = MULTIPLIERS = LOWER TRIANGULAR FACTOR, I - L.
     *     IP[K], K < N - 1 = INDEX OF K-TH PIVOT ROW.
     *     IP[N - 1] = (-1)**(NUMBER OF INTERCHANGES) OR O .
     *     IER = -1 IF MATRIX A IS NONSINGULAR, OR K IF FOUND TO BE
     *             SINGULAR AT STAGE K.
     *  USE  SOL  TO OBTAIN SOLUTION OF LINEAR SYSTEM.
     *  DETERM(A) = IP[N - 1] * A[0][0] * A[1][1] * ... * A[N - 1][N - 1].
     *  IF IP[N - 1] = O, A IS SINGULAR, SOL WILL DIVIDE BY ZERO.
     *
     *  REFERENCE..
     *     C. B. MOLER, ALGORITHM 423, LINEAR EQUATION SOLVER,
     *     C.A.C.M. 15 (1972), P. 274.
     * -----------------------------------------------------------------------
     */
    public int dec(int N, int NDIM, double[][] A, int[] IP) {
        int K, KP1, M, I, J, IER;
        double T;
        /**/

        IER = -1;
        IP[N - 1] = 1;

        for(K = 0; K < N - 1; K++) {
            KP1 = K + 1;
            M = K;
            for(I = KP1; I < N; I++) {
                if( Math.abs(A[I][K]) > Math.abs(A[M][K]) ) {
                    M = I;
                }/*if*/
            }/*for*/
            IP[K] = M;
            T = A[M][K];
            if(M != K) {
                IP[N - 1] = -IP[N - 1];
                A[M][K] = A[K][K];
                A[K][K] = T;
            }/*if*/

            if(T == 0.0) {
                IER = K;
                IP[N - 1] = 0;
                return IER;
            }/*if*/

            T = 1.0 / T;
            for(I = KP1; I < N; I++) {
                A[I][K] *= -T;
            }/*for*/

            for(J = KP1; J < N; J++) {
                T = A[M][J];
                A[M][J] = A[K][J];
                A[K][J] = T;
                if(T != 0.0) {
                    for(I = KP1; I < N; I++) {
                        A[I][J] += A[I][K] * T;
                    }/*for*/
                }/*if*/
            }/*for*/

        }/*for*/

        K = N - 1;
        if(A[N - 1][N - 1] == 0.0) {
            IER = K;
            IP[N - 1] = 0;
        }/*if*/

        return IER;
    }/*dec*/

    /**
    * -----------------------------------------------------------------------
    *  SOLUTION OF LINEAR SYSTEM, A * X = B .
    *  INPUT..
    *    N = ORDER OF MATRIX.
    *    NDIM = DECLARED DIMENSION OF ARRAY  A .
    *    A = TRIANGULARIZED MATRIX OBTAINED FROM DEC.
    *    B = RIGHT HAND SIDE VECTOR.
    *    IP = PIVOT VECTOR OBTAINED FROM DEC.
    *  DO NOT USE IF DEC HAS SET IER != -1.
    *  OUTPUT..
    *    B = SOLUTION VECTOR, X .
    * -----------------------------------------------------------------------
    */
    public void sol(int N, int NDIM, double[][] A, double[] B, int[] IP) {
        int K, KP1, M, I, KB, KM1;
        double T;
        /**/

        for(K = 0; K < N - 1; K++) {
            KP1 = K + 1;
            M = IP[K];
            T = B[M];
            B[M] = B[K];
            B[K] = T;
            for(I = KP1; I < N; I++) {
                B[I] += A[I][K] * T;
            }/*for*/
        }/*for*/

        for(KB = 1; KB <= N - 1; KB++) {
            KM1 = N - KB;
            K = KM1;
            B[K] /= A[K][K];
            T = -B[K];
            for(I = 0; I < KM1; I++) {
                B[I] += A[I][K] * T;
            }/*for*/
        }/*for*/

        B[0] /= A[0][0];
    }/*sol*/


    /**
    * -----------------------------------------------------------------------
    *  MATRIX TRIANGULARIZATION BY GAUSSIAN ELIMINATION
    *  ------ MODIFICATION FOR COMPLEX MATRICES --------
    *  INPUT..
    *     N = ORDER OF MATRIX.
    *     NDIM = DECLARED DIMENSION OF ARRAYS  AR AND AI .
    *     (AR, AI) = MATRIX TO BE TRIANGULARIZED.
    *  OUTPUT..
    *     AR[I][J], I <= J = UPPER TRIANGULAR FACTOR, U ; REAL PART.
    *     AI[I][J], I <= J = UPPER TRIANGULAR FACTOR, U ; IMAGINARY PART.
    *     AR[I][J], I > J = MULTIPLIERS = LOWER TRIANGULAR FACTOR, I - L ; REAL PART.
    *     AI[I][J], I > J = MULTIPLIERS = LOWER TRIANGULAR FACTOR, I - L ; IMAGINARY PART.
    *     IP[K], K <= N - 1 = INDEX OF K-TH PIVOT ROW.
    *     IP[N - 1] = (-1)**(NUMBER OF INTERCHANGES) OR O .
    *     IER = -1 IF MATRIX A IS NONSINGULAR, OR K IF FOUND TO BE
    *              SINGULAR AT STAGE K.
    *  USE  SOLC  TO OBTAIN SOLUTION OF LINEAR SYSTEM.
    *  IF IP[N - 1] = O, A IS SINGULAR, SOLC WILL DIVIDE BY ZERO.
    *
    *  REFERENCE..
    *     C. B. MOLER, ALGORITHM 423, LINEAR EQUATION SOLVER,
    *     C.A.C.M. 15 (1972), P. 274.
    * -----------------------------------------------------------------------
    */
    public int decc(int N, int NDIM, double AR[][], double AI[][], int IP[]){
        int K, KP1, M, I, J, IER;
        double TR, TI, DEN, PRODR, PRODI;
        /**/

        IER = -1;
        IP[N - 1] = 1;

        for(K = 0; K < N - 1; K++) {
            KP1 = K + 1;
            M = K;
            for(I = KP1; I < N; I++) {
                if( Math.abs(AR[I][K]) + Math.abs(AI[I][K]) > Math.abs(AR[M][K]) + Math.abs(AI[M][K]) ) {
                    M = I;
                }/*if*/
            }/*for*/

            IP[K] = M;
            TR = AR[M][K];
            TI = AI[M][K];
            if(M != K) {
                IP[N - 1] = -IP[N - 1];
                AR[M][K] = AR[K][K];
                AI[M][K] = AI[K][K];
                AR[K][K] = TR;
                AI[K][K] = TI;
            }/*if*/

            if(Math.abs(TR) + Math.abs(TI) == 0.0) {
                IER = K;
                IP[N - 1] = 0;
                return IER;
            }/*if*/

            DEN = TR * TR + TI * TI;
            TR /= DEN;
            TI /= -DEN;
            for(I = KP1; I < N; I++) {
                PRODR = AR[I][K] * TR - AI[I][K] * TI;
                PRODI = AI[I][K] * TR + AR[I][K] * TI;
                AR[I][K] = -PRODR;
                AI[I][K] = -PRODI;
            }/*for*/

            for(J = KP1; J < N; J++) {
                TR = AR[M][J];
                TI = AI[M][J];
                AR[M][J] = AR[K][J];
                AI[M][J] = AI[K][J];
                AR[K][J] = TR;
                AI[K][J] = TI;

                if(Math.abs(TR) + Math.abs(TI) == 0.0) {
                    continue;
                }/*if*/

                if(TI == 0.0) {
                    for(I = KP1; I < N; I++) {
                        PRODR = AR[I][K] * TR;
                        PRODI = AI[I][K] * TR;
                        AR[I][J] += PRODR;
                        AI[I][J] += PRODI;
                    }/*for*/
                    continue;
                }/*if*/

                if(TR == 0.0) {
                    for(I = KP1; I < N; I++) {
                        PRODR = -AI[I][K] * TI;
                        PRODI = AR[I][K] * TI;
                        AR[I][J] += PRODR;
                        AI[I][J] += PRODI;
                    }/*for*/
                    continue;
                }/*if*/

                for(I = KP1; I < N; I++) {
                    PRODR = AR[I][K] * TR - AI[I][K] * TI;
                    PRODI = AI[I][K] * TR + AR[I][K] * TI;
                    AR[I][J] += PRODR;
                    AI[I][J] += PRODI;
                }/*for*/
            }/*for*/

        }/*for*/

        K = N - 1;
        if(Math.abs(AR[N - 1][N - 1]) + Math.abs(AI[N - 1][N - 1]) == 0.0) {
            IER = K;
            IP[N - 1] = 0;
        }/*if*/

        return IER;
    }/*decc*/

    /**
    * -----------------------------------------------------------------------
    *  SOLUTION OF LINEAR SYSTEM, A * X = B .
    *  INPUT..
    *    N = ORDER OF MATRIX.
    *    NDIM = DECLARED DIMENSION OF ARRAYS  AR AND AI.
    *    (AR,AI) = TRIANGULARIZED MATRIX OBTAINED FROM DEC.
    *    (BR,BI) = RIGHT HAND SIDE VECTOR.
    *    IP = PIVOT VECTOR OBTAINED FROM DEC.
    *  DO NOT USE IF DECC HAS SET IER != -1.
    *  OUTPUT..
    *    (BR,BI) = SOLUTION VECTOR, X .
    * -----------------------------------------------------------------------
    */
    public void solc(int N, int NDIM, double[][] AR, double[][] AI,
                     double[] BR, double[] BI, int[] IP){
        int K, KP1, M, I, KB, KM1;
        double TR, TI, PRODR, PRODI, DEN;

        for(K = 0; K < N - 1; K++) {
            KP1 = K + 1;
            M = IP[K];
            TR = BR[M];
            TI = BI[M];
            BR[M] = BR[K];
            BI[M] = BI[K];
            BR[K] = TR;
            BI[K] = TI;
            for(I = KP1; I < N; I++) {
                PRODR = AR[I][K] * TR - AI[I][K] * TI;
                PRODI = AI[I][K] * TR + AR[I][K] * TI;
                BR[I] += PRODR;
                BI[I] += PRODI;
            }/*for*/
        }/*for*/

        for(KB = 1; KB <= N - 1; KB++) {
            KM1 = N - KB;
            K = KM1;
            DEN = AR[K][K] * AR[K][K] + AI[K][K] * AI[K][K];
            PRODR = BR[K] * AR[K][K] + BI[K] * AI[K][K];
            PRODI = BI[K] * AR[K][K] - BR[K] * AI[K][K];
            BR[K] = PRODR / DEN;
            BI[K] = PRODI / DEN;
            TR = -BR[K];
            TI = -BI[K];
            for(I = 0; I < KM1; I++) {
                PRODR = AR[I][K] * TR - AI[I][K] * TI;
                PRODI = AI[I][K] * TR + AR[I][K] * TI;
                BR[I] += PRODR;
                BI[I] += PRODI;
            }/*for*/
        }/*for*/

        DEN = AR[0][0] * AR[0][0] + AI[0][0] * AI[0][0];
        PRODR = BR[0] * AR[0][0] + BI[0] * AI[0][0];
        PRODI = BI[0] * AR[0][0] - BR[0] * AI[0][0];
        BR[0] = PRODR / DEN;
        BI[0] = PRODI / DEN;
    }/*solc*/

    static class Mathadd {

        public Mathadd() {
        }/*constructor*/

        public static double sign(double a) {
            if(a > 0) return 1;
            if(a < 0) return -1;
            return 0;
        }/*sign*/

        public static double sign(double a, double b) {
            if (b >= 0) return Math.abs(a);
            return -Math.abs(a);
        }/*sign*/

    }/*Mathadd*/
}
