/*
 * Closepath.java
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

import com.sun.org.apache.bcel.internal.verifier.statics.DOUBLE_Upper;

/**
 *
 * @author Paul Wagenaars
 */
public class Closepath extends PathSection implements Cloneable {
    /**
     * Create a new Closepath object
     * @param position Coordinate to where this closepath returns.
     */
    public Closepath(double[] position) {
        params[0] = position[0];
        params[1] = position[1];
        for( int i = 2 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Closepath clone() {
        Closepath newSection = new Closepath(params);
        newSection.params = params.clone();
        return newSection;
    }

}
