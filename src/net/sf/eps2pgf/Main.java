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

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.martiansoftware.jsap.JSAPResult;

/**
 * Main class of Eps2pgf program.
 * 
 * @author Paul Wagenaars
 */
public final class Main {
    
    /** Application name. */
    private static final String APP_NAME = "Eps2pgf";
    
    /** Application version. */
    private static final String APP_VERSION = "@VERSION@";
    
    /** Date application was build. */
    private static final String APP_BUILD_DATE = "@BUILDDATE@";
    
    /** Handles program options. */
    private static Options opts = new Options();
    
    /** The log. */
    private static final Logger LOGGER = Logger.getLogger("global");
    
    
    /**
     * "Hidden" constructor.
     */
    private Main() {
    	
    }
    
    /**
     * Main program method.
     * 
     * @param args the command line arguments
     * 
     * @throws Exception the exception
     */
    public static void main(final String[] args) throws Exception {
        JSAPResult parseResult = opts.parse(args);
        
        if (opts.isHelpFlagSet()) {
            printVersionCopyright();
            printHelp();
            System.exit(0);
        }
        
        if (opts.isVersionFlagSet()) {
            printVersionCopyright();
            System.exit(0);
        }
        
        if (!parseResult.success()) {
            System.err.println();

            for (Iterator errs = parseResult.getErrorMessageIterator();
                    errs.hasNext();) {
                System.err.println("Error: " + errs.next());
            }
            
            System.exit(1);
        }
        
        if (opts.isVerboseFlagSet()) {
            LOGGER.setLevel(Level.INFO);
        } else {
            LOGGER.setLevel(Level.WARNING);
        }
        
        Converter cnv = new Converter(opts);
        cnv.convert();
    }
    
    /**
     * Creates a string with the program name and version.
     * 
     * @return Application name followed by the version number
     */
    public static String getNameVersion() {
        return APP_NAME + " " + APP_VERSION + " (build on " + APP_BUILD_DATE
        		+ ")";
    }
    
    /**
     * Print version and copyright information.
     */
    public static void printVersionCopyright() {
        System.out.println(getNameVersion());
        System.out.println("");
        System.out.println("Copyright 2007 Paul Wagenaars"
        	+ " <paul@wagenaars.org>");
        System.out.println("");
        System.out.println("Licensed under the Apache License, Version 2.0 (the"
        	+ " \"License\");");
        System.out.println("you may not use this file except in compliance with"
        	+ " the License.");
        System.out.println("You may obtain a copy of the License at");
        System.out.println("");
        System.out.println("    http://www.apache.org/licenses/LICENSE-2.0");
        System.out.println("");
        System.out.println("Unless required by applicable law or agreed to in "
        	+ "writing, software");
        System.out.println("distributed under the License is distributed on an "
        	+ "\"AS IS\" BASIS,");
        System.out.println("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,"
        	+ " either express or implied.");
        System.out.println("See the License for the specific language governing"
        	+ " permissions and");
        System.out.println("limitations under the License.");
    }

    /**
     * Prints information on program usage.
     */
    public static void printHelp() {
        System.out.println();
        System.out.println("Usage: " + APP_NAME.toLowerCase() + " "
                           + opts.getUsage());
        System.out.println();
        System.out.println(opts.getHelp());        
    }
}
