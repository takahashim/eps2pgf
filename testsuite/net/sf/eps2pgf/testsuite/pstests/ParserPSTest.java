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
public class ParserPSTest {
    
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
    
    /** Test1. @throws Exception the exception */
    @Test
    public void parserTest1() throws Exception {
        String cmd = "[1 2] count 1 eq exch type /arraytype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test2. @throws Exception the exception */
    @Test
    public void parserTest2() throws Exception {
        String cmd = "<123d>(\022=) eq  <~j34rt~>(\343\314\266\226) ";
        cmd += "eq <~z~>(\000\000\000\000)eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test3. @throws Exception the exception */
    @Test
    public void parserTest3() throws Exception {
        String cmd = "<</a(foo)/b 3>> count 1 eq exch type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test 4. @throws Exception the exception */
    @Test
    public void parserTest4() throws Exception {
        String cmd = "<< 1 /a 2 /b >> count 1 eq exch type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test 5. @throws Exception the exception */
    @Test
    public void parserTest5() throws Exception {
        String cmd = "(lka(oi){\\)) dup length 9 eq exch type /stringtype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test 6. @throws Exception the exception */
    @Test
    public void parserTest6() throws Exception {
        String cmd = "{{ {1 2 add} {/foo put}} (}) 1 2 add} count 1 eq ";
        cmd += "exch type /arraytype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test 7. @throws Exception the exception */
    @Test
    public void parserTest7() throws Exception {
        String cmd = "16#0 0 eq  16#C 12 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test. @throws Exception the exception */
    @Test
    public void parserTest8() throws Exception {
        String cmd = "(\\\\) (\\134) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test. @throws Exception the exception */
    @Test
    public void parserTest9() throws Exception {
        String cmd = "<~abc~>(\311\211) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Parser test. @throws Exception the exception */
    @Test
    public void parserTest10() throws Exception {
        String cmd = "/nr 99 def {1 2 //nr 3 4} 2 get 99 eq"
            + " /key {1 2 //nr 3 4} def /nr 88 def key"
            + " 4 eq 5 1 roll 3 eq 5 1 roll 99 eq 5 1 roll 2 eq 5 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

}
