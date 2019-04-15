/**
 * @author Archie_Paredes
 * @version 1.1
 * Server Side File Transfer
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class fileTransfer implements Runnable { // put owner port here
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
                fileOut.write(buffer, 0, r); // write to file
            }

            output.flush();
            fileOut.close();
		    client_s.close();
		} catch (Exception e) {
			System.exit(0);
        }
	}
}