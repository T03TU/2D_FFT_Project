/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Transforms;

import Resources.Complex;
import Windows.Window;
import java.util.Arrays;

/**
 *
 * @author toetu
 */
public class FFT2 extends FFT {
    
    public FFT2() {
        super();
    }

    public FFT2(Window window){
        super(window);
    }
    //2 dimensional FFT (Row By Col approach)
    public Complex[][] transform(Complex[][] signal) {
        int channels = signal.length;
        //Perform the FFT_2D on the rows first
        Complex[][] ft = new Complex[channels][channels];
        for (int row = 0; row < channels; row++) {
            ft[row] = transform(signal[row]);
        }
        //Now compute the FFT_2D of each column of the transformed rows.
        for (int col = 0; col < channels; col++) {
            Complex[] colVals = new Complex[channels];
            for (int row = 0; row < channels; row++) {
                colVals[row] = ft[row][col];//Extract column values of the transformed rows of the signal
            }
            Complex[] fftColVals = transform(colVals);//Perform FFT_2D on the column values
            for (int row = 0; row < channels; row++) {
                ft[row][col] = fftColVals[row];//substitute the values back to the 2D Fourier transform Matrix
            }
        }
        return ft;
    }
    
    //2 dimensional FFT (Row By Col approach)
    public Complex[][] reverse(Complex[][] signal) {
        int channels = signal.length;
        
        Complex[][] ift = Arrays.copyOf(signal, channels);
        for (int col = 0; col < channels; col++) {
            Complex[] colVals = new Complex[channels];
            
            for (int row = 0; row < channels; row++) {
                colVals[row] = ift[row][col];//Extract column values of the transformed rows of the signal
            }
            Complex[] fftColVals = reverse(colVals);//Perform DIF on the column values
            for (int row = 0; row < channels; row++) {
                ift[row][col] = fftColVals[row];//substitute the values back to the 2D Fourier transform Matrix
            }
        }
        for (int row = 0; row < channels; row++) {
            ift[row] = reverse(signal[row]);
        }
        
        return ift;
        
    }

    public void disp2D(Complex[][] signal) {
        for (Complex[] s : signal) {
            System.out.println(Arrays.toString(s));
        }
    }

    public Complex[][] fftShift(Complex[][] signal) {
        int channels = signal.length;
        int n2 = channels / 2 - 1;
        Complex[][] shiftedfft = new Complex[channels][channels];
        //Col shifts
        for (int row = 0; row <= n2; row++) {
            int colIncrem = 1;
            for (int i = n2; i >= 0; i--) {
                shiftedfft[row][n2 - i] = signal[row][i];
                shiftedfft[row][i] = signal[row][n2 - i];
//                System.out.println("Switching " + signal[row][i].toString()
//                        + " with " + signal[row][n2 - i].toString());
                shiftedfft[row + n2 + 1][n2 - i] = signal[row + n2 + 1][i];
                shiftedfft[row + n2 + 1][i] = signal[row + n2 + 1][n2 - i];
//                System.out.println("Switching " + signal[row + n2 + 1][i].toString()
//                        + " with " + signal[row + n2 + 1][n2 - i].toString());
                shiftedfft[row][n2 + colIncrem] = signal[row][n2 + i + 1];
                shiftedfft[row][n2 + i + 1] = signal[row][n2 + colIncrem];
//                System.out.println("Switching " + signal[row][n2 + colIncrem].toString()
//                        + " with " + signal[row][n2 + i + 1].toString());
                shiftedfft[row + n2 + 1][n2 + colIncrem] = signal[row + n2 + 1][n2 + i + 1];
                shiftedfft[row + n2 + 1][n2 + i + 1] = signal[row + n2 + 1][n2 + colIncrem];
//                System.out.println("Switching " + signal[row + n2 + 1][n2 + colIncrem].toString()
//                        + " with " + signal[row + n2 + 1][n2 + i + 1].toString());
                colIncrem++;
            }
        }
        int rowIncrem = 0;
        Complex[] r;
        Complex[] s;
        //Row Shifts
        for (int row = 0; row < channels / 4; row++) {
            r = shiftedfft[n2 - rowIncrem];
            s = shiftedfft[row];
            shiftedfft[row] = r;
            shiftedfft[n2 - rowIncrem] = s;
//            System.out.println(Arrays.toString(r) + " & " + Arrays.toString(s));
            r = shiftedfft[(2 * n2 + 1) - rowIncrem];
            s = shiftedfft[n2 + row + 1];
            shiftedfft[n2 + row + 1] = r;
            shiftedfft[(2 * n2 + 1) - rowIncrem] = s;
//            System.out.println(Arrays.toString(r) + " & " + Arrays.toString(s));
            rowIncrem++;
        }
        return shiftedfft;
    }

//    public static void main(String[] args) {
//        int n = 4;
//        FFT2 transformer = new FFT2();
//        Complex[][] signal = new Complex[n][n];
//        Random rand = new Random();
//
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                signal[i][j] = new ComplexDouble(rand.nextDouble(), rand.nextDouble());
//            }
//        }
//        System.out.println("Signal: ");
//        transformer.disp2D(signal);
//        Complex[][] ft = transformer.transform(signal);
//        System.out.println("Fourier Transform:");
//        transformer.disp2D(ft);
//        Complex[][] ift = transformer.reverse(ft);
//        System.out.println("Inverse Fourier Transform: ");
//        transformer.disp2D(ift);
//    }

}
