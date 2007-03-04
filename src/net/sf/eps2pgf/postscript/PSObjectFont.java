/*
 * PSObjectFont.java
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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.fontbox.afm.*;
import org.fontbox.util.BoundingBox;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Wrapper around a font dictionary. This class provides methods to handle the
 * font dictionry.
 * @author Paul Wagenaars
 */
public class PSObjectFont extends PSObject implements Cloneable {
    private static int nextFID = 0;
    PSObjectDict dict;
    Logger log = Logger.getLogger("global");
    
    /**
     * Create a new empty instance. It only sets the FID.
     */
    public PSObjectFont() {
        dict = new PSObjectDict();
        setFID();
    }
    
    /**
     * Creates a new instance of PSObjectFont by loading it from disk.
     * @param resourceDir Resource directory with font information
     * @param fontName Name of the font to load
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont Font file not found or font file is invalid
     */
    public PSObjectFont(File resourceDir, String fontName) throws PSErrorInvalidFont {
        File fontFile = new File(resourceDir, "fontdescriptions" + 
                File.separator + fontName + ".font");

        Properties props;
        try {
            FileInputStream in = new FileInputStream(fontFile);
            props = new Properties();
            props.loadFromXML(in);
            in.close();
        } catch (FileNotFoundException e) {
            throw new PSErrorInvalidFont();
        } catch (IOException e) {
            throw new PSErrorInvalidFont();
        }
        
        // Setting the dictionary keys with font info
        dict = new PSObjectDict();
        dict.setKey("FontType", new PSObjectInt(1));
        dict.setKey("FontMatrix", new PSObjectMatrix(1,0,0,1,0,0));
        dict.setKey("FontName", new PSObjectName(fontName, true));
        setFID();
        String encoding = props.getProperty("encoding", "Standard");
        if (encoding.equals("Standard")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getStandardVector()));
        } else if (encoding.equals("ISOLatin1")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getISOLatin1Vector()));
        } else if (encoding.equals("Symbol")) {
            dict.setKey("Encoding", new PSObjectArray(Encoding.getSymbolVector()));
        } else {
            log.severe("Unknown encoding: " + encoding);
            throw new PSErrorInvalidFont();
        }
        dict.setKey("PaintType", new PSObjectInt(2));
        dict.setKey("LatexPreCode", props.getProperty("latexprecode", ""));
        dict.setKey("LatexPostCode", props.getProperty("latexpostcode", ""));
        String charStrings = props.getProperty("charstrings", "StandardLatin");
        dict.setKey("CharStrings", loadCharStrings(resourceDir, charStrings));
        FontMetric fontMetrics = loadAfm(resourceDir, fontName);
        dict.setKey("AFM", new PSObjectAfm(fontMetrics));
        
//      dict.setKey("FontBBox", new PSObjectArray(fontBbox));
    }
    
    /**
     * Creates a new font dictionary with aDict as dictionary.
     * @param aDict Dictionary to use as font dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Dictionary is not a valid font
     */
    public PSObjectFont(PSObjectDict aDict) throws PSErrorTypeCheck {
        // Check some (not all) required font dictionary fields
        if ((!aDict.containsKey("FontType")) || (!aDict.containsKey("FontMatrix"))) {
            throw new PSErrorTypeCheck();
        }
        
        dict = aDict;
    }
    
    /**
     * Load font metrics (*.afm) from the resource directory
     * @param resourceDir Resource directory with font information
     * @param fontName Name of the font to load
     * @return Font metrics of requested font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont Font file not found
     */
    public FontMetric loadAfm(File resourceDir, String fontName) throws PSErrorInvalidFont {
        File afmFile = new File(resourceDir, "afm" + 
                File.separator + fontName + ".afm");
        
        FontMetric fontMetric;
        try {
            FileInputStream in = new FileInputStream(afmFile);
            AFMParser afm = new AFMParser(in);
            afm.parse();
            fontMetric = afm.getResult();
            in.close();
        } catch (FileNotFoundException e) {
            throw new PSErrorInvalidFont();
        } catch (IOException e) {
            throw new PSErrorInvalidFont();
        }
        
        return fontMetric;
    }
    
    /**
     * Load a file with charString (= character names -> latex code)
     * @param resourceDir Resource directory
     * @param charStringsName Name of the charStrings list to load
     * @return Dictionary with character names and associated latex code
     */
    PSObjectDict loadCharStrings(File resourceDir, String charStringsName) throws PSErrorInvalidFont {
        File charStrFile = new File(resourceDir, "charstrings" + 
                File.separator + charStringsName + ".charstrings");
        
        Properties props;
        try {
            FileInputStream in = new FileInputStream(charStrFile);
            props = new Properties();
            props.loadFromXML(in);
            in.close();
        } catch (FileNotFoundException e) {
            log.severe("Unable to load charStrings " + charStringsName + ".\n");
            throw new PSErrorInvalidFont();
        } catch (IOException e) {
            throw new PSErrorInvalidFont();
        }
        
        // Now load charStrings from the props
        PSObjectDict dict = new PSObjectDict();
        Set<String> allCharNames = Encoding.getCharNames().keySet();
        for (String charName : allCharNames) {
            // Check whether this character name in defined in this
            // charStrings list.
            String propValue = props.getProperty(charName);
            if (propValue != null) {
                dict.setKey(charName, propValue);
            }
        }

        return dict;
    }
    
    /**
     * Convert an array with character names to a string to corresponding
     * LaTeX code
     * @param charNames Character names to convert
     * @return LaTeX code corresponding to supplied character names
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font is not a valid font dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented "String" contains an unknown charString
     */
    public String charNames2charStrings(PSObjectArray charNames) throws 
            PSErrorTypeCheck, PSErrorUnimplemented {
        StringBuilder str = new StringBuilder();
        PSObjectDict charStrings = dict.lookup("CharStrings").toDict();
        PSObjectString preCode = dict.lookup("LatexPreCode").toPSString();
        PSObjectString postCode = dict.lookup("LatexPostCode").toPSString();

        str.append(preCode.value);
        for (int i = 0 ; i < charNames.size() ; i++) {
            PSObjectName charName;
            try {
                charName = charNames.get(i).toName();
                PSObject code = charStrings.lookup(charName);
                if (code == null) {
                    throw new PSErrorUnimplemented("CharString for "
                            + charNames.get(i).isis() + " is unknown.");
                }
                str.append(code.toPSString().value);
            } catch (PSErrorRangeCheck e) {
                // This can never happen inside this for loop
            }
        }
        str.append(postCode.value);

        return str.toString();
    }
    
    /**
     * Get the bounding box of a text (defined by a series of charStrings
     * @param charNames Character names of the text for which the bounding box must be
     * determined.
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font is not a invalid/incomplete
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined One or more characters are undefined in the font
     * @return Bounding box
     */
    public BoundingBox getBBox(PSObjectArray charNames)
            throws PSErrorTypeCheck, PSErrorUndefined {
        charNames = replaceLigatures(charNames);
        
        BoundingBox bbox = new BoundingBox();

        // Determine upper and lower boundary of bounding box
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        CharMetric cmFirstChar = new CharMetric();
        CharMetric cmLastChar = new CharMetric();
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                String charName = charNames.get(i).toName().name;
                CharMetric cm = getCharMetric(charName);
                BoundingBox charBbox = cm.getBoundingBox();
                minY = Math.min(minY, charBbox.getLowerLeftY());
                maxY = Math.max(maxY, charBbox.getUpperRightY());
                
                // Save 1st and last character bbox. These are used below
                // the determine left and right boundary.
                if (i == 0) {
                    cmFirstChar = cm;
                }
                if (i == (charNames.size()-1)) {
                    cmLastChar = cm;
                }
            } catch (PSErrorRangeCheck e) {
                    // This can never happen inside the for loop
            }
        }
        
        // Determin left and right boundary of bounding box
        double leftX = cmFirstChar.getBoundingBox().getLowerLeftX()/1000;
        double rightX = getWidth(charNames) - cmLastChar.getWx()/1000
                + cmLastChar.getBoundingBox().getUpperRightX()/1000;

        bbox.setLowerLeftX(new Float(leftX));
        bbox.setLowerLeftY(new Float(minY/1000));
        bbox.setUpperRightX(new Float(rightX));
        bbox.setUpperRightY(new Float(maxY/1000));
        return  bbox;
    }
    
    /**
     * Retrieves the metrics for a single character
     */
    CharMetric getCharMetric(String charName) throws PSErrorTypeCheck, PSErrorUndefined {
        FontMetric fm = getFontMetric();
        List charMetrics = fm.getCharMetrics();
        for (Object obj : charMetrics) {
            if (!(obj instanceof CharMetric)) {
                throw new PSErrorTypeCheck();
            }
            CharMetric cm = (CharMetric)obj;
            if (charName.equals(cm.getName())) {
                return cm;
            }
        }
        throw new PSErrorUndefined();
    }
    
    /**
     * Returns the fontname of this font
     * @return Fontname
     */
    String getFontName() {
        try {
            return dict.lookup("FontName").toName().name;
        } catch (PSErrorTypeCheck e) {
            // No font name is defined
            return "";
        }
    }
    
    /**
     * Returns the font ID, if defined. Returns -1 when no FID is defined.
     */
    int getFID() {
        try {
            return dict.lookup("FID").toInt();
        } catch (PSErrorTypeCheck e) {
            // FID is not an integer
            return -1;
        } catch (NullPointerException e) {
            // No FID is defined
            return -1;
        }
    }
    
    /**
     * Set the font ID for this font to the next available ID
     * @return New font ID
     */
    int setFID() {
        int FID = nextFID++;
        dict.setKey("FID", new PSObjectInt(FID));
        return FID;
    }
    
    /**
     * Return the encoding in this font
     * @return Encoding defined by this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Encoding vector is not defined by this font
     */
    public PSObjectArray getEncoding() throws PSErrorTypeCheck {
        return dict.lookup("Encoding").toArray();
    }
    
    /**
     * Return the FontMatrix
     * @return FontMatrix defined by this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font does not contain a valid font matrix
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid number of elements in FontMatrix array
     */
    public PSObjectMatrix getFontMatrix() throws PSErrorTypeCheck, PSErrorRangeCheck {
        return dict.lookup("FontMatrix").toMatrix();
    }
    
    /**
     * Returns the font metrics of this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font is invalid. The AFM field is not set properly.
     * @return Font metrics
     */
    public FontMetric getFontMetric() throws PSErrorTypeCheck {
        return dict.lookup("AFM").toFontMetric();
    }
    
    /**
     * Gets the horizontal kerning distance between two characters
     * @return Kerning distance between characters in pt (1/72 inch)
     * @param firstChar First kerning character
     * @param secondChar Second kerning character
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font does not contain font metric data
     */
    public double getKernX(String firstChar, String secondChar) throws PSErrorTypeCheck {
        FontMetric fm = getFontMetric();
        List kernPairs = fm.getKernPairs();
        for (Object obj : kernPairs) {
            KernPair kp = (KernPair)obj;
            if (firstChar.equals(kp.getFirstKernCharacter()) 
                    && secondChar.equals(kp.getSecondKernCharacter())) {
                return kp.getX()/1000;
            }
        }
        return 0;
    }
    
    /**
     * Determines the total width of a string
     * @param charNames Array with character names describing the string
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Charnames array contains other types than PSObjectName objects
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Font metric field in this font is invalid of undefined
     * @return Width of the string in pt (= 1/72 inch)
     */
    public double getWidth(PSObjectArray charNames) throws PSErrorTypeCheck, PSErrorUndefined {
        charNames = replaceLigatures(charNames);
        double width = 0;
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                String charName = charNames.get(i).toName().name;
                CharMetric cm = getCharMetric(charName);
                width += cm.getWx()/1000;
                
                // Kerning with previous character???
                if (i > 0) {
                    String prevCharName = charNames.get(i-1).toName().name;
                    width += getKernX(prevCharName, charName);
                }
            } catch (PSErrorRangeCheck e) {
                    // This can never happen inside the for loop
            }
        }
        return width;
    }
    
    /**
     * Create a copy of the array with character names with all pairs with
     * ligatures replaced by their ligatures.
     * @param charNames Character names of the text to search for ligature replacements
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Font metric information is not defined in this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined One or more character names are not defined in the font
     * @return New array with character names
     */
    public PSObjectArray replaceLigatures(PSObjectArray charNames) 
            throws PSErrorTypeCheck, PSErrorUndefined {
        PSObjectArray arr = new PSObjectArray();
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                PSObjectName charName = charNames.get(i).toName();
                
                // Check for ligatures
                Boolean ligFound = false;
                if (i < (charNames.size()-1)) {
                    String nextChar = charNames.get(i+1).toName().name;
                    CharMetric cm = getCharMetric(charName.name);
                    List ligatures = cm.getLigatures();
                    for (Object obj : ligatures) {
                        Ligature lig = (Ligature)obj;
                        if (nextChar.equals(lig.getSuccessor())) {
                            PSObjectName name = new PSObjectName(lig.getLigature(), true);
                            arr.add(name);
                            i++;
                            ligFound = true;
                            break;
                        }
                    }
                }
                
                if (!ligFound) {
                    arr.add(charName);
                }
            } catch (PSErrorRangeCheck e) {
                // This can never happen inside the for loop
            }
        }
        
        // It is possible the ligature on their turn also have ligatures.
        // Therefore we repeat this process until everything is replaced.
        if (arr.size() < charNames.size()) {
            arr = replaceLigatures(arr);
        }
        
        return arr;
    }
    
    
    /**
     * Return the dictionary used by this font
     * @return Dictionary object of this font
     */
    public PSObjectDict toDict() {
        return dict;
    }
    
    /**
     * Return this object.
     * @return This object
     */
    public PSObjectFont toFont() {
        return this;
    }

    /**
     * Creates an exact deep copy of this font.
     * @return Created copy
     * @throws java.lang.CloneNotSupportedException Unable to clone this font
     */
    public PSObjectFont clone() throws CloneNotSupportedException {
        PSObjectDict newDict = dict.clone();
        PSObjectFont newFont = null;
        try {
            newFont = new PSObjectFont(newDict);
        } catch (PSErrorTypeCheck e) {
            // Assuming that "this object" is a valid font this should never happen.
        }
        return newFont;
    }
    
    /**
     * Creates a human-readable string representation of this font.
     * @return Human-readable string representation of this font. See the
     * PostScript specification on the == operator for more info.
     */
    public String isis() {
        return "-font " + getFontName() + " (FID " + getFID() + ")-";
    }
}
