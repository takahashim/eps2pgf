/*
 * ColorConvert.java
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

/**
 * Contains method to convert colors from one color space to the other.
 * @author Paul Wagenaars
 */
public class ColorConvert {
    
    /**
     * Convert a color in CMYK to grayscale
     * @param c Cyan (ranging from 0 to 1)
     * @param m Magenta (ranging from 0 to 1)
     * @param y Yellow (ranging from 0 to 1)
     * @param k Black (ranging from 0 to 1)
     * @return Gray value (ranging from 0 to 1)
     */
    public static double CMYKtoGray(double c, double m, double y, double k) {
        return (1.0 - Math.min(1.0, 0.3*c + 0.59*m + 0.11*y + k));
    }

    /**
     * Convert a color in CMYK to RGB
     * @param c Cyan (ranging from 0 to 1)
     * @param m Magenta (ranging from 0 to 1)
     * @param y Yellow (ranging from 0 to 1)
     * @param k Black (ranging from 0 to 1)
     * @return Array with HSB values
     */
    public static double[] CMYKtoHSB(double[] cmyk) {
        double[] rgb = CMYKtoRGB(cmyk);
        return RGBtoHSB(rgb);
    }

    /**
     * Convert a color in CMYK to RGB
     * @param c Cyan (ranging from 0 to 1)
     * @param m Magenta (ranging from 0 to 1)
     * @param y Yellow (ranging from 0 to 1)
     * @param k Black (ranging from 0 to 1)
     * @return Array with RGB values
     */
    public static double[] CMYKtoRGB(double c, double m, double y, double k) {
        double r = 1.0 - Math.min(1.0, c + k);
        double g = 1.0 - Math.min(1.0, m + k);
        double b = 1.0 - Math.min(1.0, y + k);
        double[] rgb = {r, g, b};
        return rgb;
    }
    
    /**
     * Convert a color in CMYK to RGB
     * @param cmyk Array with CMYK values
     * @return Array with RGB values
     */
    public static double[] CMYKtoRGB(double[] cmyk) {
        return CMYKtoRGB(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
    }
    
    /**
     * Convert a color in CMYK to grayscale
     * @param cmyk Array with CMYK values {c, m, y, k}
     * @return Gray value (ranging from 0 to 1)
     */
    public static double CMYKtoGray(double[] cmyk) {
        return CMYKtoGray(cmyk[0], cmyk[1], cmyk[2], cmyk[3]);
    }
    
    /**
     * Convert a color in grayscale to the HSB color space
     * @param gray Gray value
     * @return Array with HSB values
     */
    public static double[] grayToHSB(double gray) {
        double[] rgb = grayToRGB(gray);
        return RGBtoHSB(rgb);
    }
    
    /**
     * Convert a color in grayscale to the RGB color space
     * @param gray Gray value
     * @return Array with RGB values
     */
    public static double[] grayToRGB(double gray) {
        double[] rgb = {gray, gray, gray};
        return rgb;
    }
    
    /**
     * Convert a color specified in HSB to RGB
     * @param h Hue (ranging from 0 to 1)
     * @param s Saturation (ranging from 0 to 1)
     * @param b Brightness (ranging from 0 to 1)
     * @return Same color in RGB color space. Array with R, G, and B value.
     */
    public static double[] HSBtoRGB(double h, double s, double b) {
        // See http://en.wikipedia.org/wiki/HSV_color_space
        h = h % 1.0;
        double Hi = Math.floor(h*6);
        double f = h*6 - Hi;
        double p = b * (1 - s);
        double q = b * (1 - f*s);
        double t = b * (1 - (1 - f)*s);
        
        double[] rgb = new double[3];
        switch ((int)Hi) {
            case 0:
                rgb[0] = b;  rgb[1] = t;  rgb[2] = p;
                break;
            case 1:
                rgb[0] = q;  rgb[1] = b;  rgb[2] = p;
                break;
            case 2:
                rgb[0] = p;  rgb[1] = b;  rgb[2] = t;
                break;
            case 3:
                rgb[0] = p;  rgb[1] = q;  rgb[2] = b;
                break;
            case 4:
                rgb[0] = t;  rgb[1] = p;  rgb[2] = b;
                break;
            case 5:
                rgb[0] = b;  rgb[1] = p;  rgb[2] = q;
                break;
                
        }
        return rgb;
    }
    
    /**
     * Convert a color in RGB to grayscale
     * @param r Red (ranging from 0 to 1)
     * @param g Green (ranging from 0 to 1)
     * @param b Blue (ranging from 0 to 1)
     * @return Gray value (ranging from 0 to 1)
     */
    public static double RGBtoGray(double r, double g, double b) {
        return (0.3*r + 0.59*g + 0.11*b);
    }
    
    /**
     * Convert a color in RGB to grayscale
     * @param rgb Array with r, g and b values
     * @return Gray value (ranging from 0 to 1)
     */
    public static double RGBtoGray(double[] rgb) {
        return RGBtoGray(rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * Convert a color in RGB to HSB
     * @param r Red (ranging from 0 to 1)
     * @param g Green (ranging from 0 to 1)
     * @param b Blue (ranging from 0 to 1)
     * @return Array with HSB values: {H, S, B}
     */
    public static double[] RGBtoHSB(double r, double g, double b) {
        // See http://en.wikipedia.org/wiki/HSV_color_space
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double h, s, v;
        
        if (max == min) {
            h = 0.0;
        } else if ( (max == r) && (g >= b) ) {
            h = 1.0/6.0 * (g-b) / (max-min);
        } else if ( (max == r) && (g < b) ) {
            h = 1.0/6.0 * (g-b) / (max-min) + 1.0;
        } else if (max == g) {
            h = 1.0/6.0 * (b-r) / (max-min) + 1.0/3.0;
        } else {
            h = 1.0/6.0 * (r-g) / (max-min) + 2.0/3.0;
        }
        
        if (max == 0.0) {
            s = 0.0;
        } else {
            s = 1 - min/max;
        }
        
        v = max;
        
        double[] hsb = {h, s, v};
        return hsb;
    }
    
    /**
     * Convert a color in RGB to HSB
     * @param rgb Array with RGB value: {R, G, B}
     * @return Array with HSB values: {H, S, B}
     */
    public static double[] RGBtoHSB(double[] rgb) {
        return RGBtoHSB(rgb[0], rgb[1], rgb[2]);
    }

}
