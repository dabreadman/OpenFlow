import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class API {

	//	static final int DEFAULT_PORT = 50001;
	static final int CC_PORT = 40000;
	static final int DEFAULT_ROUTER_PORT = 55000; // Port of the client
	static final int DEFAULT_DST_PORT = 50000; // Port of the server

	public static void main(String[] args) throws Exception {

		Thread thread1 = new Thread(){
			public void run(){
				Endpoint ep = new Endpoint(new Terminal("Endpoint 1"), 50001,55001);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread1.start();

		Thread thread2 = new Thread(){
			public void run(){
				Endpoint ep = new Endpoint(new Terminal("Endpoint 2"),  50002,55002);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		thread2.start();

		Thread thread3 = new Thread(){
			public void run(){
				Endpoint ep= new Endpoint(new Terminal("Endpoint 3"),  50003,55003);
				try {
					while(true) {
						ep.sendMessage();
						this.sleep(40);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
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

		Router router = new Router(new Terminal("Router 0"),55000);
		Runnable rtr = () -> {
			try {
				router.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr).start();

		Router router1 = new Router(new Terminal("Router 1"), 55001);
		Runnable rtr1 = () -> {
			try {
				router1.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr1).start();


		Router router2 = new Router(new Terminal("Router 2"), 55002);
		Runnable rtr2 = () -> {
			try {
				router2.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr2).start();


		Router router3 = new Router(new Terminal("Router 3"), 55003);
		Runnable rtr3 = () -> {
			try {
				router3.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(rtr3).start();

		//Router router4 = new Router(new Terminal("Bug Splat"),55004);



	}

}
