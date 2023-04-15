/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Kernels;

import ImageProcessing.ImageProcessor;
import Resources.Complex;
import Resources.ComplexDouble;
import Transforms.FFT2;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author toetu
 */
public class SampleGenerator {

    private BufferedImage image, sampleIm;
    int size;
    static double wavelength;
    private Complex[][] idealWaveArray;
    private Complex[][] sampleArray;
    private ArrayList<Point> sampleCoordinates;
    private FFT2 fft = new FFT2();

    public SampleGenerator() {
    }

    public SampleGenerator(BufferedImage im, Complex[][] imArray) {
        if (im.getWidth() != im.getHeight()) {
            try {
                throw new Exception("Image dimensions do not equal");
            } catch (Exception ex) {
                Logger.getLogger(SampleGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.image = im;
        this.idealWaveArray = imArray;
        size = im.getHeight();
    }

    public Complex[][] generateXSine(int n, double frequency, boolean override) {
        Complex[][] sineX = new ComplexDouble[n][n];
        size = n;
        int mid = size / 2;

        wavelength = n * frequency;
        for (int y = -mid; y < mid; y++) {
            for (int x = -mid; x < mid; x++) {
                sineX[y + mid][x + mid] = new ComplexDouble(Math.cos((2.0 * Math.PI * (x)) / wavelength), Math.sin((2.0 * Math.PI * (x)) / wavelength));
                // sineX[y + mid][x + mid] = new ComplexDouble(255, 0);
            }
        }
        if (override) {
            double max = getMax(sineX);
            double min = getMin(sineX);
            double[][] normalized = normalize(sineX, max, min);
            image = display(normalized, "Sine");
            idealWaveArray = sineX;
        }
        
        return sineX;
    }

    public Complex[][] generateYSine(int n, double frequency, boolean override) {
        Complex[][] sineY = new ComplexDouble[n][n];
        size = n;
        int mid = size / 2;
        wavelength = n * frequency;
        for (int y = -mid; y < mid; y++) {
            for (int x = -mid; x < mid; x++) {
                sineY[y + mid][x + mid] = new ComplexDouble(Math.cos((2.0 * Math.PI * (y)) / wavelength), Math.sin((2.0 * Math.PI * (y)) / wavelength));
            }
        }

        if (override) {
            double max = getMax(sineY);
            double min = getMin(sineY);
            double[][] normalized = normalize(sineY, max, min);
            image = display(normalized, "Sine");
            idealWaveArray = sineY;
        }

        return sineY;
    }

    public Complex[][] combine(Complex[][] signal) throws Exception {
        if (signal.length != idealWaveArray.length || signal[0].length != idealWaveArray[0].length) {
            throw new Exception("Invalid sizes");
        }

        for (int i = 0; i < idealWaveArray.length; i++) {
            for (int j = 0; j < idealWaveArray[0].length; j++) {
                idealWaveArray[i][j].addInPlace(signal[i][j]);
            }
        }
        double max = getMax(idealWaveArray);
        double min = getMin(idealWaveArray);
        double[][] normalized = normalize(idealWaveArray, max, min);
        image = display(normalized, "New Sine");

        return idealWaveArray;
    }

    public BufferedImage display(double[][] signal, String name) {
        BufferedImage im = null;
        try {
            if (im == null) {
                im = new BufferedImage(signal.length, signal[0].length, BufferedImage.SCALE_DEFAULT);
            }
            for (int y = 0; y < signal.length; y++) {
                for (int x = 0; x < signal[0].length; x++) {
                    int p = Math.round((float) signal[y][x]);
                    p = (p << 16) | (p << 8) | p;
                    im.setRGB(x, y, p);
                }
            }
            File newIm = new File("./resources/Results", name + ".png");
            ImageIO.write(im, "png", newIm);
        } catch (IOException ex) {
            Logger.getLogger(SampleGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return im;
    }

    public double[][] normalize(Complex[][] signal, double max, double min) {
        double range = max - min;
        double[][] normalized = new double[signal.length][signal.length];
        for (int i = 0; i < signal.length; i++) {
            for (int j = 0; j < signal.length; j++) {
                normalized[i][j] = (signal[i][j].getReal() - min) / range * 255.0;

            }
        }
        return normalized;
    }

    public double[][] normalizeSample(Complex[][] signal, double max, double min) {
        double range = max - min;
        double[][] normalized = new double[signal.length][signal.length];
        for (int i = 0; i < signal.length; i++) {
            for (int j = 0; j < signal.length; j++) {
                normalized[i][j] = (signal[i][j].getReal() - min) / range * 255.0;

            }
        }
        return normalized;
    }

    public BufferedImage getIdealImage() {
        return image;
    }

    public BufferedImage getSampleIm() {
        return sampleIm;
    }

    public double getMax(Complex[][] signal) {
        double max = 0.0;
        for (Complex[] i : signal) {
            for (int j = 0; j < signal.length; j++) {
                if (i[j].getReal() > max) {
                    max = i[j].getReal();
                }
            }
        }
        return max;
    }

    public double getMin(Complex[][] signal) {
        double min = 0.0;
        for (Complex[] i : signal) {
            for (int j = 0; j < signal.length; j++) {
                if (i[j].getReal() < min) {
                    min = i[j].getReal();
                }
            }
        }
        return min;
    }

    public Complex[][] radialSampling(int samples) throws Exception {
//        sampleArray = new ComplexDouble[size][size];
        sampleArray = ImageProcessor.zero(size);
        sampleCoordinates = new ArrayList<>();

        for (int i = size /Math.round(samples/2); i < size / 2; i += size / Math.round(samples/2)) {
            sampleCoordinates.addAll(getCircularSampleCoordinates(new Point(size / 2, size / 2), i, samples));
        }
        for (Point i : sampleCoordinates) {
            sampleArray[i.y][i.x] = idealWaveArray[i.y][i.x];
        }

        int vMax = 0;
        int hMax = 0;
        int counter = 0;
        for (int i = 0; i < sampleArray.length; i++) {
            counter = 0;
            for (int j = 0; j < sampleArray[0].length; j++) {
                if (sampleArray[i][j].getReal() != 0 && sampleArray[i][j].getImaginary() != 0) {
                    counter++;
                }
            }
            if (counter > hMax) {
                hMax = counter;
            }
        }
        for (int i = 0; i < sampleArray.length; i++) {
            counter = 0;
            for (int j = 0; j < sampleArray[0].length; j++) {
                if (sampleArray[j][i].getReal() != 0 && sampleArray[j][i].getImaginary() != 0) {
                    counter++;
                }
            }
            if (counter > vMax) {
                vMax = counter;
            }
        }

        System.out.println("Horizontal = " + hMax);
        System.out.println("Vertical = " + vMax);

        
        double max = getMax(sampleArray);
        double min = getMin(sampleArray);
        double[][] normalized = normalizeSample(sampleArray, max, min);
        sampleIm = display(normalized, "Radial Samples");

        return sampleArray;
    }

    public Complex[][] spiralSampling(int samples) {
        double radius = 1;
        System.out.println(radius);
        sampleCoordinates = getSpiralCoordinates(samples, radius);

        sampleArray = ImageProcessor.zero(size);
//        sampleArray = new ComplexDouble[size][size];
        for (Point i : sampleCoordinates) {
            sampleArray[i.y][i.x] = idealWaveArray[i.y][i.x];
        }

        int vMax = 0;
        int hMax = 0;
        int counter = 0;
        for (int i = 0; i < sampleArray.length; i++) {
            counter = 0;
            for (int j = 0; j < sampleArray[0].length; j++) {
                if (sampleArray[i][j].getReal() != 0 && sampleArray[i][j].getImaginary() != 0 ) {
                    counter++;
                }
            }
            if (counter > hMax) {
                hMax = counter;
            }
        }
        for (int i = 0; i < sampleArray.length; i++) {
            counter = 0;
            for (int j = 0; j < sampleArray[0].length; j++) {
                if (sampleArray[j][i].getReal() != 0 && sampleArray[j][i].getImaginary() != 0) {
                    counter++;
                }
            }
            if (counter > vMax) {
                vMax = counter;
            }
        }

        System.out.println("Horizontal = " + hMax);
        System.out.println("Vertical = " + vMax);

        double max = getMax(sampleArray);
        double min = getMin(sampleArray);
        double[][] normalized = normalizeSample(sampleArray, max, min);
        sampleIm = display(normalized, "Spiral Samples");

        return sampleArray;
    }

    public Complex[][] rectilinearSampling(int samples) {
        int intervals = (int) ((float) size / Math.ceil((float) Math.sqrt(samples)));
        System.out.println(intervals);
        sampleArray = ImageProcessor.zero(size);
        sampleCoordinates = new ArrayList<>();
        for (int i = 0; i < size; i += intervals) {
            for (int j = 0; j < size; j += intervals) {
                sampleArray[i][j] = idealWaveArray[i][j];
                sampleCoordinates.add(new Point(j, i));
            }
        }

        double max = getMax(sampleArray);
        double min = getMin(sampleArray);
        double[][] normalized = normalizeSample(sampleArray, max, min);
        sampleIm = display(normalized, "Rectilinear Samples");

        return sampleArray;
    }

    public Complex[][] randomSampling(int Samples){
        Random rand = new Random();
        sampleArray = ImageProcessor.zero(size);
        sampleCoordinates = new ArrayList<>();
        for(int i = 0; i < Samples; i++){
            sampleCoordinates.add(new Point(rand.nextInt(size),rand.nextInt(size))); 
        }
        for(Point p : sampleCoordinates){
            sampleArray[p.y][p.x] = idealWaveArray[p.y][p.x];
        }
        System.out.println("Num of Random samples: " + sampleCoordinates.size());
        
        double max = getMax(sampleArray);
        double min = getMin(sampleArray);
        double[][] normalized = normalizeSample(sampleArray, max, min);
        sampleIm = display(normalized, "Random Samples");
        
        return sampleArray;
    }
    
    private ArrayList<Point> getSpiralCoordinates(int samples, double radius) {
        ArrayList<Point> spiralPoints = new ArrayList<>();
        int x, y;
        int mid = size / 2;
        int counter = 0;

        for (int t = 0; t < samples; t++) {
            x = Math.round((float) (radius / (2 * Math.PI) * t * Math.cos(t)));
            y = Math.round((float) (radius / (2 * Math.PI) * t * Math.sin(t)));
            if (x < mid && x > -mid && y < mid && y > -mid) {
                spiralPoints.add(new Point(mid + x, mid + y));
                counter++;
            }

        }
        System.out.println(counter);
        return spiralPoints;
    }

    private ArrayList<Point> getCircularSampleCoordinates(Point mid, int radius, int samples) {
        ArrayList<Point> coordinates = new ArrayList<>();
        double phase = 0.0;
        double phaseShift = 360.0 / samples;
        int a = 0;//x-coordinate
        int b = 0;//y-coordinate
        for (int i = 0; i < samples; i++) {
            a = Math.round(((float) Math.cos(phase * (Math.PI / 180)) * radius));
            b = Math.round(((float) Math.sin(phase * (Math.PI / 180)) * radius));
            coordinates.add(new Point(mid.x + a, mid.y - b));
            phase += phaseShift;
        }

        return coordinates;
    }

    public Complex[][] getIdeal() {
        return idealWaveArray;
    }

    public Complex[][] getSample() {
        return sampleArray;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public ArrayList<Point> getSampleCoordinates() {
        return sampleCoordinates;
    }

    public static void main(String[] args) {
        try {
            SampleGenerator generator = new SampleGenerator();

            int size = 1024;

            Complex[][] sine = generator.generateXSine(size, 1.0 / 64, true);
            sine = generator.combine(generator.generateYSine(size, 1.0 / 128, false));
            generator.randomSampling(10000);
        } catch (Exception ex) {
            Logger.getLogger(SampleGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
