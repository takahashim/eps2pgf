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

package eps2pgf.postscript;

import java.io.*;
import java.util.*;

/**
 * Reads PostScript code and converts it to a queue of PostScript objects.
 *
 * @author Paul Wagenaars
 */
public class Parser {
    
    /**
     * Convert a PostScript code to a queue of PostScript objects.
     * @param in PostScript code.
     * @return Queue with PostScript object.
     * @throws java.io.IOException Unable to read from in.
     */
    public static LinkedList<PSObject> convert(Reader in) throws IOException {
        LinkedList<PSObject> seq = new LinkedList<PSObject>();
        StringBuilder strSoFar = new StringBuilder();
        boolean inComment = false;
        boolean inString = false;
        boolean inProc = false;
        int procDepth = 0;
        int readChar;
        char chr;
        
        while ( (readChar = in.read()) != -1 ) {
            chr = (char)readChar;
            boolean saveStrSoFarBefore = false;
            boolean appendCurrentChar = false;
            boolean saveStrSoFarAfter = false;
            
            // If we are in a comment, we only need to stop for a new line
            // or form feed.
            if (inComment) {
                if ((chr == 10) || (chr == 12) || (chr == 13)) {
                    inComment = false;
                }
            }
            
            // If we encounter a % and are not in a string, we found a
            // comment.
            else if (!inString && (chr == '%')) {
                inComment = true;
            }
            
            // Objects are separated by whitespace
            else if ( Character.isWhitespace(chr) && !inProc && !inString ) {
                saveStrSoFarBefore = true;
            }
            
            // Or objects are separated by [ or ]
            else if ( ((chr == '[') || (chr == ']')) && !inProc && !inString ) {
                saveStrSoFarBefore = true;
                appendCurrentChar = true;
                saveStrSoFarAfter = true;
            }

            // Start of a new string
            else if ( (chr == '(') && !inProc && !inString ) {
                inString = true;
                saveStrSoFarBefore = true;
                appendCurrentChar = true;
            }
            
            // End of a string
            else if (inString && (chr == ')') && 
                    (strSoFar.charAt(strSoFar.length()-1) != '\\')) {
                inString = false;
                saveStrSoFarAfter = true;
                appendCurrentChar = true;
            }
            
            // Start of literal
            else if ( (chr == '/') && !inProc && !inString ) {
                saveStrSoFarBefore = true;
                appendCurrentChar = true;
            }
            
            // Start of procedure ..}
            else if ( (chr == '{') && !inString ) {
                if (procDepth == 0) {
                    saveStrSoFarBefore = true;
                    inProc = true;
                }
                procDepth++;
                appendCurrentChar = true;
            }
            
            // End of procedure ..}
            else if ( inProc && (chr == '}') ) {
                procDepth = procDepth - 1;
                appendCurrentChar = true;
                if (procDepth == 0) {
                    saveStrSoFarAfter = true;
                    inProc = false;
                }
            }
            
            // All other characters must be appended to strSofar
            else {
                appendCurrentChar = true;
            }
            
            if ( saveStrSoFarBefore && (strSoFar.length() > 0) ) {
                //System.out.println("Object: " + strSoFar.capacity() + " >" + strSoFar + "<");
                seq.add(convertToPSObject(strSoFar.toString()));
                strSoFar.delete(0, strSoFar.length());
            }
            if (appendCurrentChar) {
                strSoFar.append(chr);
            }
            if ( saveStrSoFarAfter && (strSoFar.length() > 0) ) {
                //System.out.println("Object: " + strSoFar.capacity() + " >" + strSoFar + "<");
                seq.add(convertToPSObject(strSoFar.toString()));
                strSoFar.delete(0, strSoFar.length());                
            }
            

        } // end of loop through all characters
        
        if (strSoFar.length() > 0) {
            //System.out.println("Object: " + strSoFar.capacity() + " >" + strSoFar + "<");
            seq.add(convertToPSObject(strSoFar.toString()));
        }
        
        return seq;
    }  // end of method convert
    
    /** Convert a string to a PostScript object.
     * @param str String to convert.
     */
    static PSObject convertToPSObject(String str) throws IOException {
        if (PSObjectReal.isType(str)) {
            return new PSObjectReal(str);
        } else if (PSObjectProc.isType(str)) {
            return new PSObjectProc(str);
        } else if (PSObjectString.isType(str)) {
            return new PSObjectString(str);
        } else {
            // At this we assume the object to be a name, either literal
            // or executable.
            return new PSObjectName(str);
        }
    }
    
}  // end of class Parser
