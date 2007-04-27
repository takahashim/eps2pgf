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

import net.sf.eps2pgf.postscript.errors.*;

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
     * Returns the sum of this object and the passed object, if both are numeric
     * @param obj Object that will be added to this object
     * @return Sum of this object and passed object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject add(PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
            return obj.add(this);
        } else {
            int num2 = obj.toInt();
            // Do some simplistic overflow detection
            if ( (value >= Integer.MAX_VALUE/2) || (num2 >= Integer.MAX_VALUE/2) ||
                    (value <= Integer.MIN_VALUE/2) || (num2 <= Integer.MIN_VALUE/2)) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl + num2dbl);
            } else {
                return new PSObjectInt(value + num2);
            }
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
     * PostScript operator 'cvi'. Convert this object to an integer
     */
    public int cvi() {
        return value;
    }

    /**
     * PostScript operator 'cvr'. Convert this object to a real
     */
    public double cvr() throws PSErrorTypeCheck {
        return toReal();
    }
    
    /**
     * PostScript operator 'cvrs'
     */
    public String cvrs(int radix) throws PSErrorTypeCheck, PSErrorRangeCheck {
        if (radix < 2) {
            throw new PSErrorRangeCheck();
        }
        if ( (value >= 0) || (radix == 10) ) {
            return Integer.toString(value, radix).toUpperCase();
        } else {
            double negVal = Math.pow(2, 32) + value;
            return Long.toString((long)negVal, radix).toUpperCase();
        }
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() {
        return String.valueOf(value);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        try {
            if (obj instanceof PSObjectInt) {
                return (value == obj.toInt());
            } else if (obj instanceof PSObjectReal) {
                return (toReal() == obj.toReal());
            }
        } catch (PSErrorTypeCheck e) {
            // This can never happen because of the typecheck with instanceof
        }
        return false;
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
     * Multiply this object with another object
     * @param obj Multiplication of this object and passed object
     * @return Multiplication object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject mul(PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
            double num2 = obj.toReal();
            double valuedbl = this.toReal();
            return new PSObjectReal(valuedbl * num2);
        } else {
            int num2 = obj.toInt();
            
            // Simple (non-perfect) overflow check
            int maxNum = Math.max(Math.abs(value), Math.abs(num2));
            int minNum = Math.min(Math.abs(value), Math.abs(num2));
            if (maxNum >= (Integer.MAX_VALUE/minNum)) {
                double num2dbl = obj.toReal();
                double valuedbl = this.toReal();
                return new PSObjectReal(valuedbl * num2dbl);
            } else {
                // We can safely do an integer multiplication
                return new PSObjectInt(value * num2);
            }
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
     * Return this value rounded to the nearest integer
     * @return New object with same integer
     */
    public PSObjectInt round() {
        return new PSObjectInt(value);
    }

    /**
     * Subtract an object from this object
     * @param obj Object that will be subtracted from this object
     * @return Passed object subtracted from this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject sub(PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl - num2dbl);
        } else {
            int num2 = obj.toInt();
            // Do some simplistic overflow detection
            if ( (value >= Integer.MAX_VALUE/2) || (num2 >= Integer.MAX_VALUE/2) ||
                    (value <= Integer.MIN_VALUE/2) || (num2 <= Integer.MIN_VALUE/2)) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl - num2dbl);
            } else {
                return new PSObjectInt(value - num2);
            }
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

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "integertype";
    }
}
