/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.IOException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.resources.outputdevices.OutputDevice;
import net.sf.eps2pgf.util.ArrayStack;
import net.sf.eps2pgf.util.CloneMappings;

/** 
 * Manages the stack of graphics states.
 *
 * @author Paul Wagenaars
 */
public class GstateStack {
    
    /** The stack with graphics states. */
    private ArrayStack<GraphicsState> stack;
    
    /** The that keeps if the graphics states where saved by save or gsave. */
    private ArrayStack<Byte> saveOrGsave;
    
    /** Indicates that there was no gstate, the stack was empty. */
    public static final byte EMPTYSTACK = 0;
    
    /** Indicates that gstate was saved by 'save' operator. */
    public static final byte SAVE = 1;
    
    /** Indicates that gstate was saved by 'gsave' operator. */
    public static final byte GSAVE = 2;
    
    /** The current private state. */
    private GraphicsState current;
    
    /**
     * Creates a new instance of GstateStack.
     * 
     * @param output The output device to be associated with the graphics state.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public GstateStack(final OutputDevice output) throws ProgramError {
        stack = new ArrayStack<GraphicsState>();
        saveOrGsave = new ArrayStack<Byte>();
        setCurrent(new GraphicsState(this, output));
    }
    
    /**
     * Pushes a copy of the current graphics state on the stack.
     * 
     * @param savedByGSave True if this method is called as a result of 'gsave'
     * operator. False if called as a result of 'save' operator.
     * @param cloneMap The clone map.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void saveGstate(final boolean savedByGSave,
            CloneMappings cloneMap) throws ProgramError, IOException {
        
        if (cloneMap == null) {
            cloneMap = new CloneMappings();
        }
        stack.push(current().clone(cloneMap));
        if (savedByGSave) {
            saveOrGsave.push(GSAVE);
        } else {
            saveOrGsave.push(SAVE);
        }
        current().getDevice().startScope();
    }

    /**
     * Restores the topmost graphics state from the stack.
     * 
     * @param calledByGRestore True, if method called as result of 'grestore'
     * operator. False if method called as result of 'restore'.
     * @param cloneMap The clone map. Only required if calledBYGRestore is true.
     * 
     * @return Indicator whether the restored gstate was saved by 'save' or
     * 'gsave'. EMPTYSTACK: stack was already empty, GSAVE: saved by 'gsave'
     * operator, SAVE: saved by 'save' operator.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public byte restoreGstate(final boolean calledByGRestore,
            CloneMappings cloneMap) throws ProgramError, IOException {
        
        // If the stack is empty we do nothing.
        if (stack.isEmpty()) {
            return EMPTYSTACK;
        }
        
        try {
            if (saveOrGsave.peek() == GSAVE) {
                setCurrent(stack.pop());
                saveOrGsave.pop();
                current().getDevice().endScope();
                return GSAVE;
            } else {
                // Top-most gstate was saved by 'save' operator.
                // Next, check whether this method was called by 'grestore' or
                // 'restore'.
                if (calledByGRestore) {
                    // This method was called by 'grestore'
                    if (cloneMap == null) {
                        cloneMap = new CloneMappings();
                    }
                    setCurrent(stack.peek().clone(cloneMap));
                } else {
                    // This method is called by 'restore'
                    setCurrent(stack.pop());
                    saveOrGsave.pop();
                    current().getDevice().endScope();
                }
                return SAVE;
            }
        } catch (PSErrorStackUnderflow e) {
            ProgramError pe = new ProgramError("Stack underflow shouldn't"
                    + " happen here.");
            pe.initCause(e);
            throw pe;
        }
    }

    /**
     * Repeatedly performs grestore operations until it encounters a graphics
     * state that was saved by a save operation (as opposed to gsave), leaving
     * that state on the top of the graphics state stack and resetting the
     * current graphics state from it.
     * 
     * See PostScript reference on 'grestoreall' operator for more details.
     * 
     * @param forGrestoreall True, if called as a result of a 'grestoreall'
     * operator. False, if called as result of 'restore' operator.
     * @param cloneMap The clone map. Only required if calledBYGRestore is true.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void restoreAllGstate(final boolean forGrestoreall,
            CloneMappings cloneMap) throws ProgramError, IOException {
        
        if (forGrestoreall && (cloneMap == null)) {
            cloneMap = new CloneMappings();
        }
        while (restoreGstate(forGrestoreall, cloneMap) == GSAVE) {
            /* empty block */
        }
    }
    
    /**
     * Sets the current graphics state.
     * 
     * @param pCurrent The current.
     */
    private void setCurrent(final GraphicsState pCurrent) {
        current = pCurrent;
    }

    /**
     * Gets the current graphics state.
     * 
     * @return the current
     */
    public GraphicsState current() {
        return current;
    }
}
