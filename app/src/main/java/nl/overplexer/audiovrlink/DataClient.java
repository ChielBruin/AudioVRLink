package nl.overplexer.audiovrlink;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import static nl.overplexer.audiovrlink.Util.getString;


/**
 *  Wrapper class for a socket.
 */
public class DataClient {

    public Socket socket;
    private static final int SERVER_PORT = 6000;
    private static String SERVER_IP = "0.0.0.0";
    private static String LOCAL_IP = null;

    /**
     * Constructor <br>
     * Connects to the specified IP address.
     * @param ip the ipv4 address to connect to.
     */
    public DataClient(String ip) {
        SERVER_IP = ip;
        new Thread(new ClientThread()).start();
    }

    /**
     * Parses a connect code to a valid IP address.
     * The connect code must be 4 digits and follow these rules: <br>
     *     <ul>
     *         <li>The code is 4 digits long</li>
     *         <li>The first 3 digits are the last 3 digits of the IP address</li>
     *         <li>The last digit is a checksum value</li>
     *     </ul>
     * The checksum for an IP address of the form 'a.b.c.d' is (a + 3b + c) % 10
     * @param code the connect code
     * @return The parsed IP address
     * @throws IllegalArgumentException when the code does not follow the before mentioned rules.
     */
    public static String parseConnectCode(String code) throws IllegalArgumentException {
        if (code.length() != 4) throw new IllegalArgumentException(getString(R.string.ccWrongSize));
        int expected = getCheckSum();
        int actual = Character.getNumericValue(code.charAt(3));
        if(expected != actual) throw new IllegalArgumentException(getString(R.string.ccWrongChecksum));

        return LOCAL_IP.substring(0, LOCAL_IP.lastIndexOf('.')) + '.' + code.substring(0,3);
    }

    /**
     * Calculates the required checksum.
     * The checksum for an IP address of the form 'a.b.c.d' is (a + 3b + c) % 10
     * @return the checksum value.
     */
    public static int getCheckSum() {
        if(LOCAL_IP == null) getLocalIP();
        String[] vals = LOCAL_IP.split("\\.");
        int res = Integer.valueOf(vals[0]) + Integer.valueOf(vals[1]) * 3 + Integer.valueOf(vals[2]);
        return res % 10;
    }

    /**
     * Set the IP address to connect to and restart the socket.
     * @param IP the new IP address
     */
    public void setIP(String IP) {
        SERVER_IP = IP;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new ClientThread()).start();
    }

    /**
     * Gets the local IP and stores this in a constant for speed reasons.
     * @return The local IP address.
     */
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

    /**
     * Check if the socket is ready.
     * For the socket to be ready it must be initialized and connected.
     * @return true if the socket is ready, false otherwise.
     */
    public boolean isReady() {
        return socket != null && socket.isConnected();
    }

    /**
     * Send data to the connected server.
     * @param forward a forward facing direction vector.
     * @param move the movement speed of the player
     * @throws IllegalArgumentException when the vector has the wrong size.
     */
    public void send(float[] forward, float move) throws IllegalArgumentException{
        if(forward.length != 3) throw new IllegalArgumentException(getString(R.string.wrongVectorSize,3));
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), false);
            for (int i = 0; i < 3; i++) {
                out.println(forward[i]);
            }
            out.println(move);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the socket.
     * @return true is the socket closed correctly, false otherwise.
     */
    public boolean close() {
        try {
            socket.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Getter for the connected server IP.
     * @return the IP address of the connected server.
     */
    public String getServerIP() {
        return SERVER_IP;
    }

    /**
     * Thread that starts the socket.
     */
    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
