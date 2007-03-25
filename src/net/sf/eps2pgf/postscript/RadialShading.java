/*
 * RadialShading.java
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
 * Represent radial (type 3) shading dictionary
 * @author Paul Wagenaars
 */
public class RadialShading extends Shading {
    // Coordinate and radius of starting circle
    double x0, y0, r0;
    // Coordinate and radius of ending circle
    double x1, y1, r1;
    
    // Limiting values of parametric variable t
    double t0, t1;
    
    // Extend shading beyond the starting and ending circles
    boolean extendStart, extendEnd;
    
    PSFunction function;
    
    /**
     * Creates a new instance of RadialShading
     */
    public RadialShading(PSObjectDict dict) throws PSErrorTypeCheck, 
            PSErrorRangeCheck, PSErrorUnimplemented {
        // First, load the entries common to all shading types
        loadCommonEntries(dict);
        
        // Load all type specific entries
        PSObjectArray coords = dict.get("Coords").toArray();
        x0 = coords.get(0).toReal();
        y0 = coords.get(1).toReal();
        r0 = coords.get(2).toReal();
        x1 = coords.get(3).toReal();
        y1 = coords.get(4).toReal();
        r1 = coords.get(5).toReal();
        
        PSObjectArray domain = dict.lookup("Domain").toArray();
        if (domain == null) {
            t0 = 0;
            t1 = 1;
        } else {
            t0 = domain.get(0).toReal();
            t1 = domain.get(1).toReal();
        }
        
        PSObjectArray extend = dict.lookup("Extend").toArray();
        if (extend == null) {
            extendStart = false;
            extendEnd = false;
        } else {
            extendStart = extend.get(0).toBool();
            extendEnd = extend.get(1).toBool();
        }
        
        PSObject functionObj = dict.get("Function");
        if (functionObj instanceof PSObjectDict) {
            function = PSFunction.newFunction(functionObj.toDict());
        } else if (functionObj instanceof PSObjectArray) {
            throw new PSErrorUnimplemented("Defining a function with an array");
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * Get the X- and Y-coordinate corresponding to a certain value of s
     * @param s Parametric variable ranging from 0.0 to 1.0, where 0.0 corresponds
     *          with the starting circle and 1.0 with the ending circle.
     * @return Array of two values: X-coordinate and Y-coordinate
     */
    public double[] getCoord(double s) {
        double[] out = new double[2];
        out[0] = x0 + s*(x1-x0);
        out[1] = y0 + s*(y1-y0);
        return out;
    }
    
    /**
     * Get the radius corresponding to a certain value of s
     * @param s Parametric variable ranging from 0.0 to 1.0, where 0.0 corresponds
     *          with the starting circle and 1.0 with the ending circle.
     * @return Radius for the requested s
     */
    public double getRadius(double s) {
        return r0 + s*(r1-r0);
    }
    
    /**
     * Gets the color corresponding to a certain value of s
     * @param s Parametric variable ranging from 0.0 to 1.0, where 0.0 corresponds
     *          with the starting circle and 1.0 with the ending circle.
     * @return Array with values of color components. Check the ColorSpace for which
     *         value in the array is which component.
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
}
