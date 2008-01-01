/*
 * ExponentialFunction.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

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
    public ExponentialFunction(PSObjectDict dict) throws PSError {
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
