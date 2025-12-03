package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.adapters.CompetitionAdapter;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Category;
import com.example.karatecompetitionmanager.models.Competition;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.List;

public class HistoryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private TextView tvNoData;
    private TextView tvTotalCompetitions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inicializar base de datos
        dbHelper = new DatabaseHelper(getContext());

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.recycler_history);
        tvNoData = view.findViewById(R.id.tv_no_data);
        tvTotalCompetitions = view.findViewById(R.id.tv_total_competitions);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Cargar competencias
        loadCompletedCompetitions();

        return view;
    }

    private void loadCompletedCompetitions() {
        List<Competition> competitions = dbHelper.getCompletedCompetitions();

        if (competitions.isEmpty()) {
            // No hay competencias completadas
            showNoData();
        } else {
            // Hay competencias para mostrar
            showCompetitions(competitions);
        }
    }

    private void showNoData() {
        tvNoData.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvTotalCompetitions.setVisibility(View.GONE);
    }

    private void showCompetitions(List<Competition> competitions) {
        tvNoData.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvTotalCompetitions.setVisibility(View.VISIBLE);

        // Mostrar total de competencias
        tvTotalCompetitions.setText("Total de competencias: " + competitions.size());

        // Configurar adaptador con listener para clics
        adapter = new CompetitionAdapter(competitions, dbHelper, new CompetitionAdapter.OnCompetitionClickListener() {
            @Override
            public void onCompetitionClick(Competition competition) {
                showCompetitionDetails(competition);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void showCompetitionDetails(Competition competition) {
        // Obtener detalles completos
        Category category = dbHelper.getCategory(competition.getCategoryFolio());

        StringBuilder details = new StringBuilder();
        details.append("DETALLES DE LA COMPETENCIA\n\n");

        // Información de categoría
        if (category != null) {
            details.append("📋 CATEGORÍA\n");
            details.append("Folio: ").append(category.getFolio()).append("\n");
            details.append("Cinturón: ").append(category.getBelt()).append("\n");
            details.append("Edad: ").append(category.getMinAge()).append(" - ")
                    .append(category.getMaxAge()).append(" años\n");

            String type = category.getType().equals("kata") ? "Kata" :
                    (category.getType().equals("kumite") ? "Kumite" : "Kata y Kumite");
            details.append("Tipo: ").append(type).append("\n\n");
        }

        // Fecha
        details.append("📅 FECHA\n");
        details.append(competition.getDate()).append("\n\n");

        // Resultados
        details.append("🏆 RESULTADOS\n\n");

        // Primer lugar
        if (competition.getFirstPlace() != null) {
            Competitor first = dbHelper.getCompetitor(competition.getFirstPlace());
            if (first != null) {
                details.append("🥇 PRIMER LUGAR\n");
                details.append("Nombre: ").append(first.getName()).append("\n");
                details.append("Dojo: ").append(first.getDojo()).append("\n");
                details.append("Folio: ").append(first.getFolio()).append("\n\n");
            }
        }

        // Segundo lugar
        if (competition.getSecondPlace() != null) {
            Competitor second = dbHelper.getCompetitor(competition.getSecondPlace());
            if (second != null) {
                details.append("🥈 SEGUNDO LUGAR\n");
                details.append("Nombre: ").append(second.getName()).append("\n");
                details.append("Dojo: ").append(second.getDojo()).append("\n");
                details.append("Folio: ").append(second.getFolio()).append("\n\n");
            }
        }

        // Tercer lugar
        if (competition.getThirdPlace() != null) {
            Competitor third = dbHelper.getCompetitor(competition.getThirdPlace());
            if (third != null) {
                details.append("🥉 TERCER LUGAR\n");
                details.append("Nombre: ").append(third.getName()).append("\n");
                details.append("Dojo: ").append(third.getDojo()).append("\n");
                details.append("Folio: ").append(third.getFolio()).append("\n");
            }
        }

        // Mostrar diálogo con detalles
        new AlertDialog.Builder(getContext())
                .setTitle("Detalles de la Competencia")
                .setMessage(details.toString())
                .setPositiveButton("Cerrar", null)
                .setNeutralButton("Compartir", (dialog, which) -> {
                    shareCompetitionResults(details.toString());
                })
                .show();
    }

    private void shareCompetitionResults(String results) {
        // Funcionalidad para compartir resultados (opcional)
        Toast.makeText(getContext(),
                "Función de compartir no implementada",
                Toast.LENGTH_SHORT).show();

        // Para implementar compartir:
        /*
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, results);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Resultados de Competencia de Karate");
        startActivity(Intent.createChooser(shareIntent, "Compartir resultados"));
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar competencias cuando el fragment se vuelve visible
        loadCompletedCompetitions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar recursos
        if (adapter != null) {
            adapter = null;
        }
    }
}