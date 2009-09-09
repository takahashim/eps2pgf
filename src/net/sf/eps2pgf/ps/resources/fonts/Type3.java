/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

import org.fontbox.afm.CharMetric;
import org.fontbox.afm.FontMetric;
import org.fontbox.util.BoundingBox;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;

/**
 * Utility class to load metrics of type 3 fonts.
 */
public final class Type3 {
    
    /**
     * "Hidden" constructor.
     */
    private Type3() {
        /* emppty block */
    }
    
    /**
     * Load metrics from a Type 3 font.
     * 
     * @param fontDict font dictionary describing a Type 3 font
     * @param fMetrics The font metrics object where the loaded metrics will be
     * written to.
     * @param interp The interpreter.
     * 
     * @throws PSError a PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static void load(final PSObjectFontMetrics fMetrics, 
            final PSObjectDict fontDict, final Interpreter interp)
            throws PSError, ProgramError {
        
        PSObjectArray buildGlyph;
        try {
            buildGlyph = fontDict.get(PSObjectFont.KEY_BUILDGLYPH).toProc();
        } catch (PSErrorUndefined e) {
            buildGlyph = null;
        } catch (PSErrorTypeCheck e) {
            throw new PSErrorInvalidFont("Entry " + PSObjectFont.KEY_BUILDGLYPH
                    + " is not a procedure");
        }
        
        // If BuildGlyph is not defined. Apparently this is an old
        // (version 1) font. Instead we load the BuildChar procedure.
        PSObjectArray buildChar = null;
        if (buildGlyph == null) {
            try {
                buildChar =
                    fontDict.get(PSObjectFont.KEY_BUILDCHAR).toProc();
            } catch (PSErrorUndefined e) {
                throw new PSErrorInvalidFont("Required entry ("
                        + PSObjectFont.KEY_BUILDCHAR + ") is not defined");
            } catch (PSErrorTypeCheck e) {
                throw new PSErrorInvalidFont("Entry "
                        + PSObjectFont.KEY_BUILDCHAR + " is not an array");
            }
        }
        
        // Create a list of all unique charNames and their corresponding 
        // character codes.
        List<PSObject> encoding =
            fontDict.get(PSObjectFont.KEY_ENCODING).getItemList();
        encoding.remove(0);  // remove the first item, for an array it is
                             // always 1.
        List<PSObject> charNames = new ArrayList<PSObject>();
        List<Integer> charCodes = new ArrayList<Integer>();
        for (int i = 0; i < encoding.size(); i++) {
            PSObject charName = encoding.get(i);
            // Check whether this character was processed already
            if (charNames.contains(charName)) {
                continue;
            }
            charNames.add(charName);
            charCodes.add(i);
        }
        
        // Loop through all characters and execute their procedures
        fMetrics.setFontMetrics(new FontMetric());
        for (int i = 0; i < charNames.size(); i++) {
            // Create a procedure to calculate the character metrics
            PSObjectArray proc = new PSObjectArray(interp);
            proc.addToEnd(interp.getOpsGI().grestore);
            proc.addToEnd(interp.getOpsEps2pgf().eps2pgfGetmetrics);
            if (buildGlyph != null) {
                proc.addToEnd(buildGlyph);
                proc.addToEnd(charNames.get(i));
            } else {
                proc.addToEnd(buildChar);
                proc.addToEnd(new PSObjectInt(charCodes.get(i)));
            }
            proc.addToEnd(fontDict);
            proc.addToEnd(interp.getOpsGI().gsave);
            proc.cvx();
            
            // Execute the code
            interp.runObject(proc);
            
            // Get the metrics
            String charName = charNames.get(i).toString();
            PSObjectArray metrics = interp.getOpStack().pop().toArray();
            
            // Create the metric object for this character
            CharMetric charMetric = new CharMetric();
            charMetric.setName(charName);
            charMetric.setWx((float) metrics.get(0).toReal());
            charMetric.setWy((float) metrics.get(1).toReal());
            BoundingBox boundingBox = new BoundingBox();
            boundingBox.setLowerLeftX((float) metrics.get(2).toReal());
            boundingBox.setLowerLeftY((float) metrics.get(3).toReal());
            boundingBox.setUpperRightX((float) metrics.get(4).toReal());
            boundingBox.setUpperRightY((float) metrics.get(5).toReal());
            charMetric.setBoundingBox(boundingBox);
            
            // Add the character metrics object to the font metrics object
            fMetrics.getFontMetrics().addCharMetric(charMetric);
            
        }  // end of loop through all characters
    } // end of load() method

}
