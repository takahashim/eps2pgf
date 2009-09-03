/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

package net.sf.eps2pgf.ps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObject;

/**
 * Virtual Memory (VM) manager.
 * 
 * @author Paul Wagenaars
 *
 */
public class VM implements Cloneable {
    /** Ever increasing counter for assigning object ID counts. */
    private static int idCounter = 1;
    
    /**
     * Current VM allocation mode. When is global mode (=true) objects are
     * assigned negative IDs, and when in local mode (=false) objects are
     * assigned positive IDs.
     */
    private boolean isGlobal = true;
    
    /** Map that holds all composite values of local array objects. */
    private WeakHashMap<ObjectId, List<PSObject>> arraysLocal =
        new WeakHashMap<ObjectId, List<PSObject>>();
    
    /** Map that holds all composite values of global array objects. */
    private final WeakHashMap<ObjectId, List<PSObject>> arraysGlobal =
        new WeakHashMap<ObjectId, List<PSObject>>();
    
    /** Map that holds all composite values of local dictionary objects. */
    private WeakHashMap<ObjectId, Map<PSObject, PSObject>> dictsLocal =
        new WeakHashMap<ObjectId, Map<PSObject, PSObject>>();
    
    /** Map that holds all composite values of global dictionary objects. */
    private final WeakHashMap<ObjectId, Map<PSObject, PSObject>> dictsGlobal =
        new WeakHashMap<ObjectId, Map<PSObject, PSObject>>();
    
    /** Map that holds all composite values of local string objects. */
    private final WeakHashMap<ObjectId, StringBuilder> stringsLocal =
        new WeakHashMap<ObjectId, StringBuilder>();

    /** Map that holds all composite values of global string objects. */
    private final WeakHashMap<ObjectId, StringBuilder> stringsGlobal =
        new WeakHashMap<ObjectId, StringBuilder>();
    
    /** Map that holds all composite values of save objects. Note: all save
     * objects are added to the local VM. */
    private WeakHashMap<ObjectId, VM> savesLocal =
        new WeakHashMap<ObjectId, VM>();
    
    /**
     * Construct a new virtual memory manager.
     */
    public VM() {
        /* empty block */
    }
    
    /**
     * Adds a new shared array object.
     * 
     * @param obj The array to be added.
     * 
     * @return The object ID of the added shared array object.
     * 
     * @throws PSErrorVMError PostScript error: VMerror.
     */
    public ObjectId addArrayObj(final List<PSObject> obj)
            throws PSErrorVMError {
        
        ObjectId id = new ObjectId();
        if (id.isInGlobalVm()) {
            arraysGlobal.put(id, obj);
        } else {
            arraysLocal.put(id, obj);
        }
        return id;
    }
    
    /**
     * Adds a new dictionary value to this VM.
     * 
     * @param obj The dictionary to add.
     * 
     * @return The object id assigned to the added object.
     * 
     * @throws PSErrorVMError PostScript error: VMerror
     */
    public ObjectId addDictObj(final Map<PSObject, PSObject> obj)
            throws PSErrorVMError {
        
        ObjectId id = new ObjectId();
        if (id.isInGlobalVm()) {
            dictsGlobal.put(id, obj);
        } else {
            dictsLocal.put(id, obj);
        }
        return id;
    }
    
    /**
     * Adds a new save value to the VM.
     * 
     * @param saveObj The save object.
     * 
     * @return The object ID assigned to the added object.
     * 
     * @throws PSErrorVMError PostScript error: VMerror.
     */
    public ObjectId addSaveObj(final VM saveObj) throws PSErrorVMError {
        ObjectId id = new ObjectId();
        // save object are always stored in local VM
        id.setInGlobalVM(false);
        savesLocal.put(id, saveObj);
        return id;
    }
    
    /**
     * Adds a new shared string value to this VM.
     * 
     * @param obj The shared string object.
     * 
     * @return The object id assigned to the added object.
     * 
     * @throws PSErrorVMError PostScript error: VMerror.
     */
    public ObjectId addStringObj(final StringBuilder obj)
            throws PSErrorVMError {
        
        ObjectId id = new ObjectId();
        if (id.isInGlobalVm()) {
            stringsGlobal.put(id, obj);
        } else {
            stringsLocal.put(id, obj);
        }
        return id;
    }
    
    /**
     * Creates a snapshot of this VM.
     * 
     * @return The created snapshot of this VM.
     */
    @Override
    public VM clone() {
        VM copy;
        try {
            copy = (VM) super.clone();
        } catch (CloneNotSupportedException e) {
            copy = null;
        }
        
        copy.arraysLocal = new WeakHashMap<ObjectId, List<PSObject>>();
        for (Map.Entry<ObjectId, List<PSObject>> e : arraysLocal.entrySet()) {
            List<PSObject> copyValue = new ArrayList<PSObject>(e.getValue());
            copy.arraysLocal.put(e.getKey(), copyValue);
        }
        
        copy.dictsLocal = new WeakHashMap<ObjectId, Map<PSObject, PSObject>>();
        for (Map.Entry<ObjectId, Map<PSObject, PSObject>> e
                : dictsLocal.entrySet()) {
            
            Map<PSObject, PSObject> copyValue =
                new HashMap<PSObject, PSObject>(e.getValue());
            copy.dictsLocal.put(e.getKey(), copyValue);
        }
        
        // The strings are not copied (see PostScript manual)
        
        return copy;
    }
    
    /**
     * Gets the current VM allocation mode.
     * 
     * @return True = in global allocation mode, false = in local mode.
     */
    public boolean currentGlobal() {
        return isGlobal;
    }
    
    /**
     * Gets the current value of the object ID counter. The counter is not
     * increased by calling this method.
     * 
     * @return The current value of the ID counter.
     */
    public static int currentIdCounter() {
        return idCounter;
    }
    
    /**
     * Gets the array object that is associated with a certain id.
     * 
     * @param id The id.
     * 
     * @return The requested array object.
     */
    public List<PSObject> getArrayObj(final ObjectId id) {
        if (id.isInGlobalVm()) {
            return arraysGlobal.get(id);
        } else {
            return arraysLocal.get(id);
        }
    }
    
    /**
     * Gets the dictionary object that is associated with a certain id.
     * 
     * @param id The id.
     * 
     * @return The requested dictionary object.
     */
    public Map<PSObject, PSObject> getDictObj(final ObjectId id) {
        if (id.isInGlobalVm()) {
            return dictsGlobal.get(id);
        } else {
            return dictsLocal.get(id);
        }
    }
    
    /**
     * Gets the save object that is associated with a certain ID.
     * 
     * @param id The ID.
     * 
     * @return The requested save object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public VM getSaveObj(final ObjectId id) throws ProgramError {
        if (id.isInGlobalVm()) {
            throw new ProgramError("Save object with global object ID.");
        } else {
            return savesLocal.get(id);
        }
    }
    
    /**
     * Gets the string object that is associated with a certain ID.
     * 
     * @param id The ID.
     * 
     * @return The requested string object.
     */
    public StringBuilder getStringObj(final ObjectId id) {
        if (id.isInGlobalVm()) {
            return stringsGlobal.get(id);
        } else {
            return stringsLocal.get(id);
        }
    }
    
    /**
     * Restore this VM to the state of a previously made snapshot.
     * 
     * @param snapshot The snapshot.
     */
    public void restoreFromSnapshot(final VM snapshot) {
        isGlobal = snapshot.isGlobal;
        
        arraysLocal = snapshot.arraysLocal;
        dictsLocal = snapshot.dictsLocal;
        savesLocal = snapshot.savesLocal;
        //stringsLocal doesn't need to be restored
    }
    
    /**
     * Sets the VM allocation mode.
     * 
     * @param newGlobal New allocation mode. True = global, false = local.
     */
    public void setGlobal(final boolean newGlobal) {
        isGlobal = newGlobal;
    }
    
    /**
     * Checks whether this VM contains a reference to a given object ID.
     * 
     * @param id The object ID for which to look.
     * 
     * @return True, if this VM has a reference to the object ID.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public boolean hasReference(final ObjectId id) throws ProgramError {
        Object obj = getArrayObj(id);
        if (obj != null) {
            return true;
        }
        obj = getDictObj(id);
        if (obj != null) {
            return true;
        }
        obj = getSaveObj(id);
        if (obj != null) {
            return true;
        }
        obj = getStringObj(id);
        if (obj != null) {
            return true;
        }
        return false;
    }
    
    /**
     * Prints all object IDs that are managed by this VM.
     */
    public void printAllObjectIDs() {
        System.out.println("VM DUMP:");
        
        System.out.println("  arrays:");
        for (ObjectId id : arraysGlobal.keySet()) {
            System.out.println("    " + id);
        }
        for (ObjectId id : arraysLocal.keySet()) {
            System.out.println("    " + id);
        }
        
        System.out.println("  dictionaries:");
        for (ObjectId id : dictsGlobal.keySet()) {
            System.out.println("    " + id);
        }
        for (ObjectId id : dictsLocal.keySet()) {
            System.out.println("    " + id);
        }
        
        System.out.println("  saves:");
        for (ObjectId id : savesLocal.keySet()) {
            System.out.println("    " + id);
        }
        
        System.out.println("  strings:");
        for (ObjectId id : stringsGlobal.keySet()) {
            System.out.println("    " + id);
        }
        for (ObjectId id : stringsLocal.keySet()) {
            System.out.println("    " + id);
        }
    }
    
    /**
     * Object ID that identifies each shared object value. Composite PostScript
     * object refer to this object ID instead of directly to the shared object
     * value.
     * 
     * @author Paul Wagenaars
     *
     */
    public final class ObjectId {
        /** Unique ID count of this object ID. */
        private int idNumber;
        
        /** Indicates whether the object resides in the global or in the local
         * VM. */
        private boolean inGlobalVM;
        
        /**
         * Construct a new unique object ID.
         * @throws PSErrorVMError PostScript error: VM error.
         */
        private ObjectId() throws PSErrorVMError {
            // If we've used all we simply throw a VMerror. This is not really
            // required because most likely the objects previously used IDs no
            // longer exist and their IDs could be reused. But because it is
            // extremely unlikely that the ID counter ever reach it maximum
            // value I didn't bother to implement this.
            if (idCounter == Integer.MAX_VALUE) {
                throw new PSErrorVMError();
            }
            idNumber = idCounter++;
            setInGlobalVM(isGlobal);
        }
        
        /**
         * Check whether this object resides in the the global VM.
         * 
         * @return True if in global VM, false if in local VM.
         */
        public boolean isInGlobalVm() {
            return inGlobalVM;
        }
        
        /**
         * Sets the inGlobalVM property.
         * 
         * @param global True if in global VM, false if in local VM.
         */
        private void setInGlobalVM(final boolean global) {
            inGlobalVM = global;
        }
        
        /**
         * Convert this object ID to a human-readable string.
         * 
         *  @return Human-readable string representation of this object ID.
         */
        @Override
        public String toString() {
            String globLoc;
            if (isInGlobalVm()) {
                globLoc = "G";
            } else {
                globLoc = "L";
            }
            
            return String.format("%s-%09d", globLoc, idNumber);
        }
        
        /**
         * Gets the hash code of this object.
         * 
         * @return Hash code of this object.
         */
        @Override
        public int hashCode() {
            return idNumber;
        }
        
        /**
         * Checks whether this object is equal to the specified object. It only
         * compares the ID number. The type and inGlobalVM fields are not
         * checked.
         * 
         * @param obj The object with which this object is compared.
         * 
         * @return True if equal, false otherwise.
         */
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ObjectId) {
                return (idNumber == ((ObjectId) obj).idNumber);
            } else {
                return false;
            }
        }
    }
    
}
