/*
 * DictStack.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

import java.lang.reflect.*;
import java.util.*;
import net.sf.eps2pgf.util.ArrayStack;
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
        fillSystemDict(interp);
        systemdict.readonly();
    }
    
    /**
     * Check the access attribute of this object. Throws an exception when
     * not allowed.
     */
    public void checkAccess(boolean execute, boolean read, boolean write)
            throws PSErrorInvalidAccess, PSErrorStackUnderflow {
        if (dictStack.size() > 0) {
            dictStack.peek().checkAccess(execute, read, write);
        } else {
            userdict.checkAccess(execute, read, write);
        }
    }
    
    /**
     * PostScript operator <code>cleardictstack</code>. Pops all dictionaries off
     * the dictionary stack except the permanent entries.
     */
    public void cleardictstack() {
        try {
            while (true) {
                dictStack.pop();
            }
        } catch (PSErrorStackUnderflow e) {
            // we reached the end of the stack, it's empty now.
        }
    }
    
    /**
     * Implements PostScript operator 'countdictstack'
     * @return Returns the number of dictionaries on the stack
     */
    int countdictstack() {
        return 3 + dictStack.size();
    }
    
    /** Define key->value in current dictionary. */
    public void def(PSObject key, PSObject value) throws PSErrorTypeCheck {
        try {
            PSObjectDict dict = dictStack.peek();
            dict.setKey(key, value);
        } catch (PSErrorStackUnderflow e) {
            userdict.setKey(key, value);
        }
    }
    
    /**
     * PostScript operator <code>dictstack</code>
     * Stores all elements of the dictionary stack into <code>array</code>
     * and returns an object describing the initial <code>n</code>'-element
     * subarray of 'array'.
     * @param array Array to which the dictionaries will be stored
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck <CODE>array</CODE> is too short to store all dictionaries
     * @return Subarray of <code>array</code> with all dictionaries
     */
    public PSObjectArray dictstack(PSObjectArray array) throws PSErrorRangeCheck {
        int n = countdictstack();
        if (n > array.length()) {
            throw new PSErrorRangeCheck();
        }
        array.put(0, systemdict);
        array.put(1, globaldict);
        array.put(2, userdict);
        for (int i = 0 ; i < dictStack.size() ; i++) {
            array.put(i+3, dictStack.get(i));
        }
        
        return array.getinterval(0, n);
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
        System.out.println("    - " + systemdict.length() + " key->value pairs.");
        System.out.println("----- End of dictionary stack");
    }

    /** Fill the system dictionary */
    private void fillSystemDict(Interpreter interp) {
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
        
        // add errordict dictionary
        PSObjectDict errordict = new PSObjectDict();
        // dummy dictionary, error handling is not yet implemented
        systemdict.setKey("errordict", errordict);
        
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
        
        // add permanent dictionaries
        systemdict.setKey("systemdict", systemdict);
        systemdict.setKey("userdict", userdict);
        systemdict.setKey("globaldict", globaldict);
        systemdict.setKey("statusdict", new PSObjectDict());
        systemdict.setKey("FontDirectory", Fonts.FontDirectory);
        
        // add encoding vectors
        PSObjectName[] encodingVector = Encoding.getISOLatin1Vector();
        systemdict.setKey("ISOLatin1Encoding", new PSObjectArray(encodingVector));
        encodingVector = Encoding.getStandardVector();
        systemdict.setKey("StandardEncoding", new PSObjectArray(encodingVector));        
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
    public PSObjectDict where(PSObject key) {
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
    
    
    /** Lookup a key in the dictionary stack */
    public PSObject lookup(PSObject key) {
        PSObjectDict dict = where(key);
        if (dict == null) {
            return null;
        } else {
            return dict.lookup(key);
        }
    }
    
    /** Lookup a key in the dictionary stack */
    public PSObject lookup(String key) {
    	return this.lookup(new PSObjectName(key, true));
    }
    
    /** Implement PostScript operator: store */
    public void store(PSObject key, PSObject value) throws PSErrorTypeCheck {
        PSObjectDict dict = where(key);
        if (dict == null) {
            // Key is currently not defined in any dictionary. So define it in
            // the current dictionary.
            def(key, value);
        } else {
            dict.setKey(key, value);
        }        
    }
    
}
