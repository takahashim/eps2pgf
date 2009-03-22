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

import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorVMError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectFile;

/**
 * Execution stack. Stack of objects that await processing by the interpreter.
 * @author Paul Wagenaars
 */
public class ExecStack {
    /** Execution stack (see PostScript manual for more info). */
    private PSObjectArray stack;
    
    /** VM belonging to this execution stack.*/
    private VM vm;
    
    /**
     * Create a new execution stack.
     * 
     * @param virtualMemory The virtual memory manager.
     * 
     * @throws PSErrorVMError Virtual memory error.
     */
    public ExecStack(final VM virtualMemory) throws PSErrorVMError {
        vm = virtualMemory;
        stack = new PSObjectArray(vm);
    }
    
    /**
     * Returns the top-most file object on this execution stack.
     * 
     * @throws PSError A PostScript error occurred.
     * 
     * @return Topmost file or <code>null</code> when there is no file on this
     * execution stack.
     */
    public PSObjectFile getTopmostFile() throws PSError {
        for (int i = size() - 1; i >= 0; i--) {
            PSObject obj = stack.get(i);
            if (obj instanceof PSObjectFile) {
                return obj.toFile();
            }
        }
        return null;
    }
    
    /**
     * Gets the next PostScript token from the top-most item on this execution
     * stack.
     * 
     * @param interp The interpreter.
     * 
     * @return Returns next token. Returns <code>null</code> when there are no
     * more tokens left on the top item on the stack. This empty top
     * item is popped and <code>null</code> is returned.
     * 
     * @throws PSError There was a PostScript error retrieving the next token.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObject getNextToken(final Interpreter interp)
            throws PSError, ProgramError {
        
        // Check for empty stack
        if (stack.size() == 0) {
            return null;
        }
        
        // Loop through all object on the stack until we find a token
        PSObject top;
        while ((top = getTop()) != null) {
            List<PSObject> list = top.token(interp);
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
            int nr = stack.size();
            return stack.remove(nr - 1);
        } catch (PSErrorRangeCheck e) {
            // Most likely the stack is empty.
        }
        return null;
    }

    /**
     * Pushes a new item on this execution stack.
     * 
     * @param obj Object to push on the stack.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void push(PSObject obj) throws ProgramError {
        try {
            stack.addToEnd(obj);
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("Rangecheck while pushing item on execution"
                    + " stack.");
        }
    }
    
    /**
     * Return the number of items on this exection stack.
     * 
     * @return Number of items on the stack
     */
    public int size() {
        return stack.size();
    }

    /**
     * Get the top element on the execution stack.
     * 
     * @return the top
     */
    public PSObject getTop() {
        int nrItems = stack.size();
        try {
            return stack.get(nrItems - 1);
        } catch (PSErrorRangeCheck e) {
            // This can never happen.
            return null;
        }
    }

    /**
     * Returns a reference to the execution stack.
     * 
     * @return the stack
     */
    public PSObjectArray getStack() {
        return stack;
    }
    
}
