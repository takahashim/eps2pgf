/*
 * GstateStack.java
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

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;
import net.sf.eps2pgf.ps.resources.outputdevices.OutputDevice;
import net.sf.eps2pgf.util.ArrayStack;

/** 
 * Manages the stack of graphics states.
 *
 * @author Paul Wagenaars
 */
public class GstateStack {
    
    /** The stack with graphics states. */
    private ArrayStack<GraphicsState> stack;
    
    /** The current private state. */
    private GraphicsState current;
    
    /**
     * Creates a new instance of GstateStack.
     * 
     * @param output The output device to be associated with the graphics state.
     */
    public GstateStack(final OutputDevice output) {
        stack = new ArrayStack<GraphicsState>();
        setCurrent(new GraphicsState(this, output));
    }
    
    /**
     * Pushes a copy of the current graphics state on the stack.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void saveGstate() throws ProgramError {
        try {
            stack.push(current().clone());
        } catch (CloneNotSupportedException e) {
            throw new ProgramError("Clone method not yet implemented.");
        }
    }

    /**
     * Restores the topmost graphics state from the stack.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public void restoreGstate() throws ProgramError {
        // If the stack is empty we do nothing.
        if (stack.isEmpty()) {
            return;
        }
        
        try {
            setCurrent(stack.pop());
        } catch (PSErrorStackUnderflow e) {
            ProgramError pe = new ProgramError("Stack underflow shouldn't"
                    + " happen here.");
            pe.initCause(e);
            throw pe;
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
