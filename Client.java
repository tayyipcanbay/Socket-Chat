import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Client {
    public static void main(String[] args) {
        new Client().startClient("localhost", 12345);
    }

    public void startClient(String serverAddress, int serverPort) {
        try (
            Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username);

            ExecutorService executorService = Executors.newFixedThreadPool(2);

            // Asenkron görev 1 - Server'dan gelen mesajları oku ve yazdır
            executorService.submit(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("Online users:")) {
                            System.out.println("Online users: " + message.substring("Online users:".length()));
                        } else {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown(); // Hata durumunda executorService'yi kapat
                }
            });

            // Asenkron görev 2 - Kullanıcının girişlerini Server'a gönder
            executorService.submit(() -> {
                try {
                    String userInput;
                    while ((userInput = scanner.nextLine()) != null) {
                        out.println(userInput);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown(); // Kullanıcı giriş yapmayı durdurduğunda executorService'yi kapat
                }
            });

            // executorService'yi kapatmak için bekleyelim
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
