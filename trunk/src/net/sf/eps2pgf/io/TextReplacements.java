/*
 * TextReplacements.java
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

package net.sf.eps2pgf.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;

/**
 * This class describes PSfrag style rules to replace texts.
 */
public class TextReplacements {
    /** Allowed characters for vertical alignment. */
    static final String VERT_ALLOWED = "tcBb";
    
    /** Allowed characters for horizontal alignment. */
    static final String HOR_ALLOWED = "lcr";
    
    /** Allowed characters for horizontal and vertical alignment alignment. */
    static final String ALL_ALLOWED = VERT_ALLOWED + HOR_ALLOWED;
    
    /** List with all text replacement rules. */
    private HashMap<String, Rule> rules = new HashMap<String, Rule>();
    
    
    /**
     * Instantiates a new text replacements instance.
     * 
     * @param in Read PSfrag replacement rules from this input stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParseException Invalid replacement rule
     */
    public TextReplacements(final Reader in)
            throws IOException, ParseException {
        Reader texReader = new SkipTexCommentsReader(in);
        readRules(texReader);
    }
    
    /**
     * Instantiates a new text replacements instance.
     * 
     * @param in Read PSfrag replacement rules from this input stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParseException Invalid replacement rule
     */
    public TextReplacements(final File in)
            throws IOException, ParseException {
        this(new BufferedReader(new FileReader(in)));
    }
    
    /**
     * Instantiates a new empty list with text replacements.
     */
    public TextReplacements() {
        
    }
    
    /**
     * Find the replacement rule for a text.
     * 
     * @param text The text for which the replacement rule is requested.
     * 
     * @return The replacement rule, or <code>null</code> if text was not found.
     */
    public final Rule findReplacement(final String text) {
        return this.rules.get(text);
    }
    
    /**
     * Read PSfrag rules and add them to the list.
     * 
     * @param in Reader rules from this reader.
     * 
     * @return Number of rules read from the input stream.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParseException Invalid replacement rule
     */
    private int readRules(final Reader in) throws IOException, ParseException {
        while (readToString(in, "\\psfrag")) {
            // Check for starred version of \psfrag
            in.mark(1);
            if (in.read() != '*') {
                in.reset();
            }
            
            String tag = readCurlyArgument(in);
            if (tag == null) {
                continue;
            }
            String texRefPoint = readSquareArgument(in, "Bl");
            String psRefPoint = readSquareArgument(in, "Bl");
            double scale = Double.parseDouble(readSquareArgument(in, "1.0"));
            double rotation = Double.parseDouble(readSquareArgument(in, "0.0"));
            String texText = readCurlyArgument(in);
            if (texText == null) {
                texText = "";
            }
            
            Rule newRule = new Rule(tag, texRefPoint, psRefPoint, scale,
                    rotation, texText);
            this.rules.put(tag, newRule);
        }
        
        return this.rules.size();
    }
    
    /**
     * Read embedded rule.
     * 
     * @param text The text.
     * 
     * @return Rule found in the text, null if no rule was found.
     */
    public static Rule readEmbeddedRule(final String text) {
        if (!text.matches("^\\s*\\\\tex.*")) {
            return null;
        }
        
        Reader reader = new StringReader(text);
        try {
            // First, check if string starts with \tex
            consumeWhitespace(reader);
            if (!readToString(reader, "\\tex")) {
                return null;
            }
            String texRefPoint = readSquareArgument(reader, "Bl");
            String psRefPoint = readSquareArgument(reader, "Bl");
            String texText = readCurlyArgument(reader);
            if (texText == null) {
                texText = "";
            }
            
            Rule rule = new Rule(text, texRefPoint, psRefPoint, 1.0, 0.0,
                    texText);
            
            return rule;
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }
    
    /**
     * Read characters from the input stream until specified string is
     * encountered. The string itself is consumed too.
     * 
     * @param in Input stream from which characters are read.
     * @param string Search for this string.
     * 
     * @return True, if string was encountered. False, if end-of-file was
     * reached.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static boolean readToString(final Reader in, final String string)
            throws IOException {
        StringBuilder lastChars = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            lastChars.append((char) c);
            if (lastChars.length() > string.length()) {
                lastChars.deleteCharAt(0);
            }
            if (lastChars.indexOf(string) == 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Read the next argument wrapped in curly braces.
     * 
     * @param in Reader from which characters are read.
     * 
     * @return Next argument in curly braces. Or, null if the next
     *         non-whitespace character is not '{'.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String readCurlyArgument(final Reader in)
            throws IOException {
        // Skip whitespace
        consumeWhitespace(in);
        
        // Check whether there are curly braces at all
        in.mark(1);
        if (in.read() != '{') {
            in.reset();
            return null;
        }
        
        int c;
        StringBuilder str = new StringBuilder();
        int depth = 1;
        while ((c = in.read()) != -1) {
            if ((c == '{') && (str.charAt(str.length() - 1) != '\\')) {
                depth++;
            } else if ((c == '}') && (str.charAt(str.length() - 1) != '\\')) {
                depth--;
            }
            if (depth == 0) {
                break;
            }
            str.append((char) c);
        }
        return str.toString();
    }
    
    /**
     * Read the next argument wrapped in square brackets.
     * 
     * @param in Reader from which characters are read.
     * @param defaultArg Default argument if no square brackets are encountered.
     * 
     * @return Next argument in curly braces. Or, the default if the next
     *         non-whitespace character is not '['.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static String readSquareArgument(final Reader in,
            final String defaultArg) throws IOException {
        // Skip whitespace
        consumeWhitespace(in);
        
        // Check whether there are some square brackets at all
        in.mark(1);
        if (in.read() != '[') {
            in.reset();
            return defaultArg;
        }
        
        int c;
        StringBuilder str = new StringBuilder();
        while ((c = in.read()) != -1) {
            if (c == ']') {
                break;
            }
            str.append((char) c);
        }
        return str.toString();
    }
    
    /**
     * Consume all whitespace characters at the beginning of the input.
     * 
     * @param in Input stream
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void consumeWhitespace(final Reader in) throws IOException {
        int c;
        do {
            in.mark(1);
            c = in.read();
        } while (Character.isWhitespace(c));
        if (c != -1) {
            in.reset();
        }
        return;
    }
    
    
    /**
     * Assert that a reference point (as passed in a text replacement rule) is
     * valid.
     * 
     * @param pRefPoint The reference point as passed.
     * 
     * @return Valid reference point where the first character described the
     * vertical alignment and the second the horizontal.
     * 
     * @throws ParseException The reference point is invalid.
     */
    static String assertValidRefPoint(final String pRefPoint)
            throws ParseException {
        String refPoint = pRefPoint;
        
        // First, check for invalid characters
        for (int i = 0; i < refPoint.length(); i++) {
            if (TextReplacements.ALL_ALLOWED.indexOf(refPoint.charAt(i))
                    == -1) {
                throw new ParseException("Invalid character(s) in reference "
                        + "point (" + refPoint + ") in text replacement rule."
                        , -1);
            }
        }
    
        // Check the length
        if (refPoint.length() == 0) {
            refPoint = "cc";
        } else if (refPoint.length() == 1) {
            refPoint += "c";
        } else if (refPoint.length() > 2) {
            throw new ParseException("Reference point (" + refPoint + ") in the"
                    + " text replacement rules has more than two characters."
                    , -1);
        }
        
        // Check which one is horizontal and which is vertical
        char firstChar = refPoint.charAt(0);
        char secondChar = refPoint.charAt(1);
        if ((VERT_ALLOWED.indexOf(firstChar) != -1)
                && (HOR_ALLOWED.indexOf(secondChar) != -1)) {
            return Character.toString(firstChar) + secondChar;
        } else if ((VERT_ALLOWED.indexOf(secondChar) != -1)
                && (HOR_ALLOWED.indexOf(firstChar) != -1)) {
            return Character.toString(secondChar) + firstChar;
        } else {
            throw new ParseException("Invalid reference point (" + refPoint
                    + ") in text replacement rules.", -1);
        }
    }
    
    /** 
     * Returns a string representation of the object.
     * 
     * @return String representation of this rule.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        for (Rule rule : this.rules.values()) {
            builder.append(rule.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Single text replacement rule.
     */
    static final class Rule {
        
        /** Text as in eps file. */
        private String tag;
        
        /** Position of TeX text relative to the reference point.  */
        private String texRefPoint;
        
        /** Position of reference point relative to eps text. */
        private String psRefPoint;
        
        /** Scaling factor for TeX text. */
        private double scale;
        
        /** Addition rotation (in degrees) for TeX text. */
        private double rotation;
        
        /** TeX text to use in output. */
        private String texText;
        
        /**
         * Create a new text replacement rule.
         * 
         * @param pTag Text as is occurs in the figure
         * @param pTexRefPoint Position of TeX text relative to the reference
         *        point. 
         * @param pPSRefPoint Position of reference point relative to eps text.
         * @param pScale Scaling factor for TeX text.
         * @param pRotation Addition rotation (in degrees) for TeX text.
         * @param pTexText TeX text to use in output.
         * 
         * @throws ParseException Invalid replacement rule
         */
        private Rule(final String pTag, final String pTexRefPoint,
                final String pPSRefPoint, final double pScale,
                final double pRotation, final String pTexText)
                throws ParseException {
            this.tag = pTag;
            this.texRefPoint = assertValidRefPoint(pTexRefPoint);
            this.psRefPoint = assertValidRefPoint(pPSRefPoint);
            this.scale = pScale;
            this.rotation = pRotation;
            this.texText = pTexText;
        }
        
        /**
         * Gets the reference point of the (La)TeX text.
         * 
         * @return TeX reference point.
         */
        public String getTexRefPoint() {
            return this.texRefPoint;
        }
        
        /**
         * Gets the reference point of the PostScript text.
         * 
         * @return PostScript text reference point.
         */
        public String getPsRefPoint() {
            return this.psRefPoint;
        }
        
        /**
         * Gets the (La)TeX text.
         * 
         * @return (La)TeX text.
         */
        public String getTexText() {
            return this.texText;
        }
        
        /**
         * Gets the extra rotation.
         * 
         * @return Extra rotation.
         */
        public double getRotation() {
            return this.rotation;
        }
        
        /**
         * Gets the scaling.
         * 
         * @return Scaling.
         */
        public double getScaling() {
            return this.scale;
        }
        
        /** 
         * Returns a string representation of the object.
         * 
         * @return String representation of this rule.
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "\\psfrag{" + this.tag + "}[" + this.texRefPoint + "]["
                    + this.psRefPoint + "][" + this.scale + "][" + this.rotation
                    + "]{" + this.texText + "}";
        }
        
    }

}
