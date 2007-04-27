/*
 * PSObject.java
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

import java.util.List;

import org.fontbox.afm.*;

import net.sf.eps2pgf.postscript.errors.*;

/** Base class for PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class PSObject implements Cloneable, Iterable<PSObject> {
    static final int ACCESS_UNLIMITED = 0;
    static final int ACCESS_READONLY = 1;
    static final int ACCESS_EXECUTEONLY = 2;
    static final int ACCESS_NONE = 3;
    
    Boolean isLiteral = true;
    int access = ACCESS_UNLIMITED;

    /**
     * Returns the absolute value of this object, if possible
     * @return Absolute value of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object is not numeric
     */
    public PSObject abs() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Returns the sum of this object and the passed object, if both are numeric
     * @param obj Object that will be added to this object
     * @return Sum of this object and passed object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Object(s) are not numeric
     */
    public PSObject add(PSObject obj) throws PSErrorTypeCheck {
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
     * Creates a copy of this object.
     * @throws java.lang.CloneNotSupportedException This object does not suppert cloneing.
     * @return Deep copy of this object
     */
    public PSObject clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
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
     * Copies values from another object to this object, if possible.
     * @param obj Object from which the values must the copied
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck The supplied object does not have the correct number of elements.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck The supplied object can not be converted to the same type as this
     * object.
     */
    public void copyValuesFrom(PSObject obj) throws PSErrorRangeCheck, PSErrorTypeCheck {
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
     */
    public PSObject cvx() {
        isLiteral = false;
        return this;
    }
    
    /**
     * Make this object executable
     */
    
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
     * Executes this object in the supplied interpreter
     * @param interp Interpreter in which this object is executed.
     * @throws java.lang.Exception An error occured during the execution of this object.
     */
    public void execute(Interpreter interp) throws Exception {
        interp.opStack.push(this);
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() throws PSErrorTypeCheck, PSErrorInvalidAccess {
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
    public PSObject getinterval(int index, int count) throws PSErrorRangeCheck, PSErrorTypeCheck {
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
     */
    public void noaccess() throws PSErrorTypeCheck, PSErrorInvalidAccess {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * Process this object in the supplied interpreter. This is the way
     * objects from the operand stack are processed.
     * @param interp Interpreter in which this object is processed.
     * @throws java.lang.Exception An error occured during the execution of this object.
     */
    public void process(Interpreter interp) throws Exception {
        interp.opStack.push(this);
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
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck, PSErrorRangeCheck {
        throw new PSErrorTypeCheck();
    }
    
    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     */
    public boolean rcheck() throws PSErrorTypeCheck {
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
     * Convert this object to dictionary key, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a dict key
     * @return Dictionary key that represents this object
     */
    public String toDictKey() throws PSErrorTypeCheck {
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
     * Convert this object to a literal object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented Converting this object type to literal is not yet supported
     * @return This object converted to a literal object
     */
    public PSObject cvlit() throws PSErrorUnimplemented {
        isLiteral = true;
        return this;
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
    public PSObjectProc toProc() throws PSErrorTypeCheck {
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unbale to convert this object to a real number
     * @return Floating-point number representation of this object
     */
    public double toReal() throws PSErrorTypeCheck {
        throw new PSErrorTypeCheck();
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
     * PostScript operator 'xcheck'. Checks whether this object is executable
     * @return Returns true if this object is executable
     */
    public boolean xcheck() {
        return !isLiteral;
    }
    
}
