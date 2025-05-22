package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String playerName; // Oyuncu adı veya id
    private boolean active = true; // Bağlantı durumu
    private volatile boolean surrendered = false;


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
        }).start();
    }
    public boolean hasSurrendered() {
        return surrendered;
    }

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        // Out önce!
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            Message firstMessage = readMessage();
            if ("JOIN_GAME".equals(firstMessage.type)) {
                this.playerName = (String) firstMessage.get("playerName");
                System.out.println("[DEBUG] Yeni oyuncu: " + playerName);

                synchronized (ServerMain.waitingClients) {
                    ServerMain.waitingClients.add(this);
                    System.out.println("[DEBUG] Oyuncu bekleme listesine eklendi: " + playerName);
                    ServerMain.tryPairClients();
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



    public Message readMessage() throws IOException, ClassNotFoundException {
        Message msg = (Message) in.readObject();
        if ("SURRENDER".equals(msg.type)) {
            markSurrendered();
        }
        return msg;
    }



    public void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public void close() {
        active = false;
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }



    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void markSurrendered() {
        this.surrendered = true;
    }

}