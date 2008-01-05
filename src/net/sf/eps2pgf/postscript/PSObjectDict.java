/*
 * PSObjectDict.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;

/**
 * Represent PostScript dictionary.
 * @author Paul Wagenaars
 */
public class PSObjectDict extends PSObject {
    private HashMap<PSObject, PSObject> map = new HashMap<PSObject, PSObject>();
    
    int capacity;
    
    /** Creates a new instance of PSObjectDict */
    public PSObjectDict() {
        capacity = 0;
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
    @Override
    public PSObjectDict clone() {
        PSObjectDict copy = (PSObjectDict) super.clone();
        copy.map = new HashMap<PSObject, PSObject>();
        for (PSObject key : map.keySet()) {
            copy.map.put(key.clone(), map.get(key).clone());
        }        
        return copy;
    }

    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * @param obj1 Copy values from obj1
     * @return Returns subsequence of this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Invalid or incompatible object type(s) for copy operator
     */
    public PSObject copy(PSObject obj1) throws PSErrorTypeCheck {
        PSObjectDict dict1 = obj1.toDict();
        for (Map.Entry<PSObject, PSObject> entry : dict1.map.entrySet()) {
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
        Set<PSObject> keys = map.keySet();
        for (PSObject key: keys) {
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
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     * 
     * @param obj The object to compare to.
     * 
     * @return True, if equal.
     */
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
    public int hashCode() {
        return map.hashCode();
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * @return Value associated with key
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     */
    public PSObject get(PSObject key) throws PSErrorUndefined {
        PSObject value = lookup(key);
        if (value == null) {
            throw new PSErrorUndefined("Key (" + key.isis() + ") not defined in dictionary.");
        }
        return value;
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * @return Value associated with key
     * @param key Key for which the associated value will be returned
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     */
    public PSObject get(String key) throws PSErrorUndefined {
        return this.get(new PSObjectName(key, true));
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
        List<PSObject> lst = new ArrayList<PSObject>();
        lst.add(new PSObjectInt(2));
        for (Map.Entry<PSObject, PSObject> entry : map.entrySet()) {
            lst.add(entry.getKey());
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
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(PSObject key) {
        return map.containsKey(key);
    }
    
    /**
     * PostScript operator 'known'
     * @param key Key of the entry to check
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(String key) {
        return map.containsKey(new PSObjectName(key, true));
    }
    
    /**
     * Get the number of elements
     * @return The number of entries in this dictionary.
     */
    public int length() {
        return map.size();
    }
    
    /**
     * Looks up a key in this dictionary
     * @return Object associated with the key, <code>null</code> if no object is associated with this key.
     * @param key Key of the entry to look up.
     */
    public PSObject lookup(PSObject key) {
        return map.get(key);
    }
    
    /**
     * Looks up a key in this dictionary
     * @return Object associated with the key, <code>null</code> if no object is associated with this key.
     * @param key Key of the entry to look up.
     */
    public PSObject lookup(String key) {
        return map.get(new PSObjectName(key, true));
    }
    
    /**
     * PostScript operator 'maxlength'
     * @return maximum of the 'capacity' and the actual number of entries
     */
    public int maxlength() {
        return Math.max(capacity, length());
    }
    
    /**
     * PostScript operator: 'noaccess'
     */
    public void noaccess() {
        setAccess(Access.NONE);
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * Same as setKey() method
     * @param key Key or key for new value
     * @param value New value
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key does not have a valid type
     */
    public void put(PSObject key, PSObject value) throws PSErrorTypeCheck {
        this.setKey(key, value);
    }

    /**
     * PostScript operator 'rcheck'. Checks whether the access attribute is
     * 'unlimited' or 'readonly'.
     * @return Returns 'true' when 'access' attribute is set to 'unlimited' or
     * 'readonly'. Returns false otherwise.
     */
    public boolean rcheck() {
        if ( (getAccess() == Access.UNLIMITED)
                || (getAccess() == Access.READONLY) ) {
            
            return true;
        } else {
            return false;
        }
    }

    /**
     * PostScript operator: 'readonly'.
     */
    public void readonly() {
        //TODO: can this on be removeed. Isn't is exactly the same as fot the
        // generic PSObject.
        setAccess(Access.READONLY);
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(PSObject key, PSObject value) {
    	if (key instanceof PSObjectString) {
    		key = new PSObjectName(((PSObjectString)key).toString(), true);
    	}
        map.put(key, value);
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(String key, PSObject value) {
        map.put(new PSObjectName(key, true), value);
    }
    
    /**
     * Sets a key in the dictionary
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(String key, String value) {
        map.put(new PSObjectName(key, true), new PSObjectString(value));
    }
    
    /**
     * Set a key in the dictionary
     * @return Created PSObjectString.
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary item. Will be converted to a PSObjectString.
     */
    public PSObjectString setKey(PSObject key, String value) {
        PSObjectString psoValue = new PSObjectString(value);
        map.put(key, psoValue);
        return psoValue;
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
     */
    public void undef(PSObject key) throws PSErrorTypeCheck {
        map.remove(key);
    }

    /**
     * PostScript operator 'wcheck'. Checks whether the access attribute is
     * 'unlimited'.
     * @return Returns true when there is write access to this object, returns
     * false otherwise
     */
    public boolean wcheck() {
        return (getAccess() == Access.UNLIMITED);
    }
}
