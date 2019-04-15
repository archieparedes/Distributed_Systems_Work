/**
 * @author Archie_Paredes
 * @version 1.1
 * Client Side Send Type
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class sendType implements Runnable {
	BufferedReader br;
	DataOutputStream output;
	String lPort;

	/**
	 * Type of send to other clients and server
	 * @param br BufferReader to handel inputs
	 * @param output DataOutputStream outputs information to server
	 * @param p String p lport for file 
	 */
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
					System.out.println("Invalid send type... moving on");
				}
			}
			output.writeUTF(""); // clear. not needed?
		} catch (IOException e) {
			System.exit(0);
		}
	}
}