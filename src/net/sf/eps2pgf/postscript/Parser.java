/*
 * Parser.java
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

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Reads PostScript code and converts it to a queue of PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class Parser {
    /**
     * Numbers of characters consumed in the last convertSingle call
     */
    public static int charsLastConvert = -1;
    
    /**
     * Convert PostScript code to list of objects.
     * @param in Reader with PostScript code
     * @return List with PostScript objects
     * @throws java.io.IOException Unable to read from in
     */
    public static List<PSObject> convertAll(Reader in) throws IOException, PSErrorIOError {
        List<PSObject> seq = new ArrayList<PSObject>();
        PSObject obj;
        while ( (obj = convertSingle(in)) != null ) {
            seq.add(obj);
        }
        return seq;
    }
    
    /**
     * Read PostScript code until a single object is encountered
     * @param in Read characters (PostScript code) from this reader
     * @throws java.io.IOException Unable to read from input
     * @return Object read from in reader or 'null' if there were no more objects
     */
    public static PSObject convertSingle(Reader in) throws IOException, PSErrorIOError {
        StringBuilder strSoFar = new StringBuilder();
        boolean inComment = false;
        boolean inString = false;
        boolean inProc = false;
        boolean inHexString = false;
        boolean inB85String = false;
        int procDepth = 0;
        int stringDepth = 0;
        int readChar;
        char chr = '\000';
        char prevChr;
        String lastTwo;
        
        int charsThisConvert = 0;
        while ( (readChar = in.read()) != -1 ) {
            charsThisConvert++;
            
            prevChr = chr;
            chr = (char)readChar;
            if (charsThisConvert > 1) {
                lastTwo = Character.toString(prevChr) + Character.toString(chr);
            } else {
                lastTwo = Character.toString(chr);
            }
            boolean tokenBefore = false;
            boolean appendCurrentChar = true;
            boolean tokenAfter = false;
            
            // If we are in a comment, we only need to stop for a new line
            // or form feed.
            if (inComment) {
                appendCurrentChar = false;
                if ((chr == 10) || (chr == 12) || (chr == 13)) {
                    inComment = false;
                }
            }
            
            // In a string
            else if (inString) {
                if ( (chr == ')') && !lastTwo.equals("\\)")) {
                    stringDepth--;
                    if (stringDepth == 0) {
                        inString = false;
                        if (!inProc) {
                            tokenAfter = true;
                        }
                    }
                } else if ( (chr == '(') && !lastTwo.equals("\\(")) {
                    stringDepth++;
                }
            }
            
            // In a procedure
            else if (inProc) {
                // End of the procedure
                if (chr == '}') {
                    procDepth--;
                    if (procDepth == 0) {
                        tokenAfter = true;
                        inProc = false;
                    }
                }
                // A string in a procedure
                else if (chr == '(') {
                    stringDepth++;
                    inString = true;
                }
                // a procedure in a procedure
                else if (chr == '{') {
                    procDepth++;
                }
            }

            else if (inHexString) {
                // End of the hex string
                if (chr == '>') {
                    inHexString = false;
                    tokenAfter = true;
                }
            }
            
            else if (inB85String) {
                // End of base-85 string
                if (lastTwo.equals("~>")) {
                    inB85String = false;
                    tokenAfter = true;
                }
            }
            
            // start of comment
            else if (chr == '%') {
                appendCurrentChar = false;
                inComment = true;
            }
            
            // start of string
            else if (chr == '(') {
                stringDepth++;
                inString = true;
                tokenBefore = true;
            }
            
            // start or end of array
            else if ((chr == '[') || (chr == ']')) {
                tokenBefore = true;
                tokenAfter = true;
            }
            
            // Start of literal
            else if (chr == '/') {
                tokenBefore = true;
            }
            
            // Start of procedure .. {
            else if ( (chr == '{') && !inString ) {
                tokenBefore = true;
                inProc = true;
                procDepth++;
            }
            
            // start of hex string, base-85 string or dictionary
            else if (chr == '<') {
                if (prevChr == '<') {
                    tokenAfter = true;
                } else {
                    tokenBefore = true;
                }
            }
            
            // second char of base-85 string
            else if ( (prevChr == '<') && (chr == '~') ) {
                inB85String = true;
            }
            
            // second char of hex string
            else if (prevChr == '<') {
                inHexString = true;
            }
            
            // end of dictionary >>
            else if (chr == '>') {
                if (prevChr == '>') {
                    tokenAfter = true;
                } else {
                    tokenBefore = true;
                }
            }
            
            // object ended by whitespace
            else if ( Character.isWhitespace(chr) ) {
                appendCurrentChar = false;
                tokenBefore = true;
            }
            
            //
            // Create token, append current character
            //
            if ( tokenBefore && (strSoFar.length() > 0) ) {
                if (!Character.isWhitespace(chr)) {
                    in.reset();
                    charsThisConvert--;
                }
                PSObject newObj = convertToPSObject(strSoFar.toString());
                charsLastConvert = charsThisConvert;
                return newObj;
            }
            if (appendCurrentChar) {
                strSoFar.append(chr);
            }
            if ( tokenAfter && (strSoFar.length() > 0) ) {
                PSObject newObj = convertToPSObject(strSoFar.toString());
                charsLastConvert = charsThisConvert;
                return newObj;
            }
            
            in.mark(1);
        } // end of loop through all characters
        
        if (strSoFar.length() > 0) {
                PSObject newObj = convertToPSObject(strSoFar.toString());
                charsLastConvert = charsThisConvert;
                return newObj;
        }
        
        charsLastConvert = charsThisConvert;
        return null;
    }
    
    /** Convert a string to a PostScript object.
     * @param str String to convert.
     */
    static PSObject convertToPSObject(String str) throws IOException, PSErrorIOError {
        if (PSObjectInt.isType(str)) {
            return new PSObjectInt(str);
        } else if (PSObjectReal.isType(str)) {
            return new PSObjectReal(str);
        } else if (PSObjectArray.isType(str)) {
            return new PSObjectArray(str);
        } else if (PSObjectString.isType(str)) {
            return new PSObjectString(str, true);
        } else {
            // At this we assume the object to be a name, either literal
            // or executable.
            return new PSObjectName(str);
        }
    }
    
}  // end of class Parser
