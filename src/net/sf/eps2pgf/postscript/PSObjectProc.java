/*
 * PSObjectProc.java
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

import java.awt.event.ItemEvent;
import java.io.*;
import java.util.*;
import net.sf.eps2pgf.postscript.errors.PSError;

/**
 * Procedure PostScript object.
 * @author Paul Wagenaars
 */
public class PSObjectProc extends PSObject {
    static int nextId = 1;
    int id;
    private LinkedList<PSObject> procObjects;
    
    /**
     * Creates a new instance of PSObjectProc
     * @param str String representing a valid procedure.
     * @throws java.io.IOException Unable to read the string from a StringBuffer.  
     */
    public PSObjectProc(String str) throws IOException {
        id = nextId++;
        str = str.substring(1,str.length()-1);
        
        StringReader strReader = new StringReader(str);
        
        procObjects = Parser.convert(strReader);
    }
    
    /**
     * Check whether a string is a procedure
     * @param str String to check.
     * @return Returns true when the string is a procedure. Returns false otherwise.
     */
    public static boolean isType(String str) {
        int len = str.length();
        if ( (str.charAt(0) == '{') && (str.charAt(len-1) == '}') ) {
            return true;
        } else {
            return false;
        }
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() throws PSError {
        StringBuilder str = new StringBuilder();
        Iterator<PSObject> it = procObjects.iterator();
        str.append("{");
        while (it.hasNext()) {
            PSObject ob = it.next();
                str.append(" " + ob.isis());
        }
        str.append(" }");
        return str.toString();
    }
    
    /** Replace executable name objects with their values */
    public PSObject bind(Interpreter interp) throws PSError {
        LinkedList<PSObject> newList = new LinkedList<PSObject>();
        
        while(procObjects.size() > 0) {
            PSObject obj = procObjects.poll();
            newList.add(obj.bind(interp));
        }
        procObjects = newList;
        return this;
    }
    
    /** Executes this object in the supplied interpreter */
    public void execute(Interpreter interp) throws Exception {
        interp.execStack.copyAndAddObjectList(procObjects);
    }
    
}
