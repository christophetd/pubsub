package pubsub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Stores a map of subjects and their subscribed users. It ensures no change happen to the map while it
 * is being read and no two threads try to change it at the same time. Also, each set of clients (groupped
 * by subject) has it's own read/write lock allowing maximum granularity of the locking system.
 * 
 * Two concurrent read can occur and a write operation to one specific set of client is NOT considered a 
 * write operation on the whole map. Therefore, the following situations are possible:
 * 
 *          use case       |  global read  | global write  | local read | local write
 * ------------------------+---------------+---------------+------------+-------------
 *  iterating over clients |        X      |               |     X      |
 *  subscribe/unsubscribe  |        X      |               |            |     X
 *  add/remove subject     |               |        X      |            |
 *  
 */
public class SubscriptionsStore {
	private HashMap<String, ClientSet> subscriptions = new HashMap<>();
	
	private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
	
	/**
	 * Subscribes c to subj
	 * @param client client to subscribe
	 * @param subj subject to subscribe to
	 */
	public void subscribe(Client client, String subj) {
		
		// General case, no need for a write lock
		mutex.readLock().lock();
		System.out.println("Client "+client.getUniqId()+" subscribing to "+subj);
		ClientSet clients = null;
		
		while((clients = subscriptions.get(subj)) == null) {
			// Exceptional case, need a write lock to create subject
			mutex.readLock().unlock();
			mutex.writeLock().lock();
			
			// Then add a subject
			if (subscriptions.get(subj) == null) {
				clients = new ClientSet();
				subscriptions.put(subj, clients);
			}
			mutex.writeLock().unlock();
			mutex.readLock().lock();
		}
		
		// Now we have read lock and clients associated to a subject
		clients.add(client);
		client.markSubscribed(subj);
		mutex.readLock().unlock();
	}
	
	/**
	 * Unsubscribes client from subj
	 * @param client to unsubscribe
	 * @param subj subject to unsubscribe from
	 */
	public void unsubscribe(Client client, String subj) {
		mutex.readLock().lock();
		
		ClientSet clients = subscriptions.get(subj);
		
		if(clients == null) {
			System.err.println("Client unsubscribes from unexisting subject");
			mutex.readLock().unlock();
			return;
		}
		
		clients.remove(client);
		client.markUnsubscribed(subj);
		
		if(clients.isEmpty()) {
			// Exceptional case, need a write lock to remove subject
			mutex.readLock().unlock();
			mutex.writeLock().lock();
			
			clients = subscriptions.get(subj);
			// only one thread at a time can reach this portion (therefore, manipulate clients) so the portion is thread-safe
			// even though isEmpty() isn't
			if (clients != null && clients.isEmpty()) {
				// Then remove subject
				subscriptions.remove(subj);
			}
			mutex.writeLock().unlock();
		} else {
			mutex.readLock().unlock();
		}
	}
	
	/**
	 * Calls cb.run for each client subscribed to subject, ensuring no modification of this part of the store occurs during the iteration
	 * @param subject subject to iterate
	 * @param cb callback to be called on each client subscribed to subject
	 */
	public void iterateSubject(String subject, Callback cb) {
		mutex.readLock().lock();
		ClientSet clients = subscriptions.get(subject);
		if (clients != null) {
			clients.iterate(cb);
		}
		mutex.readLock().unlock();
	}
	
	public static interface Callback {
		public void run(Client c);
	}
	
	/**
	 * Local collection of clients subscribed to a given subject. Implements it's own locking mecanism
	 */
	public class ClientSet {
		Set<Client> clients = new HashSet<>();
		private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
		
		public void add(Client client) {
			mutex.writeLock().lock();
			clients.add(client);
			mutex.writeLock().unlock();
		}
		
		public void remove(Client client) {
			mutex.writeLock().lock();
			clients.remove(client);
			mutex.writeLock().unlock();
		}
		
		// This is NOT thread safe but no big deal, we take care of this above.
		public boolean isEmpty() {
			return clients.isEmpty();
		}
		
		// Runns cb on each elements while ensuring read is allowed.
		public void iterate(Callback cb) {
			mutex.readLock().lock();
			for (Client c : clients) {
				cb.run(c);
			}
			mutex.readLock().unlock();
		}
	}
}
