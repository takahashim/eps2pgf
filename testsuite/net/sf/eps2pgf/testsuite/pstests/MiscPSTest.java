/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2009 Paul Wagenaars <paul@wagenaars.org>
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
 * This class contains some test to test the PostScript handling.
 */
public class MiscPSTest {
    
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
    public void miscTest1() throws Exception {
        String cmd = "version type /stringtype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest2() throws Exception {
        String cmd = "realtime type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest3() throws Exception {
        String cmd = "usertime type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest4() throws Exception {
        String cmd = "languagelevel type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest5() throws Exception {
        String cmd = "product type /stringtype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest6() throws Exception {
        String cmd = "revision type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest7() throws Exception {
        String cmd = "serialnumber type /integertype eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void miscTest8() throws Exception {
        String cmd = "letter a4 legal a3 true";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest9() throws Exception {
        String cmd = "<</DoesntExist (test) /JobName (myname)>> setuserparams"
            + " currentuserparams /JobName get (myname) eq"
            + " currentuserparams /DoesntExist known false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void miscTest10() throws Exception {
        String cmd = "<</PrinterName (myname)>> setsystemparams"
            + " currentsystemparams /PrinterName get (myname) eq";
        assertTrue(Common.testString(interp, cmd));
    }

}
