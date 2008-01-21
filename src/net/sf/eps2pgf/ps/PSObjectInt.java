/*
 * PSObjectInt.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.ps;

import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;

/**
 * PostScript object: integer.
 *
 * @author Paul Wagenaars
 */
public class PSObjectInt extends PSObject {
    
    /** Value of this integer object. */
    private int value;
    
    /**
     * Creates a new instance of PSObjectInt.
     * @param i Value of new integer object.
     */
    public PSObjectInt(final int i) {
        this.value = i;
    }
    
    /**
     * Creates a new instance of PSObjectInt.
     * 
     * @param r New integer value will be the integer nearest to this value
     *          (i.e. round(r)).
     */
    public PSObjectInt(final double r) {
        this.value = (int) Math.round(r);
    }
    
    /**
     * Creates a new instance of PSObjectInt.
     * 
     * @param str String representing an integer
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public PSObjectInt(final String str) throws PSErrorTypeCheck {
        try {
            int i = str.indexOf("#");
            if (i < 0) {
                this.value = Integer.parseInt(str);
            } else {
                int base = Integer.parseInt(str.substring(0, i));
                String strNumber = str.substring(i + 1);
                this.value = Integer.parseInt(strNumber, base);
            }
        } catch (NumberFormatException e) {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * Create a new instance of PSObjectInt that is the exact copy of another
     * PSObjectInt.
     * 
     * @param obj Create an exact copy of this object.
     */
    public PSObjectInt(final PSObjectInt obj) {
        this.value = obj.value;
        copyCommonAttributes(obj);
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return String representation of this object.
     */
    @Override
    public String isis() {
        return String.valueOf(this.value);
    }

    /**
     * Returns the absolute value of this integer.
     * @return Absolute value of this object
     */
    @Override
    public PSObject abs() {
        if (this.value == Integer.MIN_VALUE) {
            double dbl = (double) this.value;
            return new PSObjectReal(Math.abs(dbl));
        } else {
            return new PSObjectInt(Math.abs(this.value));
        }
    }
    
    /**
     * Returns the sum of this object and the passed object, if both are
     * numeric.
     * 
     * @param obj Object that will be added to this object
     * 
     * @return Sum of this object and passed object
     * 
     * @throws PSErrorTypeCheck Object is not numeric
     */
    @Override
    public PSObject add(final PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
            return obj.add(this);
        } else {
            int num2 = obj.toInt();
            // Do some simplistic overflow detection
            if ((this.value >= Integer.MAX_VALUE / 2)
                    || (num2 >= Integer.MAX_VALUE / 2)
                    || (this.value <= Integer.MIN_VALUE / 2)
                    || (num2 <= Integer.MIN_VALUE / 2)) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl + num2dbl);
            } else {
                return new PSObjectInt(this.value + num2);
            }
        }
    }

    /**
     * PostScript operator 'and'.
     * @param obj2 Object to 'and' with this object.
     * @throws PSErrorTypeCheck Obj2 is not an integer object.
     * @return Bitwise and between this object and obj2.
     */
    @Override
    public PSObjectInt and(final PSObject obj2) throws PSErrorTypeCheck {
        int obj2Int = obj2.toInt();
        return new PSObjectInt(this.value & obj2Int);
    }
    
    /**
     * Return this value rounded upwards.
     * @return New object with same integer
     */
    @Override
    public PSObjectInt ceiling() {
        return new PSObjectInt(this.value);
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectInt clone() {
        PSObjectInt copy = (PSObjectInt) super.clone();
        return copy;
    }

    /**
     * PostScript operator 'cvi'. Convert this object to an integer
     * 
     * @return Integer representation of this object.
     */
    @Override
    public int cvi() {
        return this.value;
    }

    /**
     * PostScript operator 'cvr'. Convert this object to a real
     * 
     * @return Floating point representation of this object.
     */
    @Override
    public double cvr() {
        return toReal();
    }
    
    /**
     * PostScript operator 'cvrs'.
     * 
     * @param radix The radix.
     * 
     * @return String representation with radix
     * 
     * @throws PSErrorTypeCheck PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck PostScript rangecheck error occurred.
     */
    @Override
    public String cvrs(final int radix) throws PSErrorTypeCheck,
            PSErrorRangeCheck {
        if (radix < 2) {
            throw new PSErrorRangeCheck();
        }
        if ((this.value >= 0) || (radix == 10)) {
            return Integer.toString(this.value, radix).toUpperCase();
        } else {
            double negVal = Math.pow(2, 32) + this.value;
            return Long.toString((long) negVal, radix).toUpperCase();
        }
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        return String.valueOf(this.value);
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Copy of this object.
     */
    @Override
    public PSObjectInt dup() {
        return new PSObjectInt(this);
    }
    
    /**
     * Compare this object with another object and return true if they are
     * equal. See PostScript manual on what's equal and what's not.
     * 
     * @param obj Object to compare this object with
     * 
     * @return True if objects are equal, false otherwise
     */
    @Override
    public boolean eq(final PSObject obj) {
        try {
            if (obj instanceof PSObjectInt) {
                return (this.value == obj.toInt());
            } else if (obj instanceof PSObjectReal) {
                return (toReal() == obj.toReal());
            }
        } catch (PSErrorTypeCheck e) {
            // This can never happen because of the typecheck with instanceof
        }
        return false;
    }

    /**
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     * 
     * @param obj The object to compare to.
     * 
     * @return True, if equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PSObject) {
            return eq((PSObject) obj);
        } else {
            return false;
        }
    }
    
    /**
     * Return this value rounded downwards.
     * 
     * @return New object with same integer
     */
    @Override
    public PSObjectInt floor() {
        return new PSObjectInt(this.value);
    }
    
    /**
     * PostScript operator 'gt'.
     * @param obj2 Object to compare this object to
     * @throws PSErrorTypeCheck Unable to compare the type of this object and/or
     *                          obj2.
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     */
    @Override
    public boolean gt(final PSObject obj2) throws PSErrorTypeCheck {
        return (this.toReal() > obj2.toReal());
    }
    
    /**
     * Returns a hashCode value for this object. This method is supported
     * for the benefit hashtables, such as used in PSObjectDict.
     * 
     * @return Hash for this object.
     */
    @Override
    public int hashCode() {
        return this.value * 1000000;
    }
    
    /**
     * Check whether a string is a integer.
     * 
     * @param str String to check.
     * 
     * @return Returns true when str is a valid integer. Returns false
     *         otherwise.
     */
    public static boolean isType(final String str) {
        try {
            new PSObjectInt(str);
        } catch (PSErrorTypeCheck e) {
            return false;
        }
        return true;
    }
    
    /**
     * Multiply this object with another object.
     * @param obj Multiplication of this object and passed object
     * @return Multiplication object
     * @throws PSErrorTypeCheck Object(s) are not numeric
     */
    @Override
    public PSObject mul(final PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
            double num2 = obj.toReal();
            double valuedbl = this.toReal();
            return new PSObjectReal(valuedbl * num2);
        } else {
            int num2 = obj.toInt();
            
            // Simple (non-perfect) overflow check
            int maxNum = Math.max(Math.abs(this.value), Math.abs(num2));
            int minNum = Math.min(Math.abs(this.value), Math.abs(num2));
            if ((minNum != 0) && (maxNum >= (Integer.MAX_VALUE / minNum))) {
                double num2dbl = obj.toReal();
                double valuedbl = this.toReal();
                return new PSObjectReal(valuedbl * num2dbl);
            } else {
                // We can safely do an integer multiplication
                return new PSObjectInt(this.value * num2);
            }
        }
    }

    /**
     * Returns the negative value of this integer.
     * 
     * @return Absolute value of this integer
     */
    @Override
    public PSObject neg() {
        if (this.value == Integer.MIN_VALUE) {
            double dbl = (double) this.value;
            return new PSObjectReal(-dbl);
        } else {
            return new PSObjectInt(-this.value);
        }
    }
    
    /**
     * PostScript operator: 'not'.
     * 
     * @return Logical negation of this object
     */
    @Override
    public PSObjectInt not() {
        return new PSObjectInt(~this.value);
    }
    
    /**
     * PostScript operator 'or'.
     * 
     * @param obj2 Object to 'or' with this object.
     * 
     * @return Return bitwise or between this object and obj2
     * 
     * @throws PSErrorTypeCheck Obj2 is not an integer object
     */
    @Override
    public PSObjectInt or(final PSObject obj2) throws PSErrorTypeCheck {
        int obj2Int = obj2.toInt();
        return new PSObjectInt(this.value | obj2Int);
    }
    
    /**
     * Return this value rounded to the nearest integer.
     * 
     * @return New object with same integer
     */
    @Override
    public PSObjectInt round() {
        return new PSObjectInt(this.value);
    }

    /**
     * Subtract an object from this object.
     * 
     * @param obj Object that will be subtracted from this object
     * 
     * @return Passed object subtracted from this object
     * 
     * @throws PSErrorTypeCheck Object is not numeric
     */
    @Override
    public PSObject sub(final PSObject obj) throws PSErrorTypeCheck {
        if (obj instanceof PSObjectReal) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl - num2dbl);
        } else {
            int num2 = obj.toInt();
            // Do some simplistic overflow detection
            if ((this.value >= Integer.MAX_VALUE / 2)
                    || (num2 >= Integer.MAX_VALUE / 2)
                    || (this.value <= Integer.MIN_VALUE / 2)
                    || (num2 <= Integer.MIN_VALUE / 2)) {
                double valuedbl = this.toReal();
                double num2dbl = obj.toReal();
                return new PSObjectReal(valuedbl - num2dbl);
            } else {
                return new PSObjectInt(this.value - num2);
            }
        }
    }

    /**
     * Convert this object to dictionary key, if possible.
     * @throws PSErrorTypeCheck Unable to convert this object type to a dict key
     * @return Dictionary key that represents this object.
     */
    public String toDictKey() throws PSErrorTypeCheck {
        return Integer.toString(this.value);
    }
    
    /**
     * Convert this object to a human readable string.
     * 
     * @return Human readable string.
     */
    @Override
    public String toString() {
        return "Int: " + this.value;
    }
    
    /**
     * Convert this object to integer, if possible.
     * 
     * @return Integer representation of this object.
     */
    @Override
    public int toInt() {
        return this.value;
    }

    /**
     * Convert this object to a real number, if possible.
     * 
     * @return Real/floating point representation of this object.
     */
    @Override
    public double toReal() {
        return (double) this.value;
    }
    
    /**
     * Return this value rounded towards zero.
     * 
     * @return New object with same integer
     */
    @Override
    public PSObjectInt truncate() {
        return new PSObjectInt(this.value);
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "integertype";
    }

    /**
     * PostScript operator 'xor'.
     * @param obj2 Object to 'xor' with this object
     * @throws PSErrorTypeCheck Obj2 is not an integer object
     * @return Return bitwise xor between this object and obj2
     */
    @Override
    public PSObjectInt xor(final PSObject obj2) throws PSErrorTypeCheck {
        int obj2Int = obj2.toInt();
        return new PSObjectInt(this.value ^ obj2Int);
    }
    
}
