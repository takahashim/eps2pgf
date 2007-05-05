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
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represent PostScript dictionary.
 * @author Paul Wagenaars
 */
public class PSObjectDict extends PSObject {
    private HashMap<String, PSObject> map = new HashMap<String, PSObject>();
    
    int capacity;
    
    /** Creates a new instance of PSObjectDict */
    public PSObjectDict() {
        capacity = Integer.MAX_VALUE;
    }
    
    /**
     * Creates a new instance of PSObjectDict
     * @param aCapacity Maximum number of items that can be stored in this dictionary.
     * This value has no effect. The dictionary size is unlimited.
     */
    public PSObjectDict(int aCapacity) {
        capacity = aCapacity;
    }
    
    /**
     * Creates a deep copy of this object.
     * @throws java.lang.CloneNotSupportedException Clone not supported.
     * @return Deep copy of this object.
     */
    public PSObjectDict clone() {
        PSObjectDict newDict = new PSObjectDict();
        newDict.capacity = capacity;
        for(String key : map.keySet()) {
            newDict.map.put(new String(key), map.get(key).clone());
        }
        return newDict;
    }

    /**
     * Checks whether this dictionary has a specific key.
     * @return True when the entry exists, false otherwise.
     * @throws net.sf.eps2pgf.postscript.errors.PSError Unable to check the supplied key.
     * @param key Search for an entry with this key.
     */
    public boolean containsKey(PSObject key) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.containsKey(key.toDictKey());
    }
    
    /**
     * Checks whether this dictionary has a specific key.
     * @param key Key of the entry to check.
     * @return True when the entry exists, false otherwise.
     */
    public boolean containsKey(String key) throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.containsKey(key);
    }
    
    /**
     * Dumps the entire dictionary to stdout
     * @param preStr String that will be prepended to each line written to output.
     * @throws net.sf.eps2pgf.postscript.errors.PSError There was an error reading from this dictionary.
     */
    public void dumpFull(String preStr) {
        Set<String> keys = map.keySet();
        for (String key: keys) {
            System.out.println(preStr + key + " -> " + map.get(key).isis());
        }
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectDict dup() {
        return this;
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     * @return Value associated with key
     */
    public PSObject get(String key) throws PSErrorUndefined, PSErrorInvalidAccess {
        checkAccess(false, true, false);
        PSObject value = lookup(key);
        if (value == null) {
            throw new PSErrorUndefined();
        }
        return value;
    }
    
    /**
     * PostScript operator: get
     * Return value associated with key.
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     * @return Value associated with key
     */
    public PSObject get(PSObject key) throws PSErrorUndefined,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        return get(key.toDictKey());
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return String representing this object.
     */
    public String isis() {
        return "-dict(" + map.size() + ")-";
    }
    
    /**
     * Returs all keys defined in this dictionary.
     * @return Set with all defined keys.
     */
/*    public Set<PSObject> keySet() {
        Set<String> strKeys = map.keySet();
        Iterator<String> strKeyIter = strKeys.iterator();
        Set<PSObject> keys = new HashSet<PSObject>();
        while (strKeyIter.hasNext()) {
            String key = strKeyIter.next();
            keys.add(new PSObjectName(key, true));
        }
        return keys;
    }*/
    
    /**
     * Get the number of elements
     * @return The number of entries in this dictionary.
     */
    public int length() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.size();
    }
    
    /**
     * Looks up a key in this dictionary
     * @param key Dictionary key to look up.
     * @throws net.sf.eps2pgf.postscript.errors.PSError When key is not a valid dictionary key.
     * @return Value associated with key.
     */
    public PSObject lookup(PSObject key) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        return lookup(key.toDictKey());
    }
    
    /**
     * Looks up a key in this dictionary
     * @param key Key of the entry to look up.
     * @return Object associated with the key.
     */
    public PSObject lookup(String key) throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.get(key);
    }
    
    /**
     * PostScript operator: 'noaccess'
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess Access attribute does no allow changes to this object
     */
    public void noaccess() throws PSErrorInvalidAccess {
        access = ACCESS_NONE;
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * Same as setKey() method
     * @param index Index or key for new value
     * @param value New value
     */
    public void put(PSObject index, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        setKey(index, value);
    }

    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     * @return Returns 'true' when 'access' attribute is set to 'unlimited' or
     * 'readonly'. Returns false otherwise.
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess Access attribute does no allow changes to this object
     */
    public void readonly() throws PSErrorInvalidAccess {
        checkAccess(true, true, false);
        access = ACCESS_READONLY;
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(String key, PSObject value) throws PSErrorInvalidAccess {
        checkAccess(false, false, true);
        map.put(key, value);
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     * @throws net.sf.eps2pgf.postscript.errors.PSError There was an error creating the new dictionary entry.
     */
    public void setKey(PSObject key, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        setKey(key.toDictKey(), value);
    }
    
    /**
     * Set a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary item. Will be converted to a PSObjectString.
     * @return Created PSObjectString.
     */
    public PSObjectString setKey(String key, String value) throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        PSObjectString psoValue = new PSObjectString(value);
        map.put(key, psoValue);
        return psoValue;
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     * @throws net.sf.eps2pgf.postscript.errors.PSError There was an error creating the new entry.
     */
    public void setKey(PSObject key, String value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        setKey(key.toDictKey(), value);
    }
    
    /**
     * Convert this object to a dictionary, if possible.
     * @return This dictionary.
     */
    public PSObjectDict toDict() {
        return this;
    }
    
    /**
     * Convert this object to a font.
     */
    public PSObjectFont toFont() throws PSErrorTypeCheck {
        return new PSObjectFont(this);
    }
    
    /**
     * Convert this object to string
     * @return Human-readable string representation of this object.
     */
    public String toString() {
        return "Dict: (" + map.size() + " items)";
    }
    
    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "dicttype";
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     */
    public boolean wcheck() {
        return (access == ACCESS_UNLIMITED);
    }
}
