/*
 * Main.java
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
import java.util.logging.*;

/**
 * Main class of Eps2pgf program
 * @author Paul Wagenaars
 */
public class Main {
    static final String appName = "Eps2pgf";
    static final int versionMajor = 0;
    static final int versionMinor = 3;
    static final int versionRevision = 0;
    static Options opts = new Options();;
    
    static Logger log = Logger.getLogger("global");
    
    /**
     * Main program method
     * @param args the command line arguments
     * @throws java.lang.Exception Something went wrong
     */
    public static void main(String[] args) throws Exception {
        opts.parse(args);
        
        if (opts.args.getBoolean("version") || opts.args.getBoolean("help")) {
            printVersionCopyright();
            if (opts.args.getBoolean("help")) {
                printHelp();
            }
            
            System.exit(0);
        }
        
        if (!opts.args.success()) {
            
            System.err.println();

            for (java.util.Iterator errs = opts.args.getErrorMessageIterator();
                    errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            
            printHelp();
            
            System.exit(1);
        }
        
        if (opts.args.getBoolean("verbose")) {
            log.setLevel(Level.INFO);
        } else {
            log.setLevel(Level.WARNING);
        }
        
        Converter cnv = new Converter(opts.input, opts.output);
        cnv.convert();
    }
    
    /**
     * Creates a string with the program name and version
     * @return Application name followed by the version number
     */
    public static String getNameVersion() {
        return appName + " " + versionMajor + "." + versionMinor + "." + versionRevision;
    }
    
    /**
     * Print version and copyright information
     */
    public static void printVersionCopyright() {
        System.out.println(getNameVersion());
        System.out.println("Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>");
        System.out.println(appName + " comes with ABSOLUTELY NO WARRANTY. This is free software, and you");
        System.out.println("are welcome to redistribute it under certain conditions. See version 2 of");
        System.out.println("the GNU General Public License for details.");
    }

    /**
     * Prints information on program usage
     */
    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: " + appName.toLowerCase() + " "
                           + opts.getUsage());
        System.out.println();
        System.out.println(opts.getHelp());        
    }
}
