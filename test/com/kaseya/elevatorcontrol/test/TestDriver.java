package com.kaseya.elevatorcontrol.test;

import java.util.Random;
import java.util.logging.Level;

import com.kaseya.elevatorcontrol.Direction;
import com.kaseya.elevatorcontrol.Elevator;
import com.kaseya.elevatorcontrol.ElevatorControl;

/**
 * This is a simulator to test the Elevator Control
 * @author Henry
 *
 */
public class TestDriver {

	public static void main(String[] args) {
		ElevatorControl elevatorControl = new ElevatorControl();
		elevatorControl.init();
		Random rand = new Random();
		// random calls to call elevator and request floors
		for (int i = 0; i < 100; i++) {
			int floorBegin = rand.nextInt(ElevatorControl.TOP_FLOOR)+1;
			int floorEnd = rand.nextInt(ElevatorControl.TOP_FLOOR)+1;
			elevatorControl.callElevator(floorBegin, Direction.UP);
			ElevatorControl.log.log(Level.INFO, "Calling Elevator");
			Elevator elevator = elevatorControl.getFirstElevatorOnMyFloor(floorBegin);
			
			int waitIntervalsLimit = 100;
			int waitIntervalIndex = 0;
			// wait for the elevator to arrive
			// note that the elevatorEvent is triggered on 'callElevator'
			// as this is a 'simulator', its async and thus the wait with a timeout and a limit
			// - basically polling.
			while (elevator == null && waitIntervalIndex < waitIntervalsLimit) {
				synchronized (elevatorControl.elevatorEvent) {
					try {
						elevatorControl.elevatorEvent.wait(100); // waiting for my elevator
						waitIntervalIndex++;
					} catch (Exception e) {
						e.printStackTrace();
					}
					elevator = elevatorControl.getFirstElevatorOnMyFloor(floorBegin);
				}
				
			}
			if (elevator != null) {
				ElevatorControl.log.log(Level.INFO, "My elevator has arrived!");
				elevatorControl.requestFloor(elevator, floorEnd, rand.nextBoolean());
			}
		}
		
	}

}
