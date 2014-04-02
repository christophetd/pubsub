package pubsub;

import java.io.IOException;
import java.net.Socket;

import lsr.concurrence.provided.server.CommandID;
import lsr.concurrence.provided.server.InputFormatException;
import lsr.concurrence.provided.server.InputReader;

public class TCPReader implements Runnable {
	private final Socket socket;
	private final InputReader input;
	private boolean running = true;
	private Client client;
	private MyBlockingQueue<Command> buff;
	
	public TCPReader(Socket socket, MyBlockingQueue<Command> buff) throws IOException {
		this.socket = socket;
		this.input = new InputReader(socket.getInputStream());
		this.buff = buff;
		this.client = new Client(socket.getOutputStream());
		
		buff.pushBack(new Command(client, CommandID.NEWCLIENT, null, null));
	}

	@Override
	public void run() {
		// Handle incoming commands
		try {
			while (running) {
				try {
					readExecuteCommand();
				} 
				catch (InputFormatException e) {
					System.err.println("Invalid command");
				}
			}
		} 
		catch (IOException e) {
			System.err.println("Error while reading client input");
			e.printStackTrace(System.err);
		} 
		finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} 
				catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private void readExecuteCommand() throws IOException, InputFormatException {
		input.readCommand();
		CommandID command = input.getCommandId();
		
		switch (command) {
			case ENDOFCLIENT:
				running = false;
			
			default:
				Command cmd = new Command(client, command, input.getMessage(), input.getTopic());
				buff.pushBack(cmd);
				break;
			
		}
	}
}
