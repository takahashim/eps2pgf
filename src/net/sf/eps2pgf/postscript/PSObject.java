/*
 * PSObject.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript;

import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

import org.fontbox.afm.FontMetric;

/** Base class for PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class PSObject implements Cloneable, Iterable<PSObject> {
    /** Unlimited access. */
    static final int ACCESS_UNLIMITED = 0;
    
    /** Only reading and executing is allowed. */
    static final int ACCESS_READONLY = 1;
    
    /** Only executing is allowed. */
    static final int ACCESS_EXECUTEONLY = 2;
    
    /** Nothing is allowed. */
    static final int ACCESS_NONE = 3;
    
    /** Indicates whether this object is literal or executable. */
    Boolean isLiteral = true;
    
    /** Current access level of object. */
    int access = ACCESS_UNLIMITED;

    /**
     * Returns the absolute value of this object, if possible.
     * @return Absolute value of this object
     * @throws PSErrorTypeCheck Object is not numeric
     */
    public PSObject abs() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns the sum of this object and the passed object, if both are
     * numeric.
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
     * PostScript operator 'and'
     * @param obj2 Object to 'and' with this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or both object does not have the correct type for the xor
     * operation
     * @return Logical 'and' of both values
     */
    public PSObject and(PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Implements bind operator for this object. For most object this wil
     * be the same object, without any change.
     * @param interp Interpreter in which this object will be executed.
     * @return Return this object after binding
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This object type does not support bind.
     */
    public PSObject bind(Interpreter interp) throws PSErrorTypeCheck {
        return this;
    }
    
    /**
     * Return this value rounded upwards
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     * @return Value of this object rounded upwards
     */
    public PSObject ceiling() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Check the access attribute of this object. Throws an exception when
     * not allowed.
     */
    public void checkAccess(boolean execute, boolean read, boolean write) 
            throws PSErrorInvalidAccess {
        if (access == ACCESS_READONLY) {
            if (write) {
                throw new PSErrorInvalidAccess();
            }
        } else if (access == ACCESS_EXECUTEONLY) {
            if (write || read) {
                throw new PSErrorInvalidAccess();
            }
        } else if (access == ACCESS_NONE) {
            if (write || read || execute) {
                throw new PSErrorInvalidAccess();
            }
        }
    }
    
    /**
     * Creates a (deep) copy of this object.
     * @return Deep copy of this object
     */
    public PSObject clone() {
        return dup();
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Subarray/substring longer that original array/string
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Invalid or incompatible object type(s) for copy operator
     */
    public PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Copies common object attributes (literal/executable and access) from
     * another object to this object.
     */
    void copyCommonAttributes(PSObject obj) {
        access = obj.access;
        isLiteral = obj.isLiteral;
    }
    
    /**
     * PostScript operator 'cvrs'
     */
    public String cvrs(int radix) throws PSErrorTypeCheck, PSErrorRangeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
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
        dup.isLiteral = false;
        return dup;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObject dup() {
        return null;
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        return (this == obj);
    }
    
    /**
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     */
    public boolean equals(Object obj) {
    	if (obj instanceof PSObject) {
    		return this.eq((PSObject)obj);
    	} else {
    		return false;
    	}
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not set access attribute of this object type to 'executeonly'
     */
    public void executeonly() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Return this value rounded downwards
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     * @return Value of this object rounded downwards
     */
    public PSObject floor() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator: get
     * Gets a single element from this object.
     * @param index Index/key of object to get
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'get' from this object or invalid index/key
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Index out of bounds
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Unknown key
     * @return Requested object
     */
    public PSObject get(PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorUndefined {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * @param index Index of the first element of the subarray
     * @param count Number of elements in the subarray
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid index or number of elements
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to get a subinterval of this object
     * @return Object representing a subarray of this object. The data is shared
     * between both objects.
     */
    public PSObject getinterval(int index, int count) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This object does not have a list of items
     */
    public List<PSObject> getItemList() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'gt'
     * @param obj2 Object to compare this object to
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to compare the type of this object and/or obj2
     * @return Returns true when this object is greater than obj2, return false
     * otherwise.
     */
    public boolean gt(PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns a hashCode value for this object. This method is supported
     * for the benefit hashtables, such as used in PSObjectDict.
     */
    public int hashCode() {
    	return this.isis().hashCode();
    }
    
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
    public static boolean isType(String str) {
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
     * Implements PostScript operate: length
     * @return Length of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to get the length of this object type
     */
    public int length() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Multiply this object with another object
     * @param obj Multiplication of this object and passed object
     * @return Multiplication object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject mul(PSObject obj) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * Returns the negative value of this object, if possible
     * @return Negative value of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject neg() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'noaccess'. Set access attribute to 'none'.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not change 'access' attribute of this object type
     */
    public void noaccess() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator: 'not'
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to determine logical negation of this object
     * @return Logical negation of this object
     */
    public PSObject not() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /** PostScript operator 'or' */
    public PSObject or(PSObject obj2) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid index or key
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'put' anything in this object type
     */
    public void put(PSObject index, PSObject value) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'putinterval' anything in this object type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Index or (index+length) out of bounds
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck,
            PSErrorRangeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not check 'access' attribute of this object type
     * @return Returns true when this object is readable; returns false otherwise
     */
    public boolean rcheck() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * PostScript operator 'readonly'. Set access attribute to 'readonly'.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not change the 'access' attribute of this object type
     */
    public void readonly() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Return this value rounded to the nearest integer
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     * @return Value of this object rounded to the nearest integer
     */
    public PSObject round() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Subtract an object from this object
     * @param obj Object that will be subtracted from this object
     * @return Passed object subtracted from this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject sub(PSObject obj) throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to an array, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can't convert this object to an array.
     * @return Array representation of this object.
     */
    public PSObjectArray toArray() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a boolean, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a boolean
     * @return Boolean representation of this object
     */
    public boolean toBool() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a dictionary, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a dictionary
     * @return Dictionary representation of this object.
     */
    public PSObjectDict toDict() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a file object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a dict key
     * @return File object representation of this object
     */
    public PSObjectFile toFile() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a font dictionary, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a dictionary
     * @return Dictionary representation of this object.
     */
    public PSObjectFont toFont() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a FontBox FontMetric object, if possible
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a FontMetric
     * @return FontMetric object
     */
    public FontMetric toFontMetric() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to an integer, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object to an integer
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
     * @throws PSErrorTypeCheck This object has no integer representation.
     */
    public int cvi() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a literal object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Converting this object type to literal is not yet supported
     * @return This object converted to a literal object
     */
    public PSObject cvlit() throws PSErrorUnimplemented {
        PSObject dup = dup();
        dup.isLiteral = true;
        return dup;
    }
    
    /**
     * PostScript operator 'cvr'. Convert this object to a real
     */
    public double cvr() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck This object does not have the correct number of elements. A matrix
     * should have six elements.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a matrix
     * @return Matrix representation of this object
     */
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck, PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a name object, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object to a name
     * @return Name representation of this object
     */
    public PSObjectName toName() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a non-negative integer, if possible
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Integer is negative
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object to an integer
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
     * Convert this object to a non-negative double, if possible
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Real number is negative
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object to a double
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a procedure.
     * @return Procedure representation of this object
     */
    public PSObjectArray toProc() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a string object, if possible
     * @throws PSErrorTypeCheck Unble to convert this object type to a PostScript string object
     * @return PostScript string object representation of this object
     */
    public PSObjectString toPSString() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Convert this object to a real number, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object to a real number
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
     * @return List with one or more objects. The following are possible:
     *         1 object : { <false boolean> }
     *         2 objects: { <next token>, <true boolean> }
     *         3 objects: { <remainder of this object>, <next token>, <true boolean> }
     */
    public List<PSObject> token() throws PSError {
        List<PSObject> list = new ArrayList<PSObject>(3);
        list.add(0, null);
        list.add(1, this);
        list.add(2, new PSObjectBool(true));
        return list;
    }
    
    /**
     * Return this value towards zero
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     * @return Value of this object towards zero
     */
    public PSObject truncate() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "generictype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not check 'access' attribute of this object type
     * @return Returns true when this object is writable; returns false otherwise
     */
    public boolean wcheck() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }

    /**
     * PostScript operator 'xcheck'. Checks whether this object is executable
     * @return Returns true if this object is executable
     */
    public boolean xcheck() {
        return !isLiteral;
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
    
}
