/*
 * PSFunction.java
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

package net.sf.eps2pgf.ps;

import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;

/**
 * Represents a PostScript function dictionary.
 *
 * @author Paul Wagenaars
 */
public class PSFunction {
    
    /** Domain (see PostScript manual). */
    private double[] domain;
    
    /** Range (see PostScript manual). */
    private double[] range;
    
    /** number of input values. */
    private int nrInputValues;
    
    /** number of output values. */
    private int nrOutputValues;
    
    /**
     * Create a new function of the type specified in the FuntionType field.
     * 
     * @param dict PostScript dictionary with function description
     * 
     * @return New PostScript function
     * 
     * @throws PSError A PostScript error occurred.
     */
    public static PSFunction newFunction(final PSObjectDict dict)
            throws PSError {
        
        PSObject typeObj = dict.lookup("FunctionType");
        if (typeObj == null) {
            throw new PSErrorRangeCheck();
        }
        
        PSFunction newFunction;
        switch (typeObj.toInt()) {
            case 0:
                throw new PSErrorUnimplemented("FunctionType 0");
                //break;
            case 2:
                newFunction = new ExponentialFunction(dict);
                break;
            case 3:
                newFunction = new StitchingFunction(dict);
                break;
            default:
                throw new PSErrorRangeCheck();
        }
        
        return newFunction;
    }
    
    /**
     * Load entries common to all PostScript functions.
     * 
     * @param dict PostScript dictionary describing the function
     * 
     * @throws PSErrorUndefined The PostScript error undefined.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    void loadCommonEntries(final PSObjectDict dict)
            throws PSErrorUndefined, PSErrorTypeCheck {
        
        // Load domain field
        domain = dict.get("Domain").toArray().toDoubleArray();
        nrInputValues = (int) Math.floor(domain.length / 2.0);
        
        // Load range field
        PSObject obj;
        try {
            obj = dict.get("Range");
            range = obj.toArray().toDoubleArray();
            nrOutputValues = (int) Math.floor(range.length / 2.0);
        } catch (PSErrorUndefined e) {
            range = new double[0];
            nrOutputValues = -1;
        }
    }
    
    /**
     * Evaluate this function for a set of input values.
     * 
     * @param input The input.
     * 
     * @return The output values.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorUnimplemented Encountered a PostScript feature that is not
     * (yet) implemented.
     */
    public double[] evaluate(final double[] input) throws PSErrorRangeCheck, 
            PSErrorUnimplemented {
        
        throw new PSErrorUnimplemented("Evaluating functions of this type");
    }
    
    /**
     * Pre-processing common to the evaluate methods of all function types.
     * 
     * @param input Input values
     * 
     * @return New array with input values (clipped to the domain)
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    double[] evaluatePreProcess(final double[] input) throws PSErrorRangeCheck {
        // Check whether the number of input values is correct
        if (input.length != nrInputValues) {
            throw new PSErrorRangeCheck();
        }
        
        // Clip input values to domain
        double[] newInput = new double[nrInputValues];
        for (int i = 0; i < nrInputValues; i++) {
            if (input[i] < domain[2 * i]) {
                newInput[i] = domain[i];
            } else if (input[i] > domain[2 * i + 1]) {
                newInput[i] = domain[2 * i + 1];
            } else {
                newInput[i] = input[i];
            }
        }
        
        return newInput;
    }
    
    /**
     * Post-processing common to the evaluate methods of all function types.
     * 
     * @param output Unclipped output values of the function
     * 
     * @return Same array as input parameter
     */
    double[] evaluatePostProcess(final double[] output) {
        if (range.length > 0) {
            for (int i = 0; i < nrOutputValues; i++) {
                if (output[i] < range[2 * i]) {
                    output[i] = range[2 * i];
                } else if (output[i] > range[2 * i + 1]) {
                    output[i] = range[2 * i + 1];
                }
            }
        }
        
        return output;
    }

    /**
     * Sets the number of output values.
     * 
     * @param pNrOutputValues the nrOutputValues to set
     */
    void setNrOutputValues(final int pNrOutputValues) {
        nrOutputValues = pNrOutputValues;
    }

    /**
     * Get the number of output values.
     * 
     * @return the nrOutputValues
     */
    int getNrOutputValues() {
        return nrOutputValues;
    }
    
    /**
     * Get a single value from the domain[] array.
     * 
     * @param index The index of the value.
     * 
     * @return The requested value.
     */
    double getDomain(final int index) {
        return domain[index];
    }

}
