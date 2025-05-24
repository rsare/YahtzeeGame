
package net.codejava.yahtzeegame.client.model;

public enum ScoreCategory {
    ONES,           // 1'ler
    TWOS,           // 2'ler
    THREES,         // 3'ler
    FOURS,          // 4'ler
    FIVES,          // 5'ler
    SIXES,          // 6'lar
    THREE_OF_A_KIND,    // Üçlü Bir Tür
    FOUR_OF_A_KIND,     // Dörtlü Bir Tür
    FULL_HOUSE,         // Dolu Ev
    SMALL_STRAIGHT,     // Küçük Düz
    LARGE_STRAIGHT,     // Büyük Düz
    YAHTZEE,            // Yahtzee
    CHANCE;             // Şans

    public String displayName() {
        // İstersen Türkçeleştir, İngilizce de bırakabilirsin
        switch (this) {
            case ONES: return "Ones";
            case TWOS: return "Twos";
            case THREES: return "Threes";
            case FOURS: return "Fours";
            case FIVES: return "Fives";
            case SIXES: return "Sixes";
            case THREE_OF_A_KIND: return "Three of a Kind";
            case FOUR_OF_A_KIND: return "Four of a Kind";
            case FULL_HOUSE: return "Full House";
            case SMALL_STRAIGHT: return "Small Straight";
            case LARGE_STRAIGHT: return "Large Straight";
            case YAHTZEE: return "Yahtzee";
            case CHANCE: return "Chance";
            default: return name();
        }
    }
}
