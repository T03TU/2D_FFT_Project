// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// https://ateraimemo.com/Swing/ZoomAndPanPanel.html

package GUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    try {
      Image img = ImageIO.read(new File("./resources/AfghanGirl.jpg"));
      add(new JScrollPane(new ZoomAndPanePanel((BufferedImage) img)));
    } catch (IOException ex) {
    }
    setPreferredSize(new Dimension(320, 240));
  }

//  public static void main(String... args) {
//    EventQueue.invokeLater(() -> {
//        createAndShowGui();
//    });
//  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
    }
    JFrame frame = new JFrame("ZoomAndPanPanel");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
