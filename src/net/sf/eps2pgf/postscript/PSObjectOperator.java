/*
 * PSObjectOperator.java
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

import java.util.*;
import java.lang.reflect.*;
import net.sf.eps2pgf.postscript.errors.PSError;

/**
 * Object that represents a PostScript operator. Either a built-in or user-defined operator.
 * @author Paul Wagenaars
 */
public class PSObjectOperator extends PSObject {
    String name;
    Method opMethod;
    
    /**
     * Creates a new instance of PSObjectOperator
     */
    public PSObjectOperator(String nm, Method op) {
        name = nm;
        opMethod = op;        
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        return "--" + name + "--";
    }
    
    /**
     * Executes this object in the supplied interpreter
     * @param interp Interpreter in which this object is executed.
     * @throws java.lang.Exception An error occured during the execution of this object.
     */
    public void execute(Interpreter interp) throws Exception {
        try {
            opMethod.invoke(interp);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof PSError) {
                throw (PSError)e.getCause();
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Process this object in the supplied interpreter. This is the way
     * objects from the operand stack are processed.
     * @param interp Interpreter in which this object is processed.
     * @throws java.lang.Exception An error occured during the execution of this object.
     */
    public void process(Interpreter interp) throws Exception {
        execute(interp);
    }

    /**
     * Checks whether this object is executable
     * @return Returns true if this object is executable
     */
    public boolean isExecutable() {
        return true;
    }
}
