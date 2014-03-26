package pubsub;

public class MessageBroker implements Runnable {
	private MyBlockingQueue<Command> buffer;
	
	public MessageBroker(MyBlockingQueue<Command> buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public void run() {
		while(true) {
			Command nextCommand = buffer.popFront();
		}
	}

}
