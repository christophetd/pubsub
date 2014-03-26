package test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

import lsr.concurrence.provided.tests.ClientInputReader;
import lsr.concurrence.provided.tests.ClientOutputWriter;
import lsr.concurrence.provided.tests.InputChecker;



public class TestPubSubProvided {

	static void printTestResult(int testID, int error){
		System.out.print("**** TEST "+testID+": ");
		if(error==0){
			 System.out.println("passed\n");
		}
		else{
			System.out.println("failed\n");
			System.exit(1);
		}

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host="localhost";
		int port=7676;
		
		
		//getting the command line arguments if any
		if(args.length ==2){
			host=args[0];
			port=Integer.parseInt(args[1]);
		}
		
		System.out.println("Test with server "+host+":"+port);
		
		try {

			ArrayList<String> topics=new ArrayList<String>();
			ArrayList<String> msgs=new ArrayList<String>();
			topics.add("epfl");
			topics.add("concurrence");
			msgs.add("bonjour");
			msgs.add("hello");
			int error=0;

			Socket connection = new Socket(host, port);
			
			ClientOutputWriter output = new ClientOutputWriter(connection.getOutputStream());
			InputChecker inputCheck= new InputChecker(new ClientInputReader(connection.getInputStream()));
			
			if(inputCheck.checkConnected()!=0){
				System.out.println("Error in connection");
				connection.close();
				return;
			}
			
			// A simple test
			// sequence: subscribe - publish - unsubscribe
			System.out.println("**** TEST 1 ");
			error=0;
			
			output.subscribeTo(topics.get(0));
			error += inputCheck.checkSubscribe(topics.get(0));
			output.publish(topics.get(0), msgs.get(0));
			error += inputCheck.checkPublish(topics.get(0), msgs.get(0));
			output.unsubscribeTo(topics.get(0));
			error +=inputCheck.checkUnsubscribe(topics.get(0));
			
			printTestResult(1,error);
			
			// Another test with 2 topics
			System.out.println("**** TEST 2 ");
			error=0;
			
			output.subscribeTo(topics.get(0));
			error += inputCheck.checkSubscribe(topics.get(0));
			output.subscribeTo(topics.get(1));
			error += inputCheck.checkSubscribe(topics.get(1));
			
			output.publish(topics.get(0), msgs.get(0));
			error += inputCheck.checkPublish(topics.get(0), msgs.get(0));
			output.publish(topics.get(1), msgs.get(1));
			error += inputCheck.checkPublish(topics.get(1), msgs.get(1));
			
			output.unsubscribeTo(topics.get(0));
			error +=inputCheck.checkUnsubscribe(topics.get(0));
			
			output.publish(topics.get(0), msgs.get(1)); // no check as we are not supposed to receive anything
			output.publish(topics.get(1), msgs.get(0));
			error += inputCheck.checkPublish(topics.get(1), msgs.get(0));
			
			output.unsubscribeTo(topics.get(1));
			error+=inputCheck.checkUnsubscribe(topics.get(1));
			
			printTestResult(2,error);
			connection.close();
			
			// Test with multiple clients
			// Same scenario as before
			System.out.println("**** TEST 3 ");
			CyclicBarrier barrier= new CyclicBarrier(2);

			Thread tr0=new Thread(new Task3_0(host,port,topics,msgs,barrier));
			tr0.start();
			Thread tr1=new Thread(new Task3_1(host,port,topics,msgs,barrier));
			tr1.start();
			
			try {
				tr0.join();
				tr1.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Another test with multiple clients
			// With multiple clients, order of messages published on the same topic is not known in advance.
			// Thus, in this test, we use an InputSequenceChecker
			System.out.println("**** TEST 4 ");
			CyclicBarrier barrier3= new CyclicBarrier(3);

			Thread tr3=new Thread(new Task4_0(host,port,topics,msgs,barrier3));
			tr3.start();
			
			Thread tr4=new Thread(new Task4_1(host,port,topics,msgs,barrier3));
			tr4.start();
			
			Thread tr5=new Thread(new Task4_2(host,port,topics,msgs,barrier3));
			tr5.start();
			
			try {
				tr3.join();
				tr4.join();				
				tr5.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
	}

}
