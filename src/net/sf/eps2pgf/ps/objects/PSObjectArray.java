/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.PSStringInputStream;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.Parser;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;

/**
 * PostScript object: array.
 *
 * @author Paul Wagenaars
 */
public class PSObjectArray extends PSObjectComposite implements Cloneable {
    
    /**
     * Offset in array (in other words: first "offset" items in array are
     * skipped.
     */
    private int offset;
    
    /** Number of items. Set to -1 to make array variable size. */
    private int count;
    
    /**
     * Creates a new PostScript array.
     * 
     * @param array The shared array object.
     * @param startIndex The index (in the shared object) of the first item.
     * @param length The (maximum) number of items.
     * @param virtualMemory The VM.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    private PSObjectArray(final List<PSObject> array, final int startIndex,
            final int length, final VM virtualMemory) throws PSErrorVMError {
        
        super(virtualMemory);
        
        offset = startIndex;
        count = length;
        setArray(array);
    }
    
    /**
     * Create a new empty variable-size PostScript array.
     * 
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error
     */
    public PSObjectArray(final VM virtualMemory) throws PSErrorVMError {
        this(new ArrayList<PSObject>(), 0, -1, virtualMemory);
    }
    
    /**
     * Create a new PostScript array with n elements filled with PSObjectNull
     * objects.
     * 
     * @param n Number of elements.
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error
     */
    public PSObjectArray(final int n, final VM virtualMemory)
            throws PSErrorVMError {
        
        this(new ArrayList<PSObject>(n), 0, n, virtualMemory);
        List<PSObject> list = getArray();
        PSObjectNull nullObj = new PSObjectNull();
        for (int i = 0; i < n; i++) {
            list.add(nullObj);
        }
    }

    /**
     * Creates a new instance of PSObjectArray.
     * 
     * @param dblArray Array of doubles
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error
     */
    //TODO is this method still required???
    public PSObjectArray(final double[] dblArray, final VM virtualMemory)
            throws PSErrorVMError {
        
        this(new ArrayList<PSObject>(dblArray.length), 0, dblArray.length,
                virtualMemory);
        List<PSObject> list = getArray();
        for (int i = 0; i < dblArray.length; i++) {
            list.add(new PSObjectReal(dblArray[i]));
        }
    }
    
    /**
     * Creates a new instance of PSObjectArray.
     * 
     * @param objs Objects that will be stored in the new array.
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error
     */
    public PSObjectArray(final PSObject[] objs, final VM virtualMemory)
            throws PSErrorVMError {
        
        this(new ArrayList<PSObject>(objs.length), 0, objs.length,
                virtualMemory);
        List<PSObject> list = getArray();
        for (int i = 0; i < objs.length; i++) {
            list.add(i, objs[i]);
        }
    }
    
    /**
     * Creates a new instance of PSObjectArray.
     * 
     * @param objs Objects that will be stored in the new array.
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error
     */
    //TODO Is this constructor actually used?
    //TODO Do I need to construct a new List<> object, or can I use the old one
    public PSObjectArray(final List<PSObject> objs, final VM virtualMemory)
            throws PSErrorVMError {
        
        this(new ArrayList<PSObject>(objs), 0, objs.size(), virtualMemory);
    }
    
    /**
     * Creates a new executable array object.
     * 
     * @param str String representing a valid procedure (executable array)
     * @param interp The interpreter (only required if string contains
     * immediately evaluated names).
     * 
     * @throws PSError PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectArray(String str, final Interpreter interp)
            throws ProgramError, PSError {
        
        super(interp.getVm());
        
        // quick check whether it is a literal or executable array
        if (str.charAt(0) == '{') {
            setLiteral(false);
            str = str.substring(1, str.length() - 1);
        } else if (str.charAt(0) == '[') {
            setLiteral(true);
            str = str.substring(1, str.length() - 1);
        }
        InputStream inStream =
            new PSStringInputStream(new PSObjectString(str, getVm()));
        try {
            setArray(Parser.convertAll(inStream, interp));
            count = getArray().size();
            offset = 0;
        } catch (IOException e) {
            throw new ProgramError("An IOException occured in"
                    + " PSObjectArray(String)");
        }
    }
    
    /**
     * Creates a new instance of PSObjectArray. The new array is exactly the
     * same as the supplied array. It shares the shared object and has the same
     * offset and count.
     * 
     * @param obj Complete array from which this new PSObjectArray is a subset.
     * 
     * @throws PSErrorRangeCheck Indices out of range.
     */
    public PSObjectArray(final PSObjectArray obj) throws PSErrorRangeCheck {
        super(obj.getVm(), obj.getId());
        offset = obj.offset;
        count = obj.count;
        copyCommonAttributes(obj);
    }
    
    /**
     * Creates a new instance of PSObjectArray. The new array is a subset
     * of the supplied array. They share the data (changing a value is one
     * also changes the value in the other.
     * 
     * @param obj Complete array from which this new PSObjectArray is a subset.
     * @param newOffset Index of the first element of the subarray in the object
     *        array.
     * @param newCount Number of items in the subarray.
     * 
     * @throws PSErrorRangeCheck Indices out of range.
     */
    public PSObjectArray(final PSObjectArray obj, final int newOffset,
            final int newCount) throws PSErrorRangeCheck {
        
        super(obj.getVm(), obj.getId());
        
        if ((newCount != 0) || (newOffset != 0)) {
            if ((newOffset + newCount) > obj.size()) {
                throw new PSErrorRangeCheck();
            }
        }
        
        offset = obj.offset + newOffset;
        count = newCount;
        copyCommonAttributes(obj);
    }
    
    /**
     * Add an element to the end to this array. This can only be done with
     * variable-size arrays (i.e. count = -1).
     * 
     * @param value Value of the new element
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public final void addToEnd(final PSObject value) throws PSErrorRangeCheck {
        if (count != -1) {
            throw new PSErrorRangeCheck();
        }
        getArray().add(value);
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
    @Override
    public final PSObjectArray bind(final Interpreter interp)
            throws PSErrorTypeCheck {
        
        List<PSObject> list = getArray();
        int startIndex = offset;
        int endIndex = startIndex + count;
        for (int i = startIndex; i < endIndex; i++) {
            PSObject obj = list.get(i);
            list.set(i, obj.bind(interp));
            if (obj instanceof PSObjectArray) {
                if (!obj.toArray().isLiteral()) {
                    obj.readonly();
                }
            }
        }
        
        return this;
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * 
     * @param obj1 Copy values from obj1
     * 
     * @return Returns subsequence of this object
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public final PSObject copy(final PSObject obj1)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        PSObjectArray tArray = obj1.toArray();
        putinterval(0, tArray);
        return getinterval(0, tArray.length());
    }
    
    /**
     * Creates a copy of this array object.
     * 
     * @return The copy of this object/
     */
    @Override
    public PSObjectArray clone() {
        return (PSObjectArray) super.clone();
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PSObjectArray dup() {
        return clone();
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
            PSObjectArray objArr = obj.toArray();
            if ((count != objArr.count) || (offset != objArr.offset)) {
                return false;
            }
            return (getId() == objArr.getId());
        } catch (PSErrorTypeCheck e) {
            return false;
        }
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
     * Returns a hash code value for the object.
     * 
     * @return Hash code of this object.
     */
    @Override
    public int hashCode() {
        return getArray().hashCode() + offset + count;
    }
    
    /**
     * Returns an object from this array.
     * 
     * @param index Index of the element to return.
     * 
     * @return Value of the specifiec element.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public PSObject get(final int index) throws PSErrorRangeCheck {
        if ((index < 0) || (index >= size())) {
            throw new PSErrorRangeCheck();
        }
        return getArray().get(index + offset);
    }
    
    /**
     * PostScript operator: get
     * Gets a single element from this object.
     * 
     * @param index The index.
     * 
     * @return the PS object
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorUndefined A PostScript undefined error occurred.
     */
    @Override
    public PSObject get(final PSObject index) throws PSErrorTypeCheck,
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
    @Override
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
     * 
     * @param index Index of the first element of the subarray
     * @param pCount Number of items in the subarray
     * 
     * @return Subarray
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public PSObjectArray getinterval(final int index, final int pCount)
            throws PSErrorRangeCheck {
        return new PSObjectArray(this, index, pCount);
    }
    
    /**
     * Gets the object at the requested index and returns the real value of that
     * object.
     * 
     * @param index Index of the object to get
     * 
     * @return the real
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double getReal(final int index)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        return get(index).toReal();
    }
    
    /**
     * Check whether a string is an array.
     * 
     * @param str String to check.
     * 
     * @return Returns true when the string is an array. Returns false
     * otherwise.
     */
    public static boolean isType(final String str) {
        int len = str.length();
        if ((str.charAt(0) == '{') && (str.charAt(len - 1) == '}')) {
            return true;
        } else {
            return ((str.charAt(0) == '[') && (str.charAt(len - 1) == ']'));
        }
    }

    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator.
     * 
     * @return String representation of this object.
     */
    @Override
    public String isis() {
        StringBuilder str = new StringBuilder();
        if (isLiteral()) {
            str.append("[ ");
        } else {
            str.append("{ ");
        }
        for (PSObject obj : this) {
            str.append(obj.isis() + " ");
        }
        if (isLiteral()) {
            str.append("]");
        } else {
            str.append("}");
        }
        return str.toString();
    }
    
    /**
     * Implements PostScript operate: length.
     * 
     * @return Length of this object
     */
    @Override
    public int length() {
        return size();
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * 
     * @param index Index or key for new value
     * @param value New value
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public void put(final PSObject index, final PSObject value)
            throws PSErrorRangeCheck, PSErrorTypeCheck {
        
        put(index.toInt(), value);
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * 
     * @param index Index or key for new value
     * @param value New value
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void put(final int index, final PSObject value)
            throws PSErrorRangeCheck {
        
        if ((index < 0) || (index >= size())) {
            throw new PSErrorRangeCheck();
        }
        getArray().set(index + offset, value);
    }
    
    /**
     * PostScript operator putinterval.
     * 
     * @param index Start index of subsequence
     * @param obj Subsequence
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    @Override
    public void putinterval(final int index, final PSObject obj)
            throws PSErrorTypeCheck, PSErrorRangeCheck {
        
        PSObjectArray array3 = obj.toArray();
        List<PSObject> list = getArray();
        int nr = array3.length();
        for (int i = 0; i < nr; i++) {
            list.set(offset + index + i, array3.get(i));
        }
    }

    /**
     * Remove an element from this array.
     * 
     * @param index Index of the element to remove.
     * 
     * @return Removed element.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public PSObject remove(final int index) throws PSErrorRangeCheck {
        if ((index < 0) || (index >= size())) {
            throw new PSErrorRangeCheck();
        }
        
        PSObject element = getArray().remove(index + offset);
        
        if (count != -1) {
            count--;
        }
        
        return element; 
    }
    
    /**
     * Replaces a value in this matrix.
     * 
     * @param index The index.
     * @param value The value.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    //TODO rename this method to putReal(..., ...)
    public void setReal(final int index, final double value)
            throws PSErrorRangeCheck {
        
        put(index, new PSObjectReal(value));
    }
    
    /**
     * Returns the number of elements in this array.
     * @return Number of items in this array.
     */
    public int size() {
        if (count == -1) {
            return getArray().size() - offset;
        } else {
            return count;
        }
    }
    
    /**
     * Convert this object to an array.
     * @return This array
     */
    @Override
    public PSObjectArray toArray() {
        return this;
    }
    
    /**
     * Converts this PostScript array to a double[] array with the requested
     * size.
     * 
     * @param k Required number of items in the array. If the actual number of
     * items is different a PSErrorRangeCheck is thrown.
     * 
     * @return Array with doubles
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] toDoubleArray(final int k) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        
        if (k != size()) {
            throw new PSErrorRangeCheck();
        }
        
        return toDoubleArray();
    }
    
    /**
     * Convert this PostScript array to a double[] array.
     * 
     * @return the array with doubles
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public double[] toDoubleArray() throws PSErrorTypeCheck {
        
        double[] newArray = new double[size()];
        List<PSObject> list = getArray();
        int startIndex = offset;
        int endIndex = startIndex + count;
        for (int i = startIndex; i < endIndex; i++) {
            newArray[i] = list.get(i).toReal();
        }
        
        return newArray;
    }
    
    /**
     * Convert this object to a matrix, if possible.
     * 
     * @return Matrix representation of this array
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObjectMatrix toMatrix() throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        return new PSObjectMatrix(this);
    }
    
    /**
     * Convert this object to an executable array (procedure), if possible.
     * 
     * @return the PS object array
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObjectArray toProc() throws PSErrorTypeCheck {
        if (isLiteral()) {
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
     * @param interp The interpreter. Not required, may be null.
     * 
     * @return List with one or more objects. The following are possible:
     * 1 object : { "false boolean" }
     * 2 objects: { "next token", "true boolean" }
     * 3 objects: { "remainder of this object", "next token",
     * "true boolean" }
     * 
     * @throws PSError A PostScript error occurred.
     */
    @Override
    public List<PSObject> token(final Interpreter interp) throws PSError {
        
        List<PSObject> list;
        int nr = size();
        if (nr == 0) {
            list = new ArrayList<PSObject>(1);
            list.add(0, new PSObjectBool(false));
        } else {
            list = new ArrayList<PSObject>(3);
            if (nr == 1) {
                PSObjectArray empty = new PSObjectArray(getVm());
                empty.setLiteral(isLiteral());
                list.add(0, empty);
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
    @Override
    public String type() {
        return "arraytype";
    }

    /**
     * Sets the array.
     * 
     * @param pArray the array to set
     * 
     * @throws PSErrorVMError A PostScript error: VMerror
     */
    void setArray(final List<PSObject> pArray) throws PSErrorVMError {
        setId(getVm().addArrayObj(pArray));
    }

    /**
     * @return the array
     */
    List<PSObject> getArray() {
        return getVm().getArrayObj(getId());
    }
}
