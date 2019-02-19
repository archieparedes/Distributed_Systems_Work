import java.net.*;
import java.io.*;
import java.util.*;

class clients implements Runnable{
	public Socket client_socket;
	DataInputStream input;
	DataOutputStream output;
	public String name;

	public clients(Socket s, DataInputStream in, DataOutputStream out, String n){ // initializer
		this.client_socket = s;
		this.input = in;
		this.output = out;
		this.name = n;
	}

	@Override
	public void run(){
		String message;

		while(true){
			try {
				message = input.readUTF();
				if (message == null || message == ""){ // if empty or null
					this.client_socket.shutdownOutput();
					this.client_socket.close(); // close client socket
					ChatServer.client_list.remove(this);
				}

				for (clients c : ChatServer.client_list){ // iterate through clients
					if (!c.equals(this))	c.output.writeUTF(this.name + ": " + message); // send message to those clients that don't match sender
				}

			} catch (IOException e){
				try{
					this.client_socket.shutdownOutput();
					this.client_socket.close(); // close client socket
					ChatServer.client_list.remove(this);
				} catch (Exception e1){};
			}
		}
	}
}

public class ChatServer {    
	static ArrayList<clients> client_list = new ArrayList<clients>(); // stores clients

    public static void main(String[] args){
		try{
			String port = args[0]; // first argument
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));

			while(true) {
				Socket client_s = server_s.accept(); // accept clients
				DataInputStream input = new DataInputStream((client_s.getInputStream()));
				DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
				String name = input.readUTF();
				System.out.println(name);
				clients c = new clients(client_s, input, output, name);
				Thread rec = new Thread(c);
				client_list.add(c); // add to list of clients
				rec.start(); // starting the receiving thread
			}
		} catch (Exception e){}
	}
}
