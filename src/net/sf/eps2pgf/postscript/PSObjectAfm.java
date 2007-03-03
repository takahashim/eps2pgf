/*
 * PSObjectAfm.java
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

import org.fontbox.afm.*;

/**
 * Wrapper class to wrap font metric information loaded by FontBox in a
 * PostScript object.
 * @author Paul Wagenaars
 */
public class PSObjectAfm extends PSObject implements Cloneable {
    FontMetric fontMetrics;
    
    /** Creates a new instance of PSObjectAfm */
    public PSObjectAfm(FontMetric aFontMetrics) {
        fontMetrics = aFontMetrics;
    }
    
    /**
     * Create a shallow copy of this object. The fontMetrics object it not copied.
     */
    public PSObjectAfm clone() {
        return new PSObjectAfm(fontMetrics);
    }
    
    /**
     * Returns the FontMetric object of this font
     * @return FontMetric object
     */
    public FontMetric toFontMetric() {
        return fontMetrics;
    }
    
}
