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

package net.sf.eps2pgf.ps.errors;

import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Indicates that a PostScript error occurred.
 *
 * @author Paul Wagenaars
 */
public class PSError extends Exception {
    
    /*
     * Define all standard PostScript errors
     */
    /** setpagedevice or setdevparams request cannot be satisfied. */
    public static final PSObjectName CONFIGURATIONERROR = 
        new PSObjectName("/configurationerror");
    
    /** No more room in dictionary. */
    public static final PSObjectName DICTFULL = 
        new PSObjectName("/dictfull");
    
    /** Too many begin operators. */
    public static final PSObjectName DICTSTACKOVERFLOW = 
        new PSObjectName("/dictstackoverflow");
    
    /** Too many end operators. */
    public static final PSObjectName DICTSTACKUNDERFLOW = 
        new PSObjectName("/dictstackunderflow");
    
    /** Executive stack nesting too deep. */
    public static final PSObjectName EXECSTACKOVERFLOW = 
        new PSObjectName("/execstackoverflow");
    
    /** Called to report error information. */
    public static final PSObjectName HANDLEERROR = 
        new PSObjectName("/handleerror");
    
    /** External interrupt request (for example, Control-C). */
    public static final PSObjectName INTERRUPT = 
        new PSObjectName("/interrupt");
    
    /** Attempt to violate access attribute. */
    public static final PSObjectName INVALIDACCESS = 
        new PSObjectName("/invalidaccess");
    
    /** exit not in loop. */
    public static final PSObjectName INVALIDEXIT = 
        new PSObjectName("/invalidexit");
    
    /** Unacceptable access string. */
    public static final PSObjectName INVALIDFILEACCESS = 
        new PSObjectName("/invalidfileaccess");
    
    /** Invalid Font resource name or font or CIDFont dictionary. */
    public static final PSObjectName INVALIDFONT = 
        new PSObjectName("/invalidfont");
    
    /** Improper restore. */
    public static final PSObjectName INVALIDRESTORE = 
        new PSObjectName("/invalidrestore");
    
    /** Input/output error. */
    public static final PSObjectName IOERROR = 
        new PSObjectName("/ioerror");
    
    /** Implementation limit exceeded. */
    public static final PSObjectName LIMITCHECK = 
        new PSObjectName("/limitcheck");
    
    /** Current point undefined. */
    public static final PSObjectName NOCURRENTPOINT = 
        new PSObjectName("/nocurrentpoint");
    
    /** Operand out of bounds. */
    public static final PSObjectName RANGECHECK = 
        new PSObjectName("/rangecheck");
    
    /** Operand stack overflow. */
    public static final PSObjectName STACKOVERFLOW = 
        new PSObjectName("/stackoverflow");
    
    /** Operand stack underflow. */
    public static final PSObjectName STACKUNDERFLOW = 
        new PSObjectName("/stackunderflow");
    
    /** PostScript language syntax error. */
    public static final PSObjectName SYNTAXERROR = 
        new PSObjectName("/syntaxerror");
    
    /** Time limit exceeded. */
    public static final PSObjectName TIMEOUT = 
        new PSObjectName("/timeout");
    
    /** Operand of wrong type. */
    public static final PSObjectName TYPECHECK = 
        new PSObjectName("/typecheck");
    
    /** Name not known. */
    public static final PSObjectName UNDEFINED = 
        new PSObjectName("/undefined");
    
    /** File not found. */
    public static final PSObjectName UNDEFINEDFILENAME = 
        new PSObjectName("/undefinedfilename");
    
    /** Resource instance not found. */
    public static final PSObjectName UNDEFINEDRESOURCE = 
        new PSObjectName("/undefinedresource");
    
    /** Overflow, underflow, or meaningless result. */
    public static final PSObjectName UNDEFINEDRESULT = 
        new PSObjectName("/undefinedresult");
    
    /** Expected mark not on stack. */
    public static final PSObjectName UNMATCHEDMARK = 
        new PSObjectName("/unmatchedmark");
    
    /** Internal error. */
    public static final PSObjectName UNREGISTERED = 
        new PSObjectName("/unregistered");
    
    /** Virtual memory exhausted. */
    public static final PSObjectName VMERROR = 
        new PSObjectName("/VMerror");
    
    /** List of all PostScript errors. */
    private static final PSObjectName[] ALLPSERRORS = {CONFIGURATIONERROR,
        DICTFULL, DICTSTACKOVERFLOW, DICTSTACKUNDERFLOW, EXECSTACKOVERFLOW,
        HANDLEERROR, INTERRUPT, INVALIDACCESS, INVALIDEXIT, INVALIDFILEACCESS,
        INVALIDFONT, INVALIDRESTORE, IOERROR, LIMITCHECK, NOCURRENTPOINT,
        RANGECHECK, STACKOVERFLOW, STACKUNDERFLOW, SYNTAXERROR, TIMEOUT,
        TYPECHECK, UNDEFINED, UNDEFINEDFILENAME, UNDEFINEDRESOURCE,
        UNDEFINEDRESULT, UNMATCHEDMARK, UNREGISTERED, VMERROR};
    
    
    /*
     * Define errors that are specific to this implementation of the
     * PostScript specification. They are used internally by this interpreter. 
     */
    /** Indicates that a stop has been executed. */
    public static final PSObjectName STOPEXECUTED =
        new PSObjectName("/eps2pgfstopexecuted");
    
    /** Indicates that a quit has been executed. */
    public static final PSObjectName QUITEXECUTED =
        new PSObjectName("/eps2pgfquitexecuted");
    
    /** List of all implementation-specific errors. */
    private static final PSObjectName[] ALLINTERNALERRORS = {
        STOPEXECUTED, QUITEXECUTED};
        
    
    
    /** Serial version UID field. */
    private static final long serialVersionUID = 1L;
    
    /** Type of error. */
    private PSObjectName type;
    
    /** Custom error customMessage. */
    private String customMessage;
    
    /**
     * Create a new instance of PSError of the specified type.
     * 
     * @param errorType The error type specified as name or string.
     */
    protected PSError(final PSObjectName errorType) {
        this(errorType, "");
    }
    
    /**
     * Create a new instance of PSError of the specified type and with the
     * specified custom message.
     * 
     * @param errorType The error type specified as name or string.
     * @param message The error message.
     */
    protected PSError(final PSObjectName errorType, final String message) {
        for (int i = 0; i < ALLPSERRORS.length; i++) {
            if (errorType.eq(ALLPSERRORS[i])) {
                type = ALLPSERRORS[i];
                customMessage = message;
                return;
            }
        }
        for (int i = 0; i < ALLINTERNALERRORS.length; i++) {
            if (errorType.eq(ALLINTERNALERRORS[i])) {
                type = ALLINTERNALERRORS[i];
                customMessage = message;
                return;
            }
        }
        
        type = UNREGISTERED;
        customMessage = "Invalid error type (" + errorType.isis() 
                        + ") specified while constructing a new error.";
    }
    
    /**
     * Gets a (shallow) copy of the list with all PostScript errors.
     * 
     * @return A copy of the list with all standard PostScript errors.
     */
    public static PSObjectName[] getAllPSErrors() {
        PSObjectName[] copy = new PSObjectName[ALLPSERRORS.length];
        for (int i = 0; i < ALLPSERRORS.length; i++) {
            copy[i] = ALLPSERRORS[i];
        }
        return copy;
    }
    
    
    /**
     * Gets the name of this error.
     * 
     * @return Name/type of this error.
     */
    public PSObjectName getErrorName() {
        return type;
    }
    
    /**
     * Get a the customMessage describing the error.
     * 
     * @return Message describing the error.
     */
    @Override
    public String getMessage() {
        // Get the object where the error occurred from
        StackTraceElement[] trace = this.getStackTrace();
        String methodName = "";
        for (int i = 0; i < trace.length; i++) {
            String mName = trace[i].getMethodName();
            if (mName.startsWith("op_")) {
                methodName = mName.substring(3);
                break;
            }
        }
        if (methodName.length() == 0) {
            methodName = trace[0].getMethodName();
        }
        
        String msg = type.isis() + " in " + methodName;
        if (customMessage.length() > 0) {
            msg += " (" + customMessage + ")";
        }
        return msg;
    }
    
    
}
