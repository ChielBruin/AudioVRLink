package nl.overplexer.audiovrlink;

import android.hardware.SensorManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by chiel on 15-12-15.
 */
public class DataClient {

    public Socket socket;
    private static final int SERVER_PORT = 6000;
    //private static String SERVER_IP = "145.94.220.198";
    private static String SERVER_IP = "192.168.1.10";

    public DataClient() {
        new Thread(new ClientThread()).start();
    }

    public void setIP(String IP) {
        SERVER_IP = IP;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new ClientThread()).start();
    }

    public String getIP() {
        return SERVER_IP;
    }

    public boolean isReady(){
        if(socket == null) return false;
        return socket.isConnected();
    }

    public void send(float[] forward, float move) {
        if(forward.length != 3) throw new IllegalArgumentException("Size of the vector is not 3, but " + forward.length);
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), false);
            for (int i = 0; i < 3; i++) {
                out.println(forward[i]);
            }
            out.println(move);
            out.flush();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        try {
            socket.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
