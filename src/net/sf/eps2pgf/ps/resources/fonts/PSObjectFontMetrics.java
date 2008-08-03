/*
 * PSObjectFontMetrics.java
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

package net.sf.eps2pgf.ps.resources.fonts;

import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectDict;

import org.fontbox.afm.FontMetric;

/**
 * Wrapper class to wrap font metric information loaded by FontBox in a
 * PostScript object.
 * @author Paul Wagenaars
 */
public class PSObjectFontMetrics extends PSObject implements Cloneable {
    
    /** Stores all font metrics. */
    private FontMetric fontMetrics;
    
    /** Subrs entry from private dictionary. */
    private List<List<PSObject>> subrs;
    
    /**
     * Creates a new instance of PSObjectFontMetrics.
     *
     * @param pFontMetrics Glyph metrics.
     * @param pSubrs Subrs entry from private dictionary.
     */
    public PSObjectFontMetrics(final FontMetric pFontMetrics,
            final List<List<PSObject>> pSubrs) {
        
        fontMetrics = pFontMetrics;
        subrs = pSubrs;
    }
    
    /**
     * Creates a new instance of PSObjectFontMetrics.
     *
     * @param pFontMetrics Glyph metrics.
     */
    public PSObjectFontMetrics(final FontMetric pFontMetrics) {
        fontMetrics = pFontMetrics;
        subrs = new ArrayList<List<PSObject>>();
    }
    
    /**
     * Instantiates a new font metrics object. First it checks the
     * FontType entry and then extract the metric data from the
     * corresponding font dictionary entries.
     * 
     * @param fontDict Font dictionary of the font.
     * 
     * @throws PSError a PostScript error occurred
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectFontMetrics(final PSObjectDict fontDict)
            throws PSError, ProgramError {
        
        int fontType;
        try {
            fontType = fontDict.get("FontType").toInt();
        } catch (PSErrorUndefined e) {
            throw new PSErrorInvalidFont("FontType is not defined.");
        } catch (PSErrorTypeCheck e) {
            throw new PSErrorInvalidFont("FontType is not an integer.");
        }
        
        switch (fontType) {
            case 1:
                Type1.load(this, fontDict);
                break;
            case 3:
                Type3.load(this, fontDict);
                break;
            default:
                throw new PSErrorUnimplemented("type " + fontType + " fonts");
        }
            
    }
    
    /**
     * Creates a (deep) copy of this object.
     * 
     * @return Deep copy of this object
     */
    @Override
    public PSObjectFontMetrics clone() {
        PSObjectFontMetrics copy = (PSObjectFontMetrics) super.clone();
        copy.fontMetrics = fontMetrics;
        copy.subrs = subrs;
        return new PSObjectFontMetrics(fontMetrics, subrs);
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectFontMetrics dup() {
        PSObjectFontMetrics dupM = new PSObjectFontMetrics(fontMetrics, subrs);
        dupM.copyCommonAttributes(this);
        return dupM;
    }

    /**
     * Indicates whether some other object is equal to this one.
     * Required when used as index in PSObjectDict
     * 
     * @param obj The object to compare to.
     * 
     * @return True, if equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PSObject) {
            return eq((PSObject) obj);
        } else {
            return false;
        }
    }
    
    
    /**
     * Returns a hash code value for the object.
     * 
     * @return Hash code of this object.
     */
    @Override
    public int hashCode() {
        return isis().hashCode();
    }
    
    
    
    /**
     * Returns the FontMetric object of this font.
     * 
     * @return FontMetric object
     */
    @Override
    public FontMetric toFontMetric() {
        return fontMetrics;
    }

    
    /**
     * Get the fontMetric field.
     * 
     * @return The fontMetrics field.
     */
    public FontMetric getFontMetrics() {
        return fontMetrics;
    }
    
    /**
     * Set the fontMetric field.
     * 
     * @param pFontMetrics The font metrics.
     */
    public void setFontMetrics(final FontMetric pFontMetrics) {
        fontMetrics = pFontMetrics;
    }
    
    /**
     * Get the subrs field.
     * 
     * @return the subrs field.
     */
    public List<List<PSObject>> getSubrs() {
        return subrs;
    }
    
    /**
     * Set the subrs field.
     * 
     * @param pSubrs The subrs
     */
    public void setSubrs(final List<List<PSObject>> pSubrs) {
        subrs = pSubrs;
    }
    
}
