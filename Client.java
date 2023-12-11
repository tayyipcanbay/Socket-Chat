import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) {
        // Client sınıfının nesnesi oluşturulur ve startClient metodu çağrılarak sunucu adresi ve port numarası belirtilir
        new Client().startClient("localhost", 12345);
    }

    // Client'ın sunucu ile iletişimini başlatan metod
    public void startClient(String serverAddress, int serverPort) {
        try (
            // Socket, PrintWriter ve BufferedReader try-with-resources bloğu içinde tanımlanır
            Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in)
        ) {
            // Kullanıcıdan kullanıcı adı alınır ve sunucuya gönderilir
            System.out.print("Kullanıcı Adınızı Giriniz: ");
            String username = scanner.nextLine();
            out.println(username);

            // Asenkron görevleri yönetmek için ExecutorService oluşturulur
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            // Asenkron görev 1 - Sunucudan gelen mesajları oku ve yazdır
            executorService.submit(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        // Sunucudan gelen mesajı işler

                        if (message.startsWith("Çevrimiçi Kullanıcılar")) {
                            // Eğer mesaj "Online users:" ile başlıyorsa, çevrimiçi kullanıcı listesini gösterir
                            System.out.println("Çevrimiçi Kullanıcılar: " + message.substring("Çevrimiçi Kullanıcılar:".length()));
                        } else {
                            // Diğer durumlarda gelen mesajı doğrudan ekrana yazdırır
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown(); // Hata durumunda executorService'yi kapat
                }
            });

            // Asenkron görev 2 - Kullanıcının girişlerini sunucuya gönder
            executorService.submit(() -> {
                try {
                    String userInput;
                    while ((userInput = scanner.nextLine()) != null) {
                        // Kullanıcının girişini sunucuya gönderir
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
