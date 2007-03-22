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
    
    /**
     * Creates a new instance of RadialShading
     */
    public RadialShading(PSObjectDict dict) throws PSErrorTypeCheck, PSErrorRangeCheck {
        PSObjectArray coords = dict.lookup("Coords").toArray();
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
        
        PSObject functionObj = dict.lookup("Function");
    }
    
    
    
    
}
