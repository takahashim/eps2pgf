/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars
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

package net.sf.eps2pgf.ps.resources;

import java.io.File;
import java.util.logging.Logger;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.resources.fonts.FontManager;

/**
 * Class with some utilities related to resource handling.
 * 
 * @author Paul Wagenaars
 *
 */
public final class Utils {

    /** Name of directory containing font resources. */
    private static final String RESOURCE_DIR_NAME = "resources";
    
    /**
     * Resource directory.
     */
    private static File resourceDir = null;
    
    /** The log. */
    private static final Logger LOG
                                  = Logger.getLogger("net.sourceforge.eps2pgf");
    
    /**
     * "Hidden" constructor.
     */
    private Utils() {
        /* empty block */
    }
    
    /**
     * Try to find the resource in the current directory or its parent
     * directories.
     * 
     * @param pDir The directory path to search.
     * 
     * @return the file
     */
    private static File findResourceDirInPath(final File pDir) {
        File dir = pDir;
        File returnDir = null;
        
        while (dir != null) {
            // Check whether this dir has a valid resource subdir
            // It just checks for a "resources" directory with two known
            // subdirectories.
            returnDir = new File(dir, RESOURCE_DIR_NAME);
            if (returnDir.exists()) {
                File afmDir = new File(returnDir, FontManager.AFM_DIR_NAME);
                if (afmDir.exists()) {
                    File fontDescDir =
                        new File(returnDir, FontManager.FONTDESC_DIR_NAME);
                    if (fontDescDir.exists()) {
                        break;
                    }
                }
            }
            returnDir = null;
            dir = dir.getParentFile();
        }
        
        return returnDir;
    }
    
    /**
     * Finds and returns the resource directory.
     * 
     * @return Resource directory.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static synchronized File getResourceDir() throws ProgramError {
        if (resourceDir != null) {
            return resourceDir;
        }
        
        // Try to find the resource dir in the current directory, ...
        File userDir = new File(System.getProperty("user.dir"));
        LOG.info("Current directory = " + userDir.toString());

        // ..., or relative to the class path
        String fullClassPath = System.getProperty("java.class.path");
        LOG.info("Java class path = " + fullClassPath);
        String pathSeparator = System.getProperty("path.separator");
        int index = fullClassPath.indexOf(pathSeparator);
        File classPath;
        if (index == -1) {
            classPath = new File(fullClassPath);
        } else {
            classPath = new File(fullClassPath.substring(0, index));
        }
        if (!(classPath.isAbsolute())) {
            classPath = new File(userDir, classPath.getPath()); 
        }
        if (classPath.isFile()) {
            classPath = classPath.getParentFile();
        }
        
        // ..., or in the dist_root directory in the current directory.
        File distRoot = new File(userDir, "dist_root");

        resourceDir = findResourceDirInPath(classPath);
        if (resourceDir == null) {
            resourceDir = findResourceDirInPath(userDir);
        }
        if (resourceDir == null) {
            resourceDir = findResourceDirInPath(distRoot);
        }
        if (resourceDir == null) {
            throw new ProgramError("Unable to find resource directory.");
        }

        LOG.info("Resource directory = " + resourceDir);
        return resourceDir;
    }

}
