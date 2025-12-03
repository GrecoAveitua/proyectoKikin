package com.example.karatecompetitionmanager.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KataJudgeCalculator {

    public static double calculateKataScore(List<Double> scores) {
        if (scores == null || scores.size() < 3) {
            throw new IllegalArgumentException("Se requieren al menos 3 calificaciones");
        }

        List<Double> sortedScores = new ArrayList<>(scores);
        Collections.sort(sortedScores);

        // Eliminar la más alta y más baja
        sortedScores.remove(0); // más baja
        sortedScores.remove(sortedScores.size() - 1); // más alta

        // Calcular promedio
        double sum = 0;
        for (double score : sortedScores) {
            sum += score;
        }

        return sum / sortedScores.size();
    }

    public static String determineWinner(double score1, double score2) {
        if (score1 > score2) return "COMPETITOR_1";
        else if (score2 > score1) return "COMPETITOR_2";
        else return "TIE";
    }
}