#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# run_PS_tests.py (part of Eps2pgf)
#
# Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import os
import re
import sys
import subprocess

# Command run the PostScript interpreter. %outputFile% and %inputFile% are
# automatically replaced by the real paths.
if len(sys.argv) > 1 and sys.argv[1] == 'gs':
    programCmd = "\"C:\\Program Files\\gs\\gs8.60\\bin\\gswin32c.exe\" -sDEVICE=pdfwrite -sOutputFile=\"%outputFile%\" -dBATCH -q \"%inputFile%\""
    nextArg = 2
else:
    programCmd = "java -jar ..\\dist_root\\eps2pgf.jar \"%inputFile%\" --output \"%outputFile%\""
    nextArg = 1
    
if len(sys.argv) > nextArg:
    nameFilter = sys.argv[nextArg]
else:
    nameFilter = '.*'

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
        
        if not re.match(nameFilter, name):
            continue
        
        print '%(linenr)4d %(name)-32s' % {'linenr':linenr, 'name':name},
        
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
