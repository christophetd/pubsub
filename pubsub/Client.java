package pubsub;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a client
 *
 */
public class Client { 

    /**
     *	A lock for the access to the subscriptions of the client
     */
    final private ReentrantLock subscriptionsLock = new ReentrantLock();
    
    /**
     *	A lock for the access to the output stream of the client
     */
    final private ReentrantLock outputLock = new ReentrantLock();
	
	/**
	 * 	The output stream to communicate with the client
	 */
	final private OutputStream output;
	
	/**
	 *	A random id attributed to the client, for debugging purposes 
	 */
	final private String uniqId;


	/**
	 *	The topics the client is subscribed to.
	 *	The double-way mapping (Client => Topics in this class and Topic => Clients in SubscriptionsStore
	 *	gives us an efficient way to both publish and unsubscribe 
	 *
	 * 	@see {@link pubsub.SubscriptionsStore}
	 */
	private Set<String> subscribedTopics = new HashSet<>();
	
	
	/**
	 * Creates a new client 
	 * 
	 * @param output	An output stream to communicate with the client
	 */
	public Client(OutputStream output) {
		this.output = output;
		this.uniqId = String.valueOf(new Random().nextInt(10000));
	}
	
	/**
	 * Adds a topic to the list of subscribed topics
	 * 
	 * @param topic
	 */
	public void markSubscribed(String topic) {
		try {
			subscriptionsLock.lock();
			subscribedTopics.add(topic);
		}
		finally {
			subscriptionsLock.unlock();
		}
	}
	
	/**
	 * Removes a topic to the list of subscribed topics
	 * 
	 * @param topic
	 */
	public void markUnsubscribed(String topic) {
		try {
			subscriptionsLock.lock();
			subscribedTopics.remove(topic);
		}
		finally {
			subscriptionsLock.unlock();
		}
	}

	
	/**
	 * Sends a message to the client
	 * 
	 * @param message	The message to send
	 */
	public void sendMessage(String message) {
		try {
			outputLock.lock();
			output.write((message+"\n").getBytes());
		}
		catch(IOException e) {
			System.err.println("Unable to send message ["+message+"] to client");
			System.err.println(e.getStackTrace());
		}
		finally {
			outputLock.unlock();
		}
	}
	
	/**
	 * @return The set of all topics the client is subscribed to
	 */
	public synchronized Set<String> getSubscribedSubjects() {
		try {
			subscriptionsLock.lock();
			return new HashSet<>(subscribedTopics);
		}
		finally {
			subscriptionsLock.unlock();
		}
		
	}
	
	/**
	 * @return	The uniqid of the client
	 */
	public String getUniqId() {
		return uniqId;
	}
}
