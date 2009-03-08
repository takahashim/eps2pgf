/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

package net.sf.eps2pgf.ps.resources.functions;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

/**
 * Represent a PostScript function of type 2 (exponential interpolation
 * function).
 *
 * @author Paul Wagenaars
 */
public class ExponentialFunction extends PSFunction {
    /** Interpolation exponent. */
    private double capN;
    
    /** C0 parameter. */
    private double[] c0;
    
    /** C1 parameter. */
    private double[] c1;
    
    /**
     * Creates a new instance of ExponentialFunction.
     * 
     * @param dict The funcion dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public ExponentialFunction(final PSObjectDict dict) throws PSError {
        // First, load all entries common to all functions
        loadCommonEntries(dict);
        
        // Next, load all type specific entries
        capN = dict.get("N").toReal();
        
        try {
            if (getNrOutputValues() >= 0) {
                c0 = dict.get("C0").toArray()
                        .toDoubleArray(getNrOutputValues());
            } else {
                c0 = dict.get("C0").toArray().toDoubleArray();
                setNrOutputValues(c0.length);
            }
        } catch (PSErrorRangeCheck e) {
            c0 = new double[1];
            c0[0] = 0.0;
            setNrOutputValues(1);
        }
        
        try {
            c1 = dict.get("C1").toArray().toDoubleArray(getNrOutputValues());
        } catch (PSErrorRangeCheck e) {
            c1 = new double[1];
            c1[0] = 1.0;
        }
        
    }
    
    /**
     * Evaluate this function for a set of input values.
     * 
     * @param pInput Input values.
     * 
     * @return Output values.
     * 
     * @throws PSError A PostScript error occurred.
     */
    @Override
    public double[] evaluate(final double[] pInput) throws PSError {
        double[] input = evaluatePreProcess(pInput);
        
        double x = input[0];
        double[] y = new double[getNrOutputValues()];
        for (int i = 0; i < getNrOutputValues(); i++) {
            y[i] = c0[i] + Math.pow(x, capN) * (c1[i] - c0[i]);
        }
        
        return evaluatePostProcess(y);
    }
}
