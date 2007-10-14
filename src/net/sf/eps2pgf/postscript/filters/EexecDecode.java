/*
 * EexecDecode.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
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

package net.sf.eps2pgf.postscript.filters;

import java.io.InputStream;
import java.io.IOException;

/**
 * Applies eexec decryption to an InputStream. See "Adobe Type 1 Font Format"
 * for for information on this encryption.
 * @author Paul Wagenaars
 */
public class EexecDecode extends InputStream {
    /**
     * <code>InputStream</code> from which encrypted data is read.
     */
    private InputStream in;
    
    /**
     * Number of random bytes at start of encrypted data
     */
    private int n;
    
    /*
     * Random variables used for decryption
     */
    private int R;
    private int c1;
    private int c2;
    
    /**
     * Value of R at the time of the last <code>mark()</code>
     */
    private int markedR;
    
    
    /**
     * Wrap a eexec decryption layer around and input stream
     * @param in Stream from which encrypted data will be read
     */
    public EexecDecode (InputStream in) {
        this.in = new HexDecode(in);
        n = 4;
        R = 55665;
        c1 = 52845;
        c2 = 22719;
    }
    
    /**
     * Wrap a eexec decryption layer around and input stream
     * @param in Stream from which encrypted data will be read
     */
    public EexecDecode (InputStream in, int password, boolean binaryInput) {
        if (binaryInput) {
            this.in = in;
        } else {
            this.in = new HexDecode(in);
        }
        n = 4;
        R = password;
        c1 = 52845;
        c2 = 22719;
    }
    
    public int available() throws IOException {
        return (in.available() - n);
    }
    
    public void close() throws IOException {
        in = null;
    }
    
    public void mark(int readlimit) {
        in.mark(readlimit + n);
        markedR = R;
    }
    
    public boolean markSupported() {
        return in.markSupported();
    }
    
    public int read() throws IOException {
        if (in == null) {
            return -1;
        }

        int c = in.read();
        if (c == -1) {
            return -1;
        } else if (c > 255) {
            throw new IOException();
        }
        
        int T = R >>> 8;
        int P = (c ^ T);
        R = ((c + R)*c1 + c2) & 0x0000FFFF;
        
        if (n > 0) {
            n--;
            P = this.read();
        } //else {
            //System.out.print((char)P);
        //}
        
        return P;
    }
    
    public void reset() throws IOException {
        if (in == null) {
            throw new IOException();
        }
        in.reset();
        R = markedR;
    }
    
}
