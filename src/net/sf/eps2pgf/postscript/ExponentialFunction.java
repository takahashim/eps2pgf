/*
 * ExponentialFunction.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represent a PostScript function of type 2 (exponential interpolation function)
 *
 * @author Paul Wagenaars
 */
public class ExponentialFunction extends PSFunction {
    // Interpolation exponent
    double N;
    
    // C0
    double[] C0;
    
    // C1
    double[] C1;
    
    /** Creates a new instance of ExponentialFunction */
    public ExponentialFunction(PSObjectDict dict) throws PSErrorRangeCheck, 
            PSErrorTypeCheck, PSErrorUndefined {
        // First, load all entries common to all functions
        loadCommonEntries(dict);
        
        // Next, load all type specific entries
        N = dict.get("N").toReal();
        
        try {
            if (n >= 0) {
                C0 = dict.get("C0").toArray().toDoubleArray(n);
            } else {
                C0 = dict.get("C0").toArray().toDoubleArray();
                n = C0.length;
            }
        } catch (PSErrorRangeCheck e) {
            C0 = new double[1];
            C0[0] = 0.0;
            n = 1;
        }
        
        try {
            C1 = dict.get("C1").toArray().toDoubleArray(n);
        } catch (PSErrorRangeCheck e) {
            C1 = new double[1];
            C1[0] = 1.0;
        }
        
    }
    
    /**
     * Evaluate this function for a set of input values
     * @param input values
     * @return output values
     */
    public double[] evaluate(double[] input) throws PSErrorRangeCheck, 
            PSErrorUnimplemented {
        input = evaluatePreProcess(input);
        
        double x = input[0];
        double[] y = new double[n];
        for (int i = 0 ; i < n ; i++) {
            y[i] = C0[i] + Math.pow(x,N)*(C1[i]-C0[i]);
        }
        
        return evaluatePostProcess(y);
    }
}
