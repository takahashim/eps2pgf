/*
 * PSObjectFont.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.postscript.errors.PSError;
import net.sf.eps2pgf.postscript.errors.PSErrorInvalidFont;
import net.sf.eps2pgf.postscript.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorTypeCheck;
import net.sf.eps2pgf.postscript.errors.PSErrorUndefined;
import net.sf.eps2pgf.postscript.errors.PSErrorUnimplemented;

import org.fontbox.afm.AFMParser;
import org.fontbox.afm.CharMetric;
import org.fontbox.afm.FontMetric;
import org.fontbox.util.BoundingBox;

/**
 * Wrapper around a font dictionary. This class provides methods to handle the
 * font dictionary.
 * 
 * @author Paul Wagenaars
 */
public class PSObjectFont extends PSObject implements Cloneable {
    
    /** The next font id. */
    private static int nextFID = 0;
    
    /** The font dictionary. */
    PSObjectDict dict;
    
    /** The log. */
    Logger log = Logger.getLogger("net.sourceforge.eps2pgf");
    
    // Standard fields in font dictionary
    /** The fontinfo field name. */
    public static String KEY_FONTINFO = "FontInfo";
    
    /** The fontname field name. */
    public static String KEY_FONTNAME = "FontName";
    
    /** The encoding field name. */
    public static String KEY_ENCODING = "Encoding";
    
    /** The painttype field name. */
    public static String KEY_PAINTTYPE = "PaintType";
    
    /** The fonttype field name. */
    public static String KEY_FONTTYPE = "FontType";
    
    /** The fontmatrix field name. */
    public static String KEY_FONTMATRIX = "FontMatrix";
    
    /** The fontbbox field name. */
    public static String KEY_FONTBBOX = "FontBBox";
    
    /** The uniqueid field name. */
    public static String KEY_UNIQUEID = "UniqueID";
    
    /** The metrics field name. */
    public static String KEY_METRICS = "Metrics";
    
    /** The strokewidth field name. */
    public static String KEY_STROKEWIDTH = "StrokeWidth";
    
    /** The private field name. */
    public static String KEY_PRIVATE = "Private";
    
    /** The charstrings field name. */
    public static String KEY_CHARSTRINGS = "CharStrings";
    
    /** The fid field name. */
    public static String KEY_FID = "FID";
    
    // Standard fields in Private Dictionary
    /** The subrs field name. */
    public static String KEY_PRV_SUBRS = "Subrs";
    
    // Eps2pgf specific fields in font dictionary
    /** The latexprecode field name. */
    public static String KEY_LATEXPRECODE = "LatexPreCode";
    
    /** The latexpostcode field name. */
    public static String KEY_LATEXPOSTCODE = "LatexPostCode";
    
    /** The afm field name. */
    public static String KEY_AFM = "AFM";
    
    /** The texstrings field name. */
    public static String KEY_TEXSTRINGS = "TexStrings";
    
    // Field specific for Type 3 fonts
    /** The BuildGlyph field name */
    public static String KEY_BUILDGLYPH = "BuildGlyph";
    
    /** The BuildChar field name */
    public static String KEY_BUILDCHAR = "BuildChar";
    

    /**
     * Create a new empty instance. It sets the FID, font type and default matrix.
     */
    public PSObjectFont() {
        dict = new PSObjectDict();
        setFID();
        dict.setKey(KEY_FONTTYPE, new PSObjectInt(1));
        dict.setKey(KEY_FONTMATRIX, new PSObjectMatrix(0.001, 0, 0, 0.001, 0, 0));
    }
    
    /**
     * Creates a new instance of PSObjectFont by loading it from disk.
     * 
     * @param resourceDir Resource directory with font information
     * @param fontName Name of the font to load
     * 
     * @throws PSErrorInvalidFont the PS error invalid font
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
        dict.setKey(KEY_LATEXPOSTCODE, props.getProperty("latexpostcode", ""));
        FontMetric fontMetrics = loadAfm(resourceDir, fontName);
        dict.setKey(KEY_AFM, new PSObjectAfm(fontMetrics));
        String texStringName = props.getProperty("texstrings", "default");
        dict.setKey(KEY_TEXSTRINGS, Fonts.getTexStringDict(texStringName));
        
        // An AFM file foes not specify CharStrings. Instead, we make a fake entry
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
     * 
     * @return Returns <code>true</code> if this font was already valid. Returns
     * <code>false</code> when one or more fields were missing.
     * 
     * @throws PSError A PostScript error occurred.
     */
    boolean assertValidFont() throws PSError, ProgramError {
        boolean alreadyValid = true;
        
        String fontname = this.getFontName();
        
        // See if texstrings are defined for this font. If not, copy standard texstrings
        if (!dict.known(KEY_TEXSTRINGS)) {
            dict.setKey(KEY_TEXSTRINGS, Fonts.getTexStringDictByFontname(fontname));
            alreadyValid = false;
        }
        
        // If the LaTeX pre- and post codes are unknown, try to derive
        // them from the font name.
        if (!dict.known(KEY_LATEXPRECODE) || !dict.known(KEY_LATEXPOSTCODE)) {
            String[][] fontTypes = {
                    {"Serif",     "\\textrm{", "}"},
                    {"Roman",     "\\textrm{", "}"},
                    {"Sans",      "\\textsf{", "}"},
                    {"Mono",      "\\texttt{", "}"},
                    {"Monospace", "\\texttt{", "}"},
                    {"Bold",      "\\textbf{", "}"},
                    {"Oblique",   "\\textsl{", "}"},
                    {"Obli",      "\\textsl{", "}"},
                    {"Slanted",   "\\textsl{", "}"},
                    {"Italic",    "\\textit{", "}"},
                    {"Ital",      "\\textit{", "}"}};
            String pre = "";
            String post = "";
            // Append an "X" to the font name to make sure that
            // there is at least one [^a-z] character after the
            // font type string.
            String augmentedFontname = fontname + "X"; 
            for (int i = 0 ; i < fontTypes.length ; i++) {
                String regexp = ".*" + fontTypes[i][0] + "[^a-z].*";
                if (augmentedFontname.matches(regexp)) {
                    pre += fontTypes[i][1];
                    post = fontTypes[i][2] + post;
                }
            }
            dict.setKey(KEY_LATEXPRECODE, pre);
            dict.setKey(KEY_LATEXPOSTCODE, post);
            alreadyValid = false;
        }
        
        // Check for font metrics
        if (!dict.known(KEY_AFM)) {
            // Apparently there are no metrics. Try to extract it from the 
            // character descriptions.
            alreadyValid = false;
            dict.setKey(KEY_AFM, new PSObjectAfm(dict));
        }
        
        return alreadyValid;
    }
    
    /**
     * Convert an array with character names to a string to corresponding
     * LaTeX code.
     * 
     * @param charNames Character names to convert
     * 
     * @return LaTeX code corresponding to supplied character names
     * 
     * @throws PSError a PostScript error occurred
     */
    public String charNames2texStrings(PSObjectArray charNames) throws 
            PSError, ProgramError {
        assertValidFont();
        
        StringBuilder str = new StringBuilder();
        PSObjectDict texStrings = dict.lookup(KEY_TEXSTRINGS).toDict();
        PSObjectString preCode = dict.lookup(KEY_LATEXPRECODE).toPSString();
        PSObjectString postCode = dict.lookup(KEY_LATEXPOSTCODE).toPSString();
        
        str.append(preCode.toString());
        for (int i = 0 ; i < charNames.size() ; i++) {
            try {
                PSObjectName charName = charNames.get(i).toName();
                PSObject code = texStrings.lookup(charName);
                if (code == null) {
                    throw new PSErrorUnimplemented("TexString for "
                            + charNames.get(i).isis() + " is unknown.");
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
     * 
     * @return Duplicate of this object
     */
    public PSObjectFont dup() {
        return this;
    }
    
    /**
     * PostScript operator: get
     * Return value associated with key.
     * 
     * @param key Key for which the associated value will be returned
     * 
     * @return Value associated with key
     * 
     * @throws PSErrorUndefined the PostScript error undefined
     * @throws PSErrorTypeCheck the PostScript error type check
     */
    public PSObject get(PSObject key) throws PSErrorUndefined, PSErrorTypeCheck {
        return dict.get(key);
    }

    /**
     * Get the bounding box of a text (defined by a series of charStrings.
     * 
     * @param charNames Character names of the text for which the bounding box must be
     * determined.
     * 
     * @return Bounding box
     * 
     * @throws PSError a PostScript error occurred
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public BoundingBox getBBox(final PSObjectArray charNames)
            throws PSError, ProgramError {
        //charNames = replaceLigatures(charNames);
        
        BoundingBox bbox = new BoundingBox();

        // Determine upper and lower boundary of bounding box
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        CharMetric cmFirstChar = new CharMetric();
        CharMetric cmLastChar = new CharMetric();
        for (int i = 0; i < charNames.size(); i++) {
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
                if (i == (charNames.size() - 1)) {
                    cmLastChar = cm;
                }
            } catch (PSErrorRangeCheck e) {
                    // This can never happen inside the for loop
            }
        }
        
        // Determine left and right boundary of bounding box
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
     * Retrieves the metrics for a single character.
     * 
     * @param charName the char name
     * 
     * @return the char metric
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    CharMetric getCharMetric(final String charName)
            throws PSError, ProgramError {
        FontMetric fm = getFontMetric();
        List charMetrics = fm.getCharMetrics();
        for (Object obj : charMetrics) {
            if (!(obj instanceof CharMetric)) {
                throw new PSErrorTypeCheck();
            }
            CharMetric cm = (CharMetric) obj;
            if (charName.equals(cm.getName())) {
                return cm;
            }
        }
        
        // CharMetric was not found, let's return an empty one
        CharMetric empty = new CharMetric();
        BoundingBox bbox = new BoundingBox();
        bbox.setLowerLeftX(0);
        bbox.setLowerLeftY(0);
        bbox.setUpperRightX(0);
        bbox.setUpperRightY(0);
        empty.setBoundingBox(bbox);
        empty.setWx(0);
        return empty;
    }
    
    /**
     * Return the encoding in this font.
     * 
     * @return Encoding defined by this font
     * 
     * @throws PSErrorTypeCheck the PS error type check
     */
    public PSObjectArray getEncoding() throws PSErrorTypeCheck {
        return dict.lookup(KEY_ENCODING).toArray();
    }
    
    /**
     * Returns the font ID, if defined. Returns -1 when no FID is defined.
     * 
     * @return the FID
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
     * Return the FontMatrix.
     * 
     * @return FontMatrix defined by this font
     * 
     * @throws PSErrorTypeCheck the PS error type check
     * @throws PSErrorRangeCheck the PS error range check
     */
    public PSObjectMatrix getFontMatrix() throws PSErrorTypeCheck, PSErrorRangeCheck {
        return dict.lookup(KEY_FONTMATRIX).toMatrix();
    }
    
    /**
     * Returns the font metrics of this font.
     * 
     * @return Font metrics
     * 
     * @throws PSErrorTypeCheck the PS error type check
     */
    public FontMetric getFontMetric() throws PSError, ProgramError {
        assertValidFont();
        return dict.lookup(KEY_AFM).toFontMetric();
    }
    
    /**
     * Returns the fontname of this font.
     * 
     * @return Fontname
     */
    String getFontName() {
        if (dict.known(KEY_FONTNAME)) {
            PSObject fontNameObj = dict.lookup(KEY_FONTNAME);
            if (fontNameObj instanceof PSObjectName) {
                return ((PSObjectName) fontNameObj).toName().name;
            }
        }
        // No font name is defined in this dictionary. Instead we use the key
        // in the FontDirectory that is associated with this font.
        List<PSObject> items = Fonts.FontDirectory.getItemList();
        int thisFID = this.getFID(); 
        try {
            for (int i = 1; i < items.size(); i += 2) {
                PSObjectFont itemFont = items.get(i + 1).toFont();
                if (itemFont.getFID() == thisFID) {
                    return items.get(i).toString();
                }
            }
        } catch (PSErrorTypeCheck e) {
            return "";
        }
        return "";
    }
    
    /**
     * Returns the font size in pt (= 1/72 inch).
     * 
     * @return Font size of this font in pt (= 1/72 inch)
     * 
     * @throws PSErrorTypeCheck the PS error type check
     * @throws PSErrorRangeCheck the PS error range check
     */
    public double getFontSize() throws PSErrorTypeCheck, PSErrorRangeCheck {
        return dict.lookup(KEY_FONTMATRIX).toMatrix().getMeanScaling() * 1000;
    }
    
    /**
     * Returns a list with all items in object.
     * 
     * @return List with all items in this object. The first object (with
     * index 0) is always a PSObjectInt with the number of object
     * in a single item. For most object types this is 1, but for
     * dictionaries this is 2. All consecutive items (index 1 and
     * up) are the object's items.
     */
    public List<PSObject> getItemList() {
        return dict.getItemList();
    }

    /**
     * Determines the total width of a string.
     * 
     * @param charNames Array with character names describing the string
     * 
     * @return Width of the string in pt (= 1/72 inch)
     * 
     * @throws PSError the PS error
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public double getWidth(final PSObjectArray charNames)
            throws PSError, ProgramError {
        //charNames = replaceLigatures(charNames);
        double width = 0;
        for (int i = 0; i < charNames.size(); i++) {
            try {
                String charName = charNames.get(i).toName().name;
                CharMetric cm = getCharMetric(charName);
                double scaling = getFontMatrix().getMeanScaling();
                width += cm.getWx() * scaling;
            } catch (PSErrorRangeCheck e) {
                    // This can never happen inside the for loop
            }
        }
        return width;
    }
    
    /**
     * Creates a human-readable string representation of this font.
     * 
     * @return Human-readable string representation of this font. See the
     * PostScript specification on the == operator for more info.
     */
    public String isis() {
        return "-font " + getFontName() + " (FID " + getFID() + ")-";
    }

    /**
     * Get the number of elements.
     * 
     * @return The number of dictionary entries in this font.
     */
    public int length() {
        return dict.length();
    }
    
    /**
     * Load font metrics (*.afm) from the resource directory
     * 
     * @param resourceDir Resource directory with font information
     * @param fontName Name of the font to load
     * 
     * @return Font metrics of requested font
     * 
     * @throws PSErrorInvalidFont the PS error invalid font
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
     * 
     * @param index Index or key for new value
     * @param value New value
     * 
     * @throws PSErrorTypeCheck the PS error type check
     */
    public void put(PSObject index, PSObject value) throws PSErrorTypeCheck {
        dict.put(index, value);
    }
    
    /**
     * Set the font ID for this font to the next available ID.
     * 
     * @return New font ID
     */
    int setFID() {
        int FID = nextFID++;
        dict.setKey(KEY_FID, new PSObjectInt(FID));
        return FID;
    }
    
    /**
     * Return the dictionary used by this font.
     * 
     * @return Dictionary object of this font
     */
    public PSObjectDict toDict() {
        return dict;
    }
    
    /**
     * Return this object.
     * 
     * @return This object
     */
    public PSObjectFont toFont() {
        return this;
    }

    /**
     * Returns the type of this object.
     * 
     * @return Type of this object (see PostScript manual for possible values)
     */
    public String type() {
        return "dicttype";
    }
}
