import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient {    
    
    public static void main(String[] args){
        if(args.length < 1) throw new IllegalArgumentException("No arguments found"); // if address found in args

        try {
            Socket client_s;
            DataOutputStream output;
            DataInputStream input;		
            BufferedReader br;
            
            String server_address;
            String port = args[0];

            try{    // if no second argument, default to local host
				server_address = args[1]; 
			} catch (Exception e){
				server_address = "localhost";
			}

            client_s = new Socket(server_address, Integer.parseInt(port));
            output = new DataOutputStream(client_s.getOutputStream());
            input = new DataInputStream((client_s.getInputStream()));	
            br = new BufferedReader(new InputStreamReader(System.in));

            // send name to server
            Thread send_name = new Thread(new Runnable(){
                String name;
                @Override
                public void run() {
                    try{
                        System.out.println("What is your name?");
                        name = br.readLine();
                        output.writeUTF(name);
                        System.out.println("Sending name to server...");
                    } catch (Exception e){}
                }
            });
            send_name.start();

            // sending messages to server
            Thread send = new Thread(new Runnable(){
                String message;	
                @Override
                public void run() {
                    while(true){
                        try{
                            message = br.readLine(); // gets input from client

                            if(message.isEmpty()){ // if the input is empty, close and turn off socket
                                try{
                                    br.close();
                                    input.close();
                                    client_s.shutdownOutput();
                                    client_s.close();
                                    System.exit(0);
                                } catch (Exception e1){System.exit(0);}
                                System.exit(0); // exit
                            }

                            output.writeUTF(message); // write message to other clients
                            output.flush();
                        } catch (Exception e) {System.exit(0);} // exit
                    }
                }
            });
            

            // receiving messages from clients
            Thread rec = new Thread(new Runnable(){
                String message;

                @Override
                public void run() {
                    try {
                        message = input.readUTF(); // reads message
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
                        } catch(Exception e1){System.exit(0);} // exit
                        System.exit(0);
                    }
                }
            });
            
            // start threads
            send.start();
            rec.start();
        } catch (IOException e){System.exit(0);} // exit
    }
}

// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.IOException;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.ArrayList;

// public class ChatClient {
// 	public static void main(String[] args) {
// 		int port = 0;

// 		if (args.length != 1) // Does not contain port number in CLA
// 			System.exit(0);

// 		try { // Get port number
// 			port = Integer.valueOf(args[0]);
// 		} catch (IndexOutOfBoundsException | NumberFormatException e) { // Invalid port number
// 			System.exit(0);
// 		}

// 		startClient(port);

// 	}

// 	private static void startClient(int port) {
// 		try {
// 			Socket clientSocket = new Socket("localhost", port); //Socket to localhost
// 			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
// 			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

// 			Send send = new Send(output, clientSocket); //Class to handle sending messages to server
// 			Receive receive = new Receive(input, clientSocket); //Class to handle receiving messages from server
			
// 			//First message is always the name
// 			Scanner in = new Scanner(System.in);
// 			System.out.println("What is your name?");
// 			String name = in.nextLine();
			
// 			System.out.println("Sending name to server...");
// 			output.writeUTF(name);
			
// 			// Creates new threads
// 			Thread sendThread = new Thread(send);
// 			Thread receiveThread = new Thread(receive);
			
// 			//Start threads
// 			sendThread.start();
// 			receiveThread.start();
			
// 		} catch (Exception e) {
// 		}

// 	}
	
// 	public static class Send implements Runnable{
// 		private DataOutputStream output;
// 		private Socket clientSocket;
		
// 		public Send(DataOutputStream output, Socket clientSocket) {
// 			this.output = output;
// 			this.clientSocket = clientSocket;
// 		}
// 		@Override
// 		public void run() {
// 			Scanner input = new Scanner(System.in); //Handles standard input
// 			String message = ""; //Placeholder message
// 			while (input.hasNext()) { //Continuously runs until no input is given
// 				message = input.nextLine(); //Stores standard input
// 				try { //Attempts to send message to client/server
// 					output.writeUTF(message); 
// 				} catch (IOException e) {}
// 			}
// 			input.close(); //Close input when no more messages
// 			try { //Attempt to close the client socket and shutdown
// 				clientSocket.shutdownOutput();
// 				clientSocket.close();
// 			} catch (Exception e) {
// 				System.exit(0);
// 			}
// 		}
		
// 	}
	
// 	public static class Receive implements Runnable{
// 		DataInputStream input;
// 		Socket clientSocket;
		
// 		public Receive(DataInputStream input, Socket clientSocket) {
// 			this.input = input;
// 			this.clientSocket = clientSocket;
// 		}
		
// 		@Override
// 		public void run() {
// 			String message = ""; //Placeholder message
// 			try {
// 				message = input.readUTF(); //Attempts to read the incoming message
// 				while(true) { //Prints message and continuously receives new messages
// 					System.out.println(message);
// 					message = input.readUTF();
// 				}
// 			} catch (Exception e) {
// 			}
			
// 		}
		
// 	}
// }
