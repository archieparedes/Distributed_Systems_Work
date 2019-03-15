import java.net.*;
import java.io.*;
import java.util.*;

//////////////////////////// file Transfer classes ////////////////////////////
class fileTransfer implements Runnable{
	String fileName;
	int port;
	public fileTransfer(String fn, String p){
		this.fileName = fn;
		this.port = Integer.parseInt(p);
	}
	
	@Override
	public void run() {
		try {
				Socket sock = new Socket("localhost", port);
				DataOutputStream output = new DataOutputStream(sock.getOutputStream());
				DataInputStream input = new DataInputStream(sock.getInputStream());
				output.writeUTF(fileName);
				FileOutputStream fileOut = new FileOutputStream(fileName);
				int numRead;
				byte[] buffer = new byte[1500];
				while ((numRead = input.read(buffer)) != -1) {
					fileOut.write(buffer, 0, numRead);
				}
				fileOut.close();
				sock.close();
		} catch (Exception e) {System.exit(0);}
	}

}

class fileClient implements Runnable{
	int port;

	public fileClient(String p){
		this.port = Integer.parseInt(p);
	}

	@Override
	public void run() {
		try {
			ServerSocket servSock= new ServerSocket(this.port);
			while (true) {
				Socket sock = servSock.accept();
				DataOutputStream output = new DataOutputStream(sock.getOutputStream());
				DataInputStream input = new DataInputStream(sock.getInputStream());
				String fileName = input.readUTF();
				File file = new File(fileName);
				FileInputStream fileInput = new FileInputStream(file);
				byte[] file_buffer = new byte[1500];
				int numRead;
				while ((numRead = fileInput.read(file_buffer)) != -1) {
					output.write(file_buffer, 0, numRead);
				}
				fileInput.close();
				sock.close();
			}
		} catch (Exception e) {
			System.exit(0);
		}
	}
}

class fileServer implements Runnable{
	ServerSocket server_s;
	
	public fileServer(ServerSocket s){
		this.server_s = s;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket sock = (this.server_s).accept();
				DataOutputStream output = new DataOutputStream(sock.getOutputStream());
				DataInputStream input = new DataInputStream(sock.getInputStream());
				String fileName = input.readUTF();
				File file = new File(fileName);
				FileInputStream fileInput = new FileInputStream(file);
				byte[] file_buffer = new byte[1500];
				int numRead;
				while ((numRead = fileInput.read(file_buffer)) != -1) {
					output.write(file_buffer, 0, numRead);
				}
				fileInput.close();
				sock.close();
			}

		} catch (Exception e) {System.exit(0);}
	}
}

public class MessengerWithFiles{
	//////////////////////////// server method ////////////////////////////
	public static void server(String port){
		try{
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));
			Socket client_s = server_s.accept();
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// get lport 6002
			String lport = input.readUTF();
			
			fileServer ftS = new fileServer(server_s);
			Thread ft = new Thread(ftS);

			// sending types to client
			Thread sendType = new Thread(new Runnable(){
				String inputType, message, fileName;	
				@Override
				public void run() {
					while(true){
						try{
							System.out.println("Enter an option ('m', 'f', 'x'):\n (M)essage (send)\n (F)ile (request)\ne(X)it");
							inputType = br.readLine(); // gets input from client
							//System.out.println("inputtype: " + inputType);
							if(inputType.equals("m")){
								// send message to server
								System.out.println("Enter your message:");
								message = br.readLine(); 
								if(message.isEmpty()){ // if the input is empty, close
									try{
										br.close();
										input.close();
										client_s.close();
										System.exit(0);
									} catch (Exception e1){System.exit(0);}
								}
								output.writeUTF(message); // write message to server
								output.flush();
							} else if(inputType.equals("f")){
								// request a file from server
								System.out.println("Which file do you want?");
								fileName = br.readLine(); // user inputs file name
								fileTransfer f = new fileTransfer(fileName, lport);
								Thread transfer = new Thread(f);
								transfer.start();
							
							} else if(inputType.equals("x")){
								// exit if statement
								System.out.println("closing your sockets...goodbye");
								// close 
								br.close();
								input.close();
								client_s.shutdownOutput();
								client_s.close();
								server_s.close();
								System.exit(0);
							} // if invalid input, restart
							//System.out.println("if statement failed");
							
						} catch (Exception e) {System.exit(0);} // if any error is found, exit program
					}
				}
			});
			sendType.start();
			ft.start();

			// receiving messages from client
			String message;
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
		} catch (Exception e){}	
	}

	//////////////////////////// client method ////////////////////////////
	public static void client(String port, String server_address, String secondPort){
		try {
			Socket client_s = new Socket(server_address, Integer.parseInt(port));
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// send lport
			output.writeUTF(secondPort);
			
			fileClient ftC = new fileClient(secondPort);
			Thread ft = new Thread(ftC);
			// sending messages to server
			Thread sendType = new Thread(new Runnable(){
				String inputType, message, fileName;	
				@Override
				public void run() {
					while(true){
						try{
							System.out.println("Enter an option ('m', 'f', 'x'):\n (M)essage (send)\n (F)ile (request)\ne(X)it");
							inputType = br.readLine(); // gets input from client
							//System.out.println("inputtype: " + inputType);
							if(inputType.equals("m")){
								// send message to server
								System.out.println("Enter your message:");
								message = br.readLine(); 
								if(message.isEmpty()){ // if the input is empty, close
									try{
										br.close();
										input.close();
										client_s.close();
										System.exit(0);
									} catch (Exception e1){System.exit(0);}
								}
								output.writeUTF(message); // write message to server
								output.flush();
							} else if(inputType.equals("f")){
								// request a file from server
								System.out.println("Which file do you want?");
								fileName = br.readLine(); // user inputs file name
								fileTransfer f = new fileTransfer(fileName, secondPort);
								Thread transfer = new Thread(f);
								transfer.start();
							
							} else if(inputType.equals("x")){
								// exit
								System.out.println("closing your sockets...goodbye");
								br.close();
								input.close();
								client_s.close();
								System.exit(0);
							} // if invalid input, restart
							//System.out.println("if statement failed");
						} catch (Exception e) {System.exit(0);} // if any error is found, exit program
					}
				}
			});
			sendType.start();
			ft.start();

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
		} catch (IOException e){System.exit(0);}
	}

	public static void main(String args[]){
		String port;
		String serverAddress;
		String secondPort;
		
		if(args.length < 1){
			throw new IllegalArgumentException("No arguments found");
		}
		else {
			if (args[0].charAt(0) == '-' && args[0].charAt(1) == 'l' && args.length == 2) { // Server
				port = args[1];
				server(port);
			} else if(args[0].charAt(0) == '-' && args[0].charAt(1) == 'l' && args[2].charAt(0) == '-' && args[2].charAt(1) == 'p' && args.length == 4){
				secondPort = args[1];
				port = args[3];
				serverAddress = "localhost";
				client(port, serverAddress, secondPort);
			}
		}
	
	}
}