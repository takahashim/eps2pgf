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
import net.sf.eps2pgf.ps.VM;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectString;

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
     * @param vm The virtual memory manager.
     * 
     * @throws PSError a PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static void load(final PSObjectFontMetrics fMetrics, 
            final PSObjectDict fontDict, final VM vm)
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
        
        // Create a temporary in which the characters will be processed
        try {
            Interpreter fi = new Interpreter();
            
            // Load some often used procedures to make execution faster
            fi.getExecStack().push(new PSObjectString("systemdict /gsave get "
               + "systemdict /grestore get systemdict /eps2pgfgetmetrics get",
               vm));
            fi.run();
            PSObject getmetrics = fi.getOpStack().pop();
            PSObject grestore = fi.getOpStack().pop();
            PSObject gsave = fi.getOpStack().pop();

            // Create a list of all unique charNames and their corresponding 
            // character codes.
            List<PSObject> encoding = fontDict.get(PSObjectFont.KEY_ENCODING)
                                                                 .getItemList();
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

            // Next Step: fill the temporary interpreter with code to run the
            // buildGlyph/Char procedures.
            if (buildGlyph != null) {
                for (int i = 0; i < charNames.size(); i++) {
                    // Fill the execution stack with code
                    fi.getExecStack().push(grestore);
                    fi.getExecStack().push(getmetrics);
                    fi.getExecStack().push(buildGlyph);
                    fi.getExecStack().push(charNames.get(i));
                    fi.getExecStack().push(fontDict);
                    fi.getExecStack().push(gsave);
                }
            } else {
                // BuildGlyph is not defined. Apparently this is an old
                // (version 1) font. Instead we load the BuildChar procedure.
                PSObjectArray buildChar;
                try {
                    buildChar = fontDict.get(PSObjectFont.KEY_BUILDCHAR)
                                                                      .toProc();
                } catch (PSErrorUndefined e) {
                    throw new PSErrorInvalidFont("Required entry ("
                            + PSObjectFont.KEY_BUILDCHAR + ") is not defined");
                } catch (PSErrorTypeCheck e) {
                    throw new PSErrorInvalidFont("Entry "
                            + PSObjectFont.KEY_BUILDCHAR + " is not an array");
                }
                
                for (int i = 0; i < charNames.size(); i++) {
                    // Fill the execution stack with code
                    fi.getExecStack().push(grestore);
                    fi.getExecStack().push(getmetrics);
                    fi.getExecStack().push(buildChar);
                    fi.getExecStack().push(new PSObjectInt(charCodes.get(i)));
                    fi.getExecStack().push(fontDict);
                    fi.getExecStack().push(gsave);
                }
            }
            
            fi.run();
            
            fMetrics.setFontMetrics(new FontMetric());
            for (int i = 0; i < charNames.size(); i++) {
                String charName = charNames.get(i).toString();
                PSObjectArray metrics = fi.getOpStack().pop().toArray();
                
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
                fMetrics.getFontMetrics().addCharMetric(charMetric);
            }

        } catch (PSError e) {
            // Catch all errors produces by the font code
            throw new ProgramError("Exception occurred in loadType3, which"
                    + " should not be possible.\n(Error generated by font: "
                    + e + ")");
        } catch (ProgramError e) {
            // Catch all errors produces by the font code
            throw new ProgramError("Exception occurred in loadType3, which"
                    + " should not be possible.\n(Error generated by font: "
                    + e + ")");
        }
        
        
    }

}
