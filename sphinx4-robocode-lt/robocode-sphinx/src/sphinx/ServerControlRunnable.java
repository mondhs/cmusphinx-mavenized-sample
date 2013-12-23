package sphinx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerControlRunnable implements Runnable {

	private RobotControlData controlData;
	ServerSocket server;

	public ServerControlRunnable(RobotControlData controlData) {
		this.controlData = controlData;
	}

	public void shutdhown() {
		debug("server Shutdown " + Thread.currentThread().getId());
		this.controlData.shutdhown = true;
		try {
			server.close();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void run() {
		debug("server started "  + Thread.currentThread().getId());
		try {
			server = new ServerSocket(9090);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		Thread waitForClients = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!controlData.shutdhown) {
					try {
						Socket client = server.accept();
						debug("server got client "  + Thread.currentThread().getId());
						ClientControlRunnable clientControlRunnable = new ClientControlRunnable(
								controlData, client);
						new Thread(clientControlRunnable).start();
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}

				}
				shutdhown();
			}
		});
		waitForClients.start();
	}

	private void debug(String msg) {
		System.out.println(msg);
		
	}
}
