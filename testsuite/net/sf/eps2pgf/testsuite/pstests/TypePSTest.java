/*
 * TypePSTest.java
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
public class TypePSTest {
    
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
    
    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest1Type() throws Exception {
        String cmd = "[1 2 3] type /arraytype eq {1 2 add} type /arraytype eq";
        cmd += " matrix type /arraytype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest2Type() throws Exception {
        String cmd = "true type /booleantype eq  null type /nulltype eq";
        cmd += " mark type /marktype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest3Type() throws Exception {
        String cmd = "123 type /integertype eq  1.1 type /realtype eq";
        cmd += " (abc) type /stringtype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest4Type() throws Exception {
        String cmd = "10 dict type /dicttype eq";
        cmd += " /add load type /operatortype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest5Type() throws Exception {
        String cmd = "/burp type /nametype eq  1 type type /nametype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest6Cvlit() throws Exception {
        String cmd = "{1 2 add} dup cvlit exch bind pop 2 get type"
            + " /operatortype eq  1 cvlit pop";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest7Cvx() throws Exception {
        String cmd = "[1 2 3]dup cvx exec count 4 eq"
            + " 5 -1 roll exec count 5 eq"
            + " exch pop 5 -1 roll 1 eq"
            + " 5 -1 roll 2 eq 5 -1 roll 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest8Cvx() throws Exception {
        String cmd = "[1 2 3] dup cvx 2 /x put 2 get /x eq  [1 2 3] dup cvx eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest9Xcheck() throws Exception {
        String cmd = "/koe xcheck false eq  [1 2 3] xcheck false eq"
            + " {1 2 3} xcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest10Xcheck() throws Exception {
        String cmd = "/add cvx load xcheck true eq  /koe cvx xcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest11Executeonly() throws Exception {
        String cmd = "(123) executeonly rcheck false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest12Noaccess() throws Exception {
        String cmd = "[1 2] noaccess pop {/abc noaccess rcheck} stopped true eq"
            + " {9 dict executeonly noaccess} stopped true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest13Readonly() throws Exception {
        String cmd = "[1 2 3] readonly dup rcheck true eq exch wcheck false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest14Rcheck() throws Exception {
        String cmd = "[1 2 3] rcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest15Wcheck() throws Exception {
        String cmd = "(koe) wcheck true eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest16Cvi() throws Exception {
        String cmd = "(3.3E1) cvi 33 eq  2 cvi 2 eq  -47.8 cvi -47 eq"
            + " 520.9 cvi 520 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest17Cvn() throws Exception {
        String cmd = "(abc) cvn type /nametype eq  (abc) cvn xcheck false eq"
            + " (abc) cvn /abc eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest18Cvn() throws Exception {
        String cmd = "(abc) cvx cvn type /nametype eq"
            + " (abc) cvx cvn xcheck true eq   (abc) cvx cvn /abc eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest19Cvr() throws Exception {
        String cmd = "1.1 cvr 1.1 eq  78 cvr 78.0 eq  (78.9) cvr 78.9 eq"
            + " (1e3) cvr 1000.0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest20Cvrs() throws Exception {
        String cmd = "/temp 12 string def  123 10 temp cvrs (123) eq"
            + " -123 10 temp cvrs (-123) eq  123.4 10 temp cvrs (123.4) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest21Cvrs() throws Exception {
        String cmd = "/temp 12 string def  123 16 temp cvrs (7B) eq"
            + " -123 16 temp cvrs (FFFFFF85) eq  123.4 16 temp cvrs (7B) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest22Cvs() throws Exception {
        String cmd = "579 20 string cvs (579) eq"
            + " mark 20 string cvs (--nostringval--) eq "
            + " /foo 20 string cvs (foo) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void typeTest23Cvx() throws Exception {
        String cmd = "/proc {{1} {2} /gt cvx} bind def proc xcheck";
        cmd += " 3 1 roll pop pop /proc load 2 get xcheck false eq";
        assertTrue(Common.testString(interp, cmd));
    }


}
