/*
 * PSObjectMark.java
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

/** Represent PostScript mark
 *
 * @author Paul Wagenaars
 */
public class PSObjectMark extends PSObject {
    
    /**
     * Create new mark object
     */
    public PSObjectMark() {
        
    }
    
    /**
     * Create new mark object
     */
    public PSObjectMark(PSObjectMark obj) {
        copyCommonAttributes(obj);
    }
    
    /**
     * PostScript operator 'dup'. Create a copy of this object. The values
     * of composite object is not copied, but shared.
     */
    public PSObjectMark dup() {
        return new PSObjectMark(this);
    }
    
    /**
     * Compare this object with another object and return true if they are equal.
     * See PostScript manual on what's equal and what's not.
     * @param obj Object to compare this object with
     * @return True if objects are equal, false otherwise
     */
    public boolean eq(PSObject obj) {
        return (obj instanceof PSObjectMark);
    }
    
    public String isis() {
        return "-mark-";
    }

    /**
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "marktype";
    }
}
