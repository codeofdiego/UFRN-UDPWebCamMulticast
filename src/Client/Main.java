package Client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Date;

import javax.imageio.ImageIO;

public class Main {
	// Constantes
	final static int PORTA = 5555;
	final static String HOST = "239.1.1.66";
	final static int HEADER_SIZE = 14;
	final static int IMAGE_START = 128;
	final static int DATAGRAM_MAX_SIZE = 65507 - HEADER_SIZE;
	final static int SERVER_MAX_SIZE = 65407 - HEADER_SIZE;
	
	public static void main(String[] args) {
		int porta = 5555;
		int currentImage = -9999;
		int fragmentsReceived = 0;
		byte[] imageData = new byte[DATAGRAM_MAX_SIZE];
		boolean canReceive = false;
		
		try {
			
			// Inicializa tela
			Display display = new Display();
			
			// Configura rede e inicializa grupo e socket
			System.setProperty("java.net.preferIPv4Stack", "true");
			InetAddress group = InetAddress.getByName("239.1.1.66");
			MulticastSocket socketCliente = new MulticastSocket(porta);
			socketCliente.joinGroup(group);

			// Debug
			System.out.println("Aguardando imagens: ");
		
			while (true) {
				byte[] buffer = new byte[DATAGRAM_MAX_SIZE];
		
				// Constri pacote de retorno e espera pela resposta
				DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
				socketCliente.receive(pacoteRecebido);
				
				// Extrai dados do pacote
				byte[] fragment = pacoteRecebido.getData();
				
				// Extrai informações dos dados
				byte flag = fragment[0];
				short imageNumber = (short)(fragment[1]);
				int fragments = (int)(fragment[2] & 0xff);
                	short currentFragment = (short)(fragment[3] & 0xff);
                	int size = (int)((fragment[4] & 0xff) << 8 | (fragment[5] & 0xff));
                	byte[] timestampByteArray = new byte[] {
            			(byte) (fragment[6] & 0xff),
            			(byte) (fragment[7] & 0xff),
            			(byte) (fragment[8] & 0xff),
            			(byte) (fragment[9] & 0xff),
            			(byte) (fragment[10] & 0xff),
            			(byte) (fragment[11] & 0xff),
            			(byte) (fragment[12] & 0xff),
            			(byte) (fragment[13] & 0xff),
                	};
                	
                	// Converte o timestamp de milisegundos para Date
                	Date timestamp = new Date(ByteBuffer.wrap(timestampByteArray).getLong());
                	
                	// Checa se é um início de imagem
				if(((flag & IMAGE_START) == IMAGE_START) && (imageNumber != currentImage)){
					// Define o numero da imagem atual
					currentImage = imageNumber;
					
					// Zera o contador de fragmentos recebidos
					fragmentsReceived = 0;
					
					// Inicializa buffer da imagem total calculando o numero de fragmentos x tamanho maximo do pacote
					imageData = new byte[fragments * SERVER_MAX_SIZE];
					
					// Começa a anexar fragmentos de imagem
					canReceive = true;
				}
				
				// Checa se fragmento pertece a imagem atual e está anexando imagens
				if(canReceive && imageNumber == currentImage) {
					// Anexa fragmentos a imagem total 
					System.arraycopy(fragment, HEADER_SIZE, imageData, currentFragment*SERVER_MAX_SIZE, size);
					
					// Incrementa contador de imagens recebidas
					fragmentsReceived++;
				}
				
				// Checa se todas as imagens já foram recebidas
				if(fragmentsReceived == fragments){
					ByteArrayInputStream bis= new ByteArrayInputStream(imageData);
					BufferedImage image = ImageIO.read(bis);
					System.out.println("Finalizou imagem");
					
					// Exibe a imagem na tela
					display.updateImage(image, timestamp);
					
					// Para de receber fragmentos
					canReceive = false;
				}
			}
		} catch (IllegalArgumentException | IOException e) {
			
			e.printStackTrace();
			System.out.println("Erro: " + e.getMessage());
		}
	}

}
