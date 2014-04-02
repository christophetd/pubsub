package pubsub;

public class MyServer {

	public static void main(String[] args) {
		new Thread(new TCPAcceptor()).start();
	}

}
