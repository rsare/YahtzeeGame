package net.codejava.yahtzeegame.server;

import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.client.model.ScoreCategory;
import net.codejava.yahtzeegame.client.model.ScoreCalculator;

import java.io.IOException;
import java.util.*;

public class GameSession implements Runnable {
    private final ClientHandler p1, p2;
    private final String name1, name2;

    private volatile boolean surrendered = false;

    private static final int ROUNDS = 13;
    private Map<ScoreCategory, Integer> p1Scores = new EnumMap<>(ScoreCategory.class);
    private Map<ScoreCategory, Integer> p2Scores = new EnumMap<>(ScoreCategory.class);
    private boolean[] p1Used = new boolean[ScoreCategory.values().length];
    private boolean[] p2Used = new boolean[ScoreCategory.values().length];
    private boolean gameActive = true;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.p1 = player1;
        this.p2 = player2;
        this.name1 = p1.getPlayerName();
        this.name2 = p2.getPlayerName();
    }

    @Override
    public void run() {
        try {
            sendGameStartMessages();
            playGame();
        } catch (Exception e) {
            handleGameError(e);
        } finally {
            cleanup();
        }
    }

    private void sendGameStartMessages() throws IOException {
        Message msg1 = createGameStartMessage(name1, name2);
        Message msg2 = createGameStartMessage(name2, name1);
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

        // 13 tur x 2 oyuncu
        for (int round = 0; round < ROUNDS * 2 && gameActive; round++) {

            if (p1Turn && p2.hasSurrendered()) {
                handleSurrender(false);
                break;
            }
            if (!p1Turn && p1.hasSurrendered()) {
                handleSurrender(true);
                break;
            }

            ClientHandler current = p1Turn ? p1 : p2;
            initializeTurn(dice, rnd);

            int rollCount = 0;  // Her oyuncunun kendi turunda roll sayısı

            sendTurnUpdates(p1Turn, dice, rollCount);

            boolean turnFinished = false;

            while (!turnFinished && gameActive) {
                Message req = current.readMessage();

                if ("SURRENDER".equals(req.type)) {
                    handleSurrender(p1Turn);
                    return;
                } else if ("ROLL_REQUEST".equals(req.type)) {
                    if (rollCount < 3) {
                        rollCount = handleRollRequest(req, dice, rnd, rollCount);
                        sendTurnUpdates(p1Turn, dice, rollCount);
                    } else {
                        // 3 roll hakkı doldu, roll isteği kabul edilmez, hata mesajı gönderilebilir
                        current.sendMessage(new Message("ERROR", "No rolls left"));
                    }
                } else if ("CATEGORY_CHOICE".equals(req.type)) {
                    handleCategoryChoice(req, dice,
                            p1Turn ? p1Scores : p2Scores,
                            p1Turn ? p1Used : p2Used);
                    sendTurnUpdates(p1Turn, dice, rollCount);
                    turnFinished = true;
                }
            }
            p1Turn = !p1Turn;  // Sırayı değiştir
        }

        if (gameActive) {
            endGame();
        }
    }

    private void initializeTurn(int[] dice, Random rnd) {
        for (int i = 0; i < 5; i++) {
            dice[i] = rnd.nextInt(6) + 1;
        }
    }

    private int handleRollRequest(Message req, int[] dice, Random rnd, int rollCount) {
        List<Boolean> holds = (List<Boolean>) req.get("holds");
        for (int i = 0; i < 5; i++) {
            if (holds == null || !holds.get(i)) {
                dice[i] = rnd.nextInt(6) + 1;
            }
        }
        return rollCount + 1;
    }

    private void handleCategoryChoice(Message req, int[] dice,
                                      Map<ScoreCategory, Integer> curScores,
                                      boolean[] curUsed) {
        String catStr = (String) req.get("category");
        ScoreCategory cat = ScoreCategory.valueOf(catStr);
        int score = ScoreCalculator.calculate(cat, dice);
        curScores.put(cat, score);
        curUsed[cat.ordinal()] = true;
    }

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

    private void handleSurrender(boolean p1Turn) throws IOException {
        String winner = p1Turn ? name2 : name1;
        sendGameOver(winner);
        gameActive = false;
    }

    private void endGame() throws IOException {
        int p1Total = calculateTotalScore(p1Scores);
        int p2Total = calculateTotalScore(p2Scores);
        String winner = determineWinner(p1Total, p2Total);
        sendGameOver(winner);
    }

    private int calculateTotalScore(Map<ScoreCategory, Integer> scores) {
        int total = scores.values().stream().mapToInt(Integer::intValue).sum();
        int upperSum = Arrays.stream(ScoreCategory.values())
                .limit(6) // Üst kategori
                .mapToInt(cat -> scores.getOrDefault(cat, 0))
                .sum();
        if (upperSum >= 63) total += 35; // Bonus
        return total;
    }

    private String determineWinner(int p1Total, int p2Total) {
        if (p1Total > p2Total) return name1;
        else if (p2Total > p1Total) return name2;
        else return "Draw";
    }

    private void sendGameOver(String winner) throws IOException {
        int p1Total = calculateTotalScore(p1Scores);
        int p2Total = calculateTotalScore(p2Scores);

        Message end = new Message("GAME_OVER");
        Map<String, Integer> results = new HashMap<>();
        results.put(name1, p1Total);
        results.put(name2, p2Total);
        end.put("results", results);
        end.put("winner", winner);

        p1.sendMessage(end);
        p2.sendMessage(end);
    }

    private void handleGameError(Exception e) {
        e.printStackTrace();
        try {
            Message errorMsg = new Message("GAME_ERROR");
            errorMsg.put("message", "A game error occurred");
            p1.sendMessage(errorMsg);
            p2.sendMessage(errorMsg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void cleanup() {
        try { p1.close(); } catch (Exception ignored) {}
        try { p2.close(); } catch (Exception ignored) {}
    }
}
