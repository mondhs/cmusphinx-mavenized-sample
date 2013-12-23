/*******************************************************************************
 * Copyright (c) 2001-2013 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *******************************************************************************/
package sphinx;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_W;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.DeathEvent;
import robocode.GunTurnCompleteCondition;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import robocode.util.Utils;

/**
 * VoicedRobot - a modified version of the sample robot Interactive by Flemming
 * N. Larsen to use voice command for movements (up, right, down, left) by Tuan
 * Anh Nguyen.
 * <p/>
 * This is a robot that is controlled using the arrow keys (or WASD) and mouse
 * only.
 * <p/>
 * Keys: - W or arrow up: Move up - S or arrow down: Move down - A or arrow
 * right: Move right - D or arrow left: Move left Mouse: - Moving: Moves the
 * aim, which the gun will follow - Button 1: Fire a bullet with power = 1 -
 * Button 2: Fire a bullet with power = 2 - Button 3: Fire a bullet with power =
 * 3
 * <p/>
 * The bullet color depends on the fire power: - Power = 1: Yellow - Power = 2:
 * Orange - Power = 3: Red
 * <p/>
 * Note that the robot will continue firing as long as the mouse button is
 * pressed down.
 * <p/>
 * By enabling the "Paint" button on the robot console window for this robot, a
 * cross hair will be painted for the robots current aim (controlled by the
 * mouse).
 * 
 * @author Flemming N. Larsen (original)
 * @author Tuan Anh Nguyen (contributor)
 * 
 * @version 1.0
 * 
 * @since 1.7.2.2
 */
public class VoicedRobot extends AdvancedRobot {

	// The coordinate of the aim (x,y)
	Point2D.Double aim = new Point2D.Double(0D, 0D);
	
	
	
	

	ServerControlRunnable serverControlRunnable;

	RobotControlData controlData;



	private double bearing;





	private double distance;

	@Override
	public void onStatus(StatusEvent e) {
		// TODO Auto-generated method stub
		super.onStatus(e);
	}

	@Override
	public void onDeath(DeathEvent event) {
		super.onDeath(event);
		debug("onDeath");
		serverControlRunnable.shutdhown();
	}

	@Override
	public void onWin(WinEvent event) {
		super.onWin(event);
		debug("onWin");
		serverControlRunnable.shutdhown();
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		super.onBattleEnded(event);
		debug("onBattleEnded");
		serverControlRunnable.shutdhown();
	}

	public void onScannedRobot(ScannedRobotEvent e) {
	    // Absolute angle towards target
	    double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
	 
	    // Subtract current radar heading to get the turn required to face the enemy, be sure it is normalized
	    double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
	 
	    // Distance we want to scan from middle of enemy to either side
	    // The 36.0 is how many units from the center of the enemy robot it scans.
	    double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
	 
	    // Adjust the radar turn so it goes that much further in the direction it is going to turn
	    // Basically if we were going to turn it left, turn it even more left, if right, turn more right.
	    // This allows us to overshoot our enemy so that we get a good sweep that will not slip.
	    radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);
	    
	    this.bearing = e.getBearing();
	    this.distance = e.getDistance(); 
//	    double aimAngle = e.getBearingRadians();
//	    aim.x = getX()+(e.getDistance()* Math.cos(aimAngle));
//	    aim.y = getY()+(e.getDistance()* Math.sin(aimAngle));
//	    setDebugProperty("aim", "" + aim);
	    setDebugProperty("distance", "" +e.getDistance());
	    setDebugProperty("bearing", "rad: " + e.getBearingRadians() + " deg: " + e.getBearing());
	    //Turn the radar
//	    setTurnRadarRightRadians(radarTurn);
	}

	private void debug(String msg) {
		System.out.println(msg);
	}

	// Called when the robot must run
	public void run() {
		
		controlData = new RobotControlData();
		// Sets the colors of the robot
		// body = black, gun = white, radar = red
		setColors(Color.BLACK, Color.WHITE, Color.RED);

		serverControlRunnable = new ServerControlRunnable(controlData);
		new Thread(serverControlRunnable).start();
//		turnRadarRight(360);
		// Loop forever
		while (true) {
			doScanner();
			doMovement();
			setDebugProperty("command", "" + controlData.command);

			// Fire the gun with the specified fire power, unless the fire power
			// = 0
			if (controlData.firePowerRequest > 0) {
				fireRequest(controlData.firePowerRequest);
				controlData.firePowerRequest -= 1;
			}

			// Execute all pending set-statements
			execute();

			// Next turn is processed in this loop..
		}
	}
	
	private void doMovement() {
		// Sets the robot to move forward, backward or stop moving depending
		// on the move direction and amount of pixels to move
		if(controlData.move != 0){
			ahead(controlData.move);
			setDebugProperty("move", "" + controlData.move);
			controlData.move= 0;
		}

		// Decrement the amount of pixels to move until we reach 0 pixels
		// This way the robot will automatically stop if the mouse wheel
		// has stopped it's rotation
//		if(controlData.move - 1<0){
//			scan();
//		}
			
		controlData.move = Math.max(0, controlData.move - 1);

		

		if(controlData.turnDegree != 0){
			double turnDegree = Math.abs(controlData.turnDegree)-getHeading();
			setDebugProperty("turnDegree", "" + turnDegree);
			turnRight(turnDegree); // degrees
			controlData.turnDegree = 0;
		}
		
	}

	void doScanner() {
		setTurnRadarRight(360);
	}

	private void fireRequest(int firePowerRequest) {
//		setFire(firePowerRequest);
		if(firePowerRequest!=0){
			turnGunRight(getHeading() - getGunHeading() + bearing);
			waitFor(new GunTurnCompleteCondition(this));
			fire(Math.min(400 / distance, 3));
		}
		//turnGunRight(Math.toRadians((getHeading()+bearing) % 360));
		
	}

	// Called when a key has been pressed
	public void onKeyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case VK_UP:
		case VK_W:
			// Arrow up key: move direction = forward (infinitely)
			break;
		}
	}

	// Called when a key has been released (after being pressed)
	public void onKeyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case VK_UP:
		case VK_W:
		case VK_DOWN:
		case VK_S:
		case VK_RIGHT:
		case VK_D:
		case VK_LEFT:
		case VK_A:
			controlData.move = 0;
			controlData.turnDegree = 0;
			break;
		}
	}





	// Called when a mouse button has been pressed
	public void onMousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			// Button 3: fire power = 3 energy points, bullet color = red
			controlData.firePowerRequest = 3;
			setBulletColor(Color.RED);
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			// Button 2: fire power = 2 energy points, bullet color = orange
			controlData.firePowerRequest = 2;
			setBulletColor(Color.ORANGE);
		} else {
			// Button 1 or unknown button:
			// fire power = 1 energy points, bullet color = yellow
			controlData.firePowerRequest = 1;
			setBulletColor(Color.YELLOW);
		}
	}

	// Called when a mouse button has been released (after being pressed)
	public void onMouseReleased(MouseEvent e) {
		// Fire power = 0, which means "don't fire"
		controlData.firePowerRequest = 0;
	}

	// Called in order to paint graphics for this robot.
	// "Paint" button on the robot console window for this robot must be
	// enabled in order to see the paintings.
	public void onPaint(Graphics2D g) {
		 //Draw a red cross hair with the center at the current aim
		 //coordinate (x,y)
//		int x = (int)aim.getX();
//		int y = (int)aim.getY();
		
		int x = (int) getX();
		int y = (int) getY();
		double rad = Math.toRadians((getHeading()+bearing) % 360);
//		double rad = Math.toRadians(degree);
		int targetX = x + (int)(distance * Math.sin(rad));
		int targetY = y + (int)(distance * Math.cos(rad));
		
		 
		
		 g.setColor(Color.RED);
		 g.drawOval(targetX - 15, targetY - 15, 30, 30);
		 g.drawLine(targetX, targetY - 4, targetX, targetY + 4);
		 g.drawLine(targetX - 4, targetY, targetX + 4, targetY);
//		 g.drawLine(x, y, targetX, targetY);
	}

}