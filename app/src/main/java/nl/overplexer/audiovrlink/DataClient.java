package nl.overplexer.audiovrlink;

import android.hardware.SensorManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by chiel on 15-12-15.
 */
public class DataClient {

    public Socket socket;
    private static final int SERVER_PORT = 6000;
    //private static String SERVER_IP = "145.94.220.198";
    private static String SERVER_IP = "192.168.1.10";
    private static String LOCAL_IP = null;

    public static String parseConnectCode(String code) throws IllegalArgumentException {
        if (code.length() != 4) throw new IllegalArgumentException("The given code is not 4 character long!");
        int expected = getCheckSum();
        int actual = Character.getNumericValue(code.charAt(3));
        if(expected != actual)throw new IllegalArgumentException("This code does not connect to a device on this network!\nChecksum was " + actual + " and not " + expected);

        return LOCAL_IP.substring(0, LOCAL_IP.lastIndexOf('.')) + '.' + code.substring(0,3);
    }

    public DataClient() {
        new Thread(new ClientThread()).start();
    }

    public DataClient(String ip) {
        SERVER_IP = ip;
        new Thread(new ClientThread()).start();
    }

    public static int getCheckSum() {
        if(LOCAL_IP == null) getLocalIP();
        String[] vals = LOCAL_IP.split("\\.");
        int res = Integer.valueOf(vals[0]) + Integer.valueOf(vals[1]) * 3 + Integer.valueOf(vals[2]);
        return res % 10;
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

    public static String getLocalIP() {
        if(LOCAL_IP == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
                        for (; n.hasMoreElements();)
                        {
                            NetworkInterface e = n.nextElement();
                            Enumeration<InetAddress> a = e.getInetAddresses();
                            for (; a.hasMoreElements();)
                            {
                                InetAddress addr = a.nextElement();
                                if(!addr.getHostAddress().startsWith("127.") && !addr.getHostAddress().contains("::")){
                                    LOCAL_IP = addr.getHostAddress();
                                    return; // May cause bugs later
                                }
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            while(LOCAL_IP == null);
        }
        return LOCAL_IP;
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

    public String getServerIP() {
        return SERVER_IP;
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
