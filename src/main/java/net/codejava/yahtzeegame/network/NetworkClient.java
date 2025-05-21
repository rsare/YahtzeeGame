package net.codejava.yahtzeegame.network;

import net.codejava.yahtzeegame.network.Message;

import java.io.*;
import java.net.Socket;

/**
 * Server ile mesaj alışverişini yönetir.
 * Bağlantı kurar, mesaj gönderir, mesaj alır, bağlantıyı kapatır.
 */
public class NetworkClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public NetworkClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        // DİKKAT: Sıra bu şekilde olacak:
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // Mutlaka!
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Server'a mesaj gönderir.
     */
    public synchronized void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    /**
     * Server'dan mesaj alır (blocking).
     */
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        Message msg = (Message) in.readObject();
        System.out.println("NetworkClient: Mesaj alındı: " + msg.type + " | " + msg.data);
        return msg;
    }

    /**
     * Bağlantıyı kapatır.
     */
    public void close() {
        try { in.close(); } catch (Exception ignored) {}
        try { out.close(); } catch (Exception ignored) {}
        try { socket.close(); } catch (Exception ignored) {}
    }

    /**
     * Bağlantı durumu sorgusu.
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
