package pubsub;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPAcceptor implements Runnable {

	public final static int LISTENING_PORT = 7676;
	private ServerSocket server;
	private MessageBroker broker = new MessageBroker();
	private MyBlockingQueue<Command> commandBuffer = new MyBlockingQueue<Command>(); 

	public TCPAcceptor() {
		try {
			server = new ServerSocket(LISTENING_PORT);
		} catch (IOException e) {
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
			while (true) {
				Socket socket = server.accept();
				try {
					TCPReader reader = new TCPReader(socket, commandBuffer);
					Thread t = new Thread(reader);
					t.start();
				} catch (IOException e) {
					System.err.println("Error while //TODO");
					e.printStackTrace(System.err);
				}
			}
		} catch (IOException e) {
			System.err.println("Error while connecting to client");
			e.printStackTrace(System.err);
		}
	}

}
