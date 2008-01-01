/*
 * PSObjectNull.java
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

/** Represent PostScript null object.
 *
 * @author Paul Wagenaars
 */
public class PSObjectNull extends PSObject {
    
    /**
     * Create a new null object
     */
    public PSObjectNull() {
        
    }
    
    /**
     * Create a new null object
     */
    public PSObjectNull(PSObjectNull obj) {
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObject dup() {
        return new PSObjectNull(this);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        return (obj instanceof PSObjectNull);
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "null";
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "nulltype";
    }
}
