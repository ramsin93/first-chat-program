package ramchat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread {
    private Socket clientSocket;
    
    public Server (Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
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
        
        try (BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(bos, "ISO-8859-1");) {
            Scanner in = new Scanner(System.in);
            
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
            return;
        }
    }
    
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
                
        while (true) {
            try {
                Socket s = serverSocket.accept();
                System.out.println("Connected to " + s.getLocalAddress());
                new Server(s).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
