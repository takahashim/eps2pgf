/*
 * Shading.java
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

import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/**
 * Base class for shading 
 *
 * @author Paul Wagenaars
 */
public class Shading {
    String ColorSpace;
    
    PSFunction function;
    
    /**
     * Lower limit of parametric variable t (see PostScript doc for more info)
     */
    public double t0;
    
    /**
     * Upper limit of parametric variable t (see PostScript doc for more info)
     */
    public double t1;
    
    
    /**
     * Create a new shading of the type defined in the supplied shading dictionary.
     * @param dict PostScript shading dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck One or more required fields were not found in the dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One of the fields has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A feature is not (yet) implemented
     * @return New shading object
     */
    public static Shading newShading(PSObjectDict dict) throws PSError {
        PSObject shadingTypeObj = dict.lookup("ShadingType");
        if (shadingTypeObj == null) {
            throw new PSErrorRangeCheck();
        }

        Shading newShading;
        switch (shadingTypeObj.toInt()) {
            case 3:
                newShading = new RadialShading(dict);
                break;
            default:
                throw new PSErrorUnimplemented("Shading type " + shadingTypeObj.toInt());
        }
        
        return newShading;
    }
    
    /**
     * Load the entries common to all types of shading dictionaries
     */
    void loadCommonEntries(PSObjectDict dict) throws PSError {
        PSObject colSpaceObj = dict.get("ColorSpace");
        if (colSpaceObj instanceof PSObjectName) {
            ColorSpace = ((PSObjectName)colSpaceObj).name;
        } else if (colSpaceObj instanceof PSObjectArray) {
            throw new PSErrorUnimplemented("Defining ColorSpace with an array");
        } else {
            throw new PSErrorTypeCheck();
        }
        
        PSObject functionObj = dict.get("Function");
        if (functionObj instanceof PSObjectDict) {
            function = PSFunction.newFunction(functionObj.toDict());
        } else if (functionObj instanceof PSObjectArray) {
            throw new PSErrorUnimplemented("Defining a function with an array");
        } else {
            throw new PSErrorTypeCheck();
        }

        PSObjectArray domain = dict.lookup("Domain").toArray();
        if (domain == null) {
            t0 = 0;
            t1 = 1;
        } else {
            t0 = domain.get(0).toReal();
            t1 = domain.get(1).toReal();
        }
        
    }
    
    /**
     * Gets the color corresponding to a certain value of s
     * @param s Parametric variable ranging from 0.0 to 1.0, where 0.0 corresponds
     *          with the starting circle and 1.0 with the ending circle.
     * @return Array with values of color components. Check the ColorSpace for which
     *         value in the array is which component.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck S-value out of range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A required feature is not (yet) implemented
     */
    public double[] getColor(double s) throws PSErrorRangeCheck, 
            PSErrorUnimplemented {
        // First, we must convert s to t
        double t = s*(t1-t0) + t0;
        double[] in = {t};
        double[] color = function.evaluate(in);
        for (int i = 0 ; i < color.length ; i++) {
            if (color[i] < 0) {
                color[i] = 0.0;
            } else if (color[i] > 1) {
                color[i] = 1.0;
            }
        }
        return color;
    }
    
    /**
     * Fit linear segments on color
     * @return S-values that create a good fit with linear segments
     * @param maxError Maximum vertical distance between original and
     *                 fitted colors.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A required feature is not yet implemented
     */
    public double[] fitLinearSegmentsOnColor(double maxError) 
            throws PSErrorUnimplemented {
        int N = 101;
        double[] s = new double[N];
        for (int i = 0 ; i < N ; i++) {
            s[i] = ((double)i)/(((double)N)-1.0);
        }
        // First get the colors for N steps
        List<double[]> colors = new ArrayList<double[]>(N);
        try {
            for (int i = 0 ; i < N ; i++) {
                colors.add(getColor(s[i]));
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        
        List<Double> fits = new ArrayList<Double>();
        int lastIndex = 0;
        fits.add(s[0]);
        for (int i = 2 ; i < N ; i++) {
            double currentError = getMaxError(s, colors, lastIndex, i);
            if (currentError > maxError) {
                lastIndex = i-1;
                fits.add(s[i-1]);
            }
        }
        fits.add(s[N-1]);
        
        double[] fit = new double[fits.size()];
        for (int i = 0 ; i < fits.size() ; i++) {
            fit[i] = fits.get(i);
        }
        return fit;
    }

    /**
     * Determine the maximum (vertical) distance between the actual points
     * and a straight line between the beginning and end point.
     * @param s Array with s-values
     * @param colors Colors associated with s-values
     * @param start First index of line section
     * @param end Last index of line section
     * @return Maximum vertical distance between given colors and straight line
     * between start end end index
     */
    double getMaxError(double[] s, List<double[]> colors, int start, int end) {
        double maxSoFar = 0.0;
        double[] startColor = colors.get(start);
        double[] endColor = colors.get(end);
        for (int i = (start+1) ; i < end ; i++) {
            double[] currentColor = colors.get(i);
            
            // Loop through all color component and check the distance for
            // each color.
            for (int j = 0 ; j < startColor.length ; j++) {
                double frac = (s[i] - s[start]) / (s[end] - s[start]);
                double linCol = startColor[j] + (frac*(endColor[j]-startColor[j]));
                double error = Math.abs(linCol-currentColor[j]);
                maxSoFar = Math.max(maxSoFar, error);
            }
        }
        return maxSoFar;
    }
    
}
