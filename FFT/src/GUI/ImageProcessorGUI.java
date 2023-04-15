/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import ImageProcessing.ImageProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;

public class ImageProcessorGUI extends JPanel implements ActionListener {

    public static JProgressBar status;
    private final JFileChooser fc;
    private File imageFile;
    private final JButton process, stop, choose, loadOriginal, loadResult, loadSine;
    private final JLabel dimension;
    private final JComboBox<String> processes, windows;
    private final JPanel southPanel, northPanel;
    ZoomPane drawPanel;
    private final Timer timer;
    private ImageProcessor processor;
    private final JPanel innerSouthPanel;
    private final JCheckBox useOriginal;

    public ImageProcessorGUI() {
        super(new BorderLayout());
        southPanel = new JPanel();
        southPanel.setLayout(new GridLayout());
        innerSouthPanel = new JPanel();
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout());
        drawPanel = new ZoomPane();
//File Chooser------------------------------------------------------------------
        fc = new JFileChooser();
        fc.setCurrentDirectory(new File("./resources"));
//Buttons-----------------------------------------------------------------------
        choose = new JButton("Choose File");
        choose.addActionListener(this);
        loadOriginal = new JButton("Load Original");
        loadOriginal.addActionListener(this);
        loadResult = new JButton("Load Result");
        loadResult.addActionListener(this);
        process = new JButton("Process");
        process.addActionListener(this);
        stop = new JButton("STOP");
        stop.addActionListener(this);
        stop.setEnabled(false);
        loadSine = new JButton("Load Wave");
        loadSine.addActionListener(this);
//Labels -----------------------------------------------------------------------
        dimension = new JLabel();
        dimension.setPreferredSize(new Dimension(100, 45));
        dimension.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Dimensions: "),
                dimension.getBorder()));
//Combo Box---------------------------------------------------------------------
        String[] algorithmOptions = {"FFT", "IFFT", "Gaussian Gridding", "Gaussian Blur", "Ridge Detection", "Box Blur", "Edge Detection",
            "Generate Sine", "Radial Sampling", "Spiral Sampling", "Rectilinear Sampling", "Random Sampling"};
        processes = new JComboBox<>(algorithmOptions);
        processes.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Process: "),
                processes.getBorder()));
        String[] windowOptions = {"None", "Blackman", "Hanning", "Kaiser", "Rectangle"};
        windows = new JComboBox<>(windowOptions);
        windows.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Windows: "),
                windows.getBorder()));
//Check Box---------------------------------------------------------------------
        useOriginal = new JCheckBox("Use original Image:", true);
        useOriginal.addActionListener(this);

//------------------------------------------------------------------------------
        northPanel.add(processes);
        northPanel.add(windows);
        northPanel.add(dimension);
        innerSouthPanel.add(choose);
        innerSouthPanel.add(process);
        southPanel.add(innerSouthPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);
        add(southPanel, BorderLayout.SOUTH);
        add(new JScrollPane(drawPanel), BorderLayout.CENTER);

        timer = new Timer(20, this);
        timer.start();
    }

    public static class ImagePanel extends JPanel {

        public static int xTranslation = 0;
        public static int yTranslation = 0;
        public static int ZOOM = 1;
        JLabel imageIcon = new JLabel();
        BufferedImage image;

        public ImagePanel() {
            super();
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(600, 600));
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.image != null) {
                g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
            }

        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source == choose) {
            innerSouthPanel.remove(loadOriginal);
            innerSouthPanel.remove(loadResult);
            int option = fc.showOpenDialog(null);

            if (option == JFileChooser.APPROVE_OPTION) {
                imageFile = fc.getSelectedFile();
                try {
                    processor = new ImageProcessor(imageFile);
                    dimension.setText(String.valueOf(processor.getHeight()) + " x " + String.valueOf(processor.getWidth()));
                    drawPanel.setBackground(ImageIO.read(new File("./resources/Results/grayScaleCopy.png")));
                    System.out.println("Just opened an image.");
                } catch (IOException ex) {
                    Logger.getLogger(ImageProcessorGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        if (source == process) {
            if (processes.getSelectedItem() == "FFT") {
                if (windows.getSelectedItem() == "Rectangle") {
                    processor.setWindow(ImageProcessor.WindowType.RECTANGULAR);
                }
                if (windows.getSelectedItem() == "Kaiser") {
                    processor.setWindow(ImageProcessor.WindowType.KAISER);
                }
                if (windows.getSelectedItem() == "Hanning") {
                    processor.setWindow(ImageProcessor.WindowType.HANNING);
                }
                if (windows.getSelectedItem() == "None") {
                    processor.setWindow(ImageProcessor.WindowType.NONE);
                }
                if (windows.getSelectedItem() == "Blackman") {
                    processor.setWindow(ImageProcessor.WindowType.BLACKMAN);
                }
                innerSouthPanel.remove(loadResult);
                processor.setProcess(ImageProcessor.Process.FFT);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadOriginal);
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "IFFT") {

                processor.setProcess(ImageProcessor.Process.IFFT);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "Gaussian Blur") {
                innerSouthPanel.remove(loadOriginal);
                innerSouthPanel.remove(loadResult);
                processor.setProcess(ImageProcessor.Process.GAUSSIAN_BLUR);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadOriginal);
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "Ridge Detection") {
                innerSouthPanel.remove(loadOriginal);
                innerSouthPanel.remove(loadResult);
                processor.setProcess(ImageProcessor.Process.RIDGE_DETECTION);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadOriginal);
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "Edge Detection") {
                innerSouthPanel.remove(loadOriginal);
                innerSouthPanel.remove(loadResult);
                processor.setProcess(ImageProcessor.Process.SOBEL);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadOriginal);
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "Box Blur") {
                innerSouthPanel.remove(loadOriginal);
                innerSouthPanel.remove(loadResult);
                processor.setProcess(ImageProcessor.Process.BOX_BLUR);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadOriginal);
                southPanel.add(useOriginal);
            }
            if (processes.getSelectedItem() == "Generate Sine") {
                innerSouthPanel.remove(loadOriginal);
                innerSouthPanel.remove(loadResult);
                innerSouthPanel.remove(useOriginal);
                
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GENERATE_SINE);
                processor.run();
                drawPanel.setBackground(processor.getImage());
                dimension.setText(processor.getImage().getHeight() + " x " + processor.getImage().getWidth());
            }
            if (processes.getSelectedItem() == "Radial Sampling") {
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GENERATE_CIRCULAR_SAMPLE);
                processor.run();
                drawPanel.setBackground(processor.getImage());
                innerSouthPanel.add(loadSine);
            }
            if (processes.getSelectedItem() == "Spiral Sampling") {
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GENERATE_SPIRAL_SAMPLE);
                processor.run();
                drawPanel.setBackground(processor.getImage());
                innerSouthPanel.add(loadSine);
            }
            if (processes.getSelectedItem() == "Rectilinear Sampling") {
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GENERATE_RECTILINEAR_SAMPLE);
                processor.run();
                drawPanel.setBackground(processor.getImage());
                innerSouthPanel.add(loadSine);
            }
            if (processes.getSelectedItem() == "Random Sampling") {
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GENERATE_RANDOM_SAMPLE);
                processor.run();
                drawPanel.setBackground(processor.getImage());
                innerSouthPanel.add(loadSine);
            }
            if (processes.getSelectedItem() == "Gaussian Gridding") {
                innerSouthPanel.add(loadOriginal);
                innerSouthPanel.remove(loadResult);
                if (processor == null) {
                    processor = new ImageProcessor();
                }
                processor.setProcess(ImageProcessor.Process.GAUSSIAN_GRIDDING);
                processor.run();
                drawPanel.setBackground(processor.getResultImage());
                innerSouthPanel.add(loadSine);
                innerSouthPanel.add(useOriginal);                
            }

        }
//For displaying the Original photo and the Result photo------------------------
        if (source == loadOriginal) {
            drawPanel.setBackground(processor.getImage());
            innerSouthPanel.remove(loadOriginal);
            innerSouthPanel.add(loadResult);
        }
        if (source == loadResult) {
            drawPanel.setBackground(processor.getResultImage());
            innerSouthPanel.remove(loadResult);
            innerSouthPanel.add(loadOriginal);
        }
        if(source == loadSine){
            innerSouthPanel.remove(loadResult);
            innerSouthPanel.remove(loadOriginal);
            drawPanel.setBackground(processor.loadSine());
        }
        if (source == useOriginal) {
            if (useOriginal.isSelected()) {
                processor.setUseResult(true);
                System.out.println("use original");
            } else {
                processor.setUseResult(false);
                System.out.println("do not use original");
            }
        }

        this.revalidate();
        this.repaint();
        drawPanel.repaint();
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Image Processor");
        // kill all threads when frame closes
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ImageProcessorGUI());
        frame.pack();
//        frame.setResizable(false);
        // position the frame in the middle of the screen
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenDimension = tk.getScreenSize();
        Dimension frameDimension = frame.getSize();
        frame.setLocation((screenDimension.width - frameDimension.width) / 2,
                (screenDimension.height - frameDimension.height) / 2);
        frame.setVisible(true);
    }

}
