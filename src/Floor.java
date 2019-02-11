import java.util.ArrayList;

public class Floor implements Runnable {
	private FloorSubsystem controller; // The FloorSubsystem that this object belongs to

	private int floorNum; // The number of this floor
	private int numElevatorShafts; // The number of elevator shafts on this floor

	// Information for the arrival lamps and their directions
	// There is one set of arrival lamsp and directions for each elevator on the floor
	private ArrayList<Integer> elevatorLocation;
	private ArrayList<UtilityInformation.LampState> arrivalLamp;
	private ArrayList<UtilityInformation.ElevatorDirection> arrivalLampDir;

	// Information for the buttons the floor
	private UtilityInformation.ButtonState upButton;
	private UtilityInformation.ButtonState downButton;
	
	private ArrayList<Integer[]> serviceRequests;
	
	private long startTime;

	/**
	 * Floor
	 * 
	 * Constructor
	 * 
	 * Creates a new Floor object. Initializes all of the buttons and lamps.
	 * 
	 * @param controller        FloorSubsystem that this Floor belongs to
	 * @param floorNum          This Floor's number
	 * @param numElevatorShafts Number of elevator shafts on the Floor
	 * 
	 * @return None
	 */
	public Floor(FloorSubsystem controller, int floorNum, int numElevatorShafts) {
	    serviceRequests = new ArrayList<Integer[]>();
	    
		elevatorLocation = new ArrayList<Integer>();
		arrivalLamp = new ArrayList<UtilityInformation.LampState>();
		arrivalLampDir = new ArrayList<UtilityInformation.ElevatorDirection>();

		// Save all of the information
		this.controller = controller;

		this.floorNum = floorNum;
		this.setNumElevatorShafts(numElevatorShafts);

		// Configure lamps and buttons
		for (int i = 0; i < numElevatorShafts; i++) {
			elevatorLocation.add(0);
			arrivalLamp.add(UtilityInformation.LampState.OFF);
			arrivalLampDir.add(UtilityInformation.ElevatorDirection.STATIONARY);
		}

		upButton = UtilityInformation.ButtonState.UNPRESSED;
		downButton = UtilityInformation.ButtonState.UNPRESSED;
	}

	/**
	 * createElevatorRequest
	 * 
	 * Tells the FloorSubsystem that a new request was made at this Floor. Sets the
	 * corresponding lamp and button values.
	 * 
	 * @param timeOfReq    Time at which the request was made in ms
	 * @param direction    Direction that the user wants to travel
	 * @param endFloor     Floor that user wants to travel to
	 * 
	 * @return void
	 */
	public void createElevatorRequest(int timeOfReq, UtilityInformation.ElevatorDirection direction, int endFloor) {
		// Set the button and lamp states
		if (direction == UtilityInformation.ElevatorDirection.UP) {
			upButton = UtilityInformation.ButtonState.PRESSED;
		} else if (direction == UtilityInformation.ElevatorDirection.DOWN) {
			downButton = UtilityInformation.ButtonState.PRESSED;
		}
		
		Integer[] request = new Integer[5];
		request[0] = timeOfReq;
		request[1] = this.getFloorNumber();
		request[2] = direction.ordinal();
		request[3] = endFloor;
		request[4] = -1;

		serviceRequests.add(request);
	}
	
	/**
     * updateElevatorLocation
     * 
     * Updates the lamps and buttons depending on the given
     * elevator's direction and location.
     * 
     * @param elevatorShaftNum	Elevator that is moving
     * @param floorNum	Floor number the elevator is at
     * @param direction	Direction of the elevator
     * 
     * @return	void
     */
    public void updateElevatorLocation(int elevatorShaftNum, 
							    	   int floorNum, 
							    	   UtilityInformation.ElevatorDirection direction) {
    	// If the elevator is at this floor
    	// Set the arrival lamp and
    	// check if this is the floor that the elevator shaft is stopping at
    	if ((floorNum == this.getFloorNumber()) && 
    		(direction == UtilityInformation.ElevatorDirection.STATIONARY)) {
    		// Turn off up/down buttons if the elevator is stopping at this floor
			arrivalLamp.set(elevatorShaftNum - 1, UtilityInformation.LampState.ON);
			downButton = UtilityInformation.ButtonState.UNPRESSED;
			upButton = UtilityInformation.ButtonState.UNPRESSED;
    	} else {
    		arrivalLamp.set(elevatorShaftNum - 1, UtilityInformation.LampState.OFF);
    	}
    	
    	// Update the elevator location and direction
    	elevatorLocation.set(elevatorShaftNum - 1, floorNum);
    	arrivalLampDir.set(elevatorShaftNum - 1, direction);
    }

	/**
	 * getFloorNumber
	 * 
	 * Returns the number of this floor.
	 * 
	 * @param None
	 * 
	 * @return int Floor number of this Floor
	 */
	public int getFloorNumber() {
		return (this.floorNum);
	}

	/**
	 * getNumElevatorShafts
	 * 
	 * Return the number of elevator shafts at this floor.
	 * 
	 * @param None
	 * 
	 * @return int Number of elevator shafts on this floor
	 */
	public int getNumElevatorShafts() {
		return numElevatorShafts;
	}

	/**
	 * setNumElevatorShafts
	 * 
	 * Set the number of elevator shafts on this floor
	 * 
	 * @param numElevatorShafts The new number of elevator shafts
	 * 
	 * @return void
	 */
	public void setNumElevatorShafts(int numElevatorShafts) {
		this.numElevatorShafts = numElevatorShafts;
		
		// Update the array lists which are dependent on the number of elevators
		if (elevatorLocation.size() > numElevatorShafts) {
		    // If size is less than the number of elevators,
		    // add values to the array list
			while (elevatorLocation.size() > numElevatorShafts) {
				elevatorLocation.remove(elevatorLocation.size() - 1);
			}
		} else {
		    // If size is greater than the number of elevators,
            // remove values from teh array list
			while (elevatorLocation.size() < numElevatorShafts) {
				elevatorLocation.add(0);
			}
		}
		
		// Repeat the same as above for the next array list
		if (arrivalLamp.size() > numElevatorShafts) {
			while (arrivalLamp.size() > numElevatorShafts) {
				arrivalLamp.remove(arrivalLamp.size() - 1);
			}
		} else {
			while (arrivalLamp.size() < numElevatorShafts) {
				arrivalLamp.add(UtilityInformation.LampState.OFF);
			}
		}
		
		// Repeat the same as above for the next array list
		if (arrivalLampDir.size() > numElevatorShafts) {
			while (arrivalLampDir.size() > numElevatorShafts) {
				arrivalLampDir.remove(arrivalLampDir.size() - 1);
			}
		} else {
			while (arrivalLampDir.size() < numElevatorShafts) {
				arrivalLampDir.add(UtilityInformation.ElevatorDirection.STATIONARY);
			}
		}
	}

	/**
	 * getArrivalLamp
	 * 
	 * Returns the state of the arrival lamp
	 * 
	 * @param elevatorShaftNum The elevator shaft that the arrival lamp belongs to
	 * 
	 * @return lampState The state of the arrival lamp
	 */
	public UtilityInformation.LampState getArrivalLamp(int elevatorShaftNum) {
		return arrivalLamp.get(elevatorShaftNum);
	}

	/**
	 * setArrivalLamp
	 * 
	 * Set the state of the arrival lamp
	 * 
	 * @param  newLampState        The new lamp state for the arrival lamp
	 * @param  elevatorShaftNum    The elevator shaft that the arrival lamp belongs to
	 * 
	 * @return void
	 */
	public void setArrivalLamp(UtilityInformation.LampState newLampState, int elevatorShaftNum) {
		arrivalLamp.set(elevatorShaftNum - 1, newLampState);
	}

	/**
	 * getArrivalLampDir
	 * 
	 * Returns the direction set on the arrivalLamp
	 * 
	 * @param elevatorShaftNum The elevator shaft that the arrival lamp belongs to
	 * 
	 * @return FloorSubsystem.Direction Direction currently being displayed on the
	 *         lamp
	 */
	public UtilityInformation.ElevatorDirection getArrivalLampDir(int elevatorShaftNum) {
		return arrivalLampDir.get(elevatorShaftNum);
	}

	/**
	 * setArrivalLampDir
	 * 
	 * Sets the current direction displayed on the lamp.
	 * 
	 * @param  newDirection        The new direction for the arrival lamp
	 * @param  elevatorShaftNum    The elevator shaft that the lamp belongs to
	 * 
	 * @return void
	 */
	public void setArrivalLampDir(UtilityInformation.ElevatorDirection newDirection, int elevatorShaftNum) {
		arrivalLampDir.set(elevatorShaftNum - 1, newDirection);
	}
	
	/**
	 * getUpButton
	 * 
	 * Returns the state of the upButton.
	 * 
	 * @param	None
	 * 
	 * @return UtilityInformation.ButtonState	State of the upButton
	 */
	public UtilityInformation.ButtonState getUpButton() {
		return upButton;
	}

	/**
	 * setUpButton
	 * 
	 * Sets the state of the upButton to the given state.
	 * 
	 * @param newState The new state of the upButton
	 * 
	 * @return	void
	 */
	public void setUpButton(UtilityInformation.ButtonState newState) {
		this.upButton = newState;
	}

	/**
	 * getDownButton
	 * 
	 * Returns the state of the downButton.
	 * 
	 * @param	None
	 * 
	 * @return UtilityInformation.ButtonState	State of the down button
	 */
	public UtilityInformation.ButtonState getDownButton() {
		return downButton;
	}

	/**
	 * setDownButton
	 * 
	 * Sets the state of the downButton to the given state.
	 * 
	 * @param newState The new state of the downButton
	 * 
	 * @return	void
	 */
	public void setDownButton(UtilityInformation.ButtonState newState) {
		this.downButton = newState;
	}
	
	/**
	 * toString
	 * 
	 * Overridden
	 * 
	 * Returns a String describing this Floor object
	 * 
	 * @param  None
	 * 
	 * @return String  String describing this Floor object
	 */
	public String toString() {	    
	    String toReturn = "";
	    
	    toReturn += String.format("Floor Number: %d", floorNum);
	    
	    // Add the information about the lamps
	    for (int i = 0; i < numElevatorShafts; i++) {
	        toReturn += String.format("; Elevator: %d", i);
	        toReturn += String.format(", Floor: %d", elevatorLocation.get(i));
            toReturn += String.format(", Direction: %s", arrivalLampDir.get(i).toString());
            toReturn += String.format(", ArrivalLamp: %s", arrivalLamp.get(i).toString());
	    }
	    
        toReturn += String.format("; Up Button: %s", upButton.toString());
       
        toReturn += String.format("; Down Button: %s", downButton.toString());
	    
        return(toReturn);
	    
	}
	
	public synchronized void sendRequest() {
	    while ((serviceRequests.size() == 0) ||
	            (System.currentTimeMillis() - startTime < serviceRequests.get(0)[0])) {
	    }
	    
	    Integer[] request = serviceRequests.get(0);
	    
	    byte[] signal = new byte[4];
	    
	    signal[0] = (byte)(int)request[1];
	    signal[1] = (byte)(int)request[2];
	    signal[2] = (byte)(int)request[3];
	    signal[3] = (byte)(int)request[4];
	    
	    controller.sendElevatorRequest(request[1], 
                        	           request[3], 
                        	           UtilityInformation.ElevatorDirection.values()[request[2]]);
	    
	    serviceRequests.remove(0);
	}

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        
        while (true) {
            this.sendRequest();
        }
    }
}
