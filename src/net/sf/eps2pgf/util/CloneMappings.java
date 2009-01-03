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

package net.sf.eps2pgf.util;

import java.util.ArrayList;

import net.sf.eps2pgf.ProgramError;

//TODO maybe I can make the list sorted to increase lookup speed drastically

/**
 * This class implements a Map object, but instead of using .equals() method for
 * comparisons it always uses ==.
 */
public class CloneMappings {
    //TODO Use WeakReference to store the elements so that they can be removed
    //by the garbage collector.
    /** List with the original objects. */
    private ArrayList<Object> origObjs = new ArrayList<Object>();
    
    /** List with the cloned objects corresponding to the original objects. */
    private ArrayList<Object> clonedObjs = new ArrayList<Object>();
    
    /**
     * Construct a new empty object map.
     */
    public CloneMappings() {
        /* empty block */
    }

    /**
     * Returns the index of the specified key. Or -1 if the key is not
     * specified.
     * 
     * @param origObj The key for which to check.
     * 
     * @return index of key, or -1 if key isn't specified.
     */
    private int getIndex(final Object origObj) {
        // Simply search the entire array
        //TODO cache result from last getIndex()
        for (int i = origObjs.size() - 1; i >= 0; i--) {
            if (origObj == origObjs.get(i)) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     * 
     * @param origObj The key for which to check.
     * 
     * @return true, if contains key
     */
    public boolean containsKey(final Object origObj) {
        //TODO rename this method to containOrigObj(...), or something similar
        return (getIndex(origObj) >= 0);
    }

    /**
     * Returns the value to which this map maps the specified key. Returns null
     * if the map contains no mapping for this key.
     * 
     * @param key The key to get.
     * 
     * @return the value to which this map maps the specified key, or null if
     * the map contains no mapping for this key.
     */
    public Object get(final Object key) {
        int index = getIndex(key);
        if (index >= 0) {
            return clonedObjs.get(index);
        } else {
            return null;
        }
    }

    /**
     * Add a key-value pair. If the key is already defined we check whether the
     * associated value is the same as the new value. If so, nothing happens. If
     * they are not equal a ProgramError is thrown.
     * 
     * @param origObj The key.
     * @param clonedObj The value.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void add(final Object origObj, final Object clonedObj)
            throws ProgramError {
        
        int index = getIndex(origObj);
        if (index >= 0) {
            // Due to chained super.clone() calls the same object is add
            // multiple times, right after each other. The cloned top class is
            // added first and the subclass is added last. Therefore, we replace
            // if index is the last index of the array. Otherwise, we throw an
            // error because the same original object is being defined multiple
            // times.
            //TODO can I speed up this check?
            if (false) {
                throw new ProgramError("Defining pair with same origObj but"
                        + " the clonedObjs have a different hashCode. ("
                        + index + "/" + origObjs.size() + ") " + origObj + "->"
                        + clonedObj + "/" + clonedObjs.get(index));
            } else {
                clonedObjs.set(index, clonedObj);
            }
        } else {
            origObjs.add(origObj);
            clonedObjs.add(origObj);
        }
    }
}
