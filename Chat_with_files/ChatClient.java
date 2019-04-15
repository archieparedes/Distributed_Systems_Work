/**
 * @author Archie_Paredes
 * @version 1.1
 * Client Side Main
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient {    
    public static void main(String[] args){
        if(args.length < 1) throw new IllegalArgumentException("No arguments found"); // if address found in args
 
        try {
            // Example run: java ChatClient -l 6002 -p 6001
            String server_address;
            String port = args[3];
            String secondPort = args[1];

            try{    // if no second argument, default to local host
                server_address = args[4];
            } catch (Exception e){
                server_address = "localhost";
            }

            Socket client_s = new Socket(server_address, Integer.parseInt(port));
            DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
            DataInputStream input = new DataInputStream((client_s.getInputStream()));  
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            // send name to server
            String name = "";
            System.out.println("What is your name?");
            name = br.readLine();
            
            // folder to transfer files
            File f = new File("./"+name);
            if (f.exists() && f.isDirectory()) { // checks if file exists in directory
                System.out.println("Client Folder Exists");
            } else { // else makes a local folder for user
                new File("./"+name).mkdirs();
                System.out.println("Folder created for "+name);
            }
            output.writeUTF(name);
            output.flush();
            output.writeUTF(secondPort);
            System.out.println("Sending name and lport to server...");

			sendType send = new sendType(br, output, port); // what type of send does client want to do?
			Thread sendThread = new Thread(send);
			sendThread.start();

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