/*
 * PSObjectReal.java
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

package net.sf.eps2pgf.ps.objects;

import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;

/** PostScript object: real.
 *
 * @author Paul Wagenaars
 */
public class PSObjectReal extends PSObject {
    
    /** Value of this real object. */
    private double value;
    
    /**
     * Creates a new instance of PSObjectReal.
     * 
     * @param str String with valid real.
     */
    public PSObjectReal(final String str) {
        this.value = Double.parseDouble(str);
    }
    
    /**
     * Create a new real object.
     *
     * @param dbl Value of new real object.
     */
    public PSObjectReal(final double dbl) {
        this.value = dbl;
    }
    
    /**
     * Creates a new real object.
     * 
     * @param obj New object is exact copy of this object.
     */
    public PSObjectReal(final PSObjectReal obj) {
        this.value = obj.value;
        copyCommonAttributes(obj);
    }
    
    /**
     * Check whether a string is a real.
     * 
     * @param str String to check.
     * 
     * @return Returns true when str is a valid real. Returns false otherwise.
     */
    public static boolean isType(final String str) {
        try {
            Double.parseDouble(str);            
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return String representation of this object.
     */
    @Override
    public final String isis() {
        return String.valueOf(this.value);
    }

    /**
     * Returns the absolute value of this integer.
     * @return Absolute value of this object
     */
    @Override
    public final PSObjectReal abs() {
        return new PSObjectReal(Math.abs(this.value));
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
    public final PSObject add(final PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(this.value + num2);
    }

    /**
     * Return this value rounded upwards.
     * 
     * @return Value of this object rounded upwards
     */
    @Override
    public final PSObject ceiling() {
        return new PSObjectReal(Math.ceil(this.value));
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectReal clone() {
        PSObjectReal copy = (PSObjectReal) super.clone();
        return copy;
    }

    /**
     * PostScript operator 'cvi'. Convert this object to an integer.
     * 
     * @return Integer representation of this object.
     */
    @Override
    public final int cvi() {
        try {
            return (int) truncate().toReal();
        } catch (PSErrorTypeCheck e) {
            // this can never happen, because this object is a real
            return 0;
        }
    }
    
    /**
     * PostScript operator 'cvr'. Convert this object to a real.
     * 
     * @return Value of this object.
     */
    @Override
    public final double cvr() {
        return this.value;
    }
    
    /**
     * PostScript operator 'cvrs'.
     * 
     * @param radix Radix of integer
     * 
     * @return Integer string representation of this object with radix.
     * 
     * @throws PSErrorTypeCheck PostScript typecheck error
     * @throws PSErrorRangeCheck PostScript rangecheck error
     */
    @Override
    public final String cvrs(final int radix)
            throws PSErrorTypeCheck, PSErrorRangeCheck {
        if (radix < 2) {
            throw new PSErrorRangeCheck();
        }
        if (radix == 10) {
            return Double.toString(this.value);
        } else {
            PSObjectInt valueInt = new PSObjectInt(cvi());
            return valueInt.cvrs(radix);
        }
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public final String cvs() {
        return String.valueOf(this.value);
    }

    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Exact copy of this object.
     */
    @Override
    public final PSObjectReal dup() {
        return new PSObjectReal(this);
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
    public final boolean eq(final PSObject obj) {
        try {
            if ((obj instanceof PSObjectReal) || (obj instanceof PSObjectInt)) {
                return (this.value == obj.toReal());
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
     * @return Value of this object rounded downwards
     */
    @Override
    public final PSObject floor() {
        return new PSObjectReal(Math.floor(this.value));
    }

    /**
     * PostScript operator 'gt'.
     * 
     * @param obj2 Object to compare this object to.
     * 
     * @throws PSErrorTypeCheck Unable to compare the type of this object
     *                          and/or obj2.
     * 
     * @return Returns true when this object is greater than obj2, return false
     *         otherwise.
     */
    @Override
    public final boolean gt(final PSObject obj2) throws PSErrorTypeCheck {
        return (toReal() > obj2.toReal());
    }
    
    /**
     * Returns a hashCode value for this object. This method is supported
     * for the benefit hashtables, such as used in PSObjectDict.
     * 
     * @return Hash value of this object.
     */
    @Override
    public final int hashCode() {
        return (int) (this.value * 1000000);
    }
    
    /**
     * Multiply this object with another object.
     * 
     * @param obj Multiplication of this object and passed object
     * 
     * @return Multiplication object
     * 
     * @throws PSErrorTypeCheck Object(s) are not numeric
     */
    @Override
    public final PSObject mul(final PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(this.value * num2);
    }

    /**
     * Returns the negative value of this double.
     * 
     * @return Absolute value of this double
     */
    @Override
    public final PSObjectReal neg() {
        return new PSObjectReal(-this.value);
    }

    /**
     * Return this value rounded to the nearest integer.
     * 
     * @return Value of this object rounded to the nearest integer
     */
    @Override
    public final PSObject round() {
        return new PSObjectReal(Math.round(this.value));
    }

    /**
     * Subtract an object from this object.
     * @param obj Object that will be subtracted from this object
     * @return Passed object subtracted from this object
     * @throws PSErrorTypeCheck Object is not numeric
     */
    @Override
    public final PSObject sub(final PSObject obj) throws PSErrorTypeCheck {
        double num2 = obj.toReal();
        return new PSObjectReal(this.value - num2);
    }

    /**
     * Convert this object to a human readable string.
     * @return Human readable string.
     */
    @Override
    public final String toString() {
        return "Real: " + this.value;
    }
    
    /**
     * Convert this object to a real number, if possible.
     * 
     * @return Real/floating point representation of this object.
     */
    @Override
    public final double toReal() {
        return this.value;
    }
    
    /**
     * Return this value rounded towards zero.
     * 
     * @return Value of this object rounded towards zero
     */
    @Override
    public final PSObject truncate() {
        if (this.value > 0) {
            return floor();
        } else {
            return ceiling();
        }
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public final String type() {
        return "realtype";
    }
}
