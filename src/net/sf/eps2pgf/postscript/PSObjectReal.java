/*
 * PSObjectReal.java
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

/** PostScript object: real
 *
 * @author Paul Wagenaars
 */
public class PSObjectReal extends PSObject {
    double value;
    
    /**
     * Creates a new instance of PSObjectReal
     * @param str String with valid real.
     */
    public PSObjectReal(String str) {
        value = Double.parseDouble(str);
    }
    
    /** Create a new real object. */
    public PSObjectReal(double dbl) {
        value = dbl;
    }
    
    /**
     * Check whether a string is a real
     * @param str String to check.
     * @return Returns true when str is a valid real. Returns false otherwise.
     */
    public static boolean isType(String str) {
        try {
            Double.parseDouble(str);            
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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
    public PSObjectReal abs() {
        return new PSObjectReal(Math.abs(value));
    }

    /**
     * Return this value rounded upwards
     * @return Value of this object rounded upwards
     */
    public PSObject ceiling() {
        return new PSObjectReal(Math.ceil(value));
    }

    /**
     * Return this value rounded downwards
     * @return Value of this object rounded downwards
     */
    public PSObject floor() {
        return new PSObjectReal(Math.floor(value));
    }

    /**
     * Returns the negative value of this double
     * @return Absolute value of this double
     */
    public PSObjectReal neg() {
        return new PSObjectReal(-value);
    }

    /**
     * Return this value rounded to the nearest integer
     * @return Value of this object rounded to the nearest integer
     */
    public PSObject round() {
        return new PSObjectReal(Math.round(value));
    }

    /**
     * Convert this object to a human readable string.
     * @return Human readable string.
     */
    public String toString() {
        return "Real: " + value;
    }
    
    /** Convert this object to the nearest integer. */
    public int toInt() {
        return (int)Math.round(value);
    }

    /** Convert this object to a real number, if possible. */
    public double toReal() {
        return value;
    }
    
    /**
     * Return this value rounded towards zero
     * @return Value of this object rounded towards zero
     */
    public PSObject truncate() {
        if (value > 0) {
            return floor();
        } else {
            return ceiling();
        }
    }

    /** Creates and returns a copy of this object. */
    public PSObjectReal clone() {
        return new PSObjectReal(value);
    }
}
