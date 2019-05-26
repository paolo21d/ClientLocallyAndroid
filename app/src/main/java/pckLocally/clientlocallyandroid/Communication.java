package pckLocally.clientlocallyandroid;

import android.net.DhcpInfo;
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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import static android.content.Context.WIFI_SERVICE;

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
    String pin;
    //BufferedReader inFromUser;
    DatagramSocket udpSocket;
    InetAddress IPAddress;
    DatagramPacket receivePacket;
    DatagramPacket sendPacket;

    MainActivity mainActivity;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Communication(MainActivity ma) {
        mainActivity = ma;
        sendThread = new SendThread();
        receiveThread = new ReceiveThread();
        try {
            udpSocket = new DatagramSocket();
//            String ipaddr = "192.168.0.172";
//            IPAddress = InetAddress.getByName(ipaddr);
            IPAddress = getBroadcastAddress();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private InetAddress getBroadcastAddress() {
        InetAddress broadcastAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces();

            while (broadcastAddress == null && networkInterface.hasMoreElements()) {
                NetworkInterface singleInterface = networkInterface.nextElement();
                String interfaceName = singleInterface.getName();
                if (interfaceName.contains("wlan0") || interfaceName.contains("eth0")) {
                    for (InterfaceAddress infaceAddress : singleInterface.getInterfaceAddresses()) {
                        broadcastAddress = infaceAddress.getBroadcast();
                        if (broadcastAddress != null) {
                            break;
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return broadcastAddress;
    }

    public void run() {
        //System.out.print(getBroadcastAddress().toString());
        if (!initConnection()) {
            MainActivity.connected = false;
            return;
        }
        MainActivity.connected = true;
        TCPConnection();

    }

    public boolean initConnection() {
        System.out.println("Communication thread start");

        String sentence = pin;
        sendData = sentence.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, communicationPort);
        try {
            udpSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            udpSocket.setSoTimeout(1500);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        try {
            udpSocket.receive(receivePacket);
        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        IPAddress = receivePacket.getAddress();

        String modifiedSentence = new String(receivePacket.getData());
        modifiedSentence = modifiedSentence.substring(0, 7);
        System.out.println("FROM SERVER: " + modifiedSentence);
        System.out.println(IPAddress);
        udpSocket.close();
        if (modifiedSentence.equals("ERRORCN"))
            return false;
        else
            return true;
    }

    public void TCPConnection() {
        ///TCP
        sendThread.start();
        receiveThread.start();
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

    public void comVolMute() {
        Message message = new Message(MessageType.VOLMUTE);
        sendThread.message = json.toJson(message);
    }

    public void comVolDown() {
        Message message = new Message(MessageType.VOLDOWN);
        sendThread.message = json.toJson(message);
    }

    public void comVolUp() {
        Message message = new Message(MessageType.VOLUP);
        sendThread.message = json.toJson(message);
    }

    public void closeCommunication() {
        if (sendThread != null)
            sendThread.close();
        if (receiveThread != null)
            sendThread.close();
    }

    public void resetCommunication() {
        sendThread.close();
        receiveThread.close();
        sendThread = null;
        receiveThread = null;
    }

    public enum MessageType {
        PLAYPAUSE, NEXT, PREV, REPLAY, LOOP, STATUS, VOLMUTE, VOLDOWN, VOLUP, SETSONG, SETVOLUME
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

        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    //closeCommunication();
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
            mainActivity.closeCommunication();
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

        public void close() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class Message {
        MessageType messageType;
        String message;
        Song song;
        Double volValue;
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
