package pubsub;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generic blocking queue implementing the producer/consummer problem:
 * one producer at a time can add something to the queue,
 * one consummer at a time can grab the last message on the queue
 *
 */
public class MyBlockingQueue<T> {
	private ReentrantLock mutex = new ReentrantLock();
	private Condition canRead = mutex.newCondition();
	private Condition canWrite = mutex.newCondition();
	private int nb_readers_waiting = 0;
	private int nb_readers = 0;
	private int nb_writers = 0;
	private List<T> elements = new LinkedList<T>();
	
	public static final int MAX_SIZE = 100;
	
	/**
	 * Adds an element at the end of the queue
	 * @param el
	 */
	public void pushBack(T el) {
		try {
			init_write();
			elements.add(el);
			end_write();
		}
		catch(InterruptedException e) {
			System.err.println("[Blocking queue] push operation interrupted!");
			System.err.println(e.getStackTrace());
		}
	}
	
	/**
	 * Removes the element at the front of the queue and returns it.
	 * @return element at the front of the queue
	 */
	public T popFront() {
		T cmd = null;
		try {
			init_read();
			cmd = elements.get(0);
			elements.remove(0);
			end_read();
		}
		catch(InterruptedException err) {
			System.err.println("[Blocking queue] pop operation interrupted!");
			System.err.println(err.getStackTrace());
		}
		
		return cmd;
	}
	
	/**
	 * Starts a read critical section
	 * @throws InterruptedException
	 */
	private void init_read() throws InterruptedException {
		try {
			mutex.lock();
			++nb_readers_waiting;
			while(nb_readers > 0 || nb_writers > 0 || elements.isEmpty()) {
				canRead.await();
			}
			--nb_readers_waiting;
			++nb_readers;
		}
		finally {
			mutex.unlock();
		}
	}
	
	/**
	 * Ends a read critical section
	 * @throws InterruptedException
	 */
	private void end_read() throws InterruptedException {
		try {
			mutex.lock();
			--nb_readers;
			if(nb_readers == 0) {
				canWrite.signal();
			} else {
				canRead.signal();
			}
		}
		finally {
			mutex.unlock();
		}
	}
	
	/**
	 * Starts a write critical section
	 * @throws InterruptedException
	 */
	private void init_write() throws InterruptedException  {
		try {
			mutex.lock();
			while(nb_readers > 0 || nb_writers > 0 || isFull()) {
				canWrite.await();
			}
			++nb_writers;
		}
		finally {
			mutex.unlock();
		}
	}
	
	/**
	 * Ends a write critical section
	 * @throws InterruptedException
	 */
	private void end_write() throws InterruptedException {
		try {
			mutex.lock();
			--nb_writers;
			
			if(nb_readers_waiting > 0) {
				canRead.signal();
			}
			else {
				canWrite.signal();
			}
		}
		finally {
			mutex.unlock();
		}
	}
	
	/**
	 * Returns true when queue is full
	 */
	private boolean isFull() {
		return elements.size() == MAX_SIZE;
	}
}
