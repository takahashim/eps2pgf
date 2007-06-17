/*
 * PSError.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.eps2pgf.postscript.errors;

/**
 *
 * @author Paul Wagenaars
 */
public class PSError extends Exception {
    String methodName = "";
    
    /** Creates a new instance of PSError */
    public PSError() {
    }
    
    public String getMessage() {
        // Get the error type from the class name
        String error = this.getClass().getName();
        error = error.substring(error.lastIndexOf('.')+1);
        if (error.startsWith("PSError")) {
            error = error.substring(7);
        }
        error = error.toLowerCase();
        
        // Get the object where the error occured from
        StackTraceElement[] trace = this.getStackTrace();
        String methodName = new String();
        for (int i = 0 ; i < trace.length ; i++) {
            String mName = trace[i].getMethodName();
            if (mName.startsWith("op_")) {
                methodName = mName.substring(3);
                break;
            }
        }
        if (methodName.length() == 0) {
            methodName = trace[0].getMethodName();
        }
        
        return "Error: /" + error + " in " + methodName;
    }
    
}
