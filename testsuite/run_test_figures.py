#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# This file is part of Eps2pgf.
#
# Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
#
# Pw1-todo is free software; you can redistribute it and/or modify it under
# the terms of version 2 of the GNU General Public License as published by the
# Free Software Foundation.
#
# Pw1-todo is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
#
# You should have received a copy of the GNU General Public License along with
# this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA. 
#

import os
import re
import sys
import subprocess

programCmd = "java -jar ..\\dist\\eps2pgf.jar \"%inputFile%\" --output \"%outputFile%\""
nextArg = 1
    
if len(sys.argv) > 1:
    nameFilter = sys.argv[1]
else:
    nameFilter = '.*'

def main():
    scriptDir = findScriptDir()
    testFile = os.path.join(scriptDir, 'test_figures.txt')
    tests = loadTestFile(testFile)
    
    workDir = os.path.join(scriptDir, 'workdir')
    if not os.path.exists(workDir):
        os.mkdir(workDir)
    
    errorsPgf = list()
    errorsTex = list()
    for test in tests:
        linenr = test[0]
        basename = test[1]
        args = test[2]
        
        if not re.match(nameFilter, basename):
            continue
        
        print '%(linenr)4d %(name)-30s' % {'linenr':linenr, 'name':basename},
        
        # Determine input filename
        if os.path.exists(os.path.join('test_figures', basename+'.eps')):
            inputFile = os.path.join('test_figures', basename+'.eps')
        elif os.path.exists(os.path.join('test_figures', basename+'.ps')):
            inputFile = os.path.join('test_figures', basename+'.ps')
        else:
            print "ERROR (file not found)"
            errorsPgf.append(basename)
            continue
        
        # Determine output filename
        outputFile = os.path.join(workDir, basename+'.pgf')
        
        # Prepare program command
        global programCmd
        thisCmd = programCmd.replace('%inputFile%', inputFile)
        thisCmd = thisCmd.replace('%outputFile%', outputFile)
        
        # Execute the interpreter
        procObj = subprocess.Popen(thisCmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        procObj.wait()
        
        # Check standard error
        errorMsg = procObj.stderr.read()
        if len(errorMsg) > 0:
            print "ERROR"
            errorsPgf.append(basename)
        else:
            print "ok"
            
    print '-----'
    print '%(nr)d eps2pgf error(s)' % {'nr':len(errorsPgf)},
    for err in errorsPgf:
        print "'" + err + "'",
    print
    
    cleanup(workDir)


def findScriptDir():
    """Tries to find the dir of the script that is currently running. In most
    situations this is sys.path[0], but that is not always the case.
    
    """
    # sys.argv[0] is the can contain the full path, but that is not garantueed
    if os.path.isabs(sys.argv[0]) and False:
        return os.path.dirname(sys.argv[0])
    else:
        scriptName = os.path.basename(sys.argv[0])
        
    # The next best thing we can do to find the directory is to use the first
    # directory in the sys.path list that has a file with the name <scriptName>
    # in it. Normally, this is the first directory in the list.
    for path in sys.path:
        if os.path.exists(os.path.join(path, scriptName)):
            return path
        
    return None

def loadTestFile(testFile):
    if not os.path.exists(testFile):
        exit("ERROR: Unable to find file with PostScript tests ("+testFile+").")
        
    tests= list()
    reObj = re.compile("([^:]*)\s*:\s*(.*)\s*")
    fd = open(testFile, 'r')
    linenr = 0
    for line in fd:
        linenr += 1
        line = line.strip()
        if len(line) == 0:
            continue
        
        matchObj = reObj.search(line)
        tests.append( (linenr, matchObj.group(1).strip(), matchObj.group(2).strip()) )
    
    fd.close()
    
    return tests

def cleanup(workDir):
    """Cleans up the working directory.
    
    """
    if os.path.exists(workDir):
        try:
            files = os.listdir(workDir)
            # First, remove all .pgf files
            for filename in files:
                fullfile = os.path.join(workDir, filename)
                if os.path.isfile(fullfile) and filename.endswith('.pgf'):
                    os.remove(fullfile)
            
            # Last, remove the directory itself
            os.rmdir(workDir)
        except WindowsError:
            print "WARNING: Failed to remove workdir ("+workDir+")"
            pass
    

main()
