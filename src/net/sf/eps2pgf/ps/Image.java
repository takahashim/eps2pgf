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

package net.sf.eps2pgf.ps;

import java.io.IOException;

import net.sf.eps2pgf.ProgramError;
import net.sf.eps2pgf.ps.errors.PSError;
import net.sf.eps2pgf.ps.errors.PSErrorIOError;
import net.sf.eps2pgf.ps.errors.PSErrorRangeCheck;
import net.sf.eps2pgf.ps.errors.PSErrorUnregistered;
import net.sf.eps2pgf.ps.objects.PSObject;
import net.sf.eps2pgf.ps.objects.PSObjectArray;
import net.sf.eps2pgf.ps.objects.PSObjectDict;
import net.sf.eps2pgf.ps.objects.PSObjectFile;
import net.sf.eps2pgf.ps.objects.PSObjectMatrix;
import net.sf.eps2pgf.ps.objects.PSObjectName;
import net.sf.eps2pgf.ps.objects.PSObjectString;
import net.sf.eps2pgf.ps.resources.colors.PSColor;
import net.sf.eps2pgf.util.CloneMappings;

/**
 * Represents a bitmap image.
 */
public class Image {
    
    /** Key of ImageType entry in dictionary. */
    public static final PSObjectName IMAGE_TYPE =
        new PSObjectName("/ImageType");
    
    /** Key of Width entry in dictionary. */
    public static final PSObjectName WIDTH = new PSObjectName("/Width");
    
    /** Key of Height entry in dictionary. */
    public static final PSObjectName HEIGHT = new PSObjectName("/Height");
    
    /** Key of ImageMatrix entry in dictionary. */
    public static final PSObjectName IMAGE_MATRIX =
        new PSObjectName("/ImageMatrix");
    
    /** Key of MultipleDataSources entry in dictionary. */
    public static final PSObjectName MULTIPLE_DATA_SOURCES =
        new PSObjectName("/MultipleDataSources");
    
    /** Key of DataSource entry in dictionary. */
    public static final PSObjectName DATA_SOURCE =
        new PSObjectName("/DataSource");
    
    /** Key of BitsPerComponent entry in dictionary. */
    public static final PSObjectName BITS_PER_COMPONENT =
        new PSObjectName("/BitsPerComponent");
    
    /** Key of Decode entry in dictionary. */
    public static final PSObjectName DECODE =
        new PSObjectName("/Decode");
    
    /** Key of Interpolate entry in dictionary. */
    public static final PSObjectName INTERPOLATE =
        new PSObjectName("/Interpolate");
    
    /** (Eps2pgf specific) key of ColorSpace entry in dictionary. */
    public static final PSObjectName COLORSPACE =
        new PSObjectName("/ColorSpace");
    
    /** Index of lower-left corner. */
    private static final int LL = 0;
    
    /** Index of lower-right corner. */
    private static final int LR = 1;
    
    /** Index of upper-right corner. */
    private static final int UR = 2;
    
    /** Index if upper-left corner. */
    private static final int UL = 3;
    
    /** Width of image (in raw data stream) in pixels. */
    private int imgWidthPx;
    
    /** Height of image (in raw data stream) in pixels. */
    private int imgHeightPx;
    
    /** Width of image (in output) in pixels. */
    private int outputWidthPx;
    
    /** Height of image (in output) in pixels. */
    private int outputHeightPx;
    
    /** Describes how component values from data are mapped to their
     * corresponding values in the current color space. */
    private double[] decode;
    
    /** Interpolate between pixels. */
    private boolean interpolate;
    
    /** Matrix describing transformation from user space to image space. */
    private PSObjectMatrix imageMatrix;
    
    /** Transformation matrix from user space to device space. */
    private PSObjectMatrix ctm;
    
    /** ColorSpace of loaded image. It can be Gray, RGB or CMYK. If the source
     * image was in a different color space, it is converted to RGB. */
    private PSColor colorSpace;
    
    /** Number of bits per input value/component. */
    private int bitsPerComponent;
    
    /** Number of bytes per line (horizontal line in image space). */
    private int bytesPerLine;
    
    /** Raw image data. */
    private byte[] data;
    
    /**
     * List with coordinates (in pixels) of corners of image in image space.
     * i.e. LL = (0,0), LR = (width, 0), UR = (width, height), UL = (0,height) 
     */
    private double[][] imgBbox = new double[4][2];
    
    /**
     * List with coordinates (in device units) of image in device space.
     * Coordinates are transformed coordinates from imgBbox list.
     */
    private double[][] deviceBbox = new double[4][2];
    
    /**
     * List with mappings between corners of image as it appears in the output
     * and the corners of the image in image space (deviceBbox).
     * index LL: index in deviceBbox array of the lower-left corner as it
     * appears in the output, index LR: lower-right, index UR: upper-right, and
     * index UL: upper-left corner.
     */
    private int[] cornerMap = new int[4];
    
    /**
     * Angle between line lower-left-to-lower-right and horizontal line.
     * Definition of lower-left and lower-right corners follow cornerMap.
     */
    private double angle;
    
    /** Vector describing a single pixel along the "horizontal" axis. */
    private double[] vectorHor = new double[2];
    
    /** Vector describing a single pixel along the "vertical" axis. */
    private double[] vectorVert = new double[2];

    /**
     * Creates a new ImageHandler object.
     * 
     * @param dict The image dictionary.
     * @param interp Interpreter to which this image belongs.
     * @param pColorSpace The current color space.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    public Image(final PSObjectDict dict, final Interpreter interp,
            final PSColor pColorSpace) throws PSError, ProgramError {
        
        // Get some information from the graphics state.
        CloneMappings cloneMap = new CloneMappings();
        ctm = interp.getGstate().current().getCtm().clone(cloneMap);
        colorSpace = pColorSpace.clone(cloneMap);
        
        int imageType = dict.get(IMAGE_TYPE).toInt();
        try {
            switch (imageType) {
                case 1:
                    loadType1Image(dict, interp);
                    break;
                case 3:
                    throw new PSErrorUnregistered("Bitmap images of type 3.");
                    // break;
                case 4:
                    throw new PSErrorUnregistered("Bitmap images of type 4.");
                    // break;
                default:
                    throw new PSErrorRangeCheck();
            }
        } catch (IOException e) {
            throw new PSErrorIOError();
        }
    }
    
    /**
     * Loads type 1 bitmap.
     * 
     * @param dict The dictionary describing the image.
     * @param interp Interpreter to which this image belongs.
     * 
     * @throws PSError A PostScript error occurred.
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     */
    private void loadType1Image(final PSObjectDict dict,
            final  Interpreter interp) throws PSError, ProgramError,
            IOException {
        
        // Read bitmap dimensions
        imgWidthPx = dict.get(WIDTH).toInt();
        imgHeightPx = dict.get(HEIGHT).toInt();
        
        // Read pixel/color information
        int nrInputValues = colorSpace.getNrInputValues();
        PSObjectArray decodeArray = dict.get(DECODE).toArray();
        if (decodeArray.size() / 2 != nrInputValues) {
            throw new PSErrorRangeCheck();
        }
        decode = decodeArray.toDoubleArray();
        bitsPerComponent = dict.get(BITS_PER_COMPONENT).toInt();
        
        // Read info about how it is included in the PostScript document
        imageMatrix = dict.get(IMAGE_MATRIX).toMatrix();
        if (dict.known(INTERPOLATE)) {
            interpolate = dict.get(INTERPOLATE).toBool();
        } else {
            interpolate = false;
        }
        
        // Read the data
        boolean multipleSources;
        if (dict.known(MULTIPLE_DATA_SOURCES)) {
            multipleSources = dict.get(MULTIPLE_DATA_SOURCES).toBool();
        } else {
            multipleSources = false;
        }
        PSObject dataSource = dict.get(DATA_SOURCE);
        if (multipleSources) {
            throw new PSErrorUnregistered("Image from multiple data sources.");
        } else {
            if (dataSource instanceof PSObjectFile) {
                loadDataFromFile((PSObjectFile) dataSource, imgWidthPx,
                        imgHeightPx, nrInputValues);
            } else if (dataSource instanceof PSObjectString) {
                throw new PSErrorUnregistered("Reading (bitmap) image data"
                        + " from a string.");
            } else if (dataSource instanceof PSObjectArray) {
                if (multipleSources) {
                    throw new PSErrorUnregistered("Reading bitmap data from"
                            + " multiple data sources.");
                } else {
                    loadDataFromProcedure((PSObjectArray) dataSource,
                            imgWidthPx, imgHeightPx, nrInputValues, interp);
                }
            } else {
                throw new PSErrorUnregistered("Reading (bitmap) image data"
                        + " from other " + dataSource);
            }
        }
        
        // Determine the coordinates of bounding box in image and device space.
        imgBbox[LL][0] = 0.0;
        imgBbox[LL][1] = 0.0;
        imgBbox[LR][0] = imgWidthPx;
        imgBbox[LR][1] = 0.0;
        imgBbox[UR][0] = imgWidthPx;
        imgBbox[UR][1] = imgHeightPx;
        imgBbox[UL][0] = 0.0;
        imgBbox[UL][1] = imgHeightPx;
        deviceBbox[LL] = ctm.transform(imageMatrix.itransform(imgBbox[LL]));
        deviceBbox[LR] = ctm.transform(imageMatrix.itransform(imgBbox[LR]));
        deviceBbox[UR] = ctm.transform(imageMatrix.itransform(imgBbox[UR]));
        deviceBbox[UL] = ctm.transform(imageMatrix.itransform(imgBbox[UL]));
        
        // Determine how image in image space is mapped to device space
        int llIndex = findNearest(-1e10, -1e10, deviceBbox, true);
        int neighbour1 = ((llIndex - 1) + 4) % 4;
        int neighbour2 = (llIndex + 1) % 4;
        double dx1 = deviceBbox[neighbour1][0] - deviceBbox[llIndex][0];
        double dy1 = deviceBbox[neighbour1][1] - deviceBbox[llIndex][1];
        double dx2 = deviceBbox[neighbour2][0] - deviceBbox[llIndex][0];
        double dy2 = deviceBbox[neighbour2][1] - deviceBbox[llIndex][1];
        double angle1 = Math.atan2(dy1, dx1);
        double angle2 = Math.atan2(dy2, dx2);
        int lrIndex, urIndex, ulIndex;
        if ((Math.abs(angle1) - Math.abs(angle2)) < 1e-3) {
            angle = angle1;
            lrIndex = neighbour1;
            urIndex = ((llIndex - 2) + 4) % 4;
            ulIndex = neighbour2;
        } else {
            angle = angle2;
            lrIndex = neighbour2;
            urIndex = (llIndex + 2) % 4;
            ulIndex = neighbour1;
        }
        
        cornerMap[LL] = llIndex;
        cornerMap[LR] = lrIndex;
        cornerMap[UR] = urIndex;
        cornerMap[UL] = ulIndex;
        
        // Determine width and height of figure in output
        // If the figure is rotated this is not necessarily the same as the
        // width and height in the raw data.
        if (((cornerMap[LL] == 0) && (cornerMap[LR] == 1))
                || ((cornerMap[LL] == 1) && (cornerMap[LR] == 0))
                || ((cornerMap[LL] == 2) && (cornerMap[LR] == 3))
                || ((cornerMap[LL] == 3) && (cornerMap[LR] == 2))) {
            outputWidthPx = imgWidthPx;
            outputHeightPx = imgHeightPx;
        } else {
            outputWidthPx = imgHeightPx;
            outputHeightPx = imgWidthPx;
        }
        
        // Determine the displacement vector in image space that correspond to
        // the LL-LR and LL-UL axes in device space.
        vectorHor[0] = (imgBbox[cornerMap[LR]][0] - imgBbox[cornerMap[LL]][0])
                / outputWidthPx;
        vectorHor[1] = (imgBbox[cornerMap[LR]][1] - imgBbox[cornerMap[LL]][1])
                / outputWidthPx;
        vectorVert[0] = (imgBbox[cornerMap[UL]][0] - imgBbox[cornerMap[LL]][0])
                / outputHeightPx;
        vectorVert[1] = (imgBbox[cornerMap[UL]][1] - imgBbox[cornerMap[LL]][1])
                / outputHeightPx;
    }
    
    
    /**
     * Loads data from a file data source.
     * 
     * @param in The file from which the data is read.
     * @param width The width in pixels.
     * @param height The height in pixels
     * @param nrComponents Number of components per pixel.
     * appropriate for the color space.
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws PSErrorRangeCheck Unable to read the required number of bytes.
     */
    private void loadDataFromFile(final PSObjectFile in,
            final int width, final int height, final int nrComponents)
            throws IOException, PSErrorRangeCheck {
        
        bytesPerLine = (int) Math.ceil(((double) width)
                * ((double) nrComponents) * ((double) bitsPerComponent) / 8.0);
        
        data = new byte[height * bytesPerLine];
            
        int bytesRead = 0;
        while (true) {
            int nrNewBytes = in.getStream().read(data, bytesRead,
                    data.length - bytesRead);
            if (nrNewBytes == 0) {
                break;
            }
            bytesRead += nrNewBytes;
        }

        if (bytesRead != (height * bytesPerLine)) {
            throw new PSErrorRangeCheck();
        }
    }
    
    /**
     * Loads data from a procedure data source.
     * 
     * @param proc Procedure that must produce the data.
     * @param width The width in pixels.
     * @param height The height in pixels.
     * @param nrComponents Number of components per pixel.
     * appropriate for the color space.
     * @param interp Interpreter in which the procedure in executed.
     * 
     * @throws ProgramError This shouldn't happen, it indicates a bug.
     * @throws PSError A PostScript error occurred.
     */
    private void loadDataFromProcedure(final PSObjectArray proc,
            final int width, final int height, final int nrComponents,
            final Interpreter interp) throws ProgramError, PSError {
        
        bytesPerLine = (int) Math.ceil(((double) width)
                * ((double) nrComponents) * ((double) bitsPerComponent) / 8.0);
        data = new byte[height * bytesPerLine];
        
        int bytesRead = 0;
        while (bytesRead < height * bytesPerLine) {
            interp.runObject(proc);
            PSObjectString dataStr = interp.getOpStack().pop().toPSString();
            for (int i = 0; i < dataStr.length(); i++) {
                data[bytesRead] = (byte) dataStr.get(i);
                bytesRead++;
            }
        }
    }
    
    /**
     * Dumps some information about this image to the standard output. Only used
     * for debugging purposes.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public void dumpInfo() throws PSError {
        System.out.println("Width: " + imgWidthPx + "px, height: " + imgHeightPx
                + "px");
        
        // Compute the position of the corners in device coordinates.
        // ll lr ur ul
                
        System.out.println("Image corners:");
        System.out.println("ll: (" + deviceBbox[0][0] / 1e3 + " mm, "
                + deviceBbox[0][1] / 1e3 + " mm)");
        System.out.println("lr: (" + deviceBbox[1][0] / 1e3 + " mm, "
                + deviceBbox[1][1] / 1e3 + " mm)");
        System.out.println("ur: (" + deviceBbox[2][0] / 1e3 + " mm, "
                + deviceBbox[2][1] / 1e3 + " mm)");
        System.out.println("ul: (" + deviceBbox[3][0] / 1e3 + " mm, "
                + deviceBbox[3][1] / 1e3 + " mm)");
        
        System.out.println("Device corners:");
        System.out.println("ll: (" + deviceBbox[cornerMap[0]][0] / 1e3
                + " mm, " + deviceBbox[cornerMap[0]][1] / 1e3 + " mm)");
        System.out.println("lr: (" + deviceBbox[cornerMap[1]][0] / 1e3
                + " mm, " + deviceBbox[cornerMap[1]][1] / 1e3 + " mm)");
        System.out.println("ur: (" + deviceBbox[cornerMap[2]][0] / 1e3
                + " mm, " + deviceBbox[cornerMap[2]][1] / 1e3 + " mm)");
        System.out.println("ul: (" + deviceBbox[cornerMap[3]][0] / 1e3
                + " mm, " + deviceBbox[cornerMap[3]][1] / 1e3 + " mm)");
        System.out.println("Angle: " + angle / Math.PI * 180);
        
        
        int[] testCoor = {9, 0};
        int[] imgCoor = convertCoorDeviceToImg(testCoor);
        System.out.println("Converting coordinate: ("
                + testCoor[0] + ", " + testCoor[1] + ") -> ("
                + imgCoor[0] + ", " + imgCoor[1] + ")");
    }
    
    /**
     * Find the coordinate that is nearest to another coordinate.
     * 
     * @param searchCoor A list of coordinates that is to be searched.
     * @param verticalPriority If two coordinates are the same distance, this
     * parameter indicates which of the two is chosen. If true, the coordinate
     * with shortest vertical distance is chosen. If false, the coordinate
     * with shortest horizontal distance is chosen.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * 
     * @return Index of the nearest coordinate.
     */
    private int findNearest(final double x, final double y,
            final double[][] searchCoor, final boolean verticalPriority) {
        
        int bestCoor = -1;
        double bestDistance = Double.MAX_VALUE;
        double bestXDistance = Double.MAX_VALUE;
        double bestYDistance = Double.MAX_VALUE;
        
        // Loop through all coordinates.
        for (int i = 0; i < searchCoor.length; i++) {
            double xDistance = Math.abs(x - searchCoor[i][0]);
            double yDistance = Math.abs(y - searchCoor[i][1]);
            double distance = Math.sqrt(Math.pow(xDistance, 2)
                    + Math.pow(yDistance, 2));
            
            boolean newBest = false;
            if (Math.abs(distance - bestDistance) < 1e-3) {
                // Same distance, check the vertical and horizontal distances
                if (verticalPriority && (yDistance < bestYDistance)) {
                    newBest = true;
                } else if (!verticalPriority && (xDistance < bestXDistance)) {
                    newBest = true;
                }
            } else if (distance < bestDistance) {
                newBest = true;
            }
            
            // Is this coordinate better than the previous best one?
            if (newBest) {
                bestXDistance = xDistance;
                bestYDistance = yDistance;
                bestDistance = distance;
                bestCoor = i;
            }
        }
        
        return bestCoor;
    }
    
    /**
     * Convert a coordinate (in pixels) in device space to the corresponding
     * coordinate (in pixels) in image space.
     * 
     * @param deviceCoor The pixel coordinate (in pixels) in device coordinates.
     * The coordinate (0,0) corresponds to the lower-left corner.
     * 
     * @return The pixel coordinate (in pixels) in image coordinates.
     * The coordinate (0,0) corresponds to the lower-left corner.
     * 
     * @throws PSError A PostScript error occurred.
     */
    private int[] convertCoorDeviceToImg(final int[] deviceCoor)
            throws PSError {
        
        double deviceX = 0.5 + (double) deviceCoor[0];
        double deviceY = 0.5 + (double) deviceCoor[1];
        
        double tmpX = deviceX * vectorHor[0] + deviceY * vectorVert[0]
                + imgBbox[cornerMap[LL]][0];
        double tmpY = deviceX * vectorHor[1] + deviceY * vectorVert[1]
                + imgBbox[cornerMap[LL]][1];
        
        int[] imgCoor = new int[2];
        imgCoor[0] = (int) Math.round(tmpX - 0.5);
        imgCoor[1] = (int) Math.round(tmpY - 0.5);
        return imgCoor;
    }
    
    /**
     * Gets the width of the output image in PostScript points (pt).
     * 
     * @return The width (in pt) of the image as it is printed in the output.
     */
    public double getOutputWidthPt() {
        double dx = deviceBbox[cornerMap[LR]][0] - deviceBbox[cornerMap[LL]][0];
        double dy = deviceBbox[cornerMap[LR]][1] - deviceBbox[cornerMap[LL]][1];
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) / 1e3 / 25.4 * 72.0;
    }

    /**
     * Gets the height of the output image in PostScript points (pt).
     * 
     * @return The height (in pt) of the image as it is printed in the output.
     */
    public double getOutputHeightPt() {
        double dx = deviceBbox[cornerMap[UL]][0] - deviceBbox[cornerMap[LL]][0];
        double dy = deviceBbox[cornerMap[UL]][1] - deviceBbox[cornerMap[LL]][1];
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) / 1e3 / 25.4 * 72.0;
    }
    
    /**
     * Gets the width (in pixels) of the image in the output.
     * 
     * @return Width (px).
     */
    public int getOutputWidthPx() {
        return outputWidthPx;
    }
    
    /**
     * Gets the height (in pixels) of the image in the output.
     * 
     * @return Height (px).
     */
    public int getOutputHeightPx() {
        return outputHeightPx;
    }
    
    /**
     * Get the number of color components in the output.
     * 
     * @return Number of color components in output.
     */
    public PSColor getColorSpace() {
        return colorSpace;
    }
    
    /**
     * Get the number of bits per color component.
     * 
     * @return Number of bits.
     */
    public int getBitsPerComponent() {
        return bitsPerComponent;
    }
    
    /**
     * Get a copy of the decode array.
     * 
     * @return A copy of the decode array.
     */
    public double[] getDecode() {
        return decode.clone();
    }
    
    /**
     * Is interpolation turned on?
     * 
     * @return True if interpolation is on, false if interpolation is off.
     */
    public boolean getInterpolate() {
        return interpolate;
    }
    
    /**
     * Get the color information (input values) of a single pixel.
     * 
     * @param x The x-coordinate (pixels in device space)
     * @param y The y-coordinate (pixels in device space)
     * 
     * @return Array with color component values.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public double[] getPixelInputValues(final int x, final int y)
            throws PSError {
        
        int[] intValues = getPixelInputIntValues(x, y);
        int nrValues = intValues.length;
        double[] values = new double[nrValues];
        
        for (int v = 0; v < nrValues; v++) {
            double dmin = decode[2 * v];
            double dmax = decode[2 * v + 1];
            values[v] = dmin + ((double) intValues[v]) * (dmax - dmin)
                    / (Math.pow(2.0, (double) bitsPerComponent) - 1.0);
        }
        
        return values;
    }
    
    /**
     * Get the color information (integer input values) of a single pixel. The
     * decode array is not yet applied to the values.
     * 
     * @param x The x-coordinate (pixels in device space)
     * @param y The y-coordinate (pixels in device space)
     * 
     * @return Array with color component values.
     * 
     * @throws PSError A PostScript error occurred.
     */
    public int[] getPixelInputIntValues(final int x, final int y)
            throws PSError {
        
        int[] deviceCoor = {x, y};
        int[] imgCoor = convertCoorDeviceToImg(deviceCoor);
        
        int nrValues = colorSpace.getNrInputValues();
        int[] values = new int[nrValues];
        for (int v = 0; v < nrValues; v++) {
            int bitMsb = 8 * bytesPerLine * imgCoor[1]
                    + bitsPerComponent * nrValues * imgCoor[0]
                    + v * bitsPerComponent;
            int bitLsb = bitMsb + bitsPerComponent - 1;
            int byteMsb = bitMsb / 8;
            int bitInByteMsb = 7 - bitMsb % 8;
            int byteLsb = bitLsb / 8;
            int bitInByteLsb = 7 - bitLsb % 8;
            
            int intValue;
            if (byteMsb == byteLsb) {
                int mask = ((1 << (bitInByteMsb + 1)) - 1)
                        ^ ((1 << bitInByteLsb) - 1);
                intValue = (data[byteMsb] & mask) >> bitInByteLsb;
            } else if (byteMsb == (byteLsb - 1)) {
                int maskMsb = ((1 << (bitInByteMsb + 1)) - 1);
                int maskLsb = 255 ^ ((1 << bitInByteLsb) - 1);
                intValue = (data[byteMsb] & maskMsb) << (8 - bitInByteLsb);
                intValue |= (data[byteLsb] & maskLsb) >> bitInByteLsb;
            } else {
                throw new PSErrorUnregistered("More than 16 bit per component"
                        + " is not supported in bitmap images.");
            }
            values[v] = intValue;
        }
        
        return values;
    }

    /**
     * Return the bounding box in device space.
     * 
     * @return Two dimensional array with coordinates of all four corners.
     */
    public double[][] getDeviceBbox() {
        return deviceBbox.clone();
    }

    /**
     * Gets the corner map.
     * List with mappings between corners of image as it appears in the output
     * and the corners of the image in image space (deviceBbox).
     * [LL LR UR UL]
     * index LL: index in deviceBbox array of the lower-left corner as it
     * appears in the output, index LR: lower-right, index UR: upper-right, and
     * index UL: upper-left corner.
     * 
     * @return the cornerMap
     */
    public int[] getCornerMap() {
        return cornerMap.clone();
    }
    
    /**
     * Gets the angle of the image.
     * Angle between line lower-left-to-lower-right and horizontal line.
     * Definition of lower-left and lower-right corners follow cornerMap.
     * @return The angle (in degrees)
     */
    public double getAngle() {
        return angle / Math.PI * 180.0;
    }
}
