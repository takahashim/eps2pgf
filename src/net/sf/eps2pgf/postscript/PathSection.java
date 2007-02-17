/*
 * PathSection.java
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
 * Represent a part of a PostScript path (i.e. lineto, curveto, moveto, ...)
 * @author Paul Wagenaars
 */
public class PathSection implements Cloneable {
    public double[] params = new double[6];
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public PathSection clone() {
        PathSection newSection = new PathSection();
        newSection.params = params.clone();
        return newSection;
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = params[0];
        coor[1] = params[1];
        return coor;
    }
}
