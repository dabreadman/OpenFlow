
public class API {

	static final int DEFAULT_PORT = 50001;
	static final int CC_PORT = 60000;
	static final int DEFAULT_SRC_PORT = 50002; // Port of the client
	static final int DEFAULT_DST_PORT = 50001; // Port of the server

	public static void main(String[] args) throws Exception {
		CAndC CAndC = new CAndC(new Terminal("Command and Control"), DEFAULT_DST_PORT, CC_PORT);
		Runnable CC = () -> {
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
		new Thread(CC).start();

		Broker broker = new Broker(new Terminal("Broker"), DEFAULT_PORT);
		Runnable brk = () -> {
			try {
				broker.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		new Thread(brk).start();

		Terminal terminal= new Terminal("Client Starter");
		int i =0;
		while(true){
			String input= terminal.read("Name: ");
			terminal.println("Successfully started worker "+input+" on port "+(DEFAULT_SRC_PORT+i));
			Worker worker = new Worker(new Terminal(input), DEFAULT_DST_PORT, DEFAULT_SRC_PORT+i++,input);
			Runnable wkr = () -> {
				try {
						worker.volunteer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			new Thread(wkr).start();
		}
	}

}
