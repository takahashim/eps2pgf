/*
 * PSObjectDict.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript;

import java.lang.*;
import java.util.*;

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
     * 
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
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Invalid or incompatible object type(s) for copy operator
     */
    public PSObject copy(PSObject obj1) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        obj1.checkAccess(false, true, false);
        
        PSObjectDict dict1 = obj1.toDict();
        for (Map.Entry<String, PSObject> entry : dict1.map.entrySet()) {
            setKey(entry.getKey(), entry.getValue());
        }
        
        return this;
    }

    /**
     * Dumps the entire dictionary to stdout
     * 
     * @param preStr String that will be prepended to each line written to output.
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
     * @return Duplicate of this object
     */
    public PSObjectDict dup() {
        return this;
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * @return Value associated with key
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
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
     * @return Value associated with key
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key has invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     */
    public PSObject get(PSObject key) throws PSErrorUndefined,
            PSErrorTypeCheck, PSErrorInvalidAccess {
        return get(key.toDictKey());
    }
    
    /**
     * Returns a list with all items in object.
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This object does not have a list of items
     */
    public List<PSObject> getItemList() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        List<PSObject> lst = new ArrayList<PSObject>();
        lst.add(new PSObjectInt(2));
        for (Map.Entry<String, PSObject> entry : map.entrySet()) {
            lst.add(new PSObjectName(entry.getKey(), true));
            lst.add(entry.getValue());
        }
        return lst;
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
     * PostScript operator 'known'
     * @param key Key of the entry to check
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Key has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(PSObject key) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        return known(key.toDictKey());
    }
    
    /**
     * PostScript operator 'known'
     * @param key Key of the entry to check
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(String key) throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.containsKey(key);
    }
    
    /**
     * Get the number of elements
     * @return The number of entries in this dictionary.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     */
    public int length() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.size();
    }
    
    /**
     * Looks up a key in this dictionary
     * @param key Dictionary key to look up.
     * @return Value associated with key.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key is not the correct type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     */
    public PSObject lookup(PSObject key) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        return lookup(key.toDictKey());
    }
    
    /**
     * Looks up a key in this dictionary
     * @return Object associated with the key.
     * @param key Key of the entry to look up.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     */
    public PSObject lookup(String key) throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return map.get(key);
    }
    
    /**
     * PostScript operator 'maxlength'
     * @return maximum of the 'capacity' and the actual number of entries
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No read access to this object
     */
    public int maxlength() throws PSErrorInvalidAccess {
        checkAccess(false, true, false);
        return Math.max(capacity, length());
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
     * @param key Key or key for new value
     * @param value New value
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key does not have a valid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
     */
    public void put(PSObject key, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        setKey(key, value);
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
     */
    public void setKey(String key, PSObject value) throws PSErrorInvalidAccess {
        checkAccess(false, false, true);
        map.put(key, value);
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Key has not a valid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
     */
    public void setKey(PSObject key, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        setKey(key.toDictKey(), value);
    }
    
    /**
     * Set a key in the dictionary
     * @return Created PSObjectString.
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary item. Will be converted to a PSObjectString.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
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
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Key has an invalid type
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
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
     * @return Font dictionary object
     */
    public PSObjectFont toFont() {
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
     * PostScript operator 'undef'
     * @param key Key of the entry to remove
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Type of key is incorrect
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidAccess No write access to this object
     */
    public void undef(PSObject key) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        checkAccess(false, false, true);
        String keyStr = key.toDictKey();
        map.remove(keyStr);
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     * @return Returns true when there is write access to this object, returns
     * false otherwise
     */
    public boolean wcheck() {
        return (access == ACCESS_UNLIMITED);
    }
}
