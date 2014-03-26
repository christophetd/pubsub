package test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

import lsr.concurrence.provided.tests.ClientInputReader;
import lsr.concurrence.provided.tests.ClientOutputWriter;


/**
 * 
 * A test task
 *
 */
public abstract class Task implements Runnable{

	protected Socket connection;
	protected ClientOutputWriter output;
	protected ClientInputReader input;
	
	protected CyclicBarrier barrier;
	protected ArrayList<String> topics;
	protected ArrayList<String> msgs;
	
	/**
	 * 
	 * @param host the Hostname if the machine to connect to
	 * @param port the port to connect to
	 * @param topics a list of topics that can be used during the test
	 * @param msgs a list of messages that can be used during the test
	 * @param barrier the barrier used to synchronize tasks
	 */
	public Task(String host, int port, ArrayList<String> topics, ArrayList<String> msgs, CyclicBarrier barrier){
		this.barrier=barrier;
		this.topics=topics;
		this.msgs=msgs;
		try {
			this.connection = new Socket("localhost", 7676);
			
			this.output = new ClientOutputWriter(connection.getOutputStream());
			this.input= new ClientInputReader(connection.getInputStream());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * close the socket
	 */
	protected void closeConnection(){
		try {
			this.connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
