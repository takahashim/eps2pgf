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
public class StackPSTest {
    
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
    public void pop1() throws Exception {
        String cmd = "true true false pop";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void exch1() throws Exception {
        String cmd = "1 2 exch 1 eq exch 2 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void dup1() throws Exception {
        String cmd = "(abcde) dup count 2 eq 3 1 roll pop pop"
            + " /foo dup cvx xcheck true eq"
            + " exch xcheck false eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void dup2() throws Exception {
        String cmd = "(abc) dup cvx xcheck true eq  exch xcheck false eq"
            + " 1.0 dup cvx xcheck true eq  exch xcheck false eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void dup3() throws Exception {
        String cmd = "1 dict dup cvx pop xcheck true eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void copy1() throws Exception {
        String cmd = "(a)(b)(c) 2 copy (c) eq 5 1 roll (b) eq 5 1 roll (c)"
            + " eq 5 1 roll (b) eq 5 1 roll (a) eq";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void copy2() throws Exception {
        String cmd = "(a)(b)(c) 0 copy (c) eq 3 1 roll (b) eq 3 1 roll (a) eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void index1() throws Exception {
        String cmd = "(a)(b) (c) (d) 0 index (d) eq 5 1 roll (d)"
            + " eq 5 1 roll (c) eq 5 1 roll (b) eq 5 1 roll (a) eq";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void index2() throws Exception {
        String cmd = "(a)(b) (c) (d) 3 index (a) eq 5 1 roll (d)"
            + " eq 5 1 roll (c) eq 5 1 roll (b) eq 5 1 roll (a) eq";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void roll1() throws Exception {
        String cmd = "(a)(b)(c) 3 -1 roll (a) eq 3 1 roll (c) eq 3 1 roll(b)eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void roll2() throws Exception {
        String cmd = "(a)(b)(c) 3 1 roll (b) eq 3 1 roll (a) eq 3 1 roll (c)eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void roll3() throws Exception {
        String cmd = "(a)(b)(c) 3 0 roll (c) eq 3 1 roll (b) eq 3 1 roll (a)eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void clear1() throws Exception {
        String cmd = "1 2 3 4 5 clear count 0 eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void count1() throws Exception {
        String cmd = "1 (b) 3 (d) 5 (f) count 6 eq"
            + " 7 1 roll pop pop pop pop pop pop";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void mark1() throws Exception {
        String cmd = "mark 1 2 3] type /arraytype eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void cleartomark1() throws Exception {
        String cmd = "1 2 [ 3 4 5 cleartomark 2 eq 2 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void counttomark1() throws Exception {
        String cmd = "1 mark 2 3 counttomark 2 eq 5 1 roll 3 eq 5 1 roll 2 eq"
            + " 5 1 roll mark eq 5 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd, 5));
    }

    /** Type test. @throws Exception the exception */
    @Test
    public void counttomark2() throws Exception {
        String cmd = "1 mark counttomark 0 eq 3 1 roll mark eq 3 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

}
