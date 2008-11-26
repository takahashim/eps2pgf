/*
 * This file is part of Eps2pgf.
 *
 * Copyright 2007-2008 Paul Wagenaars <paul@wagenaars.org>
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

package net.sf.eps2pgf.ps.resources.encodings;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectName;

/**
 * Defines the standard encoding vector.
 * 
 * @author Paul Wagenaars
 *
 */
public final class ISOLatin1Encoding {
    
    /** Encoding vector. */
    private static PSObjectArray vector = null;
    
    /**
     * "Hidden" constructor.
     */
    private ISOLatin1Encoding() {
        /* empty block */
    }
    
    /**
     * Initialize the encoding vector.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private static void initializeVector() throws ProgramError {
        vector = new PSObjectArray(256);
        try {
            vector.set(0, EncodingManager.SYMB_NOTDEF);     //000
            vector.set(1, EncodingManager.SYMB_NOTDEF);     //001
            vector.set(2, EncodingManager.SYMB_NOTDEF);     //002
            vector.set(3, EncodingManager.SYMB_NOTDEF);     //003
            vector.set(4, EncodingManager.SYMB_NOTDEF);     //004
            vector.set(5, EncodingManager.SYMB_NOTDEF);     //005
            vector.set(6, EncodingManager.SYMB_NOTDEF);     //006
            vector.set(7, EncodingManager.SYMB_NOTDEF);     //007
            vector.set(8, EncodingManager.SYMB_NOTDEF);     //010
            vector.set(9, EncodingManager.SYMB_NOTDEF);     //011
            vector.set(10, EncodingManager.SYMB_NOTDEF);     //012
            vector.set(11, EncodingManager.SYMB_NOTDEF);     //013
            vector.set(12, EncodingManager.SYMB_NOTDEF);     //014
            vector.set(13, EncodingManager.SYMB_NOTDEF);     //015
            vector.set(14, EncodingManager.SYMB_NOTDEF);     //016
            vector.set(15, EncodingManager.SYMB_NOTDEF);     //017
            vector.set(16, EncodingManager.SYMB_NOTDEF);     //020
            vector.set(17, EncodingManager.SYMB_NOTDEF);     //021
            vector.set(18, EncodingManager.SYMB_NOTDEF);     //022
            vector.set(19, EncodingManager.SYMB_NOTDEF);     //023
            vector.set(20, EncodingManager.SYMB_NOTDEF);     //024
            vector.set(21, EncodingManager.SYMB_NOTDEF);     //025
            vector.set(22, EncodingManager.SYMB_NOTDEF);     //026
            vector.set(23, EncodingManager.SYMB_NOTDEF);     //027
            vector.set(24, EncodingManager.SYMB_NOTDEF);     //030
            vector.set(25, EncodingManager.SYMB_NOTDEF);     //031
            vector.set(26, EncodingManager.SYMB_NOTDEF);     //032
            vector.set(27, EncodingManager.SYMB_NOTDEF);     //033
            vector.set(28, EncodingManager.SYMB_NOTDEF);     //034
            vector.set(29, EncodingManager.SYMB_NOTDEF);     //035
            vector.set(30, EncodingManager.SYMB_NOTDEF);     //036
            vector.set(31, EncodingManager.SYMB_NOTDEF);     //037
            vector.set(32, new PSObjectName("/space"));       //040
            vector.set(33, new PSObjectName("/exclam"));      //041
            vector.set(34, new PSObjectName("/quotedbl"));    //042
            vector.set(35, new PSObjectName("/numbersign"));  //043
            vector.set(36, new PSObjectName("/dollar"));      //044
            vector.set(37, new PSObjectName("/percent"));     //045
            vector.set(38, new PSObjectName("/ampersand"));   //046
            vector.set(39, new PSObjectName("/quoteright"));  //047
            vector.set(40, new PSObjectName("/parenleft"));   //050
            vector.set(41, new PSObjectName("/parenright"));  //051
            vector.set(42, new PSObjectName("/asterisk"));    //052
            vector.set(43, new PSObjectName("/plus"));        //053
            vector.set(44, new PSObjectName("/comma"));       //054
            vector.set(45, new PSObjectName("/minus"));       //055
            vector.set(46, new PSObjectName("/period"));      //056
            vector.set(47, new PSObjectName("/slash"));       //057
            vector.set(48, new PSObjectName("/zero"));        //060
            vector.set(49, new PSObjectName("/one"));         //061
            vector.set(50, new PSObjectName("/two"));         //062
            vector.set(51, new PSObjectName("/three"));       //063
            vector.set(52, new PSObjectName("/four"));        //064
            vector.set(53, new PSObjectName("/five"));        //065
            vector.set(54, new PSObjectName("/six"));         //066
            vector.set(55, new PSObjectName("/seven"));       //067
            vector.set(56, new PSObjectName("/eight"));       //070
            vector.set(57, new PSObjectName("/nine"));        //071
            vector.set(58, new PSObjectName("/colon"));       //072
            vector.set(59, new PSObjectName("/semicolon"));   //073
            vector.set(60, new PSObjectName("/less"));        //074
            vector.set(61, new PSObjectName("/equal"));       //075
            vector.set(62, new PSObjectName("/greater"));     //076
            vector.set(63, new PSObjectName("/question"));    //077
            vector.set(64, new PSObjectName("/at"));          //100
            vector.set(65, new PSObjectName("/A"));           //101
            vector.set(66, new PSObjectName("/B"));           //102
            vector.set(67, new PSObjectName("/C"));           //103
            vector.set(68, new PSObjectName("/D"));           //104
            vector.set(69, new PSObjectName("/E"));           //105
            vector.set(70, new PSObjectName("/F"));           //106
            vector.set(71, new PSObjectName("/G"));           //107
            vector.set(72, new PSObjectName("/H"));           //110
            vector.set(73, new PSObjectName("/I"));           //111
            vector.set(74, new PSObjectName("/J"));           //112
            vector.set(75, new PSObjectName("/K"));           //113
            vector.set(76, new PSObjectName("/L"));           //114
            vector.set(77, new PSObjectName("/M"));           //115
            vector.set(78, new PSObjectName("/N"));           //116
            vector.set(79, new PSObjectName("/O"));           //117
            vector.set(80, new PSObjectName("/P"));           //120
            vector.set(81, new PSObjectName("/Q"));           //121
            vector.set(82, new PSObjectName("/R"));           //122
            vector.set(83, new PSObjectName("/S"));           //123
            vector.set(84, new PSObjectName("/T"));           //124
            vector.set(85, new PSObjectName("/U"));           //125
            vector.set(86, new PSObjectName("/V"));           //126
            vector.set(87, new PSObjectName("/W"));           //127
            vector.set(88, new PSObjectName("/X"));           //130
            vector.set(89, new PSObjectName("/Y"));           //131
            vector.set(90, new PSObjectName("/Z"));           //132
            vector.set(91, new PSObjectName("/bracketleft")); //133
            vector.set(92, new PSObjectName("/backslash"));   //134
            vector.set(93, new PSObjectName("/bracketright")); //135
            vector.set(94, new PSObjectName("/asciicircum")); //136
            vector.set(95, new PSObjectName("/underscore"));  //137
            vector.set(96, new PSObjectName("/quoteleft"));   //140
            vector.set(97, new PSObjectName("/a"));           //141
            vector.set(98, new PSObjectName("/b"));           //142
            vector.set(99, new PSObjectName("/c"));           //143
            vector.set(100, new PSObjectName("/d"));           //144
            vector.set(101, new PSObjectName("/e"));           //145
            vector.set(102, new PSObjectName("/f"));           //146
            vector.set(103, new PSObjectName("/g"));           //147
            vector.set(104, new PSObjectName("/h"));           //150
            vector.set(105, new PSObjectName("/i"));           //151
            vector.set(106, new PSObjectName("/j"));           //152
            vector.set(107, new PSObjectName("/k"));           //153
            vector.set(108, new PSObjectName("/l"));           //154
            vector.set(109, new PSObjectName("/m"));           //155
            vector.set(110, new PSObjectName("/n"));           //156
            vector.set(111, new PSObjectName("/o"));           //157
            vector.set(112, new PSObjectName("/p"));           //160
            vector.set(113, new PSObjectName("/q"));           //161
            vector.set(114, new PSObjectName("/r"));           //162
            vector.set(115, new PSObjectName("/s"));           //163
            vector.set(116, new PSObjectName("/t"));           //164
            vector.set(117, new PSObjectName("/u"));           //165
            vector.set(118, new PSObjectName("/v"));           //166
            vector.set(119, new PSObjectName("/w"));           //167
            vector.set(120, new PSObjectName("/x"));           //170
            vector.set(121, new PSObjectName("/y"));           //171
            vector.set(122, new PSObjectName("/z"));           //172
            vector.set(123, new PSObjectName("/braceleft"));   //173
            vector.set(124, new PSObjectName("/bar"));         //174
            vector.set(125, new PSObjectName("/braceright"));  //175
            vector.set(126, new PSObjectName("/asciitilde"));  //176
            vector.set(127, EncodingManager.SYMB_NOTDEF);     //177
            vector.set(128, EncodingManager.SYMB_NOTDEF);     //200
            vector.set(129, EncodingManager.SYMB_NOTDEF);     //201
            vector.set(130, EncodingManager.SYMB_NOTDEF);     //202
            vector.set(131, EncodingManager.SYMB_NOTDEF);     //203
            vector.set(132, EncodingManager.SYMB_NOTDEF);     //204
            vector.set(133, EncodingManager.SYMB_NOTDEF);     //205
            vector.set(134, EncodingManager.SYMB_NOTDEF);     //206
            vector.set(135, EncodingManager.SYMB_NOTDEF);     //207
            vector.set(136, EncodingManager.SYMB_NOTDEF);     //210
            vector.set(137, EncodingManager.SYMB_NOTDEF);     //211
            vector.set(138, EncodingManager.SYMB_NOTDEF);     //212
            vector.set(139, EncodingManager.SYMB_NOTDEF);     //213
            vector.set(140, EncodingManager.SYMB_NOTDEF);     //214
            vector.set(141, EncodingManager.SYMB_NOTDEF);     //215
            vector.set(142, EncodingManager.SYMB_NOTDEF);     //216
            vector.set(143, EncodingManager.SYMB_NOTDEF);     //217
            vector.set(144, new PSObjectName("/dotlessi"));    //220
            vector.set(145, new PSObjectName("/grave"));       //221
            vector.set(146, new PSObjectName("/acute"));       //222
            vector.set(147, new PSObjectName("/circumflex"));  //223
            vector.set(148, new PSObjectName("/tilde"));       //224
            vector.set(149, new PSObjectName("/macron"));      //225
            vector.set(150, new PSObjectName("/breve"));       //226
            vector.set(151, new PSObjectName("/dotaccent"));   //227
            vector.set(152, new PSObjectName("/dieresis"));    //230
            vector.set(153, EncodingManager.SYMB_NOTDEF);     //231
            vector.set(154, new PSObjectName("/ring"));        //232
            vector.set(155, new PSObjectName("/cedilla"));     //233
            vector.set(156, EncodingManager.SYMB_NOTDEF);     //234
            vector.set(157, new PSObjectName("/hungarumlaut")); //235
            vector.set(158, new PSObjectName("/ogonek"));      //236
            vector.set(159, new PSObjectName("/caron"));       //237
            vector.set(160, new PSObjectName("/space"));       //240
            vector.set(161, new PSObjectName("/exclamdown"));  //241
            vector.set(162, new PSObjectName("/cent"));        //242
            vector.set(163, new PSObjectName("/sterling"));    //243
            vector.set(164, new PSObjectName("/currency"));    //244
            vector.set(165, new PSObjectName("/yen"));         //245
            vector.set(166, new PSObjectName("/brokenbar"));   //246
            vector.set(167, new PSObjectName("/section"));     //247
            vector.set(168, new PSObjectName("/dieresis"));    //250
            vector.set(169, new PSObjectName("/copyright"));   //251
            vector.set(170, new PSObjectName("/ordfeminine")); //252
            vector.set(171, new PSObjectName("/guillemotleft")); //253
            vector.set(172, new PSObjectName("/logicalnot"));  //254
            vector.set(173, new PSObjectName("/hyphen"));      //255
            vector.set(174, new PSObjectName("/registered"));  //256
            vector.set(175, new PSObjectName("/macron"));      //257
            vector.set(176, new PSObjectName("/degree"));      //260
            vector.set(177, new PSObjectName("/plusminus"));   //261
            vector.set(178, new PSObjectName("/twosuperior")); //262
            vector.set(179, new PSObjectName("/threesuperior")); //263
            vector.set(180, new PSObjectName("/acute"));       //264
            vector.set(181, new PSObjectName("/mu"));          //265
            vector.set(182, new PSObjectName("/paragraph"));   //266
            vector.set(183, new PSObjectName("/periodcentered")); //267
            vector.set(184, new PSObjectName("/cedilla"));     //270
            vector.set(185, new PSObjectName("/onesuperior")); //271
            vector.set(186, new PSObjectName("/ordmasculine")); //272
            vector.set(187, new PSObjectName("/guillemotright")); //273
            vector.set(188, new PSObjectName("/onequarter"));  //274
            vector.set(189, new PSObjectName("/onehalf"));     //275        
            vector.set(190, new PSObjectName("/threequarters")); //276        
            vector.set(191, new PSObjectName("/questiondown")); //277
            vector.set(192, new PSObjectName("/Agrave"));      //300
            vector.set(193, new PSObjectName("/Aacute"));      //301
            vector.set(194, new PSObjectName("/Acircumflex")); //302
            vector.set(195, new PSObjectName("/Atilde"));      //303
            vector.set(196, new PSObjectName("/Adieresis"));   //304
            vector.set(197, new PSObjectName("/Aring"));       //305
            vector.set(198, new PSObjectName("/AE"));          //306
            vector.set(199, new PSObjectName("/Ccedilla"));    //307
            vector.set(200, new PSObjectName("/Egrave"));      //310
            vector.set(201, new PSObjectName("/Eacute"));      //311
            vector.set(202, new PSObjectName("/Ecircumflex")); //312
            vector.set(203, new PSObjectName("/Edieresis"));   //313
            vector.set(204, new PSObjectName("/Igrave"));      //314
            vector.set(205, new PSObjectName("/Iacute"));      //315
            vector.set(206, new PSObjectName("/Icircumflex")); //316
            vector.set(207, new PSObjectName("/Idieresis"));   //317
            vector.set(208, new PSObjectName("/Eth"));         //320
            vector.set(209, new PSObjectName("/Ntilde"));      //321
            vector.set(210, new PSObjectName("/Ograve"));      //322
            vector.set(211, new PSObjectName("/Oacute"));      //323
            vector.set(212, new PSObjectName("/Ocircumflex")); //324
            vector.set(213, new PSObjectName("/Otilde"));      //325
            vector.set(214, new PSObjectName("/Odieresis"));   //326
            vector.set(215, new PSObjectName("/multiply"));    //327
            vector.set(216, new PSObjectName("/Oslash"));      //330
            vector.set(217, new PSObjectName("/Ugrave"));      //331
            vector.set(218, new PSObjectName("/Uacute"));      //332
            vector.set(219, new PSObjectName("/Ucircumflex")); //333
            vector.set(220, new PSObjectName("/Udieresis"));   //334
            vector.set(221, new PSObjectName("/Yacute"));      //335
            vector.set(222, new PSObjectName("/Thorn"));       //336
            vector.set(223, new PSObjectName("/germandbls"));  //337
            vector.set(224, new PSObjectName("/agrave"));      //340
            vector.set(225, new PSObjectName("/aacute"));      //341
            vector.set(226, new PSObjectName("/acircumflex")); //342
            vector.set(227, new PSObjectName("/atilde"));      //343
            vector.set(228, new PSObjectName("/adieresis"));   //344
            vector.set(229, new PSObjectName("/aring"));       //345
            vector.set(230, new PSObjectName("/ae"));          //346
            vector.set(231, new PSObjectName("/ccedilla"));    //347
            vector.set(232, new PSObjectName("/egrave"));      //350
            vector.set(233, new PSObjectName("/eacute"));      //351
            vector.set(234, new PSObjectName("/ecircumflex")); //352
            vector.set(235, new PSObjectName("/edieresis"));   //353
            vector.set(236, new PSObjectName("/igrave"));      //354
            vector.set(237, new PSObjectName("/iacute"));      //355
            vector.set(238, new PSObjectName("/icircumflex")); //356
            vector.set(239, new PSObjectName("/idieresis"));   //357
            vector.set(240, new PSObjectName("/eth"));         //360
            vector.set(241, new PSObjectName("/ntilde"));      //361
            vector.set(242, new PSObjectName("/ograve"));      //362
            vector.set(243, new PSObjectName("/oacute"));      //363
            vector.set(244, new PSObjectName("/ocircumflex")); //364
            vector.set(245, new PSObjectName("/otilde"));      //365
            vector.set(246, new PSObjectName("/odieresis"));   //366
            vector.set(247, new PSObjectName("/divide"));      //367
            vector.set(248, new PSObjectName("/oslash"));      //370
            vector.set(249, new PSObjectName("/ugrave"));      //371
            vector.set(250, new PSObjectName("/uacute"));      //372
            vector.set(251, new PSObjectName("/ucircumflex")); //373
            vector.set(252, new PSObjectName("/udieresis"));   //374
            vector.set(253, new PSObjectName("/yacute"));      //375
            vector.set(254, new PSObjectName("/thorn"));       //376
            vector.set(255, new PSObjectName("/ydieresis"));   //377
        } catch (PSErrorRangeCheck e) {
            throw new ProgramError("A range check error occurred where it"
                    + " shouldn't.");
        }
    }
    
    /**
     * Gets the encoding vector.
     * 
     * @return The vector.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public static PSObjectArray get() throws ProgramError {
        if (vector == null) {
            initializeVector();
        }
        
        return vector;
    }
    
}
