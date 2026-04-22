import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8080;
    private static final Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("SUBMITNAME");
                clientName = in.readLine();
                if (clientName == null || clientName.isEmpty()) {
                    return;
                }

                System.out.println(clientName + " connected.");
                out.println("NAMEACCEPTED " + clientName);
                
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                broadcastMessage("Server: User " + clientName + " joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        break;
                    }
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                System.out.println(clientName + " connection issue: " + e.getMessage());
            } finally {
                if (clientName != null) {
                    System.out.println(clientName + " disconnected.");
                    broadcastMessage("Server: User " + clientName + " left the chat.");
                }
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
