/*
 * Main.java
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

import java.util.logging.*;

/**
 * Main class of Eps2pgf program.
 * 
 * @author Paul Wagenaars
 */
public class Main {
    
    /** Application name */
    static final String appName = "Eps2pgf";
    
    /** Application version */
    static final String appVersion = "@VERSION@";
    
    /** Date application was build */
    static final String appBuildDate = "@BUILDDATE@";
    
    /** Handles program options */
    static Options opts = new Options();;
    
    /** The log. */
    static Logger log = Logger.getLogger("global");
    
    /**
     * Main program method.
     * 
     * @param args the command line arguments
     * 
     * @throws Exception the exception
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
     * Creates a string with the program name and version.
     * 
     * @return Application name followed by the version number
     */
    public static String getNameVersion() {
        return appName + " " + appVersion + " (build on " + appBuildDate + ")";
    }
    
    /**
     * Print version and copyright information.
     */
    public static void printVersionCopyright() {
        System.out.println(getNameVersion());
        System.out.println("");
        System.out.println("Copyright 2007 Paul Wagenaars <paul@wagenaars.org>");
        System.out.println("");
        System.out.println("Licensed under the Apache License, Version 2.0 (the \"License\");");
        System.out.println("you may not use this file except in compliance with the License.");
        System.out.println("You may obtain a copy of the License at");
        System.out.println("");
        System.out.println("    http://www.apache.org/licenses/LICENSE-2.0");
        System.out.println("");
        System.out.println("Unless required by applicable law or agreed to in writing, software");
        System.out.println("distributed under the License is distributed on an \"AS IS\" BASIS,");
        System.out.println("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        System.out.println("See the License for the specific language governing permissions and");
        System.out.println("limitations under the License.");
    }

    /**
     * Prints information on program usage.
     */
    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: " + appName.toLowerCase() + " "
                           + opts.getUsage());
        System.out.println();
        System.out.println(opts.getHelp());        
    }
}
