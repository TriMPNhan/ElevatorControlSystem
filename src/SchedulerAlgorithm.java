import java.util.ArrayList;

public class SchedulerAlgorithm {

	private ArrayList<ArrayList<Byte>> elevatorStops; // Index: Elevators, ArrayList: Elevator stops
	private ArrayList<Byte> currentFloor; // Index: Elevators, Byte: Current floor
	private ArrayList<Boolean> stopElevator; // Index: Elevators, Boolean: Should stop elevator
	private ArrayList<ArrayList<Byte>> elevatorDestinations; // Index: Elevators, ArrayList: Elevator destinations

	public SchedulerAlgorithm(byte numElevators) {
		elevatorStops = new ArrayList<ArrayList<Byte>>();
		currentFloor = new ArrayList<Byte>();
		stopElevator = new ArrayList<Boolean>();
		elevatorDestinations = new ArrayList<ArrayList<Byte>>();
		setNumberOfElevators(numElevators);
	}

	/**
	 * Called when someone on the floor has requested an elevator
	 * 
	 * @param source
	 * @param destination
	 * @param upOrDown
	 * @return
	 */
	public byte elevatorRequestMade(Byte source, Byte destination, UtilityInformation.ElevatorDirection upOrDown) {
		System.out.println("Elevator was requested at: " + source + " in the direction " + upOrDown
				+ " with destination " + destination);

		byte elevatorNum = addElevatorRequest(source, destination);
		return (elevatorNum);
	}

	/**
	 * Scheduler has been informed where the elevator is. Update the stopElevator
	 * and currentFloor ArrayList. Remove the current floor from destinations.
	 * 
	 * @param floorNum
	 * @param elevatorNum
	 */
	public void elevatorHasReachedFloor(Byte floorNum, Byte elevatorNum) {
		System.out.println("Elevator " + elevatorNum + " has reached floor: " + floorNum);

		if (elevatorStops.get(elevatorNum).contains(floorNum)) {
			System.out.println("Current floor is a destination.");
			stopElevator.set(elevatorNum, true);
		} else {
			stopElevator.set(elevatorNum, false);
		}

		currentFloor.set(elevatorNum, floorNum);
		removeFloorFromDestinations(floorNum, elevatorNum);
	}

	/**
	 * Remove a floor from an elevator's destinations
	 * 
	 * @param currentFloor
	 * @param currentElevator
	 */
	private void removeFloorFromDestinations(byte currentFloor, byte currentElevator) {
		if (elevatorStops.get(currentElevator).contains(currentFloor)) {
			int indToRemove = elevatorStops.get(currentElevator).indexOf(currentFloor);
			if (indToRemove != -1) {
				elevatorStops.get(currentElevator).remove(indToRemove);
			}

			indToRemove = elevatorDestinations.get(currentElevator).indexOf(currentFloor);

			if (indToRemove != -1) {
				elevatorDestinations.get(currentElevator).remove(indToRemove);
			}

			System.out.println(currentFloor + " was removed from the destinations list");
			System.out.println("New destination list: " + elevatorStops.toString() + "\n");
		}
	}

	/**
	 * NOTE: This is not used yet, as nobody can press a button while in the
	 * elevator in real-time at the moment.
	 * 
	 * @param pressedButton
	 * @param elevatorNum
	 */
	public void floorButtonPressed(Byte pressedButton, byte elevatorNum) {
		elevatorDestinations.get(elevatorNum).add(pressedButton);
	}

	/**
	 * When a elevator is requested from the floor. The request goes through an
	 * algorithm and is assigned to an elevator. That elevator will have the
	 * startFloor and endFloor assigned to it, and that elevator will be tasked with
	 * fulfilling the request
	 * 
	 * @param startFloor
	 * @param endFloor
	 * @return Elevator request was assigned to
	 */
	private byte addElevatorRequest(byte startFloor, byte endFloor) {

		// TODO Note: This will be changed from iteration to iteration for optimization
		// purposes

		ArrayList<Integer> closestElevator = new ArrayList<Integer>();
		int closestDiff = -1;
		int chosenElevator = -1;
		int currDiff;

		// Add destination to closest elevator
		for (int i = 0; i < currentFloor.size(); i++) {
			currDiff = Math.abs(currentFloor.get(i) - startFloor);
			if ((closestDiff == -1) || (currDiff <= closestDiff)) { // same as below
				closestElevator.add(i);
				closestDiff = currDiff;
			}
		}

		// Break ties by prioritizing elevators above the start floor
		if (closestElevator.size() > 1) {
			ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();

			for (int i = 0; i < closestElevator.size(); i++) {
				if (currentFloor.get(closestElevator.get(i)) < startFloor) {
					indicesToRemove.add(i);
				}
			}

			if (indicesToRemove.size() < closestElevator.size()) {
				for (int index : indicesToRemove) {
					closestElevator.set(index, null);
				}
			}

			while (closestElevator.remove(null)) {
			}
			;

			// Break ties by prioritizing shortest queues
			int shortestQueue = -1;
			if (closestElevator.size() > 1) {
				for (int i = 0; i < closestElevator.size(); i++) {
					if ((shortestQueue == -1) || (elevatorStops.get(closestElevator.get(i)).size() < shortestQueue)) {
						chosenElevator = closestElevator.get(i);
					}
				}
			} else {
				chosenElevator = closestElevator.get(0);
			}
		} else {
			chosenElevator = closestElevator.get(0);
		}

		int currFloor = currentFloor.get(chosenElevator);
		int startInd = -1;
		int closestInd = 0;
		closestDiff = Integer.MAX_VALUE;
		ArrayList<Byte> currDests = elevatorStops.get(chosenElevator);

		if (!elevatorDestinations.get(chosenElevator).contains(endFloor)) {
			elevatorDestinations.get(chosenElevator).add(endFloor);
		}

		if (currDests.size() == 0) {
			currDests.add(startFloor);
			currDests.add(endFloor);
		} else {
			if (!(currDests.contains(startFloor))) {
				int maxInd = currDests.size();

				if (currDests.contains(endFloor)) {
					maxInd = currDests.indexOf(endFloor);
				}

				if (((currFloor < startFloor) && (startFloor < currDests.get(0)))
						|| ((currFloor > startFloor) && (startFloor > currDests.get(0)))) {
					startInd = 0;
				} else {
					for (int i = 0; i < maxInd; i++) {
						if (i != maxInd - 1) {
							if (((currDests.get(i) < startFloor) && (startFloor < currDests.get(i + 1)))
									|| ((currDests.get(i) > startFloor) && (startFloor > currDests.get(i + 1)))) {
								startInd = i + 1;
							}
						}

						currDiff = Math.abs(currDests.get(i) - startFloor);
						if (currDiff < closestDiff) {
							closestDiff = currDiff;
							closestInd = i + 1;
						}

					}

					if (startInd == -1) {
						startInd = closestInd;
					}
				}

				currDests.add(startInd, startFloor);
			} else {
				startInd = currDests.indexOf(startFloor);
			}

			closestInd = -1;
			closestDiff = Integer.MAX_VALUE;
			int endInd = -1;

			if (!(currDests.contains(endFloor))) {
				for (int i = startInd; i < currDests.size(); i++) {
					if (i != currDests.size() - 1) {
						if (((currDests.get(i) < endFloor) && (currDests.get(i + 1) > endFloor))
								|| ((currDests.get(i) > endFloor) && (currDests.get(i + 1) < endFloor))) {
							endInd = i + 1;
						}
					}

					currDiff = Math.abs(currDests.get(i) - endFloor);
					if (currDiff < closestDiff) {
						closestDiff = currDiff;
						closestInd = i;
					}

				}

				if (endInd == -1) {
					endInd = closestInd;
				}

				if (endInd != 0) {
					endInd += 1;
				}

				currDests.add(endInd, endFloor);
			}
		}

		System.out.println("New request list: " + elevatorStops + "\n");

		return ((byte) chosenElevator);
	}

	/**
	 * Determine the direction the given elevator should travel
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public UtilityInformation.ElevatorDirection whatDirectionShouldTravel(byte elevatorNum) {
		if (elevatorStops.get(elevatorNum).size() != 0) {
			int nextFloor = elevatorStops.get(elevatorNum).get(0);

			if (nextFloor > currentFloor.get(elevatorNum)) {
				return (UtilityInformation.ElevatorDirection.UP);
			} else if (nextFloor < currentFloor.get(elevatorNum)) {
				return (UtilityInformation.ElevatorDirection.DOWN);
			} else {
				return (UtilityInformation.ElevatorDirection.STATIONARY);
			}
		} else {
			return (UtilityInformation.ElevatorDirection.STATIONARY);
		}
	}

	/**
	 * Check to see if there are floors to go above the current one for a given
	 * elevator
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public boolean floorsToGoToAbove(byte elevatorNum) {
		for (byte tempFloor : elevatorStops.get(elevatorNum)) {
			if (tempFloor > currentFloor.get(elevatorNum)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check to see if there are floors to go below for a given elevator
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public boolean floorsToGoToBelow(byte elevatorNum) {
		for (byte tempFloor : elevatorStops.get(elevatorNum)) {
			if (tempFloor < currentFloor.get(elevatorNum)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if a given elevator has anywhere to go
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public boolean somewhereToGo(byte elevatorNum) {
		return (!elevatorStops.get(elevatorNum).isEmpty());
	}

	/**
	 * Set the number of elevators from the given schematics. Updates all the
	 * ArrayLists that need to be given the correct number of elevators to be
	 * initialized
	 * 
	 * @param numElevators
	 */
	public void setNumberOfElevators(byte numElevators) {
		while (stopElevator.size() > numElevators) {
			elevatorStops.remove(elevatorStops.size() - 1);
			currentFloor.remove(currentFloor.size() - 1);
			stopElevator.remove(stopElevator.size() - 1);
			elevatorDestinations.remove(elevatorDestinations.size() - 1);
		}

		while (stopElevator.size() < numElevators) {
			currentFloor.add((byte) 0);
			stopElevator.add(true);

			ArrayList<Byte> temp = new ArrayList<Byte>();
			temp.add((byte) 0);

			elevatorStops.add(temp);

			temp = new ArrayList<Byte>();
			temp.add((byte) 0);

			elevatorDestinations.add(temp);
		}
	}

	/**
	 * Get an elevators destinations
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public ArrayList<Byte> getDestinations(byte elevatorNum) {
		return elevatorDestinations.get(elevatorNum);
	}

	/**
	 * Get an elevators stops
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public ArrayList<Byte> getElevatorStops(byte elevatorNum) {
		return elevatorStops.get(elevatorNum);
	}

	/**
	 * Get the current floor of an elevator
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public byte getCurrentFloor(byte elevatorNum) {
		return currentFloor.get(elevatorNum);
	}

	/**
	 * Determine if the current elevator should stop
	 * 
	 * @param elevatorNum
	 * @return
	 */
	public boolean getStopElevator(byte elevatorNum) {
		return stopElevator.get(elevatorNum);
	}

	/**
	 * Set the current elevator to stop
	 * 
	 * @param elevatorNum
	 * @param newVal
	 */
	public void setStopElevator(byte elevatorNum, boolean newVal) {
		stopElevator.set(elevatorNum, newVal);
	}

	/**
	 * Print all the information in the lists (for testing purposes)
	 */
	public void printAllInfo() {
		System.out.println(elevatorStops);
		System.out.println(currentFloor);
		System.out.println(stopElevator);
		System.out.println(elevatorDestinations);
	}
}
