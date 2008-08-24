/*
 * GraphicsPSTest.java
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
public class GraphicsPSTest {
    
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
    public void graphicsTest1Currentcolorspace() throws Exception {
        String cmd = "currentcolorspace 0 get /DeviceGray eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest2Setcolorspace() throws Exception {
        String cmd = "/DeviceRGB setcolorspace currentcolorspace 0 get"
            + " /DeviceRGB eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest3Setcolorspace() throws Exception {
        String cmd = "[/Indexed /DeviceRGB 2 <FF0000 00FF00 0000FF>]"
            + " setcolorspace 1.0 setcolor currentrgbcolor 0.0 eq 3 1 roll 1.0"
            + " eq 3 1 roll 0.0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest4Setcolorspace() throws Exception {
        String cmd = "[/Indexed /DeviceRGB 3 <012345 6789AB CDEF01 234567>]"
            + " setcolorspace"
            + " currentcolorspace 3 get <012345 6789AB CDEF01 234567> eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest4Currentcolor() throws Exception {
        String cmd = "currentcolor 0 eq count 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest5Setcolor() throws Exception {
        String cmd = "0.5 setcolor currentcolor 0.5 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest6Setcolor() throws Exception {
        String cmd = "0.5 setcolor /DeviceRGB setcolorspace currentcolor 0 eq"
            + " 3 1 roll 0 eq 3 1 roll 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest7Setcolor() throws Exception {
        String cmd = "/DeviceRGB setcolorspace 0.1 0.2 0.3 setcolor"
            + " currentcolor 0.3 eq 3 1 roll 0.2 eq 3 1 roll 0.1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest8Setgray() throws Exception {
        String cmd = "0.4 setgray currentcolorspace 0 get /DeviceGray eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest9Setgray() throws Exception {
        String cmd = "0.4 setgray currentcolor 0.4 eq count 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest10Sethsbcolor() throws Exception {
        String cmd = "0.123 0.456 0.789 sethsbcolor currentcolorspace 0 get"
            + " /DeviceRGB eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest11Sethsbcolor() throws Exception {
        String cmd = "0.123 0.456 0.789 sethsbcolor currentcolor 0.4292 div dup"
            + " 1 lt {1 exch div} if 1.001 le  3 1 roll 0.6947 div dup 1 lt"
            + " {1 exch div} if 1.001 le  3 1 roll 0.789 div dup 1 lt {1 exch"
            + " div} if 1.001 le";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest12Setrgbcolor() throws Exception {
        String cmd = "0.123 0.456 0.789 setrgbcolor currentcolorspace 0 get"
            + " /DeviceRGB eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest13Setrgbcolor() throws Exception {
        String cmd = "0.123 0.456 0.789 setrgbcolor currentcolor 0.789 div dup"
            + " 1 lt {1 exch div} if 1.001 le  3 1 roll 0.456 div dup 1 lt {1"
            + " exch div} if 1.001 le  3 1 roll 0.123 div dup 1 lt {1 exch div}"
            + " if 1.001 le";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest14Setcmykcolor() throws Exception {
        String cmd = "0.12 0.34 0.56 0.78 setcmykcolor currentcolorspace 0 get"
            + " /DeviceCMYK eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest15Setcmykcolor() throws Exception {
        String cmd = "0.12 0.34 0.56 0.78 setcmykcolor currentcolor 0.78 div"
            + " dup 1 lt {1 exch div} if 1.001 le  4 1 roll 0.56 div dup 1 lt"
            + " {1 exch div} if 1.001 le  4 1 roll 0.34 div dup 1 lt"
            + " {1 exch div} if 1.001 le  4 1 roll 0.12 div dup 1 lt"
            + " {1 exch div} if 1.001 le";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest16Currentgray() throws Exception {
        String cmd = "0.25 setgray currentgray 0.25 sub abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest17Currentgray() throws Exception {
        String cmd = "0.25 1 0.5 setrgbcolor currentgray 0.72 sub abs 0.0001"
            + " lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest18Currentgray() throws Exception {
        String cmd = "0.1 0.2 0.3 0.4 setcmykcolor currentgray 0.419 sub abs"
            + " 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest19Currenthsbcolor() throws Exception {
        String cmd = "0.25 setgray currenthsbcolor 0.25 sub abs 0.0001 lt"
            + " 3 1 roll abs 0.0001 lt  3 1 roll abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest20Currenthsbcolor() throws Exception {
        String cmd = "0.25 1 0.5 setrgbcolor currenthsbcolor  1.0 div dup 1 lt"
            + " {1 exch div} if 1.001 le  3 1 roll 0.75 div dup 1 lt"
            + " {1 exch div} if 1.001 le  3 1 roll 0.3889 div dup 1 lt"
            + " {1 exch div} if 1.001 le";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest21Currenthsbcolor() throws Exception {
        String cmd = "0.1 0.2 0.3 0.4 setcmykcolor currenthsbcolor 0.5 sub abs"
            + " 0.0001 lt  3 1 roll 0.4 sub abs 0.0001 lt  3 1 roll 0.08333 sub"
            + " abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest22Currentrgbcolor() throws Exception {
        String cmd = "0.25 setgray currentrgbcolor  0.25 sub abs 0.0001 lt"
            + " 3 1 roll 0.25 sub abs 0.0001 lt"
            + " 3 1 roll 0.25 sub abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest23Currentrgbcolor() throws Exception {
        String cmd = "0.25 1 0.5 setrgbcolor currentrgbcolor  0.5 div dup 1 lt"
            + " {1 exch div} if 1.001 le  3 1 roll 1.0 div dup 1 lt"
            + " {1 exch div} if 1.001 le  3 1 roll 0.25 div dup 1 lt"
            + " {1 exch div} if 1.001 le";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest24Currentrgbcolor() throws Exception {
        String cmd = "0.1 0.2 0.3 0.4 setcmykcolor currentrgbcolor"
            + " 0.3 sub abs 1e-4 lt"
            + " 3 1 roll 0.4 sub abs 1e-4 lt"
            + " 3 1 roll 0.5 sub abs 1e-4 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest25Currentcmykcolor() throws Exception {
        String cmd = "0.25 setgray currentcmykcolor  0.75 sub abs 0.0001 lt"
            + " 4 1 roll abs 0.0001 lt  4 1 roll abs 0.0001 lt"
            + " 4 1 roll abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest26Currentcmykcolor() throws Exception {
        String cmd = "0.25 1 0.5 setrgbcolor currentcmykcolor"
            + " 0 sub abs 1e-4 lt"
            + " 4 1 roll 0.5 sub abs 1e-4 lt"
            + " 4 1 roll 0 sub abs 1e-4 lt"
            + " 4 1 roll 0.75 sub abs 1e-4 lt"
            + " 0.1 0.2 0.3 setrgbcolor currentcmykcolor"
            + " 0.7 sub abs 1e-4 lt"
            + " 4 1 roll 0.0 sub abs 1e-4 lt"
            + " 4 1 roll 0.33333 sub abs 1e-4 lt"
            + " 4 1 roll 0.66666 sub abs 1e-4 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest27Currentcmykcolor() throws Exception {
        String cmd = "0.1 0.2 0.3 0.4 setcmykcolor currentcmykcolor 0.4 sub abs"
            + " 0.0001 lt  4 1 roll 0.3 sub abs 0.0001 lt  4 1 roll 0.2 sub abs"
            + " 0.0001 lt  4 1 roll 0.1 sub abs 0.0001 lt";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest28Currentlinewidth() throws Exception {
        String cmd = "currentlinewidth 1 eq"
            + " 2 setlinewidth 0.5 0.5 scale currentlinewidth 2 eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void graphicsTest29() throws Exception {
        String cmd = "true setstrokeadjust currentstrokeadjust"
            + " false setstrokeadjust currentstrokeadjust false eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    

}
