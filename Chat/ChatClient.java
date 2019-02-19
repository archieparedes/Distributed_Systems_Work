import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClient {    
    
    public static void main(String[] args){
        if(args.length < 1) throw new IllegalArgumentException("No arguments found"); // if address found in args

        try {
            Socket client_s;
            DataOutputStream output;
            DataInputStream input;		
            BufferedReader br;
            
            String server_address;
            String port = args[0];

            try{    // if no second argument, default to local host
				server_address = args[1]; 
			} catch (Exception e){
				server_address = "localhost";
			}

            client_s = new Socket(server_address, Integer.parseInt(port));
            output = new DataOutputStream(client_s.getOutputStream());
            input = new DataInputStream((client_s.getInputStream()));	
            br = new BufferedReader(new InputStreamReader(System.in));

            // send name to server
            Thread send_name = new Thread(new Runnable(){
                String name;
                @Override
                public void run() {
                    try{
                        System.out.println("What is your name?");
                        name = br.readLine();
                        output.writeUTF(name);
                        System.out.println("Sending name to server...");
                    } catch (Exception e){}
                }
            });
            send_name.start();

            // sending messages to server
            Thread send = new Thread(new Runnable(){
                String message;	
                @Override
                public void run() {
                    while(true){
                        try{
                            message = br.readLine(); // gets input from client

                            if(message.isEmpty()){ // if the input is empty, close and turn off socket
                                try{
                                    br.close();
                                    input.close();
                                    client_s.shutdownOutput();
                                    client_s.close();
                                    System.exit(0);
                                } catch (Exception e1){System.exit(0);}
                                System.exit(0); // exit
                            }

                            output.writeUTF(message); // write message to other clients
                            output.flush();
                        } catch (Exception e) {System.exit(0);} // exit
                    }
                }
            });
            

            // receiving messages from clients
            Thread rec = new Thread(new Runnable(){
                String message;

                @Override
                public void run() {
                    try {
                        message = input.readUTF(); // reads message
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
                        } catch(Exception e1){System.exit(0);} // exit
                        System.exit(0);
                    }
                }
            });
            
            // start threads
            send.start();
            rec.start();
        } catch (IOException e){System.exit(0);} // exit
    }
}