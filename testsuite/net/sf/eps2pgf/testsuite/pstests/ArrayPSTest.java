/*
 * ArrayPSTest.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007, 2008 Paul Wagenaars <paul@wagenaars.org>
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
public class ArrayPSTest {
    
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
    public void arrayTest1Array() throws Exception {
        String cmd = "3 array dup type /arraytype eq exch dup 0 get null eq"
            + " exch dup 1 get null eq exch 2 get null eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest2Length() throws Exception {
        String cmd = "[1 2 3] length 3 eq [] length 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest3Length() throws Exception {
        String cmd = "{1 2 3} length 3 eq {} length 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest4Get() throws Exception {
        String cmd = "[31 41 59] 0 get 31 eq"
            + " [0 (string 1) [] {add 2 div}] 2 get dup type /arraytype eq"
            + " exch length 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest5Get() throws Exception {
        String cmd = "{add 41 59} 0 get /add eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest6Put() throws Exception {
        String cmd = "/ar [5 17 3 8] def ar 2 (abcd) put ar 2 get (abcd) eq"
            + " ar length 4 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest7Put() throws Exception {
        String cmd = "{a b /c} dup 1 (hoi) put dup length 3 eq"
            + " exch 1 get (hoi) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest8Getinterval() throws Exception {
        String cmd = "[9 8 7 6 5] 1 3 getinterval dup 0 get 8 eq"
            + " exch dup 1 get 7 eq exch dup 2 get 6 eq exch length 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest9Getinterval() throws Exception {
        String cmd = "{9 8 7 6 5} 1 3 getinterval dup 0 get 8 eq"
            + " exch dup 1 get 7 eq exch dup 2 get 6 eq exch length 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest10Getinterval() throws Exception {
        String cmd = "[1 2 3 4 5 6] 1 4 getinterval 1 2 getinterval dup 0 get"
            + " 3 eq exch 1 get 4 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest11Getinterval() throws Exception {
        String cmd = "{1 2 3} 1 2 getinterval xcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest12Putinterval() throws Exception {
        String cmd = "/ar [5 8 2 7 3] def ar 1 [(a)(b)(c)] putinterval"
            + " ar 1 get (a) eq ar length 5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest13Putinterval() throws Exception {
        String cmd = "{5 8 2 7 3} dup 1 {(a)(b)(c)} putinterval dup 1 get (a)eq"
            + " exch length 5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest14Putinterval() throws Exception {
        String cmd = "[1 2 3 4 5 6] dup 3 3 getinterval 1 [98 99] putinterval"
            + " 5 get 99 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest15Astore() throws Exception {
        String cmd = "(a)(bcd)(ef) 3 array astore dup length 3 eq exch 2 get"
            + " (ef) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest16Aload() throws Exception {
        String cmd = "[23 (ab) -1] aload type /arraytype eq 4 1 roll -1 eq"
            + " 4 1 roll (ab) eq 4 1 roll 23 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest17Aload() throws Exception {
        String cmd = "{23 (ab) -1} aload type /arraytype eq 4 1 roll -1 eq"
            + " 4 1 roll (ab) eq 4 1 roll 23 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest18Copy() throws Exception {
        String cmd = "[/a /b /c /d /e /f] dup [1 2 3] exch copy length 3 eq"
            + " exch dup 0 get 1 eq exch 4 get /e eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest19Copy() throws Exception {
        String cmd = "{/a /b /c /d /e /f} dup {1 2 3} exch copy length 3 eq"
            + " exch dup 0 get 1 eq exch 4 get /e eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest20Copy() throws Exception {
        String cmd = "{1 2 3} {/a /b /c /d /e /f} copy xcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest21Forall() throws Exception {
        String cmd = "0 [13 29 3 -8 21] {add} forall 58 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arrayTest22Forall() throws Exception {
        String cmd = "{/a 78} {} forall 78 eq exch /a eq";
        assertTrue(Common.testString(interp, cmd));
    }

}
