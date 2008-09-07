/*
 * FilePSTest.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.testsuite.pstests;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import net.sf.eps2pgf.ps.Interpreter;

/**
 * This class contains some test to test the PostScript parser.
 */
public class FilePSTest {
    
    /** The PostScript interpreter. */
    private Interpreter interp = null;
    
    /**
     * Sets up the class.
     * 
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger("net.sourceforge.eps2pgf").setLevel(Level.OFF);
    }
    
    /**
     * Set up a single test.
     * 
     * @throws Exception An exception occurred.
     */
    @Before
    public void setUp() throws Exception {
        interp = new Interpreter();
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void fileTest1_Currentfile() throws Exception {
        String cmd = "currentfile type /filetype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void fileTest2_Readstring() throws Exception {
        String cmd = "currentfile (abc) readstring  123 pop exch ( 12) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void fileTest3_Readstring() throws Exception {
        String cmd = "{currentfile () readstring} stopped"
            + " count 1 roll"
            + " 1 1 count 3 sub {pop pop} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void fileTest4() throws Exception {
        String cmd = "currentfile /ASCII85Decode filter /RunLengthDecode filter"
            + " 100 string readstring\n"
            + "@fQ`D'c\\GJ0fVB&!!`uK*$Zpf3\"?/n#7_Ig,:YD_%L2t=%M]s..NB05#64u='c"
            + "\\GC*#/qg!!`uK*%E0Q\n"
            + "'b1HK#7_J'.O,oJ%L2t=%QH0m,9.F.#64un3&)m-*#/qg!.Y\n"
            + "~> exch"
            + "(\\000\\007\\016\\025\\034#*18?\\007\\000\\007\\016\\025\\034#*1"
            + "8\\016\\007\\000\\007\\016\\025\\034#*1\\025\\016\\007\\000\\007"
            + "\\016\\025\\034#*\\034\\025\\016\\007\\000\\007\\016\\025\\034##"
            + "\\034\\025\\016\\007\\000\\007\\016\\025\\034*#\\034\\025\\016"
            + "\\007\\000\\007\\016\\0251*#\\034\\025\\016\\007\\000\\007\\0168"
            + "1*#\\034\\025\\016\\007\\000\\007?81*#\\034\\025\\016\\007\\000)"
            + "\neq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest5_Readhexstring() throws Exception {
        String cmd = "currentfile 4 string readhexstring 1aBf90f6 exch"
            + " (\032\277\220\366) eq\n";
        cmd += "currentfile 6 string readhexstring 123456>notexecutedFexch"
            + " (\0224V\356\316\337) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest6_SubFileDecode() throws Exception {
        String cmd = "currentfile 0 (TheEnd) /SubFileDecode filter 99 string"
            + " readstring blablaTheEndfalse eq 2 1 roll (blabla) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest7_SubFileDecode() throws Exception {
        String cmd = "currentfile << /Foo (bar) >> 1 (TheEnd) /SubFileDecode"
            + " filter 99 string"
            + " readstring blablaTheEndfalse eq 2 1 roll (blablaTheEnd) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest8_SubFileDecode() throws Exception {
        String cmd = "currentfile 6 () /SubFileDecode filter 99 string"
            + " readstring blablafalse eq 2 1 roll (blabla) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest9_SubFileDecode() throws Exception {
        String cmd = "currentfile 2 (TheEnd) /SubFileDecode filter 99 string"
            + " readstring blaTheEndblaTheEnd false eq 2 1 roll"
            + " (blaTheEndblaTheEnd) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest10_SubFileDecode() throws Exception {
        String cmd = "currentfile << /EODCount 2 /EODString (TheEnd) >>"
            + " /SubFileDecode filter 99 string"
            + " readstring blaTheEndblaTheEnd false eq 2 1 roll"
            + " (blaTheEndblaTheEnd) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest11_Status() throws Exception {
        String cmd = "currentfile /ASCIIHexDecode filter dup dup status 3 1"
            + " roll closefile status false eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    
    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest12_Flushfile() throws Exception {
        String cmd = "currentfile 0 (%%End) /SubFileDecode filter dup flushfile"
            + " %%End status false eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    /** Test. @throws Exception the exception. */
    @Test
    public void fileTest13() throws Exception {
        String cmd = "currentfile 5 string readline k\n"
            + "exch (k) eq\n"
            + "currentfile 10 string readline "
            + "foo bar \n"
            + "exch (foo bar ) eq\n"
            + "currentfile 10 string dup 3 1 roll readline\n"
            + "next line\n"
            + "3 1 roll pop (next line\000) eq\n";
        assertTrue(Common.testString(interp, cmd));
    }
    

    
}
