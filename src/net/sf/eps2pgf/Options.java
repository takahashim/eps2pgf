/*
 * Options.java
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

import java.io.File;

import com.martiansoftware.jsap.*;

/**
 * Parses command line arguments and manages settings and options by the user
 *
 * @author Paul Wagenaars
 */
public class Options extends JSAP {
    JSAPResult args;
    
    String input;
    String output;
    
    /**
     * Creates a new instance of Options
     */
    public Options() {
        super();
        
        try {
            UnflaggedOption opt2 = new UnflaggedOption("inputfilename")
                                          .setStringParser(JSAP.STRING_PARSER)
                                          .setRequired(true);
            opt2.setHelp("(Encapsulated) PostScript (EPS or PS) input file");
            registerParameter(opt2);
            
            Switch sw = new Switch("verbose")
                        .setLongFlag("verbose");
            sw.setHelp("Display more information during the conversion process");
            registerParameter(sw);
            
            FlaggedOption opt1 = new FlaggedOption("outputfilename")
                                       .setShortFlag('o')
                                       .setLongFlag("output")
                                       .setStringParser(JSAP.STRING_PARSER)
                                       .setDefault("<input file with .pgf extension>")
                                       .setRequired(true);
            opt1.setHelp("Write output to this file");
            registerParameter(opt1);
            
            sw = new Switch("version")
                                .setLongFlag("version");
            sw.setHelp("Display version information");
            registerParameter(sw);
            
            sw = new Switch("help")
                        .setLongFlag("help")
                        .setShortFlag('h');
            sw.setHelp("Display program usage");
            registerParameter(sw);
            
        } catch (JSAPException e) {
            // Since all above code is fixed, without variables, this
            // should never happen.
        }
    }
    
    /**
     * Parse a set of command-line arguments
     */
    public JSAPResult parse(String[] arguments) {
        args = super.parse(arguments);
        
        postParse();
        
        return args;
    }
    
    /**
     * Post-process parsing results. Copy settings to this object, check
     * validity, etc...
     */
    private void postParse() {
        input = args.getString("inputfilename", "");
        
        if (args.getString("outputfilename").startsWith("<")) {
            String lowerInput = input.toLowerCase();
            if (lowerInput.endsWith(".eps")) {
                output = input.substring(0, input.length()-4) + ".pgf";
            } else if (lowerInput.endsWith(".ps")) {
                output = input.substring(0, input.length()-3) + ".pgf";
            } else {
                output = input + ".pgf";
            }
        } else {
            // An output filename was specified on the command-line
            output = args.getString("outputfilename");
        }
    }
    
}
