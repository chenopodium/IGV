/* Copyright (C) 2010 Ion Torrent Systems, Inc. All Rights Reserved */
/*
 * LICENSE to be determined
 */
package com.iontorrent.sam2flowgram.util;

/**
 * Utility functions
 *
 * @author nils.homer@lifetech.com
 */
public class AlignUtil {

    /**
     * Guarantees this class can't be instantiated. Also means the class can't
     * be sub-classed
     */
    private AlignUtil() {
        //should never happen unless someone tries to call 
        //constructor from within this class.  
        throw new AssertionError();
    }
    ;
	
    /**
     * DNA character array, useful for converting DNA as integer values to DNA as characters. 
     */
    public static final char DNA[] = {'A', 'C', 'G', 'T', 'N'};
    /**
     * Converts a DNA integer value to its reverse compliment integer value.
     *
     */
    public static final byte NTINT2COMP[] = {
        3, 2, 1, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    };
    /**
     * Converts the DNA ASCII value to the DNA integer value.
     */
    public static final byte NT2INT[] = {
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 0, 4, 1, 4, 4, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 0, 4, 1, 4, 4, 4, 2, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
        4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    };
    /**
     * Converts the IUPAC ASCII value to a DNA ASCII value. If the IUPAC ASCII
     * value is an non-DNA base, then it is converted to the lexicographically
     * smallest non-matching DNA base, or an A if it is an N.
     */
    public static final byte IUPAC2NT[] = {
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '-', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'A', 'A', 'C', 'C', 'N', 'N', 'G', 'G', 'N', 'N', 'N', 'A', 'N', 'G', 'A',
        'N', 'N', 'C', 'A', 'T', 'N', 'T', 'C', 'N', 'A', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'A', 'A', 'C', 'N', 'N', 'N', 'G', 'G', 'N', 'N', 'N', 'A', 'N', 'G', 'A',
        'N', 'N', 'C', 'A', 'T', 'N', 'T', 'C', 'N', 'A', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N'
    };
    /**
     * A generally useful negative infinity value.
     */
    public static final int MIN_INF = -1000000;

    /**
     * This method assumes the input ASCII quality character is Sanger encoded
     * (+33).
     *
     * @param qual The Sanger encoded ASCII quality character.
     * @return the PHRED value.
     */
    public static int CHAR2QUAL(char qual) {
        int q = (((int) qual) - 33);
        if (q < 0) {
            return 1;
        } else if (255 < q) {
            return 255;
        } else {
            return q;
        }
    }

    public static String complement(String bases) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < bases.length(); i++) {
            char base = bases.charAt(i);
            char comp = getComplement(base);
            b = b.append(comp);
        }
        return b.toString();
    }
    public static char getComplement(char base) {
        if (base == 'A') {
            return 'T';
        } else if (base == 'T') {
            return 'A';
        } else if (base == 'G') {
            return 'C';
        } else if (base == 'C') {
            return 'G';
        }
        else return base;
    }

    /**
     * @param qual The PHRED quality value.
     * @return the Sanger encoded (+33) ASCII quality character.
     */
    public static char QUAL2CHAR(int qual) {
        if (93 < qual) {
            return (char) (126);
        } else if (qual < 0) {
            return (char) (33);
        }
        return (char) (qual + 33);
    }

    /**
     * Replaces the character at the specific position.
     *
     * @param s the string to modify.
     * @param pos the zero-based position.
     * @param c the character to insert.
     * @return a new string with the given character replaced.
     */
    public static String replaceCharAt(String s, int pos, char c) {
        return s.substring(0, pos) + c + s.substring(pos + 1);
    }

    /**
     * This retains the case of the base.
     *
     * @param base the base to compliment.
     * @return the compliment of the given base.
     */
    public static char getCompliment(char base) {
        switch (base) {
            case 'A':
                return 'T';
            case 'a':
                return 't';
            case 'C':
                return 'G';
            case 'c':
                return 'g';
            case 'G':
                return 'C';
            case 'g':
                return 'c';
            case 'T':
                return 'A';
            case 't':
                return 'a';
            default:
                return base;
        }
    }

    /**
     * @param base the base in integer format.
     * @return the base in upper-case character format.
     */
    public static char ntIntToChar(byte base) {
        switch (base) {
            case 0:
                return 'A';
            case 1:
                return 'C';
            case 2:
                return 'G';
            case 3:
                return 'T';
            default:
                return 'N';
        }
    }

    /**
     * Converts a byte array of IUPAC bases in ASCII format DNA bases in ASCII
     * form.
     *
     * @param bases the byte array of bases in ASCII format.
     */
    public static void ntToInt(byte bases[]) {
        int i;
        for (i = 0; i < bases.length; i++) {
            bases[i] = NT2INT[(int) bases[i]];
        }
    }

    public static int baseCharToInt(char base) {
        return NT2INT[(int) base];
    }

    /**
     * Converts a byte array of DNA bases in ASCII format to integer format.
     *
     * @param bases the byte array of bases in ASCII format.
     */
    public static void iupacToNT(byte bases[]) {
        int i;
        for (i = 0; i < bases.length; i++) {
            bases[i] = IUPAC2NT[(int) bases[i]];
        }
    }

    /**
     * Reverses the given byte array.
     *
     * @param array the array to reverse.
     */
    public static void reverse(byte array[]) {
        int i;
        for (i = 0; i < array.length >> 1; i++) {
            byte b = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = b;
        }
    }

    /**
     * Compliments each base in the array, but does not reverse, assuming the
     * bytes are in integer format.
     *
     * @param bases the base to compliment.
     */
    public static void compliment(byte bases[]) {
        int i;
        for (i = 0; i < bases.length; i++) {
            bases[i] = NTINT2COMP[bases[i]];
        }
    }

    /**
     * Reverse ompliments DNA base array, assuming the bytes are in integer
     * format.
     *
     * @param bases the base to compliment.
     */
    public static void reverseCompliment(byte bases[]) {
        int i;
        for (i = 0; i < bases.length >> 1; i++) {
            byte b = NTINT2COMP[bases[i]];
            bases[i] = NTINT2COMP[bases[bases.length - i - 1]];
            bases[bases.length - i - 1] = b;
        }
        if (1 == (bases.length % 2)) {
            bases[i] = NTINT2COMP[bases[i]];
        }
    }

    /**
     * @param bases the DNA base string.
     * @return a byte array of the DNA bases in integer format.
     */
    public static byte[] basesToInt(String bases) {
        byte[] b = null;
        int i;

        b = new byte[bases.length()];

        for (i = 0; i < b.length; i++) {
            b[i] = NT2INT[(int) bases.charAt(i)];
        }

        return b;
    }

    /**
     * @param logA the first value in log10 space.
     * @param logB the second value in log10 space.
     * @return the two values summed in log10 space.
     */
    public static double addLog10(double logA, double logB) {
        double logT;
        if (logA < MIN_INF || Double.NaN == logA) {
            logA = MIN_INF;
        }
        if (logB < MIN_INF || Double.NaN == logB) {
            logB = MIN_INF;
        }
        if (logA < logB) {
            logT = logA;
            logA = logB;
            logB = logT; // swap
        }
        if (logB <= MIN_INF) {
            return logA;
        }
        return logA + Math.log10(1.0 + Math.pow(10.0, (logB - logA)));
    }

    /**
     * @param flowSignal the 100 scaled flow signal, to be used to convert a reference sequence into a theoretical flow signals
     * @return the base call represented by this flow signal
     * 
     */
    public static int getBaseCallFromFlowSignal(double flowSignal) {        
        return (int) ((flowSignal + 50.0) / 100.0);
    }

    public static int getFlowSignalFromBaseCall(int baseCall) {
        return (baseCall * 100);
    }
}
