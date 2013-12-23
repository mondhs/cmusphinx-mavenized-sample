package sphinx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientControlRunnable implements Runnable {

	private RobotControlData controlData;
	private Socket client;

	public ClientControlRunnable(RobotControlData controlData, Socket client) {
		this.controlData = controlData;
		this.client = client;
	}

	@Override
	public void run() {
		debug("Cleint started " + Thread.currentThread().getId());
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (IOException e2) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw new IllegalArgumentException(e2);
		}

		while (!controlData.shutdhown) {
			debug("Cleint ready to listen "+ Thread.currentThread().getId());
			try {
				String command = reader.readLine();
				if(command == null){
					break;
				}
				debug("Cleint got command: "+ command);
				controlData.command = command;
			} catch (IOException e) {
				break;
			}
			//GO FORWARD|BACKWARD [1 STEP]
			if (controlData.command.startsWith("GO") ) {
				int move = 0;
				if(controlData.command.contains("FORWARD")){
					move = 1;	
				}else if(controlData.command.contains("BACKWARD")){
					move = -1;
				}
				move *= 100;
				if(controlData.command.contains("TWO")){
					move *= 2;
				}else if(controlData.command.contains("THREE")){
					move *= 3;
				}else if(controlData.command.contains("FOUR")){
					move *= 4;
				}else if(controlData.command.contains("FIVE")){
					move *= 5;
				}
				controlData.move = move;
			//TURN LEFT|RIGHT FULL
			}else if (controlData.command.startsWith("TURN")) {
				int turnDegree = 0;
				if(controlData.command.contains("RIGHT")){
					turnDegree = 1;	
				}else if(controlData.command.contains("LEFT")){
					turnDegree = -1;
				}
				
				if(controlData.command.contains("THREE")){
					turnDegree *= 90;
				}else if(controlData.command.contains("SIX")){
					turnDegree *= 180;
				}else if(controlData.command.contains("NINE")){
					turnDegree *= 270;
				}else if(controlData.command.contains("TWELVE")){
					turnDegree *= 360;
				}
				
				controlData.turnDegree = turnDegree;
			} else if (controlData.command.startsWith("FIRE")) {
				controlData.firePowerRequest = 3; 
			} else if ("STOP".equals(controlData.command)) {
				controlData.move = 0;
				controlData.turnDegree = 0;
			} else if ("EXIT".equals(controlData.command)) {
				break;
			}
		}
		try {
			debug("Cleint closes " + Thread.currentThread().getId());
			reader.close();
			client.close();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	

	private void debug(String msg) {
		System.out.println(msg);
		
	}

}
