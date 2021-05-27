import java.net.*;
import java.util.ArrayList;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TheClient {

	// Create global variables for the socket, and the input and output, which we will need to write and Read messages
	// between server and client
	private Socket socket = null;
	private BufferedReader in = null;
	private DataOutputStream out = null;
    private DataInputStream din = null;
	//public Server[] servers;
	private int largestServerIndex = 0;
	private String inputString;
	private Boolean completed = false;

	// Constructor for our client class: we connect the socket to the address 127.0.0.1 and to the port 50000, as
	// provided by the server, and we initialize the input variable (in) and the output (out)
	// in will be used for Reading the messages sent by the server, while out will be used for writing messages 
	// to the server
	public TheClient() {
		try {
			socket = new Socket("localhost", 50000);
            din = new DataInputStream(socket.getInputStream());
			//in = new BufferedReader(new DataInputStream(socket.getInputStream()));
            

			out = new DataOutputStream(socket.getOutputStream()); 
		} catch (UnknownHostException i) {
			System.out.println("Error: " + i);
		} catch (IOException i) {
			System.out.println("Error: " + i);
		}
	}
    
	// We instantiate one client object in main, and we need some method that will start communication with the server,
	// which I called start().
	public static void main(String args[]) {
		TheClient client = new TheClient();
		client.start();
	}

	public void start() {
		write("HELO");
        //System.out.println("Sent HELLO");
		inputString = Read();
        //System.out.println("Received " + inputString);
		write("AUTH " + System.getProperty("user.name"));
        //System.out.println("Sent Auth " + System.getProperty("user.name"));
		inputString = Read();
        //System.out.println("Received " + inputString);
		
		
		write("REDY");
        //System.out.println("Sent REDY");
		inputString = Read();
        //System.out.println("Received " + inputString);
        System.out.println(inputString);
        allToLargest();
		quit();
	}


    public void allToLargest (){
        if (inputString.equals("NONE")) {
			quit();
		} else {
			while (!completed) {
				if (inputString.equals("OK") || inputString.equals(".OK") || inputString.equals(".")) {
					write("REDY");
                    //System.out.println("Sent REDY");
					inputString = Read();
                    //System.out.println("Received " + inputString);
				}
                String [] splitMessage = inputString.split("\\s+");
                String firstWord = splitMessage[0];
                while (firstWord.equals("JCPL") || firstWord.equals("RESF") || firstWord.equals("RESR")) {
                    write("REDY");
                    //System.out.println("Sent REDY");
					inputString = Read();
                    //System.out.println("Received " + inputString);

                    splitMessage = inputString.split("\\s+");
                    firstWord = splitMessage[0];
                }
				if (firstWord.equals("NONE")) {
					completed = true;
					break;
				}

                
               // write("OK");
				String[] jobSections = inputString.split("\\s+"); 
                String gets = "GETS Capable " + jobSections[4] + " " + jobSections[5] + " " + jobSections[6];
                System.out.println(gets);
                write(gets);
                Read();
                write("OK");
                
                String a = Read(); 
                System.out.println(a);
                String server = getsCapable(a);
                write("OK");
                Read();
                
                String[] serverInfo = server.split(" "); 
                String serverID = serverInfo[1]; 
                String serverName = serverInfo[0]; 

				String jobnum = jobSections[2];
				String scheduleMessage = "SCHD " + jobnum + " " + serverName + " " + serverID;
				write(scheduleMessage);
                
               // write("OK");
                //System.out.println("JOB SENT: SCHD " + count + " " + servers[largestServer].type + " " + "0");
                inputString = Read();
                //System.out.println("Received " + inputString);
			}
		}
    }


    public String getsCapable(String string){ 
        String[] splitInput = string.split("\\r?\\n");
        ArrayList <String> array = new ArrayList<String>();
        for(int i =0; i<splitInput.length; i++) { 
            System.out.println(splitInput[i]);
            String[] server = splitInput[i].split(" ");
            if(server[3].contains("active") || server[3].contains("booting") || server[3].contains("unavailable")){ 
                i = i;
            }
            
            else{ 
                array.add(splitInput[i]);
            }
        } 

        if(array.isEmpty()){ 
            for(int i =0; i<splitInput.length; i++) { 
                String[] server = splitInput[i].split(" ");
                if(server[3].equals("unavailable") == false);
                    array.add(splitInput[i]); 
            }       
        }
        
        return array.get(0);
    }
    

    // public void ReadFile(File file) {
	// 	try {
			
	// 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// 		DocumentBuilder builder = factory.newDocumentBuilder();
	// 		Document systemDocument = builder.parse(file);
	// 		systemDocument.getDocumentElement().normalize();
			
	// 		NodeList serverNodeList = systemDocument.getElementsByTagName("server");
	// 		servers = new Server[serverNodeList.getLength()];
	// 		for (int i = 0; i < serverNodeList.getLength(); i++) {
	// 			Element server = (Element) serverNodeList.item(i);
	// 			String t = server.getAttribute("type");
	// 			int c = Integer.parseInt(server.getAttribute("coreCount"));
	// 			Server temp = new Server(i, t, c);
	// 			servers[i] = temp;
	// 		}
	// 		largestServerIndex = 0;
	// 	} catch (Exception i) {
	// 		i.printStackTrace();
	// 	}

	// }
        public void write(String text) {
            try {
                out.write((text).getBytes());
                // System.out.print("SENT: " + text);
                out.flush();
            } catch (IOException i) {
                System.out.println("ERR: " + i);
            }
        }
    
        public String Read() {
            String text = "";
            try {
                byte[] byteArray = new byte[din.available()];
		din.read(byteArray);
                text = new String(byteArray); 
                // System.out.print("RCVD: " + text);
                inputString = text;
            } catch (IOException i) {
                System.out.println("ERR: " + i);
            }
            return text;
        }
    
        public void quit() {
            try {
                write("QUIT");
                //System.out.println("Sent QUIT");
                inputString = Read();
                //System.out.println("Received" + inputString);
                if (inputString.equals("QUIT")) {
                    in.close();
                    out.close();
                    socket.close();
                }
            } catch (IOException i) {
                System.out.println("ERR: " + i);
            }
        }
    
    
    
}
