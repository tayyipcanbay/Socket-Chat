import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    // Bağlı istemcilerin PrintWriter nesnelerini tutan bir liste
    private List<PrintWriter> clientWriters = new ArrayList<>();
    // Bağlı olan kullanıcı adlarını tutan bir liste
    private List<String> connectedUsers = new ArrayList<>();

    public static void main(String[] args) {
        // Sunucu nesnesi oluşturulur ve belirtilen bağlantı noktası ile başlatılır
        new Server().startServer(12345);
    }

    // Sunucuyu başlatan metod
    public void startServer(int port) {
        System.out.println("Sohbet Suncusu Koşuyor..... TUTUN!!");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                // Yeni bir istemci bağlantısı kabul edilir
                Socket clientSocket = serverSocket.accept();
                // Istemciye mesaj göndermek için PrintWriter oluşturulur ve listeye eklenir
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(out);

                // Istemciden gelen kullanıcı adı okunur ve listeye eklenir
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String username = in.readLine();
                connectedUsers.add(username);
                // Yeni kullanıcı bağlandığında tüm kullanıcılara online kullanıcı listesini gönderen metodu çağırır
                broadcast(username + " Gelmiş.Hoşgelmiş!! ");

                // Yeni bir istemci için bir ClientHandler oluşturulur ve bu işlemleri yeni bir thread'da çalıştırır
                ClientHandler clientHandler = new ClientHandler(clientSocket, out, in, username);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Her bir istemci için çalışacak olan ClientHandler sınıfı
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        // ClientHandler yapıcısı
        public ClientHandler(Socket socket, PrintWriter out, BufferedReader in, String username) {
            this.socket = socket;
            this.out = out;
            this.in = in;
            this.username = username;
        }

        // Runnable arayüzünden implement edilen run metodu
        @Override
        public void run() {
            try {
                // Istemcinin IP adresini alır
                String clientIP = socket.getInetAddress().getHostAddress();
                System.out.println(username + " bağlandı.Şu IP üzerinden bağlanıyor: " + clientIP);

                // Yeni kullanıcı bağlandığında tüm kullanıcılara online kullanıcı listesini gönderen metodu çağırır
                broadcast("Çevrimiçi Kullanıcılar: " + String.join(", ", connectedUsers));

                String message;
                // Istemciden gelen mesajları dinleyen döngü
                while ((message = in.readLine()) != null) {
                    // Kullanıcının kendi mesajını göndermemesi için kontrol
                    if (message.equals("/online")) {
                        // Kullanıcı "/online" yazdığında o anda online olan kullanıcıları gönderen metodu çağırır
                        out.println("Çevrimiçi Kullanıcılar: " + String.join(", ", connectedUsers));
                    } else if (message.equals("/help")) {
                        // Kullanıcı "/help" yazdığında yardım mesajını gönderen metodu çağırır
                        out.println("Yazabileceğin Komutlar: /online, /quit, /help, /private <username> <message>, /kick <username> <reason>");
                    } else if (message.startsWith("/kick")) {
                        // Kullanıcı "/kick" yazdığında işlemleri gerçekleştiren blok
                        String[] tokens = message.split(" ");
                        String receiver = tokens[1];
                        String reason = tokens[2];
                        try {
                            // Kullanıcıyı kickleme işlemi
                            PrintWriter receiverWriter = clientWriters.get(connectedUsers.indexOf(receiver));
                            receiverWriter.println("Şu Adam Tarafından Atıldın: " + username + " Sebebi de bu : " + reason);
                            receiverWriter.close();
                        } catch (Exception e) {
                            // Kullanıcı çevrimiçi değilse uyarı mesajı gönderir
                            out.println("Kullanıcı " + receiver + " gitmiş. Bulamadık valla!");
                        }
                    } else if (message.equals("/admin")) {
                        // Kullanıcı "/admin" yazdığında admin mesajını gönderir
                        out.println("BABA GELDİ!!!");
                    } else if (message.equals("/quit")) {
                        // Kullanıcı "/quit" yazdığında bağlantıyı kapatır
                        out.println("Allaha Emanet!!!");
                        break;
                    } else if (message.startsWith("/private")) {
                        // Özel mesaj gönderme işlemleri
                        String[] tokens = message.split(" ");
                        String receiver = tokens[1];
                        String privateMessage = tokens[2];
                        try {
                            // Özel mesajı alıcıya gönderir
                            PrintWriter receiverWriter = clientWriters.get(connectedUsers.indexOf(receiver));
                            receiverWriter.println(username + " (private): " + privateMessage);
                        } catch (Exception e) {
                            // Alıcı çevrimiçi değilse uyarı mesajı gönderir
                            out.println("Kullanıcı " + receiver + " gitmiş. Bulamadık valla!");
                        }
                    } else if (!message.startsWith(username + ":")) {
                        // Diğer durumlarda mesajı tüm kullanıcılara yayınlayan metodu çağırır
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Kullanıcı çıkış yaptığında yapılacak temizlik işlemleri
                connectedUsers.remove(username);
                clientWriters.remove(out);
                broadcast(username + " çıktı. İçeride Kalanlar: " + String.join(", ", connectedUsers));
            }
        }
    }

    // Tüm istemcilere mesaj yayınlayan metot
    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }
}
