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

import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represent radial (type 3) shading dictionary
 * @author Paul Wagenaars
 */
public class RadialShading extends Shading {
    /**
     * X-coordinate of first point (see PostScript doc for more info)
     */
    public double x0;
    
    /**
     * Y-coordinate of first point (see PostScript doc for more info)
     */
    public double y0;

    /**
     * First radius (see PostScript doc for more info)
     */
    public double r0;

    /**
     * X-coordinate of second point (see PostScript doc for more info)
     */
    public double x1;

    /**
     * Y-coordinate of second point (see PostScript doc for more info)
     */
    public double y1;

    /**
     * Second radius (see PostScript doc for more info)
     */
    public double r1;
    
    /**
     * Extend shading beyond first point? (see PostScript doc for more info)
     */
    public boolean extend0;
    
    /**
     * Extend shading beyong second point? (see PostScript doc for more info)
     */
    public boolean extend1;
    
    /**
     * Creates a new instance of RadialShading
     * @param dict PostScript dictionary describing the shading
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or more entries has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck One or more entries are out of range
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented A required feature is not implemented
     */
    public RadialShading(PSObjectDict dict) throws PSErrorTypeCheck, 
            PSErrorUndefined, PSErrorRangeCheck, PSErrorUnimplemented {
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
        
        PSObjectArray extend = dict.lookup("Extend").toArray();
        if (extend == null) {
            extend0 = false;
            extend1 = false;
        } else {
            extend0 = extend.get(0).toBool();
            extend1 = extend.get(1).toBool();
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
     * Find the s value for a given radius (inverse of getRadius)
     * @param radius Radius
     * @return S-value corresponding to radius
     */
    public double getSForRadius(double radius) {
        if (r1 != r0) {
            return (radius - r0)/(r1-r0);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    /**
     * Find the s value for given x-value
     * @param x X-coordinate
     * @return S-value corresponding to x-coordinate
     */
    public double getSForXValue(double x) {
        if (x0 != x1) {
            return (x - x0)/(x1-x0);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Find the s value for given y-value
     * @param y Y-coordinate
     * @return S-value corresponding to y-coordinate
     */
    public double getSForYValue(double y) {
        if (y0 != y1) {
            return (y - y0)/(y1-y0);
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    
    /**
     * Find the smallest (absolute) s value (within the given limit) for
     * which the x-value or y-value is -<value> or <value> or the radius
     * is zero or <value>.
     * @param value Distance
     * @param minS Minimal s-value
     * @param maxS Maximum s-value
     * @return Smallest s-value corresponding to distance
     */
    public double getSForDistance(double value, double minS, double maxS) {
        double ret = Double.POSITIVE_INFINITY;
        double s;
        
        // Check radius
        s = getSForRadius(0);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        s = getSForRadius(value);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        
        // Check X-value
        s = getSForXValue(-value);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        s = getSForXValue(value);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        
        // Check Y-value
        s = getSForYValue(-value);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        s = getSForYValue(value);
        if ((s >= minS) && (s <= maxS) && (Math.abs(s) < Math.abs(ret))) {
            ret = s;
        }
        
        return ret;
    }
}
