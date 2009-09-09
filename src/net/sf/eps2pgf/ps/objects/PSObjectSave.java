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

package net.sf.eps2pgf.ps.objects;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidRestore;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;

/**
 * Standard PostScript object: save.
 * 
 * @author Paul Wagenaars
 *
 */
public class PSObjectSave extends PSObjectComposite implements Cloneable {
    
    /** InterpCounter at time of save.*/
    private int interpCount;
    
    /** Indicate whether this save object is valid. */
    private boolean valid = true;
    
    /**
     * Dictionary to keep track of the latest (i.e. highest interpCount)
     * restore operations.
     */
    private static HashMap<Integer, ArrayList<PSObjectSave>> allSaveObjs =
                                new HashMap<Integer, ArrayList<PSObjectSave>>();
    
    /**
     * Create a new save object from the current state of the interpreter.
     * 
     * @param interpreter The interpreter.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSErrorVMError Virtual memory error.
     */
    public PSObjectSave(final Interpreter interpreter)
            throws PSErrorVMError, ProgramError {
        
        super(interpreter);
        
        interpCount = interpreter.getInterpCounter();
        setId(getVm().addSaveObj(interpreter.getVm().clone()));
        
        int interpId = interpreter.hashCode();
        ArrayList<PSObjectSave> saveObjs;
        if (!allSaveObjs.containsKey(interpId)) {
            saveObjs = new ArrayList<PSObjectSave>();
            allSaveObjs.put(interpId, saveObjs);
        } else {
            saveObjs = allSaveObjs.get(interpId);
        }
        saveObjs.add(this);
    }
    
    /**
     * Create a new save object. The new object is a shallow copy of the
     * supplied save object.
     * 
     * @param saveObj Save object to copy.
     */
    public PSObjectSave(final PSObjectSave saveObj) {
        super(saveObj.getInterp(), saveObj.getId());
        
        interpCount = saveObj.interpCount;
        valid = saveObj.valid;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObject dup() {
        return new PSObjectSave(this);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * 
     * @param obj The object to compare to.
     * 
     * @return True if objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PSObjectSave)) {
            return false;
        }
        PSObjectSave objSave = (PSObjectSave) obj;
        
        return (interpCount == objSave.interpCount)
                && (valid == objSave.valid)
                && getId().equals(objSave.getId())
                && getVm().equals(objSave.getVm());
    }
    
    /**
     * Returns a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by java.util.Hashtable.
     * 
     * @return The hashcode of this object.
     */
    @Override
    public int hashCode() {
        return interpCount;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * @return Text representation of this object.
     */
    @Override
    public String isis() {
        return "-save(" + interpCount + ")-";
    }
    
    /**
     * Restore the interpreter to the state as it is stored in this object.
     * 
     * @param interp The interpreter.
     * 
     * @throws PSErrorInvalidRestore An invalid restore occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void restore(final Interpreter interp)
            throws PSErrorInvalidRestore, ProgramError {
        
        if (!valid) {
            throw new PSErrorInvalidRestore();
        }
        
        VM savedVm = getVm().getSaveObj(getId());
        
        // Before we do the actual restore we look through the operand,
        // execution and dictionary stack to see if they contain a reference
        // to a composite object to would be discarded by this restore.
        // Do the actual restore
        interp.getVm().restoreFromSnapshot(savedVm);
        
        // Invalidate this object and all newer save objects
        valid = false;
        int interpId = interp.hashCode();
        ArrayList<PSObjectSave> saveObjs = allSaveObjs.get(interpId);
        for (int i = saveObjs.size() - 1; i >= 0; i--) {
            PSObjectSave currentObj = saveObjs.get(i);
            if (currentObj.interpCount > interpCount) {
                currentObj.valid = false;
                saveObjs.remove(i);
            }
        }
    }
    
    /**
     * Return this object.
     * 
     * @return This object
     */
    @Override
    public PSObjectSave toSave() {
        return this;
    }
    
    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "savetype";
    }
}
