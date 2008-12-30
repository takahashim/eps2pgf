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

package net.sf.eps2pgf.util;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * The Class Eps2pgfHandler.
 * Handler for the logging for messages. It writes warnings and errors to the
 * standard error and other messages to the standard output.
 */
public class Eps2pgfHandler extends Handler {

    /**
     * Close the Handler and free all associated resources.
     */
    @Override
    public void close() {
        /* empty block */
    }

    /**
     * Flush any buffered output.
     */
    @Override
    public void flush() {
        System.out.flush();
        System.err.flush();
    }

    /**
     * Format and publish a <code>LogRecord</code>.
     * 
     * @param record Record to format and publish.
     */
    @Override
    public void publish(final LogRecord record) {
        Formatter formatter = getFormatter();
        String message = formatter.format(record);
        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
            System.err.print(message);
        } else {
            System.out.print(message);
        }
    }

}
