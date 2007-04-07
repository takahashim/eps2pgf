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
     * Creates a new instance of PSObjectInt
     * @param i Value of new integer object.
     */
    public PSObjectInt(int i) {
        value = i;
    }
    
    /**
     * Creates a new instance of PSObjectInt
     * @param str String representing an integer
     */
    public PSObjectInt(String str) {
        value = Integer.parseInt(str);
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
     * Return this value rounded upwards
     * @return New object with same integer
     */
    public PSObjectInt ceiling() {
        return new PSObjectInt(value);
    }

    /**
     * Return this value rounded downwards
     * @return New object with same integer
     */
    public PSObjectInt floor() {
        return new PSObjectInt(value);
    }
    
    /**
     * Check whether a string is a integer
     * @param str String to check.
     * @return Returns true when str is a valid integer. Returns false otherwise.
     */
    public static boolean isType(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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
     * Return this value rounded to the nearest integer
     * @return New object with same integer
     */
    public PSObjectInt round() {
        return new PSObjectInt(value);
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
     * Return this value rounded towards zero
     * @return New object with same integer
     */
    public PSObjectInt truncate() {
        return new PSObjectInt(value);
    }

    /**
     * Creates an exact copy of this object.
     */
    public PSObjectInt clone() {
        return new PSObjectInt(value);
    }
}
