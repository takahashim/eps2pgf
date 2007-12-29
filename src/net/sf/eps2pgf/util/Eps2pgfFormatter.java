/*
 * Eps2pgfFormatter.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.util;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Wagenaars
 *
 */
public class Eps2pgfFormatter extends Formatter {

    /**
     * Format the given log record and return the formatted string.
     * 
     * @param record The log record to be formatted.
     * 
     * @return The formatted log record.
     */
    public String format(final LogRecord record) {
        StringBuilder str = new StringBuilder();
        if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
            str.append("ERROR: ");
        } else if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            str.append("WARNING: ");
        }
        str.append(record.getMessage());
        
        if (str.charAt(str.length() - 1) != '\n') {
            str.append("\n");
        }
        return str.toString();
    }

}
