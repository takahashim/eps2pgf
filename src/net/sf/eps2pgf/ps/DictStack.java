/*
 * DictStack.java
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

package net.sf.eps2pgf.ps;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.eps2pgf.Main;
import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorDictStackUnderflow;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidAccess;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectBool;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectOperator;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.Encoding;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * PostScript dictionary stack.
 * See PostScript manual for more info "3.3.9 Dictionary objects"
 *
 * @author Paul Wagenaars
 */
public class DictStack {
    
    /** Key to the internaldict as defined in the systemdict. */
    public static final PSObjectName KEY_INTERNALDICT =
        new PSObjectName("/eps2pgfinternaldict");
    
    /** Dictionary stack (except for the permanent dictionaries). */
    private ArrayStack<PSObjectDict> dictStack = new ArrayStack<PSObjectDict>();
    
    /** User dictionary. */
    private PSObjectDict userdict = new PSObjectDict();
    
    /** Global dictionary. */
    private PSObjectDict globaldict = new PSObjectDict();
    
    /** System dictionary. */
    private PSObjectDict systemdict = new PSObjectDict();
    
    
    /**
     * Create a new dictionary stack.
     * 
     * @param interp The interpreter with which this dictionary stack is
     * associated.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public DictStack(final Interpreter interp) throws PSError, ProgramError {
        fillSystemDict(interp);
        try {
            systemdict.readonly();
        } catch (PSErrorTypeCheck e) {
            /* this can never happen */
        }
    }
    
    /**
     * Check the access attribute of this object. Throws an exception when
     * not allowed.
     * 
     * @param execute Is this object to be executed?
     * @param read Is this object to be read?
     * @param write Is this object to be written?
     * 
     * @throws PSErrorInvalidAccess the PS error invalid access
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public void checkAccess(final boolean execute, final boolean read,
            final boolean write)
            throws PSErrorInvalidAccess, PSErrorStackUnderflow {
        if (dictStack.size() > 0) {
            dictStack.peek().checkAccess(execute, read, write);
        } else {
            userdict.checkAccess(execute, read, write);
        }
    }
    
    /**
     * PostScript operator <code>cleardictstack</code>. Pops all dictionaries
     * off the dictionary stack except the permanent entries.
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
     * Implements PostScript operator 'countdictstack'.
     * 
     * @return Returns the number of dictionaries on the stack
     */
    int countdictstack() {
        return 3 + dictStack.size();
    }
    
    /**
     * Define key->value in current dictionary.
     * 
     * @param key The key.
     * @param value The value.
     */
    public void def(final PSObject key, final PSObject value) {
        try {
            PSObjectDict dict = dictStack.peek();
            dict.setKey(key, value);
        } catch (PSErrorStackUnderflow e) {
            userdict.setKey(key, value);
        }
    }
    
    /**
     * PostScript operator <code>dictstack</code>.
     * Stores all elements of the dictionary stack into <code>array</code>
     * and returns an object describing the initial <code>n</code>'-element
     * subarray of 'array'.
     * 
     * @param array Array to which the dictionaries will be stored
     * 
     * @throws PSErrorRangeCheck <CODE>array</CODE> is too short to store all
     * dictionaries.
     * 
     * @return Subarray of <code>array</code> with all dictionaries
     */
    public PSObjectArray dictstack(final PSObjectArray array)
            throws PSErrorRangeCheck {
        int n = countdictstack();
        if (n > array.length()) {
            throw new PSErrorRangeCheck();
        }
        array.put(0, systemdict);
        array.put(1, globaldict);
        array.put(2, userdict);
        for (int i = 0; i < dictStack.size(); i++) {
            array.put(i + 3, dictStack.get(i));
        }
        
        return array.getinterval(0, n);
    }
    
    /**
     * Write a representation of the full dictionary stack to the standard
     * output.
     * 
     * @throws PSErrorRangeCheck A PostScript rangecheck error occurred.
     */
    public void dumpFull() throws PSErrorRangeCheck {
        System.out.println("----- Dictionary stack");
        for (int i = dictStack.size() - 1; i >= 0; i--) {
            PSObjectDict dict = dictStack.get(i);
            System.out.println("  --- dict" + i);
            dict.dumpFull("    - ");
        }
        System.out.println("  --- userdict");
        userdict.dumpFull("    - ");
        System.out.println("  --- systemdict");
        System.out.println("    - " + systemdict.length()
                + " key->value pairs.");
        System.out.println("----- End of dictionary stack");
    }

    /**
     * Fill the system dictionary with all operators and other values.
     * 
     * @param interp The interpreter.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void fillSystemDict(final Interpreter interp)
            throws PSError, ProgramError {

        PSObjectArray emptyProc = new PSObjectArray("{}");

        // Add operators
        Method[] mthds = interp.getClass().getMethods();
        HashMap<String, String> replaceNames = new HashMap<String, String>();
        replaceNames.put("sqBrackLeft",  "[");
        replaceNames.put("sqBrackRight", "]");
        replaceNames.put("dblLessBrackets", "<<");
        replaceNames.put("dblGreaterBrackets", ">>");
        replaceNames.put("isis", "==");
        
        for (int i = 0; i < mthds.length; i++) {
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
        errordict.setKey("handleerror", new PSObjectArray("{stop}"));
        systemdict.setKey("errordict", errordict);
        
        // Add $error dictionary
        PSObjectDict dollarerror = new PSObjectDict();
        dollarerror.setKey("newerror", new PSObjectBool(false));
        dollarerror.setKey("errorname", emptyProc);
        dollarerror.setKey("command", emptyProc);
        dollarerror.setKey("errorinfo", emptyProc);
        dollarerror.setKey("ostack", emptyProc);
        dollarerror.setKey("estack", emptyProc);
        dollarerror.setKey("dstack", emptyProc);
        dollarerror.setKey("recordstacks", new PSObjectBool(true));
        dollarerror.setKey("binary", new PSObjectBool(false));
        systemdict.setKey("$error", dollarerror);
        
        // add permanent dictionaries
        systemdict.setKey("systemdict", systemdict);
        systemdict.setKey("userdict", userdict);
        systemdict.setKey("globaldict", globaldict);
        systemdict.setKey("statusdict", new PSObjectDict());
        systemdict.setKey("FontDirectory",
                interp.getResourceManager().getFontManager());
        systemdict.setKey(KEY_INTERNALDICT, new PSObjectDict());
        
        // VM allocation mode
        systemdict.setKey("currentglobal", new PSObjectBool(false));
        
        // add encoding vectors
        PSObjectName[] encodingVector = Encoding.getISOLatin1Vector();
        systemdict.setKey("ISOLatin1Encoding",
                new PSObjectArray(encodingVector));
        encodingVector = Encoding.getStandardVector();
        systemdict.setKey("StandardEncoding",
                new PSObjectArray(encodingVector));
        
        // Add some dummy operators that set the page size. These are sometimes
        // used.
        systemdict.setKey("11x17", emptyProc);
        systemdict.setKey("a3", emptyProc);
        systemdict.setKey("a4", emptyProc);
        systemdict.setKey("a4small", emptyProc);
        systemdict.setKey("b5", emptyProc);
        systemdict.setKey("ledger", emptyProc);
        systemdict.setKey("legal", emptyProc);
        systemdict.setKey("letter", emptyProc);
        systemdict.setKey("lettersmall", emptyProc);
        systemdict.setKey("note", emptyProc);
        
        // add other operators
        systemdict.setKey("currentpacking", new PSObjectBool(false));
        systemdict.setKey("languagelevel", new PSObjectInt(3));
        
        String version = Main.APP_VERSION;
        systemdict.setKey("version", new PSObjectString(version));
        int revision;
        try {
            revision = Integer.parseInt(version.replace(".", "0"));
        } catch (NumberFormatException e) {
            revision = 0;
        }
        systemdict.setKey("revision", new PSObjectInt(revision));
        systemdict.setKey("serialnumber", new PSObjectInt(0));
    }

    /**
     * Push a dictionary onto the stack.
     * 
     * @param dict The dictionary.
     */
    public void pushDict(final PSObjectDict dict) {
        dictStack.push(dict);
    }
    
    /**
     * Peeks at the topmost dictionary.
     * 
     * @return the top-most dictionary on the stack.
     */
    public PSObjectDict peekDict() {
        try {
            return dictStack.peek();
        } catch (PSErrorStackUnderflow e) {
            return userdict;
        }
    }
    
    /**
     * Pops the topmost dictionary from the stack.
     * 
     * @return The popped dictionary.
     * 
     * @throws PSErrorDictStackUnderflow the PS error dict stack underflow
     */
    public PSObjectDict popDict() throws PSErrorDictStackUnderflow {
        try {
            return dictStack.pop();
        } catch (PSErrorStackUnderflow e) {
            throw new PSErrorDictStackUnderflow();
        }
    }
    
    /**
     * Search the dictionary that defines a specific key.
     * 
     * @param key The key.
     * 
     * @return The dictionary in which key is defined.
     */
    public PSObjectDict where(final PSObject key) {
        // First look in the non-permanent dictionaries
        for (int i = dictStack.size() - 1; i >= 0; i--) {
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
    
    
    /**
     * Lookup a key in the dictionary stack.
     * 
     * @param key The key.
     * 
     * @return The requested object. Or <code>null</code> if the key is not
     * found.
     */
    public PSObject lookup(final PSObject key) {
        PSObjectDict dict = where(key);
        if (dict == null) {
            return null;
        } else {
            return dict.lookup(key);
        }
    }
    
    /**
     * Lookup a key in the dictionary stack.
     * 
     * @param key The key.
     * 
     * @return The requested object. Or <code>null</code> if the key is not
     * found.
     */
    public PSObject lookup(final String key) {
        return this.lookup(new PSObjectName(key, true));
    }
    
    /**
     * Implement PostScript operator: store.
     * 
     * @param key The key.
     * @param value The value.
     * 
     * @throws PSErrorTypeCheck A PostScript typecheck error occurred.
     */
    public void store(final PSObject key, final PSObject value)
            throws PSErrorTypeCheck {
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
