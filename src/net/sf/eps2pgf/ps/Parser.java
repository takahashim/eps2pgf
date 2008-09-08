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

package net.sf.eps2pgf.ps;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectInt;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectReal;
import net.sf.eps2pgf.ps.objects.PSObjectString;

/**
 * Reads PostScript code and converts it to a queue of PostScript objects.
 *
 * @author Paul Wagenaars
 */
public final class Parser {
    /** Numbers of characters consumed in the last convertSingle call. */
    private static int charsLastConvert = -1;
    
    /**
     * "Hidden" constructor.
     */
    private Parser() {
        /* empty block */
    }
    
    /**
     * Convert PostScript code to list of objects.
     * 
     * @param in Reader with PostScript code
     * 
     * @return List with PostScript objects
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static List<PSObject> convertAll(final InputStream in)
            throws IOException, PSError, ProgramError {
        
        List<PSObject> seq = new ArrayList<PSObject>();
        PSObject obj;
        while ((obj = convertSingle(in)) != null) {
            seq.add(obj);
        }
        return seq;
    }
    
    /**
     * Read PostScript code until a single object is encountered.
     * 
     * @param in Read characters (PostScript code) from this reader.
     * 
     * @return Object read from in reader or 'null' if there were no more
     * objects.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static PSObject convertSingle(final InputStream in)
            throws IOException, PSError, ProgramError {
        
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
        char prevChr = '\000';
        String lastTwo;
        
        int charsThisConvert = 0;
        while ((readChar = in.read()) != -1) {
            charsThisConvert++;
            
            // Escaped backslashes should not be treated as special characters.
            // Therefore, we replace escaped backslashes in 'prevChr' with \000
            // Note: \134 (octal) is the code of the backslash in ASCII.
            if ((chr == '\134') && (prevChr == '\134')) {
                prevChr = '\000';
            } else {
                prevChr = chr;
            }
            chr = (char) readChar;
            
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
            } else if (inString) {
                //
                // In a string
                //
                if ((chr == ')') && !lastTwo.equals("\\)")) {
                    stringDepth--;
                    if (stringDepth == 0) {
                        inString = false;
                        if (!inProc) {
                            tokenAfter = true;
                        }
                    }
                } else if ((chr == '(') && !lastTwo.equals("\\(")) {
                    stringDepth++;
                }
            } else if (inHexString) {
                //
                // End of the hex string
                //
                if (chr == '>') {
                    inHexString = false;
                    tokenAfter = true;
                }
            } else if (inB85String) {
                //
                // End of base-85 string
                //
                if (lastTwo.equals("~>")) {
                    inB85String = false;
                    tokenAfter = true;
                }
            } else if (chr == '%') {
                //
                // start of comment
                //
                appendCurrentChar = false;
                inComment = true;
            } else if (inProc) {
                //
                // In a procedure
                //
                
                // End of the procedure
                if (chr == '}') {
                    procDepth--;
                    if (procDepth == 0) {
                        tokenAfter = true;
                        inProc = false;
                    }
                } else if (chr == '(') {
                    //
                    // A string in a procedure
                    //
                    stringDepth++;
                    inString = true;
                } else if (chr == '{') {
                    //
                    // a procedure in a procedure
                    //
                    procDepth++;
                }
            } else if (chr == '(') {
                //
                // start of string
                //
                stringDepth++;
                inString = true;
                tokenBefore = true;
            } else if ((chr == '[') || (chr == ']')) {
                //
                // start or end of array
                //
                tokenBefore = true;
                tokenAfter = true;
            } else if (chr == '/') {
                //
                // Start of literal
                //
                tokenBefore = true;
            } else if ((chr == '{') && !inString) {
                //
                // Start of procedure .. {
                //
                tokenBefore = true;
                inProc = true;
                procDepth++;
            } else if (chr == '<') {
                //
                // start of hex string, base-85 string or dictionary
                //
                if (prevChr == '<') {
                    tokenAfter = true;
                } else {
                    tokenBefore = true;
                }
            } else if ((prevChr == '<') && (chr == '~')) {
                //
                // second char of base-85 string
                //
                inB85String = true;
            } else if (prevChr == '<') {
                //
                // second char of hex string
                //
                inHexString = true;
            } else if (chr == '>') {
                //
                // end of dictionary >>
                //
                if (prevChr == '>') {
                    tokenAfter = true;
                } else {
                    tokenBefore = true;
                }
            } else if (Character.isWhitespace(chr)) {
                //
                // object ended by whitespace
                //
                appendCurrentChar = false;
                tokenBefore = true;
            }
            
            //
            // Create token, append current character
            //
            if (tokenBefore && (strSoFar.length() > 0)) {
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
            if (tokenAfter && (strSoFar.length() > 0)) {
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
    
    /**
     * Convert a string to a PostScript object.
     * 
     * @param str String to convert.
     * 
     * @return the PS object
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    static PSObject convertToPSObject(final String str)
            throws IOException, PSError, ProgramError {
        
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

    /**
     * @return the charsLastConvert
     */
    public static int getCharsLastConvert() {
        return charsLastConvert;
    }
    
}
