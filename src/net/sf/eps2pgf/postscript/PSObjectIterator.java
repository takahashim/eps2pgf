/*
 * PSObjectArrayIterator.java
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

package net.sf.eps2pgf.postscript;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;

/**
 *
 * @author Paul Wagenaars
 */
public class PSObjectIterator implements Iterator<PSObject> {
    private PSObject obj;
    private int nextIndex;
    
    /** Creates a new instance of PSObjectArrayIterator */
    public PSObjectIterator(PSObject newObj) {
        obj = newObj;
        nextIndex = 0;
    }
    
    /**
     * Returns true if the iteration has more elements. (In other words, return true if next would return an
     * element rather than throwing an exception.
     * @return true if the iterator has more elements
     */
    public boolean hasNext() {
        try {
            return (nextIndex < obj.length());
        } catch (PSErrorTypeCheck e) {
            return false;
        }
    }
    
    /**
     * Returns the next element in the iteration. Callnig this method repeatedly until the hasNext() method
     * returns false will return each element in the underlying collection exactly once.
     * @return The next element in the iteration.
     * @throws NoSuchElementException Iteration has no more elements.
     */
    public PSObject next() throws NoSuchElementException {
        try {
            PSObject retObj = obj.get(new PSObjectInt(nextIndex));
            nextIndex++;
            return retObj;
        } catch (PSError e) {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Not implemented in this iterator. Always throws UnsupportedOperationException
     * @throws UnsupportedOperationException This operation is not supported by this iterator.
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
