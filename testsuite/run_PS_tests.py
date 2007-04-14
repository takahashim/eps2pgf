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

# Command run the PostScript interpreter. %outputFile% and %inputFile% are
# automatically replaced by the real paths.
#programCmd = "\"C:\\Program Files\\gs\\gs8.56\\bin\\gswin32c.exe\" -sDEVICE=pdfwrite -sOutputFile=\"%outputFile%\" -dBATCH -q \"%inputFile%\""
programCmd = "java -jar ..\\dist\\eps2pgf.jar \"%inputFile%\" --output \"%outputFile%\""

def main():
    scriptDir = findScriptDir()
    testFile = os.path.join(scriptDir, 'PSTests.txt')
    tests = loadTestFile(testFile)
    
    global workDir
    workDir = os.path.join(scriptDir, 'workdir')
    if not os.path.exists(workDir):
        os.mkdir(workDir)
    
    global inputFile
    inputFile = os.path.join(workDir, 'input.ps')
    global programCmd
    programCmd = programCmd.replace('%inputFile%', inputFile)
    
    global outputFile
    outputFile = os.path.join(workDir, 'output')
    programCmd = programCmd.replace('%outputFile%', outputFile)
    
    errors = list()
    failed = list()
    for test in tests:
        linenr = test[0]
        name = test[1]
        code = test[2]
        
        print '%(linenr)4d %(name)-30s' % {'linenr':linenr, 'name':name},
        
        fd = open(inputFile, 'w')
        fd.write(code)
        if not code.endswith("pstack"):
            fd.write(" pstack")
        fd.close()
        
        if os.path.exists(outputFile):
            os.remove(outputFile)
            
        # Execute the interpreter
        procObj = subprocess.Popen(programCmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        procObj.wait()
        
        # Check standard error
        if len(procObj.stderr.read()) > 0:
            print "ERROR"
            errors.append(name)
        elif hasFalse(procObj.stdout.read()):
            print "FAILED"
            failed.append(name)
        else:
            print "ok"
            
    print '-----'
    print '%(nr)d error(s)' % {'nr':len(errors)},
    for err in errors:
        print "'" + err + "'",
    print
    print '%(nr)d failed' % {'nr':len(failed)},
    for fail in failed:
        print "'" + fail + "'",
    print


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

def hasFalse(text):
    """Checks whether a text (typically standard output of postscript
    interpreter) has any lines with the string 'false'.
    
    """
    for line in text.splitlines():
        line = line.strip().lower()
        if line == 'false':
            return True
        
    return False

   
def cleanup():
    """Cleans up the working directory.
    
    """
    global inputFile
    if os.path.exists(inputFile):
        os.remove(inputFile)
    
    global outputFile
    if os.path.exists(outputFile):
        os.remove(outputFile)
        
    global workDir
    if os.path.exists(workDir):
        try:
            os.rmdir(workDir)
        except WindowsError:
            print "WARNING: Failed to remove workdir ("+workDir+")"
            pass
    

main()
cleanup()
