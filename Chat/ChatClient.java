import java.net.*;
import java.io.*;

public class ChatServer {    
    Socket client_s;
    DataOutputStream output;
    DataInputStream input;		
    BufferedReader br;
    public ChatServer(String p, String serverAddress){
        try{
            this.client_s = new Socket(serverAddress, Integer.parseInt(p));
            this.output = new DataOutputStream((this.client_s).getOutputStream());
            this.input = new DataInputtStream((this.client_s).getOutputStream());
            this.br = new BufferedReader(new InputStreamReader(System.in));
        }
    }  
    public static void client(String port, String server_address){
        try {
            // sending messages to server
            Thread send = new Thread(new Runnable(){
                String message;	
                @Override
                public void run() {
                    while(true){
                        try{
                            message = br.readLine(); // gets input from client
                            if(message.isEmpty()){ // if the input is empty, close
                                try{
                                    br.close();
                                    input.close();
                                    client_s.close();
                                } catch (Exception e1){System.exit(0);}
                                System.exit(0);
                            }
                            output.writeUTF(message); // write message to server
                            output.flush();
                        } catch (Exception e) {System.exit(0);}
                    }
                }
            });
            send.start();

            // receiving messages from servers
            Thread rec = new Thread(new Runnable(){
                String message;
                @Override
                public void run() {
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
                }
            });
            rec.start();
        } catch (IOException e){System.exit(0);}
    }
}