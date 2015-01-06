package com.kaseya.elevatorcontrol;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * 
 * @author Henry
 *
 * Elevator model
 */
public class Elevator {
	private int floor = 1; // ground floor is 1
	private Direction direction = Direction.NONE;
	private Set<Integer> floorsToStop = new TreeSet<Integer>();

	ElevatorControl elevatorControl;

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Set<Integer> getFloorsToStop() {
		return floorsToStop;
	}

	public void setFloorsToStop(Set<Integer> floorsToStop) {
		this.floorsToStop = floorsToStop;
	}

	public ElevatorControl getElevatorControl() {
		return elevatorControl;
	}

	public void setElevatorControl(ElevatorControl elevatorControl) {
		this.elevatorControl = elevatorControl;
	}

	public Elevator(ElevatorControl elevatorControl) {
		this.elevatorControl = elevatorControl;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	/*
	 * is the elevator busy?
	 */
	public boolean isBusy() {
		return floorsToStop.size() > 0;
	}


	/*
	 * moves the elevator to that floor ... an async call
	 */
	public void moveTo(final int toFloor) {

		if (floor == toFloor) {
			direction = Direction.NONE;
		} else if (floor > toFloor) {
			direction = Direction.DOWN;
		} else {
			direction = Direction.UP;

		}
		floorsToStop.add(toFloor);

		Runnable simulateMoving = new Runnable() {

			@Override
			public void run() {
				try {
					// simulates time that an elevator door is opening/closing,
					// moving, stuck, etc
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ElevatorControl.log.log(Level.INFO, "Destination arrived, floor: " + toFloor);
				floorsToStop.remove(toFloor);
				Elevator.this.floor = toFloor;
				synchronized (elevatorControl.elevatorEvent) {
					elevatorControl.elevatorEvent.notifyAll(); // notify the elevatorControl about where this elevator is
				}
				
			}
		};
		new Thread(simulateMoving).start();

	}

	/*
	 * is the elevator headed towards that floor direction-wise ?
	 */
	public boolean isHeadedTowards(int toFloor, Direction direction) {
		boolean retval = false;
		if (direction.equals(Direction.NONE) || (direction.equals(Direction.UP) && toFloor > floor)
				|| (direction.equals(Direction.DOWN) && toFloor < floor)) {
			retval = true;
		}
		return retval;
	}
}
