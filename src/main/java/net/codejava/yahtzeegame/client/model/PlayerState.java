package net.codejava.yahtzeegame.client.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Oyuncunun skor tablosunu ve oyun sırasındaki state'ini tutar.
 * Her bir oyuncu için bir PlayerState nesnesi tutulur.
 */
public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Her kategori için alınan puanlar (kategori adı -> puan)
    private Map<String, Integer> scores = new HashMap<>();

    // Her kategoriye puan girildi mi
    private Map<String, Boolean> usedCategories = new HashMap<>();

    // Toplam skor
    private int totalScore = 0;

    // Kaçıncı turda olduğumuzu tutar (1..13)
    private int round = 1;

    // Oyuncunun adı veya ID'si
    private String playerName;

    public PlayerState(String playerName) {
        this.playerName = playerName;
        // Başlangıçta tüm kategoriler kullanılmamış ve skorlar 0'dır
        for (String cat : YahtzeeCategories.ALL_CATEGORIES) {
            scores.put(cat, 0);
            usedCategories.put(cat, false);
        }
    }

    public int getScore(String category) {
        return scores.getOrDefault(category, 0);
    }

    public void setScore(String category, int score) {
        scores.put(category, score);
        usedCategories.put(category, true);
        recalculateTotalScore();
    }

    public boolean isCategoryUsed(String category) {
        return usedCategories.getOrDefault(category, false);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getRound() {
        return round;
    }

    public void nextRound() {
        if (round < 13)
            round++;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isAllCategoriesUsed() {
        return usedCategories.values().stream().allMatch(b -> b);
    }

    // Gerekiyorsa bonus eklemesi burada yapılabilir
    private void recalculateTotalScore() {
        int sum = 0;
        for (int s : scores.values()) sum += s;

        // Üst bölüm bonusu (örnek)
        int upperSum = 0;
        for (String cat : YahtzeeCategories.UPPER_CATEGORIES) {
            upperSum += scores.getOrDefault(cat, 0);
        }
        if (upperSum >= 63) sum += 35;

        this.totalScore = sum;
    }

    // Kategorilere dışarıdan erişmek için statik bir yardımcı inner class:
    public static class YahtzeeCategories {
        public static final String[] UPPER_CATEGORIES = {
                "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"
        };
        public static final String[] LOWER_CATEGORIES = {
                "ThreeOfKind", "FourOfKind", "FullHouse",
                "SmallStraight", "LargeStraight", "Yahtzee", "Chance"
        };
        public static final String[] ALL_CATEGORIES = {
                "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
                "ThreeOfKind", "FourOfKind", "FullHouse",
                "SmallStraight", "LargeStraight", "Yahtzee", "Chance"
        };
    }

    // Skor tablosunu string olarak göstermek için (opsiyonel)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(playerName + " Scores:\n");
        for (String cat : YahtzeeCategories.ALL_CATEGORIES) {
            sb.append(cat).append(": ")
                    .append(scores.get(cat))
                    .append(isCategoryUsed(cat) ? " ✓" : "")
                    .append("\n");
        }
        sb.append("TOTAL: ").append(totalScore);
        return sb.toString();
    }
}