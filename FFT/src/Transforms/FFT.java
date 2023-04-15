package Transforms;

import Resources.Complex;
import Windows.Window;
import java.util.Arrays;

/**
 *
 * @author toetu
 */
public class FFT {

    protected Window window;
    
    public FFT() {
    }
    public FFT(Window window) {
        this.window = window;
    }

    //1-Dimensional Radix-2 DIT algorithm 
    public Complex[] transform(Complex[] signal) {
        return dit(signal, false);
    }

    public Complex[] reverse(Complex[] signal) {
        int channels = signal.length;
        Complex[] ift = dit(signal, true);

        for (int i = 0; i < channels; i++) {
            ift[i].multiplyInPlace(1.0 / channels);
        }
        return ift;
    }

    private Complex[] dit(Complex[] signal, boolean inverse) {
        int channels = signal.length;
        Complex[] working = signal;
        
        if (channels % 2 != 0) {
            throw new IllegalArgumentException("Invalid FFT size: " + channels);
        }
        
        if(window != null && !inverse)
            working = window.applyWindow(working);
        
        Complex[] ft = bitReverse(working);
        Complex omega = null;
        
        for (int i = 2; i <= channels; i *= 2) {//Determines the ith roots of unity
            if (!inverse) {
                omega = signal[0].getRootOfUnity(i);
            } else {
                omega = signal[0].getConjugate(signal[0].getRootOfUnity(i));
            }
            for (int j = 0; j < channels; j += i) {
                Complex tf = signal[0].getUnit(); //Helps extract the twiddle factors from the roots of unity
                for (int k = 0; k < i / 2; k++) {
                    Complex point2 = tf.multiply(ft[i / 2 + j + k]);
                    Complex point1 = ft[j + k];
                    ft[j + k] = point1.add(point2);//point1 is multiplied by 1 here
                    ft[i / 2 + j + k] = point1.subtract(point2);//On signal diagram point2 is multiplied by -1. This caused the subtraction in the 'Butterfly Operation'
                    tf.multiplyInPlace(omega); //get the next twiddle factor. 
                }
            }
        }
        return ft;
    }

    private Complex[] dif(Complex[] signal) {
        int channels = signal.length;
        Complex[] ift = Arrays.copyOf(signal, channels);

        for (int i = channels; i >= 2; i /= 2) {
            Complex omega = signal[0].getConjugate(signal[0].getRootOfUnity(i));
            for (int j = 0; j < channels; j += i) {
                Complex x = signal[0].getUnit();
                for (int k = 0; k < i / 2; k++) {
                    Complex point = ift[j + k];
                    ift[j + k] = point.add(ift[j + k + i / 2]);
                    ift[j + k + i / 2] = point.subtract(ift[j + k + i / 2]);
                    ift[j + k + i / 2].multiplyInPlace(x);
                    x.multiplyInPlace(omega);
                }
            }
        }
        //Unscramble the ft
        return bitReverse(ift);
    }

    //Bit reverse algorithm
    private static Complex[] bitReverse(Complex[] array) {
        int n = array.length;
        Complex[] bitReversedCopy = new Complex[n];

        for (int i = 0; i < n; i++) {
            int index = i;
            int reversedIndex = 0;
            for (int j = 1; j < n; j *= 2) {//increment by multiplying to 2
                int BitPosition = index & 1;
                reversedIndex = (reversedIndex << 1) + BitPosition;
                index = (index >>> 1);
            }
            bitReversedCopy[reversedIndex] = array[i];

        }
        return bitReversedCopy;
    }

////Test
//    public static void main(String[] args) {
//        Complex[] signal = new ComplexDouble[4];
//        FFT fft = new FFT();
//        for (int i = 0; i < signal.length; i++) {
//            signal[i] = new ComplexDouble(i + 1, 0);
//        }
//        System.out.println("Original signal: " + Arrays.toString(signal));
//        Complex[] transformed = fft.transform(signal);
//        System.out.println("Transformed signal: " + Arrays.toString(transformed));
//        Complex[] reversed = fft.reverse(transformed);
//        System.out.println("Reversed signal: " + Arrays.toString(reversed));
//    }
}
