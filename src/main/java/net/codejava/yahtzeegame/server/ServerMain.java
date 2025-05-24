package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.System.out;

/**
Yahtzee sunucusunun giriş noktası
 */
public class ServerMain {

    // Sabitler & Paylaşılan Kuyruk
    public static final int PORT = 12345;
    public static final Queue<ClientHandler> waitingClients = new LinkedList<>();


    public static void main(String[] args) {
        out.println("Server running on port " + PORT);

        // try-with-resources: ServerSocket kapanırsa otomatik close
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Ana kabul döngüsü
            while (true) {
                Socket socket = serverSocket.accept();          // Bloklanır
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler, "client-" + socket.getPort()).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//       Kuyruktaki oyuncuları eşleştir
    public static void tryPairClients() {
        synchronized (waitingClients) {
            System.out.println("Bekleyen oyuncular: " + waitingClients.size());

            // Kuyrukta ikiden fazla varsa olası maksimum sayıda eşleşme yap
            while (waitingClients.size() >= 2) {
                ClientHandler p1 = waitingClients.poll();
                ClientHandler p2 = waitingClients.poll();

                // Her iki soket hala aktif mi
                if (p1.isActive() && p2.isActive()) {
                    System.out.println("Eşleştirme yapılıyor: " +
                            p1.getPlayerName() + " ve " + p2.getPlayerName());

                    // Yeni oyun oturumu başlat
                    new Thread(new GameSession(p1, p2),
                            "session-" + p1.getPlayerName() + "-" + p2.getPlayerName()).start();

                } else {
                    System.out.println("Bağlantı hatası – oyuncular eşleştirilemedi");

                    // Bağlantısı kopan oyuncu kuyruğa geri eklenmez
                    if (p1.isActive()) waitingClients.add(p1);
                    if (p2.isActive()) waitingClients.add(p2);
                }
            }
        }
    }
}
