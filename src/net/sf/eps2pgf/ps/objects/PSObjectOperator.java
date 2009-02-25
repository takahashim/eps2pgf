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

import java.lang.reflect.Method;

/**
 * Object that represents a PostScript operator. Either a built-in or
 * user-defined operator.
 * 
 * @author Paul Wagenaars
 */
public class PSObjectOperator extends PSObject implements Cloneable {
    
    /** Name of this operator. */
    private String name;
    
    /** Method with which this object is associated. */
    private Method opMethod;
    
    /**
     * Creates a new instance of PSObjectOperator.
     * 
     * @param pName The name of the operator.
     * @param op The method associated with this operator.
     */
    public PSObjectOperator(final String pName, final Method op) {
        name = pName;
        opMethod = op;
        setLiteral(false);
    }
    
    /**
     * Create new operator object.
     * 
     * @param obj New object is shallow copy of this object.
     */
    public PSObjectOperator(final PSObjectOperator obj) {
        name = obj.name;
        opMethod = obj.opMethod;
        copyCommonAttributes(obj);
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
        return opMethod.hashCode();
    }
    
    /**
     * Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     * 
     * @return PostScript representation of this object.
     */
    @Override
    public String isis() {
        return "--" + name + "--";
    }
    
    /**
     * Produce a text representation of this object (see PostScript
     * operator 'cvs' for more info).
     * 
     * @return Text representation
     */
    @Override
    public String cvs() {
        return name;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectOperator dup() {
        return new PSObjectOperator(this);
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
     * Returns the method associated with this operator.
     * 
     * @return the opMethod
     */
    public Method getOpMethod() {
        return opMethod;
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
