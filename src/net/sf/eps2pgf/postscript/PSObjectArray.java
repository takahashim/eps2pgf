/*
 * PSObjectArray.java
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;

/**
 * PostScript object: array.
 *
 * @author Paul Wagenaars
 */
public class PSObjectArray extends PSObject {
    List<PSObject> array;
    int offset;
    int count;
    
    /**
     * Create a new empty PostScript array.
     */
    public PSObjectArray() {
        this.array = new ArrayList<PSObject>();
        this.offset = 0;
        this.count = Integer.MAX_VALUE;
    }

    /**
     * Creates a new instance of PSObjectArray.
     * 
     * @param dblArray Array of doubles
     */
    public PSObjectArray(final double[] dblArray) {
        this.array = new ArrayList<PSObject>(dblArray.length);
        for (int i = 0; i < dblArray.length; i++) {
            this.array.add(new PSObjectReal(dblArray[i]));
        }
        
        // Use the entire array
        this.offset = 0;
        this.count = dblArray.length;
    }
    
    /**
     * Creates a new instance of PSObjectArray.
     * 
     * @param objs Objects that will be stored in the new array.
     */
    public PSObjectArray(final PSObject[] objs) {
        this.array = new ArrayList<PSObject>(objs.length);
        for (int i = 0; i < objs.length; i++) {
            this.array.add(objs[i]);
        }
        
        // Use the entire array
        this.offset = 0;
        this.count = objs.length;
    }
    
    /**
     * Creates a new executable array object.
     * 
     * @param pStr String representing a valid procedure (executable array)
     * 
     * @throws IOException Unable to read the string
     * @throws PSError PostScript error occurred.
     */
    public PSObjectArray(final String pStr) throws IOException, PSError {
    	String str = pStr;
    	
        // quick check whether it is a literal or executable array
        if (str.charAt(0) == '{') {
            isLiteral = false;
        } else if (str.charAt(0) == '[') {
            isLiteral = true;
        }
        
        str = str.substring(1, str.length() - 1);
        
        InputStream inStream = new PSStringInputStream(new PSObjectString(str));
        
        this.array = Parser.convertAll(inStream);
        this.count = this.array.size();
        this.offset = 0;
    }
    
    /**
     * Creates a new instance of PSObjectArray. The new array is a subset
     * of the supplied array. They share the data (changing a value is one
     * also changes the value in the other.
     * 
     * @param obj Complete array from which this new PSObjectArray is a subset.
     * @param index Index of the first element of the subarray in the obj array.
     * @param newCount Number of items in the subarray.
     * 
     * @throws PSErrorRangeCheck Indices out of range.
     */
    public PSObjectArray(final PSObjectArray obj, final int index,
    		final int newCount) throws PSErrorRangeCheck {
        int n = obj.size();
        if ((newCount != 0) || (index != 0)) {
            if (index >= n) {
                throw new PSErrorRangeCheck();
            }
            if ((index + this.count - 1) >= n) {
                throw new PSErrorRangeCheck();
            }
        }

        this.array = obj.array;
        this.offset = obj.offset + index;
        this.count = newCount;
        copyCommonAttributes(obj);
    }
    
    /**
     * Insert an element at the specified position in this array.
     * 
     * @param index Index at which the new element will be inserted.
     * @param value Value of the new element
     */
    public final void addAt(final int index, final PSObject value) {
        this.array.add(index + this.offset, value);
    }
    
    /**
     * Add an element to the end to this array.
     * @param value Value of the new element
     */
    public final void addToEnd(final PSObject value) {
        this.array.add(value);
    }
    
    /**
     * Replace executable name objects with their values.
     * 
     * @param interp Interpreter to which the operators must be bound.
     * 
     * @return This array.
     * 
     * @throws PSErrorTypeCheck PostScript typecheck error.
     */
    public final PSObjectArray bind(final Interpreter interp)
    		throws PSErrorTypeCheck {
        try {
            for (int i = 0; i < size(); i++) {
                PSObject obj = get(i);
                set(i, obj.bind(interp));
                if (obj instanceof PSObjectArray) {
                    if (!((PSObjectArray) obj).isLiteral) {
                        obj.readonly();
                    }
                }
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        return this;
    }
    
    /**
     * Creates a deep copy of this array.
     * 
     * @return Deep copy of this array
     */
    public PSObjectArray clone() {
        PSObject[] objs = new PSObject[size()];
        int i = 0;
        for (PSObject obj : this) {
            objs[i] = obj.clone();
            i++;
        }
        PSObjectArray newArray = new PSObjectArray(objs);
        newArray.copyCommonAttributes(this);
        return newArray;
    }

    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     */
    public final PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck {
        PSObjectArray array = obj1.toArray();
        putinterval(0, array);
        return getinterval(0, array.length());
    }
    
    /**
     * Convert this object to a literal object.
     * 
     * @return This object converted to a literal object
     */
    public final PSObject cvlit() {
        isLiteral = true;
        return this;
    }

    /**
     * PostScript operator 'cvx'. Makes this object executable
     * 
     * @return This object after it has been made executable.
     */
    public final PSObject cvx() {
        isLiteral = false;
        return this;
    }

    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     */
    public PSObjectArray dup() {
        try {
            return new PSObjectArray(this, 0, size());
        } catch (PSErrorRangeCheck e) {
            // this can never happen
            return null;
        }
    }
    
    /**
     * Compare this object with another object and return true if they are
     * equal. See PostScript manual on what's equal and what's not.
     * 
     * @param obj Object to compare this object with
     * 
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        try {
            PSObjectArray objArr = obj.toArray();
            if ((this.count != objArr.count)
            		|| (this.offset != objArr.offset)) {
                return false;
            }
            return (this.array == objArr.array);
        } catch (PSErrorTypeCheck e) {
            return false;
        }
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() {
        access = ACCESS_EXECUTEONLY;
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
     * PostScript operator: get
     * Gets a single element from this object.
     */
    public PSObject get(PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorUndefined {
        return get(index.toInt());
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() {
        List<PSObject> items = new LinkedList<PSObject>();
        items.add(new PSObjectInt(1));
        for (PSObject obj : this) {
            items.add(obj);
        }
        return items;
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
     * Gets the object at the requested index and returns the real value of that
     * object.
     * @param index Index of the object to get
     */
    public double getReal(int index) throws PSErrorRangeCheck, PSErrorTypeCheck {
        return get(index).toReal();
    }
    
    /**
     * Check whether a string is a procedure
     * @param str String to check.
     * @return Returns true when the string is a procedure. Returns false otherwise.
     */
    public static boolean isType(String str) {
        int len = str.length();
        if ( (str.charAt(0) == '{') && (str.charAt(len-1) == '}') ) {
            return true;
        } else if ( (str.charAt(0) == '[') && (str.charAt(len-1) == ']') ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return String representation of this object.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Something went wrong.
     */
    public String isis() {
        StringBuilder str = new StringBuilder();
        if (isLiteral) {
            str.append("[ ");
        } else {
            str.append("{ ");
        }
        for (PSObject obj : this) {
            str.append(obj.isis() + " ");
        }
        if (isLiteral) {
            str.append("]");
        } else {
            str.append("}");
        }
        return str.toString();
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() {
        return size();
    }
    
    /**
     * PostScript operator: 'noaccess'
     */
    public void noaccess() {
        access = ACCESS_NONE;
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     */
    public void put(PSObject index, PSObject value) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        put(index.toInt(), value);
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     */
    public void put(int index, PSObject value) throws PSErrorRangeCheck {
        if ( (index < 0) || (index >= size()) ) {
            throw new PSErrorRangeCheck();
        }
        array.set(index+offset, value);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'putinterval' anything in this object type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Index or (index+length) out of bounds
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck, PSErrorRangeCheck {
        PSObjectArray array3 = obj.toArray();
        int N = array3.length();
        for (int i = 0 ; i < N ; i++) {
            set(index + i, array3.get(i));
        }
    }

    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     */
    public boolean rcheck() {
        if ( (access == ACCESS_UNLIMITED) || (access == ACCESS_READONLY) ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PostScript operator: 'readonly'
     */
    public void readonly() {
        access = ACCESS_READONLY;
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
     * Replaces a value in this matrix
     */
    public void setReal(int index, double value) throws PSErrorRangeCheck {
        set(index, new PSObjectReal(value));
    }
    
    /**
     * Returns the number of elements in this array.
     * @return Number of items in this array.
     */
    public int size() {
        return Math.min(count, array.size()-offset);
    }
    
    /**
     * Convert this object to an array.
     * @return This array
     */
    public PSObjectArray toArray() {
        return this;
    }
    
    /**
     * Converts this PostScript array to a double[] array with the requested size.
     * @param k Required number of items in the array. If the actual number of
     * items is different a PSErrorRangeCheck is thrown.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck The number of items in this array is not the same as the required
     * number of items.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck One or more items in this array can not be converted to a double.
     * @return Array with doubles
     */
    public double[] toDoubleArray(int k) throws PSErrorRangeCheck, PSErrorTypeCheck {
        if (k != size()) {
            throw new PSErrorRangeCheck();
        }
        return toDoubleArray();
    }
    
    /**
     * Convert this PostScript array to a double[] array.
     */
    public double[] toDoubleArray() throws PSErrorTypeCheck {
        double[] newArray = new double[size()];
        try {
            for (int i = 0 ; i < size() ; i++) {
                newArray[i] = get(i).toReal();
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen
        }
        
        return newArray;
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Array is not a valid matrix
     * @return Matrix representation of this array
     */
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck, PSErrorTypeCheck {
        return new PSObjectMatrix(this);
    }
    
    /**
     * Convert this object to an executable array (procedure), if possible.
     */
    public PSObjectArray toProc() throws PSErrorTypeCheck {
        if (isLiteral) {
            throw new PSErrorTypeCheck();
        }
        return this;
    }
    
    /**
     * Reads characters from this object, interpreting them as PostScript
     * code, until it has scanned and constructed an entire object.
     * Please note that this method does not perform a type check following the
     * offical 'token' operator. This method will always return a result.
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
        List<PSObject> list;
        int nr = size();
        if (nr == 0) {
            list = new ArrayList<PSObject>(1);
            list.add(0, new PSObjectBool(false));
        } else {
            list = new ArrayList<PSObject>(3);
            if (nr == 1) {
                list.add(0, new PSObjectArray());
            } else {
                list.add(0, new PSObjectArray(this, 1, nr - 1));
            }
            list.add(1, get(0));
            list.add(2, new PSObjectBool(true));
        }
        return list;
    }
    
    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "arraytype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     */
    public boolean wcheck() {
        return (access == ACCESS_UNLIMITED);
    }

}
