/*
 * PSObject.java
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

package eps2pgf.postscript;

import eps2pgf.postscript.errors.*;

/** Base class for PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class PSObject implements Cloneable {
    public boolean isLiteral = true;
    
    /**
     * Checks whether the supplied string is of this type.
     * @param str String to be checked.
     * @return Return true when the string of the objects type.
     */
    public static boolean isType(String str) {
        return false;
    }

    /** Executes this object in the supplied interpreter */
    public void execute(Interpreter interp) throws Exception {
        interp.opStack.push(this);
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() throws PSError {
        return this.getClass().getName();
    }
    
    /** Convert this object to an integer, if possible. */
    public int toInt() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /** Convert this object to a non-negative integer, if possible */
    public int toNonNegInt() throws PSError {
        int n = toInt();
        if (n < 0) {
            throw new PSErrorRangeCheck();
        }
        return n;
    }
    
    /** Convert this object to a real number, if possible. */
    public double toReal() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /** Convert this object to dictionary key, if possible. */
    public String toDictKey() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /** Convert this object to a boolean, if possible. */
    public boolean toBool() throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /** Creates a copy of this object. */
    public PSObject clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    /** Implements PostScript operator getinterval. Returns a new object
     * with an interval from this object. */
    public PSObject getinterval(int index, int count) throws PSError {
        throw new PSErrorTypeCheck();
    }
    
    /** Implements bind operator for this object. For most object this wil
     * be the same object, without any change.
     */
    public PSObject bind(Interpreter interp) throws PSError {
        return this;
    }
}
