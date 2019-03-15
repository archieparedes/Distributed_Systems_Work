import java.net.*;
import java.io.*;
import java.util.*;

class fileTransfer implements Runnable {
	String fileName;
    int lPort;
    String owner;

	public fileTransfer(String fileName, String p, String Owner) {
		this.fileName = fileName;
        this.lPort = Integer.parseInt(p);
        this.owner = Owner; 
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
                    output.writeUTF(inputType);
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
                    output.writeUTF(inputType);
                    System.out.println("Who owns the file?");
                    String owner = br.readLine();
					System.out.println("Which file do you want?");
					String fileName = br.readLine();
					String ownerFile = owner+"**"+fileName;
					fileTransfer get = new fileTransfer(fileName, lPort, owner);
					Thread fileTransf = new Thread(get);
					fileTransf.start();

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

            ftC fileClient = new ftC(secondPort); // connect client to file transfer port
			sendType send = new sendType(br, output, port); // what type of send does client want to do?
			Thread sendThread = new Thread(send);
			Thread fileTransferClient = new Thread(fileClient);
			
			sendThread.start();
			fileTransferClient.start();

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