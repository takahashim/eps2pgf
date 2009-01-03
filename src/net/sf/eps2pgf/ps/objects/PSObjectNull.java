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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.util.CloneMappings;
import net.sf.eps2pgf.util.MapCloneable;

/** Represent PostScript null object.
 *
 * @author Paul Wagenaars
 */
public class PSObjectNull extends PSObject implements MapCloneable {
    
    /**
     * Create a new null object.
     */
    public PSObjectNull() {
        /* empty block */
    }
    
    /**
     * Create a new null object.
     * 
     * @param obj Common attributes are copied from this object.
     */
    public PSObjectNull(final PSObjectNull obj) {
        copyCommonAttributes(obj);
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @param cloneMap The clone map.
     * 
     * @return Deep copy of this object.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    @Override
    public PSObjectNull clone(CloneMappings cloneMap)
            throws ProgramError {
        
        if (cloneMap == null) {
            cloneMap = new CloneMappings();
        } else if (cloneMap.containsKey(this)) {
            return (PSObjectNull) cloneMap.get(this);
        }
        
        PSObjectNull copy = (PSObjectNull) super.clone(cloneMap);
        cloneMap.add(this, copy);
        
        return copy;
    }

    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PSObject dup() {
        return new PSObjectNull(this);
    }
    
    /**
     * Compare this object with another object and return true if they are
     * equal. See PostScript manual on what's equal and what's not.
     * 
     * @param obj Object to compare this object with
     * 
     * @return True if objects are equal, false otherwise
     */
    @Override
    public boolean eq(final PSObject obj) {
        return (obj instanceof PSObjectNull);
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
        return 856346;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return PostScript representation of this object.
     */
    @Override
    public String isis() {
        return "null";
    }
    
    /**
     * Returns the same object.
     * 
     * @return This object.
     */
    @Override
    public PSObjectNull toNull() {
        return this;
    }
    


    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "nulltype";
    }
}
