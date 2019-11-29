import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 * Client class
 *
 * An instance accepts input from the user, marshalls this into a datagram, sends
 * it to a server instance and then waits for a reply. When a packet has been
 * received, the type of the packet is checked and if it is an acknowledgement,
 * a message is being printed and the waiting main method is being notified.
 *
 */
public class Endpoint extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 55000; // Port of the server
	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header
	static final int LENGTH_POS = 1;

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_MESSAGE = 1;

	Terminal terminal;
	InetSocketAddress dstAddress;
	int port;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Endpoint(Terminal terminal, int srcPort, int dstPort) {
		try {
			this.terminal= terminal;
			port = srcPort;
			socket= new DatagramSocket(srcPort);
			dstAddress = new InetSocketAddress("",dstPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}


	/**
	 * Handles packets
	 */

	public synchronized void onReceipt(DatagramPacket packet) {
		try {
			
			String content;
			byte[] data;
			byte[] buffer;
			data = packet.getData();
			buffer= new byte[data[LENGTH_POS]];
			System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
			content= new String(buffer);
			
			String s = content.substring(content.indexOf(";")+1);
			switch(data[TYPE_POS]) {
			case TYPE_MESSAGE:
				terminal.println(content);
				terminal.print(s);
				break;
				//todo
			default:
				terminal.println("Unexpected packet" + packet.toString());
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * Sender Method
	 *
	 */
	public synchronized void sendMessage() throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		String input = null;
		input = terminal.read("Enter Message:")+";E"+(port-50000);
		buffer = input.getBytes();
		data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = TYPE_MESSAGE;
		data[LENGTH_POS] = (byte)buffer.length;
		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);

		//if input not valid
		if(!input.contains(":") || !input.substring(0,input.indexOf(":")).matches("E[0-9]+")) {
			terminal.println("Wrong format");
			//sendMessage();
		}

		//elif input valid
		else {
			packet= new DatagramPacket(data, data.length);
			InetSocketAddress temp = dstAddress;
			packet.setSocketAddress(temp);
			socket.send(packet);
			terminal.println("Work sent to "+dstAddress);
		}
	}	

}