/*
 * PSObjectArrayIterator.java
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;

/**
 *
 * @author Paul Wagenaars
 */
public class PSObjectIterator implements Iterator {
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
