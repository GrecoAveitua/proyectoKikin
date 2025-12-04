package com.example.karatecompetitionmanager.models;

public class Match {
    public Competitor competitor1;
    public Competitor competitor2;
    public Competitor winner;

    public Match(Competitor competitor1, Competitor competitor2) {
        this.competitor1 = competitor1;
        this.competitor2 = competitor2;
        this.winner = null;
    }

    public boolean isComplete() {
        return winner != null;
    }

    public Competitor getLoser() {
        if (winner == null) return null;
        return winner == competitor1 ? competitor2 : competitor1;
    }
}