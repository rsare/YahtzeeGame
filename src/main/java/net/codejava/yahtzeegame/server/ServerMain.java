package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.System.out;

public class ServerMain {
    public static final int PORT = 5000;
    public static final Queue<ClientHandler> waitingClients = new LinkedList<>();



    public static void main(String[] args) {
        out.println("Server running on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void tryPairClients() {
        synchronized (waitingClients) {
            System.out.println("Bekleyen oyuncular: " + waitingClients.size());

            while (waitingClients.size() >= 2) {
                ClientHandler p1 = waitingClients.poll();
                ClientHandler p2 = waitingClients.poll();

                // Bağlantıları kontrol et
                if (p1.isActive() && p2.isActive()) {
                    System.out.println("Eşleştirme yapılıyor: " +
                            p1.getPlayerName() + " ve " + p2.getPlayerName());

                    // Yeni oyun başlat
                    new Thread(new GameSession(p1, p2)).start();
                } else {
                    System.out.println("Bağlantı hatası - oyuncular eşleştirilemedi");
                }
            }
        }
    }


}
