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
    
    /** VM where this object's shared value is stored. */
    private VM vm;
    
    /**
     * Creates a new composite object.
     * 
     * @param virtualMemory The VM object in which the shared object will be
     * stored.
     */
    protected PSObjectComposite(final VM virtualMemory) {
        vm = virtualMemory;
    }
    
    /**
     * Creates a new composite object.
     * 
     * @param virtualMemory The VM object in which the shared object will be
     * stored.
     * @param id Object ID to be associated with this composite object.
     */
    protected PSObjectComposite(final VM virtualMemory, final ObjectId id) {
        vm = virtualMemory;
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
