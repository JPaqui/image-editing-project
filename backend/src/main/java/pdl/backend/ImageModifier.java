package pdl.backend;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.enhance.EnhanceImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

import java.util.Map;

public abstract class ImageModifier {


    /**
     * Compute the cumulative histogram with the histogram in parameter.
     *
     * @param hist The image's histogram
     * @return the cumulative histogram
     */
    public static int[] getCumulatedHist(int[] hist) {
        int[] cHist = new int[256];
        for (int i = 0; i < 256; i++) {
            cHist[i] = 0;
        }
        cHist[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            cHist[i] = cHist[i - 1] + hist[i];
        }
        return cHist;
    }

    /**
     * Add to each bands (RGB) of the input image the value of delta.
     *
     * @param delta The luminosity value added (can be negative)
     * @param input The input image
     */
    public static void addLuminosity(Planar<GrayU8> input, int delta) {
        int numBands = input.getNumBands();
        numBands = Math.max(1, Math.min(numBands, 3));
        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {
                for (int i = 0; i < numBands; i++) {
                    input.getBand(i).set(x, y, Math.max(0, Math.min(input.getBand(i).get(x, y) + delta, 255)));
                }
            }
        }
    }


    /**
     * Convert RGB value to HSV.
     *
     * @param r   in [0,255]
     * @param g   in [0,255]
     * @param b   in [0,255]
     * @param hsv with h in [0,360], s and v in [0,1]
     */
    public static void rgbToHsv(int r, int g, int b, float[] hsv) {
        float r_float = (float) r / 255;
        float g_float = (float) g / 255;
        float b_float = (float) b / 255;
        int maxRGB_int = Math.max(Math.max(r, g), b);
        float maxRGB_float = (float) maxRGB_int / 255;
        int minRGB_int = Math.min(Math.min(r, g), b);
        float minRGB_float = (float) minRGB_int / 255;


        if ((r == g) && (g == b)) {
            hsv[0] = 0;
        } else if (maxRGB_int == r) {
            hsv[0] = (60 * (g_float - b_float) / (maxRGB_float - minRGB_float) + 360) % 360;
        } else if (maxRGB_int == g) {
            hsv[0] = 60 * (b_float - r_float) / (maxRGB_float - minRGB_float) + 120;
        } else {
            hsv[0] = 60 * (r_float - g_float) / (maxRGB_float - minRGB_float) + 240;
        }

        hsv[1] = (maxRGB_float == 0) ? 0 : 1 - (minRGB_float / maxRGB_float);

        hsv[2] = maxRGB_float;

    }

    /**
     * Convert HSV value to RGB.
     *
     * @param h   in [0,360]
     * @param s   in [0,1]
     * @param v   in [0,1]
     * @param rgb with r, g and b in [0,255]
     */
    public static void hsvToRgb(float h, float s, float v, int[] rgb) {
        int t = ((int) (h / 60)) % 6;
        float f = h / 60 - t;
        int l = (int) ((v * (1 - s)) * 255);
        int m = (int) ((v * (1 - f * s)) * 255);
        int n = (int) ((v * (1 - (1 - f) * s)) * 255);
        int v_int = (int) (v * 255);
        int[][] resultRGB = {{v_int, n, l},
                {m, v_int, l},
                {l, v_int, n},
                {l, m, v_int},
                {n, l, v_int},
                {v_int, l, m}
        };

        System.arraycopy(resultRGB[t], 0, rgb, 0, rgb.length);
    }

    /**
     * histogram equalizer with gray level images.
     *
     * @param gray gray level image
     */
    public static void glequalizer(GrayU8 gray) {

        int[] histogram = new int[256];
        int[] transform = new int[256];

        ImageStatistics.histogram(gray, 0, histogram);
        EnhanceImageOps.equalize(histogram, transform);
        EnhanceImageOps.applyTransform(gray, transform, gray);
    }

    /**
     * histogram equalizer with color image with S or V band.
     * If input is a gray level image, glequalizer is called.
     *
     * @param input image
     * @param canal char 'S' or 'V' for the equalizer in each band
     */
    public static void equalizer(Planar<GrayU8> input, char canal) {
        int numBands = input.getNumBands();
        if (numBands < 3) {
            glequalizer(input.getBand(0));
            return;
        }
        numBands = Math.max(1, Math.min(numBands, 3));
        int[] hist = new int[256];
        float[] hsv = new float[3];
        int[] rgb = new int[3];
        for (int i = 0; i < 256; i++) {
            hist[i] = 0;
        }
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                for (int i = 0; i < numBands; i++) {
                    rgb[i] = input.getBand(i).get(x, y);
                }
                rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
                switch (canal) {
                    case 'V':
                        hist[Math.round(hsv[2] * 255)]++;
                        break;
                    case 'S':
                        hist[Math.round(hsv[1] * 255)]++;
                        break;
                }
            }
        }
        int[] cHist = getCumulatedHist(hist);
        float[] newValues = new float[256];

        for (int i = 0; i < 256; i++) {
            newValues[i] = (float) cHist[i] / input.totalPixels();
            if (newValues[i] > 1) {
                newValues[i] = 1;
            } else if (newValues[i] < 0) {
                newValues[i] = 0;
            }
        }

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                for (int i = 0; i < numBands; i++) {
                    rgb[i] = input.getBand(i).get(x, y);
                }
                rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
                switch (canal) {
                    case 'V':
                        hsv[2] = newValues[Math.round(hsv[2] * 255)];
                        break;
                    case 'S':
                        hsv[1] = newValues[Math.round(hsv[1] * 255)];
                        break;
                }
                hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
                for (int i = 0; i < numBands; i++) {
                    input.getBand(i).set(x, y, rgb[i]);
                }
            }
        }
    }

    /**
     * Change each pixel of the input image to match the new hue value.
     * If input have less than one band it does nothing.
     *
     * @param input color image
     * @param hue   int value of the new hue
     */
    public static void hueFilter(Planar<GrayU8> input, int hue) {
        int numBands = input.getNumBands();
        if (numBands < 3) {
            throw new ImageControllerException("The input picture must have at least 3 bands");
        }else if (hue < 0 || 360 < hue){
            throw new ImageControllerException("min or max value out of bound of 0 360");
        }
        numBands = 3;
        float[] hsv = new float[3], rgb = new float[3];
        double radHue = Math.toRadians(hue);
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                ColorHsv.rgbToHsv(input.getBand(0).get(x, y), input.getBand(1).get(x, y), input.getBand(2).get(x, y), hsv);
                hsv[0] = (float) radHue;
                ColorHsv.hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
                for (int i = 0; i < numBands; i++) input.getBand(i).set(x, y, (int) rgb[i]);
            }
        }
    }

    /**
     * Apply the meanFilter on the input image and save it in the output image.
     * The NORMALIZE borderType is used.
     *
     * @param input  The input image
     * @param output The output image
     * @param size   size of the filter
     */
    public static void meanFilterWithBorders(Planar<GrayU8> input, Planar<GrayU8> output, int size) {
        int numBands = input.getNumBands();
        if (numBands == 4) {
            for (int y = 0; y < input.height; y++) {
                for (int x = 0; x < input.width; x++) {
                    output.getBand(3).set(x, y, input.getBand(3).get(x, y));
                }
            }
        }
        numBands = Math.max(1, Math.min(numBands, 3));
        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {
                for (int i = 0; i < numBands; i++) {
                    int total = 0;
                    int n = 0;
                    for (int u = -size; u <= size; u++) {
                        for (int v = -size; v <= size; v++) {
                            if (input.isInBounds(x + u, y + v)) {
                                n++;
                                total += input.getBand(i).get(x + u, y + v);
                            }
                        }
                    }
                    output.getBand(i).set(x, y, total / n);
                }
            }
        }
    }

    /**
     * Apply both the mean filter or gaussian filter on the output image depending on the type variable.
     *
     * @param input  The input image
     * @param output The output image
     * @param size   Size of the filter
     * @param type   To choose the type of the filter 'G' for the gaussian filter and 'M' for the mean filter
     */
    public static void blur(Planar<GrayU8> input, Planar<GrayU8> output, int size, char type) {
        switch (type) {
            case 'G':
                GBlurImageOps.gaussian(input, output, -1, size, null);
                break;
            case 'M':
                meanFilterWithBorders(input, output, size);
                break;
            default:
                throw new ImageControllerException("axis parameter must be \'G\' or \'M\'");
        }
    }

    /**
     * Convert the input image into output gray level image.
     * If the input have less than 3 bands, it does nothing
     *
     * @param input The input image
     */
    public static void toGray(Planar<GrayU8> input) {
        double[] rgbToGray = {0.3f, 0.59f, 0.11f};
        if (input.getNumBands() < 3) {
            throw new ImageControllerException("The input picture must have at least 3 bands");
        }
        for (int y = 0; y < input.height; y++) {
            for (int x = 0; x < input.width; x++) {
                double value = 0;
                for (int i = 0; i < 3; i++) {
                    value += input.getBand(i).get(x, y) * rgbToGray[i];
                }
                for (int i = 0; i < 3; i++) {
                    input.getBand(i).set(x, y, (int) value);
                }
            }
        }
    }

    /**
     * Apply the Sobel filter on the gray version of the input image and save it in the output image.
     * if input is already an gray level image, it apply directly the Sobel filter on it.
     *
     * @param input  The input image
     * @param output The output image
     */
    public static void gradientImageSobel(Planar<GrayU8> input, Planar<GrayU8> output) {
        toGray(input);
        int numBands = input.getNumBands();
        if (numBands == 4) {
            for (int y = 0; y < input.height; y++) {
                for (int x = 0; x < input.width; x++) {
                    output.getBand(3).set(x, y, input.getBand(3).get(x, y));
                }
            }
            numBands = 3;
        }
        for (int i = 0; i < numBands; i++) {
            GrayU8 bandIn = input.getBand(i);
            GrayU8 bandOut = output.getBand(i);

            int[][] h1 = {
                    {-1, 0, 1},
                    {-2, 0, 2},
                    {-1, 0, 1}
            };

            int[][] h2 = {
                    {-1, -2, -1},
                    {0, 0, 0},
                    {1, 2, 1}
            };
            for (int y = 1; y < input.height - 1; y++) {
                for (int x = 1; x < input.width - 1; x++) {
                    int Gx = 0, Gy = 0;
                    for (int v = 0; v < 3; v++) {
                        for (int u = 0; u < 3; u++) {
                            Gx += bandIn.get(x + u - 1, y + v - 1) * h2[v][u];
                            Gy += bandIn.get(x + u - 1, y + v - 1) * h1[v][u];
                        }
                    }
                    int newValue = Math.max(0, Math.min((int) Math.sqrt(Gx * Gx + Gy * Gy), 255));
                    bandOut.set(x, y, newValue);
                }
            }
        }
        //toGray(output);
    }

    /**
     * Create a rainbow hue effect from h = 0 to h = 330 with a step of 30.
     * The rainbow strips can be horizontal or vertical depending on the direction parameter
     * If the input is not a colored image, do nothing.
     *
     * @param input     The input image
     * @param direction 'V' for vertical strips and 'H' for horizontal one
     */
    public static void rainbow(Planar<GrayU8> input, char direction) {
        int numBands = input.getNumBands(), hue;
        if (numBands < 3) {
            throw new ImageControllerException("The input picture must have at least 3 bands");
        }
        numBands = 3;
        int x_center = input.width / 2;
        int y_center = input.height / 2;
        double dist_max = Math.sqrt((input.width / 2) * (input.width / 2) + (input.height / 2) * (input.height / 2));
        float[] hsv = new float[3], rgb = new float[3];
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                ColorHsv.rgbToHsv(input.getBand(0).get(x, y), input.getBand(1).get(x, y), input.getBand(2).get(x, y), hsv);
                switch (direction) {
                    case 'H':
                        hue = Math.max(0, 330 - (y / (input.height / 12) * 30));
                        break;
                    case 'V':
                        hue = Math.max(0, 330 - (x / (input.width / 12) * 30));
                        break;
                    case 'C':
                        double dist_from_center = Math.sqrt((x_center - x) * (x_center - x) + (y_center - y) * (y_center - y));
                        hue = Math.max(0, 330 - ((int) dist_from_center / ((int) dist_max / 12) * 30));
                        break;
                    default:
                        throw new ImageControllerException("axis parameter must be \'V\', \'H\' or \'C\'");
                }
                double radHue = Math.toRadians(hue);
                hsv[0] = (float) radHue;
                ColorHsv.hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
                for (int i = 0; i < numBands; i++) input.getBand(i).set(x, y, (int) rgb[i]);
            }
        }
    }

    /**
     * Desaturate all pixels with hue that isn't betwin the min and max values.
     * If max < min, desaturate pixels with hue betwin min and max values.
     * If the input is not a colored image, do nothing.
     *
     * @param input The input image
     * @param min   Min value
     * @param max   Max value
     */
    public static void hueSelector(Planar<GrayU8> input, int min, int max) {
        int numBands = input.getNumBands();
        if (numBands < 3) {
            throw new ImageControllerException("The input picture must have at least 3 bands");
        } else if (min < 0 || 360 < min || max < 0 || 360 < max){
            throw new ImageControllerException("min or max value out of bound of 0 360");
        }
        numBands = 3;
        float[] hsv = new float[3], rgb = new float[3];
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                ColorHsv.rgbToHsv(input.getBand(0).get(x, y), input.getBand(1).get(x, y), input.getBand(2).get(x, y), hsv);
                double hue = Math.toDegrees(hsv[0]);
                if (min > max) {
                    if (min >= hue && hue >= max) hsv[1] = 0;
                } else {
                    if (!(max >= hue && hue >= min)) hsv[1] = 0;
                }
                ColorHsv.hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
                for (int i = 0; i < numBands; i++) input.getBand(i).set(x, y, (int) rgb[i]);
            }
        }
    }

    /**
     * Change the scale of the picture with new height and width.
     * If the new height or width egal 0 or negative, do nothing.
     *
     * @param input     The input image
     * @param output    The output image
     * @param newHeight New height
     * @param newWidth  New width
     */
    public static void scaling(Planar<GrayU8> input, Planar<GrayU8> output, int newHeight, int newWidth) {
        if (newHeight <= 0 || newWidth <= 0) {
            throw new ImageControllerException("newHeight and newWidth parameter value must be positive values");
        }
        output.reshape(newWidth, newHeight);
        int numBands = input.getNumBands();
        int old_height = input.height;
        int old_width = input.width;
        float y_ratio = (float) old_height / newHeight;
        float x_ratio = (float) old_width / newWidth;
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int fetch_x = (int) Math.floor(x_ratio * x);
                int fetch_y = (int) Math.floor(y_ratio * y);
                for (int i = 0; i < numBands; i++) {
                    output.getBand(i).set(x, y, input.getBand(i).get(fetch_x, fetch_y));
                }
            }
        }
    }

    /**
     * Change the color of all pixel Hue, with the opposite color in the Hue specter
     *
     * @param input The input image
     */
    public static void reverseHue(Planar<GrayU8> input) {
        int numBands = input.getNumBands();
        if (numBands < 3) {
            throw new ImageControllerException("The picture must have at least 3 bands");
        }
        numBands = 3;
        float[] hsv = new float[3], rgb = new float[3];
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                ColorHsv.rgbToHsv(input.getBand(0).get(x, y), input.getBand(1).get(x, y), input.getBand(2).get(x, y), hsv);
                hsv[0] = (float) ((hsv[0] + Math.PI) % (2 * Math.PI));
                ColorHsv.hsvToRgb(hsv[0], hsv[1], hsv[2], rgb);
                for (int i = 0; i < numBands; i++) input.getBand(i).set(x, y, (int) rgb[i]);
            }
        }
    }

    /**
     * change the image into negative, thanks to the complementary number in the RGB scale.
     *
     * @param input The input image
     */
    public static void negative(Planar<GrayU8> input) {
        int numBands = input.getNumBands();
        if (numBands > 3) numBands = 3;
        for (int i = 0; i < numBands; i++) {
            for (int y = 0; y < input.getHeight(); y++) {
                for (int x = 0; x < input.getWidth(); x++) {
                    input.getBand(i).set(x, y, 255 - input.getBand(i).get(x, y));
                }
            }
        }
    }

    /**
     * Flip the picture with the vertical or horizontal axis
     *
     * @param input The input image
     * @param axis  The axis used to flip the image (H for Horizontal, V for Vertical)
     */
    public static void flip(Planar<GrayU8> input, char axis) {
        int numBands = input.getNumBands();
        int compute_height = input.getHeight();
        int compute_width = input.getWidth();
        switch (axis) {
            case 'H':
                compute_height = input.getHeight() / 2;
                break;
            case 'V':
                compute_width = input.getWidth() / 2;
                break;
            default:
                throw new ImageControllerException("axis parameter must be \'V\' or \'H\'");
        }
        for (int y = 0; y < compute_height; y++) {
            for (int x = 0; x < compute_width; x++) {
                int fetch_x = x, fetch_y = y;
                if (axis == 'H') fetch_y = input.height - 1 - y;
                else fetch_x = input.width - 1 - x;
                for (int i = 0; i < numBands; i++) {
                    int tmp = input.getBand(i).get(x, y);
                    input.getBand(i).set(x, y, input.getBand(i).get(fetch_x, fetch_y));
                    input.getBand(i).set(fetch_x, fetch_y, tmp);
                }
            }
        }
    }

    /**
     * rotate the picture with the angle given in params
     *
     * @param input  The input image
     * @param output The output image
     * @param angle  The angle of the rotation
     */
    public static void rotate(Planar<GrayU8> input, Planar<GrayU8> output, int angle) {
        int numBands = input.getNumBands();
        double radiantAngle = Math.toRadians(angle);
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int xp = (int) ((x - input.width / 2) * Math.cos(radiantAngle) - (y - input.height / 2) * Math.sin(radiantAngle) + input.width / 2);
                int yp = (int) ((x - input.width / 2) * Math.sin(radiantAngle) + (y - input.height / 2) * Math.cos(radiantAngle) + input.height / 2);
                if (0 <= xp && xp < input.width && 0 <= yp && yp < input.height)
                    for (int i = 0; i < numBands; i++) output.getBand(i).set(x, y, input.getBand(i).get(xp, yp));
            }
        }
    }

    /**
     * Create a wave effect in vertical or horizontal axis, wave length and the amplitude of the wave can be changed with params
     *
     * @param input      The input picture
     * @param output     The output image
     * @param waveAxis   The axis used to wave the image (H for Horizontal, V for Vertical)
     * @param waveOffset The wave offset pixels from the top left of the picture, if waveOffset > waveLengthit's the same than waveOffset%waveLength
     * @param waveType   'C' for curve, 'R' for rectangle and 'T' for triangle
     * @param amplitude  The amplitude of the wave
     * @param waveLength The length of the wave
     */
    public static void wave(Planar<GrayU8> input, Planar<GrayU8> output, char waveAxis, int waveOffset, char waveType, int amplitude, int waveLength) {
        int numBands = input.getNumBands();
        double border_offset;
        int[] oldXY = new int[2];
        int[] newXY = new int[2];
        int modified_axis;
        double half_length = ((double) waveLength / 2);
        if (waveAxis == 'V') {
            modified_axis = 0;
        } else if (waveAxis == 'H') {
            modified_axis = 1;
        } else
            throw new ImageControllerException("waveAxis parameter must be \'V\' or \'H\'");
        for (oldXY[1] = 0 ; oldXY[1] <  input.getHeight(); oldXY[1]++) {
            for (oldXY[0] = 0; oldXY[0] < input.getWidth(); oldXY[0]++) {
                newXY[0] = oldXY[0];
                newXY[1] = oldXY[1];
                switch (waveType) {
                    case 'C':
                        double radiantAngle = ((oldXY[1 - modified_axis] + waveOffset) * Math.PI / half_length);
                        border_offset = Math.sin(radiantAngle);
                        break;
                    case 'R':
                        radiantAngle = ((oldXY[1 - modified_axis] + waveOffset) * Math.PI / half_length);
                        border_offset = Math.sin(radiantAngle);
                        border_offset = (border_offset > 0) ? 1 : -1;
                        break;
                    case 'T':
                        border_offset = -((2. / half_length * (oldXY[1 - modified_axis] + waveOffset - half_length * Math.floor(((double) (oldXY[1 - modified_axis] + waveOffset) / half_length + 1. / 2))) * Math.pow(-1, Math.floor((oldXY[1 - modified_axis] + waveOffset) / half_length - 1. / 2))));
                        break;
                    default:
                        throw new ImageControllerException("waveType parameter must be \'C\', \'R\' or \'T\'");
                }
                newXY[modified_axis] = (int) (oldXY[modified_axis] + border_offset * amplitude);
                if (0 <= newXY[0] && newXY[0] < input.width && 0 <= newXY[1] && newXY[1] < input.height)
                    for (int i = 0; i < numBands; i++)
                        output.getBand(i).set(newXY[0], newXY[1], input.getBand(i).get(oldXY[0], oldXY[1]));
            }
        }
    }

    /**
     * Turn the whole image into a sphere (it can also be an ellipse) with the elliptical grid mapping
     *
     * @param input      The input image
     * @param output     The output image
     * @param sphereType The type of the sphere (S for Spherical, E for Elliptical)
     */
    public static void sphere(Planar<GrayU8> input, Planar<GrayU8> output, char sphereType) {
        int numBands = input.getNumBands();
        int x_center = input.width / 2;
        int y_center = input.height / 2;
        int x_size, y_size;
        switch (sphereType) {
            case 'S':
                x_size = Math.min(x_center, y_center);
                y_size = Math.min(x_center, y_center);
                break;
            case 'E':
                x_size = x_center;
                y_size = y_center;
                break;
            default:
                throw new ImageControllerException("shpereType parameter must be \'S\' or \'E\'");
        }
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                int xp = (int) (((double) (x - x_center) / x_center) * Math.sqrt(1 - ((double) (y - y_center) / y_center) * ((double) (y - y_center) / y_center) / 2) * x_size + x_center);
                int yp = (int) (((double) (y - y_center) / y_center) * Math.sqrt(1 - ((double) (x - x_center) / x_center) * ((double) (x - x_center) / x_center) / 2) * y_size + y_center);
                if (0 <= xp && xp < input.width && 0 <= yp && yp < input.height) {
                    for (int i = 0; i < numBands; i++) output.getBand(i).set(xp, yp, input.getBand(i).get(x, y));
                }
            }
        }
    }

    /**
     * Apply the sepia filter to the input picture
     *
     * @param input The input image
     */
    public static void sepia(Planar<GrayU8> input) {
        int numBands = input.getNumBands();
        if (numBands < 3) return;
        if (numBands > 3) numBands = 3;
        double[][] coef = {{0.393, 0.769, 0.189}, {0.349, 0.686, 0.168}, {0.272, 0.534, 0.131}};
        int[] oldRGB = new int[3];
        double[] newRGB = new double[3];
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                for (int i = 0; i < numBands; i++) {
                    oldRGB[i] = input.getBand(i).get(x, y);
                    newRGB[i] = 0;
                }
                for (int i = 0; i < numBands; i++) {
                    for (int j = 0; j < numBands; j++) {
                        newRGB[i] += coef[i][j] * oldRGB[j];
                    }
                    newRGB[i] = Math.min(newRGB[i], 255);
                    input.getBand(i).set(x, y, (int) newRGB[i]);
                }
            }
        }
    }

    /**
     * Create an output with four miniature version of the input on each coorner
     * each miniature is 1/4's of the original input size
     *
     * @param input  The input picture
     * @param output The output picture
     */
    public static void mozaic(Planar<GrayU8> input, Planar<GrayU8> output) {
        int newx, newy;
        //si width impaire alors +1 si height impaire alors +1
        int WidthHalf = input.getWidth() / 2 + ((input.getWidth() % 2 == 1) ? 1 : 0);
        int HeightHalf = input.getHeight() / 2 + ((input.getHeight() % 2 == 1) ? 1 : 0);
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                for (int i = 0; i < input.getNumBands(); i++) {
                    int num = x / 2 + WidthHalf * (y / 2);
                    newx = num % WidthHalf + ((x % 2 == 1) ? WidthHalf : 0);
                    newy = num / WidthHalf + ((y % 2 == 1) ? HeightHalf : 0);
                    output.getBand(i).set(newx, newy, input.getBand(i).get(x, y));
                }
            }
        }
    }


    /**
     * create a twisted effect amplified in the center of the picture
     * it's similar to the rotate function but the angle change linearly from the center of the picture to the edge where it is 0
     *
     * @param input    The input picture
     * @param output   The output picture
     * @param maxAngle the angle of the rotation at the center of the picture
     */
    public static void twist(Planar<GrayU8> input, Planar<GrayU8> output, int maxAngle) {
        int numBands = input.getNumBands();
        int x_center = input.width / 2;
        int y_center = input.height / 2;
        double dist_max = Math.sqrt((x_center) * (x_center) + (y_center) * (y_center));
        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                double dist_from_center = Math.sqrt((x_center - x) * (x_center - x) + (y_center - y) * (y_center - y));
                double variation_function = -(double) maxAngle / dist_max * dist_from_center + maxAngle;
                double radiantAngle = Math.toRadians(variation_function);
                int xp = (int) ((x - x_center) * Math.cos(radiantAngle) - (y - y_center) * Math.sin(radiantAngle) + x_center);
                int yp = (int) ((x - x_center) * Math.sin(radiantAngle) + (y - y_center) * Math.cos(radiantAngle) + y_center);
                if (0 <= xp && xp < input.width && 0 <= yp && yp < input.height)
                    for (int i = 0; i < numBands; i++) output.getBand(i).set(x, y, input.getBand(i).get(xp, yp));
            }
        }
    }


    private static int GaussFunction2D(int x, int y, int amplitude, int xCenter, int yCenter, float xSpread, float ySpread) {
        return (int) Math.round(amplitude * Math.exp(-(((x - xCenter) * (x - xCenter) / (2 * xSpread * xSpread) + ((y - yCenter) * (y - yCenter)) / (2 * ySpread * ySpread)))));
    }

    /**
     * Threshold Halftoning algorithm (or dithering) with 2 dimensional Gaussian function.
     * If the gray level of a pixel is lower than the gaussian function value associated, the pixel become black, white if it's not the case.
     *
     * @param input     The input picture
     * @param amplitude amplitude of the gaussian function, define the sensivity of halftoning //recommended to be around 255
     * @param spread    spread variable of the gaussian function, make the picture more white //recommended to be betwin 0 and 10 depending on the size parameter
     * @param size      max size of each dots
     */
    public static void Halftoning(Planar<GrayU8> input, int amplitude, float spread, int size){
        if(size < 0){
            throw new ImageControllerException("size parameter value must be positive");
        }
        toGray(input);
        int[][] gaussKernel = new int[size][size];
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                gaussKernel[y][x] = GaussFunction2D(x, y, amplitude, size / 2, size / 2, spread, spread);

        for (int i = 0; i < 3; i++) {
            for (int y = 0; y < input.getHeight(); y += size) {
                for (int x = 0; x < input.getWidth(); x += size) {
                    for (int yShift = 0; yShift < size; yShift++) {
                        for (int xShift = 0; xShift < size; xShift++) {
                            if (0 <= x + xShift && x + xShift < input.width && 0 <= y + yShift && y + yShift < input.height) {
                                if (gaussKernel[yShift][xShift] < input.getBand(i).get(x + xShift, y + yShift)) {
                                    input.getBand(i).set(x + xShift, y + yShift, 255);
                                } else {
                                    input.getBand(i).set(x + xShift, y + yShift, 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Apply specified algorithm in param to the input and output if needed
     *
     * @param input  The input image
     * @param output The output image (may not be used)
     * @param params The map containing parameters
     * @return Returns true if the result is in output, false if it's in input
     */
    public static boolean treatInput(Planar<GrayU8> input, Planar<GrayU8> output, Map<String, String> params) {
        switch (params.get("algorithm")) {
            case "addLuminosityRGB":
                if (!params.containsKey("gain"))
                    throw new ImageControllerException("Wrong argument");
                addLuminosity(input, Integer.parseInt(params.get("gain")));
                return false;
            case "equalize":
                if (!params.containsKey("canal"))
                    throw new ImageControllerException("Wrong arguments");
                equalizer(input, params.get("canal").charAt(0));
                return false;
            case "hueFilter":
                if (!params.containsKey("hue"))
                    throw new ImageControllerException("Wrong arguments");
                hueFilter(input, Integer.parseInt(params.get("hue")));
                return false;
            case "gradientImageSobel":
                gradientImageSobel(input, output);
                return true;
            case "blur":
                if (!params.containsKey("size") || !params.containsKey("type"))
                    throw new ImageControllerException("Wrong arguments");
                blur(input, output, Integer.parseInt(params.get("size")), params.get("type").charAt(0));
                return true;
            case "rainbow":
                if (!params.containsKey("direction"))
                    throw new ImageControllerException("Wrong arguments");
                rainbow(input, params.get("direction").charAt(0));
                return false;
            case "hueSelector":
                if (!params.containsKey("min") || !params.containsKey("max"))
                    throw new ImageControllerException("Wrong arguments");
                hueSelector(input, Integer.parseInt(params.get("min")), Integer.parseInt(params.get("max")));
                return false;
            case "scale":
                if (!params.containsKey("width") || !params.containsKey("height"))
                    throw new ImageControllerException("Wrong arguments");
                scaling(input, output, Integer.parseInt(params.get("width")), Integer.parseInt(params.get("height")));
                return true;
            case "reverseHue":
                reverseHue(input);
                return false;
            case "negative":
                negative(input);
                return false;
            case "flip":
                if (!params.containsKey("axis"))
                    throw new ImageControllerException("Wrong arguments");
                flip(input, params.get("axis").charAt(0));
                return false;
            case "rotate":
                if (!params.containsKey("angle"))
                    throw new ImageControllerException("Wrong arguments");
                rotate(input, output, Integer.parseInt(params.get("angle")));
                return true;
            case "wave":
                if (!params.containsKey("waveAxis") && !params.containsKey("waveOffset") && !params.containsKey("waveType") && !params.containsKey("amplitude") && !params.containsKey("waveLength"))
                    throw new ImageControllerException("Wrong arguments");
                wave(input, output, params.get("waveAxis").charAt(0), Integer.parseInt(params.get("waveOffset")), params.get("waveType").charAt(0), Integer.parseInt(params.get("amplitude")), Integer.parseInt(params.get("waveLength")));
                return true;
            case "sphere":
                if (!params.containsKey("sphere_type"))
                    throw new ImageControllerException("Wrong arguments");
                sphere(input, output, params.get("sphere_type").charAt(0));
                return true;
            case "sepia":
                sepia(input);
                return false;
            case "mozaic":
                mozaic(input, output);
                return true;
            case "twist":
                if (!params.containsKey("maxAngle"))
                    throw new ImageControllerException("Wrong arguments");
                twist(input, output, Integer.parseInt(params.get("maxAngle")));
                return true;
            case "halftoning":
                if (!params.containsKey("spread") || !params.containsKey("dotSize"))
                    throw new ImageControllerException("Wrong arguments");
                ImageModifier.Halftoning(input, 255, Float.parseFloat(params.get("spread")), Integer.parseInt(params.get("dotSize")));
                return false;
            default:
                throw new ImageControllerException("Unsupported algorithm");
        }
    }
}