import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Messenger{

	public static void server(String port){
		try{
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));
			Socket client_s = server_s.accept();

			System.out.println("Client connected to server port #" + port + ".");
		
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream(client_s.getInputStream());				

			String message = input.readUTF();			
			System.err.println( "Received from client: " + message );
			output.writeUTF( message );
			client_s.close();
		} catch (Exception e){
			System.out.println(e.getMessage() + " Connection Failed. Exiting");
			return;
		}
	}

	public static void client(String port, String server_address){
		System.out.println("hello");
		try {
			Socket s = new Socket(server_address, Integer.parseInt(port));
			Scanner scans = new Scanner(System.in);
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print( "Enter message: " );
			String message = scans.next();
			
			//InetAddress loopback= InetAddress.getLoopbackAddress();
			DataOutputStream output= new DataOutputStream(s.getOutputStream());
			DataInputStream input= new DataInputStream(s.getInputStream());				

			output.writeUTF(message);
			message = input.readUTF();
			System.out.println("message returned: " + message);
			s.close();
		} catch (Exception e){
			System.out.println("Connection Failed. Exiting\n" + e);
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
