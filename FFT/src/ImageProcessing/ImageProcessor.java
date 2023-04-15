package ImageProcessing;

import GUI.ImageProcessorGUI.ImagePanel;
import Kernels.Kernel;
import Kernels.SampleGenerator;
import Resources.Complex;
import Resources.ComplexDouble;
import Transforms.FFT2;
import Windows.BlackmanWindow;
import Windows.HanningWindow;
import Windows.KaiserWindow;
import Windows.RectangularWindow;
import Windows.Window;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author toetu
 */
public class ImageProcessor implements Runnable {

    public ImageProcessor() {

    }

    public enum Process {
        FFT, IFFT, CONVOLVE, GAUSSIAN_BLUR, RIDGE_DETECTION,
        BOX_BLUR, SOBEL, GENERATE_SINE, GENERATE_CIRCULAR_SAMPLE,
        GENERATE_SPIRAL_SAMPLE, GENERATE_RECTILINEAR_SAMPLE, GAUSSIAN_GRIDDING, GENERATE_RANDOM_SAMPLE;
    }

    public enum WindowType {
        BLACKMAN, HANNING, KAISER, RECTANGULAR, NONE;
    }
    private int width, height;
    private BufferedImage image, resultsImage;
    private Process process;
    private WindowType w;
    private Window window;
    private Complex[][] resultsArray, ImageArray;
    private boolean useOriginal = true;
    private SampleGenerator generator;
    Kernel kernel;

    public ImageProcessor(File image) throws IOException {
        this.image = ImageIO.read(image);
        this.width = this.image.getWidth();
        this.height = this.image.getHeight();
        this.useOriginal = true;
        int p;
        /**
         * The requires a matrix to be a square matrix with a size that is a
         * power of 2 For efficiency in computation
         *
         */
        if ((width != height)
                || (Math.log(width) / Math.log(2)) - Math.round(Math.log(width) / Math.log(2)) != 0
                || (Math.log(height) / Math.log(2)) - Math.round(Math.log(height) / Math.log(2)) != 0) {
            //Makes sure that the matrix is a square Matrix
            if (this.width > this.height) {
                p = (int) Math.pow(2, ImageProcessor.nextPowerOf(width, 2));
            } else {
                p = (int) Math.pow(2, ImageProcessor.nextPowerOf(height, 2));
            }
            width = p;
            height = p;
            this.image = resizeImage(this.image, width, height);
        }
        RGB2gray();
        ImageArray = getGrayImageArray();
        generator = new SampleGenerator(this.image, this.getGrayImageArray());
    }

//Image Tools-------------------------------------------------------------------
    /**
     * Converts an RGB image to a grayscale image.
     *
     * @throws IOException
     */
    private void RGB2gray() throws IOException {
        BufferedImage grayIm = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);
                int r = (p >> 16) & 0xff;//Red
                int g = (p >> 8) & 0xff;//Green
                int b = p & 0xff;//Blue

                int avg = (r + b + g) / 3; //Grayscale

                p = (avg << 16) | (avg << 8) | avg;

                grayIm.setRGB(x, y, p);
            }
        }
        image = grayIm;
        ImageIO.write(grayIm, "png", new File("./resources/Results", "grayScaleCopy.png"));
    }

    /**
     *
     * @return grayscale Image Array
     * @throws IOException
     */
    public Complex[][] getGrayImageArray() throws IOException {
        Complex[][] pixels = new Complex[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y) & 0xff;
                pixels[y][x] = new ComplexDouble(p, 0);
            }
        }
        return pixels;
    }

    /**
     * Resizes a given image to the specified width and height
     *
     * @param image Image to be resized
     * @param width new Width
     * @param height new Height
     * @return resized image
     */
    protected final BufferedImage resizeImage(BufferedImage image, int width, int height) {
        Image resizedImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(resizedImage, 0, 0, null);
        return result;
    }

    /**
     * Calculates the next power of a given base from the current numerical
     * value n
     *
     * @param base base value
     * @param n index value. (Exponential value, power of base)
     * @return the next power of base
     */
    public static int nextPowerOf(int base, int n) {
        return (int) Math.ceil((Math.log(base) / Math.log(n)));
    }

    /**
     *
     * @param signal signal of double Type
     * @param name Name of the image file to be saved
     * @param format image format
     * @param type Image type
     * @return
     * @throws IOException
     */
    public static BufferedImage pixels2Im(double[][] signal, String name, String format, int type) throws IOException {
        int m = signal.length;
        int n = signal[0].length;
        BufferedImage signalIm = new BufferedImage(n, m, type);
        for (int y = 0; y < m; y++) {
            for (int x = 0; x < n; x++) {
                int p = Math.round((float) signal[y][x]);
                p = (p << 16) | (p << 8) | p;
                signalIm.setRGB(x, y, p);
            }
        }
        File newIm = new File("./resources/Results", name + "." + format);
        ImageIO.write(signalIm, format, newIm);

        return signalIm;
    }

    /**
     * Generates a NxN matrix of zeros
     *
     * @param size dictates the size of the matrix
     * @return zero matrix
     */
    public static Complex[][] zero(int size) {
        Complex[][] zero = new ComplexDouble[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                zero[row][col] = new ComplexDouble(0, 0);
            }
        }

        return zero;
    }

    /**
     * Given a Complex 2D signal, this function will fetch a Matrix that
     * contains the modulo of each pixel value.
     *
     * @param signal 2D Complex signal
     * @return Modulo matrix
     */
    public static double[][] getModuloMatrix(Complex[][] signal) {
        int channels = signal.length;

        if (signal.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatch signal lengths!");
        }
        double[][] modMatrix = new double[channels][channels];
        for (int row = 0; row < channels; row++) {
            for (int col = 0; col < channels; col++) {
                modMatrix[row][col] = signal[row][col].modulus();
            }
        }

        return modMatrix;
    }

    /**
     * This fetches all the real values of a 2D Complex signal
     *
     * @param signal 2D Complex signal
     * @return 2D Complex signal that contains all the real values of signal
     */
    public static double[][] getRealMatrix(Complex[][] signal) {
        int m = signal.length;
        int n = signal[0].length;
        double[][] real = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                try {
                    real[i][j] = signal[i][j].getReal();
                } catch (NullPointerException e) {
                    real[i][j] = 0;
                }
            }
        }
        return real;
    }

    /**
     * Fetches all the imaginary components of a given 2D complex signal
     *
     * @param signal 2D Complex signal
     * @return Imaginary matrix
     */
    public static double[][] getImaginaryMatrix(Complex[][] signal) {
        int m = signal.length;
        int n = signal[0].length;
        double[][] imaginary = new double[n][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                imaginary[i][j] = signal[i][j].getImaginary();
            }
        }
        return imaginary;
    }

    /**
     * Calculates the phase of each complex values within the 2D Complex signal
     *
     * @param signal 2D Complex signal
     * @return Phase matrix
     */
    public static double[][] getPhaseMatrix(Complex[][] signal) {
        int m = signal.length;
        int n = signal[0].length;
        double[][] phase = new double[n][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                phase[i][j] = Math.atan(signal[i][j].getImaginary() / signal[i][j].getReal());
            }
        }
        return phase;
    }

    /**
     * Finds the maximum modulo value within the 2D Complex signal
     *
     * @param signal 2D Complex signal
     * @return maximum modulo of signal
     */
    public static double getMaxModulo(Complex[][] signal) {
        double maxVal = 0.0;
        for (Complex[] signal1 : signal) {
            for (int col = 0; col < signal[0].length; col++) {
                if (signal1[col].modulus() > maxVal) {
                    maxVal = signal1[col].modulus();
                }
            }
        }
        return maxVal;
    }
    
    public static double getMinModulo(Complex[][] signal) {
        double minVal = 0.0;
        for (Complex[] signal1 : signal) {
            for (int col = 0; col < signal[0].length; col++) {
                if (signal1[col].modulus() < minVal) {
                    minVal = signal1[col].modulus();
                }
            }
        }
        return minVal;
    }

    public double[][] normalize(double[][] M, double max){
        double[][] normalized = new double[M.length][M[0].length];
        for(int i = 0; i < M.length; i++){
            for(int j = 0; j < M[0].length; j++){
                normalized[i][j] = M[i][j]/max;
            }
        }
        
        return normalized;
    }
    
    /**
     * Performs a log transform the given signal
     *
     * @param signal 2D signal
     * @param maxVal max signal value
     * @return log transformed signal
     */
    public double[][] logTransform(double[][] signal, double maxVal) {
        int channels = signal.length;
        double[][] logMatrix = new double[channels][channels];
        if (signal.length % 2 != 0) {
            throw new IllegalArgumentException("Mismatch signal lengths!");
        }
        double c = 255.0 / (Math.log(maxVal + 1));
        for (int row = 0; row < channels; row++) {
            for (int col = 0; col < channels; col++) {
                logMatrix[row][col] = c * Math.log(Math.abs(signal[row][col]) + 1);
            }
        }
        return logMatrix;
    }

//Getters and setters-----------------------------------------------------------
    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public void setWindow(WindowType window) {
        this.w = window;
    }

    public void setUseResult(boolean useResult) {
        if (resultsImage != null) {
            this.useOriginal = useResult;
        }
    }

    public BufferedImage getResultImage() {
        return resultsImage;
    }

    public BufferedImage getImage() {
        return image;
    }

    public BufferedImage loadSine() {
        if (JOptionPane.showConfirmDialog(null, "This will override the current Image. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION) == 0) {
            try {
                this.image = generator.getIdealImage();
                this.ImageArray = this.getGrayImageArray();
            } catch (IOException ex) {
                Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
    
    
    
    @Override
    public void run() {

        if (null != process) {
            switch (process) {
                case FFT -> {
                    if (w != null) {
                        switch (w) {
                            case BLACKMAN -> {
                                window = new BlackmanWindow(width);
                            }
                            case HANNING -> {
                                window = new HanningWindow(width);
                            }
                            case KAISER -> {
                                String alpha = JOptionPane.showInputDialog("Please enter in alpha value");
                                window = new KaiserWindow(width, Integer.parseInt(alpha));
                            }
                            case RECTANGULAR -> {
                                int size = Integer.parseInt(JOptionPane.showInputDialog("Please enter in size for rectangle"));
                                window = new RectangularWindow(width, width / 2 - size, width / 2 + size);
                            }
                            case NONE -> {
                                window = null;
                            }
                        }
                    }
                    try {
                        FFT2 fft;
                        //Check if there is a chosen window for the Tranform
                        if (window != null) {
                            fft = new FFT2(window);
                        } else {
                            fft = new FFT2();
                        }
                        if (useOriginal) {
                            resultsArray = fft.transform(ImageArray);
                        } else {
                            resultsArray = fft.transform(this.resultsArray);
                        }

                        double max = ImageProcessor.getMaxModulo(resultsArray);
                        Complex[][] shifted = fft.fftShift(resultsArray);
                        double[][] logMatrix = logTransform(ImageProcessor.getModuloMatrix(shifted), max);
                        resultsImage = ImageProcessor.pixels2Im(logMatrix, "FFT", "png", BufferedImage.SCALE_DEFAULT);

                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case IFFT -> {
                    try {
                        double[][] realImage = null;
                        if (!useOriginal) {
                            FFT2 fft = new FFT2();
                            resultsArray = fft.reverse(resultsArray);
                            realImage = ImageProcessor.getRealMatrix(resultsArray);
                        } else {
                            FFT2 fft = new FFT2();
                            resultsArray = fft.reverse(ImageArray);
                            realImage = ImageProcessor.getRealMatrix(resultsArray);

                        }
                        resultsImage = ImageProcessor.pixels2Im(realImage, "IFFT", "png", BufferedImage.TYPE_BYTE_GRAY);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case GAUSSIAN_BLUR -> {
                    try {
                        kernel = new Kernel(Kernel.kernelType.GAUSSIAN_BLUR);
                        Complex[][] im = null;

                        //Determines which image to work on
                        if (useOriginal) {
                            im = ImageArray;
                        } else {
                            im = resultsArray;
                        }

                        Complex[][] newIm = new Complex[im.length][im[0].length];
                        System.out.println(im.length + " and " + im[0].length);

                        for (int y = 0; y < im.length; y++) {
                            for (int x = 0; x < im[0].length; x++) {
                                newIm[y][x] = kernel.convolve(im, x, y);
                            }
                        }
                        resultsArray = newIm;
                        resultsImage = pixels2Im(ImageProcessor.getRealMatrix(newIm), "Blurred", "png", BufferedImage.SCALE_DEFAULT);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case BOX_BLUR -> {
                    try {
                        kernel = new Kernel(Kernel.kernelType.BOX_BLUR);
                        Complex[][] im = null;

                        //Determines which image to work on
                        if (useOriginal) {
                            im = ImageArray;
                        } else {
                            im = resultsArray;
                        }

                        Complex[][] newIm = new Complex[im.length][im[0].length];
                        for (int y = 0; y < im.length; y++) {
                            for (int x = 0; x < im[0].length; x++) {
                                newIm[y][x] = kernel.convolve(im, x, y);
                            }
                        }
                        resultsArray = newIm;
                        resultsImage = pixels2Im(ImageProcessor.getRealMatrix(newIm), "Blurred", "png", BufferedImage.TYPE_BYTE_GRAY);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                case RIDGE_DETECTION -> {
                    try {
                        kernel = new Kernel(Kernel.kernelType.RIDGE_DETECTION);
                        Complex[][] im = null;

                        //Determines which image to work on
                        if (useOriginal) {
                            im = ImageArray;
                        } else {
                            im = resultsArray;
                        }

                        Complex[][] newIm = new Complex[im.length][im[0].length];
                        for (int y = 0; y < im.length; y++) {
                            for (int x = 0; x < im[0].length; x++) {
                                newIm[y][x] = kernel.convolve(im, x, y);
                            }
                        }
                        resultsArray = newIm;
                        resultsImage = pixels2Im(ImageProcessor.getRealMatrix(newIm), "Ridges", "png", BufferedImage.TYPE_BYTE_GRAY);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                case SOBEL -> {
                    try {
                        kernel = new Kernel(Kernel.kernelType.SOBEL);
                        Complex[][] im = null;

                        //Determines which image to work on
                        if (useOriginal) {
                            im = ImageArray;
                        } else {
                            im = resultsArray;
                        }

                        Complex[][] Gx = new Complex[im.length][im[0].length];
                        Complex[][] Gy = new Complex[im.length][im[0].length];
                        for (int y = 0; y < im.length; y++) {
                            for (int x = 0; x < im[0].length; x++) {
                                Gx[y][x] = kernel.convolve(im, x, y);
                            }
                        }
                        for (int y = 0; y < im.length; y++) {
                            for (int x = 0; x < im[0].length; x++) {
                                Gy[y][x] = kernel.convolve(im, x, y);
                            }
                        }

                        resultsArray = Gx;
                        resultsImage = pixels2Im(ImageProcessor.getRealMatrix(Gx), "Edges", "png", BufferedImage.TYPE_BYTE_GRAY);
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case GENERATE_SINE -> {
                    int size = 0;
                    if (generator == null) {
                        generator = new SampleGenerator();
                    }

                    if (generator.getIdealImage() == null) {
                        size = Integer.parseInt(JOptionPane.showInputDialog("Please enter size of signal."));
                        if (Math.log(size) / Math.log(2) != Math.floor(Math.log(size) / Math.log(2))
                                || Math.log(size) / Math.log(2) != Math.ceil(Math.log(size) / Math.log(2))) {
                            throw new InputMismatchException("Needs to be a power of 2");
                        }

                        double frequency = Double.parseDouble(JOptionPane.showInputDialog("Please enter frequency of the signal"));
                        if (JOptionPane.showConfirmDialog(null, "Horizontal?", "Input", JOptionPane.YES_NO_OPTION) == 0) {
                            ImageArray = generator.generateXSine(size, 1.0 / frequency, true);
                        } else {
                            ImageArray = generator.generateYSine(size, 1.0 / frequency, true);
                        }
                    } else {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to combine this with the existing sine wave?", "Input", JOptionPane.YES_NO_OPTION) == 1) {
                            size = Integer.parseInt(JOptionPane.showInputDialog("Please enter size of signal."));

                            if (Math.log(size) / Math.log(2) != Math.floor(Math.log(size) / Math.log(2))
                                    || Math.log(size) / Math.log(2) != Math.ceil(Math.log(size) / Math.log(2))) {
                                throw new InputMismatchException("Needs to be a power of 2");
                            }

                            double frequency = Double.parseDouble(JOptionPane.showInputDialog("Please enter frequency of the signal"));

                            //Horizontal or vertical sine
                            if (JOptionPane.showConfirmDialog(null, "Horizontal?", "Input", JOptionPane.YES_NO_OPTION) == 0) {
                                ImageArray = generator.generateXSine(size, 1.0 / frequency, true);
                            } else {
                                ImageArray = generator.generateYSine(size, 1.0 / frequency, true);
                            }

                        } else {
                            try {
                                size = Integer.parseInt(JOptionPane.showInputDialog("Please enter size of signal."));

                                if (Math.log(size) / Math.log(2) != Math.floor(Math.log(size) / Math.log(2))
                                        || Math.log(size) / Math.log(2) != Math.ceil(Math.log(size) / Math.log(2))) {
                                    throw new InputMismatchException("Needs to be a power of 2");
                                }

                                double frequency = Double.parseDouble(JOptionPane.showInputDialog("Please enter frequency (Hz) of the signal"));

                                //Horizontal or vertical sine
                                if (JOptionPane.showConfirmDialog(null, "Horizontal?", "Input", JOptionPane.YES_NO_OPTION) == 0) {
                                    ImageArray = generator.combine(generator.generateXSine(size, 1.0 / frequency, false));
                                } else {
                                    ImageArray = generator.combine(generator.generateYSine(size, 1.0 / frequency, false));
                                }

                            } catch (Exception ex) {
                                Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    height = size;
                    width = size;
                    image = generator.getIdealImage();

                }
                case GENERATE_CIRCULAR_SAMPLE -> {
                    try {
                        int samples = Integer.parseInt(JOptionPane.showInputDialog("Please enter the amount of radial rays"));
                        ImageArray = generator.radialSampling(samples);
                        image = generator.getSampleIm();
                        System.out.println("Circular Samples");
                    } catch (Exception ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case GENERATE_SPIRAL_SAMPLE -> {
                    try {
                        int samples = Integer.parseInt(JOptionPane.showInputDialog("Please enter sample size"));
                        ImageArray = generator.spiralSampling(samples);
                        image = generator.getSampleIm();
                        System.out.println("Spiral Samples");
                    } catch (HeadlessException | NumberFormatException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case GENERATE_RANDOM_SAMPLE -> {
                    try {
                        int samples = Integer.parseInt(JOptionPane.showInputDialog("Please enter sample size"));
                        ImageArray = generator.randomSampling(samples);
                        image = generator.getSampleIm();
                        System.out.println("Random Samples");
                    } catch (HeadlessException | NumberFormatException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                case GENERATE_RECTILINEAR_SAMPLE -> {
                    try {
                        int samples = Integer.parseInt(JOptionPane.showInputDialog("Please enter sample size"));
                        ImageArray = generator.rectilinearSampling(samples);
                        image = generator.getSampleIm();
                        System.out.println("Rectilinear Samples");
                    } catch (HeadlessException | NumberFormatException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                case GAUSSIAN_GRIDDING -> {
                    try {
                        if (generator.getSampleIm() == null) {
                            throw new Exception("No samples available");
                        } else {
                            kernel = new Kernel(Kernel.kernelType.GAUSSIAN_BLUR);
                            JFrame kernelDisplay = new JFrame("Kernel");
                            kernelDisplay.setSize(new Dimension(500, 500));
                            ImagePanel displayer;
                            displayer = new ImagePanel();
                            displayer.setImage(kernel.visualizeKernel());
                            kernelDisplay.getContentPane().add(displayer);
                            kernelDisplay.setVisible(true);
                            Complex[][] im = generator.getSample();
                            Complex[][] newIm = ImageProcessor.zero(im.length);
                            System.out.println(im.length + " and " + im[0].length);

                            FFT2 fft;
                            //Check if there is a chosen window for the Tranform
                            if (window != null) {
                                fft = new FFT2(window);
                            } else {
                                fft = new FFT2();
                            }
                            
                            for (Point i : generator.getSampleCoordinates()) {
                                newIm[i.y][i.x] = kernel.convolve(im, i.x, i.y);
                            }
                                                        
                            double[][] newImsignal = generator.normalize(newIm, generator.getMax(newIm), generator.getMin(newIm));
                            generator.display(newImsignal, "Interpolated");
                            
                            
                            resultsArray = fft.transform(newIm);
                            
                            
                            double max = ImageProcessor.getMaxModulo(resultsArray);
                            Complex[][] shifted = fft.fftShift(resultsArray);
                            
                            Complex[][] ideal = fft.transform(generator.getIdeal());
                            ideal = fft.fftShift(ideal);
                            
                            double idealMax = ImageProcessor.getMaxModulo(ideal);
                            System.out.println(idealMax);
                            double resultMax = ImageProcessor.getMaxModulo(shifted);
                            System.out.println(resultMax);
                            double[][] idealModulos = ImageProcessor.getModuloMatrix(ideal);
                            double[][] resultModulos = ImageProcessor.getModuloMatrix(shifted);
                            
                            double[][] normalizedResults = normalize(resultModulos, resultMax);
                            double[][] normalizedIdeal = normalize(idealModulos, idealMax);
                            
                            //Error calculation
                            double num = 0,den = 0;
                            for(int i  = 0 ; i  < shifted.length; i++){
                                for(int j = 0 ; j < shifted[0].length; j++){
                                    num += Math.pow(normalizedResults[i][j] - normalizedIdeal[i][j],2);
                                    den += Math.pow(normalizedIdeal[i][j],2);
                                }
                            }
                            double n = shifted.length;
                            num = num/(n*n);
                            
                            double RRMSE = Math.sqrt(num/den);
                            
                            System.out.println("RRMSE = " + RRMSE);
                            
                            double[][] logMatrix = logTransform(ImageProcessor.getModuloMatrix(shifted), max);
                            pixels2Im(logTransform(ImageProcessor.getModuloMatrix(ideal),ImageProcessor.getMaxModulo(ideal)),"Ideal", "png", BufferedImage.TYPE_BYTE_GRAY);
                            resultsImage = pixels2Im(logMatrix, "GRIDDING", "png", BufferedImage.TYPE_BYTE_GRAY);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(ImageProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                default -> {
                }
            }
        }
    }

}
