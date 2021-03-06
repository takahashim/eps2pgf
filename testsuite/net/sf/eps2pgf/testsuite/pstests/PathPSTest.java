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
public class PathPSTest {
    
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
    public void newpath() throws Exception {
        String cmd = "newpath true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void moveto() throws Exception {
        String cmd = "0 0 moveto currentpoint 0 eq exch 0 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void rmoveto() throws Exception {
        String cmd = "1 2 moveto 1 2 rmoveto currentpoint 4 eq exch 2 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void lineto() throws Exception {
        String cmd = "9 9 moveto 10 10 lineto true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void rlineto() throws Exception {
        String cmd = "1 2 moveto 1 2 rlineto currentpoint 4 eq exch 2 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arc() throws Exception {
        String cmd = "1 2 3 4 90 arc true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arcn() throws Exception {
        String cmd = "1 2 3 4 5 arcn true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arct() throws Exception {
        String cmd = "0 0 moveto 0 4 4 4 1 arct true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arcto1() throws Exception {
        String cmd = "0 0 moveto 0 4 4 4 1 arcto 4.0 eq  4 1 roll 1.0 eq"
            + " 4 1 roll 3.0 eq  4 1 roll 0.0 eq"
            + " currentpoint"
            + " 4.00 sub abs 0.001 lt 2 1 roll"
            + " 1.00 sub abs 0.001 lt"; 
        assertTrue(Common.testString(interp, cmd, 6));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arcto2() throws Exception {
        String cmd = "10 0 moveto 0 0 -10 0 1 arcto  0.0 eq  4 1 roll 0.0 eq"
            + " 4 1 roll 0.0 eq  4 1 roll 0.0 eq"
            + " currentpoint"
            + " 0.0 sub abs 0.001 lt 2 1 roll"
            + " 0.0 sub abs 0.001 lt";
        assertTrue(Common.testString(interp, cmd, 6));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void arcto3() throws Exception {
        String cmd = "10 0 moveto 0 0 10 0 1 arcto  0.0 eq  4 1 roll 0.0 eq"
            + " 4 1 roll 0.0 eq  4 1 roll 0.0 eq"
            + " currentpoint"
            + " 0.0 sub abs 0.001 lt 2 1 roll"
            + " 0.0 sub abs 0.001 lt";
        assertTrue(Common.testString(interp, cmd, 6));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void arcto4() throws Exception {
        String cmd = "0 0 moveto 90 0 100 10 10 arcto currentpoint"
            + " 2.929 sub abs 0.001 lt 6 1 roll"
            + " 92.929 sub abs 0.001 lt 6 1 roll"
            + " 2.929 sub abs 0.001 lt 6 1 roll"
            + " 92.929 sub abs 0.001 lt 6 1 roll"
            + " 0.000 sub abs 0.001 lt 6 1 roll"
            + " 85.858 sub abs 0.001 lt 6 1 roll";
        assertTrue(Common.testString(interp, cmd, 6));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void curveto() throws Exception {
        String cmd = "0 0 moveto 1 2 3 4 5 6 curveto true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void rcurveto() throws Exception {
        String cmd = "1 1 moveto 1 2 3 4 5 6 rcurveto currentpoint 7.0 div dup"
            + " 1 lt {1 exch div} if 1.001 le  exch 6.0 div dup 1 lt {1 exch"
            + " div} if 1.001 le";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void flattenpath() throws Exception {
        String cmd = "100 100 moveto 110 110 100 0 120 arc flattenpath"
            + " {} {} {notgood} {} pathforall"
            + " 196.6 sub abs 0.1 lt" 
            + " count 1 roll"
            + " 1 1 count 4 sub {pop pop} for"
            + " 100 sub abs 0.1 lt";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void clippath() throws Exception {
        String cmd = "clippath 1 setgray fill true";
        assertTrue(Common.testString(interp, cmd, 1));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathbbox1() throws Exception {
        String cmd = "100 100 moveto 150 150 150 150 200 100 curveto pathbbox"
            + " 150 sub abs 1e-3 lt  4 1 roll 200 sub abs 1e-3 lt  4 1 roll 100"
            + " sub abs 1e-3 lt  4 1 roll 100 sub abs 1e-3 lt";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathbbox2() throws Exception {
        String cmd = "100 100 moveto 150 150 150 150 200 100 curveto"
            + " flattenpath pathbbox  137.5 sub abs 1e-1 lt  4 1 roll 200 sub"
            + " abs 1e-3 lt  4 1 roll 100 sub abs 1e-3 lt  4 1 roll 100 sub abs"
            + " 1e-3 lt";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathbbox3() throws Exception {
        String cmd = "100 100 moveto 100 200 lineto 200 200 lineto 200 100"
            + " lineto closepath 45 rotate pathbbox  70.71 sub abs 0.1 lt"
            + " 4 1 roll 282.84 sub abs 0.1 lt  4 1 roll -70.71 sub abs 0.1 lt"
            + " 4 1 roll 141.42 sub abs 0.1 lt";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathbbox4() throws Exception {
        String cmd = "62 79 moveto 92 74 lineto 100 100 moveto pathbbox 79 sub"
            + " abs 1e-3 lt  4 1 roll 92 sub abs 1e-3 lt  4 1 roll 74 sub abs"
            + " 1e-3 lt  4 1 roll 62 sub abs 1e-3 lt";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathbbox5() throws Exception {
        String cmd = "100 100 moveto pathbbox 100 sub abs 1e-3 lt  4 1 roll 100"
            + " sub abs 1e-3 lt  4 1 roll 100 sub abs 1e-3 lt  4 1 roll 100 sub"
            + " abs 1e-3 lt";
        assertTrue(Common.testString(interp, cmd, 4));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathforall1() throws Exception {
        String cmd = "1 2 moveto 3 4 lineto 1 2 3 4 5 6 curveto 2 2 scale {sub}"
            + " {add add} {add sub add sub add add} {} pathforall 6.5 exch"
            + " 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void pathforall2() throws Exception {
        String cmd = "1 2 moveto 3 4 lineto {exit} {} {} {} pathforall 2 eq"
            + " exch 1 eq";
        assertTrue(Common.testString(interp, cmd, 2));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void charpath() throws Exception {
        String cmd = "[1 0 0 1 0 0] setmatrix /Times-Roman findfont 10"
            + " scalefont setfont 0 0 moveto (abc) true charpath pathbbox floor"
            + " 6 eq 4 1 roll floor 13 eq 4 1 roll floor -1 eq 4 1 roll floor 0"
            + " eq";
        assertTrue(Common.testString(interp, cmd, 4));
    }

}
