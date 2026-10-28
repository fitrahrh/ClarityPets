package com.harukiyu.claritypets.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DuelMatch {

    public enum State {
        PREPARING,
        FIGHTING,
        COMPLETED
    }

    private final UUID matchId;
    private final Player player1;
    private final PlayerPetData petData1;
    private final ActivePet activePet1;
    private final ColosseumStance stance1;

    private final Player player2;
    private final PlayerPetData petData2;
    private final ActivePet activePet2;
    private final ColosseumStance stance2;

    private final long startTime;
    private final int maxDurationSeconds;
    private State state;
    private UUID winnerUuid;
    private boolean draw;

    public DuelMatch(@NotNull Player player1, @NotNull PlayerPetData petData1, @NotNull ActivePet activePet1, @NotNull ColosseumStance stance1,
                     @NotNull Player player2, @NotNull PlayerPetData petData2, @NotNull ActivePet activePet2, @NotNull ColosseumStance stance2,
                     int maxDurationSeconds) {
        this.matchId = UUID.randomUUID();
        this.player1 = player1;
        this.petData1 = petData1;
        this.activePet1 = activePet1;
        this.stance1 = stance1;
        this.player2 = player2;
        this.petData2 = petData2;
        this.activePet2 = activePet2;
        this.stance2 = stance2;
        this.startTime = System.currentTimeMillis();
        this.maxDurationSeconds = maxDurationSeconds;
        this.state = State.PREPARING;
        this.winnerUuid = null;
        this.draw = false;
    }

    @NotNull
    public UUID getMatchId() { return matchId; }

    @NotNull
    public Player getPlayer1() { return player1; }
    @NotNull
    public PlayerPetData getPetData1() { return petData1; }
    @NotNull
    public ActivePet getActivePet1() { return activePet1; }
    @NotNull
    public ColosseumStance getStance1() { return stance1; }

    @NotNull
    public Player getPlayer2() { return player2; }
    @NotNull
    public PlayerPetData getPetData2() { return petData2; }
    @NotNull
    public ActivePet getActivePet2() { return activePet2; }
    @NotNull
    public ColosseumStance getStance2() { return stance2; }

    public long getStartTime() { return startTime; }
    public int getMaxDurationSeconds() { return maxDurationSeconds; }

    public boolean isExpired() {
        return (System.currentTimeMillis() - startTime) >= (maxDurationSeconds * 1000L);
    }

    public long getRemainingSeconds() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000L;
        return Math.max(0, maxDurationSeconds - elapsed);
    }

    @NotNull
    public State getState() { return state; }
    public void setState(@NotNull State state) { this.state = state; }

    @Nullable
    public UUID getWinnerUuid() { return winnerUuid; }
    public void setWinnerUuid(@Nullable UUID winnerUuid) { this.winnerUuid = winnerUuid; }

    public boolean isDraw() { return draw; }
    public void setDraw(boolean draw) { this.draw = draw; }

    public boolean involves(@NotNull UUID playerUuid) {
        return player1.getUniqueId().equals(playerUuid) || player2.getUniqueId().equals(playerUuid);
    }

    public boolean involvesEntity(@NotNull UUID entityUuid) {
        return activePet1.getEntity().getUniqueId().equals(entityUuid)
                || activePet2.getEntity().getUniqueId().equals(entityUuid);
    }
}
