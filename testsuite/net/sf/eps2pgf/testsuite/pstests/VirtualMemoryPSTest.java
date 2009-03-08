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

package net.sf.eps2pgf.testsuite.pstests;

import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.eps2pgf.ps.Interpreter;

/**
 * This class contains some test to test the PostScript parser.
 */
public class VirtualMemoryPSTest {
    
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
    public void currentAndSetglobal() throws Exception {
        String cmd = "currentglobal not"
            + " true setglobal currentglobal"
            + " false setglobal currentglobal not";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore1() throws Exception {
        String cmd = "save"
            + " errordict /undefined {1 2 3 4 5 6 7 8 9 10 11 12 13 14 15} put"
            + " errordict /undefined get length 15 eq"
            + " exch restore"
            + " errordict /undefined get length 15 ne";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore2() throws Exception {
        String cmd = "currentpacking save exch dup not setpacking dup"
            + " currentpacking ne 3 1 roll exch restore currentpacking eq"
            + " currentglobal save exch dup not setglobal dup currentglobal ne"
            + " 3 1 roll exch restore currentglobal eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore3() throws Exception {
        String cmd = "currentuserparams /MaxExecStack get dup save exch 1 sub"
            + " dup /MaxExecStack exch << 3 1 roll >> setuserparams"
            + " currentuserparams /MaxExecStack get eq 3 1 roll restore"
            + " currentuserparams /MaxExecStack get eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore4() throws Exception {
        String cmd = "{save grestore restore} stopped false eq"
            + "{restore} stopped";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore5() throws Exception {
        String cmd = "[1 2 3] dup save exch 0 (a) put restore dup 0 get 1 eq"
            + " exch dup 0 (a) put 0 get (a) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore6() throws Exception {
        String cmd = "save /abc 1 def restore /abc where not";
        assertTrue(Common.testString(interp, cmd, 1));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore7() throws Exception {
        String cmd = "{save save exch restore restore} stopped"
            + " exch type /savetype eq"
            + "{save save restore restore} stopped not";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore8() throws Exception {
        String cmd = "/a [1 2 3] def save a 0 99 put a exch restore"
            + " a 0 get 1 eq exch 0 get 1 eq"
            + " /b (abc) def save b 0 100 put b exch restore"
            + " b 0 get 100 eq exch 0 get 100 eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void saveRestore9() throws Exception {
        String cmd = "save /test [1 2 3] def restore /test where not";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void saveGrestoreall() throws Exception {
        String cmd = "gsave 2 setlinewidth save 3 setlinewidth grestoreall"
            + " currentlinewidth 2 eq exch pop";
        assertTrue(Common.testString(interp, cmd, 1));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void localGlobalVM1() throws Exception {
        String cmd = "/lstr (string1) def"
            + " /ldict 10 dict def"
            + " true setglobal"
            + " /gstr (string2) def"
            + " /gdict 5 dict def"
            + " false setglobal"
            + " {ldict /a lstr put} stopped not"
            + " {gdict /b gstr put} stopped not"
            + " {ldict /c gstr put} stopped not"
            + " {gdict /d lstr put} stopped 4 1 roll pop pop pop"
            + " {gdict /e 7 put} stopped not";
        assertTrue(Common.testString(interp, cmd, 5));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void localGlobalVM2() throws Exception {
        String cmd = "{save 3 array dup /a def exch restore} stopped"
            + " 3 1 roll pop pop"
            + " true setglobal save 3 array dup /b def exch restore"
            + " type /arraytype eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void gcheck1() throws Exception {
        String cmd = "1 gcheck"
            + " /lstr (string1) def"
            + " /ldict 10 dict def"
            + " /larray [1 2 3] def"
            + " true setglobal"
            + " /gstr (string2) def"
            + " /gdict 5 dict def"
            + " /garray [1 2 3] def"
            + " false setglobal"
            + " lstr gcheck not"
            + " ldict gcheck not"
            + " larray gcheck not"
            + " gstr gcheck"
            + " gdict gcheck"
            + " garray gcheck";
        assertTrue(Common.testString(interp, cmd, 7));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void gcheck2() throws Exception {
        String cmd = "{100 array execstack dup length 1 sub get gcheck not}"
            + " stopped not";
        assertTrue(Common.testString(interp, cmd, 2));
    }
    
    
    

}
