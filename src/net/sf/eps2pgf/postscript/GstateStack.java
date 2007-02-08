/*
 * GstateStack.java
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

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.collections.ArrayStack;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

/** Manages the graphics states (stack, ...)
 *
 * @author Paul Wagenaars
 */
public class GstateStack {
    private ArrayStack<GraphicsState> stack;
    GraphicsState current;
    
    /**
     * Creates a new instance of GstateStack
     */
    public GstateStack() {
        stack = new ArrayStack<GraphicsState>();
        current = new GraphicsState();
    }
    
    /**
     * Pushes a copy of the current graphics state on the stack.
     */
    public void saveGstate() throws PSError {
        try {
            stack.push(current.clone());
        } catch (CloneNotSupportedException e) {
            throw new PSErrorUnimplemented("Clone method not yet implemented for " + e.getStackTrace());
        }
    }

    /**
     * Restores the topmost graphics state from the stack.
     */
    public void restoreGstate() throws PSError {
        if (stack.isEmpty()) {
            return;
        }
        current = stack.pop();
    }
}
