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
public class BoolPSTest {
    
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
    
    /** Boolean test 1. @throws Exception the exception */
    @Test
    public void eq1() throws Exception {
        String cmd = "1 1 eq 1.0 1 eq 1 1.0 eq  1.0 1.0 eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void eq2() throws Exception {
        String cmd = "(abc) (abc) eq (abc) /abc eq /abc (abc) eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void eq3() throws Exception {
        String cmd = "false false eq mark [ eq null null eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void eq4() throws Exception {
        String cmd = "[1 2 3] dup eq [1 2 3] [1 2 3] eq false eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void eq5() throws Exception {
        String cmd = "[1 2 3 4 5 6] dup 2 3 getinterval eq false eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void ne1() throws Exception {
        String cmd = "1 1 ne false eq  (abc)(abc) ne false eq  [1 2 3] [1 2 3]";
        cmd += " ne";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void ge1() throws Exception {
        String cmd = "4.2 4 ge  (abc)(de)ge false eq  (aba)(ab) ge  (aba)(aba)";
        cmd += " ge";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void gt1() throws Exception {
        String cmd = "4.0 4 gt false eq  4.4 4 gt  (abc)(de)gt false eq";
        cmd += "  (aba)(aba)gt false eq (abc)(ab)gt";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void le1() throws Exception {
        String cmd = "3.8 4 le  (abc)(de) le  (aba)(aba) le";
        cmd += "  (aba)(ab)le false eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void lt1() throws Exception {
        String cmd = "4.0 4 lt false eq  (abc)(de)lt  (aba)(aba)lt false eq";
        cmd += " (aba)(ab)lt false eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void and1() throws Exception {
        String cmd = "true true and  true false and false eq  99 1 and 1 eq";
        cmd += "  52 7 and 4 eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void not1() throws Exception {
        String cmd = "true not false eq  false not  52 not -53 eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void or1() throws Exception {
        String cmd = "true true or  true false or  false false or false eq";
        cmd += "  17 5 or 21 eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void xor1() throws Exception {
        String cmd = "true true xor false eq  true false xor";
        cmd +=  "  false false xor false eq  7 3 xor 4 eq  12 3 xor 15 eq";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Boolean test. @throws Exception the exception */
    @Test
    public void bitshift1() throws Exception {
        String cmd = "23 5 bitshift 736 eq  23 -2 bitshift 5 eq";
        cmd += " -8945 13 bitshift -73277440 eq  -8945 -8 bitshift 16777181 eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }


}
