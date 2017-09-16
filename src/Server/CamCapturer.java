package Server;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

/*
 * Captura frames da webcam padrão
 */
public class CamCapturer {
	public static BufferedImage takePhoto() throws IOException {
		
		// Define imagem e imagem padrão
		BufferedImage image;
		BufferedImage defaultImage = ImageIO.read(new File("images/nocamdefault.png"));
		
		// Inicializa webcam padrão
		Webcam webcam = Webcam.getDefault();
		
		// Se não detecta webcam usa imagem padrão
		if (webcam != null) {
			System.out.println("Webcam: " + webcam.getName());
			// Habilita dimensões customizadas (e.g. HD)
			Dimension[] nonStandardResolutions = new Dimension[] {
				WebcamResolution.PAL.getSize(),
				WebcamResolution.HD720.getSize(),
				new Dimension(2000, 1000),
				new Dimension(1000, 500),
			};
			
			// Define a resolução da imagem para 720p
			webcam.setCustomViewSizes(nonStandardResolutions);
			webcam.setViewSize(WebcamResolution.HD720.getSize());
			
			// Liga a webcam
			webcam.open();
		} else {
			// Return default image if no webcam detected
			System.out.println("No webcam detected");
			return defaultImage;
		}
		
		// Captura image
		image = webcam.getImage();

		// Fecha a webcam
		webcam.close();
		
		// Retorna imagem captura
		return image;
	}
}
