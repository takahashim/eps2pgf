/*
 * DictPSTest.java
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
public class DictPSTest {
    
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
    public void dictTest1dict() throws Exception {
        String cmd = "10 dict type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest2Lessless() throws Exception {
        String cmd = "<< 1 2 3 counttomark 3 eq"
            + " 5 1 roll pop pop pop pop";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest3Greatergreater() throws Exception {
        String cmd = "mark /test {1 add} >> type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest4Length() throws Exception {
        String cmd = "10 dict length 0 eq  << /a 1/b 2 /c{2 add}>> length 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest5Maxlength() throws Exception {
        String cmd = "11 dict maxlength 11 eq  << /a 1 /b 2 >> maxlength 2 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest6Begin() throws Exception {
        String cmd = "10 dict dup begin currentdict eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest7End() throws Exception {
        String cmd = "10 dict begin currentdict end currentdict dup 3 1 roll ne"
            + " exch userdict eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest8Def() throws Exception {
        String cmd = "/str (abcdefghi) def (123) str cvs (123) eq"
            + " str (123defghi) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest9Def() throws Exception {
        String cmd = "/str1 (1 2 add) def str1 (1 2 add) eq"
            + " /str2 (1 2 add) cvx def str2 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest10Def() throws Exception {
        String cmd = "(abc) dup /foo exch def cvx pop foo xcheck false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest11Load() throws Exception {
        String cmd = "<< /a (abc) /b 123 >> begin  /a load (abc) eq"
            + " /b load 123 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest12Store() throws Exception {
        String cmd = "/a 123 store a 123 eq  /a (abc) store a (abc) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest13Get() throws Exception {
        String cmd = "<</a 1.0 /b 2 >> /b get 2 eq"
            + " /foo (abc) def currentdict /foo get (abc) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest14Put() throws Exception {
        String cmd = "10 dict dup /foo (bar) put  begin foo (bar) eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest15Undef() throws Exception {
        String cmd = "10 dict dup /foo (bar) put dup /foo undef begin {foo}"
            + " stopped";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest16Known() throws Exception {
        String cmd = "/mydict 5 dict def mydict /total 0 put mydict /total"
            + " known  mydict /badname known false eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest17Where() throws Exception {
        String cmd = "<</a 123 /b 456>> dup begin <</d (abc) /e (def)>> begin"
            + " /b where 3 1 roll eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest18Copy() throws Exception {
        String cmd = "<</a 123>> <</b (abc) /c (def)>> dup 3 1 roll copy eq"
            + "<</a 123>> <</b (abc) /c (def)>> copy /a known";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest19Forall() throws Exception {
        String cmd = "<</a 123 /b 123>> {} forall  123 eq  4 1 roll dup /b eq"
            + " exch /a eq or  4 1 roll 123 eq  4 1 roll dup/a eq exch/b eq or";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest20Forall() throws Exception {
        String cmd = "0 <</a 1 /b 2 /c 3>> {exch pop add} forall 6 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest21Currentdict() throws Exception {
        String cmd = "currentdict userdict eq"
            + " 10 dict begin currentdict userdict ne";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest22Errordict() throws Exception {
        String cmd = "errordict type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest23Error() throws Exception {
        String cmd = "$error type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest24Systemdict() throws Exception {
        String cmd = "systemdict type /dicttype eq"
            + " {systemdict /foo (bar) put} stopped";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest25Globaldict() throws Exception {
        String cmd = "globaldict type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest26Statusdict() throws Exception {
        String cmd = "statusdict type /dicttype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest27Countdictstack() throws Exception {
        String cmd = "countdictstack 3 eq  10 dict begin countdictstack 4 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest28Dictstack() throws Exception {
        String cmd = "10 array dictstack dup 0 get systemdict"
            + " eq exch length 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest29Cleardictstack() throws Exception {
        String cmd = "10 dict begin countdictstack 4 eq"
            + " cleardictstack countdictstack 3 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest30() throws Exception {
        String cmd = "<< 1 (abc) >> {} forall pop 1 add 2 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest31() throws Exception {
        String cmd = "1 dict dup /a 99 put (a) get 99 eq"
            + " 1 dict dup 1 999 put 1.0 get 999 eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest32() throws Exception {
        String cmd = "10 dict dup (abc) 123 put {} forall pop type/nametype eq";
        assertTrue(Common.testString(interp, cmd));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void dictTest33Copy() throws Exception {
        String cmd = "<</a 123>> <</b (abc) /c (def)>> dup 3 1 roll copy eq";
        assertTrue(Common.testString(interp, cmd));
    }
}
