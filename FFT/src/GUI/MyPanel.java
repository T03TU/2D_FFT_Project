/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package GUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author
 * https://stackoverflow.com/questions/63583595/java-graphics2d-zoom-on-mouse-location
 */
public class MyPanel extends JPanel implements MouseWheelListener {

    private double zoom = 1;
    private int zoomPointX;
    private int zoomPointY;
    private Image image;

    public MyPanel(Image image) {
        super();
//        this.setPreferredSize(new Dimension(600,600));
        this.image = image;
        addMouseWheelListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomPointX = e.getX();
        zoomPointY = e.getY();
        if (e.getPreciseWheelRotation() < 0) {
            zoom -= 0.1;
        } else {
            zoom += 0.1;
        }
        if (zoom < 0.01) {
            zoom = 0.01;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        super.paintComponent(g2D);
        g2D.drawImage(image, 0, 0, this);
        AffineTransform at = g2D.getTransform();
        at.translate(zoomPointX, zoomPointY);
        at.scale(zoom, zoom);
        at.translate(-zoomPointX, -zoomPointY);
        g2D.setTransform(at);
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Zoom");
        MyPanel panel = new MyPanel(ImageIO.read(new File("./resources/AfghanGirl.jpg")));
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

}
