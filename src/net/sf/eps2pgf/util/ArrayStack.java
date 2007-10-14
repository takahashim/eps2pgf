/*
 * ArrayStack.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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
    public E pop() throws PSErrorStackUnderflow {
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
    public E peek(int n) throws PSErrorStackUnderflow {
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
    public E peek() throws PSErrorStackUnderflow {
        return peek(0);
    }
}
