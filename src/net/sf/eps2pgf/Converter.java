/*
 * Converter.java
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

package net.sf.eps2pgf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

import net.sf.eps2pgf.io.LimitedSectionInputStream;
import net.sf.eps2pgf.io.TextReplacements;
import net.sf.eps2pgf.postscript.DSCHeader;
import net.sf.eps2pgf.postscript.Header;
import net.sf.eps2pgf.postscript.Interpreter;
import net.sf.eps2pgf.postscript.PSObjectFile;

/**
 * Object that converts Encapsulated PostScript (EPS) to Portable Graphics
 * Format (PGF).
 *
 * @author Paul Wagenaars
 */
public class Converter {
    
    /** Input file. */
    private File inFile;
    
    /** Output file. */
    private File outFile;
    
    /** Options describing behavior of program. */
    private Options opts;
    
    /**
     * Creates a new instance of Converter.
     * 
     * @param pOpts command line options passed to the program
     */
    public Converter(final Options pOpts) {
        this.inFile = new File(pOpts.getInput());
        this.outFile = new File(pOpts.getOutput());
        this.opts = pOpts;
    }
    
    /**
     * Starts the actual conversion.
     * @throws java.lang.Exception Something went wrong
     */
    public final void convert() throws Exception {
        // Check for a binary header
        int[] dim = Header.getPostScriptSection(inFile);
        
        // Open the file for reading the postscript code
        InputStream in = new BufferedInputStream(new FileInputStream(inFile));
        
        // If it has a binary header, read only the postscript code and skip
        // binary data.
        if (dim != null) {
            in = new LimitedSectionInputStream(in, dim[0], dim[1]);
        }
        
        // Read info from the DSC header
        DSCHeader header = new DSCHeader(in);
        
        // Read text replacements file
        TextReplacements textReplace = null;
        if (opts.getTextreplacefile().length() > 0) {
            Reader inTextReplace = new BufferedReader(
                    new FileReader(opts.getTextreplacefile()));
            textReplace = new TextReplacements(inTextReplace);
            inTextReplace.close();
        }
        
        // Open output file
        Writer out = new BufferedWriter(new FileWriter(outFile));
        
        // Create PostScript interpreter and add file to execution stack
        Interpreter interp = new Interpreter(out, header, opts.getOutputtype(),
                textReplace);
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
