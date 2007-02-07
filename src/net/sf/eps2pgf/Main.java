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

import net.sf.eps2pgf.Converter;
import java.io.*;

/**
 *
 * @author Paul Wagenaars
 */
public class Main {
    static final String appName = "Eps2pgf";
    static final int versionMajor = 0;
    static final int versionMinor = 1;
    static final int versionRevision = 0;
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
//        if (args.length < 1) {
//            printUsage();
//            return;
//        }
        Converter cnv = new Converter("F:\\home\\devel\\eps2pgf\\testfigure.eps", 
                "F:\\home\\devel\\eps2pgf\\testfigure.pgf");
        cnv.convert();
    }
    
    static void printUsage() {
        System.out.println(appName + " v" + versionMajor + "." 
                + versionMinor + "." + versionRevision);
    }
    
    /**
     * Creates a string with the program name and version
     */
    public static String getNameVersion() {
        return appName + " v" + versionMajor + "." + versionMinor + "." + versionRevision;
    }
    
}
