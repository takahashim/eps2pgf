/*
 * PSError.java
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
