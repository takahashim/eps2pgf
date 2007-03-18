/*
 * Curveto.java
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

/**
 * cubic Bezier curve path section
 *
 * @author Paul Wagenaars
 */
public class Curveto extends PathSection implements Cloneable {
    
    /** Creates a new instance of Curveto */
    public Curveto() {
        for (int i = 0 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }
    
    /**
     * Creates a new instance of Curveto
     * @param controlCoor1 First Bezier control point
     * @param controlCoor2 Second Bezier control point
     * @param endCoor Endpoint
     */
    public Curveto(double[] controlCoor1, double[] controlCoor2, 
            double[] endCoor) {
        params[0] = controlCoor1[0];
        params[1] = controlCoor1[1];
        params[2] = controlCoor2[0];
        params[3] = controlCoor2[1];
        params[4] = endCoor[0];
        params[5] = endCoor[1];
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = params[4];
        coor[1] = params[5];
        return coor;
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Curveto clone() {
        Curveto newSection = new Curveto();
        newSection.params = params.clone();
        return newSection;
    }
    
}
