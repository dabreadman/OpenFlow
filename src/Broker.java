import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

public class Broker extends Node {
	static final int DEFAULT_PORT = 50001;
	static final int CC_PORT = 60000;

	static final int HEADER_LENGTH = 2;
	static final int TYPE_POS = 0;
	static final int LENGTH_POS = 1;
	static final int ACKCODE_POS = 1;

	static final byte TYPE_UNKNOWN = 0;
	static final byte TYPE_STRING = 1;
	static final byte TYPE_ACK = 2;
	static final byte TYPE_VOLUNTEER = 3;
	static final byte TYPE_WORK = 4;
	static final byte TYPE_REPLY = 5;
	static final byte TYPE_INVALID = 6;

	static final byte ACK_ALLOK = 10;

	static String name;
	static int port;

	Terminal terminal;
	ArrayList<InetSocketAddress> address = new ArrayList<InetSocketAddress>();

	Broker(Terminal terminal, int port) {
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
			returnACK(packet.getSocketAddress());
			switch(data[TYPE_POS]) {
			case TYPE_STRING:
				buffer= new byte[data[LENGTH_POS]];
				System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
				content= new String(buffer);
				if(content.length()>0){
					terminal.println("length: "+content.length()+" "+content);
				}
				break;
			case TYPE_VOLUNTEER:
				InetSocketAddress temp = (InetSocketAddress) packet.getSocketAddress();
				terminal.println(temp+ " volunteered");
				if(!address.contains(temp)) {
					address.add(temp);
				}
				break;
			case TYPE_WORK:
				terminal.println("Receiving job from C&C");
				if(address.size()>0) {
					buffer= new byte[data[LENGTH_POS]];
					System.arraycopy(data, HEADER_LENGTH, buffer, 0, buffer.length);
					content= new String(buffer);

					int ntimes = Integer.parseInt(content.substring(content.indexOf(":")+1));
					int [] tempArr = distribute(ntimes,address.size());

					for(int i=0;i<address.size();i++) {
						if(tempArr[i]>0) {
							String s = content.substring(0, content.indexOf(":")+1)+tempArr[i];
							buffer = s.getBytes();
							data = new byte[HEADER_LENGTH+buffer.length];
							data[TYPE_POS] = TYPE_WORK;
							data[LENGTH_POS] = (byte)buffer.length;
							System.arraycopy(buffer, 0, data, HEADER_LENGTH, buffer.length);
							packet= new DatagramPacket(data, data.length);
							packet.setSocketAddress(address.get(i));
							terminal.println("Sending job to "+address.get(i));
							socket.send(packet);
						}
					}
					//remove addresses
					for(int i=tempArr.length-1;i>=0;i--) {
						if(tempArr[i]>0)
							address.remove(i);
					}
				}
				else {
					terminal.println("Replying to C&C no workers are available.");
					replyNoWorker();
				}
				break;
			case TYPE_REPLY:
			case TYPE_INVALID:
				InetSocketAddress a=new InetSocketAddress("localhost",CC_PORT);
				terminal.println("Forwarding reply from "+ packet.getSocketAddress()+" to C&C.");
				packet.setSocketAddress(a);
				socket.send(packet);
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
	public synchronized void replyNoWorker() throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_REPLY;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(new InetSocketAddress("localhost",CC_PORT));
		socket.send(packet);
	}
	
	/**
	 * Send an ACK packet 
	 */
	public synchronized void returnACK(SocketAddress socketAddress) throws Exception {
		byte[] data= null;
		byte[] buffer= null;
		DatagramPacket packet= null;
		data = new byte[HEADER_LENGTH];
		data[TYPE_POS] = TYPE_ACK;
		data[LENGTH_POS] = 0;
		packet= new DatagramPacket(data, data.length);
		packet.setSocketAddress(socketAddress);
		terminal.println("Sending ACK to "+socketAddress);
		socket.send(packet);
	}

	public synchronized void start() throws Exception {
		terminal.println("I am listening to port "+port);
		this.wait();
	}

	/**
	 * Return an array list containing even distribution of ntimes to npeople
	 */
	public int[] distribute(int ntimes, int npeople) {
		int nleast = ntimes/npeople;
		int nleft = ntimes%npeople;
		int []result = new int[npeople];
		Arrays.fill(result, nleast);
		for(int i=0;i<nleft;i++) {
			result[i]=result[i]+1;
		}
		return result;
	}
	/*
	 * 
	 */
	public static void main(String[] args) {
		try {			
			Broker broker = new Broker(new Terminal("Broker"), DEFAULT_PORT);
			broker.start();	
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}