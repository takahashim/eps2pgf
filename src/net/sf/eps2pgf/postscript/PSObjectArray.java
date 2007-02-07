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
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/** PostScript object: array
 *
 * @author Paul Wagenaars
 */
public class PSObjectArray extends PSObject {
    private ArrayList<PSObject> array;
    private int offset;
    private int count;
    
    /** Creates a new instance of PSObjectArray */
    public PSObjectArray(PSObject[] objs) {
        array = new ArrayList<PSObject>(objs.length);
        for (int i = 0 ; i < objs.length ; i++) {
            array.add(objs[i]);
        }
        
        // Use the entire array
        offset = 0;
        count = Integer.MAX_VALUE;
    }
    
    /** Creates a new instance of PSObjectArray. The new array is a subset
     * of the supplied array. Thery share the data (changing a value is one
     * also changes the value in the other.
     */
    public PSObjectArray(PSObjectArray obj, int index, int newCount) throws PSError {
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
    
    /** Returns the number of elements in this array. */
    public int size() {
        return Math.min(count, array.size()-offset);
    }
    
    /**
     * Replace the element with offset with value.
     */
    public void set(int index, PSObject value) throws PSError {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        array.set(index+offset, value);
    }
    
    /**
     * Returns an object from this array.
     */
    public PSObject get(int index) throws PSError {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.get(index+offset);
    }
    
    /**
     * Remove an element from this array.
     */
    public PSObject remove(int index) throws PSError {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        return array.remove(index+offset);
    }
    
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() throws PSError {
        StringBuilder str = new StringBuilder();
        str.append("[ ");
        for (int i = 0 ; i < size() ; i++) {
            str.append(get(i).isis() + " ");
            if ( ((i+offset)%15) == 14 ) {
                str.append("\n");
            }
        }
        str.append("]");
        return str.toString();
    }
    
    /** Replace executable name objects with their values */
    public PSObjectArray bind(Interpreter interp) throws PSError {
        for (int i = 0 ; i < size() ; i++) {
            set(i, get(i).bind(interp));
        }
        return this;
    }
    
    /** Copies the values from an array into this array. */
    public void copyFrom(PSObjectArray toBeCopied) throws PSError {
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
    
    /** Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object. */
    public PSObjectArray getinterval(int index, int count) throws PSError {
        return new PSObjectArray(this, index, count);
    }
}
