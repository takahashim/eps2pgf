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

import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.VM.ObjectId;

/**
 * Super class for composite objects.
 * 
 * @author Paul Wagenaars
 */
public abstract class PSObjectComposite extends PSObject {
    /** Object ID of object's shared value. */
    private ObjectId objectId;
    
    /** Interpreter to which this object belongs. */
    private Interpreter interp;
    
    /** VM where this object's shared value is stored. Note that this value is
     * just a copy from interp.getVm(). */
    private VM vm;
    
    /**
     * Creates a new composite object.
     * 
     * @param interpreter The interpreter.
     */
    protected PSObjectComposite(final Interpreter interpreter) {
        interp = interpreter;
        vm = interp.getVm();
    }
    
    /**
     * Creates a new composite object.
     * 
     * @param id Object ID to be associated with this composite object.
     * @param interpreter The interpreter.
     */
    protected PSObjectComposite(final Interpreter interpreter,
            final ObjectId id) {
        
        interp = interpreter;
        vm = interp.getVm();
        objectId = id;
    }
    
    /**
     * PostScript operator: gcheck.
     * 
     * @return Returns true if the operand is a simple object, or if it is
     * composite and its value resides in global VM. It returns false if the
     * operand is composite and its value resides in local VM. 
     */
    @Override
    public boolean gcheck() {
        return getId().isInGlobalVm();
    }
    
    /**
     * Gets the interpreter to which this object is associated.
     * 
     * @return The VM.
     */
    protected Interpreter getInterp() {
        return interp;
    }
    
    /**
     * Gets the VM object associated with this object.
     * 
     * @return The VM.
     */
    protected VM getVm() {
        return vm;
    }
    
    /**
     * Gets object ID of the shared object.
     * 
     * @return The shared object's ID.
     */
    public ObjectId getId() {
        return objectId;
    }
    
    /**
     * Sets the object ID of the shared object.
     * 
     * @param id The object id.
     */
    protected void setId(final ObjectId id) {
        objectId = id;
    }
}
