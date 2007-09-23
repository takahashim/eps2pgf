/*
 * PSObjectFont.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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
    
    // Standard fields in font dictionary
    public static String KEY_FONTINFO = "FontInfo";
    public static String KEY_FONTNAME = "FontName";
    public static String KEY_ENCODING = "Encoding";
    public static String KEY_PAINTTYPE = "PaintType";
    public static String KEY_FONTTYPE = "FontType";
    public static String KEY_FONTMATRIX = "FontMatrix";
    public static String KEY_FONTBBOX = "FontBBox";
    public static String KEY_UNIQUEID = "UniqueID";
    public static String KEY_METRICS = "Metrics";
    public static String KEY_STROKEWIDTH = "StrokeWidth";
    public static String KEY_PRIVATE = "Private";
    public static String KEY_CHARSTRINGS = "CharStrings";
    public static String KEY_FID = "FID";    
    
    // Eps2pgf specific fields in font dictionary
    public static String KEY_LATEXPRECODE = "LatexPreCode";
    public static String KEY_LATXEPOSTCODE = "LatexPostCode";
    public static String KEY_AFM = "AFM";
    public static String KEY_TEXSTRINGS = "TexStrings";
    
    /**
     * Create a new empty instance. It only sets the FID.
     */
    public PSObjectFont() {
        dict = new PSObjectDict();
        setFID();
        dict.setKey(KEY_FONTTYPE, new PSObjectInt(1));
        dict.setKey(KEY_FONTMATRIX, new PSObjectMatrix(0.001, 0, 0, 0.001, 0, 0));
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
        dict.setKey(KEY_FONTTYPE, new PSObjectInt(1));
        dict.setKey(KEY_FONTMATRIX, new PSObjectMatrix(0.001, 0, 0, 0.001, 0, 0));
        dict.setKey(KEY_FONTNAME, new PSObjectName(fontName, true));
        setFID();
        String encoding = props.getProperty("encoding", "Standard");
        if (encoding.equals("Standard")) {
            dict.setKey(KEY_ENCODING, new PSObjectArray(Encoding.getStandardVector()));
        } else if (encoding.equals("ISOLatin1")) {
            dict.setKey(KEY_ENCODING, new PSObjectArray(Encoding.getISOLatin1Vector()));
        } else if (encoding.equals("Symbol")) {
            dict.setKey(KEY_ENCODING, new PSObjectArray(Encoding.getSymbolVector()));
        } else {
            log.severe("Unknown encoding: " + encoding);
            throw new PSErrorInvalidFont();
        }
        dict.setKey(KEY_PAINTTYPE, new PSObjectInt(2));
        dict.setKey(KEY_LATEXPRECODE, props.getProperty("latexprecode", ""));
        dict.setKey(KEY_LATXEPOSTCODE, props.getProperty("latexpostcode", ""));
        FontMetric fontMetrics = loadAfm(resourceDir, fontName);
        dict.setKey(KEY_AFM, new PSObjectAfm(fontMetrics));
        
        // An AFM file foes not specify CharStrings. Instead, we make a fake answer
        List charMetrics = fontMetrics.getCharMetrics();
        PSObjectDict charStrings = new PSObjectDict();
        for (Object obj : charMetrics) {
            if (obj instanceof CharMetric) {
                CharMetric cm = (CharMetric)obj;
                charStrings.setKey(cm.getName(), "");
            }
        }
        dict.setKey(KEY_CHARSTRINGS, charStrings);
    }
    
    /**
     * Creates a new font dictionary with aDict as dictionary.
     * 
     * @param aDict Dictionary to use as font dictionary
     */
    public PSObjectFont(PSObjectDict aDict) {
        dict = aDict;
        access = aDict.access;
        isLiteral = aDict.isLiteral;
    }
    
    /**
     * Assert that this font contains all fields that are expected from a font.
     * If they don't exist they will be created.
     * @return Returns <code>true</code> if this font was already valid. Returns
     *         <code>false</code> when one or more fields were missing.
     */
    boolean assertValidFont() {
        boolean alreadyValid = true;
        
        // See if texstrings are defined for this font. If not, copy standard texstrings
        if (!dict.known(KEY_TEXSTRINGS)) {
            PSObjectDict texStrings = Fonts.texstrings.clone();
            dict.setKey(KEY_TEXSTRINGS, texStrings);
            alreadyValid = false;
        }
        
        // Check latex pre- and post code
        if (!dict.known(KEY_LATEXPRECODE)) {
            dict.setKey(KEY_LATEXPRECODE, "");
            alreadyValid = false;
        }
        if (!dict.known(KEY_LATXEPOSTCODE)) {
            dict.setKey(KEY_LATXEPOSTCODE, "");
            alreadyValid = false;
        }
        
        // Check for font metrics
        if (!dict.known(KEY_AFM)) {
            if (!dict.known(KEY_CHARSTRINGS)) {
                
            }
            
        }
        
        return alreadyValid;
    }
    
    /**
     * Convert an array with character names to a string to corresponding
     * LaTeX code
     * @param charNames Character names to convert
     * @return LaTeX code corresponding to supplied character names
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font is not a valid font dictionary
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented "String" contains an unknown charString
     */
    public String charNames2texStrings(PSObjectArray charNames) throws 
            PSErrorTypeCheck, PSErrorUnimplemented {
        assertValidFont();
        
        StringBuilder str = new StringBuilder();
        PSObjectDict texStrings = dict.lookup(KEY_TEXSTRINGS).toDict();
        PSObjectString preCode = dict.lookup(KEY_LATEXPRECODE).toPSString();
        PSObjectString postCode = dict.lookup(KEY_LATXEPOSTCODE).toPSString();
        
        str.append(preCode.toString());
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                String charName = charNames.get(i).toName().toDictKey();
                PSObject code = texStrings.lookup(charName);
                if (code == null) {
                    // Maybe there is an all-lower-case version of this character name defined
                    code = texStrings.lookup(charName.toLowerCase());
                    if (code == null) {
                        throw new PSErrorUnimplemented("TexString for "
                                + charNames.get(i).isis() + " is unknown.");
                    }
                }
                str.append(code.toPSString().toString());
            } catch (PSErrorRangeCheck e) {
                // This can never happen inside this for loop
            }
        }
        str.append(postCode.toString());

        return str.toString();
    }
    
    /**
     * Creates an exact deep copy of this font.
     * 
     * @return Created copy
     */
    public PSObjectFont clone() {
        PSObjectDict newDict = dict.clone();
        PSObjectFont newFont = null;
        newFont = new PSObjectFont(newDict);
        return newFont;
    }
    
    /**
     * PostScript operator 'dup'. Create a (shallow) copy of this object. The values
     * of composite object is not copied, but shared.
     * @return Duplicate of this object
     */
    public PSObjectFont dup() {
        return this;
    }
    
    /**
     * PostScript operator: get
     * Return value associated with key.
     * @param key Key for which the associated value will be returned
     * @return Value associated with key
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key is not a valid dictionary key
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorUndefined Requested key is not defined in this dictionary
     */
    public PSObject get(PSObject key) throws PSErrorUndefined, PSErrorTypeCheck {
        return dict.get(key);
    }

    /**
     * Get the bounding box of a text (defined by a series of charStrings
     * @param charNames Character names of the text for which the bounding box must be
     * determined.
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred.
     * @return Bounding box
     */
    public BoundingBox getBBox(PSObjectArray charNames) throws PSError {
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
        double scaling = getFontMatrix().getMeanScaling();
        double leftX = cmFirstChar.getBoundingBox().getLowerLeftX()*scaling;
        double rightX = getWidth(charNames) - cmLastChar.getWx()*scaling
                + cmLastChar.getBoundingBox().getUpperRightX()*scaling;

        bbox.setLowerLeftX(new Float(leftX));
        bbox.setLowerLeftY(new Float(minY*scaling));
        bbox.setUpperRightX(new Float(rightX));
        bbox.setUpperRightY(new Float(maxY*scaling));
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
     * Return the encoding in this font
     * @return Encoding defined by this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Encoding vector is not defined by this font
     */
    public PSObjectArray getEncoding() throws PSErrorTypeCheck {
        return dict.lookup(KEY_ENCODING).toArray();
    }
    
    /**
     * Returns the font ID, if defined. Returns -1 when no FID is defined.
     */
    int getFID() {
        try {
            return dict.lookup(KEY_FID).toInt();
        } catch (PSErrorTypeCheck e) {
            // FID is not an integer
            return -1;
        } catch (NullPointerException e) {
            // No FID is defined
            return -1;
        }
    }
    
    /**
     * Return the FontMatrix
     * @return FontMatrix defined by this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font does not contain a valid font matrix
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid number of elements in FontMatrix array
     */
    public PSObjectMatrix getFontMatrix() throws PSErrorTypeCheck, PSErrorRangeCheck {
        return dict.lookup(KEY_FONTMATRIX).toMatrix();
    }
    
    /**
     * Returns the font metrics of this font
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font is invalid. The AFM field is not set properly.
     * @return Font metrics
     */
    public FontMetric getFontMetric() throws PSErrorTypeCheck {
        return dict.lookup(KEY_AFM).toFontMetric();
    }
    
    /**
     * Returns the fontname of this font
     * @return Fontname
     */
    String getFontName() {
        try {
            return dict.lookup(KEY_FONTNAME).toName().name;
        } catch (PSErrorTypeCheck e) {
            // No font name is defined
            return "";
        }
    }
    
    /**
     * Returns the font size in pt (= 1/72 inch)
     * @return Font size of this font in pt (= 1/72 inch)
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck This font does not contain a valid font matrix
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck Invalid number of elements in FontMatrix array
     */
    public double getFontSize() throws PSErrorTypeCheck, PSErrorRangeCheck {
        return dict.lookup(KEY_FONTMATRIX).toMatrix().getMeanScaling() * 1000;
    }
    
    /**
     * Returns a list with all items in object.
     * 
     * @return List with all items in this object. The first object (with
     *         index 0) is always a PSObjectInt with the number of object
     *         in a single item. For most object types this is 1, but for
     *         dictionaries this is 2. All consecutive items (index 1 and
     *         up) are the object's items.
     */
    public List<PSObject> getItemList() {
        return dict.getItemList();
    }

    /**
     * Gets the horizontal kerning distance between two characters
     * @return Kerning distance between characters in pt (1/72 inch)
     * @param firstChar First kerning character
     * @param secondChar Second kerning character
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred.
     */
    public double getKernX(String firstChar, String secondChar) throws PSError {
        FontMetric fm = getFontMetric();
        List kernPairs = fm.getKernPairs();
        for (Object obj : kernPairs) {
            KernPair kp = (KernPair)obj;
            if (firstChar.equals(kp.getFirstKernCharacter()) 
                    && secondChar.equals(kp.getSecondKernCharacter())) {
                double scaling = getFontMatrix().getMeanScaling();
                return kp.getX()*scaling;
            }
        }
        return 0;
    }
    
    /**
     * Determines the total width of a string
     * @param charNames Array with character names describing the string
     * @throws net.sf.eps2pgf.postscript.errors.PSError A PostScript error occurred.
     * @return Width of the string in pt (= 1/72 inch)
     */
    public double getWidth(PSObjectArray charNames) throws PSError {
        charNames = replaceLigatures(charNames);
        double width = 0;
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                String charName = charNames.get(i).toName().name;
                CharMetric cm = getCharMetric(charName);
                double scaling = getFontMatrix().getMeanScaling();
                width += cm.getWx()*scaling;
                
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
     * Creates a human-readable string representation of this font.
     * @return Human-readable string representation of this font. See the
     * PostScript specification on the == operator for more info.
     */
    public String isis() {
        return "-font " + getFontName() + " (FID " + getFID() + ")-";
    }

    /**
     * Get the number of elements
     * @return The number of dictionary entries in this font.
     */
    public int length() {
        return dict.length();
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
     * PostScript operator put. Replace a single value in this object.
     * @param index Index or key for new value
     * @param value New value
     * @throws net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck Supplied key is not a valid dictionary key.
     */
    public void put(PSObject index, PSObject value) throws PSErrorTypeCheck {
        dict.put(index, value);
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
                            arr.addToEnd(name);
                            i++;
                            ligFound = true;
                            break;
                        }
                    }
                }
                
                if (!ligFound) {
                    arr.addToEnd(charName);
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
     * Set the font ID for this font to the next available ID
     * @return New font ID
     */
    int setFID() {
        int FID = nextFID++;
        dict.setKey(KEY_FID, new PSObjectInt(FID));
        return FID;
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
     * Returns the type of this object
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "dicttype";
    }
}
