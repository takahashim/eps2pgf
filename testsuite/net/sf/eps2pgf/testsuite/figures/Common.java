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

package net.sf.eps2pgf.testsuite.figures;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import net.sf.eps2pgf.Converter;
import net.sf.eps2pgf.Options;
import net.sf.eps2pgf.ProgramError;

/**
 * Utility class for PostScript figure tests.
 */
public final class Common {
    
    /** Base directory of test suite. */
    private static File testSuiteDir;
    
    /** Working directory for conversion process. */
    private static File workDir;
    
    /** Directory with test figures. */
    private static File figureDir;
    
    /** GhostScript command. */
    private static File gs;
    
    /** Name of test suite directory. */
    private static final String TESTSUITE_DIRNAME = "testsuite";
    
    /** Name of directory with test figures. */
    private static final String TESTFIGURES_DIRNAME = "test_figures";
    
    /** Name of working directory. */
    private static final String WORK_DIRNAME = "workdir";
    
    /**
     * "Hidden" constructor.
     */
    private Common() {
        /* empty block */
    }
    
    /**
     * Search the base directory of the test suite.
     * 
     * @return The base directory of the test suite.
     * 
     * @throws FileNotFoundException Unable to find the test suite directory.
     */
    private static File findTestSuiteDir() throws FileNotFoundException {
        // First search the current directory.
        File userDir = new File(System.getProperty("user.dir"));

        // ..., or relative to the class path
        String fullClassPath = System.getProperty("java.class.path");
        int index = fullClassPath.indexOf(';');
        File classPath;
        if (index == -1) {
            classPath = new File(fullClassPath);
        } else {
            classPath = new File(fullClassPath.substring(0, index));
        }
        if (!(classPath.isAbsolute())) {
            classPath = new File(userDir, classPath.getPath()); 
        }
        if (classPath.isFile()) {
            classPath = classPath.getParentFile();
        }
        
        File dir = new File(userDir, TESTSUITE_DIRNAME);
        if (dir.exists()) {
            return dir;
        }
        
        dir = new File(classPath, TESTSUITE_DIRNAME);
        if (dir.exists()) {
            return dir;
        }
        
        dir = new File(userDir.getParentFile(), TESTSUITE_DIRNAME);
        if (dir.exists()) {
            return dir;
        }
        
        dir = new File(classPath.getParentFile(), TESTSUITE_DIRNAME);
        if (dir.exists()) {
            return dir;
        }
        
        throw new FileNotFoundException("Unable to find test suite directory.");
    }
    
    /**
     * Search for the figure directory.
     * 
     * @return The directory with test figures.
     * 
     * @throws FileNotFoundException Unable to find directory.
     */
    private static File findFigureDir() throws FileNotFoundException {
        if (testSuiteDir == null) {
            testSuiteDir = findTestSuiteDir();
        }
        
        File dir = new File(testSuiteDir, TESTFIGURES_DIRNAME);
        if (!dir.exists()) {
            throw new FileNotFoundException("Unable to find test figures"
                    + " directory.");
        }
        
        return dir;
    }
    
    /**
     * Find Ghostscript executable.
     * 
     * @return the file
     * 
     * @throws FileNotFoundException the file not found exception
     */
    private static File findGhostscript() throws FileNotFoundException {
        String[] gsCmds = {"gs", "gswin32c"};
        
        // First, we try to see if it is already available on the path
        for (String gsCmd : gsCmds) {
            try {
                Runtime runtime = Runtime.getRuntime();
                String[] cmdArray = {gsCmd, "--version"};
                Process process = runtime.exec(cmdArray);
                process.waitFor();
                if (process.exitValue() == 0) {
                    return new File(gsCmd);
                }
            } catch (IOException e) {
                /* empty block */
            } catch (InterruptedException e) {
                /* empty block */
            }
        }
        
        boolean isWin = System.getProperty("os.name").toLowerCase()
                                                               .contains("win");
        if (isWin) {
            // This is Windows, try to find Ghostscript in Program Files
            File programFiles = new File("C:", "Program Files");
            File gsDir = new File(programFiles, "gs");
            File gsCheck = new File(new File(gsDir, "bin"), "gswin32c.exe");
            if (gsCheck.exists()) {
                return gsCheck;
            }
            
            File[] files = gsDir.listFiles();
            for (File file : files) {
                if (file.getName().matches("gs[0-9\\.]*")) {
                    gsCheck = new File(new File(file, "bin"), "gswin32c.exe");
                    if (gsCheck.exists()) {
                        return gsCheck;
                    }
                }
            }
        }
        
        throw new FileNotFoundException("Unable to locate Ghostscript.");
    }
    
    /**
     * Determine the working directory and make sure it exists.
     * 
     * @return The working directory.
     * 
     * @throws FileNotFoundException Unable to find directory.
     */
    private static File findAndCreateWorkDir() throws FileNotFoundException {
        if (testSuiteDir == null) {
            testSuiteDir = findTestSuiteDir();
        }
        
        File dir = new File(testSuiteDir, WORK_DIRNAME);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new FileNotFoundException("Unable to create working"
                        + " directory (" + dir + ").");
            }
        }
        
        return dir;
    }

    /**
     * Test a single PostScript figure.
     * 
     * @param conv Converter that will be used to convert the figure.
     * @param figureFilename Filename of the figure to test.
     * 
     * @return true, if test figure
     * 
     * @throws Exception the exception
     */
    public static boolean testFigure(final Converter conv,
            final String figureFilename) throws Exception {
        
        if (figureDir == null) {
            figureDir = findFigureDir();
        }
        if (workDir == null) {
            workDir = findAndCreateWorkDir();
        }
        
        File inputFile = new File(figureDir, figureFilename);
        if (!inputFile.exists()) {
            throw new FileNotFoundException("Unable to find figure ("
                    + inputFile + ").");
        }
        
        int index = figureFilename.lastIndexOf('.');
        String baseFilename;
        if (index > 0) {
            baseFilename = figureFilename.substring(0, index);
        } else {
            baseFilename = figureFilename;
        }
        
        // Replace %figureDir% in the text-replace file
        Options opts = conv.getOpts();
        File textReplaceFile = opts.getTextreplacefile();
        if (textReplaceFile != null) {
            String textReplace = textReplaceFile.getPath();
            textReplace = textReplace.replace("%figureDir%",
                    figureDir.getAbsolutePath());
            opts.setTextreplacefile(new File(textReplace));
        }
        
        // Convert the PostScript figure to pgf
        File pgfFile = new File(workDir, baseFilename + ".pgf");
        convertEps2pgf(conv, inputFile, pgfFile);
        
        // Compile the pgf with PDFLaTeX to produce a pdf
        File texFile = new File(workDir, baseFilename + ".tex");
        if (!convertPgf2pdf(pgfFile, texFile)) {
            throw new ProgramError("TeX returned errors.");
        }
        
        // Delete temporary files.
        if (!texFile.delete()) {
            throw new IOException("Unable to delete temporary TeX file ("
                    + texFile + ").");
        }
        if (!pgfFile.delete()) {
            throw new IOException("Unable to delete PGF file ("
                    + pgfFile + ").");
        }
        File auxFile = new File(workDir, baseFilename + ".aux");
        if (!auxFile.delete()) {
            throw new IOException("Unable to delete AUX file ("
                    + auxFile + ").");
        }
        File logFile = new File(workDir, baseFilename + ".log");
        if (!logFile.delete()) {
            throw new IOException("Unable to delete LOG file ("
                    + logFile + ").");
        }        
        

        // Convert the pdf to and png
        File pdfFile = new File(workDir, baseFilename + ".pdf");
        File fullPngFile = new File(workDir, baseFilename + "_full.png");
        if (!convertPdf2bitmap(pdfFile, fullPngFile)) {
            throw new ProgramError("Failed to convert PDF to PNG.");
        }
        
        // Crop the png
        BufferedImage fullImg = ImageIO.read(fullPngFile);
        BufferedImage img = cropImage(fullImg);
        File pngFile = new File(workDir, baseFilename + ".png");
        if (!ImageIO.write(img, "png", pngFile)) {
            throw new IOException("Unable to write cropped PNG file ("
                    + pngFile + ").");
        }
        if (!fullPngFile.delete()) {
            throw new IOException("Unable to delete full PNG file ("
                    + fullPngFile + ").");
        }
        
        // Load the reference rendering
        BufferedImage refImg;
        try {
            File refImageFile = new File(figureDir, baseFilename + ".png");
            refImg = ImageIO.read(refImageFile);
        } catch (IIOException e) {
            // Unable to load the image, use a black image as reference
            refImg = new BufferedImage(img.getWidth(), img.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
        }
        
        // Compare (cropped) png with reference rendering.
        BufferedImage diffImg = new BufferedImage(refImg.getWidth(),
                refImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        int maxDiff = diffImages(img, refImg, diffImg);
        
        File diffImgFile = new File(workDir, baseFilename + "_error.png");
        if (maxDiff > 0) {
            ImageIO.write(diffImg, "png", diffImgFile);            
        } else if (diffImgFile.exists()) {
            if (!diffImgFile.delete()) {
                throw new IOException("Unable to delete old error PNG file ("
                        + diffImgFile + ").");
            }
        }
        
        // Remove the (cropped) png file
        if (!pngFile.delete()) {
            throw new IOException("Unable to delete PNG file ("
                    + pngFile + ").");
        }
        
        return (maxDiff == 0);
    }
    
    /**
     * Convert a PostScript figure to pgf.
     * 
     * @param conv The converter.
     * @param in The input file.
     * @param out The output file.
     * 
     * @throws Exception the exception
     */
    private static void convertEps2pgf(final Converter conv, final File in,
            final File out) throws Exception {
        
        Options opts = conv.getOpts();
        opts.setInputFile(in);
        opts.setOutputFile(out);
        conv.convert();
    }
    
    /**
     * Convert a pgf file to pdf using PDFLaTeX to compile a test document with
     * the pgf figure.
     * 
     * @param pgfFile The pgf file.
     * @param texFile The tex file.
     * 
     * @return true, if convert pgf2pdf
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    private static boolean convertPgf2pdf(final File pgfFile,
            final File texFile) throws IOException, InterruptedException {
        
        // Create the LaTeX file
        Writer writer = new FileWriter(texFile);
        writer.write("\\documentclass{article}\n");
        writer.write("\\usepackage{lmodern}\n");
        writer.write("\\usepackage[T1]{fontenc}\n");
        writer.write("\\usepackage{amssymb}\n");
        writer.write("\\usepackage{pifont}\n");
        writer.write("\\usepackage{textcomp}\n");
        writer.write("\\usepackage{tikz}\n");
        writer.write("\\pagestyle{empty}\n");
        writer.write("\\begin{document}\n");
        writer.write("\\input{" + pgfFile.getAbsolutePath().replace('\\', '/')
                + "}\n");
        writer.write("\\end{document}\n");
        writer.close();
        
        String[] commandArray = {"pdflatex", "-interaction=batchmode",
                texFile.getAbsolutePath()};
        File compileDir = texFile.getParentFile();
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandArray, null, compileDir);
        process.waitFor();
        return (process.exitValue() == 0);
    }
    
    /**
     * Convert a pdf file to a bitmap file.
     * 
     * @param pdfFile The pdf file.
     * @param bitmapFile The bitmap file.
     * 
     * @return True, if conersion went fine.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    private static boolean convertPdf2bitmap(final File pdfFile,
            final File bitmapFile) throws IOException, InterruptedException {
        
        if (gs == null) {
            gs = findGhostscript();
        }
        
        String[] commandArray = {gs.getPath(),
                "-dSAFER", "-dBATCH", "-dNOPAUSE",
                "-r300", "-sDEVICE=png16m",
                "-dTextAlphaBits=1", "-dGraphicsAlphaBits=1",
                "-sOutputFile=" + bitmapFile.getAbsolutePath(),
                pdfFile.getAbsolutePath()};
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(commandArray, null,
                bitmapFile.getParentFile());
        process.waitFor();
        return (process.exitValue() == 0);
    }
    
    /**
     * Automatically crop an image. All surrounding whitespace is removed.
     * 
     * @param img The image.
     * 
     * @return The cropped image
     */
    private static BufferedImage cropImage(final BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        final int white = -1;
        
        int top = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                if (color != white) {
                    top = y;
                    break;
                }
            }
            if (top != -1) {
                break;
            }
        }
        
        int bottom = -1;
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int color = img.getRGB(x, y);
                if (color != white) {
                    bottom = y;
                    break;
                }
            }
            if (bottom != -1) {
                break;
            }
        }
        
        int left = -1;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = img.getRGB(x, y);
                if (color != white) {
                    left = x;
                    break;
                }
            }
            if (left != -1) {
                break;
            }
        }
        
        int right = -1;
        for (int x = width - 1; x >= 0; x--) {
            for (int y = 0; y < height; y++) {
                int color = img.getRGB(x, y);
                if (color != white) {
                    right = x;
                    break;
                }
            }
            if (right != -1) {
                break;
            }
        }
        
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
    
    /**
     * Determine the difference between two images.
     * 
     * @param img1 The first image.
     * @param img2 The second image.
     * @param ptrDiffImage The difference between image 1 and 2 will be stored
     * in this image.
     * 
     * @return The maximum difference between image 1 and image 2.
     */
    private static int diffImages(final BufferedImage img1,
            final BufferedImage img2, final BufferedImage ptrDiffImage) {
        
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());
        int maxDiff = 0;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color1 = img1.getRGB(x, y);
                int color2 = img2.getRGB(x, y);
                int diff = Math.abs(color1 - color2);
                maxDiff = Math.max(maxDiff, diff);
                ptrDiffImage.setRGB(x, y, diff);
            }
        }
        
        return maxDiff;
    }
}
