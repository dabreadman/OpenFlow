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
	static final int DEFAULT_SRC_PORT = 55000;
	static final int CC_PORT = 40000;

	static final int HEADER_LENGTH = 2;
	static final int TYPE_POS = 0;
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1;

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_MESSAGE = 1;
	static final byte TYPE_REQUEST = 2;
	static final byte TYPE_MODIFY  = 3;

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
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}
	public abstract class TimerTask extends Object implements Runnable{
		public void run() {
			notifyAll();
		}
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
					TimeUnit.SECONDS.sleep(1);
				}
				else {
					dstAddress = map.get(address);
					HashMap<String,InetSocketAddress> temp = map;
					System.out.println("Sending packet from "+s+" to "+dstAddress);
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
				//todo
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
		
	}


	public synchronized void start() throws Exception {
		terminal.println("I am listening to port "+port);
		this.wait();
	}

	public void dummyNotify() {
		notifyAll();
	}

	public static void main(String[] args) {
		//		try {			
		Router router = new Router(new Terminal("Router"), 55000);
		try {
			router.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//			router.start();	
		//		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}