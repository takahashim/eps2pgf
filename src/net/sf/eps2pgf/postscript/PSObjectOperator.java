/*
 * PSObjectOperator.java
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

import java.lang.reflect.Method;

/**
 * Object that represents a PostScript operator. Either a built-in or user-defined operator.
 * @author Paul Wagenaars
 */
public class PSObjectOperator extends PSObject {
    public String name;
    public Method opMethod;
    
    /**
     * Creates a new instance of PSObjectOperator
     */
    public PSObjectOperator(String nm, Method op) {
        name = nm;
        opMethod = op;
        setLiteral(false);
    }
    
    /**
     * Create new operator object
     */
    public PSObjectOperator(final PSObjectOperator obj) {
        name = obj.name;
        opMethod = obj.opMethod;
        copyCommonAttributes(obj);
    }
    
    /**
     * Creates a deep copy of this object.
     * 
     * @return Deep copy of this object.
     */
    @Override
    public PSObjectOperator clone() {
        PSObjectOperator copy = (PSObjectOperator) super.clone();
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
    public int hashCode() {
        return opMethod.hashCode();
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "--" + name + "--";
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
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectOperator dup() {
        return new PSObjectOperator(this);
    }
    
    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "operatortype";
    }
}
