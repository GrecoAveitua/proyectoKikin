package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.karatecompetitionmanager.models.Match;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StartCompetitionFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etCategoryFolio;
    private Button btnStartCompetition;
    private LinearLayout layoutBracket;
    private TextView tvCategoryInfo, tvEliminationOrder;

    private Category currentCategory;
    private List<Competitor> participants;
    private Competition currentCompetition;
    private String selectedCompetitionType = null;

    // Estructura del bracket
    private List<List<Match>> bracket;
    private int currentRound = 0;
    private List<String> eliminationOrder; // Orden de eliminación (lugares)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_competition, container, false);

        dbHelper = new DatabaseHelper(getContext());
        eliminationOrder = new ArrayList<>();

        etCategoryFolio = view.findViewById(R.id.et_category_folio);
        btnStartCompetition = view.findViewById(R.id.btn_start_competition);
        layoutBracket = view.findViewById(R.id.layout_bracket);
        tvCategoryInfo = view.findViewById(R.id.tv_category_info);
        tvEliminationOrder = view.findViewById(R.id.tv_elimination_order);

        btnStartCompetition.setOnClickListener(v -> startCompetition());

        return view;
    }

    private void startCompetition() {
        String folio = etCategoryFolio.getText().toString().trim();

        if (folio.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese el folio de la categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        currentCategory = dbHelper.getCategory(folio);

        if (currentCategory == null) {
            Toast.makeText(getContext(), "Categoría no encontrada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si la categoría es "both", preguntar qué tipo de competencia
        if (currentCategory.getType().equals("both")) {
            showCompetitionTypeDialog();
        } else {
            selectedCompetitionType = currentCategory.getType();
            continueStartCompetition();
        }
    }

    private void showCompetitionTypeDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_select_competition_type, null);

        RadioGroup rgCompetitionType = dialogView.findViewById(R.id.rg_competition_type);

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar Tipo de Competencia")
                .setMessage("Esta categoría permite Kata y Kumite. Seleccione el tipo:")
                .setView(dialogView)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    int selectedId = rgCompetitionType.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        Toast.makeText(getContext(), "Debe seleccionar un tipo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RadioButton rbSelected = dialogView.findViewById(selectedId);
                    selectedCompetitionType = rbSelected.getText().toString().toLowerCase().equals("kata") ? "kata" : "kumite";
                    continueStartCompetition();
                })
                .setNegativeButton("Cancelar", null)
                .setCancelable(false)
                .show();
    }

    private void continueStartCompetition() {
        loadParticipants();

        if (participants.isEmpty()) {
            Toast.makeText(getContext(), "No hay competidores elegibles", Toast.LENGTH_SHORT).show();
            return;
        }

        if (participants.size() < 2) {
            Toast.makeText(getContext(), "Se requieren al menos 2 competidores", Toast.LENGTH_SHORT).show();
            return;
        }

        displayCategoryInfo();
        initializeBracket();
        displayBracket();

        // Crear registro de competencia
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        currentCompetition = new Competition(0, currentCategory.getFolio(), date, "IN_PROGRESS", null, null, null);
        dbHelper.insertCompetition(currentCompetition);
    }

    private void loadParticipants() {
        participants = new ArrayList<>();
        List<Competitor> allCompetitors = dbHelper.getAllCompetitors("age", true);

        for (Competitor competitor : allCompetitors) {
            if (competitor.getBelt().equals(currentCategory.getBelt()) &&
                    competitor.getAge() >= currentCategory.getMinAge() &&
                    competitor.getAge() <= currentCategory.getMaxAge()) {

                if (selectedCompetitionType.equals("kata") && competitor.isParticipateKata()) {
                    participants.add(competitor);
                } else if (selectedCompetitionType.equals("kumite") && competitor.isParticipateKumite()) {
                    participants.add(competitor);
                }
            }
        }

        // Mezclar aleatoriamente
        Collections.shuffle(participants);
    }

    private void displayCategoryInfo() {
        String type = selectedCompetitionType.equals("kata") ? "Kata" : "Kumite";
        String info = "📋 Categoría: " + currentCategory.getFolio() + "\n" +
                "🥋 Cinturón: " + currentCategory.getBelt() + "\n" +
                "👥 Edad: " + currentCategory.getMinAge() + "-" + currentCategory.getMaxAge() + "\n" +
                "🎯 Tipo: " + type + "\n" +
                "👤 Participantes: " + participants.size();

        tvCategoryInfo.setText(info);
        tvCategoryInfo.setVisibility(View.VISIBLE);
    }

    private void initializeBracket() {
        bracket = new ArrayList<>();
        int numParticipants = participants.size();

        // Calcular número de rondas
        int numRounds = (int) Math.ceil(Math.log(numParticipants) / Math.log(2));

        // Crear primera ronda
        List<Match> firstRound = new ArrayList<>();
        for (int i = 0; i < numParticipants; i += 2) {
            if (i + 1 < numParticipants) {
                firstRound.add(new Match(participants.get(i), participants.get(i + 1)));
            } else {
                // Pasa automáticamente (bye)
                Match byeMatch = new Match(participants.get(i), null);
                byeMatch.winner = participants.get(i);
                firstRound.add(byeMatch);
            }
        }

        bracket.add(firstRound);

        // Crear rondas subsecuentes vacías
        for (int i = 1; i < numRounds; i++) {
            int matchesInRound = (int) Math.pow(2, numRounds - i - 1);
            List<Match> round = new ArrayList<>();
            for (int j = 0; j < matchesInRound; j++) {
                round.add(new Match(null, null));
            }
            bracket.add(round);
        }
    }

    private void displayBracket() {
        layoutBracket.removeAllViews();

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("🏆 BRACKET DE ELIMINACIÓN DIRECTA");
        tvTitle.setTextSize(18);
        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTitle.setTextColor(0xFF1A237E);
        tvTitle.setPadding(0, 20, 0, 20);
        layoutBracket.addView(tvTitle);

        for (int roundIndex = 0; roundIndex < bracket.size(); roundIndex++) {
            List<Match> round = bracket.get(roundIndex);

            // Título de ronda
            TextView tvRoundTitle = new TextView(getContext());
            String roundName = getRoundName(roundIndex, bracket.size());
            tvRoundTitle.setText(roundName);
            tvRoundTitle.setTextSize(16);
            tvRoundTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvRoundTitle.setTextColor(0xFFFF6F00);
            tvRoundTitle.setPadding(0, 16, 0, 8);
            layoutBracket.addView(tvRoundTitle);

            // Mostrar enfrentamientos
            for (int matchIndex = 0; matchIndex < round.size(); matchIndex++) {
                Match match = round.get(matchIndex);
                View matchView = createMatchView(match, roundIndex, matchIndex);
                layoutBracket.addView(matchView);
            }
        }

        // Mostrar orden de eliminación
        updateEliminationOrder();
    }

    private String getRoundName(int roundIndex, int totalRounds) {
        int remaining = totalRounds - roundIndex;
        if (remaining == 1) return "🏆 FINAL";
        if (remaining == 2) return "🥉 SEMIFINALES";
        if (remaining == 3) return "CUARTOS DE FINAL";
        return "RONDA " + (roundIndex + 1);
    }

    private View createMatchView(Match match, int roundIndex, int matchIndex) {
        LinearLayout matchLayout = new LinearLayout(getContext());
        matchLayout.setOrientation(LinearLayout.VERTICAL);
        matchLayout.setPadding(16, 12, 16, 12);
        matchLayout.setBackgroundColor(match.winner != null ? 0xFFE8F5E9 : 0xFFFFFFFF);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        matchLayout.setLayoutParams(params);

        if (match.competitor1 == null && match.competitor2 == null) {
            TextView tvPending = new TextView(getContext());
            tvPending.setText("⏳ Esperando ganador...");
            tvPending.setTextSize(14);
            tvPending.setTextColor(0xFF757575);
            matchLayout.addView(tvPending);
            return matchLayout;
        }

        // Competitor 1
        TextView tvComp1 = new TextView(getContext());
        String comp1Text = match.competitor1 != null ?
                (match.winner == match.competitor1 ? "✓ " : "") + match.competitor1.getName() : "BYE";
        tvComp1.setText(comp1Text);
        tvComp1.setTextSize(15);
        tvComp1.setTextColor(match.winner == match.competitor1 ? 0xFF2E7D32 : 0xFF000000);
        if (match.winner == match.competitor1) tvComp1.setTextColor(0xFF2E7D32);
        matchLayout.addView(tvComp1);

        TextView tvVs = new TextView(getContext());
        tvVs.setText("vs");
        tvVs.setTextSize(12);
        tvVs.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvVs.setTextColor(0xFF757575);
        matchLayout.addView(tvVs);

        // Competitor 2
        TextView tvComp2 = new TextView(getContext());
        String comp2Text = match.competitor2 != null ?
                (match.winner == match.competitor2 ? "✓ " : "") + match.competitor2.getName() : "BYE";
        tvComp2.setText(comp2Text);
        tvComp2.setTextSize(15);
        tvComp2.setTextColor(match.winner == match.competitor2 ? 0xFF2E7D32 : 0xFF000000);
        matchLayout.addView(tvComp2);

        // Click listener solo si el match no tiene ganador
        if (match.winner == null && match.competitor1 != null && match.competitor2 != null) {
            matchLayout.setBackgroundColor(0xFFFFF9C4);
            matchLayout.setOnClickListener(v -> {
                if (selectedCompetitionType.equals("kumite")) {
                    showKumiteMatchDialog(match, roundIndex, matchIndex);
                } else {
                    showKataMatchDialog(match, roundIndex, matchIndex);
                }
            });
        }

        return matchLayout;
    }

    private void showKumiteMatchDialog(Match match, int roundIndex, int matchIndex) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_kumite_bracket, null);

        TextView tvComp1 = dialogView.findViewById(R.id.tv_kumite_comp1);
        TextView tvComp2 = dialogView.findViewById(R.id.tv_kumite_comp2);
        TextView tvScore1 = dialogView.findViewById(R.id.tv_kumite_score1);
        TextView tvScore2 = dialogView.findViewById(R.id.tv_kumite_score2);
        TextView tvTimer = dialogView.findViewById(R.id.tv_kumite_timer);

        Button btnIppon1 = dialogView.findViewById(R.id.btn_ippon1);
        Button btnWazari1 = dialogView.findViewById(R.id.btn_wazari1);
        Button btnYuko1 = dialogView.findViewById(R.id.btn_yuko1);
        CheckBox cbFoul1_1 = dialogView.findViewById(R.id.cb_foul1_1);
        CheckBox cbFoul1_2 = dialogView.findViewById(R.id.cb_foul1_2);
        CheckBox cbFoul1_3 = dialogView.findViewById(R.id.cb_foul1_3);
        CheckBox cbFoul1_4 = dialogView.findViewById(R.id.cb_foul1_4);

        Button btnIppon2 = dialogView.findViewById(R.id.btn_ippon2);
        Button btnWazari2 = dialogView.findViewById(R.id.btn_wazari2);
        Button btnYuko2 = dialogView.findViewById(R.id.btn_yuko2);
        CheckBox cbFoul2_1 = dialogView.findViewById(R.id.cb_foul2_1);
        CheckBox cbFoul2_2 = dialogView.findViewById(R.id.cb_foul2_2);
        CheckBox cbFoul2_3 = dialogView.findViewById(R.id.cb_foul2_3);
        CheckBox cbFoul2_4 = dialogView.findViewById(R.id.cb_foul2_4);

        Button btnStartStop = dialogView.findViewById(R.id.btn_start_stop);
        Button btnFinish = dialogView.findViewById(R.id.btn_finish_match);

        tvComp1.setText(match.competitor1.getName());
        tvComp2.setText(match.competitor2.getName());

        final int[] score1 = {0};
        final int[] score2 = {0};
        final long[] timeLeft = {120000}; // 2 minutos
        final boolean[] timerRunning = {false};
        final Handler handler = new Handler();

        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timerRunning[0] && timeLeft[0] > 0) {
                    timeLeft[0] -= 100;
                    int minutes = (int) (timeLeft[0] / 60000);
                    int seconds = (int) ((timeLeft[0] % 60000) / 1000);
                    tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 100);

                    if (timeLeft[0] <= 0) {
                        timerRunning[0] = false;
                    }
                }
            }
        };

        // Botones de puntuación
        btnIppon1.setOnClickListener(v -> {
            score1[0] += 3;
            tvScore1.setText(String.valueOf(score1[0]));
        });

        btnWazari1.setOnClickListener(v -> {
            score1[0] += 2;
            tvScore1.setText(String.valueOf(score1[0]));
        });

        btnYuko1.setOnClickListener(v -> {
            score1[0] += 1;
            tvScore1.setText(String.valueOf(score1[0]));
        });

        btnIppon2.setOnClickListener(v -> {
            score2[0] += 3;
            tvScore2.setText(String.valueOf(score2[0]));
        });

        btnWazari2.setOnClickListener(v -> {
            score2[0] += 2;
            tvScore2.setText(String.valueOf(score2[0]));
        });

        btnYuko2.setOnClickListener(v -> {
            score2[0] += 1;
            tvScore2.setText(String.valueOf(score2[0]));
        });

        // Control de faltas
        CheckBox.OnCheckedChangeListener foulListener1 = (buttonView, isChecked) -> {
            int fouls = 0;
            if (cbFoul1_1.isChecked()) fouls++;
            if (cbFoul1_2.isChecked()) fouls++;
            if (cbFoul1_3.isChecked()) fouls++;
            if (cbFoul1_4.isChecked()) fouls++;
            if (fouls >= 4) {
                Toast.makeText(getContext(), match.competitor1.getName() + " descalificado!", Toast.LENGTH_SHORT).show();
            }
        };

        cbFoul1_1.setOnCheckedChangeListener(foulListener1);
        cbFoul1_2.setOnCheckedChangeListener(foulListener1);
        cbFoul1_3.setOnCheckedChangeListener(foulListener1);
        cbFoul1_4.setOnCheckedChangeListener(foulListener1);

        CheckBox.OnCheckedChangeListener foulListener2 = (buttonView, isChecked) -> {
            int fouls = 0;
            if (cbFoul2_1.isChecked()) fouls++;
            if (cbFoul2_2.isChecked()) fouls++;
            if (cbFoul2_3.isChecked()) fouls++;
            if (cbFoul2_4.isChecked()) fouls++;
            if (fouls >= 4) {
                Toast.makeText(getContext(), match.competitor2.getName() + " descalificado!", Toast.LENGTH_SHORT).show();
            }
        };

        cbFoul2_1.setOnCheckedChangeListener(foulListener2);
        cbFoul2_2.setOnCheckedChangeListener(foulListener2);
        cbFoul2_3.setOnCheckedChangeListener(foulListener2);
        cbFoul2_4.setOnCheckedChangeListener(foulListener2);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Combate Kumite")
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnStartStop.setOnClickListener(v -> {
            if (!timerRunning[0]) {
                timerRunning[0] = true;
                handler.post(timerRunnable);
                btnStartStop.setText("Pausar");
            } else {
                timerRunning[0] = false;
                handler.removeCallbacks(timerRunnable);
                btnStartStop.setText("Reanudar");
            }
        });

        btnFinish.setOnClickListener(v -> {
            handler.removeCallbacks(timerRunnable);

            // Verificar descalificación
            int fouls1 = (cbFoul1_1.isChecked() ? 1 : 0) + (cbFoul1_2.isChecked() ? 1 : 0) +
                    (cbFoul1_3.isChecked() ? 1 : 0) + (cbFoul1_4.isChecked() ? 1 : 0);
            int fouls2 = (cbFoul2_1.isChecked() ? 1 : 0) + (cbFoul2_2.isChecked() ? 1 : 0) +
                    (cbFoul2_3.isChecked() ? 1 : 0) + (cbFoul2_4.isChecked() ? 1 : 0);

            Competitor winner;
            Competitor loser;

            if (fouls1 >= 4) {
                winner = match.competitor2;
                loser = match.competitor1;
            } else if (fouls2 >= 4) {
                winner = match.competitor1;
                loser = match.competitor2;
            } else if (score1[0] > score2[0]) {
                winner = match.competitor1;
                loser = match.competitor2;
            } else if (score2[0] > score1[0]) {
                winner = match.competitor2;
                loser = match.competitor1;
            } else {
                Toast.makeText(getContext(), "Empate - Realice desempate", Toast.LENGTH_SHORT).show();
                return;
            }

            finishMatch(match, winner, loser, roundIndex, matchIndex);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showKataMatchDialog(Match match, int roundIndex, int matchIndex) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_kata_bracket, null);

        TextView tvComp1 = dialogView.findViewById(R.id.tv_kata_comp1);
        TextView tvComp2 = dialogView.findViewById(R.id.tv_kata_comp2);
        Button btnScore1 = dialogView.findViewById(R.id.btn_score_comp1);
        Button btnScore2 = dialogView.findViewById(R.id.btn_score_comp2);
        EditText etJudges = dialogView.findViewById(R.id.et_num_judges);

        tvComp1.setText(match.competitor1.getName());
        tvComp2.setText(match.competitor2.getName());

        final double[] finalScore1 = {0};
        final double[] finalScore2 = {0};

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Competencia de Kata")
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnScore1.setOnClickListener(v -> {
            String judgesStr = etJudges.getText().toString().trim();
            if (judgesStr.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese número de jueces", Toast.LENGTH_SHORT).show();
                return;
            }

            int numJudges = Integer.parseInt(judgesStr);
            if (numJudges != 3 && numJudges != 5 && numJudges != 7) {
                Toast.makeText(getContext(), "Solo 3, 5 o 7 jueces", Toast.LENGTH_SHORT).show();
                return;
            }

            showJudgeScoresDialog(match.competitor1, numJudges, score -> {
                finalScore1[0] = score;
                btnScore1.setText("✓ " + String.format("%.2f", score));
                btnScore1.setEnabled(false);

                if (finalScore2[0] > 0) {
                    determineKataWinner(match, finalScore1[0], finalScore2[0], roundIndex, matchIndex, dialog);
                }
            });
        });

        btnScore2.setOnClickListener(v -> {
            String judgesStr = etJudges.getText().toString().trim();
            if (judgesStr.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese número de jueces", Toast.LENGTH_SHORT).show();
                return;
            }

            int numJudges = Integer.parseInt(judgesStr);
            if (numJudges != 3 && numJudges != 5 && numJudges != 7) {
                Toast.makeText(getContext(), "Solo 3, 5 o 7 jueces", Toast.LENGTH_SHORT).show();
                return;
            }

            showJudgeScoresDialog(match.competitor2, numJudges, score -> {
                finalScore2[0] = score;
                btnScore2.setText("✓ " + String.format("%.2f", score));
                btnScore2.setEnabled(false);

                if (finalScore1[0] > 0) {
                    determineKataWinner(match, finalScore1[0], finalScore2[0], roundIndex, matchIndex, dialog);
                }
            });
        });

        dialog.show();
    }

    private void showJudgeScoresDialog(Competitor competitor, int numJudges, ScoreCallback callback) {
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
                        double finalScore = calculateKataScore(scores);
                        callback.onScoreCalculated(finalScore);
                    } else {
                        Toast.makeText(getContext(), "Complete todas las calificaciones", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private double calculateKataScore(List<Double> scores) {
        if (scores.size() <= 3) {
            // Con 3 jueces, promediar todo
            double sum = 0;
            for (double score : scores) sum += score;
            return sum / scores.size();
        } else {
            // Con 5 o 7 jueces, eliminar máximo y mínimo
            List<Double> sortedScores = new ArrayList<>(scores);
            Collections.sort(sortedScores);
            sortedScores.remove(0); // Eliminar mínimo
            sortedScores.remove(sortedScores.size() - 1); // Eliminar máximo

            double sum = 0;
            for (double score : sortedScores) sum += score;
            return sum / sortedScores.size();
        }
    }

    private void determineKataWinner(Match match, double score1, double score2,
                                     int roundIndex, int matchIndex, AlertDialog dialog) {
        Competitor winner, loser;

        if (score1 > score2) {
            winner = match.competitor1;
            loser = match.competitor2;
            Toast.makeText(getContext(), winner.getName() + " gana: " +
                    String.format("%.2f vs %.2f", score1, score2), Toast.LENGTH_LONG).show();
        } else if (score2 > score1) {
            winner = match.competitor2;
            loser = match.competitor1;
            Toast.makeText(getContext(), winner.getName() + " gana: " +
                    String.format("%.2f vs %.2f", score2, score1), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Empate - Realice desempate", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            showKataMatchDialog(match, roundIndex, matchIndex);
            return;
        }

        finishMatch(match, winner, loser, roundIndex, matchIndex);
        dialog.dismiss();
    }

    private void finishMatch(Match match, Competitor winner, Competitor loser,
                             int roundIndex, int matchIndex) {
        match.winner = winner;

        // Agregar perdedor al orden de eliminación
        eliminationOrder.add(0, loser.getName() + " (" + loser.getFolio() + ")");

        // Avanzar ganador a la siguiente ronda
        if (roundIndex + 1 < bracket.size()) {
            int nextMatchIndex = matchIndex / 2;
            Match nextMatch = bracket.get(roundIndex + 1).get(nextMatchIndex);

            if (matchIndex % 2 == 0) {
                nextMatch.competitor1 = winner;
            } else {
                nextMatch.competitor2 = winner;
            }
        }

        // Verificar si es la final
        if (roundIndex == bracket.size() - 1) {
            finishTournament(winner, loser);
        } else {
            displayBracket();
        }
    }

    private void finishTournament(Competitor champion, Competitor runnerUp) {
        // El orden ya tiene a todos los eliminados
        // Agregar subcampeón y campeón
        eliminationOrder.add(0, "🥈 2do Lugar: " + runnerUp.getName() + " (" + runnerUp.getFolio() + ")");
        eliminationOrder.add(0, "🥇 1er Lugar (CAMPEÓN): " + champion.getName() + " (" + champion.getFolio() + ")");

        // Determinar tercer lugar (último eliminado antes de la final)
        String thirdPlace = null;
        if (eliminationOrder.size() > 2) {
            thirdPlace = eliminationOrder.get(2);
        }

        // Actualizar competencia en base de datos
        currentCompetition.setStatus("COMPLETED");
        currentCompetition.setFirstPlace(champion.getFolio());
        currentCompetition.setSecondPlace(runnerUp.getFolio());
        if (thirdPlace != null && thirdPlace.contains("(")) {
            String thirdFolio = thirdPlace.substring(thirdPlace.indexOf("(") + 1, thirdPlace.indexOf(")"));
            currentCompetition.setThirdPlace(thirdFolio);
        }

        dbHelper.updateCompetition(currentCompetition);

        // Mostrar resultados finales
        StringBuilder results = new StringBuilder();
        results.append("🏆 COMPETENCIA FINALIZADA 🏆\n\n");
        results.append("Categoría: ").append(currentCategory.getFolio()).append("\n");
        results.append("Tipo: ").append(selectedCompetitionType.equals("kata") ? "Kata" : "Kumite").append("\n\n");
        results.append("RESULTADOS FINALES:\n\n");

        for (int i = 0; i < Math.min(3, eliminationOrder.size()); i++) {
            results.append(eliminationOrder.get(i)).append("\n");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("¡Competencia Finalizada!")
                .setMessage(results.toString())
                .setPositiveButton("Aceptar", (d, w) -> {
                    // Limpiar vista
                    layoutBracket.removeAllViews();
                    tvCategoryInfo.setVisibility(View.GONE);
                    tvEliminationOrder.setVisibility(View.GONE);
                    etCategoryFolio.setText("");
                    selectedCompetitionType = null;
                    bracket = null;
                    eliminationOrder.clear();
                })
                .setCancelable(false)
                .show();
    }

    private void updateEliminationOrder() {
        if (eliminationOrder.isEmpty()) {
            tvEliminationOrder.setVisibility(View.GONE);
            return;
        }

        StringBuilder orderText = new StringBuilder();
        orderText.append("Resultados actuales:\n\n");

        for (int i = 0; i < eliminationOrder.size(); i++) {
            orderText.append((i + 1)).append(". ").append(eliminationOrder.get(i)).append("\n");
        }

        tvEliminationOrder.setText(orderText.toString());
        tvEliminationOrder.setVisibility(View.VISIBLE);
    }

    interface ScoreCallback {
        void onScoreCalculated(double score);
    }
}