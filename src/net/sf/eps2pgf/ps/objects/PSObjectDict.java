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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;

/**
 * Represent PostScript dictionary.
 * @author Paul Wagenaars
 */
public class PSObjectDict extends PSObjectComposite implements Cloneable {
    
    /** Maximum number of entries in this dictionary. (this value is
     * informational only). */
    private int capacity;
    
    /**
     * Creates a new instance of PSObjectDict.
     * 
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public PSObjectDict(final VM virtualMemory) throws PSErrorVMError {
        this(-1, virtualMemory);
    }
    
    /**
     * Creates a new instance of PSObjectDict.
     * 
     * @param pCapacity Maximum number of items that can be stored in this
     * dictionary. This value has no effect. The dictionary size is unlimited.
     * @param virtualMemory The VM manager.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public PSObjectDict(final int pCapacity, final VM virtualMemory)
            throws PSErrorVMError {
        
        super(virtualMemory);
        setMap(new HashMap<PSObject, PSObject>());
        capacity = pCapacity;
    }
    
    /**
     * Creates a new PSObjectDict. The new dictionary shares its hash map with
     * the passed dictionary.
     * 
     * @param dict The new dictionary will use the same hash map as this
     * dictionary.
     */
    public PSObjectDict(final PSObjectDict dict) {
        super(dict.getVm(), dict.getId());
        capacity = dict.capacity;
        copyCommonAttributes(dict);
    }
    
    /**
     * PostScript operator copy. Copies values from obj1 to this object.
     * 
     * @param obj1 Copy values from obj1
     * 
     * @return Returns subsequence of this object
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObject copy(final PSObject obj1) throws PSErrorTypeCheck {
        PSObjectDict dict1 = obj1.toDict();
        Map<PSObject, PSObject> map = getMap();
        for (Map.Entry<PSObject, PSObject> entry : dict1.getMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        
        return this;
    }
    
    /**
     * Create a deep copy of this dictionary object.
     * 
     * @return The copy of this object.
     */
    @Override
    public PSObjectDict clone() {
        return (PSObjectDict) super.clone();
    }

    /**
     * Dumps the entire dictionary to stdout.
     * 
     * @param pre String that will be prepended to each line written to
     * output.
     */
    public void dumpFull(final String pre) {
        Set<PSObject> keys = getMap().keySet();
        for (PSObject key : keys) {
            System.out.println(pre + key + " -> " + getMap().get(key).isis());
        }
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object
     */
    @Override
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
        return getMap().hashCode();
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * 
     * @param key Key for which the associated value will be returned
     * 
     * @return Value associated with key
     * 
     * @throws PSErrorUndefined The PostScript undefined error.
     */
    @Override
    public PSObject get(final PSObject key) throws PSErrorUndefined {
        PSObject value = lookup(key);
        if (value == null) {
            throw new PSErrorUndefined("Key (" + key.isis()
                    + ") not defined in dictionary.");
        }
        return value;
    }
    
    /**
     * Return value associated with key. Same as lookup, except that a
     * PSErrorRangeCheck is thrown when the item doesn't exist.
     * 
     * @param key Key for which the associated value will be returned
     * 
     * @return Value associated with key
     * 
     * @throws PSErrorUndefined The PostScript error "undefined".
     */
    public PSObject get(final String key) throws PSErrorUndefined {
        return get(new PSObjectName(key, true));
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
        List<PSObject> lst = new ArrayList<PSObject>();
        lst.add(new PSObjectInt(2));
        for (Map.Entry<PSObject, PSObject> entry : getMap().entrySet()) {
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
    @Override
    public String isis() {
        return "-dict(" + getMap().size() + ")-";
    }
    
    /**
     * PostScript operator 'known'.
     * 
     * @param key Key of the entry to check
     * 
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(final PSObject key) {
        return getMap().containsKey(key);
    }
    
    /**
     * PostScript operator 'known'.
     * 
     * @param key Key of the entry to check
     * 
     * @return Returns true when the key is known, returns false otherwise
     */
    public boolean known(final String key) {
        return getMap().containsKey(new PSObjectName(key, true));
    }
    
    /**
     * Get the number of elements.
     * 
     * @return The number of entries in this dictionary.
     */
    @Override
    public int length() {
        return getMap().size();
    }
    
    /**
     * Looks up a key in this dictionary.
     * 
     * @return Object associated with the key, <code>null</code> if no object is
     * associated with this key.
     * 
     * @param key Key of the entry to look up.
     */
    public PSObject lookup(final PSObject key) {
        return getMap().get(key);
    }
    
    /**
     * Looks up a key in this dictionary.
     * 
     * @return Object associated with the key, <code>null</code> if no object is
     * associated with this key.
     * 
     * @param key Key of the entry to look up.
     */
    public PSObject lookup(final String key) {
        return getMap().get(new PSObjectName(key, true));
    }
    
    /**
     * PostScript operator 'maxlength'.
     * 
     * @return maximum of the 'capacity' and the actual number of entries
     */
    public int maxlength() {
        return Math.max(capacity, length());
    }
    
    /**
     * PostScript operator put. Replace a single value in this object.
     * Same as setKey() method
     * 
     * @param key Key or key for new value
     * @param value New value
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     * @throws PSErrorInvalidAccess A PostScript invalidaccess error occurred.
     */
    @Override
    public void put(final PSObject key, final PSObject value)
            throws PSErrorTypeCheck, PSErrorInvalidAccess {
        
        if (gcheck() && !value.gcheck()) {
            throw new PSErrorInvalidAccess();
        }
        
        setKey(key, value);
    }

    /**
     * Sets a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(final PSObject key, final PSObject value) {
        if (key instanceof PSObjectString) {
            PSObjectString keyStr = (PSObjectString) key;
            getMap().put(new PSObjectName(keyStr.toString(), true), value);
        } else {
            getMap().put(key, value);
        }
    }
    
    /**
     * Sets a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(final String key, final PSObject value) {
        setKey(new PSObjectName(key, true), value);
    }
    
    /**
     * Sets a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public void setKey(final String key, final String value)
            throws PSErrorVMError {
        
        setKey(new PSObjectName(key, true), new PSObjectString(value, getVm()));
    }
    
    /**
     * Sets a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(final String key, final int value) {
        setKey(new PSObjectName(key, true), new PSObjectInt(value));
    }
    
    /**
     * Sets a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary entry.
     */
    public void setKey(final String key, final boolean value) {
        setKey(new PSObjectName(key, true), new PSObjectBool(value));
    }
    
    /**
     * Set a key in the dictionary.
     * 
     * @param key Key of the new dictionary entry.
     * @param value Value of the new dictionary item. Will be converted to a
     * PSObjectString.
     * 
     * @return Created PSObjectString.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    //TODO is this method used anywhere?
    public PSObjectString setKey(final PSObject key, final String value)
            throws PSErrorVMError {
        
        PSObjectString psoValue = new PSObjectString(value, getVm());
        getMap().put(key, psoValue);
        return psoValue;
    }
    
    /**
     * Convert this object to a dictionary, if possible.
     * @return This dictionary.
     */
    @Override
    public PSObjectDict toDict() {
        return this;
    }
    
    /**
     * Convert this object to a font.
     * @return Font dictionary object
     */
    @Override
    public PSObjectFont toFont() {
        return new PSObjectFont(this);
    }
    
    /**
     * Convert this object to string.
     * 
     * @return Human-readable string representation of this object.
     */
    @Override
    public String toString() {
        return "Dict: (" + getMap().size() + " items)";
    }
    
    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "dicttype";
    }
    
    /**
     * PostScript operator 'undef'.
     * 
     * @param key Key of the entry to remove
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void undef(final PSObject key) throws PSErrorTypeCheck {
        getMap().remove(key);
    }
    
    /**
     * Register a new map to the VM manager.
     * 
     * @param map The map.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    private void setMap(Map<PSObject, PSObject> map) throws PSErrorVMError {
        setId(getVm().addDictObj(map));
    }

    /**
     * Gets the map from the VM manager.
     * 
     * @return map The map.
     */
    private Map<PSObject, PSObject> getMap() {
        return getVm().getDictObj(getId());
    }
    
}
