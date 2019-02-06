import java.net.*;
import java.io.*;

public class ChatServer {    
    public static void server(String port){
		try{
			ServerSocket server_s = new ServerSocket(Integer.parseInt(port));
			Socket client_s = server_s.accept();
			DataOutputStream output = new DataOutputStream(client_s.getOutputStream());
			DataInputStream input = new DataInputStream((client_s.getInputStream()));			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// sending messages to client
			Thread send = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
					while(true){
						try{
							message = br.readLine(); // server inputs message
							if(message.isEmpty()){ // if the message is empty, exit. close everything
								br.close();
								client_s.shutdownOutput();
								client_s.close();
								server_s.close();
								//System.exit(0);
							}
							output.writeUTF(message); // write the message to client
							output.flush(); // flush 
						} catch (Exception e) {
							//System.exit(0);
						}
					}
				}
			});
			send.start(); // start the send thread

			// receiving messages from client
			Thread rec = new Thread(new Runnable(){
				String message;	
				@Override
				public void run() {
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
						//System.exit(0);
					} catch (IOException e){
						//System.exit(0);
					}
				}
			});
			rec.start(); // starting the receiving thread
	
		} catch (Exception e){}
		
	}
}