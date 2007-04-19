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
        try {
            for (int i = 0 ; i < procObjects.length() ; i++) {
                PSObject ob = procObjects.get(i);
                str.append(" " + ob.isis());
            }
        } catch (PSErrorRangeCheck e) {
            // This can never happen due to the for-loop
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
    
    /** Executes this object in the supplied interpreter */
    public void execute(Interpreter interp) throws Exception {
        List<PSObject> list = procObjects.getItemList();
        list.remove(0);  // remove first item (= number of object per item)
        interp.processObjects(list);
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
        return procObjects.getinterval(index, count);
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
    
    /** Convert this object to a procedure object, if possible. */
    public PSObjectProc toProc() {
        return this;
    }

    /**
     * Checks whether this object is executable
     * @return Returns true if this object is executable
     */
    public boolean isExecutable() {
        return true;
    }
    
    /**
     * Convert this object to a literal object
     * @return This object converted to a literal object
     */
    public PSObject toLiteral() {
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
