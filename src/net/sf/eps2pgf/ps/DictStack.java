/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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
import net.sf.eps2pgf.ps.resources.ResourceManager;
import net.sf.eps2pgf.ps.resources.encodings.ISOLatin1Encoding;
import net.sf.eps2pgf.ps.resources.encodings.StandardEncoding;
import net.sf.eps2pgf.ps.resources.encodings.SymbolEncoding;
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
    
    /** Dictionary stack. (0: systemdict, 1: globaldict, 2:userdict */
    private ArrayStack<PSObjectDict> dictStack = new ArrayStack<PSObjectDict>();
    
    /** System dictionary, quick access pointer to system dictionary. */
    private PSObjectDict systemdict;
    
    /** Interpreter to which this dictionary stack belongs. */
    private Interpreter interp;
    
    /**
     * Quick access constant. See documentation for
     * defineQuickAccessConstants() method for more information.
     */
    // CHECKSTYLE:OFF
    public PSObjectOperator eps2pgfCshow;
    public PSObjectOperator eps2pgfFilenameforall;
    public PSObjectOperator eps2pgfFor;
    public PSObjectOperator eps2pgfForall;
    public PSObjectOperator eps2pgfKshow;
    public PSObjectOperator eps2pgfLoop;
    public PSObjectOperator eps2pgfPathforall;
    public PSObjectOperator eps2pgfRepeat;
    public PSObjectOperator eps2pgfResourceforall;
    public PSObjectOperator eps2pgfStopped;
    public PSObjectOperator eps2pgfEexec;
    // CHECKSTYLE:ON
    
    /**
     * Create a new dictionary stack.
     * 
     * @param interpreter The interpreter with which this dictionary stack is
     * associated.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public DictStack(final Interpreter interpreter)
            throws PSError, ProgramError {
        
        interp = interpreter;
        
        // The user dictionary must be allocated in local VM
        interp.getVm().setGlobal(false);
        PSObjectDict userdict = new PSObjectDict(interp.getVm());
        
        // The rest must be allocated in global VM
        interp.getVm().setGlobal(true);
        PSObjectDict globaldict = new PSObjectDict(interp.getVm());
        systemdict = new PSObjectDict(interp.getVm());
        
        dictStack.push(systemdict);
        dictStack.push(globaldict);
        dictStack.push(userdict);
        
        fillSystemDict();
        //defineQuickAccessConstants(); //TODO rm line
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
            final boolean write) throws PSErrorInvalidAccess,
            PSErrorStackUnderflow {
        
        dictStack.peek().checkAccess(execute, read, write);
    }
    
    /**
     * PostScript operator <code>cleardictstack</code>. Pops all dictionaries
     * off the dictionary stack except the permanent entries.
     */
    public void cleardictstack() {
        try {
            while (dictStack.size() > 3) {
                dictStack.pop();
            }
        } catch (PSErrorStackUnderflow e) {
            // this can never happen
        }
    }
    
    /**
     * Implements PostScript operator 'countdictstack'.
     * 
     * @return Returns the number of dictionaries on the stack
     */
    int countdictstack() {
        return dictStack.size();
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
            // this can never happen
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
     * @return Subarray of <code>array</code> with all dictionaries
     * 
     * @throws PSErrorInvalidAccess A PostScript invalid access error occurred.
     * @throws PSErrorRangeCheck <CODE>array</CODE> is too short to store all
     * dictionaries.
     */
    public PSObjectArray dictstack(final PSObjectArray array)
            throws PSErrorRangeCheck, PSErrorInvalidAccess {
        
        int n = dictStack.size();
        if (n > array.length()) {
            throw new PSErrorRangeCheck();
        }
        for (int i = 0; i < n; i++) {
            array.put(i, dictStack.get(i));
        }
        
        return array.getinterval(0, n);
    }
    
    /**
     * Defines some constants that are associated with values in the systemdict.
     * This allows faster access to these values. This is for example useful for
     * the internal eps2pgf* operators.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    //TODO move content of this method to operator container class for eps2pgf*
    //operators.
    private void defineQuickAccessConstants() throws ProgramError {
        try {
            // Looping context procedures
            eps2pgfCshow = lookup("eps2pgfcshow").toOperator();
            eps2pgfFilenameforall =
                lookup("eps2pgffilenameforall").toOperator();
            eps2pgfFor = lookup("eps2pgffor").toOperator();
            eps2pgfForall = lookup("eps2pgfforall").toOperator();
            eps2pgfKshow = lookup("eps2pgfkshow").toOperator();
            eps2pgfLoop = lookup("eps2pgfloop").toOperator();
            eps2pgfPathforall = lookup("eps2pgfpathforall").toOperator();
            eps2pgfRepeat = lookup("eps2pgfrepeat").toOperator();
            eps2pgfResourceforall =
                lookup("eps2pgfresourceforall").toOperator();

            // Other continuation functions
            eps2pgfEexec = lookup("eps2pgfeexec").toOperator();
            eps2pgfStopped = lookup("eps2pgfstopped").toOperator();
        } catch (PSErrorTypeCheck e) {
            throw new ProgramError("Object in dictstack has incorrect type for"
                    + " quick access constants.");
        }
    }
    
    /**
     * Fill the system dictionary with all operators and other values.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    //TODO move filling of dictionary stack to the classes to which they belong
    private void fillSystemDict() throws PSError, ProgramError {
        PSObjectDict globaldict = dictStack.get(1);
        PSObjectDict userdict = dictStack.get(2);
        PSObjectArray emptyProc = new PSObjectArray("{}", interp);

        // add errordict dictionary (must be in local VM)
        interp.getVm().setGlobal(false);
        PSObjectDict errordict = new PSObjectDict(interp.getVm());
        PSObjectName[] errs = PSError.getAllPSErrors();
        for (int i = 0; i < errs.length; i++) {
            String str = String.format("{%s eps2pgferrorproc stop}", 
                    errs[i].isis());
            PSObjectArray proc = new PSObjectArray(str, interp);
            errordict.setKey(errs[i], proc);
        }
        errordict.setKey("handleerror",
                new PSObjectArray("{eps2pgfhandleerror}", interp));
        systemdict.setKey("errordict", errordict);
        interp.getVm().setGlobal(true);
        
        // Add $error dictionary (must be in local VM)
        interp.getVm().setGlobal(false);
        PSObjectDict dollarerror = new PSObjectDict(interp.getVm());
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
        interp.getVm().setGlobal(true);
        
        // add permanent dictionaries in global VM
        systemdict.setKey("systemdict", systemdict);
        systemdict.setKey("userdict", userdict);
        systemdict.setKey("globaldict", globaldict);
        systemdict.setKey("GlobalFontDirectory",
                interp.getResourceManager().getFontManager());
        systemdict.setKey("SharedFontDirectory",
                interp.getResourceManager().getFontManager());
        systemdict.setKey(KEY_INTERNALDICT, new PSObjectDict(interp.getVm()));
        
        // add permanent dictionaries in local VM
        interp.getVm().setGlobal(false);
        systemdict.setKey("statusdict", new PSObjectDict(interp.getVm()));
        systemdict.setKey("FontDirectory",
                interp.getResourceManager().getFontManager());
        interp.getVm().setGlobal(true);
        
        // add encoding vectors
        ResourceManager rm = interp.getResourceManager();
        systemdict.setKey(ISOLatin1Encoding.NAME,
                rm.findResource(ResourceManager.CAT_ENCODING,
                        ISOLatin1Encoding.NAME));
        systemdict.setKey(StandardEncoding.NAME,
                rm.findResource(ResourceManager.CAT_ENCODING,
                        StandardEncoding.NAME));
        systemdict.setKey(SymbolEncoding.NAME,
                rm.findResource(ResourceManager.CAT_ENCODING,
                        SymbolEncoding.NAME));
        
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
        
        String version = Main.APP_VERSION.replace(".", "0");
        int revision;
        try {
            revision = Integer.parseInt(version);
        } catch (NumberFormatException e) {
            version = "1";
            revision = 1;
        }
        systemdict.setKey("version",
                new PSObjectString(version, interp.getVm()));
        systemdict.setKey("revision", new PSObjectInt(revision));
        systemdict.setKey("serialnumber", new PSObjectInt(0));
        systemdict.setKey("product", Main.APP_NAME);
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
            // this can never happen
            return null;
        }
    }
    
    /**
     * Pops the topmost dictionary from the stack.
     * 
     * @return The popped dictionary.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public PSObjectDict popDict() throws PSError {
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
        
        return null;
    }
    
    
    /**
     * Checks whether an object is an operator defining a looping context. See
     * PostScript manual under 'exit' operator for info which operators these
     * are.
     * 
     * @param obj The object to check.
     * 
     * @return True when object defines looping context, false otherwise.
     */
    public boolean isLoopingContext(final PSObject obj) {
        if (obj instanceof PSObjectOperator) {
            if ((obj == eps2pgfFor) || (obj == eps2pgfForall)
                    || (obj == eps2pgfLoop) || (obj == eps2pgfRepeat)
                    || (obj == eps2pgfCshow) || (obj == eps2pgfFilenameforall)
                    || (obj == eps2pgfKshow) || (obj == eps2pgfPathforall)
                    || (obj == eps2pgfResourceforall)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks whether an object is an continuation function.
     * 
     * @param obj The object to check.
     * 
     * @return True when object is continuation function, false otherwise.
     */
    public boolean isContinuationFunction(final PSObject obj) {
        if (obj instanceof PSObjectOperator) {
            if ((obj == eps2pgfFor) || (obj == eps2pgfForall)
                    || (obj == eps2pgfLoop) || (obj == eps2pgfRepeat)
                    || (obj == eps2pgfCshow) || (obj == eps2pgfFilenameforall)
                    || (obj == eps2pgfKshow) || (obj == eps2pgfPathforall)
                    || (obj == eps2pgfResourceforall)
                    || (obj == eps2pgfStopped) || (obj == eps2pgfEexec)) {
                return true;
            }
        }
        return false;
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
        return lookup(new PSObjectName(key, true));
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
    
    /**
     * Gets the complete dictionary stack.
     * 
     * @return The dictionary stack.
     */
    public ArrayStack<PSObjectDict> getStack() {
        return dictStack;
    }
    
    /**
     * Gets the system dictionary.
     * 
     * @return The dictionary stack.
     */
    public PSObjectDict getSystemDict() {
        return systemdict;
    }
}
