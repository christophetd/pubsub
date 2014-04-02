package pubsub;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {
	private ReentrantLock mutex = new ReentrantLock();
	private Condition canRead = mutex.newCondition();
	private Condition canWrite = mutex.newCondition();
	private int nb_readers_waiting = 0;
	private int nb_readers = 0;
	private int nb_writers = 0;
	private List<T> elements = new LinkedList<T>();
	
	public static final int MAX_SIZE = 100;
	
	
	public void pushBack(T cmd) {
		try {
			init_write();
			elements.add(cmd);
			end_write();
		}
		catch(InterruptedException e) {
			System.err.println("[Blocking queue] push operation interrupted!");
			System.err.println(e.getStackTrace());
		}
	}
	
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
	
	private boolean isFull() {
		return elements.size() == MAX_SIZE;
	}
}
