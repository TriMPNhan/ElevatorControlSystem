

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FloorSubsystemTests {
    private TestHost host;
    private String filePath;
    private PrintWriter writer;
    private FloorSubsystem testController;
    private int numFloors;
    private int numElevators;
    
    private ArrayList<Integer[]> reqs;
    
    @BeforeEach
    void setUp() throws Exception {
    	numFloors = 11;
    	numElevators = 1;
        host = new TestHost(1);
        
        String filePath = "test.txt";
        PrintWriter writer = null;
        
        // Create the file
        try {
            writer = new PrintWriter(filePath, StandardCharsets.UTF_8);
        } catch (IOException e1) {
            System.out.println("Error: Unable to write to test text file.");
            e1.printStackTrace();
            System.exit(1);
        }
        
        // Write the requests to the text file
        writer.println("14:05:15.0 2 Up 4");
        writer.println("03:14:15.9 7 down 0");
        writer.println("22:00:59.9 3 uP 8");
        writer.println("00:56:42.7 8 UP 9");
        writer.println("03:34:19.2 6 down 1");
        
     // Parse the created file
        FloorSubsystem testController = new FloorSubsystem(numFloors, numElevators);
        
        testController.parseInputFile(filePath);
        reqs = testController.getRequests();
        
        Thread t = new Thread(host);
        t.start();
        
    }
    
    @AfterEach
    void tearDown() throws Exception {
    	testController.teardown();
    	host.teardown();
        host = null;
        writer.close();
        
        
        testController = null;
    }

    /**
     * testSampleInput
     * 
     * Tests the FloorSubsystem with a created input file.
     * Output needs to be checked to ensure requests are created properly.
     * 
     * @input   None
     * 
     * @return  None
     */
    @Test
    void testSampleInput() {
        
        
        // Print the requests
        for (Integer[] req : reqs) {
            System.out.println(Arrays.toString(req) + "\n");
        }
        
        // Delete the test text file
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.out.println("Error: Unable to delete test text file.");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Test
    void testConfigMessage() {
        
        host.setExpectedNumMessages(1);
        
                
        testController.sendConfigurationSignal(numElevators, numFloors);
        
        
    }
    
    @Test
    void testElevatorRequestTiming() {
    	Integer[] req1 = reqs.get(1);
    	Integer[] req2 = reqs.get(2);
    	
    	int msReq1 = req1[0] * 60 * 60 * 1000 + req1[1] * 60 * 1000 + req1[2] * 1000;
    	int msReq2 = req2[0] * 60 * 60 * 1000 + req2[1] * 60 * 1000 + req2[2] * 1000;
    	
    	assertEquals(msReq2 - msReq1, 1444000);
    	
    }

}
