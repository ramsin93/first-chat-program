package ramchat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RamChat {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter address followed by port: ");
        String addr = in.next();
        int port = in.nextInt();
        
        try (Socket clientSocket = new Socket(addr, port);
            BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(bos, "ISO-8859-1");) {
            new Thread(() -> {
                try (
                        BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream()); 
                        InputStreamReader isr = new InputStreamReader(bis, "ISO-8859-1");
                )   {
                    while (true) {
                        try {
                            if (isr.ready())
                                System.out.print((char) isr.read());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } 
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                String s = in.nextLine().concat("\n");

                try {
                    osw.write(s);
                    osw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
