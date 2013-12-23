package sphinx;

public class RobotControlData {

	/**
	 *  Move direction: <1 = move forward, 0 = stand still, -1> = move backward
	 */
	double move = 0;

	/**
	 *  Turn direction: 1 = turn right, 0 = no turning, -1 = turn left
	 */
	int turnDegree = 0;

	/**
	 *  Amount of pixels/units to move
	 */
//	double moveAmount = 0;
	
	String command = "nothing";

	public boolean shutdhown =false;
	/**
	 *  Fire power, where 0 = don't fire
	 */
	public int firePowerRequest = 0;
	
	public RobotControlData() {
	}

}
