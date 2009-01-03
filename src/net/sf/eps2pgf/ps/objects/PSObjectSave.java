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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.DictStack;
import net.sf.eps2pgf.ps.InterpParams;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidRestore;
import net.sf.eps2pgf.util.CloneMappings;
import net.sf.eps2pgf.util.MapCloneable;

/**
 * Standard PostScript object: save.
 * 
 * @author Paul Wagenaars
 *
 */
public class PSObjectSave extends PSObject implements MapCloneable {
    
    /** User, system and device parameters. */
    private InterpParams interpParams;
    
    /** Dictionary stack. */
    private DictStack dictStack;
    
    /** InterpCounter at time of save.*/
    private int interpCount;
    
    /** Indicate whether this save object is valid. */
    private boolean valid = true;
    
    /** Interpreter to which this object belongs. */
    private Interpreter interp;
    
    /**
     * Dictionary to keep track of the latest (i.e. highest interpCount)
     * restore operations.
     */
    private static HashMap<Integer, ArrayList<PSObjectSave>> allSaveObjs =
                                new HashMap<Integer, ArrayList<PSObjectSave>>();
    
    /**
     * Create a new save object from the current state of the interpreter.
     */
    public PSObjectSave() {
        /* empty block */
    }

    /**
     * Create a new save object from the current state of the interpreter.
     * 
     * @param aInterp The interpreter.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectSave(final Interpreter aInterp) throws ProgramError {
        interp = aInterp;
        
        CloneMappings cloneMap = new CloneMappings();
        dictStack = interp.getDictStack().clone(cloneMap);
        interpParams = interp.getInterpParams().clone(cloneMap);
        interpCount = interp.getInterpCounter();
        
        int interpId = interp.hashCode();
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
     * Creates a *deep* copy of this object.
     * 
     * @param cloneMap The clone map.
     * 
     * @return A deep copy of this object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public PSObjectSave clone(CloneMappings cloneMap)
            throws ProgramError {
        
        if (cloneMap == null) {
            cloneMap = new CloneMappings();
        } else if (cloneMap.containsKey(this)) {
            return (PSObjectSave) cloneMap.get(this);
        }
        
        PSObjectSave copy = (PSObjectSave) super.clone(cloneMap);
        cloneMap.add(this, copy);
        
        copy.dictStack = dictStack.clone(cloneMap);
        copy.interpParams = interpParams.clone(cloneMap);
        
        return copy;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObject dup() {
        PSObjectSave copy = new PSObjectSave();
        copy.dictStack = dictStack;
        copy.interpParams = interpParams;
        copy.interpCount = interpCount;
        copy.valid = valid;
        copy.interp = interp;
        
        return copy;
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
        
        return ((interpCount == objSave.interpCount)
                && (interp.hashCode() == objSave.hashCode()));
    }

    /**
     * Returns a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by java.util.Hashtable.
     * 
     * @return The hashcode of this object.
     */
    @Override
    public int hashCode() {
        return interpCount * interp.hashCode();
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
     * @throws PSErrorInvalidRestore An invalid restore occurred.
     */
    public void restore() throws PSErrorInvalidRestore {
        if (!valid) {
            throw new PSErrorInvalidRestore();
        }
        
        interp.setDictStack(dictStack);
        interp.setInterpParams(interpParams);
        
        // Invalidate this object and all newer save objects
        valid = false;
        int interpId = interp.hashCode();
        ArrayList<PSObjectSave> saveObjs = allSaveObjs.get(interpId);
        for (PSObjectSave current : saveObjs) {
            if (current.interpCount > interpCount) {
                current.valid = false;
                saveObjs.remove(current);
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
