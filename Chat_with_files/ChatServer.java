import java.net.*;
import java.io.*;
import java.util.*;

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

 
class clients implements Runnable{
    public Socket client_socket;
    DataInputStream input;
    DataOutputStream output;
    public String name;
    int lport;
 
    public clients(Socket s, DataInputStream in, DataOutputStream out, String n, int lport){ // initializer
        this.client_socket = s;
        this.input = in;
        this.output = out;
        this.name = n;
        this.lport = lport;
    }
    public void sendMessage(){
        try{
            String message = input.readUTF();
            System.out.println(name + " sends a message");
            if (message == null || message == ""){ // if empty or null
                this.client_socket.shutdownOutput();
                this.client_socket.close(); // close client socket
                ChatServer.client_list.remove(this);
            }

            for (clients c : ChatServer.client_list){ // iterate through clients
                if (!c.equals(this))    c.output.writeUTF(this.name + ": " + message); // send message to those clients that don't match sender
            }
        } catch (Exception e){
            return;
        }
        
    }
    @Override
    public void run(){
        String message;
        String requestType;
        while(true){
            try {
                requestType = input.readUTF();
                switch(requestType){
                    case "m":
                        sendMessage();
                        break;
                    case "f":
                        System.out.println(name + " request a file");
                        break;
                    default:
                        requestType = "";
                        break;
                }
                if (requestType.equals("m")){
                    
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
                String lport = input.readUTF();
                System.out.println("User: " + name + " lport: " + lport);
                clients c = new clients(client_s, input, output, name, Integer.parseInt(lport));
                Thread rec = new Thread(c);
                client_list.add(c); // add to list of clients
                rec.start(); // starting the receiving thread
            }
        } catch (Exception e){}
    }
}