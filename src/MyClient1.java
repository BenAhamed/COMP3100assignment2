



import java.io.*;
import java.net.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;



import org.w3c.dom.Node;





public class MyClient1 {

	private Server[] servers = new Server[1];

	public static void main(String[] args) { 
		

		try { 
			
			// Intialises connection with server
			Socket client =new Socket("127.0.0.1", 50000);
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());
			DataInputStream din = new DataInputStream(client.getInputStream());

			// Writes "HELO" and sends to server all strings converted to bytes  before being sent
			String str1 = "HELO";
			dout.write(str1.getBytes()); 
			dout.flush(); 
			
			//Reads input from server 
			byte[] byteArray = new byte[din.available()];
			din.read(byteArray);
			String myString = new String(byteArray);


			 
			
			//Authenticates connection with device username 
            str1 = "AUTH benjamin";
			dout.write(str1.getBytes());
			dout.flush();   


			//These next few blocks of code are to read the server xml file on server information. 
			// Was going to use to sort servers but decided a different approach
			File XMLFile = new File("./ds-system.xml"); 
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(XMLFile); 
			doc.getDocumentElement().normalize();
			
				
			//System.out.println(	doc.getDocumentElement().getNodeName());
			
			NodeList nList = doc.getElementsByTagName("server");
			int[] coreCount = new int[nList.getLength()];
			String[] serverName = new String[nList.getLength()];

			// for( int temp =0; temp < nList.getLength(); temp++){ 

			// 	Node nNode = nList.item(temp);

			// 	if(nNode.getNodeType() == Node.ELEMENT_NODE) { 
			// 		Element eElement = (Element) nNode; 


			// 		//System.out.println(eElement.getAttribute("type")); 
			// 		//System.out.println(eElement.getAttribute("limit"));
			// 		//System.out.println(eElement.getAttribute("bootupTime"));
			// 		//System.out.println(eElement.getAttribute("hourlyRate"));
			// 		serverName[temp] = eElement.getAttribute("type");
			// 		// System.out.println(eElement.getAttribute("type"));
			// 		// System.out.println(serverName[temp]);
			// 		// System.out.println(eElement.getAttribute("coreCount"));
			// 		coreCount[temp] = Integer.parseInt(eElement.getAttribute("coreCount"));
					
			// 		//System.out.println(coreCount[temp]); 
			// 		//System.out.println(eElement.getAttribute("memory"));
			// 		//System.out.println(eElement.getAttribute("disk"));
			// 	}

			// }

			//ArrayList jobs used to store all jobs received by client 
			// Not really used for anything yet     
			ArrayList <String> jobs = new ArrayList<String>();
			//System.out.println(serverName[findBestServer(coreCount)]);
			// the boolean j value is ued for the while loop 
			//not sure if i needed to really initilaise it first but i did
			boolean j = true;

			//boolean jcpl is used in the case that when asking for a job the server replies with JCPL 
			// which then recquires the while loop to start again. 
		 
			boolean jcpl = false;

			// this is the the while loop that contains all the job handling code
			while( j = true){
				// jobID never really used not sure why exist
				String jobID;
            	byteArray = new byte[din.available()];
				din.read(byteArray);
             	myString = new String(byteArray);
            	System.out.println(myString);
				// if server replies with OK or .OK (this happens when the input wasn't fluhsed 
				// and cbf to fix) or jcpl is true meaning the server had sent details on 
				// a completed job. replies with REDY signalling for server to send job
				// resets jcpl to false
            	if(myString.equals("OK") || myString.equals(".OK") || jcpl == true) { 
               		str1 = "REDY";
			    	dout.write(str1.getBytes());
					dout.flush(); 
					jcpl = false;
					
			
				}
				// reads reply after REDY
				byteArray = new byte[din.available()];
				din.read(byteArray);
				myString = new String(byteArray);
				
				// if there are no jobs the server will reply with NONE in which case the client returns quit
				// and breaks the loop, reset jcpl to false
				if(myString.equals("NONE")){
					str1 = "QUIT";
					dout.write(str1.getBytes()); 
					dout.flush();
					jcpl = false;
					j=false;
					break;
				}

				//if The server ssends JCPL in reply to REDY jcpl is set to true and the
				// loop restarts
				else if(myString.contains("JCPL")){ 
					jcpl = true;
				}
				
				// else statement if everything checks out and the job schedulling can
				// continue
				else{
					jcpl = false;
						
					// adds details of the job to arraylist jobs. 
					// dont really use for anything atm
					jobs.add(myString);
					
					// this string contains the input of the server which is jobs details
					myString = new String(byteArray);

					System.out.println(myString);

					// splits up  job string myString and creates a string that will ask
					// the server what servers are capable of running the job
					String[] data = myString.split(" ");
					str1 = "GETS Capable " + data[4] + " "+ data[5] + " " + data[6];
					// jobID sotres ID of job to be used to schedule later.
					jobID = data[2];
					System.out.println(str1);
					dout.write(str1.getBytes());
					dout.flush(); 

					byteArray = new byte[din.available()];
					din.read(byteArray);
					myString = new String(byteArray); 
					myString = "";

					// replies with OK to server who would have sent DATA
					str1 = "OK";
					dout.write(str1.getBytes());
					dout.flush();
					

					// capable servers sent by server
					byteArray = new byte[din.available()];
					din.read(byteArray);
					myString = new String(byteArray); 
					
					// writes "OK" top the server after receving data
					dout.write(str1.getBytes());
					dout.flush();
					//printGap();
					// trims the input string by removing the first 10 chars aas they contain 
					// irrelvant data 
					//myString = myString.substring(10, myString.length());
					
					// prints a blank gap on the client console
					//printGap();


					// splits input string containing capable servers
					//based on every new line and stores in String array. 	
					String[] splitInput = myString.split("\\r?\\n");

					// Creates an arraylist that will store capable servers
					// and for loop iterates through the array and adds them
					// to the arrayList
					ArrayList<String> capableServers = new ArrayList<String>(); 
					Collections.addAll(capableServers, splitInput);
					
					
					// prints content of ArrayList
					//printArrayList(capableServers);

					//printGap(); 

					// runs arrayList through the function serverAvailable which filters
					// usable servers base on the functions criteria. 
					
					capableServers = serverAvailable(capableServers);

					System.out.println("Available Servers");

					//printArrayList(capableServers);

					// runs Arraylist through the function closestCPU which returns a string
					// containing the best Server to use for the job

					String bestServer = closestCPU(capableServers);
					//String bestServer = capableServers.get(0);
					// splits bestServer to be used in message
					String[] BSarray = bestServer.split(" ");
					

					printGap();

					System.out.println(bestServer);
					System.out.println(Arrays.toString(BSarray));
					// SCHD's the job using jobID prevously stored and values from bestServer
					String SCHD = "SCHD " + jobID + " " + BSarray[0] + " " + BSarray[1];
					dout.write(SCHD.getBytes());
					dout.flush();
					
				}	

			}
			 

           


			dout.close();
			client.close();
		
		}
		catch(Exception e) { System.out.println(e);}
	}

	public static void printArrayList(ArrayList list){ 
		for(int i =0; i<list.size();i++){ 
						
			System.out.println(list.get(i));
		}


	}


	// public static serverMessage(String msg){ 
	// 	String str1 = msg;
	// 	dout.write(str1.getBytes());
	// 	dout.flush();
	// }


	 public static ArrayList<String> serverAvailable(ArrayList<String> capableServers){ 

		// creates a temp arraylist to be used in the case that the first while loop returns 
		// an empty arrayList. contains the values within the input ArrayList
		ArrayList<String> temp1 = new ArrayList<String>();	
		for(int i = 0; i < capableServers.size(); i++){ 
			temp1.add(capableServers.get(i));
		}
		
		// for(int i =0; i<capableServers.size();i++){ 
		// 	String temp[] = capableServers.get(i).split(" ");
		// 	temp1.add(temp[2]);
		// }
		
		// Creates an Iterator object which stores the content of the input arrayList
		// this allows to accuratly iterate over an array while removing  content
		Iterator<String> itr = capableServers.iterator();

		while(itr.hasNext()) { 
			String capable = itr.next();
			String[] temp = capable.split(" ");
		//checks the status of each server and if they contain one of the following strings
		// that server is deleted from the list. 

			if(temp[2].equals("active") ||temp[2].equals("unavailable") || temp[2].equals("booting")){ 
				 		itr.remove();
			}
		}

		// if all servers are used then the arrayList from aboves filter will be of length zero
		// in which case using the temp arraylist we iterate of it again with looser conditions
		// then before
		if(capableServers.size() == 0){ 
			Iterator<String> itr1 = temp1.iterator();
			while(itr1.hasNext()) { 
				String capable = itr1.next();
				String[] temp = capable.split(" ");

				if(temp[2].equals("unavailable")){ 
					itr1.remove();
				}
			}
			return temp1;
		}

		else { 
			return capableServers;
		}
				
		
		// Iterator 

		// for(int i =0; i<capableServers.size();i++){ 
		// 	String temp[] = temp1.get(i).split(" ");

		// 	if(temp[2].equals("active") || temp[2].equals("unavailable") || temp[2].equals("booting")){ 
		// 		capableServers.remove(i);
		// 	} 

		// }
		// return capableServers;

	}
	// function that returns a string containing best server
	// I determine the best server by first making sure that the server doesn't have 0 cores available 
	// this is to ensure that incase a server is being used and the cores reported are 0 avaiblabe
	// that it is not able to be choosen.
	// after that the server whith the lowest possible CPU cores is then choosen as it would have
	// the closest value in relation to the number of cores recquired to do the job. 
	// Trying to get best fit. 
	// teh function then will return a String containing that server info which contains the smallest value;


	public static String closestCPU(ArrayList<String> capableServers){
		// used in similar way as serverAvailable function.
		ArrayList<String> temp1 = new ArrayList<String>();
		for(int i = 0; i < capableServers.size(); i++){ 
			temp1.add(capableServers.get(i));
		}
		// if Arraylist size is 1 then only one option
		if(capableServers.size() <= 1){ 
			return capableServers.get(0);
		}
		
		else {
			// creates arrayList that will store the CPU values of each server
			// based on the fact they don't contain values of 0;
			ArrayList <Integer> serverCPU = new ArrayList<Integer>();
			for(int i = 0; i < capableServers.size(); i++){ 				
				String temp[] = capableServers.get(i).split(" ");
				if(temp[4].equals("0") == false){
					serverCPU.add(Integer.parseInt(temp[4]));
				}
			}
			// if the serverCPU list size is 0 then the above array is run but without 
			// teh conditions previously stated; 
			// it will also then return a the server containing the largest cpu value
			if(serverCPU.size() == 0){ 
				ArrayList <Integer> serverCPU1 = new ArrayList<Integer>();
				for(int i = 0; i < temp1.size(); i++){ 				
					String temp[] = temp1.get(i).split(" ");
					serverCPU1.add(Integer.parseInt(temp[4]));
				}	
				int maxIndex = serverCPU1.indexOf(Collections.max(serverCPU1));
				return temp1.get(maxIndex);
			}

			//if serverCPU size is greater then 0 then the method will return the server
			// with the smallest CPU size.
			else {		

				int minIndex = serverCPU.indexOf(Collections.min(serverCPU));
				return capableServers.get(minIndex) ;
			}
		}
		
		

	}




	public static void printGap(){ 
		for(int i = 0; i<3; i++){ 
			System.out.println(" ");
		}
	}


// not used, for xml file reading;
	public static int findBestServer(int[] array) { 
		int max =0;
		int idx =0;

		for(int i = 0; i< array.length; i++){ 
			if(array[i]>=max){ 
				max = array[i];
				idx = i;
			}
		}
		return idx;
	}

}

 
