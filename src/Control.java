import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

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
public class Control extends Node {
	static final int DEFAULT_SRC_PORT = 50002; // Port of the client
	static final int DEFAULT_DST_PORT = 50001; // Port of the server
	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_STRING = 1; // Indicating a string payload

	Terminal terminal;
	InetSocketAddress dstAddress;
	String name;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Control(Terminal terminal, int dstPort, int srcPort,String name) {
		try {
			this.terminal= terminal;
			this.name=name;
			dstAddress= new InetSocketAddress("", dstPort);
			socket= new DatagramSocket(srcPort);
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
			switch(data[TYPE_POS]) {
			case TYPE_STRING:
				buffer= new byte[data[LENGTH_POS]];
				System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
				content= new String(buffer);
				terminal.println(content);
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
		String input;
		input= terminal.read("Enter anything to volunteer: ");
		//todo
			data = new byte[HEADER_LENGTH];
			data[TYPE_POS] = 0;
			data[LENGTH_POS] = 0;
			terminal.println("Sending packet...");
			packet= new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
			terminal.println("Volunteered");		
	}

	/**
	 * Sends a packet containing s as the data indicating work is finished
	 */
	public synchronized void replyFinished(String s) throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		//todo
		String reply=name+" done "+s;
		buffer=reply.getBytes();
		data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = 0;
		data[LENGTH_POS] = (byte) buffer.length;
		
		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);
		terminal.println("Sending packet...");
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
		sendMessage();
	}


	/**
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
//	public static void main(String[] args) {
//		try {
//			Terminal terminal= new Terminal("Client Starter");
//			int i =0;
//			while(true){
//				String input= terminal.read("Name: ");
//				terminal.println("Successfully started worker "+input+" on port "+(DEFAULT_SRC_PORT+i));
//				Control control = new Control(new Terminal(input), DEFAULT_DST_PORT, DEFAULT_SRC_PORT+i++,input);
//				Runnable sender = () -> {
//					try {
//						control.volunteer();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				};
//				new Thread(sender).start();
//			}
//		} catch(java.lang.Exception e) {e.printStackTrace();}
//	}
}