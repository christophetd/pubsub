package pubsub;


/**
 *	Class responsible for reading and executing commands from a buffer.
 *	Intended to be run concurrently within several threads.
 */
public class CommandHandler implements Runnable {
	
	/**
	 *	The command buffer (thread-safe) 
	 */
	private MyBlockingQueue<Command> buffer;
	
	/**
	 *	The subscription store (thread-safe), containing the subscriptions 
	 */
	private SubscriptionsStore subscriptions;
	
	
	/**
	 * ACK messages (%s will be replaced by the topic)
	 */
	final static private String CONNECTION_ACK_MSG = "connection_ack %s";
	final static private String SUBSCRIBE_ACK_MSG = "subscribe_ack %s";
	final static private String UNSUBSCRIBE_ACK_MSG = "unsubscribe_ack %s";
	
	public CommandHandler(MyBlockingQueue<Command> buffer, SubscriptionsStore subs) {
		this.buffer = buffer;
		this.subscriptions = subs;
	}
	
	@Override
	public void run() {
		while(true) {
			Command command = buffer.popFront();
			
			Client client 	= command.getClient();
			String topic 	= command.getTopic();
			String message 	= command.getMessage();
			
			
			switch(command.getId()) {
				
				case NEWCLIENT:
					connectionAck(client);
					break;
		
				case SUBSCRIBE:
					subscribe(client, topic);
					break;
					
				case UNSUBSCRIBE:
					unsubscribe(client, topic);
					break;
					
				case PUBLISH:
					publish(topic, message);
					break;
					
				case ENDOFCLIENT:
					disconnect(client);
					break;
			
			}
		} 
	}
	
	/**
	 * Sends a connection ack to a client
	 * 
	 * @param client
	 */
	private void connectionAck(Client client) {
		client.sendMessage(String.format(CONNECTION_ACK_MSG, client.getUniqId()));		
	}

	private void publish(String topic, String message) {
		
		if(!subscriptions.hasTopic(topic)) {
			System.err.println("Client is trying to publish to unexisting subject");
			return;
		}
		
		
		String data = topic + " " + message;
		for(Client client: subscriptions.get(topic)) {
			client.sendMessage(data);
		}
	}
	

	private void unsubscribe(Client client, String topic) {
		subscriptions.unsubscribe(client, topic);
		client.sendMessage(String.format(UNSUBSCRIBE_ACK_MSG, topic)); 
	}

	private void disconnect(Client client) {
		for(String subject: client.getSubscribedSubjects()) {
			unsubscribe(client, subject);
		}
	}

	private void subscribe(Client client, String topic) {
		System.out.println("Subscribing to "+topic);
		
		subscriptions.subscribe(client, topic);
		
		client.sendMessage(String.format(SUBSCRIBE_ACK_MSG, topic)); 
		
	}

}
