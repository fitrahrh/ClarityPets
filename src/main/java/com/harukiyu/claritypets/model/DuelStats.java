package com.harukiyu.claritypets.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DuelStats {

    private final UUID playerUuid;
    private int wins;
    private int losses;
    private int draws;
    private int winStreak;
    private int bestStreak;
    private int rating;

    public DuelStats(@NotNull UUID playerUuid, int wins, int losses, int draws, int winStreak, int bestStreak, int rating) {
        this.playerUuid = playerUuid;
        this.wins = Math.max(0, wins);
        this.losses = Math.max(0, losses);
        this.draws = Math.max(0, draws);
        this.winStreak = Math.max(0, winStreak);
        this.bestStreak = Math.max(0, bestStreak);
        this.rating = Math.max(0, rating);
    }

    public static DuelStats createDefault(@NotNull UUID playerUuid) {
        return new DuelStats(playerUuid, 0, 0, 0, 0, 0, 1000);
    }

    @NotNull
    public UUID getPlayerUuid() { return playerUuid; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public int getTotalDuels() {
        return wins + losses + draws;
    }

    public double getWinRate() {
        int total = getTotalDuels();
        return total == 0 ? 0.0 : ((double) wins / total) * 100.0;
    }

    public void recordWin(int ratingChange) {
        wins++;
        winStreak++;
        if (winStreak > bestStreak) {
            bestStreak = winStreak;
        }
        rating += ratingChange;
    }

    public void recordLoss(int ratingChange) {
        losses++;
        winStreak = 0;
        rating = Math.max(100, rating - ratingChange);
    }

    public void recordDraw() {
        draws++;
        winStreak = 0;
    }
}
