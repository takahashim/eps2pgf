/*
 * PSObjectName.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.postscript;

/**
 * PostScript object: name.
 *
 * @author Paul Wagenaars
 */
public class PSObjectName extends PSObject {
    
    /** The value of this name. */
    private String name;
    
    /**
     * Creates a new instance of PSObjectName.
     * 
     * @param str String representing a literal name.
     */
    public PSObjectName(final String str) {
        if (str.charAt(0) == '/') {
            name = str.substring(1);
            setLiteral(true);
        } else {
            name = str;
            setLiteral(false);
        }
    }
    
    /**
     * Create a new instance of PSObjectName.
     * 
     * @param str Name of the new PSObjectName object
     * @param pIsLiteral Indicates whether this object is a literal name
     */
    public PSObjectName(final String str, final boolean pIsLiteral) {
        name = str;
        setLiteral(pIsLiteral);
    }
    
    /**
     * Creates a new instance of PSObjectName.
     * 
     * @param obj The obj.
     */
    public PSObjectName(final PSObjectName obj) {
        name = obj.name;
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     * 
     * @return Duplicate of this object.
     */
    @Override
    public PSObjectName dup() {
        return new PSObjectName(this);
    }
    
    /**
     * Check whether a string is a name.
     * 
     * @param str String to check
     * @return Returns always true.
     */
    public static boolean isType(final String str) {
        return true;
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return String representation of this object.
     */
    @Override
    public String isis() {
        if (isLiteral()) {
            return "/" + name;
        } else {
            return name;
        }
    }
    
    /**
     * Return this object.
     * 
     * @return This object
     */
    @Override
    public PSObjectName toName() {
        return this;
    }
    
    /**
     * Produce a text representation of this object (see PostScript operator
     * 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        return name;
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
        if (obj instanceof PSObjectName) {
            PSObjectName objName = (PSObjectName) obj;
            return (name.equals(objName.name));
        } else if (obj instanceof PSObjectString) {
            PSObjectString objStr = (PSObjectString) obj;
            return (name.equals(objStr.toString()));
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
        return name.hashCode();
    }

    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectName clone() {
        PSObjectName copy = (PSObjectName) super.clone();
        return copy;
    }

    /**
     * Implement bind operator for this object.
     * 
     * @param interp The interpreter
     * 
     * @return "Binded" object.
     */
    @Override
    public PSObject bind(final Interpreter interp) {
        if (isLiteral()) {
            return this;
        } else {
            PSObject lookedUp;
            lookedUp = interp.getDictStack().lookup(this);
            if ((lookedUp != null) && (lookedUp instanceof PSObjectOperator)) {
                return lookedUp;
            }  else {
                return this;
            }
        }
    }
    
    /**
     * Implements PostScript operate: length.
     * 
     * @return Length of this object
     */
    @Override
    public int length() {
        return name.length();
    }
    
    /**
     * Value/name of this object. It ignores the literal/excetubale attribute.
     * 
     * @return The string.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values).
     */
    @Override
    public String type() {
        return "nametype";
    }
}
