package pubsub;

import lsr.concurrence.provided.server.CommandID;

public class Command {

	/**
	 *	The client who executes the command 
	 */
	private Client client;
	
	
	/**
	 *	The commandID
	 *
	 * @see {@link lsr.concurrence.provided.server.CommandID}
	 */
	private CommandID id;
	
	/**
	 * 	The message attached to the command
	 */
	private String message;
	
	
	/**
	 *	The topic of the command  
	 */
	private String topic;
	
	public Command(Client client, CommandID id, String message, String topic) {
		this.id = id;
		this.client = client;
		this.message = message;
		this.topic = topic;
	}

	public Client getClient() {
		return client;
	}

	public CommandID getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getTopic() {
		return topic;
	}
}
