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

/** PostScript object: literal name
 *
 * @author Paul Wagenaars
 */
public class PSObjectName extends PSObject {
    public String name;
    
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
        name = obj.name;
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectName dup() {
        return new PSObjectName(this);
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
    
    /**
     * Return this object
     * @return This object
     */
    public PSObjectName toName() {
        return this;
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info)
     * @return Text representation
     */
    public String cvs() {
        return name;
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        if (obj instanceof PSObjectName) {
            PSObjectName objName = (PSObjectName)obj;
            return (name.equals(objName.name));
        } else if (obj instanceof PSObjectString) {
            PSObjectString objStr = (PSObjectString)obj;
            return (name.equals(objStr.toString()));
        } else {
            return false;
        }
    }
    
    /** Return a hash code for this object. */
    public int hashCode() {
        return name.hashCode();
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
            PSObject lookedUp;
            lookedUp = interp.getDictStack().lookup(this);
            if ( (lookedUp != null) && (lookedUp instanceof PSObjectOperator) ) {
                return lookedUp;
            }  else {
                return this;
            }
        }
    }
    
    /**
     * Implements PostScript operate: length
     * @return Length of this object
     */
    public int length() {
        return name.length();
    }
    
    /**
     * Return human readable string representation of this object
     */
    public String toString() {
        return name;
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "nametype";
    }
}
