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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;

/**
 * Object that represents a PostScript operator. Either a built-in or
 * user-defined operator.
 * 
 * @author Paul Wagenaars
 */
public abstract class PSObjectOperator extends PSObject implements Cloneable {
    
    /** Name of this operator. Don't access directly, use getName() instead. */
    private String name = "";


    /**
     * Create a new operator object.
     */
    public PSObjectOperator() {
        setLiteral(false);
    }
    
    /**
     * Create a clone of this object.
     * 
     * @return A clone of this object.
     */
    @Override
    public PSObjectOperator clone() {
        PSObjectOperator copy = (PSObjectOperator) super.clone();
        
        // all fields of this object are primitive, so there is no need to clone
        // them explicitly.
        
        return copy;
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
     * Gets the name of this operator, which is whatever comes after the first
     * character of the name of this operator class. For example the class Oabs
     * has operator name "abs".
     * 
     * @return The name of this operator.
     */
    public String getName() {
        if (name.length() == 0) {
            name = getClass().getSimpleName().substring(1);
        }

        return name;
    }
    
    /**
     * Changes the name for this operator.
     * 
     * @param newName The new name.
     */
    public void setName(final String newName) {
        name = newName;
    }
    
    /**
     * Returns a hash code value for the object.
     * 
     * @return Hash code of this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
    
    /**
     * Invokes this PostScript operator.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError A program error occurred.
     */
    public abstract void invoke() throws PSError, ProgramError;
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return PostScript representation of this object.
     */
    @Override
    public String isis() {
        return "--" + getName() + "--";
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        return getName();
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectOperator dup() {
        return clone();
    }
    
    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    @Override
    public String type() {
        return "operatortype";
    }

    /**
     * Return this object if it is an operator, throw PSErrorTypeCheck
     * otherwise.
     * 
     * @return This object, if it is an operator.
     */
    @Override
    public PSObjectOperator toOperator() {
        return this;
    }
    

}
