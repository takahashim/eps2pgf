/*
 * Converter.java
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

package net.sf.eps2pgf;

import java.io.*;
import java.util.*;
import net.sf.eps2pgf.postscript.Interpreter;
import net.sf.eps2pgf.postscript.PSObject;
import net.sf.eps2pgf.postscript.Parser;

/**
 * Object that converts Encapsulated PostScript (EPS) to Portable Graphics
 * Format (PGF).
 *
 * @author Paul Wagenaars
 */
public class Converter {
    File inFile;
    File outFile;
    
    /** Creates a new instance of Converter
     * @param sourceFilename path to target EPS file
     * @param targetFilename path to target PGF file
     */
    public Converter(String sourceFilename, String targetFilename) {
        inFile = new File(sourceFilename);
        outFile = new File(targetFilename);
    }
    
    /** Starts the actual conversion.
     */
    public void convert() throws Exception {
        // Parse input file
        LinkedList<PSObject> inputObjects;
        Reader in = new BufferedReader(new FileReader(inFile));
        inputObjects = Parser.convert(in);
        in.close();
        
        // Execute parsed file
        Writer out = new BufferedWriter(new FileWriter(outFile));
        Interpreter interp = new Interpreter(inputObjects, out);
        try {
            interp.start();
        } catch (Exception e) {
            out.close();
            throw e;
        }
        out.close();
    }
    
}
