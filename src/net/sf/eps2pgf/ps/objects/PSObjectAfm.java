/*
 * PSObjectAfm.java
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

package net.sf.eps2pgf.ps.objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.StringInputStream;
import net.sf.eps2pgf.ps.GstateStack;
import net.sf.eps2pgf.ps.Interpreter;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.errors.PSErrorUnimplemented;
import net.sf.eps2pgf.ps.resources.filters.EexecDecode;
import net.sf.eps2pgf.ps.resources.outputdevices.NullDevice;
import net.sf.eps2pgf.util.ArrayStack;

import org.fontbox.afm.CharMetric;
import org.fontbox.afm.FontMetric;
import org.fontbox.util.BoundingBox;

/**
 * Wrapper class to wrap font metric information loaded by FontBox in a
 * PostScript object.
 * @author Paul Wagenaars
 */
public class PSObjectAfm extends PSObject implements Cloneable {
    
    /** Stores all font metrics. */
    private FontMetric fontMetrics;
    
    /** Subrs entry from private dictionary. */
    private List<List<PSObject>> subrs;
    
    /** Creates a new instance of PSObjectAfm */
    /**
     * @param pFontMetrics Glyph metrics.
     * @param pSubrs Subrs entry from private dictionary.
     */
    public PSObjectAfm(final FontMetric pFontMetrics,
            final List<List<PSObject>> pSubrs) {
        
        fontMetrics = pFontMetrics;
        subrs = pSubrs;
    }
    
    /** Creates a new instance of PSObjectAfm */
    /**
     * @param pFontMetrics Glyph metrics.
     */
    public PSObjectAfm(final FontMetric pFontMetrics) {
        fontMetrics = pFontMetrics;
        subrs = new ArrayList<List<PSObject>>();
    }
    
    /**
     * Instantiates a new font metric (AFM) object. First it checks the
     * FontType entry and then extract the metric data from the
     * corresponding font dictionary entries.
     * 
     * @param fontDict Font dictionary of the font.
     * 
     * @throws PSError a PostScript error occurred
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public PSObjectAfm(final PSObjectDict fontDict)
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
                loadType1(fontDict);
                break;
            case 3:
                loadType3(fontDict);
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
    public PSObjectAfm clone() {
        PSObjectAfm copy = (PSObjectAfm) super.clone();
        copy.fontMetrics = fontMetrics;
        copy.subrs = subrs;
        return new PSObjectAfm(fontMetrics, subrs);
    }
    
    /**
     * Convert a PostScript CharString to a CharMetric object.
     * 
     * @param charName The character name.
     * @param charString The character string.
     * 
     * @return Character metric
     * 
     * @throws PSError A PostScript error occurred.
     */
    public CharMetric charString2CharMetric(final String charName,
            final String charString) throws PSError {
        
        StringInputStream strInStream = new StringInputStream(charString);
        InputStream decodedCharString =
            new EexecDecode(strInStream, 4330, true);
        List<PSObject> tokens = decodeCharString(decodedCharString);
        int[] sb = new int[2];
        int[] w = new int[2];
        Path charPath = interpretCharString(tokens, sb, w);
        double[] bbox;
        if (charPath.getSections().size() > 1) {
            bbox = charPath.boundingBox();
        } else {
            bbox = new double[4]; 
        }
        
        CharMetric charMetric = new CharMetric();
        charMetric.setName(charName);
        charMetric.setWx(w[0]);
        charMetric.setWy(w[1]);
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.setLowerLeftX((float) bbox[0]);
        boundingBox.setLowerLeftY((float) bbox[1]);
        boundingBox.setUpperRightX((float) bbox[2]);
        boundingBox.setUpperRightY((float) bbox[3]);
        charMetric.setBoundingBox(boundingBox);
        
        return charMetric;
    }
    
    /**
     * Convert a list of integers to CharString integers and commands.
     * 
     * @param in Decrypted InputStream of CharString
     * 
     * @return List with CharString integers and commands. The commands are
     * represented as executable names.
     * 
     * @throws PSErrorInvalidFont The font is invalid.
     */
    public List<PSObject> decodeCharString(final InputStream in)
            throws PSErrorInvalidFont {
        List<PSObject> out = new ArrayList<PSObject>();
        try {
            while (true) {
                int v = in.read();
                if (v == -1) {
                    break;
                }
                
                if (v <= 31) {
                    // it's a command
                    String cmd;
                    switch (v) {
                        case  1: cmd = "hstem";     break;
                        case  3: cmd = "vstem";     break;
                        case  4: cmd = "vmoveto";   break;
                        case  5: cmd = "rlineto";   break;
                        case  6: cmd = "hlineto";   break;
                        case  7: cmd = "vlineto";   break;
                        case  8: cmd = "rrcurveto"; break;
                        case  9: cmd = "closepath"; break;
                        case 10: cmd = "callsubr";  break;
                        case 11: cmd = "return";    break;
                        case 13: cmd = "hsbw";      break;
                        case 14: cmd = "endchar";   break;
                        case 21: cmd = "rmoveto";   break;
                        case 22: cmd = "hmoveto";   break;
                        case 30: cmd = "vhcurveto"; break;
                        case 31: cmd = "hvcurveto"; break;
                        case 12:
                            int w = in.read();
                            switch (w) {
                                case 0:  cmd = "dotsection";      break;
                                case 1:  cmd = "vstem3";          break;
                                case 2:  cmd = "hstem3";          break;
                                case 6:  cmd = "seac";            break;
                                case 7:  cmd = "sbw";             break;
                                case 12: cmd = "div";             break;
                                case 16: cmd = "callothersubr";   break;
                                case 17: cmd = "pop";             break;
                                case 33: cmd = "setcurrentpoint"; break;
                                default:
                                    throw new PSErrorInvalidFont();
                            }
                            break;
                        default:
                            throw new PSErrorInvalidFont();
                    }
                    out.add(new PSObjectName(cmd, false));
                } else if (v <= 246) {
                    // it's a single byte integer
                    out.add(new PSObjectInt(v - 139));
                } else if (v <= 250) {
                    // it's a two byte positive integer
                    int w = in.read();
                    out.add(new PSObjectInt(((v - 247) * 256) + w + 108));
                } else if (v <= 254) {
                    // it's a two byte negative integer
                    int w = in.read();
                    out.add(new PSObjectInt(-((v - 251) * 256) - w - 108));
                } else {
                    // it's a 32-bit bit integer (5 bytes in total)
                    int b3 = in.read();
                    int b2 = in.read();
                    int b1 = in.read();
                    int b0 = in.read();
                    out.add(new PSObjectInt((b3 << 24) | (b2 << 16) | (b1 << 8)
                            | (b0)));
                }
            }
        } catch (IOException e) {
            // this should not happen
        }
        
        return out;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The
     * values of composite object is not copied, but shared.
     * 
     * @return Shallow copy of this object.
     */
    @Override
    public PSObjectAfm dup() {
        PSObjectAfm dupAfm = new PSObjectAfm(fontMetrics, subrs);
        dupAfm.copyCommonAttributes(this);
        return dupAfm;
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
     * Parses the subrs list.
     * 
     * @param pSubrs Array with subroutines. Value of 'Subrs' entry in 'Private'
     * dictionary.
     * 
     * @return List with all subroutines. Each subroutines consists of a list
     * with PostScript objects.
     * 
     * @throws PSError A PostScript error occured.
     */
    public List<List<PSObject>> parseSubrs(final PSObjectArray pSubrs)
            throws PSError {
        
        int nrSubrs = pSubrs.size();
        List<List<PSObject>> subrList = new ArrayList<List<PSObject>>(nrSubrs);
        for (int i = 0; i < nrSubrs; i++) {
            PSObjectString inString = pSubrs.get(i).toPSString();
            StringInputStream inStream = new StringInputStream(inString
                                                                   .toString());
            InputStream decodedStream = new EexecDecode(inStream, 4330, true);
            List<PSObject> subroutine = decodeCharString(decodedStream);
            subrList.add(subroutine);
        }
        return subrList;
    }
    
    /**
     * Interpret a CharString and builds the path.
     * 
     * @param execStack Stack with object that will be executed.
     * @param paramSb Pointer to array with two values. These values are the X-
     * and Y-coordinate of the left side bearing.
     * @param paramW Pointer to array with two values. These values are the X-
     * and Y-coordinate of the 'width' vector.
     * 
     * @return Path describing the character
     * 
     * @throws PSError A PostScript error occurred.
     */
    public Path interpretCharString(final List<PSObject> execStack,
            final int[] paramSb, final int[] paramW) throws PSError {
        GstateStack gstate = new GstateStack(new NullDevice());
        ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
        
        while (!execStack.isEmpty()) {
            PSObject obj = execStack.remove(0);
            
            if (obj instanceof PSObjectInt) {
                opStack.add(obj);
            } else {
                String cmd = obj.toName().toString();
                if (cmd.equals("rlineto")) {
                    int dy = opStack.pop().toInt();
                    int dx = opStack.pop().toInt();
                    gstate.current().rlineto(dx, dy);
                } else if (cmd.equals("hlineto")) {
                    int dx = opStack.pop().toInt();
                    gstate.current().rlineto(dx, 0);
                } else if (cmd.equals("vlineto")) {
                    int dy = opStack.pop().toInt();
                    gstate.current().rmoveto(0, dy);
                } else if (cmd.equals("rrcurveto")) {
                    int dy3 = opStack.pop().toInt();
                    int dx3 = opStack.pop().toInt();
                    int dy2 = opStack.pop().toInt();
                    int dx2 = opStack.pop().toInt();
                    int dy1 = opStack.pop().toInt();
                    int dx1 = opStack.pop().toInt();
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("vhcurveto")) {
                    int dy3 = 0;
                    int dx3 = opStack.pop().toInt();
                    int dy2 = opStack.pop().toInt();
                    int dx2 = opStack.pop().toInt();
                    int dy1 = opStack.pop().toInt();
                    int dx1 = 0;
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("hvcurveto")) {
                    int dy3 = opStack.pop().toInt();
                    int dx3 = 0;
                    int dy2 = opStack.pop().toInt();
                    int dx2 = opStack.pop().toInt();
                    int dy1 = 0;
                    int dx1 = opStack.pop().toInt();
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("hstem")) {
                    // Not much to for this command, except for popping two
                    // values from the stack.
                    opStack.pop().toInt();  // dy
                    opStack.pop().toInt();  // y
                } else if (cmd.equals("vstem")) {
                    // Not much to for this command, except for popping two
                    // values from the stack.
                    opStack.pop().toInt();  // dx
                    opStack.pop().toInt();  // x
                } else if (cmd.equals("hsbw")) {
                    int wx = opStack.pop().toInt();
                    int sbx = opStack.pop().toInt();
                    gstate.current().setPosition(sbx, 0);
                    paramSb[0] = sbx;
                    paramSb[1] = 0;
                    paramW[0] = wx;
                    paramW[1] = 0;
                } else if (cmd.equals("rmoveto")) {
                    int dy = opStack.pop().toInt();
                    int dx = opStack.pop().toInt();
                    gstate.current().rmoveto(dx, dy);
                } else if (cmd.equals("closepath")) {
                    gstate.current().getPath().closepath();
                } else if (cmd.equals("endchar")) {
                    break;
                } else if (cmd.equals("hmoveto")) {
                    int dx = opStack.pop().toInt();
                    gstate.current().rmoveto(dx, 0);
                } else if (cmd.equals("vmoveto")) {
                    int dy = opStack.pop().toInt();
                    gstate.current().rmoveto(0, dy);
                } else if (cmd.equals("hstem3")) {
                    // This doesn't do anything, just remove the argument from
                    // the stack.
                    for (int i = 0; i < 6; i++) {
                        opStack.pop();
                    }
                } else if (cmd.equals("vstem3")) {
                    // This doesn't do anything, just remove the argument from
                    // the stack.
                    for (int i = 0; i < 6; i++) {
                        opStack.pop();
                    }
                } else if (cmd.equals("callsubr")) {
                    int subrnr = opStack.pop().toInt();
                    execStack.addAll(0, subrs.get(subrnr));
                } else if (cmd.equals("return")) {
                    // There's nothing to do for this command. Since it will
                    // return automatically/
                } else if (cmd.equals("pop")) {
                    // Calls to OtherSubrs are ignored. That means that 'pop'
                    // can also be ignored.
                } else if (cmd.equals("callothersubr")) {
                    // Ignored, only required when rasterizing a font.
                } else {
                    System.out.println("-=-=- Unknown command: " + cmd);
                }
            }
        }
        
        gstate.current().moveto(paramW[0], paramW[1]);
        
        return gstate.current().getPath();
    }
    
    /**
     * Load metrics data from CharStrings (Type 1 font).
     * 
     * @param fontDict font dictionary describing a type 1 font
     * 
     * @throws PSError a PostScript error occurred
     */
    void loadType1(final PSObjectDict fontDict) throws PSError {
        // Extract metrics information from character descriptions
        PSObjectDict charStrings;
        try {
            charStrings = fontDict.get(PSObjectFont.KEY_CHARSTRINGS).toDict();
        } catch (PSErrorUndefined e) {
            throw new PSErrorInvalidFont("Required entry (" 
                    + PSObjectFont.KEY_CHARSTRINGS + ") not defined");
        }
        PSObjectDict privateDict;
        try {
            privateDict = fontDict.get(PSObjectFont.KEY_PRIVATE).toDict();
        } catch (PSErrorUndefined e) {
            throw new PSErrorInvalidFont("Required entry (" 
                    + PSObjectFont.KEY_PRIVATE + ") not defined");
        }

        // Parse Subrs entry in private dictionary
        PSObjectArray subrsArray = privateDict.get(PSObjectFont.KEY_PRV_SUBRS)
                                                                     .toArray();
        this.subrs = parseSubrs(subrsArray);
        
        this.fontMetrics = new FontMetric();
        List<PSObject> items = charStrings.getItemList();
        try {
            for (int i = 1; i < items.size(); i += 2) {
                PSObjectName charName = items.get(i).toName();
                PSObjectString charString = items.get(i + 1).toPSString();
                CharMetric charMetric = charString2CharMetric(
                        charName.toString(), charString.toString());
                this.fontMetrics.addCharMetric(charMetric);
            }
        } catch (PSError e) {
            throw new PSErrorInvalidFont();
        }
    }
    
    
    
    /**
     * Load metrics from a Type 3 font.
     * 
     * @param fontDict font dictionary describing a Type 3 font
     * 
     * @throws PSError a PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    void loadType3(final PSObjectDict fontDict) throws PSError, ProgramError {
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
               + "systemdict /grestore get systemdict /eps2pgfgetmetrics get"));
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
            
            this.fontMetrics = new FontMetric();
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
                this.fontMetrics.addCharMetric(charMetric);
            }

        } catch (PSError e) {
            // Catch all errors produces by the font code
            throw new ProgramError("Exception occurred in loadType3, which"
                    + " should not be possible.\n(Error generated by font: "
                    + e + ")");
        } catch (IOException e) {
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
    
    
    /**
     * Returns the FontMetric object of this font.
     * 
     * @return FontMetric object
     */
    @Override
    public FontMetric toFontMetric() {
        return fontMetrics;
    }
    
}