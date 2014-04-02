package pubsub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionsStore {
	private HashMap<String, Set<Client>> subscriptions = new HashMap<>();
	
	public synchronized void subscribe(Client c, String topic) {
		System.out.println("Client "+c.getUniqId()+" subscribing to "+topic);
		Set<Client> clients = subscriptions.get(topic);
		if(clients == null) {
			clients = new HashSet<Client>();
			subscriptions.put(topic, clients);
		}
		clients.add(c);
		c.markSubscribed(topic);
	}
	
	public synchronized void unsubscribe(Client client, String topic) {
		Set<Client> clients = subscriptions.get(topic);
		
		
		if(clients == null) {
			System.err.println("Client unsubscri");
			return;
		}
		
		clients.remove(client);
		client.markUnsubscribed(topic);
		
		if(clients.isEmpty()) {
			subscriptions.remove(topic);
		}
	}
	
	public synchronized Set<Client> get(String topic) {
		return new HashSet<>(subscriptions.get(topic));
	}
	
	public synchronized boolean hasTopic(String topic) {
		return subscriptions.containsKey(topic);
	}
}
