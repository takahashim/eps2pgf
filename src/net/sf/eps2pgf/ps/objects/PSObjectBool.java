/*
 * PSObjectBool.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;

/**
 * Represent PostScript object: boolean.
 *
 * @author Paul Wagenaars
 */
public class PSObjectBool extends PSObject {
    
    /** Value of this boolean. */
    private boolean value = false;
    
    /**
     * Creates a new instance of PSObjectBool.
     * 
     * @param bool The bool.
     */
    public PSObjectBool(final boolean bool) {
        value = bool;
    }
    
    /**
     * Creates a new boolean object.
     * 
     * @param obj Boolean value is copied from this object.
     */
    public PSObjectBool(final PSObjectBool obj) {
        value = obj.value;
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'and'.
     * 
     * @param obj2 Object to 'and' with this object
     * 
     * @return Return true when both objects are true, return false otherwise.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObjectBool and(final PSObject obj2) throws PSErrorTypeCheck {
        return new PSObjectBool((value && obj2.toBool()));
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectBool clone() {
        PSObjectBool copy = (PSObjectBool) super.clone();
        return copy;
    }
    
    /**
     * Produce a text representation of this object (see PostScript operator
     * 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }    

    /**
     * PostScript operator 'dup'. Create a shallow copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PSObjectBool dup() {
        return new PSObjectBool(this);
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
        if (obj instanceof PSObjectBool) {
            PSObjectBool objBool = (PSObjectBool) obj;
            return (value == objBool.value);
        } else {
            return false;
        }
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
        return isis().hashCode();
    }
    
    /**
     * PostScript operator: 'not'.
     * 
     * @return Logical negation of this object
     */
    @Override
    public PSObjectBool not() {
        return new PSObjectBool(!value);
    }
    
    /**
     * PostScript operator 'or'.
     * 
     * @param obj2 Object to 'or' with this object
     * 
     * @return Return true when one or both objects are true, return false
     * otherwise.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObjectBool or(final PSObject obj2) throws PSErrorTypeCheck {
        return new PSObjectBool((value || obj2.toBool()));
    }
    
    /**
     * Convert this object to a boolean, if possible.
     * 
     * @return Boolean representation of this object.
     */
    @Override
    public boolean toBool() {
        return value;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return String representation of this object.
     */
    @Override
    public String isis() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values).
     */
    @Override
    public String type() {
        return "booleantype";
    }

    /**
     * PostScript operator 'xor'.
     * 
     * @param obj2 Object to 'xor' with this object
     * 
     * @return Return true when only one of the objects is true, return false
     * otherwise.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    @Override
    public PSObjectBool xor(final PSObject obj2) throws PSErrorTypeCheck {
        boolean bool2 = obj2.toBool();
        boolean tot = ((value && !bool2) || (!value && bool2));
        return new PSObjectBool(tot);
    }
    
}
