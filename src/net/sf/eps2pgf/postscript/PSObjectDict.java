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

package net.sf.eps2pgf.postscript;

import java.util.*;
import java.lang.*;
import net.sf.eps2pgf.postscript.errors.PSError;

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
    public PSObjectDict(int aCapacity) {
        capacity = aCapacity;
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
    
    /** Sets a key in the dictionary */
    public void setKey(PSObject key, PSObject value) throws PSError {
        setKey(key.toDictKey(), value);
    }
    
    /**
     * Set a key in the dictionary
     */
    public PSObjectString setKey(String key, String value) {
        PSObjectString psoValue = new PSObjectString(value);
        map.put(key, psoValue);
        return psoValue;
    }
    
    /** Sets a key in the dictionary */
    public void setKey(PSObject key, String value) throws PSError {
        setKey(key.toDictKey(), value);
    }
    
    /** Looks up a key in this dictionary */
    public PSObject lookup(String key) {
        return map.get(key);
    }
    
    /**
     * Looks up a key in this dictionary
     * @param key Dictionary key to look up.
     * @throws net.sf.eps2pgf.postscript.errors.PSError When key is not a valid dictionary key.
     * @return Value associated with key.
     */
    public PSObject lookup(PSObject key) throws PSError {
        return this.lookup(key.toDictKey());
    }
    
    /**
     * Checks whether this dictionary has a specific key. 
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
    
    /**
     * Checks whether this dictionary has a specific key. 
     */
    public boolean containsKey(PSObject key) throws PSError {
        return map.containsKey(key.toDictKey());
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
     * Returs all keys defined in this dictionary.
     * @return Set with all defined keys.
     */
    public Set<PSObject> keySet() {
        Set<String> strKeys = map.keySet();
        Iterator<String> strKeyIter = strKeys.iterator();
        Set<PSObject> keys = new HashSet<PSObject>();
        while (strKeyIter.hasNext()) {
            String key = strKeyIter.next();
            keys.add(new PSObjectName(key, true));
        }
        return keys;
    }
    
    /** Convert this object to a dictionary, if possible. */
    public PSObjectDict toDict() throws PSError {
        return this;
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws java.lang.CloneNotSupportedException Clone not supported.
     * @return Deep copy of this object.
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
