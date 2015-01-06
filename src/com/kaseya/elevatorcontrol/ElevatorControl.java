package com.kaseya.elevatorcontrol;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An elevator control to summon and request floors
 * @author Henry
 *
 */
public class ElevatorControl {
	public static Logger log = Logger.getGlobal();
	private ArrayList<Elevator> elevators;
	public static final int MAX_AVAILABLE = 2;
	public static final int TOP_FLOOR = 6; // penthouse
	public static final String TOP_FLOOR_PASSWORD = "abc123";
	public Object elevatorEvent = new Object();

	public ArrayList<Elevator> getElevators() {
		return elevators;
	}

	public void setElevators(ArrayList<Elevator> elevators) {
		this.elevators = elevators;
	}

	public void init() {
		elevators = new ArrayList<Elevator>();
		for (int i = 0; i < MAX_AVAILABLE; i++) {
			Elevator elevator = new Elevator(this);
			elevators.add(elevator);
		}
	}

	/*
	 * you push either the UP or DOWN button to summon an elevator
	 * Note that after you have 'call'ed aka summoned an elevator, it is async, and hence you
	 * will have to wait
	 */
	public void callElevator(final int fromFloor, final Direction direction) {
		Runnable callElevatorRunnable = new Runnable() {
			@Override
			public void run() {
				Elevator elevatorFound = null;
				while (elevatorFound == null) {
					// search for an elevator that is not busy or is headed in my direction
					for (Elevator elevator : elevators) {
						if (!elevator.isBusy() || elevator.isHeadedTowards(fromFloor, direction)) {
							elevatorFound = elevator;
							break;
						}
					}
					// if no elevator is found ... wait
					if (elevatorFound == null) {
						synchronized (elevatorEvent) {
							
							try {
								elevatorEvent.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
						// move to that floor
						elevatorFound.moveTo(fromFloor);
					}
				}

			}
		};
		new Thread(callElevatorRunnable).start();
	}
	
	/*
	 * presumably there is an open elevator here, and you just hopped in and have requested a floor
	 */
	public void requestFloor(Elevator elevator, int toFloor, boolean hasKeyToTopFloor) {
		// do nothing if the requested floor is the floor that the elevator is on
		if (elevator.getFloor() == toFloor) {
			elevator.getFloorsToStop().remove(toFloor);
			return;
		} 
		// make sure you can't press a button to a floor if it is in the opposite direction
		if (!elevator.isHeadedTowards(toFloor, elevator.getDirection())) {
			log.log(Level.WARNING, "Can't request a floor that is in the opposite direction, " +
					"currentFloor: " + elevator.getFloor() + ", requestedFloor: " + toFloor 
					+ ", direction: " + elevator.getDirection());
			return;
		}
		// another requirement (addendum requirement? ;))
		if (toFloor == TOP_FLOOR && !hasKeyToTopFloor) {
			// Only the single Penthouse tenant, landlord and Emergency Service (fire, police...) 
			// should have access to the Penthouse.
			log.log(Level.WARNING, "Invalid access to penthouse floor");
			return;
		}
		elevator.moveTo(toFloor);
	}
	
	
	// returns the first elevator on my floor
	public Elevator getFirstElevatorOnMyFloor(int floor) {
		Elevator retval = null;
		for (Elevator elevator : elevators) {
			if (elevator.getFloor() == floor) {
				retval = elevator;
				break;
			}
		}
		return retval;
	}
}
