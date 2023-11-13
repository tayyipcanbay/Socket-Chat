import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<PrintWriter> clientWriters = new ArrayList<>();
    private List<String> connectedUsers = new ArrayList<>();

    public static void main(String[] args) {
        new Server().startServer(12345);
    }

    public void startServer(int port) {
        System.out.println("Chat Server is running...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(out);

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String username = in.readLine();
                connectedUsers.add(username);
                broadcast(username + " has joined the chat. ");

                ClientHandler clientHandler = new ClientHandler(clientSocket, out, in, username);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket, PrintWriter out, BufferedReader in, String username) {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.username = username;
        }
        @Override
        public void run() {
            try {
                // Örneğin, bağlantının IP adresini almak için:
                String clientIP = socket.getInetAddress().getHostAddress();
                System.out.println(username + " is connected from IP: " + clientIP);

                // Yeni kullanıcı bağlandığında tüm kullanıcılara online kullanıcı listesini gönder
                broadcast("Online users: " + String.join(", ", connectedUsers));

                String message;
                while ((message = in.readLine()) != null) {
                    // Kullanıcının kendi mesajını göndermemesi için kontrol ekle
                    if (message.equals("/online")) {
                        // Kullanıcı "/online" yazdığında o anda online olan kullanıcıları gönder
                        out.println("Online users: " + String.join(", ", connectedUsers));
                    } else if (message.equals("/help")) {
                        // Kullanıcı "/help" yazdığında yardım mesajını gönder
                        out.println("Available commands: /online, /quit, /help, /private <username> <message>, /kick <username> <reason>");
                    } else if (message.startsWith("/kick")) {
                        String[] tokens = message.split(" ");
                        String receiver = tokens[1];
                        String reason = tokens[2];
                        try {
                            PrintWriter receiverWriter = clientWriters.get(connectedUsers.indexOf(receiver));
                            receiverWriter.println("You have been kicked by " + username + " for " + reason);
                            receiverWriter.close();
                        } catch (Exception e) {
                            out.println("User " + receiver + " is not online.");
                        }
                    } else if (message.equals("/admin")) {
                        // Kullanıcı "/admin" yazdığında admin mesajını gönder
                        out.println("You are an admin!");
                    } else if (message.equals("/quit")) {
                        // Kullanıcı "/quit" yazdığında bağlantıyı kapat
                        out.println("Bye bye!");
                        break;
                    } else if (message.startsWith("/private")) {
                        String[] tokens = message.split(" ");
                        String receiver = tokens[1];
                        String privateMessage = tokens[2];
                        try {
                            PrintWriter receiverWriter = clientWriters.get(connectedUsers.indexOf(receiver));
                            receiverWriter.println(username + " (private): " + privateMessage);
                        } catch (Exception e) {
                            out.println("User " + receiver + " is not online.");
                        
                    }
                    } else if (!message.startsWith(username + ":")) {
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connectedUsers.remove(username);
                clientWriters.remove(out);
                broadcast(username + " has left the chat. Online users: " + String.join(", ", connectedUsers));
            }
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }
}
