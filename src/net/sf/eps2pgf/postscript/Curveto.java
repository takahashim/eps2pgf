/*
 * Curveto.java
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

package net.sf.eps2pgf.postscript;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * cubic Bezier curve path section
 *
 * @author Paul Wagenaars
 */
public class Curveto extends PathSection implements Cloneable {
    
    /** Creates a new instance of Curveto */
    public Curveto() {
        for (int i = 0 ; i < params.length ; i++) {
            params[i] = Double.NaN;
        }
    }
    
    /**
     * Creates a new instance of Curveto
     * @param controlCoor1 First Bezier control point
     * @param controlCoor2 Second Bezier control point
     * @param endCoor Endpoint
     */
    public Curveto(double[] controlCoor1, double[] controlCoor2, 
            double[] endCoor) {
        params[0] = controlCoor1[0];
        params[1] = controlCoor1[1];
        params[2] = controlCoor2[0];
        params[3] = controlCoor2[1];
        params[4] = endCoor[0];
        params[5] = endCoor[1];
    }
    
    /**
     * Get position in device space coordinates.
     * @return X- and Y-coordinate in device space. Returns {NaN, NaN} when
     *         this section has no coordinate.
     */
    public double[] deviceCoor() {
        double[] coor = new double[2];
        coor[0] = params[4];
        coor[1] = params[5];
        return coor;
    }
    
    /**
     * Create a clone of this object.
     * @return Returns clone of this object.
     */
    public Curveto clone() {
        Curveto newSection = new Curveto();
        newSection.params = params.clone();
        return newSection;
    }
    
    /**
     * Append a flattened version of this curve to a path
     * @param maxError Maximum distance between flattened path and real curve.
     *                 Expressed in terms of device coordinates.
     */
    public void flatten(Path path, double[] currentPoint, double maxError) throws PSError {
        double x0 = currentPoint[0];
        double y0 = currentPoint[1];
        
        // Calculate ax, ay, ... parameters (see PostScript manual p.565)
        double cx = 3*(params[0]-x0);
        double cy = 3*(params[1]-y0);
        double bx = 3*(params[2]-params[0]) - cx;
        double by = 3*(params[3]-params[1]) - cy;
        double ax = params[4] - x0 - cx - bx;
        double ay = params[5] - y0 - cy - by;
        
        // Create high resolution version of curve
        int N = 10000;
        double[] x = new double[N];
        double[] y = new double[N];
        double step = 1 / ( (double)N - 1 );
        for (int i = 0 ; i < N ; i++) {
            double t = (double)i * step;
            x[i] = ax*Math.pow(t,3) + bx*Math.pow(t,2) + cx*t + x0;
            y[i] = ay*Math.pow(t,3) + by*Math.pow(t,2) + cy*t + y0;
        }
        
        int lastPlotted = 0;
        int upper = N-1;
        int lower = 2;
        while (lower < (N-1)) {
            int current = (lower + upper + 1) / 2;
            
            // Calculate distance
            // See: http://astronomy.swin.edu.au/~pbourke/geometry/pointline/
            // See also: http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
            double x1 = x[current];
            double y1 = y[current];
            double x2 = x[lastPlotted];
            double y2 = y[lastPlotted];
            double dp2 = Math.pow(x1-x2,2) + Math.pow(y1-y2,2);
            
            // Now loop through all points between and calculate the distance of
            // each point to the line p1-p2.
            double maxSoFar = 0.0;
            for (int j = lastPlotted+1 ; j < current ; j++ ) {
                double x3 = x[j];
                double y3 = y[j];
                double num = (x3-x1)*(x2-x1) + (y3-y1)*(y2-y1);
                double u = num / dp2;
                
                double xm = x1 + u*(x2-x1);
                double ym = y1 + u*(y2-y1);
                double d = Math.sqrt( Math.pow(xm-x3,2) + Math.pow(ym-y3,2) );
                maxSoFar = Math.max(maxSoFar, d);
            }
            
            if (maxSoFar > maxError) {
                upper = current;
            } else {
                lower = current;
            }
            
            if ( ((lower+1) == upper) && (maxSoFar > maxError) ) {
                lastPlotted = lower;
                path.lineto(x[lastPlotted], y[lastPlotted]);
                lower++;
                upper = N-1;
            }
        }
        path.lineto(x[N-1], y[N-1]);
    }
    
}
