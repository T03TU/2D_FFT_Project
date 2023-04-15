package Kernels;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author toetu
 */
public class PowTest {

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) throws IOException {
//        double ans = Math.pow(-2,0.5);
//        System.out.println(ans);
        float[] matrix = new float[400];
        for (int i = 0; i < 400; i++) {
            matrix[i] = 1.0f / 400.0f;
        }
        BufferedImageOp c = new ConvolveOp(new Kernel(20, 20, matrix), ConvolveOp.EDGE_NO_OP, null);
        File file = new File("./resources/Eye.jpg");
        BufferedImage src = ImageIO.read(file);
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);;
        File newIm = new File("./resources/Results", "ConvolveOpResults.png");
        BufferedImage blurred = c.filter(src, dest);
        ImageIO.write(blurred, ".png", newIm);
    }
}
