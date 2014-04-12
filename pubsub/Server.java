package pubsub;

public class Server {

	public static void main(String[] args) {
		new Thread(new TCPAcceptor()).start();
	}

}
