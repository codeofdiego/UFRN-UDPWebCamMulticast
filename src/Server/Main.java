package Server;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

public class Main {
	// Constantes
	final static int PORTA = 5555;
	final static String HOST = "239.1.1.66";
	final static int HEADER_SIZE = 14;
	final static int IMAGE_START = 128;
	final static int DATAGRAM_MAX_SIZE = 65407 - HEADER_SIZE;
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// Variaveis
		int imageNumber = 0;
		
		// Configura rede e inicializa grupo e socket
		System.setProperty("java.net.preferIPv4Stack", "true");
		InetAddress group = null;
		MulticastSocket socketServidor = null;
		
		// Define e se junta ao grupo
		try {
			group = InetAddress.getByName(HOST);
			socketServidor = new MulticastSocket();
			socketServidor.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		// Debug
		System.out.println("Serviço de MULTICAST UDP rodando...");
		
		// Inicia o loop de captura e envio das fotos para o grupo de multicast
		while(true) {
			try {
				// Calcula hora em Milis e converte para um byte[] de 8 bytes
				long timestamp = System.currentTimeMillis();    
				byte[] timestampByteArray = ByteBuffer.allocate(8).putLong(timestamp).array();
				
				// Captura imagem da camera
				BufferedImage image = CamCapturer.takePhoto();
				
				// Converte a imagem para byteArray
				ByteArrayOutputStream imageByteArrayOS = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg", imageByteArrayOS);
				
				// Converte de ByteArrayOutputStream para Byte[], mais fácil de manipular e enviar
				byte[] imageByteArray = imageByteArrayOS.toByteArray();
				
				// Calcula a quantidade de pacotes necessários para enviar a imagem
				int fragments = (int) Math.ceil(imageByteArray.length / (float)DATAGRAM_MAX_SIZE);

				System.out.println("fragments: " + fragments);
				// Loop para enviar os pacotes separadamente
				for(int i=0; i < fragments; i++){
					// Calcula a flag de posição do pacote na imagem checando se está no inicio ou no fim
					int flags = 0;
					flags = i == 0 ? flags | IMAGE_START : flags;
					
					// Calcula o tamanho do pacote
					int size = (i + 1) < fragments ? DATAGRAM_MAX_SIZE : imageByteArray.length - i * DATAGRAM_MAX_SIZE;

					// Cria o buffer de dados a serem enviados com o tamanho apropriado
					byte[] fragment = new byte[HEADER_SIZE + size];
					
					// Seta dados
					fragment[0] = (byte)flags;						// Marca se o pacote é de inicio ou fim
					fragment[1] = (byte)imageNumber;					// Controle para fragmentos da mesma imagem
					fragment[2] = (byte)fragments;					// Quantidade de pacotes
					fragment[3] = (byte)i;							// Fragmento atual
					fragment[4] = (byte)(size >> 8);					// 2 Bytes são necessários para representar o tamanho atual 
					fragment[5] = (byte)size;
					fragment[6] = (byte)timestampByteArray[0];		// 8 Bytes são necessários para representar o timestamp 
					fragment[7] = (byte)timestampByteArray[1];
					fragment[8] = (byte)timestampByteArray[2];
					fragment[9] = (byte)timestampByteArray[3];
					fragment[10] = (byte)timestampByteArray[4];
					fragment[11] = (byte)timestampByteArray[5];
					fragment[12] = (byte)timestampByteArray[6];
					fragment[13] = (byte)timestampByteArray[7];
					
					// Copia a imagem seção da imagem junto com o header
					System.arraycopy(imageByteArray, i * DATAGRAM_MAX_SIZE, fragment, HEADER_SIZE, size);
					
					// Cria pacote para ser enviado
					DatagramPacket pacote = new DatagramPacket(fragment, fragment.length, group, PORTA);
					
					// Envia dados
					socketServidor.send(pacote);
				}
				
				// Incrementa o id da imagem sendo transmitida
				imageNumber++;
				
				// Espera 1 segundo para enviar o proximo frame
				TimeUnit.SECONDS.sleep(1);
			} catch (IndexOutOfBoundsException | ArrayStoreException | NullPointerException
					| IllegalArgumentException | IOException | InterruptedException e) {
				
				e.printStackTrace();
				System.out.println("Erro: " + e.getMessage());
			}
		}

	}

}
