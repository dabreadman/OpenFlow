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
public class Worker extends Node {
	static final int DEFAULT_SRC_PORT = 50002; // Port of the client
	static final int DEFAULT_DST_PORT = 50001; // Port of the server
	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0; // Position of the type within the header
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1; // Position of the acknowledgement type in the header

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_STRING = 1; // Indicating a string payload
	static final byte TYPE_ACK = 2;   // Indicating an acknowledgement
	static final byte TYPE_VOLUNTEER = 3;
	static final byte TYPE_WORK = 4;
	static final byte TYPE_REPLY = 5;
	static final byte TYPE_INVALID = 6;

	static final int PRINT = 0;
	static final int REVERSE = 1;



	static final byte ACK_ALLOK = 10; // Indicating that everything is OK

	Terminal terminal;
	InetSocketAddress dstAddress;
	String name;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Worker(Terminal terminal, int dstPort, int srcPort,String name) {
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
			case TYPE_ACK:
				notify();
				break;
			case TYPE_WORK:
				buffer= new byte[data[LENGTH_POS]];
				System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
				content= new String(buffer);
				int work = Integer.parseInt(content.substring(0, content.indexOf("_")));
				
				String description = content.substring(content.indexOf("_")+1,content.indexOf(":"));
				int ntimes = Integer.parseInt(content.substring(content.indexOf(":")+1));
				terminal.println("Received work: "+work +"\nDescription: "+description+"\nTimes: "+ntimes);

				switch(work) {
				case PRINT:
					for(int i=0; i<ntimes;i++)
						terminal.println((i+1)+". Printing: "+description);
					replyFinished("printing "+description+" "+ ntimes +" times.");
					//send reply
					break;
				case REVERSE:
					char[] temp = description.toCharArray();
					char[] s = new char[description.length()];
					for(int i=0;i<description.length();i++) {
						s[i]=temp[description.length()-1-i];
					}
					replyFinished("reversing with the result: "+String.valueOf(s));
					//send answer
					break;
				default:
					invalidJob();
				}

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
		input= terminal.read("Enter anything to volunteer: ");
		
			data = new byte[HEADER_LENGTH];
			data[TYPE_POS] = TYPE_VOLUNTEER;
			data[LENGTH_POS] = 0;
			terminal.println("Sending packet...");
			packet= new DatagramPacket(data, data.length);
			packet.setSocketAddress(dstAddress);
			socket.send(packet);
			terminal.println("Volunteered");		
	}

	/**
	 * Sends a packet to volunteer for work
	 * @throws Exception
	 */
	public synchronized void volunteer() throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_VOLUNTEER;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
		terminal.println("Volunteered");
		wait();
	}
	
	/**
	 * Sends a packet to indicate invalid job
	 * @throws Exception
	 */
	public synchronized void invalidJob() throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_INVALID;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		socket.send(packet);
		sendMessage();
	}
	
	/**
	 * Sends a packet containing s as the data indicating work is finished
	 */
	public synchronized void replyFinished(String s) throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		
		String reply=name+" done "+s;
		buffer=reply.getBytes();
		data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = TYPE_REPLY;
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
	public static void main(String[] args) {
		try {
			Terminal terminal= new Terminal("Client Starter");
			int i =0;
			while(true){
				String input= terminal.read("Name: ");
				terminal.println("Successfully started worker "+input+" on port "+(DEFAULT_SRC_PORT+i));
				Worker worker = new Worker(new Terminal(input), DEFAULT_DST_PORT, DEFAULT_SRC_PORT+i++,input);
				Runnable sender = () -> {
					try {
						worker.volunteer();
					} catch (Exception e) {
						e.printStackTrace();
					}
				};
				new Thread(sender).start();
			}
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}