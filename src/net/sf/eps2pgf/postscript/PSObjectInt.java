/*
 * PSObjectInt.java
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

/** PostScript object: integer
 *
 * @author Paul Wagenaars
 */
public class PSObjectInt extends PSObject {
    int value;
    
    /**
     * Creates a new instance of PSObjectReal
     * @param str String with valid real.
     */
    public PSObjectInt(int i) {
        value = i;
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return String.valueOf(value);
    }

    /**
     * Returns the absolute value of this integer
     * @return Absolute value of this object
     */
    public PSObject abs() {
        if (value == Integer.MIN_VALUE) {
            double dbl = (double)value;
            return new PSObjectReal(Math.abs(dbl));
        } else {
            return new PSObjectInt(Math.abs(value));
        }
    }
    
    /**
     * Returns the negative value of this integer
     * @return Absolute value of this integer
     */
    public PSObject neg() {
        if (value == Integer.MIN_VALUE) {
            double dbl = (double)value;
            return new PSObjectReal(-dbl);
        } else {
            return new PSObjectInt(-value);
        }
    }
    
    /**
     * Convert this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return "Int: " + value;
    }
    
    /** Convert this object to integer, if possible. */
    public int toInt() {
        return value;
    }

    /** Convert this object to a real number, if possible. */
    public double toReal() {
        return (double)value;
    }
    
    /**
     * Creates an exact copy of this object.
     */
    public PSObjectInt clone() {
        return new PSObjectInt(value);
    }
}
