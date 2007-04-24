/*
 * PSObjectProc.java
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

import java.awt.event.ItemEvent;
import java.io.*;
import java.util.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Procedure PostScript object.
 * @author Paul Wagenaars
 */
public class PSObjectProc extends PSObject {
    private PSObjectArray procObjects;
    
    /**
     * Creates a new instance of PSObjectProc
     * @param str String representing a valid procedure.
     * @throws java.io.IOException Unable to read the string from a StringBuffer.  
     */
    public PSObjectProc(String str) throws IOException {
        str = str.substring(1,str.length()-1);
        
        StringReader strReader = new StringReader(str);
        
        procObjects = new PSObjectArray(Parser.convert(strReader));
        
        isLiteral = false;
    }
    
    /**
     * Create a new instance of PSObjectProc with the supplied array as elements.
     * @param array Array with elements for the new procedure
     * @throws PSErrorTypeCheck Supplied argument is not an array.
     */
    public PSObjectProc(PSObject array) throws PSErrorTypeCheck {
        procObjects = array.toArray();
        
        isLiteral = false;
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
        } else {
            return false;
        }
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        StringBuilder str = new StringBuilder();
        str.append("{");
        for (PSObject ob : procObjects) {
            str.append(" " + ob.isis());
        }
        str.append(" }");
        return str.toString();
    }
    
    /** Replace executable name objects with their values */
    public PSObject bind(Interpreter interp) throws PSErrorTypeCheck {
        try {
            for (int i = 0 ; i < procObjects.length() ; i++) {
                PSObject obj = procObjects.get(i);
                procObjects.put(i, obj.bind(interp));
            }
        } catch (PSErrorRangeCheck e) {
            // this can never happen due to the for-loop
        }
        return this;
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     */
    public PSObject copy(PSObject obj1) throws PSErrorRangeCheck, PSErrorTypeCheck {
        PSObject subarray = procObjects.copy(obj1);
        return new PSObjectProc(subarray);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        try {
            PSObjectArray objArr = obj.toArray();
            return (procObjects == objArr);
        } catch (PSErrorTypeCheck e) {
            return false;
        }
    }
    
    /** Executes this object in the supplied interpreter */
    public void execute(Interpreter interp) throws Exception {
        List<PSObject> list = procObjects.getItemList();
        list.remove(0);  // remove first item (= number of object per item)
        interp.processObjects(list);
    }
    
    /**
     * PostScript operator 'executeonly'. Set access attribute to executeonly.
     */
    public void executeonly() throws PSErrorTypeCheck, PSErrorInvalidAccess {
        if (access == ACCESS_NONE) {
            throw new PSErrorInvalidAccess();
        }
        access = ACCESS_EXECUTEONLY;
    }
    
    /**
     * PostScript operator: get
     * Gets a single element from this object.
     */
    public PSObject get(PSObject index) throws PSErrorTypeCheck,
            PSErrorRangeCheck, PSErrorUndefined {
        return procObjects.get(index);
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
        PSObject subseq = procObjects.getinterval(index, count);
        return new PSObjectProc(subseq);
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() throws PSErrorTypeCheck {
        return procObjects.getItemList();
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() {
        return procObjects.length();
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
     */
    public void put(PSObject index, PSObject value) throws PSErrorRangeCheck,
            PSErrorTypeCheck {
        procObjects.put(index, value);
    }
    
    /**
     * PostScript operator putinterval
     * @param index Start index of subsequence
     * @param obj Subsequence
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Can not 'putinterval' anything in this object type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Index or (index+length) out of bounds
     */
    public void putinterval(int index, PSObject obj) throws PSErrorTypeCheck, PSErrorRangeCheck {
        procObjects.putinterval(index, obj);
    }
    
    /**
     * Convert this object to an array.
     * @return This array
     */
    public PSObjectArray toArray() {
        return procObjects;
    }
    
    /** Convert this object to a procedure object, if possible. */
    public PSObjectProc toProc() {
        return this;
    }

    /**
     * Convert this object to a literal object
     * @return This object converted to a literal object
     */
    public PSObject cvlit() {
        return procObjects;
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "arraytype";
    }
}
