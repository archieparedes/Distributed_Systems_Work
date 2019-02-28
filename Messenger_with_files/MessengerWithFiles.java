/**
 * @author Archie_Paredes
 * @version 1.0
 * Created on February 20, 2019
 */

import java.io.*;
import java.net.*;
import java.util.*;

//////////////////////////// file Transfer class runs////////////////////////////
class fileTransfer implements Runnable {
	String fileName;
	int lPort;

	public fileTransfer(String fileName, String p) {
		this.fileName = fileName;
		this.lPort = Integer.parseInt(p);
	}

	@Override
	public void run() {
		try {
			Socket client_s = new Socket("localhost", lPort);
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream(client_s.getInputStream());
			output.writeUTF(fileName); // gets name
			FileOutputStream fileOut = new FileOutputStream(fileName); // file send
			int r;
			byte[] buffer = new byte[1500];
			while ((r = input.read(buffer)) != -1) {
				fileOut.write(buffer, 0, r);
			}
			fileOut.close();
			client_s.close();
		} catch (Exception e) {
			System.exit(0);
		}
	}
}

class ftC implements Runnable {
	int lPort;

	public ftC(String p) {
		this.lPort = Integer.parseInt(p); // change string port to int
	}

	@Override
	public void run() {
		try {
			ServerSocket server_s = new ServerSocket(lPort);
			while (true) {
				Socket client_s = server_s.accept();
				DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
				DataInputStream input = new DataInputStream(client_s.getInputStream());
				String fileName = input.readUTF();
				File file = new File(fileName);
				FileInputStream file_input = new FileInputStream(file);
				byte[] file_buffer = new byte[1500];
				int r;
				while ((r = file_input.read(file_buffer)) != -1) {
					output.write(file_buffer, 0, r);
				}
				file_input.close();
				client_s.close();
			}
		} catch (Exception e) {System.exit(0);}
	}
}

class ftS implements Runnable {
	ServerSocket server_s;

	public ftS(ServerSocket ss) {
		this.server_s = ss;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket client_s = server_s.accept();
				DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
				DataInputStream input = new DataInputStream(client_s.getInputStream());
				String fileName = input.readUTF();
				File file = new File(fileName);
				FileInputStream file_input = new FileInputStream(file);
				byte[] file_buffer = new byte[1500];
				int r;
				while ((r = file_input.read(file_buffer)) != -1) {
					output.write(file_buffer, 0, r);
				}
				file_input.close();
				client_s.close();
			}
		} catch (Exception e) { System.exit(0); }
	}
}

//////////////////////////// Sending types run////////////////////////////
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
				inputType = br.readLine(); // gets input from client
				if (inputType.equals("m")) {
					System.out.println("Enter your message:");
					inputType = br.readLine();
					if(inputType.isEmpty()){ // if the input is empty, close
						try{
							br.close();
							output.close();
							System.exit(0);
						} catch (Exception e1){System.exit(0);}
					}
					output.writeUTF(inputType);
					output.flush();
				} else if (inputType.equals("f")) {
					System.out.println("Which file do you want?");
					String fileName = br.readLine();
					MessengerWithFiles messenger = new MessengerWithFiles();
					fileTransfer get = new fileTransfer(fileName, lPort);
					Thread fileTransf = new Thread(get);
					fileTransf.start();

				} else if (inputType.equals("x")) {
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

public class MessengerWithFiles {
	//////////////////////////// Client method ////////////////////////////
	/**
	 *
	 * @param secondPort lport
	 * @param port port server is connected to
	 * @param server_address server address
	 */
	public static void client(String secondPort, String port, String server_address) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // inputs
			Socket client_s = new Socket(server_address, Integer.parseInt(port)); // creates is a new client socket
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream()); // handles writing
			DataInputStream input = new DataInputStream(client_s.getInputStream()); // handles reading

			output.writeUTF(secondPort); // send lport to server

			ftC fileClient = new ftC(secondPort); // connect client to file transfer port
			sendType send = new sendType(br, output, port); // what type of send does client want to do?
			Thread sendThread = new Thread(send);
			Thread fileTransferClient = new Thread(fileClient);
			
			sendThread.start();
			fileTransferClient.start();

			// receiving messages from servers
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
			output.writeUTF("");
			client_s.close();
			System.exit(0);

		} catch (Exception e) {
			System.exit(0);
		}
	}

	//////////////////////////// Server method ////////////////////////////
	/**
	 *
	 * @param p port number
	 */
	public static void server(String p) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); // handles user inputs
			ServerSocket server_s = new ServerSocket(Integer.parseInt(p)); // creates a new server socket
			Socket client_s = server_s.accept(); // accept the client
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream()); // handles writing
			DataInputStream input = new DataInputStream(client_s.getInputStream()); // handles reading

			String lPort = input.readUTF(); // reads in first output from client as lport
			
			ftS fileServer = new ftS(server_s); // connect server to file transfer port
			sendType send = new sendType(br, output, lPort); // what type of send does server want to do?
			Thread sendThread = new Thread(send);
			Thread fileTransferServer= new Thread(fileServer);

			sendThread.start();
			fileTransferServer.start();

			// receiving messages from servers
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
			
			// close everything
			output.writeUTF("");
			server_s.close();
			client_s.shutdownOutput();
			client_s.close();
			System.exit(0);
		} catch (IOException e) {System.exit(0); }
	}

	/**
	 *
	 * @param args from command/terminal
	 * @throws IllegalArgumentException if no arguments found
	 */
	public static void main(String[] args){
		String port;
		String serverAddress;
		String secondPort; // lport
		
		if(args.length < 1){ // if no arguments found, throw an error
			throw new IllegalArgumentException("No arguments found");
		} else {
			if (args[0].charAt(0) == '-' && args[0].charAt(1) == 'l' && args.length == 2) { // server
				port = args[1];
				server(port);
			} else if(args[0].charAt(0) == '-' && args[0].charAt(1) == 'l' && args[2].charAt(0) == '-' && args[2].charAt(1) == 'p' && args.length == 4){ // client
				secondPort = args[1];
				port = args[3];
				try{
					serverAddress = args[4]; // 4th argument is server address
				}catch(Exception e){
					serverAddress = "localhost"; // default to local host
				}

				client(secondPort, port, serverAddress);
			}
		}
	}
}
