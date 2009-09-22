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

package net.sf.eps2pgf.testsuite.figures;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.eps2pgf.Converter;
import net.sf.eps2pgf.Options;

/**
 * Set of test figures.
 * 
 * @author Wagenaars
 *
 */
public final class AllFigures {
    
    /** The PostScript interpreter. */
    private Converter conv;
    
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
        // Create a converter with default options.
        Options opts = new Options();
        opts.parse(new String[0]);
        conv = new Converter(opts);
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void alphabet() throws Exception {
        assertTrue(Common.testFigure(conv, "alphabet.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void autocadColumbia() throws Exception {
        assertTrue(Common.testFigure(conv, "autocad_columbia.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void autocadNozzle() throws Exception {
        assertTrue(Common.testFigure(conv, "autocad_nozzle.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void colorcir() throws Exception {
        assertTrue(Common.testFigure(conv, "colorcir.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void coreldrawGraphic1() throws Exception {
        Options opts = conv.getOpts();
        opts.setTextmode(Options.TextMode.DIRECT_COPY);
        assertTrue(Common.testFigure(conv, "coreldraw_Graphic1.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void creohnSheepInGray() throws Exception {
        assertTrue(Common.testFigure(conv, "creohn_Sheep_in_gray.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void dvips1() throws Exception {
        assertTrue(Common.testFigure(conv, "dvips1.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void fig2dev1() throws Exception {
        assertTrue(Common.testFigure(conv, "fig2dev1.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void golfer() throws Exception {
        assertTrue(Common.testFigure(conv, "golfer.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void grow() throws Exception {
        assertTrue(Common.testFigure(conv, "grow.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void illustratorVw() throws Exception {
        assertTrue(Common.testFigure(conv, "illustrator_vw.ps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void ipeFigure1() throws Exception {
        assertTrue(Common.testFigure(conv, "ipe-figure1.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void mapleCubic() throws Exception {
        assertTrue(Common.testFigure(conv, "maple_cubic.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void mathematica2() throws Exception {
        assertTrue(Common.testFigure(conv, "mathematica2.eps"));
    }
    
    /** Test. @throws Exception the exception */
    @Test
    public void mathPSfragExAutoPsfrag() throws Exception {
        Options opts = conv.getOpts();
        opts.setTextreplacefile(new File("%figureDir%",
                "MathPSfrag_ex_auto-psfrag.tex"));
        assertTrue(Common.testFigure(conv, "MathPSfrag_ex_auto-psfrag.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void rectTest() throws Exception {
        assertTrue(Common.testFigure(conv, "rect_test.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void rproject1() throws Exception {
        assertTrue(Common.testFigure(conv, "rproject1.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void showtextMathematica1() throws Exception {
        assertTrue(Common.testFigure(conv, "showtext_mathematica1.ps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void tiger() throws Exception {
        assertTrue(Common.testFigure(conv, "tiger.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void matlabBitmap1() throws Exception {
        assertTrue(Common.testFigure(conv, "matlab_bitmap1.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void matplotlib1() throws Exception {
        assertTrue(Common.testFigure(conv, "matplotlib1.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void ciebasedColorspace() throws Exception {
        assertTrue(Common.testFigure(conv, "CIEBased.eps"));
    }

    /** Test. @throws Exception the exception */
    @Test
    public void quartz1() throws Exception {
        assertTrue(Common.testFigure(conv, "quartz1.eps"));
    }

}
