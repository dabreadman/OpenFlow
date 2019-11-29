import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

public class Router extends Node {
	static final int DEFAULT_PORT = 50001;
	static final int CC_PORT = 60000;

	static final int HEADER_LENGTH = 2;
	static final int TYPE_POS = 0;
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1;

	static final byte TYPE_UNKNOWN = 0;

	static String name;
	static int port;

	Terminal terminal;
	ArrayList<InetSocketAddress> address = new ArrayList<InetSocketAddress>();

	Router(Terminal terminal, int port) {
		try {
			this.terminal= terminal;
			this.port = port;
			socket= new DatagramSocket(port);
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
			//TODO
			case TYPE_UNKNOWN:
				terminal.println("Receiving job from C&C");
				if(address.size()>0) {
					buffer= new byte[data[LENGTH_POS]];
					System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
					content= new String(buffer);


					}
					//remove addresses
	
			default:
				terminal.println("Unexpected packet" + packet.toString());
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}

	/**
	 * Send a packet to indicate no worker(s) is available
	 */
	public synchronized void replyNoWorker() throws Exception {
		//sends to control
		//todo
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = 0;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(new InetSocketAddress("localhost",CC_PORT));
		socket.send(packet);
	}
	

	public synchronized void start() throws Exception {
		terminal.println("I am listening to port "+port);
		this.wait();
	}

	/**
	 * Return an array list containing even distribution of ntimes to npeople
	 */
	
	public static void main(String[] args) {
		try {			
			Router router = new Router(new Terminal("Router"), DEFAULT_PORT);
			router.start();	
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}