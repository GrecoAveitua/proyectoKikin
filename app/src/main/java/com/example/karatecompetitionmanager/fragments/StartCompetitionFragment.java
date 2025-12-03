package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Category;
import com.example.karatecompetitionmanager.models.Competition;
import com.example.karatecompetitionmanager.models.Competitor;
import com.example.karatecompetitionmanager.utils.KataJudgeCalculator;
import com.example.karatecompetitionmanager.utils.TournamentBracketManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartCompetitionFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etCategoryFolio;
    private Button btnStartCompetition;
    private LinearLayout layoutBracket;
    private TextView tvCategoryInfo;

    private Category currentCategory;
    private List<Competitor> participants;
    private TournamentBracketManager bracketManager;
    private Competition currentCompetition;

    private int currentMatchIndex = 0;
    private List<String> currentRoundMatches;
    private List<String> roundWinners;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_competition, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etCategoryFolio = view.findViewById(R.id.et_category_folio);
        btnStartCompetition = view.findViewById(R.id.btn_start_competition);
        layoutBracket = view.findViewById(R.id.layout_bracket);
        tvCategoryInfo = view.findViewById(R.id.tv_category_info);

        btnStartCompetition.setOnClickListener(v -> startCompetition());

        return view;
    }

    private void startCompetition() {
        String folio = etCategoryFolio.getText().toString().trim();

        if (folio.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese el folio de la categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentCategory = dbHelper.getCategory(folio);

        if (currentCategory == null) {
            Toast.makeText(getContext(), "Categoría no encontrada",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        loadParticipants();

        if (participants.isEmpty()) {
            Toast.makeText(getContext(), "No hay competidores para esta categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (participants.size() < 2) {
            Toast.makeText(getContext(), "Se requieren al menos 2 competidores",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        displayCategoryInfo();
        initializeTournament();
        startNextMatch();
    }

    private void loadParticipants() {
        participants = new ArrayList<>();
        List<Competitor> allCompetitors = dbHelper.getAllCompetitors("age", true);

        for (Competitor competitor : allCompetitors) {
            boolean matches = false;

            if (competitor.getBelt().equals(currentCategory.getBelt()) &&
                    competitor.getAge() >= currentCategory.getMinAge() &&
                    competitor.getAge() <= currentCategory.getMaxAge()) {

                if (currentCategory.getType().equals("kata") && competitor.isParticipateKata()) {
                    matches = true;
                } else if (currentCategory.getType().equals("kumite") && competitor.isParticipateKumite()) {
                    matches = true;
                } else if (currentCategory.getType().equals("both")) {
                    matches = true;
                }
            }

            if (matches) {
                participants.add(competitor);
            }
        }
    }

    private void displayCategoryInfo() {
        String type = currentCategory.getType().equals("kata") ? "Kata" :
                (currentCategory.getType().equals("kumite") ? "Kumite" : "Kata y Kumite");

        String info = "Categoría: " + currentCategory.getFolio() + "\n" +
                "Cinturón: " + currentCategory.getBelt() + "\n" +
                "Edad: " + currentCategory.getMinAge() + "-" + currentCategory.getMaxAge() + "\n" +
                "Tipo: " + type + "\n" +
                "Participantes: " + participants.size();

        tvCategoryInfo.setText(info);
        tvCategoryInfo.setVisibility(View.VISIBLE);
    }

    private void initializeTournament() {
        bracketManager = new TournamentBracketManager(participants);
        currentRoundMatches = bracketManager.getCurrentRoundMatches();
        roundWinners = new ArrayList<>();
        currentMatchIndex = 0;

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        currentCompetition = new Competition(0, currentCategory.getFolio(), date,
                "IN_PROGRESS", null, null, null);
        dbHelper.insertCompetition(currentCompetition);

        displayBracket();
    }

    private void displayBracket() {
        layoutBracket.removeAllViews();

        TextView tvRound = new TextView(getContext());
        tvRound.setText("RONDA ACTUAL");
        tvRound.setTextSize(18);
        tvRound.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvRound.setPadding(0, 20, 0, 20);
        layoutBracket.addView(tvRound);

        for (String match : currentRoundMatches) {
            TextView tvMatch = new TextView(getContext());
            tvMatch.setText(match);
            tvMatch.setTextSize(16);
            tvMatch.setPadding(20, 10, 20, 10);
            layoutBracket.addView(tvMatch);
        }

        if (!roundWinners.isEmpty()) {
            TextView tvWinners = new TextView(getContext());
            tvWinners.setText("\nGanadores de esta ronda:");
            tvWinners.setTextSize(16);
            tvWinners.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvWinners.setPadding(0, 20, 0, 10);
            layoutBracket.addView(tvWinners);

            for (String winner : roundWinners) {
                TextView tvWinner = new TextView(getContext());
                tvWinner.setText("✓ " + winner);
                tvWinner.setTextSize(14);
                tvWinner.setPadding(30, 5, 20, 5);
                layoutBracket.addView(tvWinner);
            }
        }
    }

    private void startNextMatch() {
        if (currentMatchIndex >= currentRoundMatches.size()) {
            if (roundWinners.size() == 1) {
                finishTournament();
            } else {
                startNextRound();
            }
            return;
        }

        String match = currentRoundMatches.get(currentMatchIndex);
        String[] competitors = match.split(" vs ");

        if (competitors.length != 2) {
            currentMatchIndex++;
            startNextMatch();
            return;
        }

        Competitor comp1 = findCompetitorByFolio(competitors[0].trim());
        Competitor comp2 = findCompetitorByFolio(competitors[1].trim());

        if (comp1 == null || comp2 == null) {
            currentMatchIndex++;
            startNextMatch();
            return;
        }

        if (currentCategory.getType().equals("kata")) {
            showKataDialog(comp1, comp2);
        } else {
            showKumiteDialog(comp1, comp2);
        }
    }

    private void showKataDialog(Competitor comp1, Competitor comp2) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_kata_judging, null);

        TextView tvCompetitor1 = dialogView.findViewById(R.id.tv_kata_competitor1);
        TextView tvCompetitor2 = dialogView.findViewById(R.id.tv_kata_competitor2);
        EditText etJudges = dialogView.findViewById(R.id.et_num_judges);
        Button btnJudgeComp1 = dialogView.findViewById(R.id.btn_judge_comp1);
        Button btnJudgeComp2 = dialogView.findViewById(R.id.btn_judge_comp2);

        tvCompetitor1.setText(comp1.getName() + " (" + comp1.getFolio() + ")");
        tvCompetitor2.setText(comp2.getName() + " (" + comp2.getFolio() + ")");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Competencia de Kata")
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final double[] score1 = {0};
        final double[] score2 = {0};

        btnJudgeComp1.setOnClickListener(v -> {
            String numJudgesStr = etJudges.getText().toString().trim();
            if (numJudgesStr.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese número de jueces (3, 5 o 7)",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int numJudges = Integer.parseInt(numJudgesStr);
            if (numJudges != 3 && numJudges != 5 && numJudges != 7) {
                Toast.makeText(getContext(), "Solo se permiten 3, 5 o 7 jueces",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showJudgeScoresDialog(comp1, numJudges, score -> {
                score1[0] = score;
                btnJudgeComp1.setText("✓ " + String.format("%.2f", score));
                btnJudgeComp1.setEnabled(false);

                if (score2[0] > 0) {
                    determineKataWinner(comp1, comp2, score1[0], score2[0], dialog);
                }
            });
        });

        btnJudgeComp2.setOnClickListener(v -> {
            String numJudgesStr = etJudges.getText().toString().trim();
            if (numJudgesStr.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese número de jueces (3, 5 o 7)",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int numJudges = Integer.parseInt(numJudgesStr);
            if (numJudges != 3 && numJudges != 5 && numJudges != 7) {
                Toast.makeText(getContext(), "Solo se permiten 3, 5 o 7 jueces",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showJudgeScoresDialog(comp2, numJudges, score -> {
                score2[0] = score;
                btnJudgeComp2.setText("✓ " + String.format("%.2f", score));
                btnJudgeComp2.setEnabled(false);

                if (score1[0] > 0) {
                    determineKataWinner(comp1, comp2, score1[0], score2[0], dialog);
                }
            });
        });

        dialog.show();
    }

    private void showJudgeScoresDialog(Competitor competitor, int numJudges,
                                       ScoreCallback callback) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_judge_scores, null);

        LinearLayout scoresLayout = dialogView.findViewById(R.id.layout_scores);

        List<EditText> scoreFields = new ArrayList<>();
        for (int i = 0; i < numJudges; i++) {
            EditText etScore = new EditText(getContext());
            etScore.setHint("Juez " + (i + 1) + " (0-10)");
            etScore.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            scoresLayout.addView(etScore);
            scoreFields.add(etScore);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Calificaciones - " + competitor.getName())
                .setView(dialogView)
                .setPositiveButton("Calcular", (d, w) -> {
                    List<Double> scores = new ArrayList<>();
                    for (EditText et : scoreFields) {
                        String scoreStr = et.getText().toString().trim();
                        if (!scoreStr.isEmpty()) {
                            scores.add(Double.parseDouble(scoreStr));
                        }
                    }

                    if (scores.size() == numJudges) {
                        double finalScore = KataJudgeCalculator.calculateKataScore(scores);
                        callback.onScoreCalculated(finalScore);
                    } else {
                        Toast.makeText(getContext(), "Complete todas las calificaciones",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void determineKataWinner(Competitor comp1, Competitor comp2,
                                     double score1, double score2, AlertDialog dialog) {
        String winner;
        if (score1 > score2) {
            winner = comp1.getFolio();
            Toast.makeText(getContext(), comp1.getName() + " gana con " +
                    String.format("%.2f", score1), Toast.LENGTH_LONG).show();
        } else if (score2 > score1) {
            winner = comp2.getFolio();
            Toast.makeText(getContext(), comp2.getName() + " gana con " +
                    String.format("%.2f", score2), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Empate - se requiere desempate",
                    Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            showKataDialog(comp1, comp2);
            return;
        }

        roundWinners.add(winner);
        bracketManager.advanceWinner(winner, currentRoundMatches.get(currentMatchIndex));
        currentMatchIndex++;
        dialog.dismiss();
        displayBracket();
        startNextMatch();
    }

    private void showKumiteDialog(Competitor comp1, Competitor comp2) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_kumite_match, null);

        TextView tvComp1Name = dialogView.findViewById(R.id.tv_kumite_comp1_name);
        TextView tvComp2Name = dialogView.findViewById(R.id.tv_kumite_comp2_name);
        TextView tvComp1Score = dialogView.findViewById(R.id.tv_kumite_comp1_score);
        TextView tvComp2Score = dialogView.findViewById(R.id.tv_kumite_comp2_score);
        TextView tvTimer = dialogView.findViewById(R.id.tv_kumite_timer);

        Button btnComp1Ippon = dialogView.findViewById(R.id.btn_comp1_ippon);
        Button btnComp1Wazari = dialogView.findViewById(R.id.btn_comp1_wazari);
        Button btnComp1Yuko = dialogView.findViewById(R.id.btn_comp1_yuko);
        Button btnComp1Foul = dialogView.findViewById(R.id.btn_comp1_foul);

        Button btnComp2Ippon = dialogView.findViewById(R.id.btn_comp2_ippon);
        Button btnComp2Wazari = dialogView.findViewById(R.id.btn_comp2_wazari);
        Button btnComp2Yuko = dialogView.findViewById(R.id.btn_comp2_yuko);
        Button btnComp2Foul = dialogView.findViewById(R.id.btn_comp2_foul);

        Button btnStartStop = dialogView.findViewById(R.id.btn_start_stop_timer);
        Button btnAdd5Sec = dialogView.findViewById(R.id.btn_add_5_sec);
        Button btnSubtract5Sec = dialogView.findViewById(R.id.btn_subtract_5_sec);
        Button btnFinish = dialogView.findViewById(R.id.btn_finish_match);

        EditText etMatchTime = dialogView.findViewById(R.id.et_match_time);
        EditText etScoreLimit = dialogView.findViewById(R.id.et_score_limit);

        tvComp1Name.setText(comp1.getName());
        tvComp2Name.setText(comp2.getName());

        final int[] score1 = {0};
        final int[] score2 = {0};
        final int[] fouls1 = {0};
        final int[] fouls2 = {0};
        final long[] timeLeft = {120000};
        final boolean[] timerRunning = {false};
        final Handler handler = new Handler();

        AlertDialog kumiteDialog = new AlertDialog.Builder(getContext())
                .setTitle("Combate Kumite")
                .setView(dialogView)
                .setCancelable(false)
                .create();

        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerRunning[0] && timeLeft[0] > 0) {
                    timeLeft[0] -= 1000;
                    int minutes = (int) (timeLeft[0] / 60000);
                    int seconds = (int) ((timeLeft[0] % 60000) / 1000);
                    tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);

                    if (timeLeft[0] <= 0) {
                        timerRunning[0] = false;
                        checkKumiteWinner(comp1, comp2, score1[0], score2[0],
                                fouls1[0], fouls2[0], kumiteDialog);
                    }
                }
            }
        };

        btnStartStop.setOnClickListener(v -> {
            if (!timerRunning[0]) {
                String timeStr = etMatchTime.getText().toString().trim();
                if (!timeStr.isEmpty() && timeLeft[0] == 120000) {
                    timeLeft[0] = Integer.parseInt(timeStr) * 1000;
                }
                timerRunning[0] = true;
                handler.post(timerRunnable);
                btnStartStop.setText("Pausar");
            } else {
                timerRunning[0] = false;
                handler.removeCallbacks(timerRunnable);
                btnStartStop.setText("Reanudar");
            }
        });

        btnAdd5Sec.setOnClickListener(v -> {
            timeLeft[0] += 5000;
            int minutes = (int) (timeLeft[0] / 60000);
            int seconds = (int) ((timeLeft[0] % 60000) / 1000);
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        });

        btnSubtract5Sec.setOnClickListener(v -> {
            timeLeft[0] -= 5000;
            if (timeLeft[0] < 0) timeLeft[0] = 0;
            int minutes = (int) (timeLeft[0] / 60000);
            int seconds = (int) ((timeLeft[0] % 60000) / 1000);
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        });

        btnComp1Ippon.setOnClickListener(v -> {
            score1[0] += 3;
            tvComp1Score.setText(String.valueOf(score1[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp1Wazari.setOnClickListener(v -> {
            score1[0] += 2;
            tvComp1Score.setText(String.valueOf(score1[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp1Yuko.setOnClickListener(v -> {
            score1[0] += 1;
            tvComp1Score.setText(String.valueOf(score1[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp1Foul.setOnClickListener(v -> {
            fouls1[0]++;
            if (fouls1[0] >= 4) {
                endKumiteMatch(comp2.getFolio(), comp2.getName() + " gana por descalificación",
                        kumiteDialog, handler, timerRunnable);
            }
        });

        btnComp2Ippon.setOnClickListener(v -> {
            score2[0] += 3;
            tvComp2Score.setText(String.valueOf(score2[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp2Wazari.setOnClickListener(v -> {
            score2[0] += 2;
            tvComp2Score.setText(String.valueOf(score2[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp2Yuko.setOnClickListener(v -> {
            score2[0] += 1;
            tvComp2Score.setText(String.valueOf(score2[0]));
            checkScoreLimit(etScoreLimit, score1[0], score2[0], comp1, comp2,
                    fouls1[0], fouls2[0], kumiteDialog, handler, timerRunnable);
        });

        btnComp2Foul.setOnClickListener(v -> {
            fouls2[0]++;
            if (fouls2[0] >= 4) {
                endKumiteMatch(comp1.getFolio(), comp1.getName() + " gana por descalificación",
                        kumiteDialog, handler, timerRunnable);
            }
        });

        btnFinish.setOnClickListener(v -> {
            timerRunning[0] = false;
            handler.removeCallbacks(timerRunnable);
            checkKumiteWinner(comp1, comp2, score1[0], score2[0], fouls1[0], fouls2[0],
                    kumiteDialog);
        });

        kumiteDialog.show();
    }

    private void checkScoreLimit(EditText etScoreLimit, int score1, int score2,
                                 Competitor comp1, Competitor comp2,
                                 int fouls1, int fouls2, AlertDialog dialog,
                                 Handler handler, Runnable timerRunnable) {
        String limitStr = etScoreLimit.getText().toString().trim();
        if (!limitStr.isEmpty()) {
            int limit = Integer.parseInt(limitStr);
            if (score1 >= limit || score2 >= limit) {
                handler.removeCallbacks(timerRunnable);
                checkKumiteWinner(comp1, comp2, score1, score2, fouls1, fouls2, dialog);
            }
        }
    }

    private void checkKumiteWinner(Competitor comp1, Competitor comp2,
                                   int score1, int score2, int fouls1, int fouls2,
                                   AlertDialog dialog) {
        if (score1 > score2) {
            endKumiteMatch(comp1.getFolio(), comp1.getName() + " gana " + score1 + "-" + score2,
                    dialog, null, null);
        } else if (score2 > score1) {
            endKumiteMatch(comp2.getFolio(), comp2.getName() + " gana " + score2 + "-" + score1,
                    dialog, null, null);
        } else {
            Toast.makeText(getContext(), "Empate - Segundo enfrentamiento",
                    Toast.LENGTH_LONG).show();
            dialog.dismiss();
            showKumiteDialog(comp1, comp2);
        }
    }

    private void endKumiteMatch(String winnerFolio, String message, AlertDialog dialog,
                                Handler handler, Runnable timerRunnable) {
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        roundWinners.add(winnerFolio);
        bracketManager.advanceWinner(winnerFolio, currentRoundMatches.get(currentMatchIndex));
        currentMatchIndex++;

        dialog.dismiss();
        displayBracket();
        startNextMatch();
    }

    private void startNextRound() {
        bracketManager.nextRound();
        currentRoundMatches = bracketManager.getCurrentRoundMatches();
        currentMatchIndex = 0;

        List<String> previousWinners = new ArrayList<>(roundWinners);
        roundWinners.clear();

        participants.clear();
        for (String folio : previousWinners) {
            Competitor comp = findCompetitorByFolio(folio);
            if (comp != null) participants.add(comp);
        }

        displayBracket();
        startNextMatch();
    }

    private void finishTournament() {
        String champion = roundWinners.get(0);

        String secondPlace = participants.size() > 1 ? participants.get(1).getFolio() : null;
        String thirdPlace = participants.size() > 2 ? participants.get(2).getFolio() : null;

        currentCompetition.setStatus("COMPLETED");
        currentCompetition.setFirstPlace(champion);
        currentCompetition.setSecondPlace(secondPlace);
        currentCompetition.setThirdPlace(thirdPlace);

        dbHelper.updateCompetition(currentCompetition);

        Competitor winner = findCompetitorByFolio(champion);
        String winnerName = winner != null ? winner.getName() : champion;

        new AlertDialog.Builder(getContext())
                .setTitle("¡Competencia Finalizada!")
                .setMessage("Campeón: " + winnerName + "\n\nLos resultados han sido guardados.")
                .setPositiveButton("Aceptar", (d, w) -> {
                    layoutBracket.removeAllViews();
                    tvCategoryInfo.setVisibility(View.GONE);
                    etCategoryFolio.setText("");
                })
                .show();
    }

    private Competitor findCompetitorByFolio(String folio) {
        for (Competitor c : participants) {
            if (c.getFolio().equals(folio)) return c;
        }
        return null;
    }

    interface ScoreCallback {
        void onScoreCalculated(double score);
    }
}