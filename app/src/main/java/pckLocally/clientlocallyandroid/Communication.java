package pckLocally.clientlocallyandroid;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Communication extends Thread {
    private final int communicationPort = 10000;
    private final int communicationPortTCP = 10001;
    //musi byc na odwrot receivePort i sendPort niz w aplikacji servera
    private final int sendPort = 10003;
    private final int receivePort = 10002;
    SendThread sendThread;
    ReceiveThread receiveThread;
    PlayerStatus status;
    boolean keepConnect = true;
    Gson json = new Gson();

    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];
    //BufferedReader inFromUser;
    DatagramSocket udpSocket;
    InetAddress IPAddress;
    DatagramPacket receivePacket;
    DatagramPacket sendPacket;

    MainActivity mainActivity;


    //TODO jakos przekazac activity aby zrobic refresh
    public Communication(MainActivity ma) {
        mainActivity = ma;
        sendThread = new SendThread();
        receiveThread = new ReceiveThread();
        try {
            udpSocket = new DatagramSocket();
            String ipaddr = "192.168.0.172";
//            String ipaddr = getBroadcast();
            IPAddress = InetAddress.getByName(ipaddr);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            initConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TCPConnection();

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

    public void TCPConnection() {
        ///TCP
        sendThread.start();
        receiveThread.start();
    }

    private String getBroadcast() throws UnknownHostException, SocketException {
        InetAddress IP = InetAddress.getLocalHost();
        String ip = new String(IP.getHostAddress());
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
    public void comPlayPause() throws Exception {
        Message message = new Message(MessageType.PLAYPAUSE);
        sendThread.message = json.toJson(message);
    }

    public void comNext() {
        Message message = new Message(MessageType.NEXT);
        sendThread.message = json.toJson(message);
    }

    public void comPrev() {
        Message message = new Message(MessageType.PREV);
        sendThread.message = json.toJson(message);
    }

    public void comReplay() {
        Message message = new Message(MessageType.REPLAY);
        sendThread.message = json.toJson(message);
    }

    public void comLoop() {
        Message message = new Message(MessageType.LOOP);
        sendThread.message = json.toJson(message);
    }

    public enum MessageType {
        PLAYPAUSE, NEXT, PREV, REPLAY, LOOP, STATUS
    }

    class SendThread extends Thread { //TODO mozna przerobi zeby po wyslaniu sie usypial a jak sie chce wyslac to budzic do signalem
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
        String msg;
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
                msg = receive();
                if (msg == null) {
                    keepConnect = false;
                    break;
                }
                Gson json = new Gson();
                //status = json.fromJson(message, PlayerStatus.class);
                Message message = json.fromJson(msg, Message.class);
                if (message != null && message.messageType == MessageType.STATUS) {
                    //controller.refreshInfo(message.statusMessage);
                    mainActivity.refreshInfo(message.statusMessage);
                    //System.out.println(message.statusMessage.title);
                }
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

    public class Message {
        MessageType messageType;
        String message;
        PlayerStatus statusMessage;

        public Message(MessageType type, PlayerStatus st) {
            messageType = type;
            statusMessage = st;
        }

        public Message(MessageType type) {
            messageType = type;
        }
    }
}
