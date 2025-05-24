
package net.codejava.yahtzeegame.client.model;

import java.util.Arrays;

public class ScoreCalculator {

    public static int calculate(ScoreCategory category, int[] dice) {
        Arrays.sort(dice);
        switch (category) {
            case ONES:      return countNumber(dice, 1) * 1;
            case TWOS:      return countNumber(dice, 2) * 2;
            case THREES:    return countNumber(dice, 3) * 3;
            case FOURS:     return countNumber(dice, 4) * 4;
            case FIVES:     return countNumber(dice, 5) * 5;
            case SIXES:     return countNumber(dice, 6) * 6;
            case THREE_OF_A_KIND:
                return hasSameOfAKind(dice, 3) ? sum(dice) : 0;
            case FOUR_OF_A_KIND:
                return hasSameOfAKind(dice, 4) ? sum(dice) : 0;
            case FULL_HOUSE:
                return isFullHouse(dice) ? 25 : 0;
            case SMALL_STRAIGHT:
                return isSmallStraight(dice) ? 30 : 0;
            case LARGE_STRAIGHT:
                return isLargeStraight(dice) ? 40 : 0;
            case YAHTZEE:
                return hasSameOfAKind(dice, 5) ? 50 : 0;
            case CHANCE:
                return sum(dice);
            default:
                return 0;
        }
    }


    // ---- Yardımcı metotlar ----
    private static int countNumber(int[] dice, int num) {
        int count = 0;
        for (int d : dice) if (d == num) count++;
        return count;
    }

    private static int sum(int[] dice) {
        int s = 0;
        for (int d : dice) s += d;
        return s;
    }

    private static boolean hasSameOfAKind(int[] dice, int count) {
        int[] counts = new int[7]; // 1..6
        for (int d : dice) counts[d]++;
        for (int c : counts) if (c >= count) return true;
        return false;
    }

    private static boolean isFullHouse(int[] dice) {
        int[] counts = new int[7];
        for (int d : dice) counts[d]++;
        boolean has3 = false, has2 = false;
        for (int c : counts) {
            if (c == 3) has3 = true;
            if (c == 2) has2 = true;
        }
        return has3 && has2;
    }

    private static boolean isSmallStraight(int[] dice) {
        // Small straight: Dört ardışık sayı (örn: 1-2-3-4, 2-3-4-5, 3-4-5-6)
        // Çift zarlar varsa da olabilir, bu yüzden set kullanıyoruz
        boolean[] seen = new boolean[7];
        for (int d : dice) seen[d] = true;

        // 1-2-3-4
        if (seen[1] && seen[2] && seen[3] && seen[4]) return true;
        // 2-3-4-5
        if (seen[2] && seen[3] && seen[4] && seen[5]) return true;
        // 3-4-5-6
        if (seen[3] && seen[4] && seen[5] && seen[6]) return true;

        return false;
    }

    private static boolean isLargeStraight(int[] dice) {
        // Large straight: Beş ardışık sayı (örn: 1-2-3-4-5 veya 2-3-4-5-6)
        boolean[] seen = new boolean[7];
        for (int d : dice) seen[d] = true;

        // 1-2-3-4-5
        if (seen[1] && seen[2] && seen[3] && seen[4] && seen[5]) return true;
        // 2-3-4-5-6
        if (seen[2] && seen[3] && seen[4] && seen[5] && seen[6]) return true;

        return false;
    }
}
