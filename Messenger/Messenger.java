import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Messenger{

	public static void server(String port){
		try{
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));
			Socket client_s = server_s.accept();
			Scanner scans = new Scanner(System.in);
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			output.flush();

			// sending messages to client
			Thread send = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					while(true){
						message = scans.next(); // gets input from server
						try{
							output.writeUTF(message);
							output.flush();
						} catch (Exception e) {
							System.out.println(e);
						}
					}
				}
			});
			send.start();

			// receiving messages from client
			Thread rec = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					try {
						message = input.readUTF();
						while(message != null){
							//System.out.println("Client says: " + message);
							System.out.println(message);
							message = input.readUTF();
						}
						System.out.println("Client disconnection.");
						input.close();
						server_s.close();
						client_s.close();
						return;
					} catch (IOException e){
						e.printStackTrace();
					}
				}
			});
			rec.start();
			
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		
	}

	public static void client(String port, String server_address){
		try {
			Socket s = new Socket(server_address, Integer.parseInt(port));
			Scanner scans = new Scanner(System.in);
			DataOutputStream output = new DataOutputStream(s.getOutputStream());
			DataInputStream input = new DataInputStream((s.getInputStream()));			

			// sending messages to server
			Thread send = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					while(true){
						message = scans.next(); // gets input from server
						try{
							output.writeUTF(message);
							output.flush();
						} catch (Exception e) {
							System.out.println(e);
						}
					}
				}
			});
			send.start();

			// receiving messages from servers
			Thread rec = new Thread(new Runnable(){
				String message;
				@Override
				public void run() {
					try{
						message = input.readUTF();
						while(message != null){
							//System.out.println("Server says: " + message);
							System.out.println(message);
							message = input.readUTF();
						}
						System.out.println("Server disconnection.");
						input.close();
						s.close();
						return;
					} catch (IOException e){
						e.printStackTrace();
					}
				}
			});
			rec.start();
			
		} catch (IOException e){
			e.printStackTrace();
		}
	
		
	}
	public static void main(String args[]){
		String port;
		String server_address;
		
		if (args[0].charAt(0) == '-' && args[0].charAt(1) == 'l') { // Server
			port = args[1];
			server(port);
		} else { // client
			System.out.println("port: " + args[0] + " sa: "+ args[1]);
			port = args[0];
			server_address = args[1];
			client(port, server_address);
		}
	}

}
