/*
 * PSFunction.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represents a PostScript function dictionary
 *
 * @author Paul Wagenaars
 */
public class PSFunction {
    double[] domain;
    double[] range;
    
    // number of input values
    int m;
    
    // number of output values
    int n;
    
    /**
     * Create a new function of the type specified in the FuntionType field.
     * @param dict PostScript dictionary with function description
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck A required fields not found and/or a dictionary value is out of
     * range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Encountered a feature that is not (yet) implemented
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck A dictionary field has an invalid type
     * @return New PostScript function
     */
    public static PSFunction newFunction(PSObjectDict dict) throws PSErrorUndefined,
            PSErrorRangeCheck, PSErrorUnimplemented, PSErrorTypeCheck, PSErrorInvalidAccess {
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
     * Load entries common to all PostScript functions
     * @param dict PostScript dictionary describing the function
     */
    void loadCommonEntries(PSObjectDict dict) throws PSErrorUndefined, 
            PSErrorTypeCheck, PSErrorInvalidAccess {
        // Load domain field
        domain = dict.get("Domain").toArray().toDoubleArray();
        m = (int)Math.floor(domain.length/2);
        
        // Load range field
        PSObject obj;
        try {
            obj = dict.get("Range");
            range = obj.toArray().toDoubleArray();
            n = (int)Math.floor(range.length/2);
        } catch (PSErrorUndefined e) {
            range = new double[0];
            n = -1;
        }
    }
    
    /**
     * Evaluate this function for a set of input values
     */
    public double[] evaluate(double[] input) throws PSErrorRangeCheck, 
            PSErrorUnimplemented {
        throw new PSErrorUnimplemented("Evaluating functions of this type");        
    }
    
    /**
     * Preprocessing common to the evaluate methods of all function types
     * @param input Input values
     * @return New array with input values (clipped to the domain)
     */
    double[] evaluatePreProcess(double[] input) throws PSErrorRangeCheck {
        // Check whether the number of input values is correct
        if (input.length != m) {
            throw new PSErrorRangeCheck();
        }
        
        // Clip input values to domain
        double[] newInput = new double[m];
        for (int i = 0 ; i < m ; i++) {
            if (input[i] < domain[2*i]) {
                newInput[i] = domain[i];
            } else if (input[i] > domain[2*i+1]) {
                newInput[i] = domain[2*i+1];
            } else {
                newInput[i] = input[i];
            }
        }
        
        return newInput;
    }
    
    /**
     * Postprocessing common to the evaluate methods of all function types
     * @param output Unclipped output values of the function
     * @return Same array as input parameter
     */
    double[] evaluatePostProcess(double[] output) {
        if (range.length > 0) {
            for (int i = 0 ; i < n ; i++) {
                if (output[i] < range[2*i]) {
                    output[i] = range[2*i];
                } else if (output[i] > range[2*i+1]) {
                    output[i] = range[2*i+1];
                }
            }
        }
        
        return output;
    }
    
}
