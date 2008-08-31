/*
 * ResourcePSTest.java
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
public class ResourcePSTest {
    
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
    public void resourceTest1_Resourcestatus() throws Exception {
        String cmd = "999 /FontType resourcestatus false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest2_Resourcestatus() throws Exception {
        String cmd = "1 /FontType resourcestatus 3 1 roll 0 eq 3 1 roll 0 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest3_Resourcestatus() throws Exception {
        String cmd = "/ASCII85Decode /Filter resourcestatus"
            + " 3 1 roll 0 eq 3 1 roll 0 eq"
            + " /DoesntExist /Filter resourcestatus false eq";
        assertTrue(Common.testString(interp, cmd));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest4_Resourcestatus() throws Exception {
        String cmd = "/DeviceGray /ColorSpaceFamily resourcestatus"
            + " 3 1 roll pop pop"
            + " /DeviceRGB /ColorSpaceFamily resourcestatus 3 1 roll pop pop"
            + " /DeviceCMYK /ColorSpaceFamily resourcestatus 3 1 roll pop pop"
            + " /DoesntExist /ColorSpaceFamily resourcestatus false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest5_Defineresource() throws Exception {
        String cmd = "/TheKey [/DeviceGray] /ColorSpace defineresource"
            + " pop true";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest6_Findresource() throws Exception {
        String cmd = "/test [ /DeviceGray ] /ColorSpace defineresource"
            + " pop /test /ColorSpace findresource 0 get /DeviceGray eq"
            + " /test /ColorSpace undefineresource";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void resourceTest7() throws Exception {
        // Example 3.7 from PostScript reference
        // plus some extra test by my.
        String cmd = "currentglobal"
            + " true setglobal"
            + " /Generic /Category findresource"
            + " dup length 1 add dict copy"
            + " dup /InstanceType /dicttype put"
            + " /Widget exch /Category defineresource pop"
            + " setglobal"
            
            + "/testkey 10 dict /Widget defineresource pop"
            + "/testkey /Widget resourcestatus 3 1 roll pop pop";
        assertTrue(Common.testString(interp, cmd));
    }

}
