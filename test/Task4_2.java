package test;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 
 * a task for test 4
 *
 */
public class Task4_2 extends Task{
	
	public Task4_2(String host, int port, ArrayList<String> topics, ArrayList<String> msgs, CyclicBarrier barrier){
			super(host,port,topics,msgs,barrier);
	}
	
	@Override
	public void run() {
		
		try {

			for(int i=0;i<2;i++){
				this.barrier.await();
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)(Math.random()*2)), msgs.get(0));
				Thread.sleep(10);
				output.publish(topics.get((int)Math.random()*2), msgs.get(0));
			}

			this.closeConnection();
			
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

}
