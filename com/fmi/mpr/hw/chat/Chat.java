package com.fmi.mpr.hw.chat;

import java.util.Scanner;

public class Chat {

    public static String IP = "224.0.0.1";
    public static int PORT = 1234;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please, choose a name: ");
        String clientName = scanner.nextLine();
        Client User = new Client(IP, PORT, clientName);
        System.out.println(String.format("\n%s, welcome to the chat room!\n", clientName));
        User.listen();
        while(true) {
            System.out.println("Please, choose what you want to send: [TEXT/IMAGE/VIDEO] ");
            String messageType = scanner.nextLine();
            String consoleInput;
            switch (messageType) {
                case "TEXT":
                    System.out.println("Please, type in the text you want to send!");
                    consoleInput = scanner.nextLine();
                    User.sendText(consoleInput);
                    break;
                case "IMAGE":
                case "VIDEO":
                    System.out.println("Please, type in the path to your file!");
                    // consoleInput = scanner.nextLine();
                    // User.sendFile(consoleInput);
                    break;
                default:
                        System.out.println("Please, use CAPITAL LETTERS only!");
            }
        }
    }
}
