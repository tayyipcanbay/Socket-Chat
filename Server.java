import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<PrintWriter> clientWriters = new ArrayList<>();

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

                ClientHandler clientHandler = new ClientHandler(clientSocket, out);
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
        private String username;

        public ClientHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                this.username = in.readLine();
                broadcast(username + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clientWriters.remove(out);
                if (username != null) {
                    broadcast(username + " has left the chat.");
                }
            }
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }
}
