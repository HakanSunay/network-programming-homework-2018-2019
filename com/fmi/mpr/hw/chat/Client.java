package com.fmi.mpr.hw.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public class Client {
    private static int BUFFER_SIZE = 64000;
    private byte[] buffer;
    private String ipAddr;
    private int portAddr;
    private MulticastSocket multicastSocket;
    private InetAddress groupIpAddr;
    private String username;
    private Random randNum = new Random();

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
        this.listen();
    }

    private void listen(){
        new Thread(() -> {
            while(true){
                try {
                    DatagramPacket packet = new DatagramPacket(this.buffer, this.buffer.length);
                    this.multicastSocket.receive(packet);
                    if (packet.getLength() != 0) {
                        String text = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        String packetType = text.substring(0, 2);
                        if (packetType.equals("#T")) {
                            System.out.println(text.substring(2));
                        } else {
                            this.readFile(packet);
                        }
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


    public void sendFile(String filepath) {
        File file = new File(filepath);
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;

            while ((bytesRead = in.read(buffer, 0, BUFFER_SIZE)) > 0) {
                DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE],
                        bytesRead, this.groupIpAddr, this.portAddr);
                packet.setData(buffer, 0, bytesRead);
                this.multicastSocket.send(packet);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while sending the file!");
            e.printStackTrace();
        }
    }

    private void readFile(DatagramPacket firstPacket){
        String fileName = generateRandomName();
        try {
            File f = new File(fileName);
            FileOutputStream out = new FileOutputStream(f);
            int bytesReceived = 0;
            out.write(firstPacket.getData());
            bytesReceived = firstPacket.getData().length;
            while (bytesReceived > 0 && bytesReceived <= BUFFER_SIZE) {
                this.multicastSocket.receive(firstPacket);
                out.write(firstPacket.getData());
                bytesReceived = firstPacket.getData().length;
            }
            out.close();
            System.out.println("Received " + fileName);
        } catch (IOException e) {
            System.out.println("Failed to receive file!");
            e.printStackTrace();
        }
    }

    private String generateRandomName(){
        return this.username + this.randNum.nextInt();
    }
}