/*
 * DictStack.java
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
import java.util.*;
import net.sf.eps2pgf.collections.ArrayStack;
import net.sf.eps2pgf.postscript.errors.*;

/** PostScript dictionary stack
 * See PostScript manual for more info "3.3.9 Dictionary objects"
 *
 * @author Paul Wagenaars
 */
public class DictStack {
    // Dictionary stack (except for the permanent dictionaries)
    private ArrayStack<PSObjectDict> dictStack = new ArrayStack<PSObjectDict>();
    
    // User dictionary
    private PSObjectDict userdict = new PSObjectDict();
    
    // Global dictionary
    private PSObjectDict globaldict = new PSObjectDict();
    
    // System dictionary
    private PSObjectDict systemdict = new PSObjectDict();
    
    
    /** Create a new dictionary stack */
    public DictStack(Interpreter interp) {
        try {
            fillSystemDict(interp);
            systemdict.readonly();
        } catch (PSErrorInvalidAccess e) {
            // this can never happen since all newly created dictionaries are read-write
        }
    }
    
    /** Fill the system dictionary */
    private void fillSystemDict(Interpreter interp) throws PSErrorInvalidAccess {
        // Add operators
        Method[] mthds = interp.getClass().getMethods();
        HashMap<String, String> replaceNames = new HashMap<String, String>();
        replaceNames.put("sqBrackLeft",  "[");
        replaceNames.put("sqBrackRight", "]");
        replaceNames.put("dblLessBrackets", "<<");
        replaceNames.put("dblGreaterBrackets", ">>");
        
        for (int i = 0 ; i < mthds.length ; i++) {
            Method mthd = mthds[i];
            String name = mthd.getName();
            
            // PostScript operator methods start with op_ All other methods
            // can be skipped.
            if (!name.startsWith("op_")) {
                continue;
            }
            
            name = name.substring(3);
            if (replaceNames.containsKey(name)) {
                name = replaceNames.get(name);
            }
            PSObjectOperator op = new PSObjectOperator(name, mthd);
            systemdict.setKey(name, op);
        }
        
        // Add other entries
        systemdict.setKey("userdict", userdict);
        
        // Add $error dictionary
        PSObjectDict dollarerror = new PSObjectDict();
        dollarerror.setKey("newerror", new PSObjectBool(false));
        dollarerror.setKey("errorname", new PSObjectNull());
        dollarerror.setKey("command", new PSObjectNull());
        dollarerror.setKey("errorinfo", new PSObjectNull());
        dollarerror.setKey("ostack", new PSObjectNull());
        dollarerror.setKey("estack", new PSObjectNull());
        dollarerror.setKey("dstack", new PSObjectNull());
        dollarerror.setKey("recordstacks", new PSObjectBool(true));
        dollarerror.setKey("binary", new PSObjectBool(false));
        systemdict.setKey("$error", dollarerror);
    }

    /** Push a dictionary onto the stack */
    public void pushDict(PSObjectDict dict) {
        dictStack.push(dict);
    }
    
    /** Peeks at the topmost dictionary. */
    public PSObjectDict peekDict() {
        try {
            return dictStack.peek();
        } catch (PSErrorStackUnderflow e) {
            return userdict;
        }
    }
    
    /** Pops the topmost dictionary from the stack. */
    public PSObjectDict popDict() throws PSErrorDictStackUnderflow {
        try {
            return dictStack.pop();
        } catch (PSErrorStackUnderflow e) {
            throw new PSErrorDictStackUnderflow();
        }
    }
    
    /** Search the dictionary that defines a specific key. */
    public PSObjectDict where(String key) throws PSErrorInvalidAccess {
        // First look in the non-permanent dictionaries
        for(int i = dictStack.size()-1 ; i >= 0 ; i--) {
            PSObjectDict dict = dictStack.get(i);
            if (dict.known(key)) {
                return dict;
            }
        }
        
        // Then look in the permanent dictionary userdict
        if (userdict.known(key)) {
            return userdict;
        }
        
        // Then look in up in the permanent dictionary globaldict
        if (globaldict.known(key)) {
            return globaldict;
        }
        
        // Finally, look in the permanent dictionary systemdict
        if (systemdict.known(key)) {
            return systemdict;
        }
        
        return null;
    }
    
    /** Search the dictionary that defines a specific key. */
    public PSObjectDict where(PSObject key) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        return where(key.toDictKey());
    }
    
    
    /** Lookup a key in the dictionary stack */
    public PSObject lookup(String key) throws PSErrorInvalidAccess {
        PSObjectDict dict = where(key);
        if (dict == null) {
            return null;
        } else {
            return dict.lookup(key);
        }
    }
    
    
    /** Define key->value in current dictionary. */
    public void def(String key, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        try {
            PSObjectDict dict = dictStack.peek();
            dict.setKey(key, value);
        } catch (PSErrorStackUnderflow e) {
            userdict.setKey(key, value);
        }
    }
    
    /** Define key->value in current dictionary. */
    public void def(PSObject key, PSObject value) throws PSErrorTypeCheck,
            PSErrorInvalidAccess {
        def(key.toDictKey(), value);
    }
    
    
    /** Implement PostScript operator: store */
    public void store(String key, PSObject value) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        PSObjectDict dict = where(key);
        if (dict == null) {
            // Key is currently not defined in any dictionary. So define it in
            // the current dictionary.
            def(key, value);
        } else if (dict == systemdict) {
            // If it is defined in the system dictionary, throw an exception.
            // The user can not alter the system dictionary.
            throw new PSErrorInvalidAccess();
        } else {
            dict.setKey(key, value);
        }        
    }
    
    /** Implement PostScript operator: store */
    public void store(PSObject key, PSObject value) throws PSErrorTypeCheck, PSErrorInvalidAccess {
        store(key.toDictKey(), value);
    }
    
    public void dumpFull() throws PSErrorRangeCheck {
        System.out.println("----- Dictionary stack");
        for(int i = dictStack.size()-1 ; i >= 0 ; i--) {
            PSObjectDict dict = dictStack.get(i);
            System.out.println("  --- dict" + i);
            dict.dumpFull("    - ");
        }
        System.out.println("  --- userdict");
        userdict.dumpFull("    - ");
        System.out.println("  --- systemdict");
        try {
            System.out.println("    - " + systemdict.length() + " key->value pairs.");
        } catch (PSErrorInvalidAccess e) {
            System.out.println("    - ?? key->value pairs.");
        }
        System.out.println("----- End of dictionary stack");
    }
}
