/*
 * PSObject.java
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

import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;

import org.fontbox.afm.FontMetric;

/** Base class for PostScript objects.
 *
 * @author Paul Wagenaars
 */
public abstract class PSObject implements Cloneable, Iterable<PSObject> {
    
    /** Possible values for access attribute. */
    public enum Access { UNLIMITED, READONLY, EXECUTEONLY, NONE };
    
    /** Indicates whether this object is literal or executable. */
    private boolean literal = true;
    
    /** Current access level of object. */
    private Access access = Access.UNLIMITED;
    

    /**
     * Returns the absolute value of this object, if possible.
     * 
     * @return Absolute value of this object
     * 
     * @throws PSErrorTypeCheck Object is not numeric
     */
    public PSObject abs() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns the sum of this object and the passed object, if both are
     * numeric.
     * 
     * @param obj Object that will be added to this object
     * 
     * @return Sum of this object and passed object
     * 
     * @throws PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject add(final PSObject obj) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'and'.
     * 
     * @param obj2 Object to 'and' with this object
     * 
     * @throws PSErrorTypeCheck One or both object does not have the correct
     * type for the xor operation.
     * 
     * @return Logical 'and' of both values
     */
    public PSObject and(final PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Implements bind operator for this object. For most object this will
     * be the same object, without any change.
     * 
     * @param interp Interpreter in which this object will be executed.
     * 
     * @return Return this object after binding
     * 
     * @throws PSErrorTypeCheck This object type does not support bind.
     */
    public PSObject bind(final Interpreter interp) throws PSErrorTypeCheck {
        return this;
    }
    
    /**
     * Return this value rounded upwards.
     * 
     * @throws PSErrorTypeCheck Object is not numeric.
     * 
     * @return Value of this object rounded upwards
     */
    public PSObject ceiling() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Check the access attribute of this object. Throws an exception when
     * not allowed.
     * 
     * @param execute Is object executed?
     * @param read Is object read?
     * @param write Is object written?
     * 
     * @throws PSErrorInvalidAccess Requested action is not permitted.
     */
    public void checkAccess(final boolean execute, final boolean read,
            final boolean write) throws PSErrorInvalidAccess {
        if (access == Access.READONLY) {
            if (write) {
                throw new PSErrorInvalidAccess();
            }
        } else if (access == Access.EXECUTEONLY) {
            if (write || read) {
                throw new PSErrorInvalidAccess();
            }
        } else if (access == Access.NONE) {
            if (write || read || execute) {
                throw new PSErrorInvalidAccess();
            }
        }
    }
    
    /**
     * Creates a (deep) copy of this object.
     * 
     * @return Deep copy of this object
     */
    @Override
    public PSObject clone() {
        PSObject copy;
        try {
            copy = (PSObject) super.clone();
        } catch (CloneNotSupportedException e) {
            copy = (PSObject) new PSObjectNull();
            copy.copyCommonAttributes(this);
        }
        return copy;
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * 
     * @param obj1 Copy values from obj1
     * 
     * @return Returns subsequence of this object
     * 
     * @throws PSErrorRangeCheck Subarray/substring longer that original
     * array/string.
     * @throws PSErrorTypeCheck Invalid or incompatible object type(s) for
     * copy operator.
     */
    public PSObject copy(final PSObject obj1)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Copies common object attributes (literal/executable and access) from
     * another object to this object.
     * 
     * @param obj Object from which attributes are copied.
     */
    protected void copyCommonAttributes(final PSObject obj) {
        setAccess(obj.getAccess());
        literal = obj.literal;
    }
    
    /**
     * PostScript operator 'cvrs'.
     * 
     * @param radix The radix.
     * 
     * @return the string
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public String cvrs(final int radix)
            throws PSErrorTypeCheck, PSErrorRangeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    public String cvs() {
        return "--nostringval--";
    }
    
    /**
     * PostScript operator 'cvx'. Makes this object executable
     * @return Exectable version of this object
     */
    public PSObject cvx() {
        PSObject dup = dup();
        dup.literal = false;
        return dup;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    public abstract PSObject dup();
    
    /**
     * Compare this object with another object and return true if they are
     * equal. See PostScript manual on what's equal and what's not.
     * 
     * @param obj Object to compare this object with
     * 
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(final PSObject obj) {
        return (this == obj);
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
    public abstract boolean equals(final Object obj);
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     * 
     * @throws PSErrorTypeCheck Can not set access attribute of this object type
     * to 'executeonly'.
     */
    public void executeonly() throws PSErrorTypeCheck {
        if ((this instanceof PSObjectArray) || (this instanceof PSObjectFile)
                || (this instanceof PSObjectString)) {
            setAccess(Access.EXECUTEONLY);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * Return this value rounded downwards.
     * 
     * @throws PSErrorTypeCheck Object is not numeric.
     * 
     * @return Value of this object rounded downwards
     */
    public PSObject floor() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator: get
     * Gets a single element from this object.
     * @param index Index/key of object to get
     * 
     * @throws PSErrorTypeCheck Can not 'get' from this object or invalid
     * index/key.
     * @throws PSErrorRangeCheck Index out of bounds.
     * @throws PSErrorUndefined Unknown key.
     * 
     * @return Requested object
     */
    public PSObject get(final PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorUndefined {
        
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * 
     * @param index Index of the first element of the subarray
     * @param count Number of elements in the subarray
     * 
     * @throws PSErrorRangeCheck Invalid index or number of elements.
     * @throws PSErrorTypeCheck Unable to get a subinterval of this object.
     * 
     * @return Object representing a subarray of this object. The data is shared
     * between both objects.
     */
    public PSObject getinterval(final int index, final int count)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns a list with all items in object.
     * 
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     *         
     * @throws PSErrorTypeCheck This object does not have a list of items.
     */
    public List<PSObject> getItemList() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'gt'.
     * 
     * @param obj2 Object to compare this object to
     * 
     * @throws PSErrorTypeCheck Unable to compare the type of this object and/or
     * obj2.
     * 
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     */
    public boolean gt(final PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns a hashCode value for this object. This method is supported
     * for the benefit hashtables, such as used in PSObjectDict.
     * 
     * @return Hash code for this object.
     */
    @Override
    public abstract int hashCode();
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return Text representation of this object.
     */
    public String isis() {
        return this.getClass().getName();
    }
    
    /**
     * Checks whether the supplied string is of this type.
     * @param str String to be checked.
     * @return Return true when the string of the objects type.
     */
    public static boolean isType(final String str) {
        return false;
    }

    /**
     * Returns an iterator over this PostScript object.
     * @return An iterator over this object.
     */
    public PSObjectIterator iterator() {
        return new PSObjectIterator(this);
    }
    
    /**
     * Implements PostScript operate: length.
     * 
     * @return Length of this object
     * 
     * @throws PSErrorTypeCheck Unable to get the length of this object type.
     */
    public int length() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
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
    public PSObject mul(final PSObject obj) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * Returns the negative value of this object, if possible.
     * 
     * @return Negative value of this object
     * 
     * @throws PSErrorTypeCheck Object is not numeric
     */
    public PSObject neg() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'noaccess'. Set access attribute to 'none'.
     * 
     * @throws PSErrorTypeCheck Can not change 'access' attribute of this object
     * type.
     */
    public void noaccess() throws PSErrorTypeCheck {
        if ((this instanceof PSObjectArray)
                || (this instanceof PSObjectFile)
                || (this instanceof PSObjectString)
                || (this instanceof PSObjectDict)
                || (this instanceof PSObjectFont)) {
            setAccess(Access.NONE);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * PostScript operator: 'not'.
     * 
     * @throws PSErrorTypeCheck Unable to determine logical negation of this
     * object.
     * 
     * @return Logical negation of this object
     */
    public PSObject not() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'or'.
     * 
     * @param obj2 The obj2.
     * 
     * @return Result of test.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public PSObject or(final PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * 
     * @param index Index or key for new value
     * @param value New value
     * 
     * @throws PSErrorRangeCheck Invalid index or key.
     * @throws PSErrorTypeCheck Can not 'put' anything in this object type.
     */
    public void put(final PSObject index, final PSObject value)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator putinterval.
     * 
     * @param index Start index of subsequence
     * @param obj Subsequence
     * 
     * @throws PSErrorTypeCheck Can not 'putinterval' anything in this object
     * type.
     * @throws PSErrorRangeCheck Index or (index+length) out of bounds.
     */
    public void putinterval(final int index, final PSObject obj)
            throws PSErrorTypeCheck, PSErrorRangeCheck {
        
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     * 
     * @throws PSErrorTypeCheck Can not check 'access' attribute of this object
     * type.
     * 
     * @return Returns true when this object is readable; returns false
     * otherwise.
     */
    public boolean rcheck() throws PSErrorTypeCheck {
        if ((this instanceof PSObjectArray)
                || (this instanceof PSObjectFile)
                || (this instanceof PSObjectString)
                || (this instanceof PSObjectDict)
                || (this instanceof PSObjectFont)) {
            Access acs = getAccess();
            return ((acs == Access.UNLIMITED) || (acs == Access.READONLY));
        } else {
            throw new PSErrorTypeCheck();
        }        
    }

    /**
     * PostScript operator 'readonly'. Set access attribute to 'readonly'.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void readonly() throws PSErrorTypeCheck {
        if ((this instanceof PSObjectArray)
                || (this instanceof PSObjectFile)
                || (this instanceof PSObjectString)
                || (this instanceof PSObjectDict)
                || (this instanceof PSObjectFont)) {
            setAccess(Access.READONLY);
        } else {
            throw new PSErrorTypeCheck();
        }
    }
    
    /**
     * Return this value rounded to the nearest integer.
     * 
     * @throws PSErrorTypeCheck Object is not numeric.
     * 
     * @return Value of this object rounded to the nearest integer
     */
    public PSObject round() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Subtract an object from this object.
     * 
     * @param obj Object that will be subtracted from this object.
     * 
     * @return Passed object subtracted from this object.
     * 
     * @throws PSErrorTypeCheck Object(s) are not numeric.
     */
    public PSObject sub(final PSObject obj) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to an array, if possible.
     * @throws PSErrorTypeCheck Can't convert this object to an array.
     * @return Array representation of this object.
     */
    public PSObjectArray toArray() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a boolean, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object type to a boolean.
     * 
     * @return Boolean representation of this object
     */
    public boolean toBool() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a dictionary, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object type to a
     * dictionary.
     * 
     * @return Dictionary representation of this object.
     */
    public PSObjectDict toDict() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a file object.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object type to a dict
     * key.
     * 
     * @return File object representation of this object
     */
    public PSObjectFile toFile() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a font dictionary, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object type to a
     * dictionary.
     * 
     * @return Dictionary representation of this object.
     */
    public PSObjectFont toFont() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a FontBox FontMetric object, if possible.
     * 
     * @return FontMetric object
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public FontMetric toFontMetric() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to an integer, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object to an integer.
     * 
     * @return Integer representation of this object
     */
    public int toInt() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'cvi'. Convert this object to an integer
     * 
     * @return Integer representation of this object.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public int cvi() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a literal object.
     * 
     * @throws PSErrorUnimplemented Converting this object type to literal is
     * not yet supported.
     * 
     * @return This object converted to a literal object
     */
    public PSObject cvlit() throws PSErrorUnimplemented {
        PSObject dup = dup();
        dup.literal = true;
        return dup;
    }
    
    /**
     * PostScript operator 'cvr'. Convert this object to a real
     * 
     * @return the double
     * 
     * @throws PSError A PostScript error occurred.
     */
    public double cvr() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * 
     * @throws PSErrorRangeCheck This object does not have the correct number of
     * elements. A matrix should have six elements.
     * @throws PSErrorTypeCheck Unable to convert this object type to a matrix.
     * 
     * @return Matrix representation of this object
     */
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a name object, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object to a name.
     * 
     * @return Name representation of this object
     */
    public PSObjectName toName() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a non-negative integer, if possible.
     * 
     * @throws PSErrorRangeCheck Integer is negative.
     * @throws PSErrorTypeCheck Unable to convert this object to an integer.
     * 
     * @return Integer representation of this object
     */
    public int toNonNegInt() throws PSErrorRangeCheck, PSErrorTypeCheck {
        int n = toInt();
        if (n < 0) {
            throw new PSErrorRangeCheck();
        }
        return n;
    }
    
    /**
     * Convert this object to a non-negative double, if possible.
     * 
     * @throws PSErrorRangeCheck Real number is negative.
     * @throws PSErrorTypeCheck Unable to convert this object to a double.
     * 
     * @return Doubler representation of this object
     */
    public double toNonNegReal() throws PSErrorRangeCheck, PSErrorTypeCheck {
        double x = toReal();
        if (x < 0) {
            throw new PSErrorRangeCheck();
        }
        return x;
    }
    
    /**
     * Convert this object to a procedure object, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object type to a
     * procedure.
     * 
     * @return Procedure representation of this object
     */
    public PSObjectArray toProc() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a string object, if possible.
     * 
     * @throws PSErrorTypeCheck Unble to convert this object type to a
     * PostScript string object.
     * 
     * @return PostScript string object representation of this object
     */
    public PSObjectString toPSString() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a real number, if possible.
     * 
     * @throws PSErrorTypeCheck Unable to convert this object to a real number.
     * 
     * @return Floating-point number representation of this object
     */
    public double toReal() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * Please note that this method does not perform a type check following the
     * official 'token' operator. This method will always return a result.
     * 
     * @return List with one or more objects. The following are possible:
     *         1 object : { "false boolean" }
     *         2 objects: { "next token", "true boolean" }
     *         3 objects: { "remainder of this object", "next token",
     *                      "true boolean" }
     * 
     * @throws PSError A PostScript error occurred.
     */
    public List<PSObject> token() throws PSError {
        List<PSObject> list = new ArrayList<PSObject>(3);
        list.add(0, null);
        list.add(1, this);
        list.add(2, new PSObjectBool(true));
        return list;
    }
    
    /**
     * Return this value towards zero.
     * 
     * @throws PSErrorTypeCheck Object is not numeric.
     * 
     * @return Value of this object towards zero
     */
    public PSObject truncate() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "generictype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     * 
     * @throws PSErrorTypeCheck Can not check 'access' attribute of this object
     * type.
     * 
     * @return Returns true when this object is writable; returns false
     * otherwise.
     */
    public boolean wcheck() throws PSErrorTypeCheck {
        if ((this instanceof PSObjectArray)
                || (this instanceof PSObjectFile)
                || (this instanceof PSObjectString)
                || (this instanceof PSObjectDict)
                || (this instanceof PSObjectFont)) {
            return (getAccess() == Access.UNLIMITED);
        } else {
            throw new PSErrorTypeCheck();
        }        
    }

    /**
     * PostScript operator 'xcheck'. Checks whether this object is executable
     * @return Returns true if this object is executable
     */
    public boolean xcheck() {
        return !literal;
    }
    
    /**
     * PostScript operator 'xor'.
     * 
     * @param obj2 Object to 'xor' with this object.
     * 
     * @throws PSErrorTypeCheck One or both objects does not have the correct
     *         type for the xor operation.
     *         
     * @return Exclusive or of both values.
     */
    public PSObject xor(final PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * Set the literal/executable property of this object.
     * 
     * @param isLiteral True if object must be literal, false if object must be
     * executable.
     */
    public void setLiteral(final boolean isLiteral) {
        literal = isLiteral;
    }

    /**
     * Check whether this object is literal or executable.
     * 
     * @return True if object is literal, false if object is executable.
     */
    public boolean isLiteral() {
        return literal;
    }

    /**
     * Sets the access permission for this object.
     * 
     * @param pAccess New access permissions.
     */
    public void setAccess(final Access pAccess) {
        access = pAccess;
    }

    /**
     * Gets the access permission for this object.
     * 
     * @return Current access permissions.
     */
    public Access getAccess() {
        return access;
    }
    
}
