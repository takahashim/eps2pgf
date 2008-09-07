/*
 * ControlPSTest.java
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
public class CoordinatesPSTest {
    
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
    public void coordinatesTest1Matrix() throws Exception {
        String cmd = "matrix {} forall  0 eq  6 1 roll 0 eq  6 1 roll 1 eq"
            + " 6 1 roll 0 eq  6 1 roll 0 eq  6 1 roll 1 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest2Matrix() throws Exception {
        String cmd = "matrix dup eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest3Initmatrix() throws Exception {
        String cmd = "/m1 matrix currentmatrix def /m2 2 2 scale initmatrix"
            + " matrix currentmatrix def  m1 0 get m2 0 get eq  m1 1 get m2 1"
            + " get eq  m1 2 get m2 2 get eq  m1 3 get m2 3 get eq  m1 4 get m2"
            + " 4 get eq  m1 5 get m2 5 get eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest4Identmatrix() throws Exception {
        String cmd = "/m matrix identmatrix def m 0 get 1 eq  m 1 get 0 eq  m 2"
            + " get 0 eq  m 3 get 1 eq  m 4 get 0 eq  m 5 get 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest5Identmatrix() throws Exception {
        String cmd = "matrix dup identmatrix pop [1 0 0 1 0 0] /m2 exch def /m1"
            + " exch def 0 1 m1 length 1 sub {dup m1 exch get 1e-40 add exch m2"
            + " exch get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le"
            + " exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest6Identmatrix() throws Exception {
        String cmd = "6 array dup  identmatrix pop [1 0 0 1 0 0] /m2 exch def"
            + " /m1 exch def 0 1 m1 length 1 sub {dup m1 exch get 1e-40 add"
            + " exch m2 exch get 1e-40 add div dup 1 lt {1 exch div} if dup"
            + " 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest7Defaultmatrix() throws Exception {
        String cmd = "/m1 matrix currentmatrix def /m2 matrix defaultmatrix def"
            + " m1 0 get m2 0 get eq  m1 1 get m2 1 get eq  m1 2 get m2 2 get"
            + " eq  m1 3 get m2 3 get eq  m1 4 get m2 4 get eq  m1 5 get m2 5"
            + " get eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest8Currentmatrix() throws Exception {
        String cmd = "matrix currentmatrix length 6 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest9Setmatrix() throws Exception {
        String cmd = "[1 2 3 4 5 6] setmatrix /m 6 array currentmatrix def  m 0"
            + " get 1 eq  m 1 get 2 eq  m 2 get 3 eq  m 3 get 4 eq  m 4 get 5"
            + " eq  m 5 get 6 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest10Translate() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 12.34 56.78 translate"
            + " matrix currentmatrix [1.2 3.4 5.6 7.8 341.88 487.14] /m2 exch"
            + " def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2 exch"
            + " get 1e-40 add div dup 1 lt {1 exch div} if dup 1.0001 le exch 0"
            + " gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest11Translate() throws Exception {
        String cmd = "12.34 56.78 matrix translate [1 0 0 1 12.34 56.78] /m2"
            + " exch def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2"
            + " exch get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le"
            + " exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest12Scale() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 12.34 56.78 scale"
            + " matrix currentmatrix [14.81 41.96 318.0 442.9 9.1 2.3] /m2 exch"
            + " def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2 exch"
            + " get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0"
            + " gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest13Scale() throws Exception {
        String cmd = "12.34 56.78 matrix scale [12.34 0 0 56.78 0 0] /m2 exch"
            + " def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2 exch"
            + " get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0"
            + " gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest14Rotate() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix -56.78 rotate matrix"
            + " currentmatrix [-4.027 -4.663 4.072 7.118 9.1 2.3] /m2 exch def"
            + " /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2 exch get"
            + " 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt}"
            + " for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest15Rotate() throws Exception {
        String cmd = "-56.78 matrix rotate [0.548 -0.837 0.837 0.548 0 0] /m2"
            + " exch def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2"
            + " exch get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le"
            + " exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest16Concat() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix [5.6 7.8 9.0 1.2 3.4"
            + " 5.6] concat matrix currentmatrix [50.4 79.88 17.52 39.96 44.54"
            + " 57.54] /m2 exch def /m1 exch def 0 1 5 {dup m1 exch get 1e-40"
            + " add exch m2 exch get 1e-40 add div dup 1 lt {1 exch div} if dup"
            + " 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest17Concatmatrix() throws Exception {
        String cmd = "[5.6 7.8 9.0 1.2 3.4 5.6] [1.2 3.4 5.6 7.8 9.1 2.3]"
            + " matrix concatmatrix [50.4 79.88 17.52 39.96 44.54 57.54] /m2"
            + " exch def /m1 exch def 0 1 5 {dup m1 exch get 1e-40 add exch m2"
            + " exch get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le"
            + " exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest18Transform() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 56.78 23.96 transform"
            + " 2 array astore [211.41 382.2] /m2 exch def /m1 exch def 0 1 m1"
            + " length 1 sub {dup m1 exch get 1e-40 add exch m2 exch get 1e-40"
            + " add div dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest19Transform() throws Exception {
        String cmd = "56.78 23.96 [1.2 3.4 5.6 7.8 9.1 2.3] transform 2 array"
            + " astore [211.41 382.2] /m2 exch def /m1 exch def 0 1 m1 length 1"
            + " sub {dup m1 exch get 1e-40 add exch m2 exch get 1e-40 add div"
            + " dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest20Dtransform() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 56.78 23.96"
            + " dtransform 2 array astore [202.31 379.94] /m2 exch def /m1 exch"
            + " def 0 1 m1 length 1 sub {dup m1 exch get 1e-40 add exch m2 exch"
            + " get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0"
            + " gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest21Dtransform() throws Exception {
        String cmd = "56.78 23.96 [1.2 3.4 5.6 7.8 9.1 2.3] dtransform 2 array"
            + " astore [202.31 379.94] /m2 exch def /m1 exch def 0 1 m1 length"
            + " 1 sub {dup m1 exch get 1e-40 add exch m2 exch get 1e-40 add div"
            + " dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest22Itransform() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 56.78 23.96"
            + " itransform 2 array astore [-25.889 14.062] /m2 exch def/m1 exch"
            + " def 0 1 m1 length 1 sub {dup m1 exch get 1e-40 add exch m2 exch"
            + " get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0"
            + " gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest23Itransform() throws Exception {
        String cmd = "56.78 23.96 [1.2 3.4 5.6 7.8 9.1 2.3] itransform 2 array"
            + " astore [-25.889 14.062] /m2 exch def /m1 exch def 0 1 m1 length"
            + " 1 sub {dup m1 exch get 1e-40 add exch m2 exch get 1e-40 add div"
            + " dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest24Idtransform() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] setmatrix 56.78 23.96"
            + " idtransform 2 array astore [-31.891 16.973] /m2 exch def /m1"
            + " exch def 0 1 m1 length 1 sub {dup m1 exch get 1e-40 add exch m2"
            + " exch get 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le"
            + " exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest25Idtransform() throws Exception {
        String cmd = "56.78 23.96 [1.2 3.4 5.6 7.8 9.1 2.3] idtransform 2 array"
            + " astore [-31.891 16.973] /m2 exch def /m1 exch def 0 1 m1 length"
            + " 1 sub {dup m1 exch get 1e-40 add exch m2 exch get 1e-40 add div"
            + " dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt} for";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void coordinatesTest26Invertmatrix() throws Exception {
        String cmd = "[1.2 3.4 5.6 7.8 9.1 2.3] matrix invertmatrix [-0.8058"
            + " 0.3512 0.5785 -0.1240 6.002 -2.911] /m2 exch def /m1 exch def 0"
            + " 1 m1 length 1 sub {dup m1 exch get 1e-40 add exch m2 exch get"
            + " 1e-40 add div dup 1 lt {1 exch div} if dup 1.001 le exch 0 gt}"
            + " for";
        assertTrue(Common.testString(interp, cmd));
    }


}
