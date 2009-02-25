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
 * This class contains some test to test the PostScript parser.
 */
public class StringPSTest {
    
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
    public void string1() throws Exception {
        String cmd = "5 string (\000\000\000\000\000) eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void length1() throws Exception {
        String cmd = "(0123456789) length 10 eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void get1() throws Exception {
        String cmd = "(abcdef) 4 get 101 eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void get2() throws Exception {
        String cmd = "(abcdef) 2 2 getinterval 1 get 100 eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void put1() throws Exception {
        String cmd = "(abcdef) dup 2 48 put (ab0def) eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void getinterval1() throws Exception {
        String cmd = "(abcdef) 3 2 getinterval (de) eq"
            + " (abcdef) 3 3 getinterval (def) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void getinterval2() throws Exception {
        String cmd = "(abcdef) 2 4 getinterval 1 2 getinterval (de) eq"
            + " (abcdef) 2 4 getinterval 1 3 getinterval (def) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void putinterval1() throws Exception {
        String cmd = "(abcdef) dup 2 (1234) putinterval (ab1234) eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void putinterval2() throws Exception {
        String cmd = "(abcdef) dup 2 3 getinterval 1 (xx) putinterval"
            + " (abcxxf) eq";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void copy1() throws Exception {
        String cmd = "(1234567890) dup (xxxxx) exch copy (xxxxx) eq"
            + " 2 1 roll (xxxxx67890) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void copy2() throws Exception {
        String cmd = "(abc) (def) 2 copy 1 (x) putinterval (abc)"
            + " eq 3 1 roll (dxf) eq 3 1 roll (abc) eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void forall1() throws Exception {
        String cmd = "0 (abcdef) {add} forall 597 eq (ty) {} forall 121 eq"
            + " 2 1 roll 116 eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void anchorsearch1() throws Exception {
        String cmd = "(abbc)(ab) anchorsearch 3 1 roll (ab) eq 3 1 roll (bc)eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void anchorsearch2() throws Exception {
        String cmd = "(abbc)(bb) anchorsearch false eq 2 1 roll (abbc) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void anchorsearch3() throws Exception {
        String cmd = "(abbc)(bc) anchorsearch false eq 2 1 roll (abbc) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void anchorsearch4() throws Exception {
        String cmd = "(abbc)(B) anchorsearch false eq 2 1 roll (abbc) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void search1() throws Exception {
        String cmd = "(abbc)(ab) search 4 1 roll () eq 4 1 roll (ab) eq"
            + " 4 1 roll (bc) eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void search2() throws Exception {
        String cmd = "(abbc)(bb) search 4 1 roll (a) eq 4 1 roll (bb) eq"
            + " 4 1 roll (c) eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void search3() throws Exception {
        String cmd = "(abbc)(bc) search 4 1 roll (ab) eq 4 1 roll (bc) eq"
            + " 4 1 roll () eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void search4() throws Exception {
        String cmd = "(abbc)(B) search false eq 2 1 roll (abbc) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void token1() throws Exception {
        String cmd = "(15(St1){1 2 add}) token 3 1 roll 15 eq"
            + " 3 1 roll ((St1){1 2 add}) eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void token2() throws Exception {
        String cmd = "((St1){1 2 add}) token 3 1 roll (St1) eq"
            + " 3 1 roll ({1 2 add}) eq";
        assertTrue(Common.testString(interp, cmd, 3));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void token3() throws Exception {
        String cmd = "({1 2 add}) token 3 1 roll dup type /arraytype eq"
            + " 4 1 roll xcheck 4 1 roll () eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void token4() throws Exception {
        String cmd = "() token false eq count 1 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void misc1() throws Exception {
        String cmd = "(abcdefghijklmn) dup 1.2 exch 3 5 getinterval cvs (1.2)eq"
            + " 2 1 roll (abc1.2ghijklmn) eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

}
