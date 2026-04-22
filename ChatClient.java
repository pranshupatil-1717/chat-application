import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;
    private BufferedReader in;
    private PrintWriter out;
    private final Scanner scanner;

    public ChatClient() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            Thread readerThread = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("SUBMITNAME")) {
                            System.out.print("Enter your username: ");
                            String name = scanner.nextLine();
                            out.println(name);
                        } else if (serverMessage.startsWith("NAMEACCEPTED")) {
                            System.out.println("Connected to the robust chat server! Type '/quit' to exit.");
                        } else {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readerThread.start();

            while (true) {
                String message = scanner.nextLine();
                if (message != null && !message.isEmpty()) {
                    out.println(message);
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to connect to the server. Make sure it is running. Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}
