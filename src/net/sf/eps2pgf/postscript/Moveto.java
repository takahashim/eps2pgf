/*
 * Moveto.java
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
 * Path section representing a PostScript moveto operator.
 * @author Paul Wagenaars
 */
public class Moveto extends PathSection implements Cloneable {
    
    /**
     * Create a new Movoto instance
     */
    public Moveto() {
        for (int i = 0 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }        
    }
    
    /**
     * Create a new Moveto instance
     * @param x X-coordinate (in device coordinates, cm)
     * @param y Y-coordinate (in device coordinates, cm)
     * @param docx document X-coordinate before CTM (in pt)
     * @param docy document Y-coordinate before CTM (in pt)
     */
    public Moveto(double x, double y, double docx, double docy) {
        params[0] = x;
        params[1] = y;
        params[2] = docx;
        params[3] = docy;
        for (int i = 4 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }

    /**
     * Create a string representation of this object.
     */
    public String toString() {
        return String.format("moveto (%.4g, %.4g)", params[0], params[1]);
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Moveto clone() {
        Moveto newSection = new Moveto();
        newSection.params = params.clone();
        return newSection;
    }

}
