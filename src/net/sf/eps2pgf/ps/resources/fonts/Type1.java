/*
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.fontbox.afm.CharMetric;
import org.fontbox.afm.FontMetric;
import org.fontbox.util.BoundingBox;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.io.StringInputStream;
import net.sf.eps2pgf.ps.GstateStack;
import net.sf.eps2pgf.ps.Path;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.ps.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUndefined;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFont;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectNull;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.filters.EexecDecode;
import net.sf.eps2pgf.ps.resources.outputdevices.NullDevice;
import net.sf.eps2pgf.util.ArrayStack;

/**
 * Utility class to load metrics of type 1 fonts.
 */
final class Type1 {
    
    /**
     * "Hidden" constructor.
     */
    private Type1() {
        /* empty block */
    }
    
    /**
     * Load metrics data from CharStrings (Type 1 font).
     * 
     * @param fontDict font dictionary describing a type 1 font
     * @param fMetrics The font metrics object where the loaded metrics will be
     * stored.
     * 
     * @throws PSError a PostScript error occurred
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static void load(final PSObjectFontMetrics fMetrics,
            final PSObjectDict fontDict) throws PSError, ProgramError {
        
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
        fMetrics.setSubrs(parseSubrs(subrsArray));
        
        fMetrics.setFontMetrics(new FontMetric());
        List<PSObject> items = charStrings.getItemList();
        try {
            for (int i = 1; i < items.size(); i += 2) {
                PSObjectName charName = items.get(i).toName();
                PSObjectString charString = items.get(i + 1).toPSString();
                CharMetric charMetric = charString2CharMetric(fMetrics,
                        charName.toString(), charString.toString());
                fMetrics.getFontMetrics().addCharMetric(charMetric);
            }
        } catch (PSError e) {
            throw new PSErrorInvalidFont();
        }
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
    private static List<List<PSObject>> parseSubrs(final PSObjectArray pSubrs)
            throws PSError {
        
        int nrSubrs = pSubrs.size();
        List<List<PSObject>> subrList = new ArrayList<List<PSObject>>(nrSubrs);
        for (int i = 0; i < nrSubrs; i++) {
            PSObject inObj = pSubrs.get(i);
            List<PSObject> subroutine;
            if (inObj instanceof PSObjectString) {
                PSObjectString inString = pSubrs.get(i).toPSString();
                StringInputStream inStream =
                    new StringInputStream(inString.toString());
                InputStream decodedStream = new EexecDecode(inStream, 4330,
                        true);
                subroutine = decodeCharString(decodedStream);
            } else if (inObj instanceof PSObjectNull) {
                subroutine = new ArrayList<PSObject>();
            } else {
                throw new PSErrorTypeCheck();
            }
            subrList.add(subroutine);
        }
        return subrList;
    }    

    /**
     * Convert a PostScript CharString to a CharMetric object.
     * 
     * @param charName The character name.
     * @param charString The character string.
     * @param fMetrics The font metrics.
     * 
     * @return Character metric
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static CharMetric charString2CharMetric(
            final PSObjectFontMetrics fMetrics, final String charName,
            final String charString) throws PSError, ProgramError {
        
        StringInputStream strInStream = new StringInputStream(charString);
        InputStream decodedCharString =
            new EexecDecode(strInStream, 4330, true);
        List<PSObject> tokens = decodeCharString(decodedCharString);
        double[] sb = new double[2];
        double[] w = new double[2];
        Path charPath = interpretCharString(fMetrics, tokens, sb, w);
        double[] bbox;
        if (charPath.getSections().size() > 1) {
            bbox = charPath.boundingBox();
        } else {
            bbox = new double[4]; 
        }
        
        CharMetric charMetric = new CharMetric();
        charMetric.setName(charName);
        charMetric.setWx((int) Math.round(w[0]));
        charMetric.setWy((int) Math.round(w[1]));
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
    public static List<PSObject> decodeCharString(final InputStream in)
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
     * Interpret a CharString and builds the path.
     * 
     * @param execStack Stack with object that will be executed.
     * @param paramSb Pointer to array with two values. These values are the X-
     * and Y-coordinate of the left side bearing.
     * @param paramW Pointer to array with two values. These values are the X-
     * and Y-coordinate of the 'width' vector.
     * @param fMetrics The font metrics object where the metrics data will be
     * stored.
     * 
     * @return Path describing the character
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static Path interpretCharString(final PSObjectFontMetrics fMetrics,
            final List<PSObject> execStack, final double[] paramSb,
            final double[] paramW) throws PSError, ProgramError {
        
        GstateStack gstate = new GstateStack(new NullDevice());
        ArrayStack<PSObject> opStack = new ArrayStack<PSObject>();
        
        while (!execStack.isEmpty()) {
            PSObject obj = execStack.remove(0);
            
            if (obj instanceof PSObjectInt) {
                opStack.add(obj);
            } else {
                String cmd = obj.toName().toString();
                if (cmd.equals("rlineto")) {
                    double dy = opStack.pop().toReal();
                    double dx = opStack.pop().toReal();
                    gstate.current().rlineto(dx, dy);
                } else if (cmd.equals("hlineto")) {
                    double dx = opStack.pop().toReal();
                    gstate.current().rlineto(dx, 0);
                } else if (cmd.equals("vlineto")) {
                    double dy = opStack.pop().toReal();
                    gstate.current().rmoveto(0, dy);
                } else if (cmd.equals("rrcurveto")) {
                    double dy3 = opStack.pop().toReal();
                    double dx3 = opStack.pop().toReal();
                    double dy2 = opStack.pop().toReal();
                    double dx2 = opStack.pop().toReal();
                    double dy1 = opStack.pop().toReal();
                    double dx1 = opStack.pop().toReal();
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("vhcurveto")) {
                    double dy3 = 0;
                    double dx3 = opStack.pop().toReal();
                    double dy2 = opStack.pop().toReal();
                    double dx2 = opStack.pop().toReal();
                    double dy1 = opStack.pop().toReal();
                    double dx1 = 0;
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("hvcurveto")) {
                    double dy3 = opStack.pop().toReal();
                    double dx3 = 0;
                    double dy2 = opStack.pop().toReal();
                    double dx2 = opStack.pop().toReal();
                    double dy1 = 0;
                    double dx1 = opStack.pop().toReal();
                    gstate.current().rcurveto(dx1, dy1, (dx1 + dx2),
                            (dy1 + dy2), (dx1 + dx2 + dx3), (dy1 + dy2 + dy3));
                } else if (cmd.equals("hstem")) {
                    // Not much to for this command, except for popping two
                    // values from the stack.
                    opStack.pop().toReal();  // dy
                    opStack.pop().toReal();  // y
                } else if (cmd.equals("vstem")) {
                    // Not much to for this command, except for popping two
                    // values from the stack.
                    opStack.pop().toReal();  // dx
                    opStack.pop().toReal();  // x
                } else if (cmd.equals("hsbw")) {
                    double wx = opStack.pop().toReal();
                    double sbx = opStack.pop().toReal();
                    gstate.current().setPosition(sbx, 0);
                    paramSb[0] = sbx;
                    paramSb[1] = 0;
                    paramW[0] = wx;
                    paramW[1] = 0;
                } else if (cmd.equals("rmoveto")) {
                    double dy = opStack.pop().toReal();
                    double dx = opStack.pop().toReal();
                    gstate.current().rmoveto(dx, dy);
                } else if (cmd.equals("closepath")) {
                    gstate.current().getPath().closepath();
                } else if (cmd.equals("endchar")) {
                    break;
                } else if (cmd.equals("hmoveto")) {
                    double dx = opStack.pop().toReal();
                    gstate.current().rmoveto(dx, 0);
                } else if (cmd.equals("vmoveto")) {
                    double dy = opStack.pop().toReal();
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
                    execStack.addAll(0, fMetrics.getSubrs().get(subrnr));
                } else if (cmd.equals("return")) {
                    // There's nothing to do for this command. Since it will
                    // return automatically/
                } else if (cmd.equals("pop")) {
                    // Calls to OtherSubrs are ignored. That means that 'pop'
                    // can also be ignored.
                } else if (cmd.equals("callothersubr")) {
                    // Ignored, only required when rasterizing a font.
                } else if (cmd.equals("div")) {
                    double num1 = opStack.pop().toReal();
                    double num2 = opStack.pop().toReal();
                    double result = num1 / num2;
                    opStack.push(new PSObjectReal(result));
                } else {
                    System.out.println("-=-=- Unknown command: " + cmd);
                }
            }
        }
        
        gstate.current().moveto(paramW[0], paramW[1]);
        
        return gstate.current().getPath();
    }
    
}
