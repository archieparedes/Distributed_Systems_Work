/**
 * @author Archie_Paredes
 * @version 1.0
 * Server Side
 */

import java.net.*;
import java.io.*;
import java.util.*;

class fileTransfer implements Runnable { // put owner port here
	String fileName;
    int lPort;
    String name;

	public fileTransfer(String fileName, int p, String name) {
		this.fileName = fileName;
        this.lPort = p;
        this.name = name;
	}

	@Override
	public void run() {
		try {
			Socket client_s = new Socket("", lPort);
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream(client_s.getInputStream());
            output.writeUTF(fileName); // sets name for ftS
            String direct = name +"/"+fileName; // set directory to put file
            
            //File file = new File(fileName);

			FileOutputStream fileOut = new FileOutputStream(direct); // creates a file
			int r;
            byte[] buffer = new byte[1500];
            
			while ((r = input.read(buffer)) > 0) { // get file from ftS
                fileOut.write(buffer, 0, r);    
            }

            output.flush();
            fileOut.close();
		    client_s.close();
		} catch (Exception e) {
			System.exit(0);
        }
	}
}

class ftS implements Runnable {
    ServerSocket server_s;
    String name;

	public ftS(ServerSocket ss, String name) {
        this.server_s = ss;
        this.name = name;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket client_s = server_s.accept();
				DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
				DataInputStream input = new DataInputStream(client_s.getInputStream());
                String fileName = input.readUTF(); // gets file name
                String direct = name +"/"+fileName; // set directory of where the file is located
                //File file = new File(fileName);
                
				FileInputStream file_input = new FileInputStream(direct); // read file
				byte[] file_buffer = new byte[1500];
                int r;
                
				while ((r = file_input.read(file_buffer)) > 0) { // send file to fileTransfer
                    output.write(file_buffer, 0, r);
                }
                
                output.flush();
				file_input.close();
		        client_s.close();
			}
        } catch (Exception e) { System.out.println("file not found" + e); }
        
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

        try{
            ServerSocket t = new ServerSocket(lport);
            ftS fileClient = new ftS(t, name);
            Thread fileTransferClient = new Thread(fileClient);
            fileTransferClient.start();
        } catch (Exception e){
            System.err.println(e);
        }
    }

    public void sendMessage(String message){
        try{
            if (message == null || message == ""){ // if empty or null
                this.client_socket.shutdownOutput();
                this.client_socket.close(); // close client socket
                ChatServer.client_list.remove(this);
            }

            for (clients c : ChatServer.client_list){ // iterate through clients
                if (!c.equals(this))    c.output.writeUTF(this.name + ": " + message); // send message to those clients that don't match sender
            }

        } catch (Exception e){
            System.err.println(e);
            return;
        }
       
    }

    public void getFile(String info){
        try {
            String[] infos = info.split("@"); 
            String owner = infos[1]; // owner name
            String filename = infos[0]; // filename
            int ownerPort = Integer.MAX_VALUE; // impossible ownerPort
            String ownerName = "";
            for (clients c : ChatServer.client_list){
                if (c.name.equals(owner)){ // sets info of owner of the files 
                    ownerName = c.name;
                    ownerPort = c.lport;
                    break;
                }
            }
            
            if (ownerPort == Integer.MAX_VALUE){ // return if no match
                System.out.println("port doesn't exist");
                return;
            }
            
            System.out.println(lport + " wants "+ filename + " from "+ownerPort);

            fileTransfer get = new fileTransfer(filename, ownerPort, name); 
            Thread fileTransf = new Thread(get);
            fileTransf.start();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void run(){
        String message;
        String requestType;
        String info;
        while(true){
            try {
                requestType = input.readUTF();
                switch(requestType){
                    case "m":
                        message = input.readUTF();
                        sendMessage(message);
                        break;
                    case "f":
                        info = input.readUTF();
                        getFile(info);
                        break;
                    default:
                        requestType = "";
                        break;
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
                
                clients c = new clients(client_s, input, output, name, Integer.parseInt(lport));
                System.out.println("User: " + name + " lport: " + lport + " connected.");
                Thread rec = new Thread(c);
                client_list.add(c); // add to list of clients
                rec.start(); // starting the receiving thread
            }
        } catch (Exception e){}
    }
}


 