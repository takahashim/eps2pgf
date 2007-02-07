/*
 * Lineto.java
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

/** Represents a path section formed by the lineto operator
 *
 * @author Paul Wagenaars
 */
public class Lineto extends PathSection {
    
    /**
     * Create a new Lineto instance
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public Lineto(double x, double y) {
        params[0] = x;
        params[1] = y;
        for (int i = 2 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }
    
    /**
     * Create a string representation of this object.
     */
    public String toString() {
        return String.format("lineto (%.4g, %.4g)", params[0], params[1]);
    }
}
