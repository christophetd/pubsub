package test;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import lsr.concurrence.provided.tests.InputChecker;


/**
 * 
 * a task for test 3
 *
 */
public class Task3_1 extends Task{
	
	public Task3_1(String host, int port, ArrayList<String> topics, ArrayList<String> msgs, CyclicBarrier barrier){
		super(host,port,topics,msgs,barrier);
	}
	
	@Override
	public void run() {
		
		InputChecker inputCheck= new InputChecker(this.input);
		
		if(inputCheck.checkConnected()!=0){
			System.out.println("Error in connection");
			this.closeConnection();
			return;
		}
	
		try {
			this.barrier.await();
			output.publish(topics.get(0), msgs.get(0));

			this.barrier.await();
			
			this.closeConnection();
		
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
