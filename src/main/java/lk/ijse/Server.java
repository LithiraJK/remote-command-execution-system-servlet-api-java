package lk.ijse;

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int port = 4000;
    private static final long startTime = System.currentTimeMillis();

    public static void main(String[] args) {
        // thread pool
        ExecutorService executorService = Executors.newCachedThreadPool();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            System.out.println("Waiting for clients...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected ");

                    // new thread for each client
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    executorService.execute(clientHandler);
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }

//   handle client connections
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    String trimmedInput = inputLine.trim();

                    System.out.println("Command received from Client " + " : " + trimmedInput);

                    String response = processCommand(trimmedInput.toUpperCase());
                    out.println(response);

                    if (trimmedInput.equalsIgnoreCase("BYE")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }

        private String processCommand(String command) {
            switch (command) {
                case "TIME":
                    return "Current server time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                case "DATE":
                    return "Today's date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                case "UPTIME":
                    long uptime = (System.currentTimeMillis() - startTime) / 1000;
                    return "Server uptime: " + uptime + " seconds";
                case "HELP":
                    return "Available commands:\n" +
                           "TIME - Get the current server time\n" +
                           "DATE - Get today's date\n" +
                           "UPTIME - Get the server uptime\n" +
                           "HELP - Display this help message\n" +
                           "BYE - Close the connection";
                case "BYE":
                    return "Goodbye ! ";
                default:
                    return "Unknown command !! Available commands: TIME, DATE, UPTIME, HELP, BYE";
            }
        }

        private void closeConnection() {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Client disconnected. ");
                }
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
