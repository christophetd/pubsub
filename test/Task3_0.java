package test;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import lsr.concurrence.provided.tests.InputChecker;


/**
 * 
 * A task for test 3
 *
 */

public class Task3_0 extends Task{
	
	public Task3_0(String host, int port, ArrayList<String> topics, ArrayList<String> msgs, CyclicBarrier barrier){
			super(host,port,topics,msgs,barrier);
	}
	
	@Override
	public void run() {
		int error=0;
		InputChecker inputCheck= new InputChecker(this.input);
		
		if(inputCheck.checkConnected()!=0){
			System.out.println("Error in connection");
			this.closeConnection();
			return;
		}

		try {

			output.subscribeTo(topics.get(0));
			error += inputCheck.checkSubscribe(topics.get(0));

			this.barrier.await();
			error += inputCheck.checkPublish(topics.get(0), msgs.get(0));			
			
			this.barrier.await();
			output.unsubscribeTo(topics.get(0));
			error +=inputCheck.checkUnsubscribe(topics.get(0));
			
			System.out.print("**** TEST 3: ");
			if(error==0){
				 System.out.println("passed\n");
			}
			else{
				System.out.println("failed\n");
				System.exit(1);
			}

			this.closeConnection();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
		
		
	}

}
