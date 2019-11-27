import java.net.DatagramSocket;
import java.net.InetAddress;
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
public class CAndC extends Node {
	static final int CC_PORT = 60000;
	static final int DEFAULT_DST_PORT = 50001; // Port of the server
	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_STRING = 1;
	static final byte TYPE_ACK = 2;
	static final byte TYPE_VOLUNTEER = 3;
	static final byte TYPE_WORK = 4;
	static final byte TYPE_REPLY = 5;
	static final byte TYPE_INVALID = 6;

	static final byte ACK_ALLOK = 10; // Indicating that everything is ok

	Terminal terminal;
	InetSocketAddress dstAddress;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	CAndC(Terminal terminal, int dstPort, int srcPort) {
		try {
			this.terminal= terminal;
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
			case TYPE_REPLY:
				if(data[LENGTH_POS]>0) {
					buffer= new byte[data[LENGTH_POS]];
					System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
					content= new String(buffer);
					terminal.println(content);
				}
				else
				{
					terminal.println("No workers are available, please try again.");
				}
				break;
			case TYPE_INVALID:
				terminal.println("Invalid job description, please try again.");
				notify();
				break;
			case TYPE_ACK:
				terminal.println("ACK received");
				notify();
				break;
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
		terminal.println("<Work>_<Description>:<Times>");
		input= terminal.read("Enter work");
		if(!input.contains("_")||!input.contains(":")) {
			terminal.println("Wrong format");
			sendMessage();
		}
		else if(input.indexOf("_")>input.indexOf(":")){
			terminal.println("Wrong format");
			sendMessage();
		}
		else {
			buffer = input.getBytes();
			data = new byte[HEADER_LENGTH+buffer.length];
			data[TYPE_POS] = TYPE_WORK;
			data[LENGTH_POS] = (byte)buffer.length;
			System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);

			packet= new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
			terminal.println("Work sent");
		}
		wait();
	}	

	/**
	 * Test method
	 *
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {
			CAndC CAndC = new CAndC(new Terminal("Command and Control"), DEFAULT_DST_PORT, CC_PORT);
			Runnable sender = () -> {
				try {
					long end=System.currentTimeMillis()+60*10;
					while(true) {
						if(System.currentTimeMillis()>end) {
							CAndC.sendMessage();
							end=System.currentTimeMillis()+60*10;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			new Thread(sender).start();

		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}