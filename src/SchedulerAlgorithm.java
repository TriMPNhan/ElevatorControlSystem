import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Arrays;

public class SchedulerAlgorithm {

	// TODO
	/**
	 * Note: I think destinations should be elevator independent between elevators,
	 * and elevatorRequests should be shared between elevators
	 */

	private ArrayList<Byte> elevatorDestinations; // Destinations
	private ArrayList<ArrayList<Byte>> elevatorRequests; // Source, Destination
	private byte currentFloor;

	/**
	 * Called when someone on the Floor has requested the elevator.
	 * 
	 * @param packet
	 */
	public void elevatorRequestMade(DatagramPacket packet) {
		System.out.println("Elevator was requested at: " + packet.getData()[1] + " in the direction "
				+ packet.getData()[2] + " with destination " + packet.getData()[3]);
		ArrayList<Byte> request = new ArrayList<Byte>();
		request.add(packet.getData()[1]);
		request.add(packet.getData()[3]);
		addElevatorRequest(request);

	}

	/**
	 * Called when the sensor informs the scheduler where the elevator is.
	 * 
	 * @param packet
	 */
	public void elevatorHasReachedFloor(DatagramPacket packet) {
		currentFloor = packet.getData()[1];
		System.out.println("Elevator has reached floor: " + currentFloor);
		if (elevatorDestinations.contains(currentFloor)) {
			System.out.println("Current floor is a destination.");
			// Stop the elevator and open the doors
		}
		for (ArrayList<Byte> request : elevatorRequests) {
			if (request.get(0) == currentFloor) {
				System.out.println("Current floor is a request source.");
				// Stop the elevator and open the doors
				addDestination(request.get(1));
				removeRequest(request);

			}
		}
		System.out.println();
		removeFloorFromDestinations(currentFloor);
		moveElevator();
	}

	private void moveElevator() {
		UtilityInformation.ElevatorDirection direction = null; // TODO I need the elevator direction
		if (direction.equals(UtilityInformation.ElevatorDirection.STATIONARY) && somewhereToGo()) {
			// Close elevator doors
			if (elevatorShouldGoUp()) {
				// Send elevator up
			} else {
				// Send elevator down
			}
		} else if (direction.equals(UtilityInformation.ElevatorDirection.UP) && somewhereToGo()) {
			// Close elevator doors
			if (floorsToGoToAbove()) {
				// Send elevator up
			} else {
				// Send elevator down
			}
		} else if (direction.equals(UtilityInformation.ElevatorDirection.DOWN) && somewhereToGo()) {
			// Close elevator doors
			if (floorsToGoToBelow()) {
				// Send elevator down
			} else {
				// Send elevator up
			}
		} else {
			// Stop elevator
			// Open elevator doors
		}
	}

	/**
	 * Remove a floor from the destinations list.
	 * 
	 * @param currentFloor
	 */
	private void removeFloorFromDestinations(byte currentFloor) {
		if (elevatorDestinations.removeAll(Arrays.asList(currentFloor))) {
			System.out.println(currentFloor + " was removed from the destinations list");
			System.out.println("New destination list: " + elevatorDestinations.toString() + "\n");
		}
	}

	/**
	 * Add a elevator request to the list
	 * 
	 * @param request
	 */
	private void addElevatorRequest(ArrayList<Byte> request) {
		elevatorRequests.add(request);
		System.out.println("New request list: " + elevatorRequests.toString() + "\n");
	}

	/**
	 * Add a destination to the list
	 * 
	 * @param destination
	 */
	private void addDestination(byte destination) {
		// Send message to elevator so they can light up lamp
		elevatorDestinations.add(destination);
		System.out.println(destination + "was added to the destination list");
		System.out.println("New destination list: " + elevatorDestinations.toString() + "\n");
	}

	/**
	 * Remove the elevator request from the list
	 * 
	 * @param request
	 */
	private void removeRequest(ArrayList<Byte> request) {
		// TODO Is this correct?
		elevatorRequests.removeAll(Arrays.asList(request));
		System.out.println("New requests: " + elevatorRequests.toString());

	}

	/**
	 * Determine if the elevator should go up
	 * 
	 * @return True if the elevator should go up, false otherwise
	 */
	private boolean elevatorShouldGoUp() {
		int difference;
		int currentClosestDistance = Integer.MAX_VALUE;
		int closestFloor = 0;

		for (byte destination : elevatorDestinations) {
			difference = Math.abs(currentFloor - destination);
			if (difference < currentClosestDistance) {
				currentClosestDistance = difference;
				closestFloor = destination;
			}
		}
		for (ArrayList<Byte> request : elevatorRequests) {
			difference = Math.abs(currentFloor - request.get(0));
			if (difference < currentClosestDistance) {
				currentClosestDistance = difference;
				closestFloor = request.get(0);
			}
		}
		if (closestFloor > currentFloor) {
			return true;
		}
		return false;
	}

	/**
	 * Check to see if there are floors we need to go to above the current floor
	 * 
	 * @return True if we need to go up, false otherwise
	 */
	private boolean floorsToGoToAbove() {
		for (byte tempFloor : elevatorDestinations) {
			if (tempFloor > currentFloor) {
				return true;
			}
		}
		for (ArrayList<Byte> tempRequests : elevatorRequests) {
			if (tempRequests.get(0) > currentFloor) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check to see if there are floors we need to go to below the current floor
	 * 
	 * @return True if we need to go down, false otherwise
	 */
	private boolean floorsToGoToBelow() {
		for (byte tempFloor : elevatorDestinations) {
			if (tempFloor < currentFloor) {
				return true;
			}
		}
		for (ArrayList<Byte> tempRequests : elevatorRequests) {
			if (tempRequests.get(0) < currentFloor) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if the elevator has somewhere to go
	 * 
	 * @return True if has somewhere to go, False otherwise
	 */
	private boolean somewhereToGo() {
		if (elevatorDestinations.isEmpty() && elevatorRequests.isEmpty()) {
			return false;
		}
		return true;
	}
}