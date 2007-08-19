/*
 * ExecStack.java
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

import net.sf.eps2pgf.postscript.*;
import net.sf.eps2pgf.postscript.errors.*;

/**
 * Execution stack. Stack of objects that await processing by the interpreter.
 * @author Paul Wagenaars
 */
public class ExecStack {
    /**
     * Execution stack (see PostScript manual for more info)
     */
    PSObjectArray stack = new PSObjectArray();
    
    /**
     * A reference to the top-most item on the execution stack for faster access.
     */
    PSObject top = null;
    
    /**
     * Returns the top-most file object on this execution stack
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred.
     * @return Topmost file or <code>null</code> when there is no file on this
     * execution stack.
     */
    public PSObjectFile getTopmostFile() throws PSError {
        for (int i = size()-1 ; i >= 0 ; i--) {
            PSObject obj = stack.get(i);
            if (obj instanceof PSObjectFile) {
                return obj.toFile();
            }
        }
        return null;
    }
    
    /**
     * Gets the next PostScript token from the top-most item on this execution stack.
     * @return Returns next token. Returns <code>null</code> when there are no
     *         more tokens left on the top item on the stack. This empty top
     *         item is popped and <code>null</code> is returned.
     * @throws net.sf.eps2pgf.postscript.errors.PSError There was a PostScript error retrieving the next token
     */
    public PSObject getNextToken() throws PSError {
        // Check for empty stack
        if (stack.size() == 0) {
            return null;
        }
        
        // Check for valid top
        if (top == null) {
            top = stack.get(stack.size() - 1);
        }
        
        // Loop through all object on the stack until we find a token
        while (top != null) {
            List<PSObject> list = top.token();
            if (list.size() == 2) {
                return list.get(0);
            } else if (list.size() == 3) {
                pop();
                PSObject remaining = list.get(0);
                if (remaining != null) {
                    push(remaining);
                }
                return list.get(1);
            } else {
                pop();
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Pops an item from the execution stack.
     * NOTE: DO NOT USED THIS METHOD TO RETRIEVE THE NEXT TOKEN FROM THE
     *       EXECUTION STACK FOR PROCESSING. USE getNextToken() INSTEAD.
     * @return Returns popped element. Returns null when no more items are left.
     */
    public PSObject pop() {
        try {
            int N = stack.size();
            if (N < 1) {
                return null;
            }
            PSObject poppedObj = stack.remove(N-1);
            if (N >= 2) {
                top = stack.get(N-2);
            } else {
                top = null;
            }
            return poppedObj;
        } catch (PSErrorRangeCheck e) {
            // This can never happen, since the stack size (N) is used.
        }
        return null;
    }

    /**
     * Pushes a new item on this execution stack.
     * @param obj Object to push on the stack.
     */
    public void push(PSObject obj) {
        stack.addToEnd(obj);
        top = obj;
    }
    
    /**
     * Return the number of items on this exection stack
     * @return Number of items on the stack
     */
    public int size() {
        return stack.size();
    }
    
}
