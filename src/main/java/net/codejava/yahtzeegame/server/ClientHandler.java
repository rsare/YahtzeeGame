package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;

import java.io.*;
import java.net.Socket;

/**
 * ClientHandler – sunucu tarafında tek bir oyuncu soketini saran ve THREAD üzerinde çalışan sınıf
 */
public class ClientHandler implements Runnable {

    //Soket ve akışlar
    private Socket socket;
    private ObjectInputStream  in;
    private ObjectOutputStream out;

    // Oyuncu durumu
    private String  playerName;               // Takma ad
    private boolean active = true;            // Bağlantı HALA açık mı?
    private volatile boolean surrendered = false;  // Teslim oldu mu?


    public void listenSurrenderAsync() {
        new Thread(() -> {
            try {
                while (!surrendered && isActive()) {
                    Message msg = (Message) in.readObject();
                    if ("SURRENDER".equals(msg.type)) {
                        surrendered = true;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }, "surrender-listener-" + playerName).start();
    }


    public boolean hasSurrendered() { return surrendered; }


    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in  = new ObjectInputStream(socket.getInputStream());

        socket.setSoTimeout(300);
        // listenSurrenderAsync();
    }

    @Override
    public void run() {
        try {
            Message firstMessage = readMessage();
            if ("JOIN_GAME".equals(firstMessage.type)) {
                this.playerName = (String) firstMessage.get("playerName");
                System.out.println("[DEBUG] Yeni oyuncu: " + playerName);

                // Eşleştirme kuyruğuna ekle
                synchronized (ServerMain.waitingClients) {
                    ServerMain.waitingClients.add(this);
                    System.out.println("[DEBUG] Bekleme kuyruğuna eklendi: " + playerName);
                    ServerMain.tryPairClients();  // Yeterince oyuncu varsa oyun başlat
                }
            }
        } catch (Exception e) {
            System.out.println("[HATA] İstemci işleme hatası: " + e.getMessage());
            close();
        }
    }


    public boolean isActive() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }


    // Blocking şekilde bir Message okur SURRENDER ise flag set eder
    public Message readMessage() throws IOException, ClassNotFoundException {
        Message msg = (Message) in.readObject();
        if ("SURRENDER".equals(msg.type)) {
            markSurrendered();
        }
        return msg;
    }

    // Thread-safe gerekmiyor oyun iş parçacığı tek seferde çağırıyor
    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public void close() {
        active = false;
        try { in.close();    } catch (Exception ignored) {}
        try { out.close();   } catch (Exception ignored) {}
        try { socket.close();} catch (Exception ignored) {}
    }

    // Getter / Setter
    public void setPlayerName(String name) { this.playerName = name; }

    public String getPlayerName() { return playerName; }

    // Surrender'ı manuel işaretle
    public void markSurrendered() { this.surrendered = true; }
}
