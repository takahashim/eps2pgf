/*
 * Encoding.java
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

package net.sf.eps2pgf.ps.resources;

import java.util.HashMap;
import java.util.Map;

import net.sf.eps2pgf.ps.objects.PSObjectName;

/** Some tools to work with character encoding in PostScript documents.
 *
 * @author Paul Wagenaars
 */
public final class Encoding {
    /** All fields and methods are static. Therefore, we need to initialize it
     * only once.
     */
    private static boolean alreadyInitialized = false;
    
    /** List with all possible character names. */
    private static Map<String, PSObjectName> names;
    
    /** Standard encoding vector. */
    private static PSObjectName[] standard = new PSObjectName[256];
    
    /** ISO Latin 1 encoding vector. */
    private static PSObjectName[] isoLatin1 = new PSObjectName[256];
    
    /** Symbol encoding vector. */
    private static PSObjectName[] symbol = new PSObjectName[256];
    
    /**
     * "Hidden" constructor.
     */
    private Encoding() {
        /* empty block */
    }
    
    /** Initialize encodings. */
    public static void initialize() {
        if (alreadyInitialized) {
            return;
        }
        
        initNames();
        initStandardEncoding();
        initISOLatin1Encoding();
        initSymbolEncoding();
        
        alreadyInitialized = true;
    }
    
    /**
     * Return the standard encoding vector.
     * @return Standard encoding array (see PostScript manual)
     */
    public static PSObjectName[] getStandardVector() {
        return standard.clone();
    }
    
    /**
     * Return the ISO Latin1 encoding vector.
     * 
     * @return ISO Latin1 encoding array (see PostScript manual)
     */
    public static PSObjectName[] getISOLatin1Vector() {
        return isoLatin1.clone();
    }
    
    /**
     * Return the symbol encoding vector.
     * 
     * @return Symbol encoding vector array (see PostScript manual)
     */
    public static PSObjectName[] getSymbolVector() {
        return symbol.clone();
    }
    
    /** Initialize character names map. */
    private static void initNames() {
        // List with all possible character names
        String[] n = {".notdef", "A", "B", "C", "D", "E", "F", "G", "H", 
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h",
            "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u",
            "v", "w", "x", "y", "z", "AE", "CR", "Mu", "Nu", "OE", "Pi",
            "Xi", "ae", "at", "ff", "fi", "fl", "mu", "nu", "oe", "pi",
            "xi", "Chi", "Eta", "Eth", "Ohm", "Phi", "Psi", "Rho", "Tau",
            "bar", "chi", "eta", "eth", "ffi", "ffl", "mu1", "one", "phi",
            "psi", "rho", "six", "tau", "two", "yen", "Beta", "Euro",
            "Iota", "Zeta", "beta", "cent", "club", "five", "four", "iota",
            "less", "nine", "phi1", "plus", "ring", "zero", "zeta", "Alpha",
            "Aring", "Delta", "Gamma", "Kappa", "Omega", "Sigma", "Theta",
            "Thorn", "Uring", "acute", "aleph", "alpha", "angle", "aring",
            "breve", "caron", "colon", "comma", "delta", "eight", "equal",
            "franc", "gamma", "grave", "heart", "kappa", "minus", "omega",
            "seven", "sigma", "slash", "space", "spade", "theta", "thorn",
            "three", "tilde", "union", "uring", "Aacute", "Abreve",
            "Agrave", "Asmall", "Atilde", "Bsmall", "Cacute", "Ccaron",
            "Csmall", "Dcaron", "Dcroat", "Dsmall", "Eacute", "Ecaron",
            "Egrave", "Esmall", "Fsmall", "Gbreve", "Gsmall", "Hsmall",
            "Iacute", "Igrave", "Ismall", "Jsmall", "Ksmall", "Lacute",
            "Lambda", "Lcaron", "Lslash", "Lsmall", "Msmall", "Nacute",
            "Ncaron", "Nsmall", "Ntilde", "Oacute", "Ograve", "Oslash",
            "Osmall", "Otilde", "Psmall", "Qsmall", "Racute", "Rcaron",
            "Rsmall", "Sacute", "Scaron", "Ssmall", "Tcaron", "Tsmall",
            "Uacute", "Ugrave", "Usmall", "Vsmall", "Wsmall", "Xsmall",
            "Yacute", "Ysmall", "Zacute", "Zcaron", "Zsmall", "aacute",
            "abreve", "agrave", "atilde", "bullet", "cacute", "ccaron",
            "dagger", "dcaron", "dcroat", "degree", "divide", "dollar",
            "eacute", "ecaron", "egrave", "emdash", "endash", "exclam",
            "florin", "gbreve", "hyphen", "iacute", "igrave", "lacute",
            "lambda", "lcaron", "lslash", "macron", "minute", "nacute",
            "ncaron", "ntilde", "oacute", "ogonek", "ograve", "omega1",
            "oslash", "otilde", "period", "racute", "rcaron", "rupiah",
            "sacute", "scaron", "second", "sigma1", "tcaron", "theta1",
            "uacute", "ugrave", "yacute", "zacute", "zcaron", "AEsmall",
            "Amacron", "Aogonek", "Emacron", "Eogonek", "Epsilon",
            "Imacron", "Iogonek", "OEsmall", "Omacron", "Omicron",
            "Umacron", "Uogonek", "Upsilon", "amacron", "aogonek",
            "arrowup", "braceex", "cedilla", "diamond", "dmacron",
            "dotmath", "element", "emacron", "eogonek", "epsilon",
            "greater", "imacron", "iogonek", "lozenge", "nbspace",
            "omacron", "omicron", "onehalf", "percent", "product",
            "radical", "section", "similar", "umacron", "uogonek",
            "upsilon", "Ccedilla", "Ethsmall", "Ifraktur", "Rfraktur",
            "Scedilla", "Upsilon1", "asterisk", "ccedilla", "currency",
            "dieresis", "dotlessi", "ellipsis", "emptyset", "fraction",
            "gradient", "infinity", "integral", "multiply", "notequal",
            "onethird", "question", "quotedbl", "scedilla", "sterling",
            "suchthat", "Adieresis", "Edieresis", "Idieresis", "Odieresis",
            "Ringsmall", "Udieresis", "Ydieresis", "adieresis", "ampersand",
            "angleleft", "applelogo", "arrowboth", "arrowdown", "arrowleft",
            "asuperior", "backslash", "braceleft", "brokenbar", "bsuperior",
            "congruent", "copyright", "daggerdbl", "dotaccent", "dsuperior",
            "edieresis", "esuperior", "idieresis", "increment", "isuperior",
            "lessequal", "logicalor", "lsuperior", "msuperior", "notsubset",
            "nsuperior", "odieresis", "oneeighth", "onefitted", "osuperior",
            "overscore", "paragraph", "parenleft", "plusminus", "quoteleft",
            "radicalex", "rsuperior", "semicolon", "ssuperior", "summation",
            "therefore", "trademark", "tsuperior", "twothirds", "udieresis",
            "universal", "ydieresis", "Acutesmall", "Aringsmall",
            "Brevesmall", "Caronsmall", "Edotaccent", "Gravesmall",
            "Idotaccent", "Thornsmall", "Tildesmall", "Zdotaccent",
            "angleright", "arrowdblup", "arrowright", "asciitilde",
            "braceright", "circleplus", "circumflex", "edotaccent",
            "exclamdown", "figuredash", "germandbls", "integralbt",
            "integralex", "integraltp", "logicaland", "logicalnot",
            "notelement", "numbersign", "onequarter", "parenright",
            "quoteright", "registered", "underscore", "zdotaccent",
            "Aacutesmall", "Acircumflex", "Agravesmall", "Atildesmall",
            "Eacutesmall", "Ecircumflex", "Egravesmall", "Iacutesmall",
            "Icircumflex", "Igravesmall", "Lslashsmall", "Macronsmall",
            "Ntildesmall", "Oacutesmall", "Ocircumflex", "Ogoneksmall",
            "Ogravesmall", "Oslashsmall", "Otildesmall", "Scaronsmall",
            "Uacutesmall", "Ucircumflex", "Ugravesmall", "Yacutesmall",
            "Zcaronsmall", "acircumflex", "approxequal", "arrowvertex",
            "asciicircum", "braceleftbt", "bracelefttp", "bracketleft",
            "commaaccent", "ecircumflex", "equivalence", "exclamsmall",
            "existential", "fiveeighths", "icircumflex", "ocircumflex",
            "oneinferior", "oneoldstyle", "onesuperior", "ordfeminine",
            "parenleftbt", "parenleftex", "parenlefttp", "partialdiff",
            "perthousand", "quotesingle", "sixinferior", "sixoldstyle",
            "sixsuperior", "twoinferior", "twooldstyle", "twosuperior",
            "ucircumflex", "weierstrass", "Cedillasmall", "Gcommaaccent",
            "Kcommaaccent", "Lcommaaccent", "Ncommaaccent", "Rcommaaccent",
            "Scommaaccent", "Tcommaaccent", "arrowdblboth", "arrowdbldown",
            "arrowdblleft", "arrowhorizex", "asteriskmath", "braceleftmid",
            "bracerightbt", "bracerighttp", "bracketright", "centinferior",
            "centoldstyle", "centsuperior", "fiveinferior", "fiveoldstyle",
            "fivesuperior", "fourinferior", "fouroldstyle", "foursuperior",
            "gcommaaccent", "greaterequal", "hungarumlaut", "intersection",
            "kcommaaccent", "lcommaaccent", "ncommaaccent", "nineinferior",
            "nineoldstyle", "ninesuperior", "ordmasculine", "parenrightbt",
            "parenrightex", "parenrighttp", "propersubset", "proportional",
            "questiondown", "quotedblbase", "quotedblleft", "rcommaaccent",
            "reflexsubset", "registersans", "scommaaccent", "seveneighths",
            "tcommaaccent", "threeeighths", "zeroinferior", "zerooldstyle",
            "zerosuperior", "Ccedillasmall", "Dieresissmall",
            "Ohungarumlaut", "Uhungarumlaut", "arrowdblright",
            "bracerightmid", "bracketleftbt", "bracketleftex",
            "bracketlefttp", "colonmonetary", "commainferior",
            "commasuperior", "copyrightsans", "eightinferior",
            "eightoldstyle", "eightsuperior", "guillemotleft",
            "guilsinglleft", "ohungarumlaut", "perpendicular",
            "questionsmall", "quotedblright", "registerserif",
            "seveninferior", "sevenoldstyle", "sevensuperior",
            "threeinferior", "threeoldstyle", "threequarters",
            "threesuperior", "trademarksans", "uhungarumlaut",
            "Adieresissmall", "Dotaccentsmall", "Edieresissmall",
            "Idieresissmall", "Odieresissmall", "Udieresissmall",
            "Ydieresissmall", "ampersandsmall", "bracketrightbt",
            "bracketrightex", "bracketrighttp", "carriagereturn",
            "circlemultiply", "copyrightserif", "dollarinferior",
            "dollaroldstyle", "dollarsuperior", "guillemotright",
            "guilsinglright", "hypheninferior", "hyphensuperior",
            "onedotenleader", "periodcentered", "periodinferior",
            "periodsuperior", "propersuperset", "quotesinglbase",
            "reflexsuperset", "trademarkserif", "twodotenleader",
            "Circumflexsmall", "exclamdownsmall", "Acircumflexsmall",
            "Ecircumflexsmall", "Icircumflexsmall", "Ocircumflexsmall",
            "Ucircumflexsmall", "Hungarumlautsmall", "parenleftinferior",
            "parenleftsuperior", "questiondownsmall", "parenrightinferior",
            "parenrightsuperior", "threequartersemdash",
        };
        names = new HashMap<String, PSObjectName>(n.length);
        for (int i = 0; i < n.length; i++) {
            names.put(n[i], new PSObjectName("/" + n[i]));
        }
    }
    
    /** Defines the standard encoding vector. */
    private static void initStandardEncoding() {
        standard[0] = names.get(".notdef");     //000
        standard[1] = names.get(".notdef");     //001
        standard[2] = names.get(".notdef");     //002
        standard[3] = names.get(".notdef");     //003
        standard[4] = names.get(".notdef");     //004
        standard[5] = names.get(".notdef");     //005
        standard[6] = names.get(".notdef");     //006
        standard[7] = names.get(".notdef");     //007
        standard[8] = names.get(".notdef");     //010
        standard[9] = names.get(".notdef");     //011
        standard[10] = names.get(".notdef");     //012
        standard[11] = names.get(".notdef");     //013
        standard[12] = names.get(".notdef");     //014
        standard[13] = names.get(".notdef");     //015
        standard[14] = names.get(".notdef");     //016
        standard[15] = names.get(".notdef");     //017
        standard[16] = names.get(".notdef");     //020
        standard[17] = names.get(".notdef");     //021
        standard[18] = names.get(".notdef");     //022
        standard[19] = names.get(".notdef");     //023
        standard[20] = names.get(".notdef");     //024
        standard[21] = names.get(".notdef");     //025
        standard[22] = names.get(".notdef");     //026
        standard[23] = names.get(".notdef");     //027
        standard[24] = names.get(".notdef");     //030
        standard[25] = names.get(".notdef");     //031
        standard[26] = names.get(".notdef");     //032
        standard[27] = names.get(".notdef");     //033
        standard[28] = names.get(".notdef");     //034
        standard[29] = names.get(".notdef");     //035
        standard[30] = names.get(".notdef");     //036
        standard[31] = names.get(".notdef");     //037
        standard[32] = names.get("space");       //040
        standard[33] = names.get("exclam");      //041
        standard[34] = names.get("quotedbl");    //042
        standard[35] = names.get("numbersign");  //043
        standard[36] = names.get("dollar");      //044
        standard[37] = names.get("percent");     //045
        standard[38] = names.get("ampersand");   //046
        standard[39] = names.get("quoteright");  //047
        standard[40] = names.get("parenleft");   //050
        standard[41] = names.get("parenright");  //051
        standard[42] = names.get("asterisk");    //052
        standard[43] = names.get("plus");        //053
        standard[44] = names.get("comma");       //054
        standard[45] = names.get("minus");       //055
        standard[46] = names.get("period");      //056
        standard[47] = names.get("slash");       //057
        standard[48] = names.get("zero");        //060
        standard[49] = names.get("one");         //061
        standard[50] = names.get("two");         //062
        standard[51] = names.get("three");       //063
        standard[52] = names.get("four");        //064
        standard[53] = names.get("five");        //065
        standard[54] = names.get("six");         //066
        standard[55] = names.get("seven");       //067
        standard[56] = names.get("eight");       //070
        standard[57] = names.get("nine");        //071
        standard[58] = names.get("colon");       //072
        standard[59] = names.get("semicolon");   //073
        standard[60] = names.get("less");        //074
        standard[61] = names.get("equal");       //075
        standard[62] = names.get("greater");     //076
        standard[63] = names.get("question");    //077
        standard[64] = names.get("at");          //100
        standard[65] = names.get("A");           //101
        standard[66] = names.get("B");           //102
        standard[67] = names.get("C");           //103
        standard[68] = names.get("D");           //104
        standard[69] = names.get("E");           //105
        standard[70] = names.get("F");           //106
        standard[71] = names.get("G");           //107
        standard[72] = names.get("H");           //110
        standard[73] = names.get("I");           //111
        standard[74] = names.get("J");           //112
        standard[75] = names.get("K");           //113
        standard[76] = names.get("L");           //114
        standard[77] = names.get("M");           //115
        standard[78] = names.get("N");           //116
        standard[79] = names.get("O");           //117
        standard[80] = names.get("P");           //120
        standard[81] = names.get("Q");           //121
        standard[82] = names.get("R");           //122
        standard[83] = names.get("S");           //123
        standard[84] = names.get("T");           //124
        standard[85] = names.get("U");           //125
        standard[86] = names.get("V");           //126
        standard[87] = names.get("W");           //127
        standard[88] = names.get("X");           //130
        standard[89] = names.get("Y");           //131
        standard[90] = names.get("Z");           //132
        standard[91] = names.get("bracketleft"); //133
        standard[92] = names.get("backslash");   //134
        standard[93] = names.get("bracketright"); //135
        standard[94] = names.get("asciicircum"); //136
        standard[95] = names.get("underscore");  //137
        standard[96] = names.get("quoteleft");   //140
        standard[97] = names.get("a");           //141
        standard[98] = names.get("b");           //142
        standard[99] = names.get("c");           //143
        standard[100] = names.get("d");           //144
        standard[101] = names.get("e");           //145
        standard[102] = names.get("f");           //146
        standard[103] = names.get("g");           //147
        standard[104] = names.get("h");           //150
        standard[105] = names.get("i");           //151
        standard[106] = names.get("j");           //152
        standard[107] = names.get("k");           //153
        standard[108] = names.get("l");           //154
        standard[109] = names.get("m");           //155
        standard[110] = names.get("n");           //156
        standard[111] = names.get("o");           //157
        standard[112] = names.get("p");           //160
        standard[113] = names.get("q");           //161
        standard[114] = names.get("r");           //162
        standard[115] = names.get("s");           //163
        standard[116] = names.get("t");           //164
        standard[117] = names.get("u");           //165
        standard[118] = names.get("v");           //166
        standard[119] = names.get("w");           //167
        standard[120] = names.get("x");           //170
        standard[121] = names.get("y");           //171
        standard[122] = names.get("z");           //172
        standard[123] = names.get("braceleft");   //173
        standard[124] = names.get("bar");         //174
        standard[125] = names.get("braceright");  //175
        standard[126] = names.get("asciitilde");  //176
        standard[127] = names.get(".notdef");     //177
        standard[128] = names.get(".notdef");     //200
        standard[129] = names.get(".notdef");     //201
        standard[130] = names.get(".notdef");     //202
        standard[131] = names.get(".notdef");     //203
        standard[132] = names.get(".notdef");     //204
        standard[133] = names.get(".notdef");     //205
        standard[134] = names.get(".notdef");     //206
        standard[135] = names.get(".notdef");     //207
        standard[136] = names.get(".notdef");     //210
        standard[137] = names.get(".notdef");     //211
        standard[138] = names.get(".notdef");     //212
        standard[139] = names.get(".notdef");     //213
        standard[140] = names.get(".notdef");     //214
        standard[141] = names.get(".notdef");     //215
        standard[142] = names.get(".notdef");     //216
        standard[143] = names.get(".notdef");     //217
        standard[144] = names.get(".notdef");     //220
        standard[145] = names.get(".notdef");     //221
        standard[146] = names.get(".notdef");     //222
        standard[147] = names.get(".notdef");     //223
        standard[148] = names.get(".notdef");     //224
        standard[149] = names.get(".notdef");     //225
        standard[150] = names.get(".notdef");     //226
        standard[151] = names.get(".notdef");     //227
        standard[152] = names.get(".notdef");     //230
        standard[153] = names.get(".notdef");     //231
        standard[154] = names.get(".notdef");     //232
        standard[155] = names.get(".notdef");     //233
        standard[156] = names.get(".notdef");     //234
        standard[157] = names.get(".notdef");     //235
        standard[158] = names.get(".notdef");     //236
        standard[159] = names.get(".notdef");     //237
        standard[160] = names.get(".notdef");     //240
        standard[161] = names.get("exclamdown");  //241
        standard[162] = names.get("cent");        //242
        standard[163] = names.get("sterling");    //243
        standard[164] = names.get("fraction");    //244
        standard[165] = names.get("yen");         //245
        standard[166] = names.get("florin");      //246
        standard[167] = names.get("section");     //247
        standard[168] = names.get("currency");    //250
        standard[169] = names.get("quotesingle"); //251
        standard[170] = names.get("quotedblleft"); //252
        standard[171] = names.get("guillemotleft"); //253
        standard[172] = names.get("guilsinglleft"); //254
        standard[173] = names.get("guilsinglright"); //255
        standard[174] = names.get("fi");          //256
        standard[175] = names.get("fl");          //257
        standard[176] = names.get(".notdef");     //260
        standard[177] = names.get("endash");      //261
        standard[178] = names.get("dagger");      //262
        standard[179] = names.get("daggerdbl");   //263
        standard[180] = names.get("periodcentered"); //264
        standard[181] = names.get(".notdef");     //265
        standard[182] = names.get("paragraph");   //266
        standard[183] = names.get("bullet");      //267
        standard[184] = names.get("quotesinglbase"); //270
        standard[185] = names.get("quotedblbase"); //271
        standard[186] = names.get("quotedblright");  //272
        standard[187] = names.get("guillemotright"); //273
        standard[188] = names.get("ellipsis");    //274
        standard[189] = names.get("perthousand"); //275        
        standard[190] = names.get(".notdef");     //276        
        standard[191] = names.get("questiondown"); //277
        standard[192] = names.get(".notdef");     //300
        standard[193] = names.get("grave");       //301
        standard[194] = names.get("acute");       //302
        standard[195] = names.get("circumflex");  //303
        standard[196] = names.get("tilde");       //304
        standard[197] = names.get("macron");      //305
        standard[198] = names.get("breve");       //306
        standard[199] = names.get("dotaccent");   //307
        standard[200] = names.get("dieresis");    //310
        standard[201] = names.get(".notdef");     //311
        standard[202] = names.get("ring");        //312
        standard[203] = names.get("cedilla");     //313
        standard[204] = names.get(".notdef");     //314
        standard[205] = names.get("hungarumlaut"); //315
        standard[206] = names.get("ogonek");      //316
        standard[207] = names.get("caron");       //317
        standard[208] = names.get("emdash");      //320
        standard[209] = names.get(".notdef");     //321
        standard[210] = names.get(".notdef");     //322
        standard[211] = names.get(".notdef");     //323
        standard[212] = names.get(".notdef");     //324
        standard[213] = names.get(".notdef");     //325
        standard[214] = names.get(".notdef");     //326
        standard[215] = names.get(".notdef");     //327
        standard[216] = names.get(".notdef");     //330
        standard[217] = names.get(".notdef");     //331
        standard[218] = names.get(".notdef");     //332
        standard[219] = names.get(".notdef");     //333
        standard[220] = names.get(".notdef");     //334
        standard[221] = names.get(".notdef");     //335
        standard[222] = names.get(".notdef");     //336
        standard[223] = names.get(".notdef");     //337
        standard[224] = names.get(".notdef");     //340
        standard[225] = names.get("AE");          //341
        standard[226] = names.get(".notdef");     //342
        standard[227] = names.get("ordfeminine"); //343
        standard[228] = names.get(".notdef");     //344
        standard[229] = names.get(".notdef");     //345
        standard[230] = names.get(".notdef");     //346
        standard[231] = names.get(".notdef");     //347
        standard[232] = names.get("Lslash");      //350
        standard[233] = names.get("Oslash");      //351
        standard[234] = names.get("OE");          //352
        standard[235] = names.get("ordmasculine"); //353
        standard[236] = names.get(".notdef");     //354
        standard[237] = names.get(".notdef");     //355
        standard[238] = names.get(".notdef");     //356
        standard[239] = names.get(".notdef");     //357
        standard[240] = names.get(".notdef");     //360
        standard[241] = names.get("ae");          //361
        standard[242] = names.get(".notdef");     //362
        standard[243] = names.get(".notdef");     //363
        standard[244] = names.get(".notdef");     //364
        standard[245] = names.get("dotlessi");    //365
        standard[246] = names.get(".notdef");     //366
        standard[247] = names.get(".notdef");     //367
        standard[248] = names.get("lslash");      //370
        standard[249] = names.get("oslash");      //371
        standard[250] = names.get("oe");          //372
        standard[251] = names.get("germandbls");  //373
        standard[252] = names.get(".notdef");     //374
        standard[253] = names.get(".notdef");     //375
        standard[254] = names.get(".notdef");     //376
        standard[255] = names.get(".notdef");     //377
    }
    
    /** Defines the ISO Latin1 encoding vector. */
    private static void initISOLatin1Encoding() {
        isoLatin1[0] = names.get(".notdef");     //000
        isoLatin1[1] = names.get(".notdef");     //001
        isoLatin1[2] = names.get(".notdef");     //002
        isoLatin1[3] = names.get(".notdef");     //003
        isoLatin1[4] = names.get(".notdef");     //004
        isoLatin1[5] = names.get(".notdef");     //005
        isoLatin1[6] = names.get(".notdef");     //006
        isoLatin1[7] = names.get(".notdef");     //007
        isoLatin1[8] = names.get(".notdef");     //010
        isoLatin1[9] = names.get(".notdef");     //011
        isoLatin1[10] = names.get(".notdef");     //012
        isoLatin1[11] = names.get(".notdef");     //013
        isoLatin1[12] = names.get(".notdef");     //014
        isoLatin1[13] = names.get(".notdef");     //015
        isoLatin1[14] = names.get(".notdef");     //016
        isoLatin1[15] = names.get(".notdef");     //017
        isoLatin1[16] = names.get(".notdef");     //020
        isoLatin1[17] = names.get(".notdef");     //021
        isoLatin1[18] = names.get(".notdef");     //022
        isoLatin1[19] = names.get(".notdef");     //023
        isoLatin1[20] = names.get(".notdef");     //024
        isoLatin1[21] = names.get(".notdef");     //025
        isoLatin1[22] = names.get(".notdef");     //026
        isoLatin1[23] = names.get(".notdef");     //027
        isoLatin1[24] = names.get(".notdef");     //030
        isoLatin1[25] = names.get(".notdef");     //031
        isoLatin1[26] = names.get(".notdef");     //032
        isoLatin1[27] = names.get(".notdef");     //033
        isoLatin1[28] = names.get(".notdef");     //034
        isoLatin1[29] = names.get(".notdef");     //035
        isoLatin1[30] = names.get(".notdef");     //036
        isoLatin1[31] = names.get(".notdef");     //037
        isoLatin1[32] = names.get("space");       //040
        isoLatin1[33] = names.get("exclam");      //041
        isoLatin1[34] = names.get("quotedbl");    //042
        isoLatin1[35] = names.get("numbersign");  //043
        isoLatin1[36] = names.get("dollar");      //044
        isoLatin1[37] = names.get("percent");     //045
        isoLatin1[38] = names.get("ampersand");   //046
        isoLatin1[39] = names.get("quoteright");  //047
        isoLatin1[40] = names.get("parenleft");   //050
        isoLatin1[41] = names.get("parenright");  //051
        isoLatin1[42] = names.get("asterisk");    //052
        isoLatin1[43] = names.get("plus");        //053
        isoLatin1[44] = names.get("comma");       //054
        isoLatin1[45] = names.get("minus");       //055
        isoLatin1[46] = names.get("period");      //056
        isoLatin1[47] = names.get("slash");       //057
        isoLatin1[48] = names.get("zero");        //060
        isoLatin1[49] = names.get("one");         //061
        isoLatin1[50] = names.get("two");         //062
        isoLatin1[51] = names.get("three");       //063
        isoLatin1[52] = names.get("four");        //064
        isoLatin1[53] = names.get("five");        //065
        isoLatin1[54] = names.get("six");         //066
        isoLatin1[55] = names.get("seven");       //067
        isoLatin1[56] = names.get("eight");       //070
        isoLatin1[57] = names.get("nine");        //071
        isoLatin1[58] = names.get("colon");       //072
        isoLatin1[59] = names.get("semicolon");   //073
        isoLatin1[60] = names.get("less");        //074
        isoLatin1[61] = names.get("equal");       //075
        isoLatin1[62] = names.get("greater");     //076
        isoLatin1[63] = names.get("question");    //077
        isoLatin1[64] = names.get("at");          //100
        isoLatin1[65] = names.get("A");           //101
        isoLatin1[66] = names.get("B");           //102
        isoLatin1[67] = names.get("C");           //103
        isoLatin1[68] = names.get("D");           //104
        isoLatin1[69] = names.get("E");           //105
        isoLatin1[70] = names.get("F");           //106
        isoLatin1[71] = names.get("G");           //107
        isoLatin1[72] = names.get("H");           //110
        isoLatin1[73] = names.get("I");           //111
        isoLatin1[74] = names.get("J");           //112
        isoLatin1[75] = names.get("K");           //113
        isoLatin1[76] = names.get("L");           //114
        isoLatin1[77] = names.get("M");           //115
        isoLatin1[78] = names.get("N");           //116
        isoLatin1[79] = names.get("O");           //117
        isoLatin1[80] = names.get("P");           //120
        isoLatin1[81] = names.get("Q");           //121
        isoLatin1[82] = names.get("R");           //122
        isoLatin1[83] = names.get("S");           //123
        isoLatin1[84] = names.get("T");           //124
        isoLatin1[85] = names.get("U");           //125
        isoLatin1[86] = names.get("V");           //126
        isoLatin1[87] = names.get("W");           //127
        isoLatin1[88] = names.get("X");           //130
        isoLatin1[89] = names.get("Y");           //131
        isoLatin1[90] = names.get("Z");           //132
        isoLatin1[91] = names.get("bracketleft"); //133
        isoLatin1[92] = names.get("backslash");   //134
        isoLatin1[93] = names.get("bracketright"); //135
        isoLatin1[94] = names.get("asciicircum"); //136
        isoLatin1[95] = names.get("underscore");  //137
        isoLatin1[96] = names.get("quoteleft");   //140
        isoLatin1[97] = names.get("a");           //141
        isoLatin1[98] = names.get("b");           //142
        isoLatin1[99] = names.get("c");           //143
        isoLatin1[100] = names.get("d");           //144
        isoLatin1[101] = names.get("e");           //145
        isoLatin1[102] = names.get("f");           //146
        isoLatin1[103] = names.get("g");           //147
        isoLatin1[104] = names.get("h");           //150
        isoLatin1[105] = names.get("i");           //151
        isoLatin1[106] = names.get("j");           //152
        isoLatin1[107] = names.get("k");           //153
        isoLatin1[108] = names.get("l");           //154
        isoLatin1[109] = names.get("m");           //155
        isoLatin1[110] = names.get("n");           //156
        isoLatin1[111] = names.get("o");           //157
        isoLatin1[112] = names.get("p");           //160
        isoLatin1[113] = names.get("q");           //161
        isoLatin1[114] = names.get("r");           //162
        isoLatin1[115] = names.get("s");           //163
        isoLatin1[116] = names.get("t");           //164
        isoLatin1[117] = names.get("u");           //165
        isoLatin1[118] = names.get("v");           //166
        isoLatin1[119] = names.get("w");           //167
        isoLatin1[120] = names.get("x");           //170
        isoLatin1[121] = names.get("y");           //171
        isoLatin1[122] = names.get("z");           //172
        isoLatin1[123] = names.get("braceleft");   //173
        isoLatin1[124] = names.get("bar");         //174
        isoLatin1[125] = names.get("braceright");  //175
        isoLatin1[126] = names.get("asciitilde");  //176
        isoLatin1[127] = names.get(".notdef");     //177
        isoLatin1[128] = names.get(".notdef");     //200
        isoLatin1[129] = names.get(".notdef");     //201
        isoLatin1[130] = names.get(".notdef");     //202
        isoLatin1[131] = names.get(".notdef");     //203
        isoLatin1[132] = names.get(".notdef");     //204
        isoLatin1[133] = names.get(".notdef");     //205
        isoLatin1[134] = names.get(".notdef");     //206
        isoLatin1[135] = names.get(".notdef");     //207
        isoLatin1[136] = names.get(".notdef");     //210
        isoLatin1[137] = names.get(".notdef");     //211
        isoLatin1[138] = names.get(".notdef");     //212
        isoLatin1[139] = names.get(".notdef");     //213
        isoLatin1[140] = names.get(".notdef");     //214
        isoLatin1[141] = names.get(".notdef");     //215
        isoLatin1[142] = names.get(".notdef");     //216
        isoLatin1[143] = names.get(".notdef");     //217
        isoLatin1[144] = names.get("dotlessi");    //220
        isoLatin1[145] = names.get("grave");       //221
        isoLatin1[146] = names.get("acute");       //222
        isoLatin1[147] = names.get("circumflex");  //223
        isoLatin1[148] = names.get("tilde");       //224
        isoLatin1[149] = names.get("macron");      //225
        isoLatin1[150] = names.get("breve");       //226
        isoLatin1[151] = names.get("dotaccent");   //227
        isoLatin1[152] = names.get("dieresis");    //230
        isoLatin1[153] = names.get(".notdef");     //231
        isoLatin1[154] = names.get("ring");        //232
        isoLatin1[155] = names.get("cedilla");     //233
        isoLatin1[156] = names.get(".notdef");     //234
        isoLatin1[157] = names.get("hungarumlaut"); //235
        isoLatin1[158] = names.get("ogonek");      //236
        isoLatin1[159] = names.get("caron");       //237
        isoLatin1[160] = names.get("space");       //240
        isoLatin1[161] = names.get("exclamdown");  //241
        isoLatin1[162] = names.get("cent");        //242
        isoLatin1[163] = names.get("sterling");    //243
        isoLatin1[164] = names.get("currency");    //244
        isoLatin1[165] = names.get("yen");         //245
        isoLatin1[166] = names.get("brokenbar");   //246
        isoLatin1[167] = names.get("section");     //247
        isoLatin1[168] = names.get("dieresis");    //250
        isoLatin1[169] = names.get("copyright");   //251
        isoLatin1[170] = names.get("ordfeminine"); //252
        isoLatin1[171] = names.get("guillemotleft"); //253
        isoLatin1[172] = names.get("logicalnot");  //254
        isoLatin1[173] = names.get("hyphen");      //255
        isoLatin1[174] = names.get("registered");  //256
        isoLatin1[175] = names.get("macron");      //257
        isoLatin1[176] = names.get("degree");      //260
        isoLatin1[177] = names.get("plusminus");   //261
        isoLatin1[178] = names.get("twosuperior"); //262
        isoLatin1[179] = names.get("threesuperior"); //263
        isoLatin1[180] = names.get("acute");       //264
        isoLatin1[181] = names.get("mu");          //265
        isoLatin1[182] = names.get("paragraph");   //266
        isoLatin1[183] = names.get("periodcentered"); //267
        isoLatin1[184] = names.get("cedilla");     //270
        isoLatin1[185] = names.get("onesuperior"); //271
        isoLatin1[186] = names.get("ordmasculine"); //272
        isoLatin1[187] = names.get("guillemotright"); //273
        isoLatin1[188] = names.get("onequarter");  //274
        isoLatin1[189] = names.get("onehalf");     //275        
        isoLatin1[190] = names.get("threequarters"); //276        
        isoLatin1[191] = names.get("questiondown"); //277
        isoLatin1[192] = names.get("Agrave");      //300
        isoLatin1[193] = names.get("Aacute");      //301
        isoLatin1[194] = names.get("Acircumflex"); //302
        isoLatin1[195] = names.get("Atilde");      //303
        isoLatin1[196] = names.get("Adieresis");   //304
        isoLatin1[197] = names.get("Aring");       //305
        isoLatin1[198] = names.get("AE");          //306
        isoLatin1[199] = names.get("Ccedilla");    //307
        isoLatin1[200] = names.get("Egrave");      //310
        isoLatin1[201] = names.get("Eacute");      //311
        isoLatin1[202] = names.get("Ecircumflex"); //312
        isoLatin1[203] = names.get("Edieresis");   //313
        isoLatin1[204] = names.get("Igrave");      //314
        isoLatin1[205] = names.get("Iacute");      //315
        isoLatin1[206] = names.get("Icircumflex"); //316
        isoLatin1[207] = names.get("Idieresis");   //317
        isoLatin1[208] = names.get("Eth");         //320
        isoLatin1[209] = names.get("Ntilde");      //321
        isoLatin1[210] = names.get("Ograve");      //322
        isoLatin1[211] = names.get("Oacute");      //323
        isoLatin1[212] = names.get("Ocircumflex"); //324
        isoLatin1[213] = names.get("Otilde");      //325
        isoLatin1[214] = names.get("Odieresis");   //326
        isoLatin1[215] = names.get("multiply");    //327
        isoLatin1[216] = names.get("Oslash");      //330
        isoLatin1[217] = names.get("Ugrave");      //331
        isoLatin1[218] = names.get("Uacute");      //332
        isoLatin1[219] = names.get("Ucircumflex"); //333
        isoLatin1[220] = names.get("Udieresis");   //334
        isoLatin1[221] = names.get("Yacute");      //335
        isoLatin1[222] = names.get("Thorn");       //336
        isoLatin1[223] = names.get("germandbls");  //337
        isoLatin1[224] = names.get("agrave");      //340
        isoLatin1[225] = names.get("aacute");      //341
        isoLatin1[226] = names.get("acircumflex"); //342
        isoLatin1[227] = names.get("atilde");      //343
        isoLatin1[228] = names.get("adieresis");   //344
        isoLatin1[229] = names.get("aring");       //345
        isoLatin1[230] = names.get("ae");          //346
        isoLatin1[231] = names.get("ccedilla");    //347
        isoLatin1[232] = names.get("egrave");      //350
        isoLatin1[233] = names.get("eacute");      //351
        isoLatin1[234] = names.get("ecircumflex"); //352
        isoLatin1[235] = names.get("edieresis");   //353
        isoLatin1[236] = names.get("igrave");      //354
        isoLatin1[237] = names.get("iacute");      //355
        isoLatin1[238] = names.get("icircumflex"); //356
        isoLatin1[239] = names.get("idieresis");   //357
        isoLatin1[240] = names.get("eth");         //360
        isoLatin1[241] = names.get("ntilde");      //361
        isoLatin1[242] = names.get("ograve");      //362
        isoLatin1[243] = names.get("oacute");      //363
        isoLatin1[244] = names.get("ocircumflex"); //364
        isoLatin1[245] = names.get("otilde");      //365
        isoLatin1[246] = names.get("odieresis");   //366
        isoLatin1[247] = names.get("divide");      //367
        isoLatin1[248] = names.get("oslash");      //370
        isoLatin1[249] = names.get("ugrave");      //371
        isoLatin1[250] = names.get("uacute");      //372
        isoLatin1[251] = names.get("ucircumflex"); //373
        isoLatin1[252] = names.get("udieresis");   //374
        isoLatin1[253] = names.get("yacute");      //375
        isoLatin1[254] = names.get("thorn");       //376
        isoLatin1[255] = names.get("ydieresis");   //377
    }
    
    /** Defines the symbol encoding vector. */
    private static void initSymbolEncoding() {
        symbol[0] = names.get(".notdef");     //000
        symbol[1] = names.get(".notdef");     //001
        symbol[2] = names.get(".notdef");     //002
        symbol[3] = names.get(".notdef");     //003
        symbol[4] = names.get(".notdef");     //004
        symbol[5] = names.get(".notdef");     //005
        symbol[6] = names.get(".notdef");     //006
        symbol[7] = names.get(".notdef");     //007
        symbol[8] = names.get(".notdef");     //010
        symbol[9] = names.get(".notdef");     //011
        symbol[10] = names.get(".notdef");     //012
        symbol[11] = names.get(".notdef");     //013
        symbol[12] = names.get(".notdef");     //014
        symbol[13] = names.get(".notdef");     //015
        symbol[14] = names.get(".notdef");     //016
        symbol[15] = names.get(".notdef");     //017
        symbol[16] = names.get(".notdef");     //020
        symbol[17] = names.get(".notdef");     //021
        symbol[18] = names.get(".notdef");     //022
        symbol[19] = names.get(".notdef");     //023
        symbol[20] = names.get(".notdef");     //024
        symbol[21] = names.get(".notdef");     //025
        symbol[22] = names.get(".notdef");     //026
        symbol[23] = names.get(".notdef");     //027
        symbol[24] = names.get(".notdef");     //030
        symbol[25] = names.get(".notdef");     //031
        symbol[26] = names.get(".notdef");     //032
        symbol[27] = names.get(".notdef");     //033
        symbol[28] = names.get(".notdef");     //034
        symbol[29] = names.get(".notdef");     //035
        symbol[30] = names.get(".notdef");     //036
        symbol[31] = names.get(".notdef");     //037
        symbol[32] = names.get("space");       //040
        symbol[33] = names.get("exclam");      //041
        symbol[34] = names.get("universal");   //042
        symbol[35] = names.get("numbersign");  //043
        symbol[36] = names.get("existential"); //044
        symbol[37] = names.get("percent");     //045
        symbol[38] = names.get("ampersand");   //046
        symbol[39] = names.get("suchthat");    //047
        symbol[40] = names.get("parenleft");   //050
        symbol[41] = names.get("parenright");  //051
        symbol[42] = names.get("asterisk");    //052
        symbol[43] = names.get("plus");        //053
        symbol[44] = names.get("comma");       //054
        symbol[45] = names.get("minus");       //055
        symbol[46] = names.get("period");      //056
        symbol[47] = names.get("slash");       //057
        symbol[48] = names.get("zero");        //060
        symbol[49] = names.get("one");         //061
        symbol[50] = names.get("two");         //062
        symbol[51] = names.get("three");       //063
        symbol[52] = names.get("four");        //064
        symbol[53] = names.get("five");        //065
        symbol[54] = names.get("six");         //066
        symbol[55] = names.get("seven");       //067
        symbol[56] = names.get("eight");       //070
        symbol[57] = names.get("nine");        //071
        symbol[58] = names.get("colon");       //072
        symbol[59] = names.get("semicolon");   //073
        symbol[60] = names.get("less");        //074
        symbol[61] = names.get("equal");       //075
        symbol[62] = names.get("greater");     //076
        symbol[63] = names.get("question");    //077
        symbol[64] = names.get("congruent");   //100
        symbol[65] = names.get("Alpha");       //101
        symbol[66] = names.get("Beta");        //102
        symbol[67] = names.get("Chi");         //103
        symbol[68] = names.get("Delta");       //104
        symbol[69] = names.get("Epsilon");     //105
        symbol[70] = names.get("Phi");         //106
        symbol[71] = names.get("Gamma");       //107
        symbol[72] = names.get("Eta");         //110
        symbol[73] = names.get("Iota");        //111
        symbol[74] = names.get("theta1");      //112
        symbol[75] = names.get("Kappa");       //113
        symbol[76] = names.get("Lambda");      //114
        symbol[77] = names.get("Mu");          //115
        symbol[78] = names.get("Nu");          //116
        symbol[79] = names.get("Omicron");     //117
        symbol[80] = names.get("Pi");          //120
        symbol[81] = names.get("Theta");       //121
        symbol[82] = names.get("Rho");         //122
        symbol[83] = names.get("Sigma");       //123
        symbol[84] = names.get("Tau");         //124
        symbol[85] = names.get("Upsilon");     //125
        symbol[86] = names.get("sigma1");      //126
        symbol[87] = names.get("Omega");       //127
        symbol[88] = names.get("Xi");          //130
        symbol[89] = names.get("Psi");         //131
        symbol[90] = names.get("Zeta");        //132
        symbol[91] = names.get("bracketleft"); //133
        symbol[92] = names.get("therefore");    //134
        symbol[93] = names.get("bracketright"); //135
        symbol[94] = names.get("perpendicular"); //136
        symbol[95] = names.get("underscore");  //137
        symbol[96] = names.get("radicalex");   //140
        symbol[97] = names.get("alpha");       //141
        symbol[98] = names.get("beta");        //142
        symbol[99] = names.get("chi");         //143
        symbol[100] = names.get("delta");       //144
        symbol[101] = names.get("epsilon");     //145
        symbol[102] = names.get("phi");         //146
        symbol[103] = names.get("gamma");       //147
        symbol[104] = names.get("eta");         //150
        symbol[105] = names.get("iota");        //151
        symbol[106] = names.get("phi1");        //152
        symbol[107] = names.get("kappa");       //153
        symbol[108] = names.get("lambda");      //154
        symbol[109] = names.get("mu");          //155
        symbol[110] = names.get("nu");          //156
        symbol[111] = names.get("omicron");     //157
        symbol[112] = names.get("pi");          //160
        symbol[113] = names.get("theta");       //161
        symbol[114] = names.get("rho");         //162
        symbol[115] = names.get("sigma");       //163
        symbol[116] = names.get("tau");         //164
        symbol[117] = names.get("upsilon");     //165
        symbol[118] = names.get("omega1");      //166
        symbol[119] = names.get("omega");       //167
        symbol[120] = names.get("xi");          //170
        symbol[121] = names.get("psi");         //171
        symbol[122] = names.get("zeta");        //172
        symbol[123] = names.get("braceleft");   //173
        symbol[124] = names.get("bar");         //174
        symbol[125] = names.get("braceright");  //175
        symbol[126] = names.get("similar");     //176
        symbol[127] = names.get(".notdef");     //177
        symbol[128] = names.get(".notdef");     //200
        symbol[129] = names.get(".notdef");     //201
        symbol[130] = names.get(".notdef");     //202
        symbol[131] = names.get(".notdef");     //203
        symbol[132] = names.get(".notdef");     //204
        symbol[133] = names.get(".notdef");     //205
        symbol[134] = names.get(".notdef");     //206
        symbol[135] = names.get(".notdef");     //207
        symbol[136] = names.get(".notdef");     //210
        symbol[137] = names.get(".notdef");     //211
        symbol[138] = names.get(".notdef");     //212
        symbol[139] = names.get(".notdef");     //213
        symbol[140] = names.get(".notdef");     //214
        symbol[141] = names.get(".notdef");     //215
        symbol[142] = names.get(".notdef");     //216
        symbol[143] = names.get(".notdef");     //217
        symbol[144] = names.get(".notdef");     //220
        symbol[145] = names.get(".notdef");     //221
        symbol[146] = names.get(".notdef");     //222
        symbol[147] = names.get(".notdef");     //223
        symbol[148] = names.get(".notdef");     //224
        symbol[149] = names.get(".notdef");     //225
        symbol[150] = names.get(".notdef");     //226
        symbol[151] = names.get(".notdef");     //227
        symbol[152] = names.get(".notdef");     //230
        symbol[153] = names.get(".notdef");     //231
        symbol[154] = names.get(".notdef");     //232
        symbol[155] = names.get(".notdef");     //233
        symbol[156] = names.get(".notdef");     //234
        symbol[157] = names.get(".notdef");     //235
        symbol[158] = names.get(".notdef");     //236
        symbol[159] = names.get(".notdef");     //237
        symbol[160] = names.get("Euro");        //240
        symbol[161] = names.get("Upsilon1");    //241
        symbol[162] = names.get("minute");      //242
        symbol[163] = names.get("lessequal");   //243
        symbol[164] = names.get("fraction");    //244
        symbol[165] = names.get("infinity");    //245
        symbol[166] = names.get("florin");      //246
        symbol[167] = names.get("club");        //247
        symbol[168] = names.get("diamond");     //250
        symbol[169] = names.get("heart");       //251
        symbol[170] = names.get("spade");       //252
        symbol[171] = names.get("arrowboth");   //253
        symbol[172] = names.get("arrowleft");   //254
        symbol[173] = names.get("arrowup");     //255
        symbol[174] = names.get("arrowright");  //256
        symbol[175] = names.get("arrowdown");   //257
        symbol[176] = names.get("degree");      //260
        symbol[177] = names.get("plusminus");   //261
        symbol[178] = names.get("second");       //262
        symbol[179] = names.get("greaterequal"); //263
        symbol[180] = names.get("multiply");     //264
        symbol[181] = names.get("proportional"); //265
        symbol[182] = names.get("partialdiff");  //266
        symbol[183] = names.get("bullet");       //267
        symbol[184] = names.get("divide");       //270
        symbol[185] = names.get("notequal");     //271
        symbol[186] = names.get("equivalence");  //272
        symbol[187] = names.get("approxequal");  //273
        symbol[188] = names.get("ellipsis");     //274
        symbol[189] = names.get("arrowvertex");  //275        
        symbol[190] = names.get("arrowhorizex"); //276
        symbol[191] = names.get("carriagereturn"); //277
        symbol[192] = names.get("aleph");        //300
        symbol[193] = names.get("Ifraktur");     //301
        symbol[194] = names.get("Rfraktur");     //302
        symbol[195] = names.get("weierstrass");  //303
        symbol[196] = names.get("circlemultiply"); //304
        symbol[197] = names.get("circleplus");   //305
        symbol[198] = names.get("emptyset");     //306
        symbol[199] = names.get("intersection"); //307
        symbol[200] = names.get("union");        //310
        symbol[201] = names.get("propersuperset"); //311
        symbol[202] = names.get("reflexsuperset"); //312
        symbol[203] = names.get("notsubset");    //313
        symbol[204] = names.get("propersubset"); //314
        symbol[205] = names.get("reflexsubset"); //315
        symbol[206] = names.get("element");      //316
        symbol[207] = names.get("notelement");   //317
        symbol[208] = names.get("angle");        //320
        symbol[209] = names.get("gradient");     //321
        symbol[210] = names.get("registerserif"); //322
        symbol[211] = names.get("copyrightserif"); //323
        symbol[212] = names.get("trademarkserif"); //324
        symbol[213] = names.get("product");       //325
        symbol[214] = names.get("radical");       //326
        symbol[215] = names.get("dotmath");       //327
        symbol[216] = names.get("logicalnot");    //330
        symbol[217] = names.get("logicaland");    //331
        symbol[218] = names.get("logicalor");     //332
        symbol[219] = names.get("arrowdblboth");  //333
        symbol[220] = names.get("arrowdblleft");  //334
        symbol[221] = names.get("arrowdblup");    //335
        symbol[222] = names.get("arrowdblright"); //336
        symbol[223] = names.get("arrowdbldown");  //337
        symbol[224] = names.get("lozenge");       //340
        symbol[225] = names.get("angleleft");     //341
        symbol[226] = names.get("registersans");  //342
        symbol[227] = names.get("copyrightsans"); //343
        symbol[228] = names.get("trademarksans"); //344
        symbol[229] = names.get("summation");     //345
        symbol[230] = names.get("parenlefttp");   //346
        symbol[231] = names.get("parenleftex");   //347
        symbol[232] = names.get("parenleftbt");   //350
        symbol[233] = names.get("bracketlefttp"); //351
        symbol[234] = names.get("bracketleftex"); //352
        symbol[235] = names.get("bracketleftbt"); //353
        symbol[236] = names.get("bracelefttp");   //354
        symbol[237] = names.get("braceleftmid");  //355
        symbol[238] = names.get("braceleftbt");   //356
        symbol[239] = names.get("braceex");       //357
        symbol[240] = names.get(".notdef");       //360
        symbol[241] = names.get("angleright");    //361
        symbol[242] = names.get("integral");      //362
        symbol[243] = names.get("integraltp");    //363
        symbol[244] = names.get("integralex");    //364
        symbol[245] = names.get("integralbt");    //365
        symbol[246] = names.get("parenrighttp");  //366
        symbol[247] = names.get("parenrightex");  //367
        symbol[248] = names.get("parenrightbt");  //370
        symbol[249] = names.get("bracketrighttp"); //371
        symbol[250] = names.get("bracketrightex"); //372
        symbol[251] = names.get("bracketrightbt"); //373
        symbol[252] = names.get("bracerighttp");  //374
        symbol[253] = names.get("bracerightmid"); //375
        symbol[254] = names.get("bracerightbt");  //376
        symbol[255] = names.get(".notdef");       //377
    }
    
}
