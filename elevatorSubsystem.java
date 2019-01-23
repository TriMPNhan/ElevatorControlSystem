package groupProject;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * SYSC 3303 Elevator Group Project
 * Client.java
 * @ author Samy Ibrahim 
 * @ student# 101037927
 * @ version 1
 * 
 * The elevator subsystem consists of the buttons and lamps inside of the elevator used to select floors and indicate the
 * floors selected, and to indicate the location of the elevator itself. The elevator subsystem is also used to operate the
 * motor and to open and close the doors. Each elevator has its own elevator subsystem. 
 * 
 * For the purpose of this project, the elevator subsystem listens for packets from the scheduler to control the motor
 * and to open the doors. The elevator subsystem also has to monitor the floor subsystem for destination requests
 * (button presses inside of the elevator car, rather than button presses at the floors) from the input file. Button presses
 * are to be rerouted to the scheduler system. Lamp (floor indications) from button pushes do not have to originate
 * from the scheduler. Rather, when the elevator subsystem detects a button request, it can then light the
 * corresponding lamp. When the elevator reaches a floor, the scheduling subsystem signals the elevator subsystem to
 * turn the lamp of
 * 
 * PORT Numbers:
 * 	Receiving from Scheduler on Port xxxx
 * 	Sending to ? on Port xxxx
 * 
 * MESSAGE ENCODING:
 * 	Bytes[x,y] = 
 * 
 * (Currently the class only carries simple server features).
 * Last Edited January 22,2019
 * 
 */
public class elevatorSubsystem {
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	
	//The current floor the elevator is on
	private int elevatorNumber = 1;
	private int currentFloor = 1;
	private Button[] allButtons = {new Button("Emergency"), new Button("Close Door"), new Button("Open Door"),
			new Button("1"), new Button("2"), new Button("3"), new Button("4")}; // and so on
	

	public elevatorSubsystem() {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send UDP Datagram packets.
			sendSocket = new DatagramSocket();

			// Construct a datagram socket and bind it to port 5000
			// on the local host machine. This socket will be used to
			// receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(4789);

			// to test socket timeout (2 seconds)
			// receiveSocket.setSoTimeout(2000);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public int getElevatorNumber() {
		return this.elevatorNumber;
	}
	public int getCurrentFloor() {
		return this.currentFloor;
	}
	public void display() {
		System.out.println("Elevator " + this.getElevatorNumber());
		System.out.println("Floor # " + this.getCurrentFloor());
		//ideally we want lights lighting up by whatever floor it is, not sure how we wil implement that yet
	}

	public void doSomething(String str) {
		this.display();
		if(str.equals("open door")) {
			// open door
		}
		if(str.equals("close door")) {
			// close door
		}
		if(str.equals("go up")) {
			// go up x floors, etc...
		}
		this.display();
	}
	
	
	// Method to validate the form of the array of bytes received from the Scheduler
	public String validPacket(byte[] data) {
		//simple example
		for(int s=2; s<data.length;s++) {
			// Eclipse uses ISO/IEC 8859-1, all English characters are encoded as byte values less than 127.
			if(data[s] > 127 && data[s]<0) {
				return "invalid";
			}
		}
		if(data[data.length-1]==0) { // if the last number in the array is a 0 and there are more that 4 zeros in the array of bytes.
			if(data[0] == 0 && data[1]==1) { // if the first two bytes are 01, then it is a read request.
				return "read";
			}
			else if(data[0]==0 && data[1]==2) { // if the first two bytes are 02, then it is a write request.
				return "write";
			}
		}
		return "invalid"; // anything else is an invalid request.
	}
	
	// Validates that the text sent to the Elevator from the Scheduler is a valid "command"
	public boolean validRequest(String str) {
	
		// List of Strings (data received) split at a each comma (CSV) to store the different items needed
		// messageReceived[0] = WHAT IT WILL BE
		// messageReceived[1] = WHAT IT WILL BE
		// messageReceived[2] = WHAT IT WILL BE...
		List<String> messageReceived = Arrays.asList(str.split(","));
		
		//simple example
		if(str.equals("open door")) {
			return true;
		}else if(str.equals("go up")) { //no clue for now just filler to show
			return true;
		}
		return false;
	}

	public void exchangeData() {
		// Construct a DatagramPacket for receiving packets up
		// to 100 bytes long (the length of the byte array).
		// Receiving Data from the Scheduler to Control the Motor and Open the doors
		this.display();
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		System.out.println("Elevater: Waiting for Packet.\n");

		// Block until a datagram packet is received from receiveSocket.
		try {
			System.out.println("Waiting..."); // so we know we're waiting
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.print("IO Exception: likely:");
			System.out.println("Receive Socket Timed Out.\n" + e);
			e.printStackTrace();
			System.exit(1);
		}

		// Process the received datagram.
		System.out.println("Elevator: Packet received:");
		System.out.println("From host: " + receivePacket.getAddress());
		System.out.println("Host port: " + receivePacket.getPort());
		int len = receivePacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");

		// Form a String from the byte array.
		String received = new String(data, 0, len);
		System.out.println(received + "\n");

		// Slow things down (wait 5 seconds)
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// if it is an invalid packet, then exit
		if(this.validPacket(data).equals("invalid")) {
			System.out.println("Invalid Packet Format");
			System.exit(1); // invalid
		}
		// if it is an invalid request, then exit
		if(this.validRequest(received) == false) {
			System.out.println("Invalid Request Format");
			System.exit(1); // invalid
		}
		

		// Create a new datagram packet containing the string received from the client.

		// Construct a datagram packet that is to be sent to a specified port
		// on a specified host.
		// The arguments are:
		// data - the packet data (a byte array). This is the packet data
		// that was received from the client.
		// receivePacket.getLength() - the length of the packet data.
		// Since we are echoing the received packet, this is the length
		// of the received packet's data.
		// This value is <= data.length (the length of the byte array).
		// receivePacket.getAddress() - the Internet address of the
		// destination host. Since we want to send a packet back to the
		// client, we extract the address of the machine where the
		// client is running from the datagram that was sent to us by
		// the client.
		// receivePacket.getPort() - the destination port number on the
		// destination host where the client is running. The client
		// sends and receives datagrams through the same socket/port,
		// so we extract the port that the client used to send us the
		// datagram, and use that as the destination port for the echoed
		// packet.

		sendPacket = new DatagramPacket(data, receivePacket.getLength(), receivePacket.getAddress(),
				receivePacket.getPort());

		System.out.println("Elevator: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		len = sendPacket.getLength();
		System.out.println("Length: " + len);
		System.out.print("Containing: ");
		System.out.println(new String(sendPacket.getData(), 0, len));
		// or (as we should be sending back the same thing)
		// System.out.println(received);

		// Send the datagram packet to the client via the send socket.
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Server: packet sent");

		// We're finished, so close the sockets.
		sendSocket.close();
		receiveSocket.close();
	}

	public static void main(String args[]) {
		elevatorSubsystem elevator = new elevatorSubsystem();
		for(;;) {
			elevator.exchangeData();
		}
		
	}
}
