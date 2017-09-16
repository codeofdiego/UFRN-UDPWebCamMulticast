package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Display {
    JFrame frame;
	JLabel labelImage;
	JLabel labelTime;
    
	public Display() throws IOException
    {
		labelImage = new JLabel();
		labelTime = new JLabel();
        frame = new JFrame("Camera Monitor");
        frame.setSize(1280,720);
        
        // Label style
        labelTime.setFont(labelTime.getFont().deriveFont(Font.BOLD, 12));
        labelTime.setForeground(Color.WHITE);
        labelTime.setHorizontalAlignment(JLabel.CENTER);
        labelTime.setVerticalAlignment(JLabel.BOTTOM);
        
        // Add label to image
        labelImage.setLayout(new BorderLayout());
        labelImage.add(labelTime);
        
        frame.add(labelImage);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	
	public void updateImage(BufferedImage image, Date timestamp) {
		ImageIcon icon = new ImageIcon(image);
        labelTime.setText(timestamp.toString());
        labelImage.setIcon(icon);
        frame.pack();
	}
}
