package pubsub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPAcceptor implements Runnable {

	public final static int LISTENING_PORT = 7676;
	public final static int NB_COMMAND_HANDLERS = 5;
	public final static int POOL_SIZE = 5;
	private ServerSocket server;
	private MyBlockingQueue<Command> commandBuffer = new MyBlockingQueue<Command>();
	//private MessageBroker broker = new MessageBroker(commandBuffer);
	private SubscriptionsStore subscriptions = new SubscriptionsStore();
	
	private ExecutorService threadPool;

	public TCPAcceptor() {
		try {	
			server = new ServerSocket(LISTENING_PORT);
			threadPool = Executors.newFixedThreadPool(POOL_SIZE);
		} 
		catch (IOException e) {
			System.err.println("Could not run socket server on port "
					+ LISTENING_PORT);
			e.printStackTrace(System.err);

			if (server != null && !server.isClosed()) {
				try {
					server.close();
				} catch (IOException err) {
					err.printStackTrace(System.err);
				}
			}
		}
	}

	@Override
	public void run() {

		try {
			// Start command handlers
			for(int i = 0; i < NB_COMMAND_HANDLERS; ++i) {
				CommandHandler handler = new CommandHandler(commandBuffer, subscriptions);
				new Thread(handler).start();
			}
			
			// Accept socket loop
			while (true) {
				Socket socket = server.accept();
				System.out.println("Client with IP "+socket.getRemoteSocketAddress()+" connected");

				TCPReader reader = new TCPReader(socket, commandBuffer);
				threadPool.execute(reader);
			}
		} catch (IOException e) {
			System.err.println("Error while connecting to client");
			e.printStackTrace(System.err);
			threadPool.shutdown();
		}
	}

}
