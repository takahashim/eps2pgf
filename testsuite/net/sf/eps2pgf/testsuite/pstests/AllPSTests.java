/*
 * AllPSTests.java
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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all synthetic PostScript tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ParserPSTest.class,
    BoolPSTest.class,
    TypePSTest.class,
    StackPSTest.class,
    ArrayPSTest.class,
    StringPSTest.class,
    DictPSTest.class,
    ControlPSTest.class,
    PathPSTest.class,
    CoordinatesPSTest.class,
    GraphicsPSTest.class,
    OutputPSTest.class,
    FilePSTest.class
})

public class AllPSTests {

}