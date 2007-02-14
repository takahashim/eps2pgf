/*
 * Fonts.java
 *
 * This file is part of Eps2pgf.
 *
 * Copyright (C) 2007 Paul Wagenaars <pwagenaars@fastmail.fm>
 *
 * Eps2pgf is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * Eps2pgf is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package net.sf.eps2pgf.postscript;

import java.util.*;
import java.util.logging.*;

import net.sf.eps2pgf.postscript.errors.*;

/**
 * Represents the link between fonts and LaTeX code. It describes how text with
 * a certain font is converted to LaTeX code.
 * @author Paul Wagenaars
 */
public class Fonts {
    private ArrayList<PSObjectDict> fonts = new ArrayList<PSObjectDict>();
    private int nextFID = 0;
    private PSObjectDict stdCharStrings = new PSObjectDict();
    private PSObjectDict symbolCharStrings = new PSObjectDict();
    
    Logger log = Logger.getLogger("global");
    
    /**
     * Create a new Fonts instance
     */
    public Fonts() {
        initStdCharStrings();
        initBaseFontDicts();
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     */
    public PSObjectDict findFont(PSObject fontNameObj) throws PSErrorTypeCheck {
        String fontName = fontNameObj.toDictKey();
        return this.findFont(fontName);
    }
    
    /**
     * Search a font and return it's corresponding font dictionary.
     */
    public PSObjectDict findFont(String fontName) throws PSErrorTypeCheck {
        for (int i = (fonts.size()-1) ; i >= 0 ; i--) {
            PSObjectDict dict = fonts.get(i);
            PSObject value = dict.lookup("FontName");
            if (fontName.equals(value.toDictKey())) {
                return dict;
            }
        }
        log.warning("Unable to find font "+fontName+". Substituting font Courier for "+fontName+".");
        return this.findFont("Courier");
    }
    
    /**
     * Define a new font
     */
    public PSObjectDict defineFont(PSObject key, PSObjectDict font) throws PSErrorUnimplemented {
        if (font.containsKey("FID")) {
            throw new PSErrorUnimplemented("Associating a font with more than one key.");
        } else {
            font.setKey("FID", new PSObjectInt(nextFID++));
            if (!key.equals(font.lookup("FontName"))) {
                throw new PSErrorUnimplemented("Defining a font when key and FontName are different.");
            }
            fonts.add(font);
        }
        return font;
    }
    
    private void initBaseFontDicts() {
        String fontNames[] = new String[13];
        String latexPreCode[] = new String[fontNames.length];
        String latexPostCode[] = new String[fontNames.length];
        ArrayList<PSObjectName[]> encoding = new ArrayList<PSObjectName[]>();
        PSObjectDict charStrings[] = new PSObjectDict[fontNames.length];
        
        fontNames[0] = "/Times-Roman";
        latexPreCode[0] = "\\textrm{";
        latexPostCode[0] = "}";
        encoding.add(0, Encoding.getStandardVector());
        charStrings[0] = stdCharStrings;
        
        fontNames[1] = "/Times-Italic";
        latexPreCode[1] = "\\textrm{\\textit{";
        latexPostCode[1] = "}}";
        encoding.add(1, Encoding.getStandardVector());
        charStrings[1] = stdCharStrings;
        
        fontNames[2] = "/Times-Bold";
        latexPreCode[2] = "\\textrm{\\textbf{";
        latexPostCode[2] = "}}";
        encoding.add(2, Encoding.getStandardVector());
        charStrings[2] = stdCharStrings;
        
        fontNames[3] = "/Times-BoldItalic";
        latexPreCode[3] = "\\textrm{\\textbf{\\textit{";
        latexPostCode[3] = "}}}";
        encoding.add(3, Encoding.getStandardVector());
        charStrings[3] = stdCharStrings;
        
        fontNames[4] = "/Helvetica";
        latexPreCode[4] = "\\textsf{";
        latexPostCode[4] = "}";
        encoding.add(4, Encoding.getStandardVector());
        charStrings[4] = stdCharStrings;
        
        fontNames[5] = "/Helvetica-Oblique";
        latexPreCode[5] = "\\textsf{\\textsl{";
        latexPostCode[5] = "}}";
        encoding.add(5, Encoding.getStandardVector());
        charStrings[5] = stdCharStrings;
        
        fontNames[6] = "/Helvetica-Bold";
        latexPreCode[6] = "\\textsf{\\textbf{";
        latexPostCode[6] = "}}";
        encoding.add(6, Encoding.getStandardVector());
        charStrings[6] = stdCharStrings;
        
        fontNames[7] = "/Helvetica-BoldOblique";
        latexPreCode[7] = "\\textsf{\\textbf{\\textsl{";
        latexPostCode[7] = "}}}";
        encoding.add(7, Encoding.getStandardVector());
        charStrings[7] = stdCharStrings;
        
        fontNames[8] = "/Courier";
        latexPreCode[8] = "\\texttt{";
        latexPostCode[8] = "}";
        encoding.add(8, Encoding.getStandardVector());
        charStrings[8] = stdCharStrings;
        
        fontNames[9] = "/Courier-Oblique";
        latexPreCode[9] = "\\texttt{\\textsl{";
        latexPostCode[9] = "}}";
        encoding.add(9, Encoding.getStandardVector());
        charStrings[9] = stdCharStrings;
        
        fontNames[10] = "/Courier-Bold";
        latexPreCode[10] = "\\texttt{\\textbf{";
        latexPostCode[10] = "}}";
        encoding.add(10, Encoding.getStandardVector());
        charStrings[10] = stdCharStrings;
        
        fontNames[11] = "/Courier-BoldOblique";
        latexPreCode[11] = "\\texttt{\\textbf{\\textsl{";
        latexPostCode[11] = "}}}";
        encoding.add(11, Encoding.getStandardVector());
        charStrings[11] = stdCharStrings;
        
        fontNames[12] = "/Symbol";
        latexPreCode[12] = "$";
        latexPostCode[12] = "$";
        encoding.add(12, Encoding.getSymbolVector());
        charStrings[12] = symbolCharStrings;
        
        // Create font dictionaries for all 13 base postscript fonts
        // See PostScript manual under "5.2 Font Dictionaries"
        double[] fontBbox = {0, 0, 1, 1};
        for (int i = 0 ; i < fontNames.length ; i++) {
            PSObjectDict dict = new PSObjectDict();
            dict.setKey("FontType", new PSObjectInt(1));
            dict.setKey("FontMatrix", new PSObjectMatrix(1,0,0,1,0,0));
            dict.setKey("FontName", new PSObjectName(fontNames[i]));
            dict.setKey("FID", new PSObjectInt(nextFID++));
            dict.setKey("Encoding", new PSObjectArray(encoding.get(i)));
            dict.setKey("FontBBox", new PSObjectArray(fontBbox));
            dict.setKey("PaintType", new PSObjectInt(2));
            dict.setKey("CharStrings", stdCharStrings);
            fonts.add(dict);
        }
        
    }
    
    /**
     * Initializes the stdCharStrings dictionary.
     * The keys are the character names, the values are corresponding LaTeX code
     * See PostScript manual for info on CharStrings
     */
    private void initStdCharStrings() {
        stdCharStrings.setKey(".notdef", "");
        stdCharStrings.setKey("A", "A");
        stdCharStrings.setKey("AE", "{\\AE}");
        stdCharStrings.setKey("Aacute", "\\'A");
        stdCharStrings.setKey("Acircumflex", "\\^A");
        stdCharStrings.setKey("Adieresis", "\\\"A");
        stdCharStrings.setKey("Agrave", "\\`A");
        stdCharStrings.setKey("B", "B");
        stdCharStrings.setKey("C", "C");
        stdCharStrings.setKey("D", "D");
        stdCharStrings.setKey("E", "E");
        stdCharStrings.setKey("F", "F");
        stdCharStrings.setKey("G", "G");
        stdCharStrings.setKey("H", "H");
        stdCharStrings.setKey("I", "I");
        stdCharStrings.setKey("J", "J");
        stdCharStrings.setKey("K", "K");
        stdCharStrings.setKey("L", "L");
        stdCharStrings.setKey("M", "M");
        stdCharStrings.setKey("N", "N");
        stdCharStrings.setKey("O", "O");
        stdCharStrings.setKey("P", "P");
        stdCharStrings.setKey("Q", "Q");
        stdCharStrings.setKey("R", "R");
        stdCharStrings.setKey("S", "S");
        stdCharStrings.setKey("T", "T");
        stdCharStrings.setKey("U", "U");
        stdCharStrings.setKey("V", "V");
        stdCharStrings.setKey("W", "W");
        stdCharStrings.setKey("X", "X");
        stdCharStrings.setKey("Y", "Y");
        stdCharStrings.setKey("Z", "Z");
        
        stdCharStrings.setKey("a", "a");
        stdCharStrings.setKey("ampersand", "{\\&}");
        stdCharStrings.setKey("asciicircum", "{\\textasciicircum}");
        stdCharStrings.setKey("asciitilde", "{\\textasciitilde}");
        stdCharStrings.setKey("asterisk", "*");
        stdCharStrings.setKey("at", "@");

        stdCharStrings.setKey("b", "b");
        stdCharStrings.setKey("backslash", "\\ensuremath{\\backslash}");
        stdCharStrings.setKey("bar", "|");
        stdCharStrings.setKey("braceleft", "{\\{}");
        stdCharStrings.setKey("braceright", "{\\}}");
        stdCharStrings.setKey("bracketleft", "[");
        stdCharStrings.setKey("bracketright", "]");

        stdCharStrings.setKey("c", "c");
        stdCharStrings.setKey("colon", ":");
        stdCharStrings.setKey("comma", ",");

        stdCharStrings.setKey("d", "d");
        stdCharStrings.setKey("dollar", "{\\$}");

        stdCharStrings.setKey("e", "e");
        stdCharStrings.setKey("eight", "8");
        stdCharStrings.setKey("equal", "=");
        stdCharStrings.setKey("exclam", "!");

        stdCharStrings.setKey("f", "f");
        stdCharStrings.setKey("five", "5");
        stdCharStrings.setKey("four", "4");

        stdCharStrings.setKey("g", "g");
        stdCharStrings.setKey("greater", ">");

        stdCharStrings.setKey("h", "h");

        stdCharStrings.setKey("i", "i");

        stdCharStrings.setKey("j", "j");

        stdCharStrings.setKey("k", "k");

        stdCharStrings.setKey("l", "l");
        stdCharStrings.setKey("less", "<");

        stdCharStrings.setKey("m", "m");

        stdCharStrings.setKey("n", "n");
        stdCharStrings.setKey("nine", "9");
        stdCharStrings.setKey("numbersign", "{\\#}");

        stdCharStrings.setKey("o", "o");
        stdCharStrings.setKey("one", "1");

        stdCharStrings.setKey("p", "p");
        stdCharStrings.setKey("parenleft", "(");
        stdCharStrings.setKey("parenright", ")");
        stdCharStrings.setKey("percent", "{\\%}");
        stdCharStrings.setKey("period", ".");
        stdCharStrings.setKey("plus", "+");

        stdCharStrings.setKey("q", "q");
        stdCharStrings.setKey("quotedbl", "\"");
        stdCharStrings.setKey("quoteleft", "`");
        stdCharStrings.setKey("quoteright", "'");

        stdCharStrings.setKey("r", "r");

        stdCharStrings.setKey("s", "s");
        stdCharStrings.setKey("semicolon", ";");
        stdCharStrings.setKey("seven", "7");
        stdCharStrings.setKey("six", "6");
        stdCharStrings.setKey("slash", "/");
        stdCharStrings.setKey("space", " ");

        stdCharStrings.setKey("t", "t");
        stdCharStrings.setKey("three", "3");
        stdCharStrings.setKey("tilde", "{\\textasciitilde}");
        stdCharStrings.setKey("two", "2");

        stdCharStrings.setKey("u", "u");
        stdCharStrings.setKey("underscore", "{\\_}");

        stdCharStrings.setKey("v", "v");

        stdCharStrings.setKey("x", "x");

        stdCharStrings.setKey("y", "y");

        stdCharStrings.setKey("z", "z");
        stdCharStrings.setKey("zero", "0");
    }
    

    /**
     * Initializes the symbolCharStrings dictionary.
     * The keys are the character names, the values are corresponding LaTeX code
     * See PostScript manual for info on CharStrings
     */
    private void initSymbolCharStrings() {
        symbolCharStrings.setKey("exclam", "!");
        symbolCharStrings.setKey("universal", "{\\forall}");
        symbolCharStrings.setKey("numbersign", "{\\#}");
        symbolCharStrings.setKey("existential", "{\\exists}");
        symbolCharStrings.setKey("percent", "{\\%}");
        symbolCharStrings.setKey("ampersand", "{\\&}");
        symbolCharStrings.setKey("suchthat", "{\\ni}");
        symbolCharStrings.setKey("parenleft", "(");
        symbolCharStrings.setKey("parenright", ")");
        symbolCharStrings.setKey("asterisk", "*");
        symbolCharStrings.setKey("plus", "+");
        symbolCharStrings.setKey("comma", ",");
        symbolCharStrings.setKey("minus", "-");
        symbolCharStrings.setKey("period", ".");
        symbolCharStrings.setKey("slash", "/");
        
        symbolCharStrings.setKey("zero", "0");
        symbolCharStrings.setKey("one", "1");
        symbolCharStrings.setKey("two", "2");
        symbolCharStrings.setKey("three", "3");
        symbolCharStrings.setKey("four", "4");
        symbolCharStrings.setKey("five", "5");
        symbolCharStrings.setKey("six", "6");
        symbolCharStrings.setKey("seven", "7");
        symbolCharStrings.setKey("eight", "8");
        symbolCharStrings.setKey("nine", "9");
        
        symbolCharStrings.setKey("colon", ":");
        symbolCharStrings.setKey("semicolon", ";");
        symbolCharStrings.setKey("less", "<");
        symbolCharStrings.setKey("equal", "=");
        symbolCharStrings.setKey("greater", ">");
        symbolCharStrings.setKey("question", "?");
        symbolCharStrings.setKey("congruent", "{\\cong}");

        symbolCharStrings.setKey("alpha", "{\\alpha}");
        symbolCharStrings.setKey("beta", "{\\beta}");
        symbolCharStrings.setKey("gamma", "{\\gamma}");
        symbolCharStrings.setKey("delta", "{\\delta}");
        symbolCharStrings.setKey("epsilon", "{\\epsilon}");
        symbolCharStrings.setKey("zeta", "{\\zeta}");
        symbolCharStrings.setKey("eta", "{\\eta}");
        symbolCharStrings.setKey("theta", "{\\theta}");
        symbolCharStrings.setKey("iota", "{\\iota}");
        symbolCharStrings.setKey("kappa", "{\\kappa}");
        symbolCharStrings.setKey("lambda", "{\\lambda}");
        symbolCharStrings.setKey("mu", "{\\mu}");
        symbolCharStrings.setKey("nu", "{\\nu}");
        symbolCharStrings.setKey("xi", "{\\xi}");
        symbolCharStrings.setKey("omicron", "o");
        symbolCharStrings.setKey("pi", "{\\pi}");
        symbolCharStrings.setKey("rho", "{\\rho}");
        symbolCharStrings.setKey("sigma", "{\\sigma}");
        symbolCharStrings.setKey("tau", "{\\tau}");
        symbolCharStrings.setKey("upsilon", "{\\upsilon}");
        symbolCharStrings.setKey("phi", "{\\phi}");
        symbolCharStrings.setKey("chi", "{\\chi}");
        symbolCharStrings.setKey("psi", "{\\psi}");
        symbolCharStrings.setKey("omega", "{\\omega}");
        
        symbolCharStrings.setKey("Alpha", "A");
        symbolCharStrings.setKey("Beta", "B");
        symbolCharStrings.setKey("Gamma", "{\\Gamma}");
        symbolCharStrings.setKey("Delta", "{\\Delta}");
        symbolCharStrings.setKey("Epsilon", "E");
        symbolCharStrings.setKey("Zeta", "Z");
        symbolCharStrings.setKey("Eta", "H");
        symbolCharStrings.setKey("Theta", "{\\Theta}");
        symbolCharStrings.setKey("Iota", "I");
        symbolCharStrings.setKey("Kappa", "K");
        symbolCharStrings.setKey("Lambda", "{\\Lambda}");
        symbolCharStrings.setKey("Mu", "M");
        symbolCharStrings.setKey("Nu", "N");
        symbolCharStrings.setKey("Xi", "{\\Xi}");
        symbolCharStrings.setKey("Omicron", "O");
        symbolCharStrings.setKey("pi", "{\\Pi}");
        symbolCharStrings.setKey("rho", "P");
        symbolCharStrings.setKey("sigma", "{\\Sigma}");
        symbolCharStrings.setKey("tau", "T");
        symbolCharStrings.setKey("upsilon", "{\\Upsilon}");
        symbolCharStrings.setKey("phi", "{\\Phi}");
        symbolCharStrings.setKey("chi", "X");
        symbolCharStrings.setKey("psi", "{\\Psi}");
        symbolCharStrings.setKey("omega", "{\\Omega}");
    }

}
