/*
 * PSObjectOperator.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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

import java.util.*;
import java.lang.reflect.*;

import net.sf.eps2pgf.postscript.errors.PSError;

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
        isLiteral = false;
    }
    
    /**
     * Create new operator object
     */
    public PSObjectOperator(PSObjectOperator obj) {
        name = obj.name;
        opMethod = obj.opMethod;
        copyCommonAttributes(obj);
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
