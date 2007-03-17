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

import org.fontbox.afm.*;

import net.sf.eps2pgf.postscript.errors.*;

/** Base class for PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class PSObject implements Cloneable {
    /**
     * Checks whether the supplied string is of this type.
     * @param str String to be checked.
     * @return Return true when the string of the objects type.
     */
    public static boolean isType(String str) {
        return false;
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
     * Process this object in the supplied interpreter. This is the way
     * objects from the operand stack are processed.
     * @param interp Interpreter in which this object is processed.
     * @throws java.lang.Exception An error occured during the execution of this object.
     */
    public void process(Interpreter interp) throws Exception {
        interp.opStack.push(this);
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
     * Convert this object to an array, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can't convert this object to an array.
     * @return Array representation of this object.
     */
    public PSObjectArray toArray() throws PSErrorTypeCheck {
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
     * Convert this object to dictionary key, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unable to convert this object type to a dict key
     * @return Dictionary key that represents this object
     */
    public String toDictKey() throws PSErrorTypeCheck {
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
     * Convert this object to a real number, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Unbale to convert this object to a real number
     * @return Floating-point number representation of this object
     */
    public double toReal() throws PSErrorTypeCheck {
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
     * Creates a copy of this object.
     * @throws java.lang.CloneNotSupportedException This object does not suppert cloneing.
     * @return Deep copy of this object
     */
    public PSObject clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
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
     * Implements bind operator for this object. For most object this wil
     * be the same object, without any change.
     * @param interp Interpreter in which this object will be executed.
     * @return Return this object after binding
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This object type does not support bind.
     */
    public PSObject bind(Interpreter interp) throws PSErrorTypeCheck {
        return this;
    }
}
