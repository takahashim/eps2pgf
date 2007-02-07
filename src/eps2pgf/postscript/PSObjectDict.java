/*
 * PSObjectDict.java
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

package eps2pgf.postscript;

import java.util.*;
import java.lang.*;

import eps2pgf.postscript.errors.*;

/**
 *
 * @author Paul Wagenaars
 */
public class PSObjectDict extends PSObject {
    private HashMap<String, PSObject> map = new HashMap<String, PSObject>();
    
    int capacity;
    
    /** Creates a new instance of PSObjectDict */
    public PSObjectDict() {
        capacity = Integer.MAX_VALUE;
    }
    
    /** Creates a new instance of PSObjectDict */
    public PSObjectDict(PSObject obj) {
        if (obj instanceof PSObjectReal) {
            double roundedVal = Math.round(((PSObjectReal)obj).value);
            capacity = (int)roundedVal;
        } else if (obj instanceof PSObjectInt) {
            capacity = ((PSObjectInt)obj).value;
        }
    }
    
    /** Convert this object to string */
    public String toString() {
        return "Dict: (" + length() + " items)";
    }
    
    /** Dumps the entire dictionary to stdout */
    public void dumpFull(String preStr) throws PSError {
        Set<String> keys = map.keySet();
        for (String key: keys) {
            System.out.println(preStr + key + " -> " + map.get(key).isis());
        }
    }
    
    /** Sets a key in the dictionary */
    public void setKey(String key, PSObject value) {
        if (value instanceof PSObjectProc) {
            value.isLiteral = false;
        }
        map.put(key, value);
    }
    
    /** Looks up a key in this dictionary */
    public PSObject lookup(String key) {
        return map.get(key);
    }
    
    /** Get the number of elements */
    public int length() {
        return map.size();
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "-dict(" + map.size() + ")-";
    }
    
    /**
     * Creates a deep copy of this object.
     */
    public PSObjectDict clone() throws CloneNotSupportedException {
        PSObjectDict newDict = new PSObjectDict();
        newDict.capacity = capacity;
        for(String key : map.keySet()) {
            newDict.setKey(new String(key), map.get(key).clone());
        }
        return newDict;
    }
}
