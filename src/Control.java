import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.HashMap;

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
	static final int DEFAULT_DST_PORT = 50000;
	static final int DEFAULT_ROUTER_PORT = 55000; // Port of the router
	static final int DEFAULT_SRC_PORT = 40000;    // Port of the control
	static final String DEFAULT_DST_NODE = "localhost";	// Name of the host for the server

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0;      // Position of the type within the header
	static final int LENGTH_POS = 1;    // Position of the length of payload
	static final int ACKCODE_POS = 1;   // Position of the acknowledgement type in the header

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_MESSAGE = 1;
	static final byte TYPE_REQUEST = 2;
	static final byte TYPE_MODIFY  = 3;
	static final byte TYPE_HELLO = 4 ;

	//Creating routing table
	HashMap<InetSocketAddress,HashMap<String,InetSocketAddress>>hashOfRouters = 
			new HashMap<InetSocketAddress,HashMap<String,InetSocketAddress>>();


	Terminal terminal;

	/**
	 * Constructor
	 *
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Control(Terminal terminal, int srcPort) {
		try {
			this.terminal= terminal;
			socket= new DatagramSocket(srcPort);
			listener.go();

			//Routing table for R0
			HashMap<String, InetSocketAddress> R0 = new HashMap<String,InetSocketAddress>();
			R0.put("E1",new InetSocketAddress("",DEFAULT_ROUTER_PORT+1));
			R0.put("E2",new InetSocketAddress("",DEFAULT_ROUTER_PORT+2));
			R0.put("E3",new InetSocketAddress("",DEFAULT_ROUTER_PORT+3));

			//Hard coding routing table for each routers
			//Routing table for R1
			HashMap<String, InetSocketAddress> R1 = new HashMap<String,InetSocketAddress>();
			R1.put("E1",new InetSocketAddress("",DEFAULT_DST_PORT+1));
			R1.put("E2",new InetSocketAddress("",DEFAULT_ROUTER_PORT));
			R1.put("E3",new InetSocketAddress("",DEFAULT_ROUTER_PORT));

			//Routing table for R2
			HashMap<String, InetSocketAddress> R2 = new HashMap<String,InetSocketAddress>();
			R2.put("E1",new InetSocketAddress("",DEFAULT_ROUTER_PORT));
			R2.put("E2",new InetSocketAddress("",DEFAULT_DST_PORT+2));
			R2.put("E3",new InetSocketAddress("",DEFAULT_ROUTER_PORT));

			//Routing table for R3
			HashMap<String, InetSocketAddress> R3 = new HashMap<String,InetSocketAddress>();
			R3.put("E1",new InetSocketAddress("",DEFAULT_ROUTER_PORT));
			R3.put("E2",new InetSocketAddress("",DEFAULT_ROUTER_PORT));
			R3.put("E3",new InetSocketAddress("",DEFAULT_DST_PORT+3));

			hashOfRouters.put(new InetSocketAddress("",DEFAULT_ROUTER_PORT+0), R0);
			hashOfRouters.put(new InetSocketAddress("",DEFAULT_ROUTER_PORT+1), R1);
			hashOfRouters.put(new InetSocketAddress("",DEFAULT_ROUTER_PORT+2), R2);
			hashOfRouters.put(new InetSocketAddress("",DEFAULT_ROUTER_PORT+3), R3);

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
			switch(data[TYPE_POS]) {
			case TYPE_REQUEST:
				sendModify(new InetSocketAddress("",packet.getPort()),content);
				break;
			case TYPE_HELLO:
				System.out.println(packet.getSocketAddress()+" said hello.");
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
	public synchronized void sendModify(InetSocketAddress dstAddress, String dst) throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		HashMap<String,InetSocketAddress> temp = hashOfRouters.get(dstAddress);
		if(temp!=null)
		buffer = (dst+" "+temp.get(dst)).getBytes();
		if(buffer!=null) {
		data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = TYPE_MODIFY;
		data[LENGTH_POS] = (byte)buffer.length;
		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);
		//terminal.println("Sending modification for: "+temp.get(dst)+" to "+dstAddress);
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(dstAddress);
		socket.send(packet);	
		}
	}
	
	
	public synchronized void start() {
	}

}