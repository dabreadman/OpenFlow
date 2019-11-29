import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

public class Router extends Node {
	static final int DEFAULT_SRC_PORT = 55000; // Port of the router
	static final int CC_PORT = 40000;          // Port of the control

	static final int HEADER_LENGTH = 2; // Fixed length of the header
	static final int TYPE_POS = 0;      // Position of the type within the header
	static final int LENGTH_POS = 1;    // Position of the length of payload
	static final int ACKCODE_POS = 1;   // Position of the acknowledgement type in the header

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_MESSAGE = 1;
	static final byte TYPE_REQUEST = 2;
	static final byte TYPE_MODIFY  = 3;
	static final byte TYPE_HELLO = 4 ;

	static String name;
	static int port;
	static InetSocketAddress address1;
	Terminal terminal;
	HashMap<String,InetSocketAddress> map = new HashMap<String,InetSocketAddress>();

	Router(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			this.port = port;
			socket= new DatagramSocket(port);
			address1 = new InetSocketAddress("",port);
			listener.go();
			sayHello();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	public synchronized void sayHello() throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_HELLO;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(new InetSocketAddress("",CC_PORT));
		socket.send(packet);
		terminal.println("Said hello to control");
	}

	/**
	 * Handles packets
	 */
	public void onReceipt(DatagramPacket packet) {
		try {
			String content = null;
			byte[] data;
			byte[] buffer;
			data = packet.getData();
			buffer= new byte[data[LENGTH_POS]];
			System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
			content= new String(buffer);
			String s = content.substring(content.indexOf(";")+1);
			int temptemptemp = port;	
			InetSocketAddress temptempmtepte = address1; 
			switch(data[TYPE_POS]) {
			case TYPE_MESSAGE:
				String address = content.substring(0,content.indexOf(":"));
				InetSocketAddress dstAddress = map.get(address);
				InetSocketAddress temptemp = address1;
				//if the route is not set
				if(dstAddress==null) {
					requestTable(address);
					packet.setSocketAddress(temptemp);
					socket.send(packet);
				}
				else {
					dstAddress = map.get(address);
					HashMap<String,InetSocketAddress> temp = map;
				//	System.out.println("Sending packet from "+s+" to "+dstAddress);
					packet.setSocketAddress(dstAddress);
					socket.send(packet);
				}
				break;
				
			case TYPE_MODIFY:
				String newAddress = content.substring(0,content.indexOf(" "));
				InetSocketAddress Iaddr =  new InetSocketAddress("",Integer.parseInt(content.substring(content.indexOf(":")+1)));
				map.put(newAddress, Iaddr);
				terminal.println("Placed "+newAddress+" and "+Iaddr);
				break;
			default:
				terminal.println("Unexpected packet" + packet.toString());
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * Send a packet to indicate no worker(s) is available
	 */
	public synchronized void requestTable(String dst) throws Exception {
		//sends to control
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		buffer = dst.getBytes();
		data = new byte[HEADER_LENGTH+buffer.length];
		data[TYPE_POS] = TYPE_REQUEST;
		data[LENGTH_POS] = (byte)buffer.length;
		System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);

		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(new InetSocketAddress("",CC_PORT));
		socket.send(packet);
		terminal.println("Request sent for route to "+dst);
		TimeUnit.SECONDS.sleep(1);
	}

	public synchronized void start() throws Exception {
		this.wait();
	}

}