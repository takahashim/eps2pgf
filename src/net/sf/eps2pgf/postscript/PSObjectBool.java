/*
 * PSObjectBool.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;

/** Represent PostScript object: boolean
 *
 * @author Paul Wagenaars
 */
public class PSObjectBool extends PSObject {
    boolean value = false;
    
    /** Creates a new instance of PSObjectBool */
    public PSObjectBool(boolean bool) {
        value = bool;
    }
    
    /**
     * Creates a new boolean object
     */
    public PSObjectBool(PSObjectBool obj) {
        value = obj.value;
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'and'
     * @param obj2 Object to 'and' with this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Obj2 is not a boolean object
     * @return Return true when both objects are true, return false otherwise.
     */
    public PSObjectBool and(PSObject obj2) throws PSErrorTypeCheck {
        return new PSObjectBool((value && obj2.toBool()));
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }    

    /**
     * PostScript operator 'dup'. Create a shallow copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectBool dup() {
        return new PSObjectBool(this);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        if (obj instanceof PSObjectBool) {
            PSObjectBool objBool = (PSObjectBool)obj;
            return (value == objBool.value);
        } else {
            return false;
        }
    }
    
    /**
     * PostScript operator: 'not'
     * @return Logical negation of this object
     */
    public PSObjectBool not() {
        return new PSObjectBool(!value);
    }
    
    /**
     * PostScript operator 'or'
     * @param obj2 Object to 'or' with this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Obj2 is not a boolean object
     * @return Return true when one or both objects are true, return false otherwise.
     */
    public PSObjectBool or(PSObject obj2) throws PSErrorTypeCheck {
        return new PSObjectBool((value || obj2.toBool()));
    }
    
    /** Convert this object to a boolean, if possible. */
    public boolean toBool() {
        return value;
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "booleantype";
    }

    /**
     * PostScript operator 'xor'
     * @param obj2 Object to 'xor' with this object
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Obj2 is not a boolean object
     * @return Return true when only one of the objects is true, return false otherwise.
     */
    public PSObjectBool xor(PSObject obj2) throws PSErrorTypeCheck {
        boolean bool2 = obj2.toBool();
        boolean tot = ((value && !bool2) || (!value && bool2));
        return new PSObjectBool(tot);
    }
    
}
