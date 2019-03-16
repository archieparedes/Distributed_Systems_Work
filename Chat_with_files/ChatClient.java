/**
 * @author Archie_Paredes
 * @version 1.0
 * Client Side
 */

import java.net.*;
import java.io.*;
import java.util.*;

class sendType implements Runnable {
	BufferedReader br;
	DataOutputStream output;
	String lPort;

	public sendType(BufferedReader br, DataOutputStream output, String p) {
		this.br = br;
		this.output = output;
		this.lPort = p;
	}

	@Override
	public void run() {
		try {
			String inputType = "";

			while (inputType != null) {
				System.out.println("Enter an option ('m', 'f', 'x'):\n (M)essage (send)\n (F)ile (request)\ne(X)it");
				String message = "";
				inputType = br.readLine(); // gets input from client
				if (inputType.equals("m")) {
					System.out.println("Enter your message:");
					message = br.readLine();
					if(inputType.isEmpty()){ // if the input is empty, close
						try{
							br.close();
							output.close();
							System.exit(0);
						} catch (Exception e1){System.exit(0);}
					}
					output.writeUTF(inputType);
					output.writeUTF(message);
					
				} else if (inputType.equals("f")) {
                    System.out.println("Who owns the file?");
                    String owner = br.readLine();
					System.out.println("Which file do you want?");
					String fileName = br.readLine();
					String ownerFile = fileName+"@"+owner;
					output.writeUTF(inputType);
					output.writeUTF(ownerFile);
				} else if (inputType.equals("x")) {
                    output.writeUTF(inputType);
					System.out.println("closing your sockets...goodbye");
					br.close();
					output.close();
					System.exit(0);
				} else {
					System.out.println("Invalid");
				}
			}
			output.writeUTF("");
		} catch (IOException e) {
			System.exit(0);
		}
	}
}


 
public class ChatClient {    
    public static void main(String[] args){
        if(args.length < 1) throw new IllegalArgumentException("No arguments found"); // if address found in args
 
        try {
            Socket client_s;
            DataOutputStream output;
            DataInputStream input;     
            BufferedReader br;
           
            String server_address;
            String port = args[3];
            String secondPort = args[1];
 
            try{    // if no second argument, default to local host
                server_address = args[4];
            } catch (Exception e){
                server_address = "localhost";
            }
			
            client_s = new Socket(server_address, Integer.parseInt(port));
            output = new DataOutputStream(client_s.getOutputStream());
            input = new DataInputStream((client_s.getInputStream()));  
            br = new BufferedReader(new InputStreamReader(System.in));
 
            // send name to server
            String name = "";
            System.out.println("What is your name?");
            name = br.readLine();
            output.writeUTF(name);
            output.flush();
            output.writeUTF(secondPort);
            System.out.println("Sending name and lport to server...");

        	//ftC fileClient = new ftC(secondPort); // connect client to file transfer port
			sendType send = new sendType(br, output, port); // what type of send does client want to do?
			Thread sendThread = new Thread(send);
			//Thread fileTransferClient = new Thread(fileClient);
			
			sendThread.start();
			//fileTransferClient.start();

            String message;
			try {
				message = input.readUTF(); // reads message from client
				while(message != null){ // if message is null, stop
					System.out.println(message); // prints message from client
					message = input.readUTF(); // reads next message
				}
			} catch (IOException e){
				try{ // if client disconnects, close everything
					input.close();
					client_s.shutdownOutput();
					client_s.close();
					System.exit(0); // exit
				} catch(Exception e1){System.exit(0);}
				System.exit(0);
			}

			client_s.close();
			System.exit(0);
        } catch (IOException e){System.exit(0);} // exit
    }
}