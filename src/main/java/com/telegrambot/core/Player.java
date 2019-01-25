package com.telegrambot.core;


class Player {

    private String playerName;
    private String role;
    private Boolean lebenstrank = false;
    private Boolean todestrank = false;
    private int punkte;
    private int playerId;
    private Boolean hasVoted = false;
    private String hasVotedFor = "";
    private Boolean hasVotedMajor = false;
    private String hasVotedMajorFor = "";
    private int votesFor;
    private int majorVotesFor;

    Player(String name, int id) {
        this.playerName = name;
        this.playerId = id;
    }

    void incrPunkte() {
        punkte++;
    }

    void incrVotesFor() {
        this.votesFor++;
    }

    void incrVotesMajorFor() {
        this.majorVotesFor++;
    }

    int getMajorVotesFor() {
        return majorVotesFor;
    }

    void resetMajorVotesFor() {
        this.majorVotesFor = 0;
    }

    void resetVotesFor() {
        this.votesFor = 0;
    }

    Boolean getHasVotedMajor() {
        return hasVotedMajor;
    }

    String getHasVotedMajorFor() {
        return hasVotedMajorFor;
    }

    void setHasVotedMajor(Boolean hasVotedMajor) {
        this.hasVotedMajor = hasVotedMajor;
    }

    void setHasVotedMajorFor(String hasVotedMajorFor) {
        this.hasVotedMajorFor = hasVotedMajorFor;
    }

    Boolean getHasVoted() {
        return hasVoted;
    }

    String getHasVotedFor() {
        return hasVotedFor;
    }

    int getVotesFor() {
        return votesFor;
    }

    void setLebenstrank() {
        this.lebenstrank = false;
    }

    void setTodestrank() {
        this.todestrank = false;
    }

    void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    void setHasVotedFor(String hasVotedFor) {
        this.hasVotedFor = hasVotedFor;
    }

    String getPlayerName() {
        return playerName;
    }

    String getRole() {
        return role;
    }

    Boolean getLebenstrank() {
        return lebenstrank;
    }

    Boolean getTodestrank() {
        return todestrank;
    }

    int getPunkte() {
        return punkte;
    }

    void setRole(String role) {
        this.role = role;
    }

    void resetPunkte() {
        this.punkte = 0;
    }

    int getPlayerId() {
        return playerId;
    }

    void fillPotions() {
        this.lebenstrank = true;
        this.todestrank = true;
    }
}
