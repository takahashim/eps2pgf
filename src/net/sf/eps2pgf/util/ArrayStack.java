/*
 * ArrayStack.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.util;

import java.util.ArrayList;

import net.sf.eps2pgf.ps.errors.PSErrorStackUnderflow;

/**
 * Stack implementation using an ArrayList.
 * 
 * @param <E> Type of objects stored in this stack.
 * 
 * @author Paul Wagenaars
 */
public class ArrayStack<E> extends ArrayList<E> {
    
    /** Serial version UID field. */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of ArrayStack.
     */
    public ArrayStack() {
        super();
    }
    
    /**
     * Creates a new ArrayStack with initial capacity.
     * 
     * @param initialCapacity The initial capacity.
     */
    public ArrayStack(final int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Tests if the stack is empty.
     * 
     * @return True, if stack is empty.
     */
    public boolean empty() {
        return isEmpty();
    }
    
    /**
     * Pushes an object on top of the stack.
     * 
     * @param item The item to add.
     * 
     * @return The added item.
     */
    public E push(final E item) {
        add(item);
        return item;
    }
    
    /**
     * Pops an object from the top of the stack.
     * 
     * @return The top-most element.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public E pop() throws PSErrorStackUnderflow {
        int sz = size();
        if (sz <= 0) {
            throw new PSErrorStackUnderflow();
        } else {
            return remove(sz - 1);
        }
    }
    
    /**
     * Looks at the n'th item from the top of the stack without removing
     * it. n = 0 corresponds to the top of the stack.
     * 
     * @param n Index (starting from top) of element to peek.
     * 
     * @return Element with index n.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public E peek(final int n) throws PSErrorStackUnderflow {
        int index = size() - 1 - n;
        if (index < 0) {
            throw new PSErrorStackUnderflow();
        } else {
            return get(index);
        }
    }
    
    /**
     * Looks at the object at the top of this stack without removing it
     * from the stack.
     * 
     * @return The top-most element.
     * 
     * @throws PSErrorStackUnderflow Tried to pop an object from an empty stack.
     */
    public E peek() throws PSErrorStackUnderflow {
        return peek(0);
    }
}
