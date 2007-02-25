/*
 * PSObjectName.java
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

import java.lang.reflect.*;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;

/** PostScript object: literal name
 *
 * @author Paul Wagenaars
 */
public class PSObjectName extends PSObject {
    String name;
    
    /**
     * Creates a new instance of PSObjectName
     * 
     * @param str String representing a literal name.
     */
    public PSObjectName(String str) {
        if (str.charAt(0) == '/') {
            name = str.substring(1);
            isLiteral = true;
        } else {
            name = new String(str);
            isLiteral = false;
        }
    }
    
    /**
     * Create a new instance of PSObjectName
     * @param str Name of the new PSObjectName object
     * @param aIsLiteral Indicates whether this object is a literal name
     */
    public PSObjectName(String str, boolean aIsLiteral) {
        name = new String(str);
        isLiteral = aIsLiteral;
    }
    
    /** Creates a new instance of PSObjectName. */
    public PSObjectName(PSObjectName obj) {
        name = new String(obj.name);
        isLiteral = obj.isLiteral;
    }
    
    /**
     * Check whether a string is a name
     * @param str String to check
     * @return Returns always true.
     */
    public static boolean isType(String str) {
        return true;
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        if (isLiteral) {
            return "/" + name;
        } else {
            return name;
        }
    }
    
    /** Execute this object */
    public void execute(Interpreter interp) throws Exception {
        PSObject obj = interp.dictStack.lookup(name);
        if (obj == null) {
            throw new PSErrorUndefined(name);
        }
        interp.processObject(obj);
    }
    
    /** Convert this object to dictionary key, if possible. */
    public String toDictKey() {
        return name;
    }
    
    /**
     * Return this object
     * @return This object
     */
    public PSObjectName toName() {
        return this;
    }
    
    /** Test whether an object is equal to this object. */
    public boolean equals(Object obj) {
        // First check whether the type is the same
        if (!(obj instanceof PSObjectName)) {
            return false;
        }
        
        PSObjectName objName = (PSObjectName)obj;
        return ((isLiteral == objName.isLiteral) && name.equals(objName.name));
    }
    
    /** Return a hash code for this object. */
    public int hashCode() {
        if (isLiteral) {
            return name.hashCode();
        } else {
            return ~(name.hashCode());
        }
    }

    /** Creates a copy of this object. */
    public PSObjectName clone() {
        return new PSObjectName(this);
    }
    
    /** Implement bind operator for this object. */
    public PSObject bind(Interpreter interp) {
        if (isLiteral) {
            return this;
        } else {
            PSObject lookedUp = interp.dictStack.lookup(name);
            if ( (lookedUp != null) && (lookedUp instanceof PSObjectOperator) ) {
                return lookedUp;
            }  else {
                return this;
            }
        }
    }
}
