/*
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
public class ControlPSTest {
    
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
    public void controlTest1Exec() throws Exception {
        String cmd = "{1 2 add} exec 3 eq  [1 2 /add load] exec length 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest2Exec() throws Exception {
        String cmd = "(3 2 add) cvx exec 5 eq  /add exec /add eq"
            + " 3 2 /add cvx exec 5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest3If() throws Exception {
        String cmd = "99 false {1 2 add} if 99 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest4If() throws Exception {
        String cmd = "true {1 2 add} if 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest5Ifelse() throws Exception {
        String cmd = "1 2 true {add} {sub} ifelse 3 eq"
            + " 1 2 false {add}{sub} ifelse -1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest6For() throws Exception {
        String cmd = "0 1 1 4 {add} for dup 10 eq exch type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest7For() throws Exception {
        String cmd = "1 2 6 {} for 5 eq  3 1 roll 3 eq"
            + " 3 1 roll type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest8For() throws Exception {
        String cmd = "3 -.5 1 {} for 1.0 eq  5 1 roll 1.5 eq  5 1 roll 2.0 eq"
            + " 5 1 roll 2.5 eq  5 1 roll 3.0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest9Repeat() throws Exception {
        String cmd = "4 {(abc)} repeat (abc) eq  4 1 roll (abc) eq"
            + " 4 1 roll (abc) eq  4 1 roll (abc) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest10Repeat() throws Exception {
        String cmd = "1 2 3 4 3 {pop} repeat 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest11Repeat() throws Exception {
        String cmd = "mark 0 {(will not happen)} repeat mark eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest12Loop() throws Exception {
        String cmd = "1 {1 add dup 5 eq {exit} if} loop 5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest13Loop() throws Exception {
        String cmd = "1 {1 add dup 5 eq {stop} if} loop 5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest14Exit() throws Exception {
        String cmd = "0 2 999 {dup 6 eq {exit} if} for 6 eq  4 1 roll 4 eq"
            + " 4 1 roll 2 eq  4 1 roll 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest15Exit() throws Exception {
        String cmd = "[1 2 3] {exit} forall 1 eq"
            + " 99 {exit} repeat count 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest16Exit() throws Exception {
        String cmd = "{exit} loop true";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest17Stop() throws Exception {
        String cmd = "{1 2 stop 3 4} stopped  3 1 roll 2 eq  3 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest18Stopped() throws Exception {
        String cmd = "{add} stopped  {1 2} stopped false eq"
            + " 3 1 roll 2 eq 3 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest19Execstack() throws Exception {
        String cmd = "{1 2 100 array execstack 3 4} exec pop pop"
            + " 3 1 roll pop pop dup length 1"
            + " sub get dup 0 get 3 eq  exch 1 get 4 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest20Countexecstack() throws Exception {
        String cmd = "countexecstack {countexecstack 999} exec pop sub -1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void controlTest21For() throws Exception {
        String cmd = "1 1 1 {99} for 99 eq exch 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

}
