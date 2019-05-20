package pckLocally.clientlocallyandroid;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;

public class Communication {
    private final int communicationPort = 10000;
    private final int communicationPortTCP = 10001;
    //musi byc na odwrot receivePort i sendPort niz w aplikacji servera
    private final int sendPort = 10003;
    private final int receivePort = 10002;
    SendThread sendThread;
    ReceiveThread receiveThread;
    PlayerStatus status;
    //Controller controller;
    boolean keepConnect = true;

    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    //BufferedReader inFromUser;
    DatagramSocket udpSocket;
    InetAddress IPAddress;
    DatagramPacket receivePacket;
    DatagramPacket sendPacket;
    ////
    /*Socket clientSocket;
    PrintWriter out;
    BufferedReader in;*/

    //TODO przekazac jakos activity aby moc odswiezac widok
    public Communication(/*Controller c*/) {
        //controller = c;
        //inFromUser = new BufferedReader(new InputStreamReader(System.in));
        sendThread = new SendThread();
        receiveThread = new ReceiveThread();
        try {
            udpSocket = new DatagramSocket();
//            String broadcast = getBroadcast();
            String broadcast = "192.168.0.172";
            IPAddress = InetAddress.getByName(broadcast);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public boolean initConnection() throws IOException {
        System.out.println("Communication thread start");

        String sentence = new String("INIT_CONNECTION");
        sendData = sentence.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, communicationPort);
        udpSocket.send(sendPacket);
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        udpSocket.receive(receivePacket);
        IPAddress = receivePacket.getAddress();

        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER: " + modifiedSentence);
        System.out.println(IPAddress);
        udpSocket.close();
        return true;
    }

    public void TCPConnection() throws InterruptedException {
        ///TCP
        sendThread.start();
        receiveThread.start();
    }

    private String getIP(){
        boolean useIPv4 = true;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
    private String getBroadcast() throws UnknownHostException, SocketException {
        //InetAddress IP = InetAddress.getLocalHost(); //TODO generuje blad
        //String ip = new String(IP.getHostAddress());
        //WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        //String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        String ip = getIP();
        String[] parts = ip.split("\\.", 0);
        short[] broadcast = new short[4];
        for (int i = 0; i < 4; i++) {
            broadcast[i] = Short.valueOf(parts[i]);
        }

        InetAddress localHost = Inet4Address.getLocalHost();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
        short mask = networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength();

        int binmask = 0x00000000;
        for (int i = 0; i < mask; i++) {
            binmask |= 1;
            binmask <<= 1;
        }
        binmask >>= 1;

        binmask <<= (32 - mask);
        //System.out.println("MASK:"+Integer.toBinaryString(binmask));

        int ad = 0;
        ad = ad | (broadcast[0] << 24) | (broadcast[1] << 16) | (broadcast[2] << 8) | (broadcast[3]);
        //System.out.println("IP:  "+Integer.toBinaryString(ad));

        int br = (ad & binmask) | (~binmask);
        //System.out.println("Brod:"+Integer.toBinaryString(br));
        int[] bradd = {(br & 0xff000000) >>> 24, (br & 0x00ff0000) >>> 16, (br & 0x0000ff00) >>> 8, br & 0x000000ff};
        //String braddress = (br&0xff000000) + "." + br&0x00ff0000 + "." + br&0x0000ff00 + "." + br&0x000000ff;
        String braddress = bradd[0] + "." + bradd[1] + "." + bradd[2] + "." + bradd[3];
        //System.out.println(braddress);
        return braddress;
    }

    //////////////////////////////
    public void comPlayPause(){
        sendThread.message = "Command:PLAYPAUSE";
    }

    public void comNext() {
        sendThread.message = "Command:NEXT";
    }

    public void comPrev() {
        sendThread.message = "Command:PREV";
    }

    public void comReplay() {
        sendThread.message = "Command:REPLAY";
    }

    public void comLoop() {
        sendThread.message = "Command:LOOP";
    }


    class SendThread extends Thread {
        public String message = "";
        //boolean keepConnect = true;
        private Socket clientSocket;
        private PrintWriter out;

        public void run() {
            try {
                clientSocket = new Socket(IPAddress, sendPort);
                System.out.println("Connected send");
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }


            while (keepConnect) {
                if (!message.equals("")) {
                    send(message);
                    message = "";
                }
            }
        }

        public void send(String msg) {
            System.out.println("Wysylam - IN PROGRESS");
            out.println(msg);
            System.out.println("Wyslano - DONE");
        }
    }

    class ReceiveThread extends Thread {
        //boolean keepConnect = true;
        String message;
        private Socket clientSocket;
        private BufferedReader in;

        public void run() {
            try {
                clientSocket = new Socket(IPAddress, receivePort);
                System.out.println("Connected receive");
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (keepConnect) {
                message = receive();
                if (message == null) {
                    keepConnect = false;
                    break;
                }
                Gson json = new Gson();
                status = json.fromJson(message, PlayerStatus.class);
                if (status != null){
                    //controller.refreshInfo(status);
                    //TODO napisac odswiezanie widoku po odebraniu danych
                }

                //System.out.println(status.path);
            }
        }

        public String receive() {
            String msg = "";
            try {
                msg = in.readLine();
            } catch (SocketException e) {
                System.out.println("Connection lost...");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return msg;
        }
    }
}
