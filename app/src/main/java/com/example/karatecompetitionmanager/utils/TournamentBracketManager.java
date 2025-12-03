package com.example.karatecompetitionmanager.utils;

import com.example.karatecompetitionmanager.models.Competitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TournamentBracketManager {

    private List<Competitor> competitors;
    private Map<String, Competitor> bracketResults;
    private List<String> winners;
    private List<String> roundMatches;

    public TournamentBracketManager(List<Competitor> competitors) {
        this.competitors = competitors;
        this.bracketResults = new HashMap<>();
        this.winners = new ArrayList<>();
        this.roundMatches = new ArrayList<>();
        initializeBracket();
    }

    private void initializeBracket() {
        // Crear emparejamientos iniciales
        for (int i = 0; i < competitors.size(); i += 2) {
            if (i + 1 < competitors.size()) {
                String matchKey = competitors.get(i).getFolio() + " vs " +
                        competitors.get(i + 1).getFolio();
                roundMatches.add(matchKey);
            }
        }
    }

    public void advanceWinner(String competitorFolio, String matchKey) {
        Competitor winner = findCompetitorByFolio(competitorFolio);
        if (winner != null) {
            bracketResults.put(matchKey, winner);
            winners.add(competitorFolio);
        }
    }

    private Competitor findCompetitorByFolio(String folio) {
        for (Competitor c : competitors) {
            if (c.getFolio().equals(folio)) return c;
        }
        return null;
    }

    public List<String> getCurrentRoundMatches() {
        return new ArrayList<>(roundMatches);
    }

    public boolean isTournamentComplete() {
        return winners.size() == 1;
    }

    public Competitor getChampion() {
        if (isTournamentComplete()) {
            return findCompetitorByFolio(winners.get(0));
        }
        return null;
    }

    public void nextRound() {
        if (winners.size() <= 1) return;

        roundMatches.clear();
        List<String> currentWinners = new ArrayList<>(winners);
        winners.clear();

        for (int i = 0; i < currentWinners.size(); i += 2) {
            if (i + 1 < currentWinners.size()) {
                String matchKey = currentWinners.get(i) + " vs " + currentWinners.get(i + 1);
                roundMatches.add(matchKey);
            }
        }
    }
}