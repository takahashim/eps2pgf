/*
 * PSObjectBool.java
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

/** Represent PostScript object: boolean
 *
 * @author Paul Wagenaars
 */
public class PSObjectBool extends PSObject {
    boolean value = false;
    
    /** Creates a new instance of PSObjectBool */
    public PSObjectBool(boolean bool) {
        value = bool;
    }
    
    /** Convert this object to a boolean, if possible. */
    public boolean toBool() {
        return value;
    }
    
    /** Return PostScript text representation of this object. See the
     * PostScript manual under the == operator
     */
    public String isis() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }
}
