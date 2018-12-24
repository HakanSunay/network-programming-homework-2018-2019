package com.fmi.mpr.hw.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {
    private static int BUFFER_SIZE = 1024;
    private byte[] buffer;
    private String ipAddr;
    private int portAddr;
    private MulticastSocket multicastSocket;
    private InetAddress groupIpAddr;
    private String username;

    public Client(String multicastIpAddr, int multicastPort, String name){
        this.ipAddr = multicastIpAddr;
        this.portAddr = multicastPort;
        this.username = name;
        this.buffer = new byte[BUFFER_SIZE];
        try {
            this.multicastSocket = new MulticastSocket(this.portAddr);
            this.groupIpAddr = InetAddress.getByName(this.ipAddr);
            this.multicastSocket.joinGroup(this.groupIpAddr);
        } catch (IOException e) {
            System.out.println("Make sure your address is multicast compatible!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void listen(){
        new Thread(() -> {
            while(true){
                try {
                    DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
                    this.multicastSocket.receive(packet);
                    String text = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    String packetType = text.substring(0,2);
                    switch (packetType) {
                        case "#T":
                            System.out.println(text.substring(2));
                            break;
                        default:
                            // this.readFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }

    public void sendText(String text){
        String msgToBeSent = "#T" + this.username + ": " + text;
        byte[] bytesToBeSent = msgToBeSent.getBytes();
        DatagramPacket packet = new DatagramPacket(bytesToBeSent,
                bytesToBeSent.length, this.groupIpAddr, this.portAddr);
        try {
            this.multicastSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Unexpected error!");
            e.printStackTrace();
        }
    }
}