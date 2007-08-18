/*
 * GstateStack.java
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

import net.sf.eps2pgf.util.ArrayStack;
import net.sf.eps2pgf.output.OutputDevice;
import net.sf.eps2pgf.postscript.errors.*;

/** Manages the graphics states (stack, ...)
 *
 * @author Paul Wagenaars
 */
public class GstateStack {
    private ArrayStack<GraphicsState> stack;
    public GraphicsState current;
    
    /**
     * Creates a new instance of GstateStack
     */
    public GstateStack(OutputDevice output) {
        stack = new ArrayStack<GraphicsState>();
        current = new GraphicsState(this, output);
    }
    
    /**
     * Pushes a copy of the current graphics state on the stack.
     */
    public void saveGstate() throws PSErrorUnimplemented {
        try {
            stack.push(current.clone());
        } catch (CloneNotSupportedException e) {
            throw new PSErrorUnimplemented("Clone method not yet implemented for " + e.getStackTrace());
        }
    }

    /**
     * Restores the topmost graphics state from the stack.
     */
    public void restoreGstate() {
        try {
            current = stack.pop();
        } catch (PSErrorStackUnderflow e) {
            return;
        }
    }
}
