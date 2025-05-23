package lk.ijse;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client1 {
    private static final String serverAddress = "localhost";
    private static final int serverPort = 4000;
    private static boolean running = true;

    public static void main(String[] args) {
        try (Socket socket = new Socket(serverAddress, serverPort)) {

            System.out.println("Connected to server at " + serverAddress + ":" + serverPort);
            System.out.println("Available commands: TIME, DATE, UPTIME, HELP, BYE");
            System.out.println("Type HELP for more information about commands.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // receive messages from server
            Thread receiverThread = new Thread(new MessageReceiver(in));
            receiverThread.start();

            // send messages to server
            Thread senderThread = new Thread(new MessageSender(out));
            senderThread.start();

            try {
                senderThread.join();

                running = false;
                receiverThread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        }

        System.out.println("Client disconnected.");
    }

    //  handle sending messages
    private static class MessageSender implements Runnable {
        private final PrintWriter out;
        private final Scanner scanner;

        public MessageSender(PrintWriter out) {
            this.out = out;
            this.scanner = new Scanner(System.in);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    System.out.print("Enter command: ");
                    String message = scanner.nextLine().trim();

                    // send the message
                    out.println(message);

                    if (message.equalsIgnoreCase("BYE")) {
                        break;
                    }
                }
            } finally {
                scanner.close();
            }
        }
    }

    private static class MessageReceiver implements Runnable {
        private final BufferedReader in;

        public MessageReceiver(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String response;
                while (running && (response = in.readLine()) != null) {
                    System.out.println("\nServer response : " + response);

                    if (response.contains("Goodbye !")) {
                        break;
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            }
        }
    }
}
