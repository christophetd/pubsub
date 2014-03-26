package test;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import lsr.concurrence.provided.tests.InputSequenceChecker;


/**
 * 
 * a task for test 4
 *
 */

public class Task4_0 extends Task{
	
	public Task4_0(String host, int port, ArrayList<String> topics, ArrayList<String> msgs, CyclicBarrier barrier){
			super(host,port,topics,msgs,barrier);
	}
	
	@Override
	public void run() {
		
		InputSequenceChecker seqChecker=new InputSequenceChecker(this.input,3);
		Thread t = new Thread(seqChecker);
		t.setDaemon(true);
		t.start();

		try {
			
			for(int i=0;i<2;i++){
				this.barrier.await();
				Thread.sleep((long)(Math.random()*30));
				output.subscribeTo(topics.get(0));
				Thread.sleep((long)(Math.random()*30));
				output.subscribeTo(topics.get(1));
				Thread.sleep((long)(Math.random()*30));
				output.unsubscribeTo(topics.get(0));
				Thread.sleep((long)(Math.random()*30));
				output.unsubscribeTo(topics.get(1));
			}
			
			Thread.sleep(1000);
			int error=seqChecker.getNumberOfError();
			if(error==0){
				System.out.print("("+this.getClass().getName()+")**** TEST 4: passed\n");
			}
			else{
				System.out.print("("+this.getClass().getName()+")**** TEST 4: failed\n");
				System.exit(1);
			}
			
			this.closeConnection();
						
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

}

