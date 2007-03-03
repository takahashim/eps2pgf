/*
 * PSObjectArray.java
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

import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/** PostScript object: array
 *
 * @author Paul Wagenaars
 */
public class PSObjectArray extends PSObject {
    private ArrayList<PSObject> array;
    private int offset;
    private int count;
    
    /**
     * Create a new empty PostScript array
     */
    public PSObjectArray() {
        array = new ArrayList<PSObject>();
        offset = 0;
        count = Integer.MAX_VALUE;
    }

    /**
     * Creates a new instance of PSObjectArray
     * 
     * @param dblArray Array of doubles
     */
    public PSObjectArray(double[] dblArray) {
        array = new ArrayList<PSObject>(dblArray.length);
        for (int i = 0 ; i < dblArray.length ; i++) {
            array.add(new PSObjectReal(dblArray[i]));
        }
        
        // Use the entire array
        offset = 0;
        count = Integer.MAX_VALUE;        
    }
    
    /**
     * Creates a new instance of PSObjectArray
     * @param objs Objects that will be stored in the new array.
     */
    public PSObjectArray(PSObject[] objs) {
        array = new ArrayList<PSObject>(objs.length);
        for (int i = 0 ; i < objs.length ; i++) {
            array.add(objs[i]);
        }
        
        // Use the entire array
        offset = 0;
        count = Integer.MAX_VALUE;
    }
    
    /**
     * Creates a new instance of PSObjectArray. The new array is a subset
     * of the supplied array. Thery share the data (changing a value is one
     * also changes the value in the other.
     * @param obj Complete array from which this new PSObjectArray is a subset.
     * @param index Index of the first element of the subarray in the obj array.
     * @param newCount Number of items in the subarray.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Indices out of range.
     */
    public PSObjectArray(PSObjectArray obj, int index, int newCount) throws PSErrorRangeCheck {
        int n = obj.size();
        if (index >= n) {
            throw new PSErrorRangeCheck();
        }
        if ( (index + count - 1) >= n ) {
            throw new PSErrorRangeCheck();
        }

        array = obj.array;
        offset = obj.offset+index;
        count = newCount;
    }
    
    /**
     * Returns the number of elements in this array.
     * @return Number of items in this array.
     */
    public int size() {
        return Math.min(count, array.size()-offset);
    }
    
    /**
     * Add an element to the end to this array.
     * @param value Value of the new element
     */
    public void add(PSObject value) {
        array.add(value);
    }
    
    /**
     * Insert an element at the specified position in this array.
     * @param index Index at which the new element will be inserted.
     * @param value Value of the new element
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public void add(int index, PSObject value) {
        array.add(index+offset, value);
    }
    
    /**
     * Replace the element with offset with value.
     * @param index Index of the element to replace.
     * @param value New value of the element.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public void set(int index, PSObject value) throws PSErrorRangeCheck {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        array.set(index+offset, value);
    }
    
    /**
     * Returns an object from this array.
     * @param index Index of the element to return.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     * @return Value of the specifiec element.
     */
    public PSObject get(int index) throws PSErrorRangeCheck {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.get(index+offset);
    }
    
    /**
     * Remove an element from this array.
     * @param index Index of the element to remove.
     * @return Removed element.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of range.
     */
    public PSObject remove(int index) throws PSErrorRangeCheck {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.remove(index+offset);
    }
    
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return String representation of this object.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong.
     */
    public String isis() {
        StringBuilder str = new StringBuilder();
        str.append("[ ");
        try {
            for (int i = 0 ; i < size() ; i++) {
                str.append(get(i).isis() + " ");
                if ( ((i+offset)%15) == 14 ) {
                    str.append("\n");
                }
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen.
        }
        str.append("]");
        return str.toString();
    }
    
    /**
     * Replace executable name objects with their values
     * @param interp Interpreter to which the operators must be bound.
     * @return This array.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong.
     */
    public PSObjectArray bind(Interpreter interp) throws PSErrorTypeCheck {
        try {
            for (int i = 0 ; i < size() ; i++) {
                set(i, get(i).bind(interp));
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        return this;
    }
    
    /**
     * Copies the values from an array into this array.
     * @param toBeCopied Object from which the values must be copied
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to copy values.
     */
    public void copyFrom(PSObjectArray toBeCopied) throws PSErrorRangeCheck, PSErrorUnimplemented {
        int n = toBeCopied.size();
        int m = size();
        if (m < n) {
            throw new PSErrorRangeCheck();
        }
        
        // Copy all objects from toBeCopied
        for (int i = 0 ; i < n ; i++) {
            PSObject obj = toBeCopied.get(i);
            try {
                set(i, obj.clone());
            } catch (CloneNotSupportedException e) {
                throw new PSErrorUnimplemented("Cloning " + obj.getClass() + " objects.");
            }
        }
        
        // If this object has more objects than toBeCopied
        for (int i = (m-1) ; i >= n ; i--) {
            remove(i);
        }
    }
    
    /**
     * Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object.
     * @param index Index of the first element of the subarray
     * @param count Number of items in the subarray
     * @return Subarray
     * @throws net.sf.eps2pgf.postscript.errors.PSError Index out of bounds.
     */
    public PSObjectArray getinterval(int index, int count) throws PSErrorRangeCheck {
        return new PSObjectArray(this, index, count);
    }
    
    /**
     * Convert this object to an array.
     * @return This array
     */
    public PSObjectArray toArray() {
        return this;
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Array is not a valid matrix
     * @return Matrix representation of this array
     */
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck, PSErrorTypeCheck {
        if (this.size() != 6) {
            throw new PSErrorRangeCheck();
        }
        return new PSObjectMatrix(this.get(0).toReal(), this.get(1).toReal(),
                this.get(2).toReal(), this.get(3).toReal(),
                this.get(4).toReal(), this.get(5).toReal());
    }
    
    /**
     * Copies values from another obj to this object.
     * @param obj Object from which the values must be copied
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to copy values from object.
     */
    public void copyValuesFrom(PSObject obj) throws PSErrorTypeCheck {
        PSObjectArray array = obj.toArray();
        
        try {
            // First remove all current elements from the array
            for (int i = size()-1 ; i >= 0 ; i--) {
                remove(i);
            }
        
            // Copies the values
            for (int i = 0 ; i < array.size() ; i++) {
                add(i, array.get(i));
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
    }
    
    /**
     * Creates a deep copy of this array.
     * @throws java.lang.CloneNotSupportedException Unable to clone this object or one of its sub-objects
     * @return Deep copy of this array
     */
    public PSObjectArray clone() throws CloneNotSupportedException {
        PSObject[] objs = new PSObject[size()];
        try {
            for (int i = 0 ; i < objs.length ; i++) {
                objs[i] = get(i).clone();
            }
        } catch (PSError e) {
            throw new CloneNotSupportedException();
        }
        return new PSObjectArray(objs);
    }

}
