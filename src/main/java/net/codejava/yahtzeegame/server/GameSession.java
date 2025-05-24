package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.client.model.ScoreCategory;
import net.codejava.yahtzeegame.client.model.ScoreCalculator;

import java.io.IOException;
import java.util.*;

/**
 iki oyuncu arasında tek bir Yahtzee maçının tüm akışını yöneten sunucu-tarafı iş parçacığı
 */
public class GameSession implements Runnable {

    private final ClientHandler p1, p2;
    private final String name1, name2;


    private static final int ROUNDS = 13;
    private final Map<ScoreCategory, Integer> p1Scores = new EnumMap<>(ScoreCategory.class);
    private final Map<ScoreCategory, Integer> p2Scores = new EnumMap<>(ScoreCategory.class);
    private final boolean[] p1Used = new boolean[ScoreCategory.values().length];
    private final boolean[] p2Used = new boolean[ScoreCategory.values().length];

    private boolean gameActive = true;     // Teslim vs. olmadıkça true


    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.p1 = player1;
        this.p2 = player2;
        this.name1 = p1.getPlayerName();
        this.name2 = p2.getPlayerName();

        p1.listenSurrenderAsync();
        p2.listenSurrenderAsync();
    }

    @Override
    public void run() {
        try {
            sendGameStartMessages();   // #1

            playGame();                // #2 bütün turlar

            // Teslimle bitti ise replay bekleme
            if (!gameActive) return;

            // Doğal bitti her iki oyuncu Play Agai basarsa döngü
            while (handleReplay()) {
                resetGame();
                playGame();
            }
        } catch (Exception e) {
            handleGameError(e);
        } finally {
            cleanup();
        }
    }

    // REPLAY sıfırlama
    private void resetGame() {
        p1Scores.clear();
        p2Scores.clear();
        Arrays.fill(p1Used, false);
        Arrays.fill(p2Used, false);
        gameActive = true;
    }


    // Her iki oyuncudan REPLAY_REQUEST dinleme
    private boolean handleReplay() throws IOException, ClassNotFoundException {
        boolean p1Ready = false, p2Ready = false;

        while (!p1Ready || !p2Ready) {

            // Bağlantı düştüyse iptal
            if (!p1.isActive() || !p2.isActive()) {
                System.out.println("Bir oyuncu koptu, replay iptal.");
                return false;
            }

            // p1’in cevabını oku
            if (!p1Ready) {
                try {
                    Message msg = p1.readMessage();
                    if ("REPLAY_REQUEST".equals(msg.type) && name1.equals(msg.get("playerName"))) {
                        p1Ready = true;
                        System.out.println(name1 + " replay istiyor.");
                    }
                } catch (Exception e) {
                    System.out.println(name1 + " bağlantı hatası, replay iptal.");
                    return false;
                }
            }

            // p2’in cevabını oku
            if (!p2Ready) {
                try {
                    Message msg = p2.readMessage();
                    if ("REPLAY_REQUEST".equals(msg.type) && name2.equals(msg.get("playerName"))) {
                        p2Ready = true;
                        System.out.println(name2 + " replay istiyor.");
                    }
                } catch (Exception e) {
                    System.out.println(name2 + " bağlantı hatası, replay iptal.");
                    return false;
                }
            }
        }
        return true;
    }


    private void sendGameStartMessages() throws IOException {
        Message msg1 = createGameStartMessage(name1, name2);
        msg1.put("youAreP1", true);          // 🔑 eklendi
        Message msg2 = createGameStartMessage(name2, name1);
        msg2.put("youAreP1", false);         // 🔑 eklendi
        p1.sendMessage(msg1);
        p2.sendMessage(msg2);
    }

    private Message createGameStartMessage(String playerName, String opponentName) {
        Message msg = new Message("GAME_START");
        msg.put("playerName", playerName);
        msg.put("opponentName", opponentName);
        return msg;
    }

    private void playGame() throws IOException, ClassNotFoundException {
        Random rnd = new Random();
        int[] dice = new int[5];
        boolean p1Turn = true;

        while (gameActive) {

            /* ① Her döngüde her iki oyuncudan “SURRENDER” geldi mi bak */
            Message maybe = tryRead(p1);
            if (maybe != null && "SURRENDER".equals(maybe.type)) {
                handleSurrender(true);   // p1 teslim, p2 kazanır
                return;
            }
            maybe = tryRead(p2);
            if (maybe != null && "SURRENDER".equals(maybe.type)) {
                handleSurrender(false);  // p2 teslim, p1 kazanır
                return;
            }

            /* ② Normal hamle akışı (yalnız current oyuncudan bloklu bekle) */
            ClientHandler current = p1Turn ? p1 : p2;
            initializeTurn(dice, rnd);
            int rollCount = 0;
            sendTurnUpdates(p1Turn, dice, rollCount);

            boolean turnFinished = false;
            while (!turnFinished && gameActive) {

                Message req = current.readMessage();       // bloklu ama sadece current’ı okur
                if ("ROLL_REQUEST".equals(req.type)) {
                    rollCount = handleRollRequest(req, dice, rnd, rollCount);
                    sendTurnUpdates(p1Turn, dice, rollCount);
                } else if ("CATEGORY_CHOICE".equals(req.type)) {
                    handleCategoryChoice(req, dice,
                            p1Turn ? p1Scores : p2Scores,
                            p1Turn ? p1Used   : p2Used);
                    sendTurnUpdates(p1Turn, dice, rollCount);
                    turnFinished = true;
                } else if ("SURRENDER".equals(req.type)) {
                    handleSurrender(p1Turn);               // current oyuncu teslim
                    return;
                }
            }
            p1Turn = !p1Turn;   // sıra değiştir
        }
    }

    /* Yardımcı: timeout olursa null, veri varsa Message döner */
    private Message tryRead(ClientHandler ch) {
        try { return ch.readMessage(); }
        catch (java.net.SocketTimeoutException e) { return null; }   // veri yok
        catch (Exception e) { return null; }                         // bağlantı sorunu (yok say)
    }




    private void initializeTurn(int[] dice, Random rnd) {
        for (int i = 0; i < 5; i++) dice[i] = rnd.nextInt(6) + 1;
    }

    private int handleRollRequest(Message req, int[] dice, Random rnd, int rollCount) {
        List<Boolean> holds = (List<Boolean>) req.get("holds");
        for (int i = 0; i < 5; i++)
            if (holds == null || !holds.get(i))
                dice[i] = rnd.nextInt(6) + 1;
        return rollCount + 1;
    }

    private void handleCategoryChoice(Message req, int[] dice,
                                      Map<ScoreCategory, Integer> curScores,
                                      boolean[] curUsed) {
        ScoreCategory cat = ScoreCategory.valueOf((String) req.get("category"));
        curScores.put(cat, ScoreCalculator.calculate(cat, dice));
        curUsed[cat.ordinal()] = true;
    }

    // TURN_UPDATE gönderimi
    private void sendTurnUpdates(boolean p1Turn, int[] dice, int rollCount) throws IOException {
        String currentPlayer = p1Turn ? name1 : name2;
        sendTurnUpdate(p1, currentPlayer, dice, p1Scores, p2Scores, p1Turn ? p1Used : p2Used, rollCount);
        sendTurnUpdate(p2, currentPlayer, dice, p1Scores, p2Scores, p1Turn ? p1Used : p2Used, rollCount);
    }

    private void sendTurnUpdate(ClientHandler target, String turnPlayer, int[] dice,
                                Map<ScoreCategory, Integer> p1Scores, Map<ScoreCategory, Integer> p2Scores,
                                boolean[] usedCats, int rollCount) throws IOException {
        Message update = new Message("TURN_UPDATE");
        update.put("currentPlayer", turnPlayer);
        update.put("dice", Arrays.asList(dice[0], dice[1], dice[2], dice[3], dice[4]));
        update.put("p1Scores", new EnumMap<>(p1Scores));
        update.put("p2Scores", new EnumMap<>(p2Scores));
        update.put("usedCategories", usedCats);
        update.put("rollCount", rollCount);
        target.sendMessage(update);
    }

    // Teslim veya doğal bitiş
    private void handleSurrender(boolean isP1Surrendering) throws IOException {
        String winner = isP1Surrendering ? name2 : name1;
        sendGameOver(winner);
        gameActive = false;
    }


    private void endGame() throws IOException {
        int p1Total = calculateTotalScore(p1Scores);
        int p2Total = calculateTotalScore(p2Scores);
        sendGameOver(determineWinner(p1Total, p2Total));
    }

    // Puan hesaplama
    private int calculateTotalScore(Map<ScoreCategory, Integer> scores) {
        int total = scores.values().stream().mapToInt(Integer::intValue).sum();
        int upperSum = Arrays.stream(ScoreCategory.values())
                .limit(6).mapToInt(cat -> scores.getOrDefault(cat, 0)).sum();
        if (upperSum >= 63) total += 35; // Üst bölüm bonusu
        return total;
    }

    private String determineWinner(int p1Total, int p2Total) {
        if (p1Total > p2Total) return name1;
        if (p2Total > p1Total) return name2;
        return "Draw";
    }

    // GAME_OVER gönderimi
    private void sendGameOver(String winner) throws IOException {
        Message end = new Message("GAME_OVER");
        Map<String, Integer> results = new HashMap<>();
        results.put(name1, calculateTotalScore(p1Scores));
        results.put(name2, calculateTotalScore(p2Scores));
        end.put("results", results);
        end.put("winner", winner);
        p1.sendMessage(end);
        p2.sendMessage(end);
    }

    // Hata & temizlik
    private void handleGameError(Exception e) {
        e.printStackTrace();
        try {
            Message err = new Message("GAME_ERROR");
            err.put("message", "A game error occurred");
            p1.sendMessage(err);
            p2.sendMessage(err);
        } catch (IOException ignore) { }
    }

    private void cleanup() {
        try { p1.close(); } catch (Exception ignored) {}
        try { p2.close(); } catch (Exception ignored) {}
    }
}
