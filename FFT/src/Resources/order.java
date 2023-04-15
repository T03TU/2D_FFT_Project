/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Resources;

import java.util.Arrays;

/**
 *
 * @author toetu

 */
public class order{
    //Bit reverse algorithm
    private static int[] bitReverseOrder(int[] array) {
        int n = array.length;
        int[] bitReversedCopy = new int[n];

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
    
//    public static void main(String[] args) {
//        int[] list = new int[16];
//        
//        for(int i = 0 ; i < list.length; i++){
//            list[i] = i + 1;
//        }
//
//        System.out.println("Original: "+Arrays.toString(list));
//        list = order.bitReverseOrder(list);
//        System.out.println("BitReversed: " + Arrays.toString(list));
//        list = order.bitReverseOrder(list);
//        System.out.println("Original: "+Arrays.toString(list));
//        
//        
//        
//    }
}
