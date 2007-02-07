/*
 * ArrayStack.java
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

package net.sf.eps2pgf.collections;

import java.util.*;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorStackUnderflow;

/** Stack implementation using an ArrayList
 *
 * @author Paul Wagenaars
 */
public class ArrayStack<E> extends ArrayList<E> {
    
    /** Creates a new instance of ArrayStack */
    public ArrayStack() {
        super();
    }
    
    /** Creates a new ArrayStack with initial capacity */
    public ArrayStack(int initialCapacity) {
        super(initialCapacity);
    }
    
    /** Tests if the stack is empty */
    public boolean empty() {
        return isEmpty();
    }
    
    /** Pushes an object on top of the stack.
     */
    public E push(E item) {
        add(item);
        return item;
    }
    
    /** Pops an object from the top of the stack.
     */
    public E pop() throws PSError {
        int sz = size();
        if (sz <= 0) {
            throw new PSErrorStackUnderflow();
        } else {
            return remove(sz - 1);
        }
    }
    
    /** Looks at the n'th item from the top of the stack without removing
     * it. n = 0 corresponds to the top of the stack.
     */
    public E peek(int n) throws PSError {
        int index = size() - 1 - n;
        if (index < 0) {
            throw new PSErrorStackUnderflow();
        } else {
            return get(index);
        }
    }
    
    /**  Looks at the object at the top of this stack without removing it
     * from the stack.
     */
    public E peek() throws PSError {
        return peek(0);
    }
}
