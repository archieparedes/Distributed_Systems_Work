import java.net.*;
import java.io.*;

public class Messenger{
	// server method
	public static void server(String port){
		try{
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));
			Socket client_s = server_s.accept();
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// sending messages to client
			Thread send = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					while(true){
						try{
							message = br.readLine(); // server inputs message
							if(message.isEmpty()){ // if the message is empty, exit. close everything
								br.close();
								client_s.shutdownOutput();
								client_s.close();
								server_s.close();
								System.exit(0);
							}
							output.writeUTF(message); // write the message to client
							output.flush(); // flush 
						} catch (Exception e) {System.exit(0);}
					}
				}
			});
			send.start(); // start the send thread

			// receiving messages from client
			Thread rec = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					try {
						message = input.readUTF();
						while(message != null){
							System.out.println(message);
							message = input.readUTF();
						}
						// client disconnected, so close everything
						br.close();
						input.close();
						client_s.shutdownOutput();
						client_s.close();
						server_s.close();
						System.exit(0);
					} catch (IOException e){System.exit(0);}
				}
			});
			rec.start(); // starting the receiving thread
	
		} catch (Exception e){}
		
	}

	// client method
	public static void client(String port, String server_address){
		try {
			Socket client_s = new Socket(server_address, Integer.parseInt(port));
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
			// sending messages to server
			Thread send = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					while(true){
						try{
							message = br.readLine(); // gets input from client
							if(message.isEmpty()){ // if the input is empty, close
								try{
									br.close();
									input.close();
									client_s.close();
								} catch (Exception e1){System.exit(0);}
								System.exit(0);
							}
							output.writeUTF(message); // write message to server
							output.flush();
						} catch (Exception e) {System.exit(0);}
					}
				}
			});
			send.start();

			// receiving messages from servers
			Thread rec = new Thread(new Runnable(){
				String message;
				@Override
				public void run() {
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
				}
			});
			rec.start();
		} catch (IOException e){System.exit(0);}
	}

	public static void main(String args[]){
		String port;
		String server_address;
		
		if(args.length < 1){
			throw new IllegalArgumentException("No arguments found");
		}
		else {
			if (args[0].charAt(0) == '-' && args[0].charAt(1) == 'l') { // Server
				port = args[1];
				server(port);
			} else { // client
				port = args[0];
				try{
					server_address = args[1];
				} catch (Exception e){
					server_address = "localhost";
				}
				client(port, server_address);
			}
		}
	}
}