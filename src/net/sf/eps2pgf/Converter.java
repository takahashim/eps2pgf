/*
 * Converter.java
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

package net.sf.eps2pgf;

import java.io.*;
import java.util.*;

import net.sf.eps2pgf.postscript.*;

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
    
    /**
     * Starts the actual conversion.
     * @throws java.lang.Exception Something went wrong
     */
    public void convert() throws Exception {
        // Parse input file
        List<PSObject> inputObjects;
        Reader in = new BufferedReader(new FileReader(inFile));
        DSCHeader header = new DSCHeader(in);
        
        
        // Open output file
        Writer out = new BufferedWriter(new FileWriter(outFile));
        
        // Create PostScript interpreter and add file to execution stack
        Interpreter interp = new Interpreter(out, header);
        interp.execStack.push(new PSObjectFile(in));
        
        // Run the interpreter
        try {
            interp.start();
        } catch (Exception e) {
            in.close();
            out.close();
            throw e;
        }
        in.close();
        out.close();
    }
    
}
