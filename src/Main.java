import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	
	static final int CC_PORT = 40000;
	static final int DEFAULT_ROUTER_PORT = 55000; // Port of the router
	static final int DEFAULT_DST_PORT = 50000;    // Port of the endpoint

	public static void main(String[] args) throws Exception {

		Thread thread1 = new Thread(){
			public void run(){
				Endpoint ep = new Endpoint(new Terminal("Endpoint 1"), DEFAULT_DST_PORT+1,DEFAULT_ROUTER_PORT+1);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {				
					e.printStackTrace();
				}
			}
		};
		thread1.start();

		Thread thread2 = new Thread(){
			public void run(){
				Endpoint ep = new Endpoint(new Terminal("Endpoint 2"),  DEFAULT_DST_PORT+2,DEFAULT_ROUTER_PORT+2);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {		
					e.printStackTrace();
				}
			}
		};
		thread2.start();

		Thread thread3 = new Thread(){
			public void run(){
				Endpoint ep= new Endpoint(new Terminal("Endpoint 3"),  DEFAULT_DST_PORT+3,DEFAULT_ROUTER_PORT+3);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread3.start();

		Control control = new Control(new Terminal("Control"), CC_PORT);
		Runnable ctl = () -> {
			try {
				control.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(ctl).start();

		Router router = new Router(new Terminal("Router 0"), DEFAULT_ROUTER_PORT);
		Runnable rtr = () -> {
			try {
				router.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr).start();

		Router router1 = new Router(new Terminal("Router 1"), DEFAULT_ROUTER_PORT+1);
		Runnable rtr1 = () -> {
			try {
				router1.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr1).start();


		Router router2 = new Router(new Terminal("Router 2"), DEFAULT_ROUTER_PORT+2);
		Runnable rtr2 = () -> {
			try {
				router2.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr2).start();


		Router router3 = new Router(new Terminal("Router 3"), DEFAULT_ROUTER_PORT+3);
		Runnable rtr3 = () -> {
			try {
				router3.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr3).start();

	}

}
