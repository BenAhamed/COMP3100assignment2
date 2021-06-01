import java.net.*;
import java.util.*;
import java.util.stream.Stream;
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
            out = new DataOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            

			//out = new DataOutputStream(socket.getOutputStream()); 
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
        //System.out.println(inputString);
        allToLargest();
		quit();
	}


    public void allToLargest (){
        if (inputString.equals("NONE")) {
			quit();
		} else {
			while (!completed) {
				if (inputString.equals("OK") || inputString.equals(".OK") || inputString.equals(".")|| inputString.equals(" .")) {
					write("REDY");
                    //System.out.println("Sent REDY");
					inputString = Read();
                    if(inputString.equals("NONE")){ 
                        break;
                    }
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

                
               write("OK");
               
               String[] jobSections = inputString.split("\\s+");
              // while (x=false){
				 if(jobSections.length <7){ 
                   // 
                    inputString = Read();
                    System.out.println(" ???? " + inputString + "     ????"); 
                 }
               
                
                jobSections = inputString.split("\\s+");
                if(jobSections[0].equals("JCPL") == false){
              
               
                String gets = "GETS Capable " + jobSections[4] + " " + jobSections[5] + " " + jobSections[6];
              
                write(gets);
                String data = Read();
                String[] splitData = data.split(" ");
                int numServers = Integer.parseInt(splitData[1]);
                write("OK");
                String[] servers = new String[numServers];                            
                String set = "";
            String l= "";
            String worstCase = "";
                for(int i =0; i<numServers; i++) {
                    
                 
                    l = Read();
                   
               
                if(l == null){ 
                	break;
                	}
                if(getsCapable(l)==true && set.equals("")){
                    set = l;
                    try {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    } catch (IOException e) {

                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                   
                }
                if(i == numServers-1 && set.equals("")){ 
                	set = l;
                }
                
               
            
                System.out.println(l);
           }
           write("OK");
           System.out.println(set);
                String[] serverInfo = set.split(" "); 
                String serverID = serverInfo[1]; 
                String serverName = serverInfo[0]; 

				String jobnum = jobSections[2];
				String scheduleMessage = "SCHD " + jobnum + " " + serverName + " " + serverID;
				//System.out.println("Received " + scheduleMessage);
				write(scheduleMessage);
                
                //write("OK");
                //System.out.println("JOB SENT: SCHD " + count + " " + servers[largestServer].type + " " + "0");
                inputString = Read();
                //System.out.println("Received " + inputString);
                }
			}
		}
    }


    public Boolean getsCapable(String string){ 
        //String[] splitInput = string.split(" ");
       
       // List <String> array = Arrays.asList(splitInput); 
        //ArrayList <String> temp = new ArrayList<String>();
        //Iterator<String> it = array.iterator();
        int j = 0;
        
       // while(j>0){
      
            
            String[] server = string.split(" ");
          
               
             //System.out.println(server[3]);
            if(server[2].equals("active") ==false && server[2].equals("booting")==false && server[2].equals("unavailable")==false){ 
                return true;
            }
           
        
          else{ 
              return false;
          }  
           
         

        
        
        
    }

        public void write(String text) {
            try {
                out.write((text + "\n").getBytes());
               // System.out.print("SENT: " + text);
                out.flush();
            } catch (IOException i) {
                System.out.println("ERR: " + i);
            }
        }
    
        public String Read() {
            String text = "";
            try {
                text = in.readLine();
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
