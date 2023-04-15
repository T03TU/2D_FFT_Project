package Kernels;

import GUI.ImageProcessorGUI;
import ImageProcessing.ImageProcessor;
import Resources.Complex;
import Resources.ComplexDouble;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author toetu
 */
public class Kernel {

    public enum kernelType {
        GAUSSIAN_BLUR, PROLATE_SPHEROIDAL, KAISER_BESSEL, BOX_BLUR, RIDGE_DETECTION, SOBEL;
    }
    private int size;
    private Complex[][] kernel;
    private kernelType name;
    //Determines which Kernel to use.

    public Kernel(kernelType name) {
        this.name = name;
        if (null != name) {
            switch (name) {
                case GAUSSIAN_BLUR -> {
                    //Use Gaussian Kernel
                    //input = new Scanner(System.in);
                    double sd = Double.parseDouble(JOptionPane.showInputDialog("Please enter in an appropriate radius"));

                    if (sd % 2 == 1 || sd == 1) {
                        size = (int) (3 * sd);
                        gaussianKernel(sd);
                    } else if (sd == 0) {
                        size = 1;
                        kernel = new ComplexDouble[size][size];
                        kernel[0][0] = new ComplexDouble(1.0, 0.0);
                    } else {
                        size = (int) (3 * sd + 1);
                        gaussianKernel(sd);
                    }
                }
                case PROLATE_SPHEROIDAL -> {
                    //Use Prolate Spheroidal kernel
                }
                case KAISER_BESSEL -> {
                    //Use Kaiser Bessel kernel
                    int input =  Integer.parseInt(JOptionPane.showInputDialog("Please enter size of kernel"));
                    if(input % 2 != 0)
                        size = input + 1;
                    double alpha =  Double.parseDouble(JOptionPane.showInputDialog("Please enter alpha value"));
                    kaiserKernel(alpha);
                }
                case BOX_BLUR -> {//Mean blur filter
                    this.size = Integer.parseInt(JOptionPane.showInputDialog("Please enter size of the kernel"));
                    kernel = new ComplexDouble[this.size][this.size];
                    for (Complex[] i : kernel) {
                        for (int j = 0; j < kernel.length; j++) {
                            i[j] = new ComplexDouble(1.0, 0);
                        }
                    }
                }
                case RIDGE_DETECTION -> {
                    kernel = new ComplexDouble[3][3];
                    kernel[0][0] = new ComplexDouble(-1.0, 0);
                    kernel[0][1] = new ComplexDouble(-1.0, 0);
                    kernel[0][2] = new ComplexDouble(-1.0, 0);
                    kernel[1][0] = new ComplexDouble(-1.0, 0);
                    kernel[1][1] = new ComplexDouble(8.0, 0);
                    kernel[1][2] = new ComplexDouble(-1.0, 0);
                    kernel[2][0] = new ComplexDouble(-1.0, 0);
                    kernel[2][1] = new ComplexDouble(-1.0, 0);
                    kernel[2][2] = new ComplexDouble(-1.0, 0);
                }
                case SOBEL -> {
                    int vertical = JOptionPane.showConfirmDialog(null, "Vertical Sobel?", "Input", JOptionPane.YES_NO_OPTION);
                    if (vertical == 0) {
                        kernel = new ComplexDouble[3][3];
                        kernel[0][0] = new ComplexDouble(-1.0, 0);
                        kernel[0][1] = new ComplexDouble(0, 0);
                        kernel[0][2] = new ComplexDouble(1.0, 0);
                        kernel[1][0] = new ComplexDouble(-2.0, 0);
                        kernel[1][1] = new ComplexDouble(0.0, 0);
                        kernel[1][2] = new ComplexDouble(2.0, 0);
                        kernel[2][0] = new ComplexDouble(-1.0, 0);
                        kernel[2][1] = new ComplexDouble(0.0, 0);
                        kernel[2][2] = new ComplexDouble(1.0, 0);
                    } else if (vertical == 1) {
                        kernel = new ComplexDouble[3][3];
                        kernel[0][0] = new ComplexDouble(-1.0, 0);
                        kernel[0][1] = new ComplexDouble(-2, 0);
                        kernel[0][2] = new ComplexDouble(-1.0, 0);
                        kernel[1][0] = new ComplexDouble(0.0, 0);
                        kernel[1][1] = new ComplexDouble(0.0, 0);
                        kernel[1][2] = new ComplexDouble(0.0, 0);
                        kernel[2][0] = new ComplexDouble(1.0, 0);
                        kernel[2][1] = new ComplexDouble(2.0, 0);
                        kernel[2][2] = new ComplexDouble(1.0, 0);
                    }
                }

            }
            JFrame kernelDisplay = new JFrame("Kernel");
            kernelDisplay.setSize(new Dimension(500, 500));
            ImageProcessorGUI.ImagePanel displayer;
            displayer = new ImageProcessorGUI.ImagePanel();
            try {
                displayer.setImage(visualizeKernel());
            } catch (IOException ex) {
                Logger.getLogger(Kernel.class.getName()).log(Level.SEVERE, null, ex);
            }
            kernelDisplay.getContentPane().add(displayer);
            kernelDisplay.setVisible(true);
        }
    }

    private void normalizeKernel(){
        Complex max = getMaxVal();
        Complex[][] normalized = new ComplexDouble[size][size];
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                normalized[i][j] = kernel[i][j].divide(max);
            }
        }
        kernel = normalized;
    }
    
    private Complex getMaxVal(){
        double maxMod = 0.0;
        Complex maxKernelval = new ComplexDouble();
        Complex current;
        for(int i = 0 ; i < size; i++){
            for(int j = 0; j < size; j++){
                current = kernel[i][j];
                
                if(current.modulus() > maxMod)
                    maxKernelval = current;                
            }
        }
        
        return maxKernelval;
    }
    
    private void gaussianKernel(double sd) {
        kernel = new ComplexDouble[this.size][this.size];
        int mid = this.size / 2;
        for (int y = -mid; y <= mid; y++) {
            for (int x = -mid; x <= mid; x++) {
                double g = (1.0) / ((2 * Math.PI * (sd * sd)) * Math.exp((y * y + x * x) / (2 * (sd * sd))));
                kernel[y + mid][x + mid] = new ComplexDouble(g, 0);
            }
        }
        
//        normalizeKernel();
    }

//Kaiser Kernel-----------------------------------------------------------------
    private void kaiserKernel(double alpha){
        double piAlpha = Math.PI * alpha;
        
    }
    
    private double getZeroOrderModifiedBessel(double x) {
        double sum = 0;
        double term = 1;
        int m = 0;
        do {
            // calculate term carefully to avoid overflows
            term = 1.0;
            for (int i = 1; i <= m; i++) {
                term *= x / (2.0 * i);
            }
            term *= term; // square term
            sum += term;
            m++;
        } while (Math.abs(term / sum) > 1E-12);
        return sum;
    }
    
//Getters and Setters ----------------------------------------------------------
    public Complex[][] getKernel() {
        return kernel;
    }

    public int getSize() {
        return size;
    }

    public Complex convolve(Complex[][] image, int x, int y) {
        Complex sum = new ComplexDouble(0, 0);
        int mid = (int) Math.floor(size / 2);
        for (int offSetY = -mid; offSetY <= mid; offSetY++) {
            for (int offSetX = -mid; offSetX <= mid; offSetX++) {
                if (y + offSetY >= 0 && y + offSetY < image.length && x + offSetX >= 0 && x + offSetX < image[0].length) {
                    sum.addInPlace(image[y + offSetY][x + offSetX].multiply(kernel[mid + offSetY][mid + offSetX]));
                }

            }
        }
        if (this.name == kernelType.BOX_BLUR) {
            sum.multiplyInPlace(Math.pow(kernelSum().getReal(), -1));
        }

        return sum;
    }

    public Complex kernelSum() {
        Complex sum = new ComplexDouble(0, 0);
        for (Complex[] y : kernel) {
            for (int x = 0; x < kernel[0].length; x++) {
                sum.addInPlace(y[x]);
            }
        }
        return new ComplexDouble(sum.getReal(), 0);
    }

    public BufferedImage visualizeKernel() throws IOException {
        double maxReal = 0.0;

        for (Complex[] row : kernel) {
            for (int x = 0; x < kernel.length; x++) {
                if (row[x].getReal() > maxReal) {
                    maxReal = row[x].getReal();
                }
            }
        }
        
        double[][] realMatrix = new double[kernel.length][kernel.length];
        for (int y = 0; y < kernel.length; y++) {
            for (int x = 0; x < kernel.length; x++) {
                realMatrix[y][x] = kernel[y][x].getReal() / maxReal * 255;
            }
        }
        return ImageProcessor.pixels2Im(realMatrix, "Kernel", "png", BufferedImage.TYPE_BYTE_GRAY);
    }

    public kernelType getName() {
        return name;
    }

    public void setName(kernelType name) {
        this.name = name;
    }

    public static void main(String[] args) throws IOException {
        Kernel k = new Kernel(kernelType.GAUSSIAN_BLUR);

        for (int row = 0; row < k.getSize(); row++) {
            System.out.println(Arrays.toString(k.getKernel()[row]));
        }
        k.visualizeKernel();
        File image = new File("./resources/Chris.png");

        ImageProcessor processor = new ImageProcessor(image);
        Complex[][] im = processor.getGrayImageArray();
//        Complex[][] im = new ComplexDouble[3][3];
        Complex[][] newIm = new Complex[im.length][im[0].length];
//
//        int index = 0;
//        for(Complex[] i : im){
//            for(int j = 0; j < i.length; j++){
//                i[j] = new ComplexDouble((index + 1), 0);
//                index++;
//            }
//            System.out.println(Arrays.toString(i));
//        }
//        
    }
    /**
     * Solution from Stack Overflow
     *
     * Source:
     * https://stackoverflow.com/questions/39684820/java-implementation-of-gaussian-blur
     *
     * @param image
     * @param kernel
     * @param filterWidth
     * @return
     * @throws java.io.IOException
     */
    public static BufferedImage blur(BufferedImage image, int[] kernel, int filterWidth) throws IOException {
        if (kernel.length % filterWidth != 0) {
            throw new IllegalArgumentException("filter contains a incomplete row");
        }

        final int width = image.getWidth();
        final int height = image.getHeight();
        final int sum = IntStream.of(kernel).sum();

        int[] input = image.getRGB(0, 0, width, height, null, 0, width);

        int[] output = new int[input.length];

        final int pixelIndexOffset = width - filterWidth;
        final int centerOffsetX = filterWidth / 2;
        final int centerOffsetY = kernel.length / filterWidth / 2;

        // apply filter
        for (int h = height - kernel.length / filterWidth + 1, w = width - filterWidth + 1, y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 0;
                int g = 0;
                int b = 0;
                for (int filterIndex = 0, pixelIndex = y * width + x;
                        filterIndex < kernel.length;
                        pixelIndex += pixelIndexOffset) {
                    for (int fx = 0; fx < filterWidth; fx++, pixelIndex++, filterIndex++) {
                        int col = input[pixelIndex];
                        int factor = kernel[filterIndex];

                        // sum up color channels seperately
                        r += ((col >>> 16) & 0xFF) * factor;
                        g += ((col >>> 8) & 0xFF) * factor;
                        b += (col & 0xFF) * factor;
                    }
                }
                r /= sum;
                g /= sum;
                b /= sum;
                // combine channels with full opacity
                output[x + centerOffsetX + (y + centerOffsetY) * width] = (r << 16) | (g << 8) | b | 0xFF000000;
            }
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        result.setRGB(0, 0, width, height, output, 0, width);
        File newIm = new File("./resources/Results", "Blurred.png");
        ImageIO.write(result, "png", newIm);

        return result;
    }
}
//
//    public Complex convolve(Complex[][] image, int x, int y) {
//        Complex sum = new ComplexDouble(0, 0);
//        double tempSum = 0.0;
//        double tempVal = 0;
//        int mid = (int) Math.floor(size / 2);
//        for (int offSetY = -mid; offSetY <= mid; offSetY++) {
//            for (int offSetX = -mid; offSetX <= mid; offSetX++) {
////                if((y + offSetY + x + offSetX)%100 < 50){
////                    tempVal = 255.0;
////                }else{
////                    tempVal = 0;
////                }
//                if (y + offSetY >= 0 && y + offSetY < image.length && x + offSetX >= 0 && x + offSetX < image[0].length) {
////                    sum.addInPlace(image[y + offSetY][x + offSetX].multiply(1.0/(size*size)));
////                      sum.addInPlace(new ComplexDouble(image[y + offSetY][x + offSetX].getReal()/(1.0*size*size),0));
////                      tempSum += image[y + offSetY][x + offSetX].getReal()/(1.0*size*size);
////                    tempSum += tempVal/(1.0*size*size);
//                    sum.addInPlace(image[y + offSetY][x + offSetX].multiply(kernel[mid + offSetY][mid + offSetX]));
////                    sum.addInPlace(new ComplexDouble(tempVal,0).multiply(kernel[mid + offSetY][mid + offSetX]));
//                }
//
//            }
//        }
//        if (this.name == kernelType.BOX_BLUR) {
//            sum.multiplyInPlace(Math.pow(kernelSum().getReal(), -1));
//        }
//
//        return sum;
//    }
